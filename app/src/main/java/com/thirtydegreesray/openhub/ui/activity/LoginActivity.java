/*
 *    Copyright 2017 ThirtyDegressRay
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.thirtydegreesray.openhub.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.thirtydegreesray.openhub.AppConfig;
import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.inject.component.AppComponent;
import com.thirtydegreesray.openhub.inject.component.DaggerActivityComponent;
import com.thirtydegreesray.openhub.mvp.contract.ILoginContract;
import com.thirtydegreesray.openhub.mvp.presenter.LoginPresenter;
import com.thirtydegreesray.openhub.ui.activity.base.BaseActivity;
import com.thirtydegreesray.openhub.util.HttpUtil;

import java.util.Map;

import butterknife.BindView;

/**
 * Created on 2017/7/12.
 *
 * @author ThirtyDegreesRay
 */

public class LoginActivity extends BaseActivity<LoginPresenter>
        implements ILoginContract.View {

    private final String TAG = "LoginActivity";

    @Nullable @BindView(R.id.web_view) WebView webView;

    private Handler handler;

    @Override
    public void onGetTokenSuccess(String token, String scope, int expireIn) {
        Intent intent = new Intent();
        intent.putExtra("accessToken", token);
        intent.putExtra("scope", scope);
        intent.putExtra("expireIn", expireIn);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 依赖注入的入口
     *
     * @param appComponent appComponent
     */
    @Override
    protected void setupActivityComponent(AppComponent appComponent) {
        DaggerActivityComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this);
    }

    /**
     * 获取ContentView id
     *
     * @return
     */
    @Override
    protected int getContentView() {
        return R.layout.activity_login;
    }

    /**
     * 初始化activity
     */
    @Override
    protected void initActivity() {
        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                String url = (String) msg.obj;
                Map<String, String> params = HttpUtil.getParams(url);
                String code = params.get("code");
                String state = params.get("state");
                mPresenter.getToken(code, state);
            }
        };
    }

    /**
     * 初始化view
     *
     * @param savedInstanceState
     */
    @Override
    protected void initView(Bundle savedInstanceState) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(@NonNull WebView view, @NonNull String url) {
                if(url.startsWith(AppConfig.OAUTH2_CALLBACK_URL)){
                    Message message = new Message();
                    message.obj = url;
                    handler.sendMessage(message);
                } else {
                    view.loadUrl(url);
                }
                Log.i(TAG, "shouldOverrideUrlLoading:" + url);
                return true;
            }
        });
//        webView.loadUrl(mPresenter.getOAuth2Url());
    }
}