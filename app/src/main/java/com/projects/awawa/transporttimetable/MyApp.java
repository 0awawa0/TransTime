package com.projects.awawa.transporttimetable;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;

public class MyApp extends Application {
	// Тут обрабатывается счетчик кликов в приложении
    int clickCounter;

    @Override
    public void onCreate() {
        super.onCreate();
        clickCounter = 0;

    }

    // Наращивание счётчика
    public void increaseClickCounter(){
        clickCounter++;
    }

    // Обнуление счётчика
    public void clearCounter(){
        clickCounter = 0;
    }

    // Получение значения счётчика
    public int getCounterValue(){
        return clickCounter;
    }
}
