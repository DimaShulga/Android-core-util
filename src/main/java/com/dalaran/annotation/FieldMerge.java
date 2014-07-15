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

package com.dalaran.annotation;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class FieldMerge {
    public boolean merge(Activity a) {
        Field[] declaredFields = a.getClass().getDeclaredFields();
        boolean valid = false;
        for (Field field : declaredFields) {
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(ViewById.class)) {
                    boolean b = checkToMerge(a, field, (ViewById) annotation);
                    if (b) {
                        valid = b;
                    }
                }
            }
            Click annotation = field.getAnnotation(Click.class);
            if (annotation != null) {
                clickAnnotation(annotation,field , a);

            }
        }
        return valid;
    }

    private void clickAnnotation(Click annotation, Field field, final Activity a) {
        try {
            String method = annotation.value();
            final Method declaredMethod = a.getClass().getDeclaredMethod(method);
            View o = (View) field.get(a);
            o.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        declaredMethod.setAccessible(true);
                        declaredMethod.invoke(a);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private boolean checkToMerge(Activity a, Field field, ViewById viewById) {
        boolean valid = false;

        field.setAccessible(true);

        boolean b = checkFiled(a, viewById, field);
        if (b) {
            valid = b;
        }

        return valid;
    }

    private boolean checkFiled(Activity a, ViewById merge, Field field) {
        View viewById = a.findViewById(merge.value());

        try {
            field.set(a, viewById);
        } catch (Throwable e) {
            //should never happened
        }
        return false;
    }

    public boolean merge(Fragment fragment, View view) {
        Field[] declaredFields = fragment.getClass().getDeclaredFields();
        boolean valid = false;
        for (Field field : declaredFields) {
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(ViewById.class)) {
                    boolean b = checkToMerge(fragment, field, (ViewById) annotation, view);
                    if (b) {
                        valid = b;
                    }
                }
            }
        }
        return valid;
    }

    private boolean checkToMerge(Fragment fragment, Field field, ViewById annotation, View view) {
        field.setAccessible(true);
        View viewById = view.findViewById(annotation.value());

        try {
            field.set(fragment, viewById);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }
}
