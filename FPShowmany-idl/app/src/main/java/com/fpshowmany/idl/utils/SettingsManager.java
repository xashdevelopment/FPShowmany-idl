package com.fpshowmany.idl.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Менеджер настроек приложения.
 * Сохраняет и загружает настройки тестирования.
 */
public class SettingsManager {

    private static final String PREFS_NAME = "fpshowmany_settings";
    private static final String KEY_GPU_MODE = "gpu_mode";
    private static final String KEY_DURATION = "duration";
    private static final String KEY_CPU_THREADS = "cpu_threads";

    public static final int GPU_MODE_AUTO = 0;
    public static final int GPU_MODE_2D = 1;
    public static final int GPU_MODE_3D = 2;

    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getGpuMode() {
        return prefs.getInt(KEY_GPU_MODE, GPU_MODE_AUTO);
    }

    public void setGpuMode(int mode) {
        prefs.edit().putInt(KEY_GPU_MODE, mode).apply();
    }

    public int getTestDuration() {
        return prefs.getInt(KEY_DURATION, 30); // По умолчанию 30 секунд
    }

    public void setTestDuration(int seconds) {
        prefs.edit().putInt(KEY_DURATION, seconds).apply();
    }

    public int getCpuThreads() {
        return prefs.getInt(KEY_CPU_THREADS, Runtime.getRuntime().availableProcessors());
    }

    public void setCpuThreads(int threads) {
        prefs.edit().putInt(KEY_CPU_THREADS, threads).apply();
    }
}
