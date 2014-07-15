/*
 * Copyright 2014 Dima Shulga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dalaran.annotation.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import com.dalaran.async.task.AbstractTask;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CreateTableProcessor extends AbstractTask<Set<String>, Boolean> {
    private SQLiteDatabase database;

    @Override
    public Boolean inBackground() throws Exception {
        return createTables();
    }

    public void dropTables() {
        try {
            List<Class<?>> items = findItems();
            for (Class<?> item : items) {
                String value = item.getAnnotation(Table.class).value();
                database.execSQL("DROP TABLE " + value);
            }


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Boolean createTables() throws ClassNotFoundException, IllegalAccessException {
        List<Class<?>> classList = findItems();

        Log.d("DB INFO", "We have " + classList.size() + " tables;");

        for (Class<?> aClass : classList) {
            createTable(aClass);
        }


        return null;
    }

    private List<Class<?>> findItems() throws ClassNotFoundException {
        Set<String> param = getParam();
        List<Class<?>> classList = new ArrayList<>();
        for (String url : param) {
            Class<?>[] interfaces = Class.forName(url).getInterfaces();
            for (Class<?> anInterface : interfaces) {
                if (findBaseColumns(anInterface)) {
                    classList.add(anInterface);
                }
            }
        }
        return classList;
    }

    private void createTable(Class<?> aClass) throws IllegalAccessException {
        String query = "\nCREATE TABLE ";
        query += createTableName(aClass);
        query += "(";

        query += createTableBody(aClass);

        query += ");";
        Log.d("DB INFO", query);
        database.execSQL(query);
    }

    private String createTableBody(Class<?> aClass) throws IllegalAccessException, ClassCastException {
        String afterBody = "";
        Table annotation1 = aClass.getAnnotation(Table.class);
        boolean autoIncrement = false;
        if (annotation1 != null) {
            autoIncrement = annotation1.isAutoIncrement();
        }
        String body = "\n\t_id integer primary key ";
        body += autoIncrement ? "autoincrement" : "";

        Field[] fields = aClass.getFields();
        for (Field field : fields) {
            Column annotation = field.getAnnotation(Column.class);
            if (annotation != null) {
                String o = (String) field.get(null);
                if (o.equals("_id")) {
                    continue;
                }
                body += ", \n\t`" + o + "` " + annotation.value().toString() + (annotation.canBeNull() ? "" : " NOT NULL");
                if (annotation.unique()) {
                    afterBody += ", \n\tUNIQUE(`" + o + "`) ";
                }
                if (annotation.index()) {
//                    afterBody += ", \n\tINDEX index_" + o + " (" + o + ") ";
                    //todo add create index for fields
                }
            }

        }

        return body += afterBody;
    }

    private String createTableName(Class<?> aClass) throws IllegalAccessException, ClassCastException {

        String tableName = null;
        Field[] declaredFields = aClass.getDeclaredFields();

        for (Field declaredField : declaredFields) {
            TableName annotation = declaredField.getAnnotation(TableName.class);
            if (annotation != null) {
                tableName = (String) declaredField.get(null);
            }
        }


        if (tableName == null) {
            String simpleName = aClass.getSimpleName();
            tableName = simpleName.replaceAll(
                    String.format("%s|%s|%s",
                            "(?<=[A-Z])(?=[A-Z][a-z])",
                            "(?<=[^A-Z])(?=[A-Z])",
                            "(?<=[A-Za-z])(?=[^A-Za-z])"
                    ),
                    "_"
            ).toLowerCase();
        }
        return tableName;
    }

    private boolean findBaseColumns(Class<?> anInterface) {
        if (anInterface == null) {
            return false;
        }
        boolean result = false;

        for (Class<?> aClass : anInterface.getInterfaces())
            if (aClass.equals(BaseColumns.class) && anInterface.getAnnotation(Table.class) != null) {
                return true;
            } else {
                if (findBaseColumns(aClass)) {
                    result = true;
                }
            }
        return result;
    }

    public void setDatabase(SQLiteDatabase database) {
        this.database = database;
    }


    public void updateTables() {
        try {
            List<Class<?>> classList = findItems();
            Map<String, Class<?>> tablesName = new HashMap<>();
            for (Class<?> aClass : classList) {
                tablesName.put(createTableName(aClass), aClass);
            }
            Cursor query = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            List<String> tableNames = new ArrayList<>();
            while (query.moveToNext()) {
                tableNames.add(query.getString(query.getColumnIndex("name")));
            }

            for (String tableName : tableNames) {
                try {
                    database.execSQL("DROP TABLE " + tableName);
                } catch (Throwable ignored) {
                }
            }

            for (String s : tablesName.keySet()) {
                try {
                    createTable(tablesName.get(s));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
