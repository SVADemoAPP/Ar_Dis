package com.huawei.hiardemo.java.fragment;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;


import com.huawei.hiar.ARAnchor;
import com.huawei.hiar.ARCamera;
import com.huawei.hiar.ARConfigBase;
import com.huawei.hiar.AREnginesApk;
import com.huawei.hiar.AREnginesSelector;
import com.huawei.hiar.ARFrame;
import com.huawei.hiar.ARHitResult;
import com.huawei.hiar.ARLightEstimate;
import com.huawei.hiar.ARPlane;
import com.huawei.hiar.ARPoint;
import com.huawei.hiar.ARPointCloud;
import com.huawei.hiar.ARPose;
import com.huawei.hiar.ARSession;
import com.huawei.hiar.ARTrackable;
import com.huawei.hiar.ARWorldTrackingConfig;
import com.huawei.hiar.exceptions.ARUnSupportedConfigurationException;
import com.huawei.hiar.exceptions.ARUnavailableClientSdkTooOldException;
import com.huawei.hiar.exceptions.ARUnavailableDeviceNotCompatibleException;
import com.huawei.hiar.exceptions.ARUnavailableEmuiNotCompatibleException;
import com.huawei.hiar.exceptions.ARUnavailableServiceApkTooOldException;
import com.huawei.hiar.exceptions.ARUnavailableServiceNotInstalledException;
import com.huawei.hiar.exceptions.ARUnavailableUserDeclinedInstallationException;
import com.huawei.hiardemo.java.DisplayRotationHelper;
import com.huawei.hiardemo.java.MainActivity;
import com.huawei.hiardemo.java.R;
import com.huawei.hiardemo.java.UtilsCommon;
import com.huawei.hiardemo.java.framework.sharef.CameraPermissionHelper;
import com.huawei.hiardemo.java.rendering.BackgroundRenderer;
import com.huawei.hiardemo.java.rendering.PlaneRenderer;
import com.huawei.hiardemo.java.rendering.PointCloudRenderer;
import com.huawei.hiardemo.java.rendering.VirtualObjectRenderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ARFragment extends Fragment implements GLSurfaceView.Renderer {


    private ARPose mArPose;  //摄像头位置的实时坐标
    private ArCameraListener mListener;  //监听器
    private long intervalTime = 1000;
    private Handler mGetArPoseHandler = new Handler();
    private boolean isFirstFlag = false;
    private Runnable mGetArPosRunnable = new Runnable() {  //定时获取坐标
        @Override
        public void run() {
            mGetArPoseHandler.postDelayed(this, intervalTime);
            mListener.getCameraPose(mArPose);
        }
    };
    private boolean hasPlane = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mArView = inflater.inflate(R.layout.activity_main, container, false);

        mFpsTextView = mArView.findViewById(R.id.fpsTextView);
        mSearchingTextView = mArView.findViewById(R.id.searchingTextView);
        mSurfaceView = mArView.findViewById(R.id.surfaceview);
        mDisplayRotationHelper = new DisplayRotationHelper(getContext());

        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                onSingleTap(e);
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });
        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });

        // Set up renderer.
        mSurfaceView.setPreserveEGLContextOnPause(true);
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        mSurfaceView.setRenderer(this);
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        installRequested = false;

        return mArView;
    }

    private static final String TAG = MainActivity.class.getSimpleName();
    private ARSession mSession;
    private GLSurfaceView mSurfaceView;
    private GestureDetector mGestureDetector;
    private DisplayRotationHelper mDisplayRotationHelper;

    private BackgroundRenderer mBackgroundRenderer = new BackgroundRenderer();
    private VirtualObjectRenderer mVirtualObject = new VirtualObjectRenderer();
    private PlaneRenderer mPlaneRenderer = new PlaneRenderer();
    private PointCloudRenderer mPointCloud = new PointCloudRenderer();

    private final float[] mAnchorMatrix = new float[UtilsCommon.MATRIX_NUM];
    private static final float[] DEFAULT_COLOR = new float[]{0f, 0f, 0f, 0f};

    // Anchors created from taps used for object placing with a given color.
    private static class ColoredARAnchor {
        public final ARAnchor anchor;
        public final float[] color;

        public ColoredARAnchor(ARAnchor a, float[] color4f) {
            this.anchor = a;
            this.color = color4f;
        }
    }

    private ArrayBlockingQueue<MotionEvent> mQueuedSingleTaps = new ArrayBlockingQueue<>(2);
    private ArrayList<ColoredARAnchor> mAnchors = new ArrayList<>();

    private float mScaleFactor = 0.15f;
    private boolean installRequested;
    private float updateInterval = 0.5f;
    private long lastInterval;
    private int frames = 0;
    private float fps;
    private TextView mFpsTextView;
    private TextView mSearchingTextView;

    public ARPose getArPose() {
        return mArPose;
    }

    @Override
    public void onResume() {
        super.onResume();
        Exception exception = null;
        String message = null;
        if (null == mSession) {
            try {
                //If you do not want to switch engines, AREnginesSelector is useless.
                // You just need to use AREnginesApk.requestInstall() and the default engine
                // is Huawei AR Engine.
                AREnginesSelector.AREnginesAvaliblity enginesAvaliblity = AREnginesSelector.checkAllAvailableEngines(getContext());
                if ((enginesAvaliblity.ordinal() &
                        AREnginesSelector.AREnginesAvaliblity.HWAR_ENGINE_SUPPORTED.ordinal()) != 0) {

                    AREnginesSelector.setAREngine(AREnginesSelector.AREnginesType.HWAR_ENGINE);

                    switch (AREnginesApk.requestInstall(getActivity(), !installRequested)) {
                        case INSTALL_REQUESTED:
                            installRequested = true;
                            return;
                        case INSTALLED:
                            break;
                    }

                    if (!CameraPermissionHelper.hasPermission(getActivity())) {
                        CameraPermissionHelper.requestPermission(getActivity());
                        return;
                    }

                    mSession = new ARSession(/*context=*/getContext());
                    ARConfigBase config = new ARWorldTrackingConfig(mSession);
                    mSession.configure(config);
                } else {
                    message = "This device does not support Huawei AR Engine ";
                }
            } catch (ARUnavailableServiceNotInstalledException e) {
                message = "Please install HuaweiARService.apk";
                exception = e;
            } catch (ARUnavailableServiceApkTooOldException e) {
                message = "Please update HuaweiARService.apk";
                exception = e;
            } catch (ARUnavailableClientSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (ARUnavailableDeviceNotCompatibleException e) {
                message = "This device does not support Huawei AR Engine ";
                exception = e;
            } catch (ARUnavailableEmuiNotCompatibleException e) {
                message = "Please update EMUI version";
                exception = e;
            } catch (ARUnavailableUserDeclinedInstallationException e) {
                message = "Please agree to install!";
                exception = e;
            } catch (ARUnSupportedConfigurationException e) {
                message = "The configuration is not supported by the device!";
                exception = e;
            } catch (Exception e) {
                message = "exception throwed";
                exception = e;
            }
            if (message != null) {
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Creating sesson", exception);
                if (mSession != null) {
                    mSession.stop();
                    mSession = null;
                }
                return;
            }
        }

        mSession.resume();
        mSurfaceView.onResume();
        mDisplayRotationHelper.onResume();
        lastInterval = System.currentTimeMillis();
    }

    @Override
    public void onPause() {

        super.onPause();
        if (mSession != null) {
            mDisplayRotationHelper.onPause();
            mSurfaceView.onPause();
            mSession.pause();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (!CameraPermissionHelper.hasPermission(getActivity())) {
            Toast.makeText(getActivity(),
                    "This application needs camera permission.", Toast.LENGTH_LONG).show();

            getActivity().finish();
        }
    }


    private void onSingleTap(MotionEvent e) {
        mQueuedSingleTaps.offer(e);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        mBackgroundRenderer.createOnGlThread(/*context=*/getActivity());

        try {
            mVirtualObject.createOnGlThread(/*context=*/getActivity(), "AR_logo.obj", "AR_logo.png");
            mVirtualObject.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f);

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Failed to read plane texture");
        }
        try {
            mPlaneRenderer.createOnGlThread(/*context=*/getActivity(), "trigrid.png");
        } catch (IOException e) {
            Log.e(TAG, "Failed to read plane texture");
        }

        mPointCloud.createOnGlThread(/*context=*/getActivity());
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mDisplayRotationHelper.onSurfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        showFpsTextView(String.valueOf(FPSCalculate()));
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (null == mSession) {
            return;
        }
        mDisplayRotationHelper.updateSessionIfNeeded(mSession);

        try {
            mSession.setCameraTextureName(mBackgroundRenderer.getTextureId());
            ARFrame frame = mSession.update();
            ARCamera camera = frame.getCamera();
            mArPose = camera.getDisplayOrientedPose();  //获取摄像头位置（即为定位位置）
            if (!isFirstFlag) {  //判断第一次进入
                if (hasPlane) { //开始定位的标志
                    isFirstFlag = true;
                    Log.e("XHF", "定位开始");
                    mGetArPoseHandler.post(mGetArPosRunnable);
                }
            }
            handleTap(frame, camera);

            mBackgroundRenderer.draw(frame);

            if (camera.getTrackingState() == ARTrackable.TrackingState.PAUSED) {
                return;
            }

            float[] projmtx = new float[UtilsCommon.MATRIX_NUM];
            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

            float[] viewmtx = new float[UtilsCommon.MATRIX_NUM];
            camera.getViewMatrix(viewmtx, 0);

            ARLightEstimate le = frame.getLightEstimate();
            float lightIntensity = 1;
            if (le.getState() != ARLightEstimate.State.NOT_VALID) {
                lightIntensity = le.getPixelIntensity();
            }
            ARPointCloud arPointCloud = frame.acquirePointCloud();
            mPointCloud.update(arPointCloud);
            mPointCloud.draw(viewmtx, projmtx);
            arPointCloud.release();

            if (mSearchingTextView != null) {
                for (ARPlane plane : mSession.getAllTrackables(ARPlane.class)) {
                    if (plane.getType() != ARPlane.PlaneType.UNKNOWN_FACING &&
                            plane.getTrackingState() == ARTrackable.TrackingState.TRACKING) {
                        hideLoadingMessage();
                        break;
                    }
                }
            }
            mPlaneRenderer.drawPlanes(mSession.getAllTrackables(ARPlane.class), camera.getDisplayOrientedPose(), projmtx);

            Iterator<ColoredARAnchor> ite = mAnchors.iterator();
            while (ite.hasNext()) {
                ColoredARAnchor coloredAnchor = ite.next();
                if (coloredAnchor.anchor.getTrackingState() == ARTrackable.TrackingState.STOPPED) {
                    ite.remove();
                } else if (coloredAnchor.anchor.getTrackingState() == ARTrackable.TrackingState.TRACKING) {
                    coloredAnchor.anchor.getPose().toMatrix(mAnchorMatrix, 0);
                    mVirtualObject.updateModelMatrix(mAnchorMatrix, mScaleFactor);
                    mVirtualObject.draw(viewmtx, projmtx, lightIntensity, coloredAnchor.color);
                }
            }

        } catch (Throwable t) {
            Log.e(TAG, "Exception on the OpenGL thread", t);
        }
    }


    private void hideLoadingMessage() {
        hasPlane = true;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSearchingTextView != null) {
                    mSearchingTextView.setVisibility(View.GONE);
                    mSearchingTextView = null;
                }
            }
        });
    }


    @Override
    public void onDestroy() {
        if (mSession != null) {
            mSession.stop();
            mSession = null;
        }
        super.onDestroy();
    }

    private void showFpsTextView(final String text) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFpsTextView.setTextColor(Color.RED);
                mFpsTextView.setTextSize(15f);
                if (text != null) {
                    mFpsTextView.setText(text);
                } else {
                    mFpsTextView.setText("");
                }
            }
        });
    }

    float FPSCalculate() {
        ++frames;
        long timeNow = System.currentTimeMillis();
        if (((timeNow - lastInterval) / 1000) > updateInterval) {
            fps = (frames / ((timeNow - lastInterval) / 1000.0f));
            frames = 0;
            lastInterval = timeNow;
        }
        return fps;
    }

    // Handle only one tap per frame, as taps are usually low frequency compared to frame rate.
    private void handleTap(ARFrame frame, ARCamera camera) {
        MotionEvent tap = mQueuedSingleTaps.poll();

        if (tap != null && camera.getTrackingState() == ARTrackable.TrackingState.TRACKING) {
            ARHitResult hitResult = null;
            ARTrackable trackable = null;
            boolean hasHitFlag = false;

            List<ARHitResult> hitTestResult = frame.hitTest(tap);
            for (int i = 0; i < hitTestResult.size(); i++) {
                // Check if any plane was hit, and if it was hit inside the plane polygon
                ARHitResult hitResultTemp = hitTestResult.get(i);
                trackable = hitResultTemp.getTrackable();
                if ((trackable instanceof ARPlane
                        && ((ARPlane) trackable).isPoseInPolygon(hitResultTemp.getHitPose())
                        && (PlaneRenderer.calculateDistanceToPlane(hitResultTemp.getHitPose(), camera.getPose()) > 0))
                        || (trackable instanceof ARPoint
                        && ((ARPoint) trackable).getOrientationMode() == ARPoint.OrientationMode.ESTIMATED_SURFACE_NORMAL)) {
                    hasHitFlag = true;
                    hitResult = hitResultTemp;
                }

                if (trackable instanceof ARPlane) {
                    break;
                }
            }

            //if hit both Plane and Point,take Plane at the first priority.
            if (hasHitFlag != true) {
                return;
            }

            // Hits are sorted by depth. Consider only closest hit on a plane or oriented point.
            // Cap the number of objects created. This avoids overloading both the
            // rendering system and ARCore.
            if (mAnchors.size() >= UtilsCommon.MAX_TRACKING_ANCHOR_NUM) {
                mAnchors.get(0).anchor.detach();
                mAnchors.remove(0);
            }

            // Assign a color to the object for rendering based on the trackable type
            // this anchor attached to. For AR_TRACKABLE_POINT, it's blue color, and
            // for AR_TRACKABLE_PLANE, it's green color.
            float[] objColor;
            trackable = hitResult.getTrackable();
            if (trackable instanceof ARPoint) {
                objColor = new float[]{66.0f, 133.0f, 244.0f, 255.0f};
            } else if (trackable instanceof ARPlane) {
                objColor = new float[]{139.0f, 195.0f, 74.0f, 255.0f};
            } else {
                objColor = DEFAULT_COLOR;
            }

            // Adding an Anchor tells ARCore that it should track this position in
            // space. This anchor is created on the Plane to place the 3D model
            // in the correct position relative both to the world and to the plane.
            mAnchors.add(new ColoredARAnchor(hitResult.createAnchor(), objColor));   //添加锚点
        }
    }

    public void setArCameraListener(ArCameraListener arCameraListener) {
        mListener = arCameraListener;
    }

    public interface ArCameraListener {
        void getCameraPose(ARPose arPose);
    }
}