package com.huawei.hiardemo.java.fragment;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hiar.ARAnchor;
import com.huawei.hiar.ARCamera;
import com.huawei.hiar.ARConfigBase;
import com.huawei.hiar.AREnginesApk;
import com.huawei.hiar.AREnginesSelector;
import com.huawei.hiar.ARFrame;
import com.huawei.hiar.ARLightEstimate;
import com.huawei.hiar.ARPlane;
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
import com.huawei.hiardemo.java.ShareMapHelper;
import com.huawei.hiardemo.java.UtilsCommon;
import com.huawei.hiardemo.java.activity.FloorMapActivity;
import com.huawei.hiardemo.java.framework.sharef.CameraPermissionHelper;
import com.huawei.hiardemo.java.framework.utils.DateUtil;
import com.huawei.hiardemo.java.rendering.BackgroundRenderer;
import com.huawei.hiardemo.java.rendering.PlaneRenderer;
import com.huawei.hiardemo.java.rendering.PointCloudRenderer;
import com.huawei.hiardemo.java.rendering.VirtualObjectRenderer;
import com.huawei.hiardemo.java.util.Constant;
import com.huawei.hiardemo.java.util.LogUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ARFragment extends Fragment implements GLSurfaceView.Renderer {


    private ARPose mArPose;  //摄像头位置的实时坐标
    private ArCameraListener mListener;  //监听器
    private long intervalTime = 2000;
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
    private boolean hasplat = false;
    private boolean initARFlag = false;
    private View mTvRsrp;
    private View mTvcode;
    private TextView mPrruNeCode;
    private TextView mPrruRsrp;


    /**
     * 传感器参数
     */
    private SensorManager mSensorManager;
    private Sensor accelerometer; // 加速度传感器
    private Sensor magnetic; // 地磁场传感器
    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];

    private float azimuthAngle;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mArView = inflater.inflate(R.layout.activity_main, container, false);
        mTvRsrp = mArView.findViewById(R.id.rsrp);
        mTvcode = mArView.findViewById(R.id.necode);
        mPrruNeCode = mArView.findViewById(R.id.prru_ne_code);
        mPrruRsrp = mArView.findViewById(R.id.prruRsrp);
        mSearchingTextView = mArView.findViewById(R.id.searchingTextView);
        mSurfaceView = mArView.findViewById(R.id.surfaceview);
        mDisplayRotationHelper = new DisplayRotationHelper(getActivity());
        mGestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
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


        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        // 初始化加速度传感器
        accelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // 初始化地磁场传感器
        magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        calculateOrientation();
        return mArView;
    }

    private void setView(final float azimuthAngle) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPrruNeCode.setText(azimuthAngle + "");

            }
        });
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
//    private static class ColoredARAnchor {
//        public final ARAnchor anchor;
//        public final float[] color;
//
//        public ColoredARAnchor(ARAnchor a, float[] color4f) {
//            this.anchor = a;
//            this.color = color4f;
//        }
//    }

    private ArrayBlockingQueue<MotionEvent> mQueuedSingleTaps = new ArrayBlockingQueue<>(2);
    private List<ARAnchor> mAnchors = new ArrayList<>();
    private ARFrame mFrame;

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
        createSession();
        mSession.resume();
        mSurfaceView.onResume();
        mDisplayRotationHelper.onResume();
        lastInterval = System.currentTimeMillis();

        // 注册监听
        mSensorManager.registerListener(new MySensorEventListener(),
                accelerometer, Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(new MySensorEventListener(), magnetic,
                Sensor.TYPE_MAGNETIC_FIELD);
    }

    public void createSession() {
        Exception exception = null;
        String message = null;

        if (null == mSession) {
            try {
                //If you do not want to switch engines, AREnginesSelector is useless.
                // You just need to use AREnginesApk.requestInstall() and the default engine
                // is Huawei AR Engine.
                AREnginesSelector.AREnginesAvaliblity enginesAvaliblity = AREnginesSelector.checkAllAvailableEngines(getActivity());
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

                    mSession = new ARSession(/*context=*/getActivity());
                    ARConfigBase config = new ARWorldTrackingConfig(mSession);
                    mSession.configure(config);
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

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSession != null) {
            mDisplayRotationHelper.onPause();
            mSurfaceView.onPause();
            mSession.pause();
        }
        mSensorManager.unregisterListener(new MySensorEventListener());

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
//        showFpsTextView(String.valueOf(FPSCalculate()));
        setView(azimuthAngle);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (null == mSession) {
            return;
        }
        mDisplayRotationHelper.updateSessionIfNeeded(mSession);

        try {
            mSession.setCameraTextureName(mBackgroundRenderer.getTextureId());
            mFrame = mSession.update();
            ARCamera camera = mFrame.getCamera();
            mArPose = camera.getDisplayOrientedPose();  //获取摄像头位置（即为定位位置）
            if (!isFirstFlag) {  //判断第一次进入
                if (hasPlane) { //开始定位的标志
                    isFirstFlag = true;
                    Log.e("XHF", "定位开始");
                    mGetArPoseHandler.post(mGetArPosRunnable);
                }
            }
//            handleTap(frame, camera);

            mBackgroundRenderer.draw(mFrame);

            if (camera.getTrackingState() == ARTrackable.TrackingState.PAUSED) {
                return;
            }

            float[] projmtx = new float[UtilsCommon.MATRIX_NUM];
            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

            float[] viewmtx = new float[UtilsCommon.MATRIX_NUM];
            camera.getViewMatrix(viewmtx, 0);

            ARLightEstimate le = mFrame.getLightEstimate();
            float lightIntensity = 1;
            if (le.getState() != ARLightEstimate.State.NOT_VALID) {
                lightIntensity = le.getPixelIntensity();
            }
            ARPointCloud arPointCloud = mFrame.acquirePointCloud();
            mPointCloud.update(arPointCloud);
            mPointCloud.draw(viewmtx, projmtx);
            arPointCloud.release();

            if (mSearchingTextView != null) {
                for (ARPlane plane : mSession.getAllTrackables(ARPlane.class)) {
                    if (plane.getType() != ARPlane.PlaneType.UNKNOWN_FACING &&
                            plane.getTrackingState() == ARTrackable.TrackingState.TRACKING) {
                        hasplat = true;
                        hideLoadingMessage();
                        break;
                    } else {
                        hasplat = false;
                    }
                }
            }
            mPlaneRenderer.drawPlanes(mSession.getAllTrackables(ARPlane.class), camera.getDisplayOrientedPose(), projmtx);

            Iterator<ARAnchor> ite = mAnchors.iterator();
            while (ite.hasNext()) {
                ARAnchor coloredAnchor = ite.next();
                if (coloredAnchor.getTrackingState() == ARTrackable.TrackingState.STOPPED) {
                    ite.remove();
                } else if (coloredAnchor.getTrackingState() == ARTrackable.TrackingState.TRACKING) {
                    coloredAnchor.getPose().toMatrix(mAnchorMatrix, 0);
                    mVirtualObject.updateModelMatrix(mAnchorMatrix, mScaleFactor);
                    mVirtualObject.draw(viewmtx, projmtx, lightIntensity, DEFAULT_COLOR);
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

    public void setArCameraListener(ArCameraListener arCameraListener) {
        mListener = arCameraListener;
    }

    public interface ArCameraListener {
        void getCameraPose(ARPose arPose);
    }

    public void setCameraPose() {
        mAnchors.add(mSession.createAnchor(mArPose));
    }

    public ByteBuffer getMapData() {
        return mSession.saveSharedData();
    }

    public Collection<ARAnchor> getARAnchors() {
        return Collections.unmodifiableList(mAnchors);
    }

    public ARSession getARSession() {
        return mSession;
    }

    public void setMapData(byte[] map) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(map.length);
        byteBuffer.put(map);

        mSession.loadSharedData(byteBuffer);
    }

    public void setARAnchors(final File arData) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int count = 0;
                while (true) {
                    ARFrame.AlignState state = mFrame.getAlignState();
                    Log.e("ARFrame.AlignState", state.toString());
                    if (state == ARFrame.AlignState.SUCCESS) {
                        mAnchors = ShareMapHelper.readAnchorFromFile(arData, getARSession());
                        /*getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTvRsrp.setVisibility(View.VISIBLE);
                                mTvcode.setVisibility(View.VISIBLE);
                                mPrruNeCode.setVisibility(View.VISIBLE);
                                mPrruNeCode.setVisibility(View.VISIBLE);
                            }
                        });*/
                        break;
                    } else if (state == ARFrame.AlignState.PROCESSING && count == 0) {
                        count++;
                    }
                }
            }
        }).start();
    }

    public void initARData() {
        String siteName = ((FloorMapActivity) getActivity()).getSiteName();
        String floorName = ((FloorMapActivity) getActivity()).getFloorName();
        File mapData = new File(Constant.AR_PATH + File.separator + siteName + File.separator + floorName + "map.data");
        if (mapData.exists()) {
            setMapData(ShareMapHelper.readBuffer(mapData));
        }
        File arData = new File(Constant.AR_PATH + File.separator + siteName + File.separator + floorName + "ar.data");
        if (arData.exists()) {
            setARAnchors(arData);

        }

    }

    public void setViewValues(String neCode, String rsrp) {
        mPrruNeCode.setText(neCode);
        mPrruRsrp.setText(rsrp);
    }

    class MySensorEventListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = event.values;
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticFieldValues = event.values;
            }
            calculateOrientation();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    private void calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues,
                magneticFieldValues);
        SensorManager.getOrientation(R, values);
        azimuthAngle = (float) Math.toDegrees(values[0]);
    }

    public float getAzimuthAngle() {
        List<Float> angles = new ArrayList<Float>(50);
        try {
            while (angles.size() < 50) {
                angles.add(azimuthAngle);
                Thread.sleep(20);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Collections.sort(angles);
        return angles.get(33);
    }

}