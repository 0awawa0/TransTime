// Эта штука нужна для работы с БД
package com.projects.awawa.transporttimetable;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;


// Класс для работы с базой данных (все запросы к бд идут через него)
public class DBHelper extends SQLiteAssetHelper {

    // DB_NAME - имя файла базы данных
    private static final String DB_NAME = "database.db";
    // DB_VERSION - номер версии базы данных (нужно для накатывания обновлений БД)
    private static final int DB_VERSION = 3;

    // Конструктор
    public DBHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
        setForcedUpgrade();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
    }

    // Функция получения списка маршрутов из БД
    // Принимает transportID - идентификатор транспорта
    // 1 - трамвай, 2 - троллейбус, 3 - автобус, 4 - маршрутка
    public TreeMap<Integer, List<String>> getRoutes(int transportID){

        // Хранит ответ от базы данных
        HashMap<Integer, List<String>> response = new HashMap<>();

        // Формируем SQL-запрос в БД
        String request = "select RouteID, RouteNumber, RouteName from routes where TransportID="+
                String.valueOf(transportID) + " and Reverse=0 order by RouteNumber";

        // Получаем объект БД
        SQLiteDatabase db = this.getReadableDatabase();

        // Получаем курсор для доступа к таблице ответа от БД
        Cursor cursor = db.rawQuery(request, null);

        // Переменная-ключ для словаря
        int i = 0;

        // Список строк - объект словаря ответа
        List<String> response_row;

        // Обрабатываем ответ от БД
        // Если ответ не пуст
        if (cursor.moveToFirst()){
            // В словарь response по ключу i добавляем массив строк, который
            // содержит ID маршрута, номер маршрута и название маршрута
            // в индексах 0, 1 и 2 соответственно
            do{
                response_row = new ArrayList<>();
                response_row.add(cursor.getString(0));
                response_row.add(cursor.getString(1));
                response_row.add(cursor.getString(2));
                response.put(i, response_row);
                i++;
            }while (cursor.moveToNext());
        }

        // Закрываем курсор и БД
        cursor.close();
        db.close();

        // Сортируем ответ и возвращаем
        TreeMap<Integer, List<String>> sorted = new TreeMap<>(response);
        return sorted;
    }

    // Метод для получения списка остановок
    // Принимает routeID - идентификатор маршрута, получается из БД
    public List<HashMap<Integer, List<String>>> getStops(String routeNumber, int transportID, int routeID){

        // Список словарей - возвращаемый словарь
        List<HashMap<Integer, List<String>>> to_return = new ArrayList<>();

        // Объет для хранения ответа от БД
        HashMap<Integer, List<String>> response = new HashMap<>();

        // Формируем SQL-запрос для получения списка остановок для прямого маршрута
        String request = "select StopID, StopNumber, StopName from stops where" +
                " RouteID=" + String.valueOf(routeID) + " order by StopNumber";

        // Получаем объект БД
        SQLiteDatabase db = this.getReadableDatabase();

        // Получаем курсор для обработки ответа от БД
        Cursor cursor = db.rawQuery(request, null);

        // Переменная-ключ для словаря
        int i = 0;

        // Строка ответа
        List<String> response_row;

        // Если ответ от БД не пустой
        if (cursor.moveToFirst()){

            // Формируем словарь по ключу i, элементами которого являются массивы строк.
            // Один массив содержит ID остановки, порядковый номер остановки по маршруту, название
            // остановки по индексам 0, 1 и 2 соответственно
            do{
                response_row = new ArrayList<>();
                response_row.add(cursor.getString(0));
                response_row.add(cursor.getString(1));
                response_row.add(cursor.getString(2));
                response.put(i, response_row);
                i++;
            }while (cursor.moveToNext());
        }

        // Закрываем курсор
        cursor.close();

        // В возврат добавляем словарь ответа
        to_return.add(response);

        // Формируем SQL-запрос в БД
        request = "select RouteID from routes where TransportID="+
                String.valueOf(transportID) + " and RouteNumber='" + routeNumber +"' and Reverse=1 order by RouteNumber";

        // Получаем курсор для доступа к таблице ответа от БД
        cursor = db.rawQuery(request, null);

        if (cursor.moveToFirst()){
            routeID = cursor.getInt(0);
        }
        else {
            cursor.close();
            db.close();
            return to_return;
        }
        // Создаём новый объект для ответа
        response = new HashMap<>();

        // Создаём новый запрос для сбора списка остановок по обратному маршруту

        request = "select StopID, StopNumber, StopName from stops where" +
                " RouteID=" + String.valueOf(routeID) + " order by StopNumber";

        // Получаем курсор для обработки ответа
        cursor = db.rawQuery(request, null);

        // Переменная-ключ для словаря
        i = 0;

        // Если ответ не пустой
        if (cursor.moveToFirst()){
            // Заполняем словарь также как и для прямого маршрута
            do{
                response_row = new ArrayList<>();
                response_row.add(cursor.getString(0));
                response_row.add(cursor.getString(1));
                response_row.add(cursor.getString(2));
                response.put(i, response_row);
                i++;
            }while (cursor.moveToNext());
        }

        // Закрываем курсор и БД
        cursor.close();
        db.close();

        // Добавляем ответ к возвращаемому массиву
        to_return.add(response);

        // Возвращаем ответ
        return to_return;
    }

    // Функция для получения таблицы времени
    public List<TreeMap<Integer, List<Integer>>> getTimes(int stopID){

        // Массивы для хранения значений часов и минут по отдельности
        List<Integer> hours = new ArrayList<>();
        List<Integer> minutes = new ArrayList<>();

        // Формируем запрос для получения таблицы времени для дневного маршрута
        String request = "select StopTime, Interval from times where StopID=" +
                String.valueOf(stopID) + " and Night=0 and Weekend=0";

        // Получаем объект БД
        SQLiteDatabase db = this.getReadableDatabase();

        // Получаем курсор для обработки ответа от БД
        Cursor cursor = db.rawQuery(request, null);

        // Если ответ не пустой
        if (cursor.moveToFirst()) {
            // Идём по таблице ответа
            for (int i = 0; i < cursor.getCount() - 1; i++) {

                // Получаем время, разделяя значение часов и минут
                String[] timeNow = cursor.getString(0).split(":");

                // Отдельно храним числовое значение часов, минут и интервал отправлений
                int hoursNow = Integer.parseInt(timeNow[0].trim());
                int minutesNow = Integer.parseInt(timeNow[1].trim());
                int interval = cursor.getInt(1);

                // Переходим на следующую строку ответа
                cursor.moveToNext();

                // Разделяем также значение часов и минут для следующей записи
                String[] timeNext = cursor.getString(0).split(":");

                // Отдельно храним также значения часов и минут
                int hoursNext = Integer.parseInt(timeNext[0].trim());
                int minutesNext = Integer.parseInt(timeNext[1].trim());

                // Пока не дойдём до следующего значения времени,
                // добавляем интервал к текущему времени
                while (true) {

                    // Если текущее значение часов равно следующему
                    if (hoursNow >= hoursNext) {

                        // И если значения минут также равны
                        if (minutesNow >= minutesNext) {
                            // Останавливаем цикл
                            break;
                            // Иначе, в списки часов и минут добавляем текущее значение и
                            // к минутам добавляем интервал
                        } else {
                            hours.add(hoursNow);
                            minutes.add(minutesNow);
                            minutesNow += interval;
                        }
                        // Если значения часов не равно
                    } else {
                        // В списки часов и минут добавлям текущее значение и к минутам добавляем
                        // интервал
                        hours.add(hoursNow);
                        minutes.add(minutesNow);
                        minutesNow += interval;
                        // Если значение минут перевалило за 59, отнимаем от этого значения 60
                        // А к часам добавляем 1
                        while (minutesNow > 59) {
                            hoursNow++;
                            minutesNow -= 60;
                        }
                    }
                }
            }
            // В конце концов добавляем к спискам часов и минут последнюю строку ответа
            hours.add(Integer.parseInt(cursor.getString(0).split(":")[0].trim()));
            minutes.add(Integer.parseInt(cursor.getString(0).split(":")[1].trim()));
        }

        // Создаём словарь для хранения возврата из функции
        List<TreeMap<Integer, List<Integer>>> to_return = new ArrayList<>();
        HashMap<Integer, List<Integer>> tab = new HashMap<>();

        // Идём по часам
        for (int i = 0; i < hours.size(); i++){
            // Получаем i значение часов
            int hoursOld = hours.get(i);
            // Создаём список значений минут для этого значения часов
            List<Integer> minutes_to_hours = new ArrayList<>();

            // Пока i не перевалило за размер списка часов
            while (true && i < hours.size()){
                // Если дошли до последнего элемента
                if (i == hours.size()-1){
                    // Добавляем значение минут к списку и выходим из цикла
                    minutes_to_hours.add(minutes.get(i));
                    break;
                }
                // Получаем следующее значение часов
                int hoursNew = hours.get(i+1);
                // Если оно равно текущему
                if (hoursOld == hoursNew){
                    // Добавляем текущее значение минут к списку и продолжаем цикл
                    minutes_to_hours.add(minutes.get(i));
                }
                else{
                    // Иначе добавляем текущее значение минут к списку и выходим из цикла
                    minutes_to_hours.add(minutes.get(i));
                    break;
                }
                // Увеличиваем счетчик
                i++;
            }
            // Добавляем в переменную возврата значение часов как ключ и список значений минут
            // в качетсве элементов
            tab.put(hoursOld, minutes_to_hours);
        }
        // Вся таблица сортируется и добавляется в словарь для возврата
        TreeMap<Integer, List<Integer>> sorted_tab = new TreeMap<>(tab);
        to_return.add(sorted_tab);

        // Дальше делается вся та же чепуха, только для выходных
        tab = new HashMap<>();

        request = "select StopTime, Interval from times where StopID=" +
                String.valueOf(stopID) + " and Night=0 and Weekend=1";

        hours = new ArrayList<>();
        minutes = new ArrayList<>();

        cursor = db.rawQuery(request, null);

        if (cursor.moveToFirst()){
            for(int i = 0; i < cursor.getCount() - 1; i++){

                String[] timeNow = cursor.getString(0).split(":");

                int hoursNow = Integer.parseInt(timeNow[0].trim());
                int minutesNow = Integer.parseInt(timeNow[1].trim());
                int interval = cursor.getInt(1);

                cursor.moveToNext();

                String[] timeNext = cursor.getString(0).split(":");

                int hoursNext = Integer.parseInt(timeNext[0].trim());
                int minutesNext = Integer.parseInt(timeNext[1].trim());

                while(true){

                    if (hoursNow >= hoursNext){

                        if (minutesNow >= minutesNext){
                            break;
                        }
                        else{
                            hours.add(hoursNow);
                            minutes.add(minutesNow);
                            minutesNow += interval;
                        }
                    }
                    else {
                        hours.add(hoursNow);
                        minutes.add(minutesNow);
                        minutesNow += interval;

                        while (minutesNow > 59){
                            hoursNow++;
                            minutesNow -= 60;
                        }
                    }
                }
            }

            hours.add(Integer.parseInt(cursor.getString(0).split(":")[0].trim()));
            minutes.add(Integer.parseInt(cursor.getString(0).split(":")[1].trim()));
        }

        // Идём по часам
        for (int i = 0; i < hours.size(); i++){
            // Получаем i значение часов
            int hoursOld = hours.get(i);
            // Создаём список значений минут для этого значения часов
            List<Integer> minutes_to_hours = new ArrayList<>();

            // Пока i не перевалило за размер списвка часов
            while (true && i < hours.size()){
                // Если дошли до последнего элемента
                if (i == hours.size()-1){
                    // Добавляем значение минут к списку и выходим из цикла
                    minutes_to_hours.add(minutes.get(i));
                    break;
                }
                // Получаем следующее значение часов
                int hoursNew = hours.get(i+1);
                // Если оно равно текущему
                if (hoursOld == hoursNew){
                    // Добавляем текущее значение минут к списку и продолжаем цикл
                    minutes_to_hours.add(minutes.get(i));
                }
                else{
                    // Иначе добавляем текущее значение минут к списку и выходим из цикла
                    minutes_to_hours.add(minutes.get(i));
                    break;
                }
                // Увеличиваем счетчик
                i++;
            }
            // Добавляем в переменную возврата значение часов как ключ и список значений минут
            // в качетсве элементов
            tab.put(hoursOld, minutes_to_hours);
        }

        sorted_tab = new TreeMap<>(tab);
        to_return.add(sorted_tab);

        // Закрываем курсор и БД
        cursor.close();
        db.close();

        // Возвращаем
        return to_return;
    }

    // Функция для получения таблицы времени (Она мб и не нужна, но пусть полежит)
    public TreeMap<Integer, List<Integer>> getTimes1(int stopID){

        // Массивы для хранения значений часов и минут по отдельности
        List<Integer> hours = new ArrayList<>();
        List<Integer> minutes = new ArrayList<>();

        // Формируем запрос для получения таблицы времени для дневного маршрута
        String request = "select StopTime, Interval from times where StopID=" +
                String.valueOf(stopID) + " and Night=0 and Weekend=0";

        // Получаем объект БД
        SQLiteDatabase db = this.getReadableDatabase();

        // Получаем курсор для обработки ответа от БД
        Cursor cursor = db.rawQuery(request, null);

        // Если ответ не пустой
        if (cursor.moveToFirst()) {
            // Идём по таблице ответа
            for (int i = 0; i < cursor.getCount() - 1; i++) {

                // Получаем время, разделяя значение часов и минут
                String[] timeNow = cursor.getString(0).split(":");

                // Отдельно храним числовое значение часов, минут и интервал отправлений
                int hoursNow = Integer.parseInt(timeNow[0].trim());
                int minutesNow = Integer.parseInt(timeNow[1].trim());
                int interval = cursor.getInt(1);

                // Переходим на следующую строку ответа
                cursor.moveToNext();

                // Разделяем также значение часов и минут для следующей записи
                String[] timeNext = cursor.getString(0).split(":");

                // Отдельно храним также значения часов и минут
                int hoursNext = Integer.parseInt(timeNext[0].trim());
                int minutesNext = Integer.parseInt(timeNext[1].trim());

                // Пока не дойдём до следующего значения времени,
                // добавляем интервал к текущему времени
                while (true) {

                    // Если текущее значение часов равно следующему
                    if (hoursNow >= hoursNext) {

                        // И если значения минут также равны
                        if (minutesNow >= minutesNext) {
                            // Останавливаем цикл
                            break;
                            // Иначе, в списки часов и минут добавляем текущее значение и
                            // к минутам добавляем интервал
                        } else {
                            hours.add(hoursNow);
                            minutes.add(minutesNow);
                            minutesNow += interval;
                        }
                        // Если значения часов не равно
                    } else {
                        // В списки часов и минут добавлям текущее значение и к минутам добавляем
                        // интервал
                        hours.add(hoursNow);
                        minutes.add(minutesNow);
                        minutesNow += interval;
                        // Если значение минут перевалило за 59, отнимаем от этого значения 60
                        // А к часам добавляем 1
                        while (minutesNow > 59) {
                            hoursNow++;
                            minutesNow -= 60;
                        }
                    }
                }
            }
            // В конце концов добавляем к спискам часов и минут последнюю строку ответа
            hours.add(Integer.parseInt(cursor.getString(0).split(":")[0].trim()));
            minutes.add(Integer.parseInt(cursor.getString(0).split(":")[1].trim()));
        }

        // Создаём словарь для хранения возврата из функции
        HashMap<Integer, List<Integer>> to_return = new HashMap<>();

        // Идём по часам
        for (int i = 0; i < hours.size(); i++){
            // Получаем i значение часов
            int hoursOld = hours.get(i);
            // Создаём список значений минут для этого значения часов
            List<Integer> minutes_to_hours = new ArrayList<>();

            // Пока i не перевалило за размер списвка часов
            while (true && i < hours.size()){
                // Если дошли до последнего элемента
                if (i == hours.size()-1){
                    // Добавляем значение минут к списку и выходим из цикла
                    minutes_to_hours.add(minutes.get(i));
                    break;
                }
                // Получаем следующее значение часов
                int hoursNew = hours.get(i+1);
                // Если оно равно текущему
                if (hoursOld == hoursNew){
                    // Добавляем текущее значение минут к списку и продолжаем цикл
                    minutes_to_hours.add(minutes.get(i));
                }
                else{
                    // Иначе добавляем текущее значение минут к списку и выходим из цикла
                    minutes_to_hours.add(minutes.get(i));
                    break;
                }
                // Увеличиваем счетчик
                i++;
            }
            // Добавляем в переменную возврата значение часов как ключ и список значений минут
            // в качетсве элементов
            to_return.put(hoursOld, minutes_to_hours);
        }

        // Закрываем курсор и БД
        cursor.close();
        db.close();

        // Сортируем ответ
        TreeMap<Integer, List<Integer>> sorted = new TreeMap<>(to_return);

        // Возвращаем
        return sorted;
    }

    // Функция для получения списка времени дежурных остановок для данной остановки
    public String getNightTimes(int stopID){
        // Формируем запрос
        String request = "select StopTime from times where StopID=" +
                String.valueOf(stopID) + " and Night=1";

        // Получаем объект БД
        SQLiteDatabase db = this.getReadableDatabase();
        // Получаем курсор для обработки ответа
        Cursor cursor = db.rawQuery(request, null);

        // Формируем выходную строку
        String night = "";
        if (cursor.moveToFirst()) {
            do {
                night += cursor.getString(0) + "\n";
            } while (cursor.moveToNext());
        }
        // Закрываем курсор и БД
        cursor.close();
        db.close();

        // Возвращаем отформатированную строку
        return night;
    }
}
