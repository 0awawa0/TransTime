// Окно с остановками по маршруту
package com.projects.awawa.transporttimetable;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;


import java.util.HashMap;
import java.util.List;

public class Stops extends AppCompatActivity implements View.OnClickListener {

    // Объявляем используемые переменные и вьюшки
    int transportID;
    int routeID;
    TabHost tabHost;
    LinearLayout straight;
    LinearLayout reverse;
    TextView stopsTitleText;
    ConstraintLayout titleStops;
    ImageView imgViewStops;
    InterstitialAd interstitialAd;
    AdView adView;

    // Обработка запусвка окна
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stops);

        // Инициализация рекламы
        MobileAds.initialize(this, "#########################");

        // Поиск идентификатора баннера
        adView = findViewById(R.id.adViewStops);

        // Загрузка межстраничного баннера
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("######################");
        interstitialAd.loadAd(new AdRequest.Builder().build());

        // Загрузка рекламного баннера
        AdRequest request = new AdRequest.Builder().build();
        adView.loadAd(request);

        // Задаем параметры для кнопок
        LinearLayout.LayoutParams btnsParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        btnsParams.bottomMargin = 20;
        btnsParams.gravity = Gravity.CENTER;

        // Получаем вьюшки для отображения номера маршрута и слой для размещения кнопок
        straight = findViewById(R.id.straight);
        reverse = findViewById(R.id.reverse);
        tabHost = findViewById(R.id.stopsTabHost);
        stopsTitleText = findViewById(R.id.stopsTitleText);
        imgViewStops = findViewById(R.id.imgViewStops);
        titleStops = findViewById(R.id.titleStops);

        // Получаем интент вызвавший окно
        Intent intent = getIntent();

        // Вытаскиваем ID для определения транспорта и номера маршрута
        transportID = intent.getIntExtra("transportID", 0);
        routeID = intent.getIntExtra("routeID", 0);

        // Настройка вкладок
        tabHost.setup();

        TabHost.TabSpec tabSpec;

        // Настраивается вкладка прямяого маршрута
        tabSpec = tabHost.newTabSpec("straight");
        tabSpec.setIndicator("Прямой");
        tabSpec.setContent(R.id.tab1);
        tabHost.addTab(tabSpec);

        // Настраивается вкладка обратного маршрута
        tabSpec = tabHost.newTabSpec("reverse");
        tabSpec.setIndicator("Обратный");
        tabSpec.setContent(R.id.tab2);
        tabHost.addTab(tabSpec);

        //Настройка вида вкладки (устанавливается чёрный цвет текста на вкладке)
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++){
            TextView tv = tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(getResources().getColor(R.color.black));
        }

        // Устанавливаем номер маршрута в заголовок окна
        stopsTitleText.setText(intent.getStringExtra("routeNumber"));

        // В зависимости от транспорта устанавливается цвет заголовка и картинка
        switch (intent.getIntExtra("transportID", 0)){
            case 1:
                stopsTitleText.setBackgroundResource(R.color.colorTramvaj);
                titleStops.setBackgroundResource(R.color.colorTramvaj);
                imgViewStops.setImageDrawable(getResources().getDrawable(R.mipmap.tram));
                break;
            case 2:
                stopsTitleText.setBackgroundResource(R.color.colorTrolley);
                titleStops.setBackgroundResource(R.color.colorTrolley);
                imgViewStops.setImageDrawable(getResources().getDrawable(R.mipmap.trolley));
                break;
            case 3:
                stopsTitleText.setBackgroundResource(R.color.colorBus);
                titleStops.setBackgroundResource(R.color.colorBus);
                imgViewStops.setImageDrawable(getResources().getDrawable(R.mipmap.bus));
                break;
            case 4:
                stopsTitleText.setBackgroundResource(R.color.colorMarsh);
                titleStops.setBackgroundResource(R.color.colorMarsh);
                imgViewStops.setImageDrawable(getResources().getDrawable(R.mipmap.marsh));
                break;
            case 5:
                stopsTitleText.setBackgroundResource(R.color.colorTaxi);
                titleStops.setBackgroundResource(R.color.colorTaxi);
                imgViewStops.setImageDrawable(getResources().getDrawable(R.mipmap.taxi));
                break;
                default: // По умолчанию ставится чёрный фон заголовка - такого быть не должно
                    stopsTitleText.setBackgroundResource(R.color.black);
                    titleStops.setBackgroundResource(R.color.black);
                    break;
        }

        // Создаемм объект для работы с базой данных
        DBHelper database = new DBHelper(this);

        // Получаем список остановок по ID транспорта и маршрута
        List<HashMap<Integer, List<String>>> stops = database.getStops(intent.getStringExtra("routeNumber"),
                intent.getIntExtra("transportID", 0), routeID);

        // Настройки картинки на кнопке
        ViewGroup.LayoutParams imgParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        // Настройки текста на кнопке
        ViewGroup.LayoutParams txtParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        // Настройки разметки
        ViewGroup.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        // В цикле отрисовываются кнопки
        for (int i = 0; i < stops.get(0).size(); i++)
        {
            // Так же как и на окне с маршрутами вместо Button используется LinearLayout
            LinearLayout LL = new LinearLayout(this);
            LL.setOrientation(LL.HORIZONTAL);
            LL.setBackgroundResource(R.color.colorButton);
            LL.setVerticalGravity(Gravity.CENTER_VERTICAL);

            ImageView img = new ImageView(this);
            // В зависимости от остановки устанавливаются разные картинки на кнопке
            if (i == 0){
                // Установка картинки начальной остановки
                img.setImageDrawable(getResources().getDrawable(R.mipmap.route_start));
            }
            else{
                if (i == stops.get(0).size() - 1){
                    // Картинка для конечной останвоки
                    img.setImageDrawable(getResources().getDrawable(R.mipmap.route_end));
                }
                else {
                    // Картинка для обычной остановки по маршруту
                    img.setImageDrawable(getResources().getDrawable(R.mipmap.route_middle));
                }
            }

            // Для чётных и нечетных кнопок ставятся разные цвета фона
            if (i % 2 == 0)
                LL.setBackgroundResource(R.color.colorNumberBack);
            else
                LL.setBackgroundResource(R.color.colorButton);

            // Устанавливается текст остановки
            TextView txt = new TextView(this);
            txt.setText(stops.get(0).get(i).get(2));
            txt.setId(Integer.parseInt(stops.get(0).get(i).get(0)));
            txt.setTextSize(20);
            txt.setGravity(Gravity.BOTTOM);
            txt.setTextColor(getResources().getColor(R.color.textColor));

            // Картинка и текст устанавливаются на кнопку
            LL.addView(img, imgParams);
            LL.addView(txt, txtParams);
            LL.setId(Integer.parseInt(stops.get(0).get(i).get(0)));
            LL.setOnClickListener(this);

            // Кнопка устанавливается во вкладке прямого маршрута
            straight.addView(LL, llParams);
        }
        
        // Если есть остановки для обратного маршрута
        // Настраивается обратный маршрут так же как и прямой
        if (stops.size()>1) {
            for (int i = 0; i < stops.get(1).size(); i++) {
                LinearLayout LL = new LinearLayout(this);
                LL.setOrientation(LL.HORIZONTAL);
                LL.setBackgroundResource(R.color.colorButton);
                LL.setVerticalGravity(Gravity.CENTER_VERTICAL);

                ImageView img = new ImageView(this);

                if (i == 0){
                    img.setImageDrawable(getResources().getDrawable(R.mipmap.route_start));
                }
                else{
                    if (i == stops.get(1).size() - 1){
                        img.setImageDrawable(getResources().getDrawable(R.mipmap.route_end));
                    }
                    else {
                        img.setImageDrawable(getResources().getDrawable(R.mipmap.route_middle));
                    }
                }

                if (i % 2 == 0)
                    LL.setBackgroundResource(R.color.colorNumberBack);
                else
                    LL.setBackgroundResource(R.color.colorButton);

                TextView txt = new TextView(this);
                txt.setText(stops.get(1).get(i).get(2));
                txt.setId(Integer.parseInt(stops.get(1).get(i).get(0)));
                txt.setTextSize(20);
                txt.setGravity(Gravity.BOTTOM);
                txt.setTextColor(getResources().getColor(R.color.textColor));


                LL.addView(img, imgParams);
                LL.addView(txt, txtParams);
                LL.setId(Integer.parseInt(stops.get(1).get(i).get(0)));
                LL.setOnClickListener(this);

                reverse.addView(LL, llParams);
            }
        }
        // Если нет обратного маршрута, устанавливается соответствующая надпись
        else{
            TextView tv = new TextView(this);
            tv.setTextSize(24);
            tv.setTextColor(getResources().getColor(R.color.black));
            tv.setText("Извините, для данного маршрута нет информации.");
            reverse.addView(tv, btnsParams);
        }
    }

    public void goBack(View v){
        AdListener intAdListener = new AdListener(){
            @Override
            public void onAdClosed() {
                interstitialAd.loadAd(new AdRequest.Builder().build());
                finish();
            }
        };

        interstitialAd.setAdListener(intAdListener);
        ((MyApp) this.getApplication()).increaseClickCounter();

        if(((MyApp)this.getApplication()).getCounterValue() >= 6 && interstitialAd.isLoaded()){
            ((MyApp) this.getApplication()).clearCounter();
            interstitialAd.show();

        }
        else {
            finish();
        }
    }

    // Обработка возврата на предыдущее окно
    @Override
    public void onClick(final View v){

        AdListener intAdListener = new AdListener(){
            @Override
            public void onAdClosed(){
                interstitialAd.loadAd(new AdRequest.Builder().build());

                // Создаем интент для вызова следующего окна
                Intent intent = new Intent(v.getContext(), StopTime.class);
                // Передаем в следующее окно ID транспорта, маршрута и остановки, и название остановки
                intent.putExtra("transportID", transportID);
                intent.putExtra("stopID", v.getId());
                String text = ((TextView) (((LinearLayout) v).getChildAt(1))).getText().toString();
                intent.putExtra("stopName", text);
                // Запускаем следующее окно
                startActivity(intent);
            }
        };
        interstitialAd.setAdListener(intAdListener);
        ((MyApp) this.getApplication()).increaseClickCounter();

        if(((MyApp)this.getApplication()).getCounterValue() >= 6 && interstitialAd.isLoaded()){
            ((MyApp) this.getApplication()).clearCounter();
            interstitialAd.show();
        }
        else {

            ((MyApp) this.getApplication()).increaseClickCounter();
            // Создаем интент для вызова следующего окна
            Intent intent = new Intent(this, StopTime.class);
            // Передаем в следующее окно ID транспорта, маршрута и остановки, и название остановки
            intent.putExtra("transportID", transportID);
            intent.putExtra("stopID", v.getId());
            String text = ((TextView) (((LinearLayout) v).getChildAt(1))).getText().toString();
            intent.putExtra("stopName", text);
            // Запускаем следующее окно
            startActivity(intent);
        }

    }
}
