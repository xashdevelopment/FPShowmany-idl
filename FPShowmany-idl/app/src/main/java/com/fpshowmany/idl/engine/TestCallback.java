package com.fpshowmany.idl.engine;

import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Интерфейс обратного вызова для получения результатов тестирования.
 */
public interface TestCallback {
    void onCpuProgress(int progress, float fps, int score);
    void onRamProgress(int progress, float opsPerSecond, int score);
    void onGpuProgress(int progress, float fps, int score);
    void onCpuComplete(int score);
    void onRamComplete(int score);
    void onGpuComplete(int score);
    void onError(String message);
}
