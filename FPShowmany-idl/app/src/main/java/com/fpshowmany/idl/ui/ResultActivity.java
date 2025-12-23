package com.fpshowmany.idl.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.fpshowmany.idl.R;
import com.fpshowmany.idl.databinding.ActivityResultBinding;
import com.fpshowmany.idl.utils.ResultEvaluator;

/**
 * Активность отображения результатов теста.
 * Показывает итоговый балл и рейтинг устройства.
 */
public class ResultActivity extends AppCompatActivity {

    private ActivityResultBinding binding;
    private ResultEvaluator evaluator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int score = getIntent().getIntExtra("score", 0);
        evaluator = new ResultEvaluator(this);

        setupUI(score);
        setupClickHandlers();
        animateResults();
    }

    private void setupUI(int score) {
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Отображаем оценку
        ResultEvaluator.Rating rating = evaluator.getRating(score);

        binding.tvScore.setText(String.format("%d", score));
        binding.tvRating.setText(rating.getLabel());
        binding.tvRatingDescription.setText(rating.getDescription());

        // Устанавливаем цвет рейтинга
        int ratingColor = rating.getColor();
        binding.tvRating.setTextColor(ratingColor);
        binding.cardResult.setCardBackgroundColor(ratingColor & 0x10FFFFFF); // Полупрозрачный фон

        // Показываем детали CPU, RAM, GPU если доступны
        int cpuScore = getIntent().getIntExtra("cpu_score", 0);
        int ramScore = getIntent().getIntExtra("ram_score", 0);
        int gpuScore = getIntent().getIntExtra("gpu_score", 0);

        if (cpuScore > 0) {
            binding.tvCpuScore.setText(String.format("%d", cpuScore));
            binding.tvRamScore.setText(String.format("%d", ramScore));
            binding.tvGpuScore.setText(String.format("%d", gpuScore));
        }
    }

    private void setupClickHandlers() {
        // Кнопка повтора теста
        binding.btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultActivity.this, TestActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void animateResults() {
        // Анимация появления карточки результата
        binding.cardResult.setScaleX(0.8f);
        binding.cardResult.setScaleY(0.8f);
        binding.cardResult.setAlpha(0f);

        binding.cardResult.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(500)
                .start();

        // Анимация счета
        binding.tvScore.setTranslationY(100);
        binding.tvScore.setAlpha(0);
        binding.tvScore.animate()
                .translationY(0)
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(300)
                .start();

        // Анимация рейтинга
        binding.tvRating.setTranslationY(50);
        binding.tvRating.setAlpha(0);
        binding.tvRating.animate()
                .translationY(0)
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(500)
                .start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
