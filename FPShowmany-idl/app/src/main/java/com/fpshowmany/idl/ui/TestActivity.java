package com.fpshowmany.idl.ui;

import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fpshowmany.idl.R;
import com.fpshowmany.idl.databinding.ActivityTestBinding;
import com.fpshowmany.idl.engine.StressTestEngine;
import com.fpshowmany.idl.utils.SettingsManager;

/**
 * Активность выполнения стресс-теста.
 * Запускает тестирование CPU, GPU и RAM и отображает результаты в реальном времени.
 */
public class TestActivity extends AppCompatActivity implements StressTestEngine.TestCallback {

    private ActivityTestBinding binding;
    private SettingsManager settingsManager;
    private StressTestEngine stressEngine;
    private Handler mainHandler;

    private boolean isTesting = false;
    private int totalScore = 0;
    private int testStage = 0; // 0: CPU, 1: RAM, 2: GPU

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settingsManager = new SettingsManager(this);
        mainHandler = new Handler(Looper.getMainLooper());

        setupUI();
        startTest();
    }

    private void setupUI() {
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTesting) {
                    stressEngine.stopAll();
                }
                finish();
            }
        });
    }

    private void startTest() {
        isTesting = true;
        testStage = 0;

        // Показываем первый этап
        showStage(getString(R.string.stage_cpu));

        stressEngine = new StressTestEngine(this, this);
        stressEngine.setCpuThreads(settingsManager.getCpuThreads());
        stressEngine.setGpuMode(settingsManager.getGpuMode());
        stressEngine.setDuration(settingsManager.getTestDuration());

        // Запускаем тест CPU
        stressEngine.startCpuTest();
    }

    private void showStage(final String stageName) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                binding.tvCurrentStage.setText(stageName);
                binding.progressBar.setProgress(0);
            }
        });
    }

    private void updateProgress(final int progress) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                binding.progressBar.setProgress(progress);
            }
        });
    }

    private void updateStats(final float fps, final int score) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                binding.tvFps.setText(String.format("%.1f FPS", fps));
                binding.tvCurrentScore.setText(String.format("%d", score));
            }
        });
    }

    private void nextStage() {
        testStage++;

        switch (testStage) {
            case 1:
                showStage(getString(R.string.stage_ram));
                stressEngine.startRamTest();
                break;
            case 2:
                showStage(getString(R.string.stage_gpu));
                stressEngine.startGpuTest(binding.glSurfaceView);
                break;
            case 3:
                // Все этапы завершены
                finishTest();
                break;
        }
    }

    private void finishTest() {
        isTesting = false;

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TestActivity.this, R.string.test_complete, Toast.LENGTH_SHORT).show();

                // Переходим к экрану результатов
                Intent intent = new Intent(TestActivity.this, ResultActivity.class);
                intent.putExtra("score", totalScore);
                startActivity(intent);

                finish();
            }
        });
    }

    @Override
    public void onCpuProgress(int progress, float fps, int score) {
        updateProgress(progress);
        updateStats(fps, score);
    }

    @Override
    public void onRamProgress(int progress, float opsPerSecond, int score) {
        updateProgress(progress);
        updateStats(opsPerSecond, score);
    }

    @Override
    public void onGpuProgress(int progress, float fps, int score) {
        updateProgress(progress);
        updateStats(fps, score);
    }

    @Override
    public void onCpuComplete(int score) {
        totalScore += score;
        nextStage();
    }

    @Override
    public void onRamComplete(int score) {
        totalScore += score;
        nextStage();
    }

    @Override
    public void onGpuComplete(int score) {
        totalScore += score;
        nextStage();
    }

    @Override
    public void onError(String message) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TestActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isTesting) {
            stressEngine.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isTesting) {
            stressEngine.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stressEngine != null) {
            stressEngine.stopAll();
        }
        binding = null;
    }
}
