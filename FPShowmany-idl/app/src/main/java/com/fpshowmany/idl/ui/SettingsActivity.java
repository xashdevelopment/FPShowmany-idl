package com.fpshowmany.idl.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.fpshowmany.idl.R;
import com.fpshowmany.idl.databinding.ActivitySettingsBinding;
import com.fpshowmany.idl.utils.SettingsManager;

/**
 * Активность настроек теста.
 * Позволяет настроить параметры стресс-теста: режим GPU, длительность, количество потоков.
 */
public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private SettingsManager settingsManager;

    private static final String[] CPU_THREADS = {"1", "2", "4", "8", "Максимум"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settingsManager = new SettingsManager(this);

        setupToolbar();
        setupGPU_mode();
        setupDuration();
        setupCpuThreads();
        loadSettings();
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings);
        }
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setupGPU_mode() {
        binding.rgGpuMode.setOnCheckedChangeListener(new android.widget.RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.RadioGroup group, int checkedId) {
                int mode = SettingsManager.GPU_MODE_AUTO;
                if (checkedId == R.id.rbMode2D) {
                    mode = SettingsManager.GPU_MODE_2D;
                } else if (checkedId == R.id.rbMode3D) {
                    mode = SettingsManager.GPU_MODE_3D;
                } else if (checkedId == R.id.rbModeAuto) {
                    mode = SettingsManager.GPU_MODE_AUTO;
                }
                settingsManager.setGpuMode(mode);
            }
        });
    }

    private void setupDuration() {
        binding.seekbarDuration.setMax(50); // 10-60 секунд
        binding.seekbarDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int duration = 10 + progress; // Минимум 10 секунд
                binding.tvDurationValue.setText(duration + " " + getString(R.string.seconds));
                settingsManager.setTestDuration(duration);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupCpuThreads() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                CPU_THREADS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerThreads.setAdapter(adapter);

        binding.spinnerThreads.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                int threads;
                switch (position) {
                    case 0: threads = 1; break;
                    case 1: threads = 2; break;
                    case 2: threads = 4; break;
                    case 3: threads = 8; break;
                    default:
                        threads = Runtime.getRuntime().availableProcessors();
                        break;
                }
                settingsManager.setCpuThreads(threads);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void loadSettings() {
        // Загрузка режима GPU
        int gpuMode = settingsManager.getGpuMode();
        switch (gpuMode) {
            case SettingsManager.GPU_MODE_2D:
                binding.rbMode2D.setChecked(true);
                break;
            case SettingsManager.GPU_MODE_3D:
                binding.rbMode3D.setChecked(true);
                break;
            default:
                binding.rbModeAuto.setChecked(true);
                break;
        }

        // Загрузка длительности
        int duration = settingsManager.getTestDuration();
        binding.seekbarDuration.setProgress(duration - 10);
        binding.tvDurationValue.setText(duration + " " + getString(R.string.seconds));

        // Загрузка количества потоков CPU
        int threads = settingsManager.getCpuThreads();
        int threadIndex;
        switch (threads) {
            case 1: threadIndex = 0; break;
            case 2: threadIndex = 1; break;
            case 4: threadIndex = 2; break;
            case 8: threadIndex = 3; break;
            default: threadIndex = 4; break;
        }
        binding.spinnerThreads.setSelection(threadIndex);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
