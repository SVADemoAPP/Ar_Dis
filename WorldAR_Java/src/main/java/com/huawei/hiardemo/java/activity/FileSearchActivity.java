package com.huawei.hiardemo.java.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.huawei.hiardemo.java.R;
import com.huawei.hiardemo.java.adapter.SearchZipAdapter;
import com.huawei.hiardemo.java.bean.ZipFile;
import com.huawei.hiardemo.java.framework.activity.BaseActivity;
import com.huawei.hiardemo.java.util.FileUtils;
import com.huawei.hiardemo.java.util.ScanFileCountUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileSearchActivity extends BaseActivity implements View.OnClickListener {
    private TextView mllTop;
    private LinearLayout mllAdd;
    private LinearLayout mBack;
    private LinearLayout mSearchHolder;
    private LinearLayout mSearch;
    private EditText mEdtSearch;
    private TextView mTvCancel;

    private Context mContext;
    private String searchName;
    private boolean mFirst = false;
    private SearchZipAdapter mSearchZipAdapter;
    private List<ZipFile> mZipFiles = new ArrayList<>();//查询出来的文件
    private List<ZipFile> mMatch = new ArrayList<>();
    private boolean mScanFlag = false;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            mScanFlag = true;
//            if (LoadingDialog.with(FileSearchActivity.this).isShowing()) {
//                LoadingDialog.with(FileSearchActivity.this).cancelDialog();
//            }
            //接收结果
            List<String> list = (List<String>) msg.obj;
            //后续显示处理
            for (String path : list) {
                String[] split = path.split("/");
                String fileName = split[split.length - 1]; //获取文件名称
                mZipFiles.add(new ZipFile(path, fileName));//获取zipFile类
            }
        }
    };
    private RecyclerView mSearchZipRl;

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mScanFlag) //没有扫描完成
        {
//            LoadingDialog.with(FileSearchActivity.this)
//                    .setProgressText("正在初始化...")
//                    .showDialog();

        }
        if (!mFirst) //判断是否是第一次进入
        {
            scanFile();
            mFirst = true;
        }
    }

    @Override
    public void findView() {
        mllTop = findViewById(R.id.tool_top_name);
        mllAdd = findViewById(R.id.tool_right_add);
        mBack = findViewById(R.id.back);
        mSearchHolder = findViewById(R.id.ll_search);
        mSearch = findViewById(R.id.ll_search_rl);
        mEdtSearch = findViewById(R.id.edt_search);
        mTvCancel = findViewById(R.id.search_cancel);
        mSearchZipRl = findViewById(R.id.search_file);

        mBack.setOnClickListener(this);
        mSearchHolder.setOnClickListener(this);
        mTvCancel.setOnClickListener(this);
        mEdtSearch.addTextChangedListener(new TextWatcher() {


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                searchName = s.toString(); //获取搜索关键字
                mMatch.clear();
                if (!searchName.equals("")) {
                    mSearchZipAdapter.setKeyName(searchName);
                    mMatch.addAll(searchText(searchName, mZipFiles));
                    if (mMatch.size() == 0) {
                        Log.e("XHF", "搜索无结果");
                    }
                    Log.e("XHF", "match" + mMatch);
                }
                if (mSearchZipAdapter != null) {
                    mSearchZipAdapter.notifyDataSetChanged();
                    Log.e("XHF", "ad");
                }

            }
        });
    }

    @Override
    public void setContentLayout() {
        mContext = this;
        setContentView(R.layout.activity_file_selector);
    }

    @Override
    public void dealLogicBeforeInitView() {

    }

    @Override
    public void initView() {
        mllTop.setText("全部文件搜索");
        mBack.setVisibility(View.VISIBLE);
        mllAdd.setVisibility(View.GONE);
    }

    @Override
    public void dealLogicAfterInitView() {
        initSearchRl();
    }

    private void initSearchRl() {
        mSearchZipAdapter = new SearchZipAdapter(mMatch);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        mSearchZipRl.setLayoutManager(layoutManager);
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        mSearchZipRl.setAdapter(mSearchZipAdapter);
        mSearchZipAdapter.setOnItemClick(new SearchZipAdapter.OnItemClick() {
            @Override
            public void setOnItemClick(String path) {
                Log.e("XHF_HH", "path=" + path);
                Intent intent = new Intent();
                intent.putExtra("filePath", path);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    /**
     * 扫描sdk卡文件
     */
    private void scanFile() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {  //判断sd卡是否正常挂载
            return;
        }
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.e("XHF", path);
        final Map<String, Set<String>> CATEGORY_SUFFIX = new HashMap<>();
        Set<String> set = new HashSet<>();
        set.add("zip");
//        set.add("rar");
//        set.add("7z");
        CATEGORY_SUFFIX.put("zip", set);

        //单一线程线程池
        ExecutorService singleExecutorService = Executors.newSingleThreadExecutor();
        singleExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                //构建对象
                ScanFileCountUtil scanFileCountUtil = new ScanFileCountUtil
                        .Builder(mHandler)
                        .setFilePath(path)
                        .setCategorySuffix(CATEGORY_SUFFIX)
                        .create();
                scanFileCountUtil.scanCountFile();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.ll_search:
                setSearch(true);
                break;
            case R.id.search_cancel:
                setSearch(false);
                mEdtSearch.setText("");//清空
                break;
            default:
                break;
        }
    }

    /**
     * 搜索框与占位View的切换
     *
     * @param flag
     */
    private void setSearch(boolean flag) {
        if (flag)  //显示搜索
        {
            mSearchHolder.setVisibility(View.GONE);
            mSearch.setVisibility(View.VISIBLE);
            showSoftInputFromWindow(mEdtSearch);

        } else {    //显示占位View
            mSearchHolder.setVisibility(View.VISIBLE);
            mSearch.setVisibility(View.GONE);
            hideSoftInputFromWindow(mEdtSearch);

        }
    }

    /**
     * 显示键盘
     *
     * @param editText
     */
    private void showSoftInputFromWindow(EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        InputMethodManager inputManager = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(editText, 0);
    }

    /**
     * 隐藏键盘
     *
     * @param editText
     */
    private void hideSoftInputFromWindow(EditText editText) {
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setResult(RESULT_CANCELED);
//        if (LoadingDialog.with(FileSearchActivity.this).isShowing()) {
//            LoadingDialog.with(FileSearchActivity.this).cancelDialog();
//        }
    }


    /**
     * 物理返回键
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * @param fileName 需要匹配的名称
     * @param data     数据
     * @return
     */
    public List<ZipFile> searchText(String fileName, List<ZipFile> data) {
        List<ZipFile> compileList = new ArrayList(); //匹配的
        Pattern pattern = Pattern.compile(fileName, Pattern.CASE_INSENSITIVE); //Pattern.CASE_INSENSITIVE为不区分大小写
        for (int i = 0; i < data.size(); i++) {
            Matcher matcher = pattern.matcher(FileUtils.getFileNameNoEx(data.get(i).getFileName()));
            if (matcher.find()) {
                compileList.add(data.get(i));
            }
        }
        return compileList;
    }

}
