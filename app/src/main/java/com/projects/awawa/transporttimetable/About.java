// Окно "О разработчиках"
package com.projects.awawa.transporttimetable;

// Список импортируемых библиотек
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class About extends AppCompatActivity {

    // Тут объявляется два рекламных баннера
    AdView adViewAbout;  // Нижний баннер
    InterstitialAd interstitialAd;  // Межстраничный баннер



    // Эта функция выполняется во время вызова окна
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Устанавливается разметка интерфейса
        setContentView(R.layout.about);

        // Инициализируется реклама
        MobileAds.initialize(this,
                "###################");

        // Здесь из файла ресурсов ищется идентификатор баннера
        adViewAbout = findViewById(R.id.adViewAbout);

        // Создаётся запрос рекламного баннер
        AdRequest adRequest = new AdRequest.Builder().build();

        // Запрос отправляется на сервер, обратно приходит реклама, которую надо выгрузить в баннер
        adViewAbout.loadAd(adRequest);

        // Здесь создаётся межстраничный банннер и загружается, но не показывается
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("#######################");
        interstitialAd.loadAd(new AdRequest.Builder().build());


    }

    // Эта функция срабатвыает при нажатии на кнопку отправления письма
    public void sendMail(View v){

        // mail - хранит адрес эл. почты для отправки письма
        String mail;
        mail = ((TextView) v).getText().toString();

        // Создаётся и настраивается 
        //	интент отправки письма (эта штука ищет по телефону все приложения, которые могут отправлять мейлы)
        Intent mailIntent = new Intent(Intent.ACTION_SEND);
        mailIntent.setType("plain/text");
        mailIntent.putExtra(Intent.EXTRA_EMAIL, new String []{mail});

        // Запуск интента (тут пользователю предлагается выбрать приложение)
        startActivity(Intent.createChooser(mailIntent, "Выберите приложение"));
    }

    // Эта функция срабатывает при нажатии на кнопку ВК
    // Принцип тот же, что и при отправке мейла, только тут открывается урл
    public void goVK(View v){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/transtimedonetsk"));
        startActivity(intent);
    }

    // Эта функция обрабатывает кнопку возврата на предыдущее окно
    public void goBack(View v){

    	// Эта штука следит за тем чё происходит с рекламным баннером
    	// По сути она просто грузит новый межстраничный баннер, когда старый закрывается
        AdListener intAdListener = new AdListener(){
            @Override
            public void onAdClosed() {
                interstitialAd.loadAd(new AdRequest.Builder().build());
                finish();
            }
        };
        // Тут она прикручивается к межстраничному баннеру
        interstitialAd.setAdListener(intAdListener);

        // Здесь наращивается счётчик кликов в приложении
        ((MyApp) this.getApplication()).increaseClickCounter();

        // Если этот счётчик достигает значения 6 или более, и при этом межстраничный баннер готов к показу
        if(((MyApp)this.getApplication()).getCounterValue() >= 6 && interstitialAd.isLoaded()){
        	// Счётчик обнуляется и показывается реклама
            ((MyApp) this.getApplication()).clearCounter();
            interstitialAd.show(); //Здесь включается прослушка баннера, когда пользователь его закрывает
            // выгружается новый баннер и закрывается окно

        }
        else {
            finish(); // Если счётчик ещё не достиг значения 6, окно просто закрывается
        }
    }

    // Эти функции для адекватной работы рекламных баннеров
    @Override
    protected void onResume(){
        super.onResume();

        adViewAbout.resume();
    }
    @Override
    protected void onPause(){
        super.onPause();

        adViewAbout.pause();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();

        adViewAbout.destroy();
    }
}
