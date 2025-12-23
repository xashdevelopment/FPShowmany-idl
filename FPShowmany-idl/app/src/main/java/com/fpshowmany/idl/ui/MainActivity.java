package com.fpshowmany.idl.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.fpshowmany.idl.databinding.ActivityMainBinding;
import com.fpshowmany.idl.utils.SettingsManager;

/**
 * Главная активность приложения.
 * Содержит только две кнопки: "Начать тест!" и "Настройки".
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settingsManager = new SettingsManager(this);

        setupUI();
    }

    private void setupUI() {
        // Кнопка запуска теста
        binding.btnStartTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestActivity.class);
                startActivity(intent);
            }
        });

        // Кнопка настроек
        binding.btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
