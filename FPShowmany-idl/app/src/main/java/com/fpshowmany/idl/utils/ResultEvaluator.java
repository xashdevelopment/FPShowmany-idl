package com.fpshowmany.idl.utils;

import android.content.Context;
import android.graphics.Color;

import com.fpshowmany.idl.R;

/**
 * Утилита для оценки результатов тестирования.
 * Определяет уровень производительности на основе набранных баллов.
 */
public class ResultEvaluator {

    public enum Rating {
        VERY_BAD("Очень Плохо", "Ваш телефон нуждается в замене", Color.parseColor("#F44336")),
        BAD("Плохо", "Устройство работает медленно", Color.parseColor("#FF5722")),
        NOT_BAD("Неплохо", "Базовые задачи выполняются нормально", Color.parseColor("#FFC107")),
        NORMAL("Нормально", "Средняя производительность", Color.parseColor("#4CAF50")),
        GOOD("Хорошо", "Устройство работает хорошо", Color.parseColor("#2196F3")),
        EXCELLENT("Отлично", "Отличная производительность!", Color.parseColor("#9C27B0"));

        private final String label;
        private final String description;
        private final int color;

        Rating(String label, String description, int color) {
            this.label = label;
            this.description = description;
            this.color = color;
        }

        public String getLabel() {
            return label;
        }

        public String getDescription() {
            return description;
        }

        public int getColor() {
            return color;
        }
    }

    private final Context context;

    public ResultEvaluator(Context context) {
        this.context = context;
    }

    /**
     * Определяет уровень производительности на основе общего балла.
     *
     * @param score Общий балл производительности
     * @return Уровень производительности
     */
    public Rating getRating(int score) {
        if (score < 1000) {
            return Rating.VERY_BAD;
        } else if (score < 3000) {
            return Rating.BAD;
        } else if (score < 5000) {
            return Rating.NOT_BAD;
        } else if (score < 7000) {
            return Rating.NORMAL;
        } else if (score < 9000) {
            return Rating.GOOD;
        } else {
            return Rating.EXCELLENT;
        }
    }

    /**
     * Возвращает текстовое описание уровня из ресурсов.
     *
     * @param rating Уровень производительности
     * @return Текст описания
     */
    public String getRatingDescription(Rating rating) {
        switch (rating) {
            case VERY_BAD:
                return context.getString(R.string.rating_very_bad);
            case BAD:
                return context.getString(R.string.rating_bad);
            case NOT_BAD:
                return context.getString(R.string.rating_not_bad);
            case NORMAL:
                return context.getString(R.string.rating_normal);
            case GOOD:
                return context.getString(R.string.rating_good);
            case EXCELLENT:
                return context.getString(R.string.rating_excellent);
            default:
                return "";
        }
    }
}
