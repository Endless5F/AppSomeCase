package com.android.core.common.ui.component;

import android.app.Application;

import com.android.core.log.HiConsolePrinter;
import com.android.core.log.HiFilePrinter;
import com.android.core.log.HiLogConfig;
import com.android.core.log.HiLogManager;

public class HiBaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initLog();
    }

    private void initLog() {
        HiLogManager.init(new HiLogConfig() {
            @Override
            public JsonParser injectJsonParser() {
                return null;
//                return (src) -> new Gson().toJson(src);
            }

            @Override
            public boolean includeThread() {
                return true;
            }
        }, new HiConsolePrinter(), HiFilePrinter.getInstance(getCacheDir().getAbsolutePath(), 0));
    }
}
