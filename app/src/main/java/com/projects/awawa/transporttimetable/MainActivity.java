// Главное меню
package com.projects.awawa.transporttimetable;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    // Объявляем кнопочки
    Button btnTramvaj;
    Button btnTrolley;
    Button btnBus;
    Button btnMarsh;
    Button btnTaxi;
    ImageButton btnMenu;

    // Тут объявляется рекламный баннер
    AdView adView;

    // В этой переменной будет лежать разметка меню
    LinearLayout main_menu_layout;

    // Переменная межстраничного баннера
    InterstitialAd interstitialAd;

    // Обработка запуска окна
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // В ресурсах ищется идентификатор разметки, это надо для обращения к элементам интерфейса
        main_menu_layout = findViewById(R.id.main_menu_layout);

        // Инициализируется реклама
        MobileAds.initialize(this, "#######################");

        // Поиск идентификатора рекламного баннера
        adView = findViewById(R.id.adViewMain);

        // Создаётся и загружается межстраничный баннер, но не показывается
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("###########################");
        interstitialAd.loadAd(new AdRequest.Builder().build());

        // Загружается рекламный баннер
        AdRequest request = new AdRequest.Builder().build();
        adView.loadAd(request);

        // Находим кнопки по ID и устанавливаем функции для обработки нажатий на кнопку
        // Нажатия на любую из кнопок обрабатываются одной функцией
        btnTramvaj = findViewById(R.id.btnTramvaj);
        btnTramvaj.setOnClickListener(this);

        btnTrolley = findViewById(R.id.btnTrolley);
        btnTrolley.setOnClickListener(this);

        btnBus = findViewById(R.id.btnBus);
        btnBus.setOnClickListener(this);

        btnMarsh = findViewById(R.id.btnMarsh);
        btnMarsh.setOnClickListener(this);

        btnTaxi = findViewById(R.id.btnTaxi);
        btnTaxi.setOnClickListener(this);

        btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(this);

    }

    // Функция обработки нажатия на кнопку
    @Override
    public void onClick(final View v){

        // Эта вся штука следит за тем, что происходит с межстраничным баннером, когда он открыт
        AdListener intAdListener = new AdListener(){

            // Это срабатывает когда юзверь закрывает баннер
            @Override
            public void onAdClosed(){
                // С сервера выгружается новый баннер
                interstitialAd.loadAd(new AdRequest.Builder().build());

                // Создается интент для вызова следующего окна
                Intent intent = new Intent(v.getContext(), Routes.class);

                // Нажатие на кнопку меню обрабатывает другая функция
                if (v.getId() == R.id.btnMenu){
                    showMenu(v);
                }
                // Остальные кнопки обрабатываются здесь
                else {
                    // К вызову новой формы прикрепляется идентификатор транспорта
                    // Это надо чтобы вытащить список маршрутов из БД
                    switch (v.getId()) {
                        case R.id.btnTramvaj:
                            intent.putExtra("transportID", 1);
                            break;
                        case R.id.btnTrolley:
                            intent.putExtra("transportID", 2);
                            break;
                        case R.id.btnBus:
                            intent.putExtra("transportID", 3);
                            break;
                        case R.id.btnMarsh:
                            intent.putExtra("transportID", 4);
                            break;
                        case R.id.btnTaxi:
                            intent.putExtra("transportID", 5);
                            break;
                        case R.id.btnMenu:
                            openOptionsMenu(); // Эта строчка по идее не должна никогда выполнятся, но пусть полежит

                    }

                    // Запускаем следующее окно
                    startActivity(intent);
                }
            }
        };

        // К межстраничному баннеру прикрепляется вся та верхняя штука
        interstitialAd.setAdListener(intAdListener);

        // Количество кликов в приложении увеличивается на 1
        ((MyApp) this.getApplication()).increaseClickCounter();

        // Если счётчик достиг значения 6 или больше
        if(((MyApp)this.getApplication()).getCounterValue() >= 6 && interstitialAd.isLoaded()){
            // Счётчик зануляется
            ((MyApp) this.getApplication()).clearCounter();
            // Показвыается баннер
            interstitialAd.show();

        }
        else {

            // Создаем интент для вызова следующей формы
            Intent intent = new Intent(this, Routes.class);

            // Нажатие на кнопку меню обрабатывает другая функция
            if (v.getId() == R.id.btnMenu) {
                showMenu(v);
            } 
            // Остальные кнопки обрабатываются здесь
            else {
                // К вызову новой формы прикрепляется идентификатор транспорта
                // Это надо чтобы вытащить список маршрутов из БД
                switch (v.getId()) {
                    case R.id.btnTramvaj:
                        intent.putExtra("transportID", 1);
                        break;
                    case R.id.btnTrolley:
                        intent.putExtra("transportID", 2);
                        break;
                    case R.id.btnBus:
                        intent.putExtra("transportID", 3);
                        break;
                    case R.id.btnMarsh:
                        intent.putExtra("transportID", 4);
                        break;
                    case R.id.btnTaxi:
                        intent.putExtra("transportID", 5);
                        break;
                    case R.id.btnMenu:
                        openOptionsMenu();

                }

                // Запускаем следующее окно
                startActivity(intent);
            }
        }
    }

    // ХЗ как это сюда попало
    public void goVK(View v){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/transtimedonetsk"));
        startActivity(intent);
    }

    // Этот блок функций надо чтобы рекламные баннеры адекватно отрабатывали
    @Override
    protected void onResume(){
        super.onResume();

        adView.resume();
    }

    @Override
    protected void onPause(){
        super.onPause();

        adView.pause();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        adView.destroy();
    }

    // Эта функция отрабатывает при нажатии на кнопку меню
    public void showMenu(View v){
        // Создаётся и показвыается объект всплывающего меню
        PopupMenu menu = new PopupMenu(this, v);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(R.menu.menu, menu.getMenu());
        menu.show();

        // Вся эта вещь следит за нажатиями на пункты меню
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AlertDialog.Builder alertDialog;
                AlertDialog dialog;
                switch (item.getItemId()){
                    // Если нажата кнопка "О разработчиках" вызывается новое окно
                    case R.id.menu_about:
                        Intent intent = new Intent(MainActivity.this, About.class);
                        startActivity(intent);
                        return true;
                    // Если нажата кнопка выбора города или информации о БД, выводится соответствующее сообщение
                    case R.id.menu_city:
                        alertDialog = new AlertDialog.Builder(MainActivity.this);
                        alertDialog.setTitle("Выбор города");
                        alertDialog.setMessage("Функция находится в разработке");
                        alertDialog.setCancelable(false);
                        alertDialog.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        dialog = alertDialog.create();
                        dialog.show();
                        return true;
                    case R.id.db_info:
                        alertDialog = new AlertDialog.Builder(MainActivity.this);
                        alertDialog.setTitle("Информация об актуальности БД");
                        alertDialog.setMessage("Последнее обновление БД 10.08.2018");
                        alertDialog.setCancelable(false);
                        alertDialog.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });


                        dialog = alertDialog.create();
                        dialog.show();
                        return true;
                        default:
                            return false;
                }
            }
        });
    }
}