// Окно с таблицей времени
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
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.List;
import java.util.TreeMap;

public class StopTime extends AppCompatActivity {

    // Объявление переменных класса
    int transportID;
    int stopID;
    TabHost tabHost;
    String stopName;
    LinearLayout weekdays;
    LinearLayout weekend;
    TextView stopTimeTitleText;
    ConstraintLayout titleStopTime;
    ImageView imgViewStopTime;
    LinearLayout table;
    AdView adViewStopTime;
    InterstitialAd interstitialAd;
    ScrollView scrollViewStopTimes;

    // Обработка создания окна
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stop_time);

        // Поиск идентификатора баннера
        adViewStopTime = findViewById(R.id.adViewStopTime);

        // Инициализация рекламы
        MobileAds.initialize(this,
                "##############################");

        // Загрузка межстраничного баннера
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("#################################");
        interstitialAd.loadAd(new AdRequest.Builder().build());

        // Загрузка реклманого баннера
        AdRequest adRequest = new AdRequest.Builder().build();
        adViewStopTime.loadAd(adRequest);

        // Параметры текста часов
        LinearLayout.LayoutParams hourParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        // Параметры текста минут
        ViewGroup.LayoutParams minuteParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        // Параметры текстовой вьюшки
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        // Параметры разметки
        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);


        // Ищем вьюшки по ID
        stopTimeTitleText = findViewById(R.id.stopTimeTitleText);
        titleStopTime = findViewById(R.id.titleStopTime);
        imgViewStopTime = findViewById(R.id.imgViewStopTime);
        tabHost = findViewById(R.id.stopTimeTabHost);
        weekdays = findViewById(R.id.tableWeekdays);
        weekend = findViewById(R.id.tableWeekend);

        // Получаем интент вызвавший это окно
        Intent intent = getIntent();
        // И вытаскиваем из него данные
        transportID = intent.getIntExtra("transportID", 0);
        stopID = intent.getIntExtra("stopID", 0);
        stopName = intent.getStringExtra("stopName");

        // Устанавливаем имя остановки в тайтл окна
        stopTimeTitleText.setText(stopName);

        // Настройка вкладок для будних и выходных
        tabHost.setup();
        TabHost.TabSpec tabSpec;

        // Вкладка будней
        tabSpec = tabHost.newTabSpec("weekdays");
        tabSpec.setIndicator("Будни");
        tabSpec.setContent(R.id.tab1StopTime);
        tabHost.addTab(tabSpec);

        // Вкладка выходных
        tabSpec = tabHost.newTabSpec("weekend");
        tabSpec.setIndicator("Выходные");
        tabSpec.setContent(R.id.tab2StopTime);
        tabHost.addTab(tabSpec);

        // Настройка текста на вкладках
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++){
            TextView tv = tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(getResources().getColor(R.color.black));
        }

        // По ID транспорта устанавливаем цвет и картинку заголовка окна
        switch (transportID) {
            case 1:
                titleStopTime.setBackgroundResource(R.color.colorTramvaj);
                stopTimeTitleText.setBackgroundResource(R.color.colorTramvaj);
                imgViewStopTime.setImageDrawable(getResources().getDrawable(R.mipmap.tram));
                break;
            case 2:
                titleStopTime.setBackgroundResource(R.color.colorTrolley);
                stopTimeTitleText.setBackgroundResource(R.color.colorTrolley);
                imgViewStopTime.setImageDrawable(getResources().getDrawable(R.mipmap.trolley));
                break;
            case 3:
                titleStopTime.setBackgroundResource(R.color.colorBus);
                stopTimeTitleText.setBackgroundResource(R.color.colorBus);
                imgViewStopTime.setImageDrawable(getResources().getDrawable(R.mipmap.bus));
                break;
            case 4:
                titleStopTime.setBackgroundResource(R.color.colorMarsh);
                stopTimeTitleText.setBackgroundResource(R.color.colorMarsh);
                imgViewStopTime.setImageDrawable(getResources().getDrawable(R.mipmap.marsh));
                break;
            case 5:
                titleStopTime.setBackgroundResource(R.color.colorTaxi);
                stopTimeTitleText.setBackgroundResource(R.color.colorTaxi);
                imgViewStopTime.setImageDrawable(getResources().getDrawable(R.mipmap.taxi));
                break;
            default:
                titleStopTime.setBackgroundResource(R.color.black);
                stopTimeTitleText.setBackgroundResource(R.color.black);
                break;
        }

        // Создаём объект для работы с БД
        DBHelper db = new DBHelper(this);

        // Вытаскиваем из БД таблицу времени
        int i = 1;
        List<TreeMap<Integer, List<Integer>>> day = db.getTimes(stopID);

        // Настраиваем первую запись в таблице времени на вкладке с буднями
        // LinearLayout в данном случае не является кнопкой, а просто содержит информацию
        LinearLayout LL = new LinearLayout(this);
        LL.setOrientation(LinearLayout.HORIZONTAL);
        LL.setVerticalGravity(Gravity.CENTER_VERTICAL);
        // Тектовая вьюшка будет хранить текст часов
        TextView hourTV = new TextView(this);
        // Устанавливается цвет текста в текстовой вьюшке
        hourTV.setTextColor(getResources().getColor(R.color.colorWhite));
        // Устанавливается размер текста
        hourTV.setTextSize(19);
        // Текст выравнивается по центру
        hourTV.setGravity(Gravity.CENTER);
        // Устанавливается цвет фона
        hourTV.setBackgroundResource(R.color.colorTableHour);
        // Устанавливается текст
        hourTV.setText("Часы");
        // Добавляем текстовую вьюшку на слой
        LL.addView(hourTV, hourParams);

        // Те же натсройки для текстовой вьюшки с минутами
        TextView minutesTV = new TextView(this);
        minutesTV.setTextColor(getResources().getColor(R.color.textColor));
        minutesTV.setTextSize(19);
        minutesTV.setText("Минуты");
        minutesTV.setGravity(Gravity.CENTER);
        minutesTV.setBackgroundResource(R.color.colorTableMinutes2);
        minutesTV.setPadding(0, 10, 0, 10);
        LL.addView(minutesTV, minuteParams);

        // Во вкладку с буднями добавляется первая строка таблицы
        weekdays.addView(LL, llParams);

        // Всё повторяется для вкладке с выходными
        LL = new LinearLayout(this);
        LL.setOrientation(LinearLayout.HORIZONTAL);
        LL.setVerticalGravity(Gravity.CENTER_VERTICAL);
        hourTV = new TextView(this);
        hourTV.setTextColor(getResources().getColor(R.color.colorWhite));
        hourTV.setTextSize(19);
        hourTV.setGravity(Gravity.CENTER);
        hourTV.setBackgroundResource(R.color.colorTableHour);
        hourTV.setText("Часы");
        LL.addView(hourTV, hourParams);

        minutesTV = new TextView(this);
        minutesTV.setTextColor(getResources().getColor(R.color.textColor));
        minutesTV.setTextSize(19);
        minutesTV.setText("Минуты");
        minutesTV.setGravity(Gravity.CENTER);
        minutesTV.setBackgroundResource(R.color.colorTableMinutes2);
        minutesTV.setPadding(0, 10, 0, 10);
        LL.addView(minutesTV, minuteParams);

        weekend.addView(LL, llParams);

        // Дальше в цикле отрисовываются остальные строки таблицы со временем
        for (int key:day.get(0).keySet()){
            // Строчки настраиваются также как и первая строка
            LL = new LinearLayout(this);
            LL.setOrientation(LinearLayout.HORIZONTAL);
            LL.setVerticalGravity(Gravity.CENTER_VERTICAL);
            hourTV = new TextView(this);
            hourTV.setTextColor(getResources().getColor(R.color.colorWhite));
            hourTV.setTextSize(30);
            hourTV.setText(String.format(" %02d ", key));
            hourTV.setGravity(Gravity.CENTER);
            hourTV.setBackgroundResource(R.color.colorTableHour);
            LL.addView(hourTV, hourParams);

            minutesTV = new TextView(this);
            minutesTV.setTextColor(getResources().getColor(R.color.textColor));
            minutesTV.setTextSize(30);
            String text = "";
            for (int minute: day.get(0).get(key)){
                text +=  String.format("%02d", minute) + "  ";
            }

            // Для чётных и нечётных строк устанавливается разный цвет фона
            if (i % 2 == 0) {
                minutesTV.setBackgroundResource(R.color.colorTableMinutes2);
            }
            else
                minutesTV.setBackgroundResource(R.color.colorTableMinutes1);
            i++;
            minutesTV.setText(text);
            minutesTV.setPadding(0, 10, 0, 10);
            LL.addView(minutesTV, minuteParams);

            weekdays.addView(LL, llParams);
        }
        // То же самое для вкладки выходных
        for (int key:day.get(1).keySet()){
            LL = new LinearLayout(this);
            LL.setOrientation(LinearLayout.HORIZONTAL);
            LL.setVerticalGravity(Gravity.CENTER_VERTICAL);
            hourTV = new TextView(this);
            hourTV.setTextColor(getResources().getColor(R.color.colorWhite));
            hourTV.setTextSize(30);
            hourTV.setText(String.format(" %02d ", key));
            hourTV.setGravity(Gravity.CENTER);
            hourTV.setBackgroundResource(R.color.colorTableHour);
            LL.addView(hourTV, hourParams);

            minutesTV = new TextView(this);
            minutesTV.setTextColor(getResources().getColor(R.color.textColor));
            minutesTV.setTextSize(30);
            String text = "";
            for (int minute: day.get(1).get(key)){
                text +=  String.format("%02d", minute) + "  ";
            }

            if (i % 2 == 0) {
                minutesTV.setBackgroundResource(R.color.colorTableMinutes2);
            }
            else
                minutesTV.setBackgroundResource(R.color.colorTableMinutes1);
            i++;
            minutesTV.setText(text);
            minutesTV.setPadding(0, 10, 0, 10);
            LL.addView(minutesTV, minuteParams);

            weekend.addView(LL, llParams);
        }

        // Отдельно обрабатываются дежурные маршруты
        String nightTimes = db.getNightTimes(stopID).trim();
        if (!nightTimes.equals("")){
            LL = new LinearLayout(this);
            LL.setOrientation(LinearLayout.HORIZONTAL);
            hourTV = new TextView(this);
            hourTV.setTextColor(getResources().getColor(R.color.colorWhite));
            hourTV.setTextSize(35);
            hourTV.setText("Дежурные: ");
            hourTV.setPadding(0, 20, 0, 0);
            hourTV.setGravity(Gravity.BOTTOM);
            LL.addView(hourTV, tvParams);
            LL.setBackgroundColor(getResources().getColor(R.color.colorTableMinutes2));

            minutesTV = new TextView(this);
            minutesTV.setTextColor(getResources().getColor(R.color.textColor));
            minutesTV.setTextSize(30);
            minutesTV.setText(nightTimes);
            minutesTV.setPadding(0, 20,  0, 20);
            minutesTV.setGravity(Gravity.CENTER_VERTICAL);
            LL.addView(minutesTV, tvParams);

            weekdays.addView(LL);
        }

    }

    // Обработка возврата на предыдущее окно
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

    @Override
    protected void onResume(){
        super.onResume();

        adViewStopTime.resume();
    }

    @Override
    protected void onPause(){
        super.onPause();

        adViewStopTime.pause();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        adViewStopTime.destroy();
    }
}
