// Окно с маршрутами по транспорту
package com.projects.awawa.transporttimetable;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class Routes extends AppCompatActivity implements View.OnClickListener{

    // Объявляем вьюшки и ID транспорта
    int transportID;
    TextView routesTitleText;
    LinearLayout routesNumbers;
    ConstraintLayout titleRoutes;
    LinearLayout route_menu_layout;
    ImageView imgViewRoutes;
    InterstitialAd interstitialAd;
    ConstraintLayout routesContentLayout;
    AdView adView;
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    FirebaseAnalytics analytics;

    // Обработка октрытия окна
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Эта штука собирает статистику
        analytics = FirebaseAnalytics.getInstance(this);

        // Получаем интент вызвавший окно
        Intent intent = getIntent();
        transportID = intent.getIntExtra("transportID", 0);

        // Если вызывается окно для такси, отрисовывается специальный интерфейс
        if (transportID == 5){
            setContentView(R.layout.taxi);
            expListView = findViewById(R.id.expandbleListTaxi);

            prepareListData(); // Эта функция готовит преобрабатывает данные для вывода в окно такси

            // Настраиваются выпадающие списки
            listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

            expListView.setAdapter(listAdapter);

        }
        else{
            // Для остальных транспортов рисуется свой интерфейс
            setContentView(R.layout.routes);
        }

        // Задаем параметры кнопок для рисования
        LinearLayout.LayoutParams btnsParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        // Получаем вьюшку заголовка
        titleRoutes = findViewById(R.id.titleRoutes);
        routesTitleText = findViewById(R.id.routesTitleText);
        imgViewRoutes = findViewById(R.id.imgViewRoutes);
        route_menu_layout = findViewById(R.id.route_menu_layout);
        routesContentLayout = findViewById(R.id.routesContentLayout);


        // Получаем слой для размещения кнопок
        routesNumbers = findViewById(R.id.routesNumbers);

        // По ID транспорта задаем заголовок и цвет
        switch (transportID){
            case 1:
                titleRoutes.setBackgroundResource(R.color.colorTramvaj);
                routesTitleText.setText(getString(R.string.Tram));
                imgViewRoutes.setImageDrawable(getResources().getDrawable(R.mipmap.tram));
                break;
            case 2:
                titleRoutes.setBackgroundResource(R.color.colorTrolley);
                routesTitleText.setText(getString(R.string.Trolley));
                imgViewRoutes.setImageDrawable(getResources().getDrawable(R.mipmap.trolley));
                break;
            case 3:
                titleRoutes.setBackgroundResource(R.color.colorBus);
                routesTitleText.setText(getString(R.string.Bus));
                imgViewRoutes.setImageDrawable(getResources().getDrawable(R.mipmap.bus));
                break;
            case 4:
                titleRoutes.setBackgroundResource(R.color.colorMarsh);
                routesTitleText.setText(getString(R.string.Marsh));
                imgViewRoutes.setImageDrawable(getResources().getDrawable(R.mipmap.marsh));
                break;
            case 5:
                titleRoutes.setBackgroundResource(R.color.colorTaxi);
                routesTitleText.setText(getString(R.string.Taxi));
                imgViewRoutes.setImageDrawable(getResources().getDrawable(R.mipmap.taxi));
                break;
            default:
                titleRoutes.setBackgroundResource(R.color.black); // Дефолтное значение чёрное, если такое установилось - это нехорошо
                break;
        }
        // Поиск идентификатора рекламного баннера
        adView = findViewById(R.id.adViewRoutes);

        // Инициализация рекламы в окне
        MobileAds.initialize(this,
                "#########################");
        // Выгрузка рекламного баннера 
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        // Загрузка межстраничного баннера
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("########################");
        interstitialAd.loadAd(new AdRequest.Builder().build());

        // Для любого транспорта кроме такси выгружаются данные с БД
        if(transportID != 5){

            // Создаем объект для работы с БД
            DBHelper database = new DBHelper(this);

            // Получаем список маршрутов
            TreeMap<Integer, List<String>> routes = database.getRoutes(transportID);

            // Эта штука хранит настройки текста номера маршрута
            ViewGroup.LayoutParams numberParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, 4);
            // А эта штука настройки текста названия маршрута
            ViewGroup.LayoutParams txtParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1);

            // Дальше в цикле создаются кнопки
            for (int i = 0; i < routes.size(); i++) {
                // Здесь настраивается сама кнопка
                // LinearLayout используется вместо Button
                // Этот объект позволяет размещать в себе другие объекты 
                LinearLayout LL = new LinearLayout(this);
                // Здесь настраиватеся ориентация размещаемых объектов
                LL.setOrientation(LL.HORIZONTAL);
                // Здесь настраивается выравнивание (объекты будут выровнены по центру вертикали)
                LL.setVerticalGravity(Gravity.CENTER_VERTICAL);
                // Здесь устанавливается уникальный идентификатор кнопки
                LL.setId(Integer.parseInt(routes.get(i).get(0)));
                // Тут ставится цвет фона кнопки (по сути юзлес строка, потому что он переставляется дальше)
                LL.setBackgroundResource(R.color.colorButton);
                // Здесь устанавливаются отступы слева, справа, снизу и сверху
                LL.setPadding(0, 5, 0, 0);
                // Устанавливается цвет фона, для чётных строчек устанавливается один цвет, для нечётных - другой
                if (i % 2 == 0)
                    LL.setBackgroundResource(R.color.colorNumberBack);
                else
                    LL.setBackgroundResource(R.color.colorButton);

                // Создаём текстовую вьюшку для записи номера маршрута
                TextView numberTxt = new TextView(this);
                // Устанавливается цвет текста
                numberTxt.setTextColor(getResources().getColor(R.color.colorRed));
                // Устанавливается размер текста
                numberTxt.setTextSize(25);
                // Устанавливается текст
                numberTxt.setText(routes.get(i).get(1).trim());
                // Выравнивание по центру (по вертикали и горизонтали одновременно)
                numberTxt.setGravity(Gravity.CENTER);

                // Те же действия проводятся для названия маршрута
                TextView routeName = new TextView(this);
                routeName.setTextColor(getResources().getColor(R.color.textColor));
                routeName.setTextSize(20);
                routeName.setText(routes.get(i).get(2).trim());
                routeName.setGravity(Gravity.LEFT);
                // Тут устанавливаются отступы
                routeName.setPadding(30, 0, 0, 0);

                // Теперь в кнопку вставляется сначала номер маршрута, затем название маршрута
                LL.addView(numberTxt, numberParams);
                LL.addView(routeName, txtParams);
                // Устанавливается объект, отслеживающий нажатия на кнопку
                LL.setOnClickListener(this);
                // В конце концов кнопка добавляется в окно
                routesNumbers.addView(LL, btnsParams);
            }
        }
    }

    // Обработка кнопки возврата
    public void goBack(View v){

        // Прослушивание поведения межстраничного баннера
        AdListener intAdListener = new AdListener(){

            // При закрытии баннера грузится новый баннер и окно закрывается
            @Override
            public void onAdClosed() {
                interstitialAd.loadAd(new AdRequest.Builder().build());
                finish();
            }
        };

        // Привзяка прослушки к баннеру
        interstitialAd.setAdListener(intAdListener);

        // Увеличение кол-ва кликов на 1
        ((MyApp) this.getApplication()).increaseClickCounter();
        // Если кол-во кликов 6 и больше
        if(((MyApp)this.getApplication()).getCounterValue() >= 6 && interstitialAd.isLoaded()){
            // Зануление счётчика
            ((MyApp) this.getApplication()).clearCounter();
            // Вывод межстраничного баннера
            interstitialAd.show();

        }
        else {
            finish();
        }
    }

    // Функция преподготовки данных для вывода в окно такси
    private void prepareListData(){

        // Список хранит заголовки выпадающих списков
        listDataHeader = new ArrayList<>();
        // Хеш таблица ставит заголовку в соответствие список номеров телефонов
        listDataChild = new HashMap<>();

        listDataHeader.add("1 Республиканское ] 80 руб. за 5 км, далее 12 руб/км");
        listDataHeader.add("Эталон ] (круглосуточно)");
        listDataHeader.add("Фаэтон ] ");
        listDataHeader.add("Приват ] 80 руб. за 5 км, далее 12 руб/км");
        listDataHeader.add("Эконом ] ");
        listDataHeader.add("А Такси ] 74 руб. за 5 км, далее 12 руб/км");
        listDataHeader.add("Такси Дн ] 75 руб. за 4 км, далее 12 руб/км");
        listDataHeader.add("А Уедь ] 70 руб. за 5 км, далее 12 руб/км");
        listDataHeader.add("Алло ] 75 руб. за 5 км, далее 12 руб/км");
        listDataHeader.add("Донбасс ] 75 руб. за 5 км, далее 12 руб/км");
        listDataHeader.add("Регион ] (круглосуточно)");
        listDataHeader.add("Новое ] ");


        List<String> firstResp = new ArrayList<>();

        firstResp.add("0505251515");
        firstResp.add("0665992146");
        firstResp.add("0713465884");
        firstResp.add("555");

        List<String> etalon = new ArrayList<>();

        etalon.add("0504886262");

        List<String> faeton = new ArrayList<>();

        faeton.add("0506427772");
        faeton.add("0505806801");
        faeton.add("333");

        List<String> privat = new ArrayList<>();

        privat.add("0503451000");
        privat.add("0508822222");
        privat.add("0503477347");
        privat.add("0710999999");

        List<String> ekonom = new ArrayList<>();

        ekonom.add("0953802112");
        ekonom.add("0504751619");
        ekonom.add("0713802112");
        ekonom.add("0714751619");

        List<String> ataksi = new ArrayList<>();

        ataksi.add("0718001818");
        ataksi.add("0668001818");
        ataksi.add("0944601265");

        List<String> taksidn = new ArrayList<>();

        taksidn.add("0504121919");
        taksidn.add("0717023000");

        List<String> aued = new ArrayList<>();

        aued.add("0502777734");
        aued.add("0633025586");

        List<String> allo = new ArrayList<>();

        allo.add("0996525099");
        allo.add("0713450095");

        List<String> donbass = new ArrayList<>();

        donbass.add("0662409594");
        donbass.add("0664652322");

        List<String> region = new ArrayList<>();

        region.add("0502100505");
        region.add("0958509999");
        region.add("0718509999");
        region.add("0713042777");

        List<String> novoe = new ArrayList<>();

        novoe.add("0991197005");
        novoe.add("0713104455");

        listDataChild.put(listDataHeader.get(0), firstResp);
        listDataChild.put(listDataHeader.get(1), etalon);
        listDataChild.put(listDataHeader.get(2), faeton);
        listDataChild.put(listDataHeader.get(3), privat);
        listDataChild.put(listDataHeader.get(4), ekonom);
        listDataChild.put(listDataHeader.get(5), ataksi);
        listDataChild.put(listDataHeader.get(6), taksidn);
        listDataChild.put(listDataHeader.get(7), aued);
        listDataChild.put(listDataHeader.get(8), allo);
        listDataChild.put(listDataHeader.get(9), donbass);
        listDataChild.put(listDataHeader.get(10), region);
        listDataChild.put(listDataHeader.get(11), novoe);
    }

    // Обработка нажатий на кнопку
    @Override
    public void onClick(final View v){

        // Всё та же прослушка межстраничного баннера
        AdListener intAdListener = new AdListener(){
            @Override
            public void onAdClosed() {
                interstitialAd.loadAd(new AdRequest.Builder().build());

                // Создаем интент для вызова следующего окна
                Intent intent = new Intent(v.getContext(), Stops.class);

                // Добавляем в интент ID транспорта и маршрута
                intent.putExtra("transportID", transportID);
                intent.putExtra("routeID", v.getId());
                String text = ((TextView) (((LinearLayout) v).getChildAt(1))).getText().toString();
                intent.putExtra("routeNumber", text);


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

            // Создаем интент для вызова следующего окна
            Intent intent = new Intent(this, Stops.class);

            // Добавляем в интент ID транспорта и маршрута
            intent.putExtra("transportID", transportID);
            intent.putExtra("routeID", v.getId());
            // А также добавляем номер маршрута
            String text = ((TextView) (((LinearLayout) v).getChildAt(0))).getText().toString();
            intent.putExtra("routeNumber", text);

            // Запускаем следующее окно
            startActivity(intent);
        }
    }

    // Функция обработки звонка в такси
    public void call(final View v){

        // Прослушка межстраничного баннера
        AdListener intAdListener = new AdListener(){
            @Override
            public void onAdClosed() {
                interstitialAd.loadAd(new AdRequest.Builder().build());

                String phone;
                phone = ((TextView)((LinearLayout) v.getParent()).getChildAt(0)).getText().toString();
                // Эта длинная портянка просто следит за тем на какой номер звонили и собирает лог для статистики
                String log;
                switch (phone){
                    case "0505251515":
                        log = "1Resp-1";
                        break;
                    case "0665992146":
                        log = "1Resp-2";
                        break;
                    case "0713465884":
                        log = "1Resp-3";
                        break;
                    case "555":
                        log = "1Resp-4";
                        break;
                    case "0504886262":
                        log = "Etalon-1";
                        break;
                    case "0506427772":
                        log = "Faeton-1";
                        break;
                    case "0505806801":
                        log = "Faeton-2";
                        break;
                    case "333":
                        log = "Faeton-3";
                        break;
                    case "0503451000":
                        log = "Privat-1";
                        break;
                    case "0508822222":
                        log = "Privat-2";
                        break;
                    case "0503477347":
                        log = "Privat-3";
                        break;
                    case "0710999999":
                        log = "Priat-4";
                        break;
                    case "0953802112":
                        log = "Ekonom-1";
                        break;
                    case "0504751619":
                        log = "Ekonom-2";
                        break;
                    case "0713802112":
                        log = "Ekonom-3";
                        break;
                    case "0714751619":
                        log = "Ekonom-4";
                        break;
                    case "0718001818":
                        log = "ATaxi-1";
                        break;
                    case "0668001818":
                        log = "ATaxi-2";
                        break;
                    case "0944601265":
                        log = "ATaxi-3";
                        break;
                    case "0504121919":
                        log = "TaxiDN-1";
                        break;
                    case "0717023000":
                        log = "TaxiDN-2";
                        break;
                    case "0502777734":
                        log = "Aued-1";
                        break;
                    case "0633025586":
                        log = "Aued-2";
                        break;
                    case "0996525099":
                        log = "Allo-1";
                        break;
                    case "0713450095":
                        log = "Allo-2";
                        break;
                    case "0662409594":
                        log = "Donbass-1";
                        break;
                    case "0664652322":
                        log = "Donbass-2";
                        break;
                    case "0502100505":
                        log = "Region-1";
                        break;
                    case "0958509999":
                        log = "Region-2";
                        break;
                    case "0718509999":
                        log = "Region-3";
                        break;
                    case "0713042777":
                        log = "Region-4";
                        break;
                    case "0991197005":
                        log = "Novoe-1";
                        break;
                    case "0713104455":
                        log = "Novoe-2";
                        break;
                    default:
                        log = "Unknown";
                }

                // Тут запаковывается статистика и отправляется в облако
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, log);
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Button");
                bundle.putString("number", log);
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Phone call");
                analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                // Ищется приложение, которое умеет обрабатывать номера телефонов и пользователь выбирает какое использовать
                Intent myIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
                myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.CALL_PHONE) ==
                        PackageManager.PERMISSION_GRANTED){
                    startActivity(myIntent);
                }
            }
        };
        interstitialAd.setAdListener(intAdListener);
        ((MyApp) this.getApplication()).increaseClickCounter();

        if(((MyApp)this.getApplication()).getCounterValue() >= 6 && interstitialAd.isLoaded()){
            ((MyApp) this.getApplication()).clearCounter();
            interstitialAd.show();

        }
        else {
            String phone;
            phone = ((TextView) ((LinearLayout) v.getParent()).getChildAt(0)).getText().toString();
            String log;
            switch (phone){
                case "0505251515":
                    log = "1Resp-1";
                    break;
                case "0665992146":
                    log = "1Resp-2";
                    break;
                case "0713465884":
                    log = "1Resp-3";
                    break;
                case "555":
                    log = "1Resp-4";
                    break;
                case "0504886262":
                    log = "Etalon-1";
                    break;
                case "0506427772":
                    log = "Faeton-1";
                    break;
                case "0505806801":
                    log = "Faeton-2";
                    break;
                case "333":
                    log = "Faeton-3";
                    break;
                case "0503451000":
                    log = "Privat-1";
                    break;
                case "0508822222":
                    log = "Privat-2";
                    break;
                case "0503477347":
                    log = "Privat-3";
                    break;
                case "0710999999":
                    log = "Priat-4";
                    break;
                case "0953802112":
                    log = "Ekonom-1";
                    break;
                case "0504751619":
                    log = "Ekonom-2";
                    break;
                case "0713802112":
                    log = "Ekonom-3";
                    break;
                case "0714751619":
                    log = "Ekonom-4";
                    break;
                case "0718001818":
                    log = "ATaxi-1";
                    break;
                case "0668001818":
                    log = "ATaxi-2";
                    break;
                case "0944601265":
                    log = "ATaxi-3";
                    break;
                case "0504121919":
                    log = "TaxiDN-1";
                    break;
                case "0717023000":
                    log = "TaxiDN-2";
                    break;
                case "0502777734":
                    log = "Aued-1";
                    break;
                case "0633025586":
                    log = "Aued-2";
                    break;
                case "0996525099":
                    log = "Allo-1";
                    break;
                case "0713450095":
                    log = "Allo-2";
                    break;
                case "0662409594":
                    log = "Donbass-1";
                    break;
                case "0664652322":
                    log = "Donbass-2";
                    break;
                case "0502100505":
                    log = "Region-1";
                    break;
                case "0958509999":
                    log = "Region-2";
                    break;
                case "0718509999":
                    log = "Region-3";
                    break;
                case "0713042777":
                    log = "Region-4";
                    break;
                case "0991197005":
                    log = "Novoe-1";
                    break;
                case "0713104455":
                    log = "Novoe-2";
                    break;
                default:
                    log = "Unknown";
            }

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, log);
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Button");
            bundle.putString("number", log);
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Phone call");
            analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent myIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) ==
                    PackageManager.PERMISSION_GRANTED) {
                startActivity(myIntent);
            }
        }
    }

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
}
