package com.androidx.ulife.init;

import androidx.annotation.NonNull;

import com.rousetime.android_startup.model.LoggerLevel;
import com.rousetime.android_startup.model.StartupConfig;
import com.rousetime.android_startup.provider.StartupProviderConfig;

public class SampleStartupProviderConfig implements StartupProviderConfig {
    @NonNull
    @Override
    public StartupConfig getConfig() {
        return new StartupConfig.Builder()
                .setLoggerLevel(LoggerLevel.DEBUG) // default LoggerLevel.NONE
                .setAwaitTimeout(10000L) // default 10000L
                .setOpenStatistics(true) // default true
                .build();
    }
}
