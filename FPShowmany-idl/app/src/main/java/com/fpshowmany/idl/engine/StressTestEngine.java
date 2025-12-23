package com.fpshowmany.idl.engine;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Движок стресс-тестирования.
 * Выполняет тестирование CPU, RAM и GPU.
 */
public class StressTestEngine {

    private static final String TAG = "StressTestEngine";

    // Константы режимов GPU
    public static final int GPU_MODE_AUTO = 0;
    public static final int GPU_MODE_2D = 1;
    public static final int GPU_MODE_3D = 2;

    private final TestCallback callback;
    private final Handler mainHandler;

    private int gpuMode = GPU_MODE_AUTO;
    private int cpuThreads = 4;
    private int duration = 30; // секунды

    private ExecutorService cpuExecutor;
    private ExecutorService ramExecutor;
    private GLSurfaceView.Renderer gpuRenderer;

    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private AtomicBoolean isPaused = new AtomicBoolean(false);

    // Результаты
    private AtomicInteger cpuScore = new AtomicInteger(0);
    private AtomicInteger ramScore = new AtomicInteger(0);
    private AtomicInteger gpuScore = new AtomicInteger(0);

    public StressTestEngine(TestCallback callback) {
        this.callback = callback;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void setGpuMode(int mode) {
        this.gpuMode = mode;
    }

    public void setCpuThreads(int threads) {
        this.cpuThreads = threads;
    }

    public void setDuration(int seconds) {
        this.duration = seconds;
    }

    /**
     * Запуск теста CPU.
     */
    public void startCpuTest() {
        if (!isRunning.compareAndSet(false, true)) {
            return;
        }

        cpuScore.set(0);
        cpuExecutor = Executors.newFixedThreadPool(cpuThreads);

        final long startTime = System.currentTimeMillis();
        final long endTime = startTime + (duration * 1000 / 3); // Треть от общего времени

        // Запуск потоков для нагрузки на CPU
        for (int i = 0; i < cpuThreads; i++) {
            final int threadId = i;
            cpuExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    long operations = 0;
                    float lastFps = 0;

                    while (isRunning.get() && System.currentTimeMillis() < endTime) {
                        if (isPaused.get()) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                break;
                            }
                            continue;
                        }

                        // Выполняем тяжелые вычисления
                        for (int j = 0; j < 100000; j++) {
                            double result = Math.sin(j) * Math.cos(j) * Math.tan(j);
                            operations++;
                        }

                        // Расчет FPS
                        long elapsed = System.currentTimeMillis() - startTime;
                        if (elapsed > 0) {
                            lastFps = (operations / 1000f) / (elapsed / 1000f);
                        }

                        // Обновление прогресса
                        final float fps = lastFps;
                        final int progress = (int) ((System.currentTimeMillis() - startTime) * 100 / (endTime - startTime));
                        final int score = calculateCpuScore(operations, elapsed);

                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onCpuProgress(progress, fps, score);
                            }
                        });
                    }

                    // Финальный результат CPU
                    long elapsed = System.currentTimeMillis() - startTime;
                    int finalScore = calculateCpuScore(operations, elapsed);
                    cpuScore.set(finalScore);

                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onCpuComplete(finalScore);
                        }
                    });
                }
            });
        }
    }

    /**
     * Запуск теста RAM.
     */
    public void startRamTest() {
        final long startTime = System.currentTimeMillis();
        final long endTime = startTime + (duration * 1000 / 3);

        ramExecutor = Executors.newFixedThreadPool(2);

        ramExecutor.submit(new Runnable() {
            @Override
            public void run() {
                long operations = 0;
                long totalAllocated = 0;
                byte[][] blocks = new byte[100][];
                int blockIndex = 0;

                while (isRunning.get() && System.currentTimeMillis() < endTime) {
                    if (isPaused.get()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            break;
                        }
                        continue;
                    }

                    try {
                        // Выделяем блок памяти
                        int blockSize = (int) (Math.random() * 1024 * 1024) + 1024; // 1KB - 1MB
                        byte[] block = new byte[blockSize];

                        // Заполняем данными
                        for (int i = 0; i < blockSize; i += 1024) {
                            block[i] = (byte) (Math.random() * 256);
                        }

                        blocks[blockIndex] = block;
                        blockIndex = (blockIndex + 1) % blocks.length;

                        operations++;
                        totalAllocated += blockSize;

                        // Освобождаем старые блоки
                        if (blockIndex == 0) {
                            for (int i = 0; i < blocks.length / 2; i++) {
                                blocks[i] = null;
                            }
                        }

                        // Расчет OPS (Operations Per Second)
                        long elapsed = System.currentTimeMillis() - startTime;
                        if (elapsed > 0) {
                            float opsPerSecond = (operations * 1000f) / elapsed;

                            final int progress = (int) ((System.currentTimeMillis() - startTime) * 100 / (endTime - startTime));
                            final int score = calculateRamScore(operations, totalAllocated, elapsed);

                            final float finalOpsPerSecond = opsPerSecond;
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onRamProgress(progress, finalOpsPerSecond, score);
                                }
                            });
                        }
                    } catch (OutOfMemoryError e) {
                        // Очищаем память и продолжаем
                        System.gc();
                        for (int i = 0; i < blocks.length; i++) {
                            blocks[i] = null;
                        }
                        blockIndex = 0;
                    }
                }

                long elapsed = System.currentTimeMillis() - startTime;
                int finalScore = calculateRamScore(operations, totalAllocated, elapsed);
                ramScore.set(finalScore);

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onRamComplete(finalScore);
                    }
                });
            }
        });
    }

    /**
     * Запуск теста GPU.
     */
    public void startGpuTest(GLSurfaceView glSurfaceView) {
        if (glSurfaceView == null) {
            // Если GLSurfaceView недоступен, завершаем тест с базовым результатом
            gpuScore.set(1000);
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onGpuComplete(1000);
                }
            });
            return;
        }

        final long startTime = System.currentTimeMillis();
        final long endTime = startTime + (duration * 1000 / 3);

        // Настройка рендерера в зависимости от режима
        setupGpuRenderer(glSurfaceView);

        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glSurfaceView.onResume();

        // Мониторинг FPS
        new Thread(new Runnable() {
            private long frameCount = 0;
            private long lastFpsTime = startTime;

            @Override
            public void run() {
                while (isRunning.get() && System.currentTimeMillis() < endTime) {
                    if (isPaused.get()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            break;
                        }
                        continue;
                    }

                    frameCount++;
                    long currentTime = System.currentTimeMillis();
                    long elapsed = currentTime - lastFpsTime;

                    if (elapsed >= 1000) {
                        float fps = (frameCount * 1000f) / elapsed;
                        int progress = (int) ((currentTime - startTime) * 100 / (endTime - startTime));
                        int score = calculateGpuScore(fps);

                        frameCount = 0;
                        lastFpsTime = currentTime;

                        final float finalFps = fps;
                        final int finalScore = score;
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onGpuProgress(progress, finalFps, finalScore);
                            }
                        });
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                long elapsed = System.currentTimeMillis() - startTime;
                int finalScore = calculateGpuScore((frameCount * 1000f) / elapsed);
                gpuScore.set(finalScore);

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onGpuComplete(finalScore);
                    }
                });
            }
        }).start();
    }

    private void setupGpuRenderer(GLSurfaceView glSurfaceView) {
        final boolean is3DMode = (gpuMode == GPU_MODE_3D) || (gpuMode == GPU_MODE_AUTO);

        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            private float rotation = 0f;

            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                gl.glEnable(GL10.GL_DEPTH_TEST);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                gl.glViewport(0, 0, width, height);
                gl.glMatrixMode(GL10.GL_PROJECTION);
                gl.glLoadIdentity();
                GLU.gluPerspective(gl, 45.0f, (float) width / height, 0.1f, 100.0f);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
                gl.glMatrixMode(GL10.GL_MODELVIEW);
                gl.glLoadIdentity();

                // Вращение объекта
                rotation += 2.0f;

                if (is3DMode) {
                    // 3D режим - вращающийся куб с улучшенной детализацией
                    drawDetailedCube(gl);
                } else {
                    // 2D режим - сложные 2D фигуры
                    drawComplex2DScene(gl);
                }
            }

            private void drawDetailedCube(GL10 gl) {
                // Вершины куба
                float vertices[] = {
                        -1.0f, -1.0f,  1.0f,
                         1.0f, -1.0f,  1.0f,
                         1.0f,  1.0f,  1.0f,
                        -1.0f,  1.0f,  1.0f,
                        -1.0f, -1.0f, -1.0f,
                        -1.0f,  1.0f, -1.0f,
                         1.0f,  1.0f, -1.0f,
                         1.0f, -1.0f, -1.0f
                };

                // Нормали для освещения
                float normals[] = {
                        0, 0, 1,  0, 0, 1,  0, 0, 1,  0, 0, 1,
                        0, 0,-1,  0, 0,-1,  0, 0,-1,  0, 0,-1,
                        0, 1, 0,  0, 1, 0,  0, 1, 0,  0, 1, 0,
                        0,-1, 0,  0,-1, 0,  0,-1, 0,  0,-1, 0,
                        1, 0, 0,  1, 0, 0,  1, 0, 0,  1, 0, 0,
                       -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0
                };

                byte indices[] = {
                        0, 1, 2, 0, 2, 3,    // Передняя грань
                        4, 5, 6, 4, 6, 7,    // Задняя грань
                        3, 2, 6, 3, 6, 5,    // Верхняя грань
                        0, 3, 5, 0, 5, 4,    // Левая грань
                        1, 7, 6, 1, 6, 2,    // Правая грань
                        4, 7, 1, 4, 1, 0     // Нижняя грань
                };

                FloatBuffer vertexBuffer = createFloatBuffer(vertices);
                FloatBuffer normalBuffer = createFloatBuffer(normals);

                gl.glTranslatef(0.0f, 0.0f, -6.0f);
                gl.glRotatef(rotation, 1.0f, 1.0f, 0.0f);

                gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);

                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
                gl.glNormalPointer(GL10.GL_FLOAT, 0, normalBuffer);

                gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_BYTE, ByteBuffer.wrap(indices));

                gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
            }

            private void drawComplex2DScene(GL10 gl) {
                // Рисуем сложную 2D сцену с множеством примитивов
                for (int i = 0; i < 20; i++) {
                    float x = (float) (Math.random() * 4 - 2);
                    float y = (float) (Math.random() * 4 - 2);

                    gl.glTranslatef(x, y, 0);
                    gl.glRotatef(rotation * 2, 0, 0, 1);

                    // Рисуем треугольник
                    float[] triangle = {
                            0.5f, 0.0f, 0.0f,
                           -0.5f, 0.4f, 0.0f,
                           -0.5f, -0.4f, 0.0f
                    };

                    FloatBuffer triangleBuffer = createFloatBuffer(triangle);
                    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, triangleBuffer);
                    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

                    float r = (float) Math.random();
                    float g = (float) Math.random();
                    float b = (float) Math.random();
                    gl.glColor4f(r, g, b, 1.0f);

                    gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);

                    gl.glLoadIdentity();
                }
            }

            private FloatBuffer createFloatBuffer(float[] data) {
                ByteBuffer buffer = ByteBuffer.allocateDirect(data.length * 4);
                buffer.order(ByteOrder.nativeOrder());
                FloatBuffer fb = buffer.asFloatBuffer();
                fb.put(data);
                fb.position(0);
                return fb;
            }
        });
    }

    /**
     * Пауза тестирования.
     */
    public void pause() {
        isPaused.set(true);
    }

    /**
     * Возобновление тестирования.
     */
    public void resume() {
        isPaused.set(false);
    }

    /**
     * Остановка всех тестов.
     */
    public void stopAll() {
        isRunning.set(false);
        isPaused.set(false);

        if (cpuExecutor != null) {
            cpuExecutor.shutdownNow();
        }
        if (ramExecutor != null) {
            ramExecutor.shutdownNow();
        }
    }

    private int calculateCpuScore(long operations, long elapsedMs) {
        if (elapsedMs <= 0) return 0;
        float opsPerSecond = (operations * 1000f) / elapsedMs;
        return Math.min((int) (opsPerSecond / 10), 10000);
    }

    private int calculateRamScore(long operations, long allocatedBytes, long elapsedMs) {
        if (elapsedMs <= 0) return 0;
        float mbPerSecond = (allocatedBytes / (1024f * 1024f)) / (elapsedMs / 1000f);
        return Math.min((int) (mbPerSecond * 100), 10000);
    }

    private int calculateGpuScore(float fps) {
        return Math.min((int) (fps * 100), 10000);
    }

    public int getCpuScore() {
        return cpuScore.get();
    }

    public int getRamScore() {
        return ramScore.get();
    }

    public int getGpuScore() {
        return gpuScore.get();
    }

    public int getTotalScore() {
        return cpuScore.get() + ramScore.get() + gpuScore.get();
    }
}
