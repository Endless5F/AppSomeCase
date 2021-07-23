package com.android.performanceanalysis.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.performanceanalysis.R;
import com.android.performanceanalysis.webview.WebViewActivity;
import com.android.performanceanalysis.webview.utils.StatusBarUtil;

public class WebViewDemoList  extends AppCompatActivity implements View.OnClickListener {

    private AutoCompleteTextView etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view_demo_list);
//        LaunchApplication.currentActivity = this;
        StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.colorPrimary), 0);
        initView();
    }

    private void initView() {
        findViewById(R.id.bt_openUrl).setOnClickListener(this);
        findViewById(R.id.bt_baidu).setOnClickListener(this);
        findViewById(R.id.bt_movie).setOnClickListener(this);
        findViewById(R.id.bt_upload_photo).setOnClickListener(this);
        findViewById(R.id.bt_call).setOnClickListener(this);
        findViewById(R.id.bt_java_js).setOnClickListener(this);

        etSearch = findViewById(R.id.et_search);
        /* 处理键盘搜索键 */
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                openUrl();
            }
            return false;
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.bt_openUrl) {
            openUrl();
        } else if (id == R.id.bt_baidu) {// 百度一下
            String baiDuUrl = "http://www.baidu.com";
            loadUrl(baiDuUrl, "百度一下");
        } else if (id == R.id.bt_movie) {// 网络视频
            String movieUrl = "https://sv.baidu.com/videoui/page/videoland?context=%7B%22nid%22%3A%22sv_5861863042579737844%22%7D&pd=feedtab_h5";
            loadUrl(movieUrl, "网络视频");
        } else if (id == R.id.bt_upload_photo) {// 上传图片
            String uploadUrl = "file:///android_asset/upload_photo.html";
            loadUrl(uploadUrl, "上传图片测试");
        } else if (id == R.id.bt_call) {// 打电话、发短信、发邮件、JS
            String callUrl = "file:///android_asset/callsms.html";
            loadUrl(callUrl, "电话短信邮件测试");
        } else if (id == R.id.bt_java_js) {//  js与android原生代码互调
            String javaJs = "file:///android_asset/java_js.html";
            loadUrl(javaJs, "js与android原生代码互调");
        }
    }

    /**
     * 打开网页
     */
    private void openUrl() {
        String url = etSearch.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            // 空url
            url = "https://github.com/youlookwhat/WebViewStudy";

        } else if (!url.startsWith("http") && url.contains("http")) {
            // 有http且不在头部
            url = url.substring(url.indexOf("http"), url.length());

        } else if (url.startsWith("www")) {
            // 以"www"开头
            url = "http://" + url;

        } else if (!url.startsWith("http") && (url.contains(".me") || url.contains(".com") || url.contains(".cn"))) {
            // 不以"http"开头且有后缀
            url = "http://www." + url;

        } else if (!url.startsWith("http") && !url.contains("www")) {
            // 输入纯文字 或 汉字的情况
            url = "http://m5.baidu.com/s?from=124n&word=" + url;
        }
        loadUrl(url, "详情");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actionbar_about) {
            loadUrl("https://github.com/youlookwhat/WebViewStudy", "WebViewStudy");
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadUrl(String mUrl, String mTitle) {
        WebViewActivity.loadUrl(this, mUrl, mTitle);
    }
}