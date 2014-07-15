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

package com.dalaran.annotation.validator;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.widget.EditText;

import com.dalaran.annotation.validator.annotations.Email;
import com.dalaran.annotation.validator.annotations.EqualsWith;
import com.dalaran.annotation.validator.annotations.Length;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.regex.Pattern;

/**
 * Created by dima on 5/23/13.
 */
public class Validator {
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    public boolean validate(Activity a) {
        Field[] declaredFields = a.getClass().getDeclaredFields();
        boolean valid = false;
        for (Field field : declaredFields) {
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(Length.class)) {
                    boolean b = checkToValidate(a, field, (Length) annotation);
                    if (b) {
                        valid = b;
                    }
                }
                if (annotation.annotationType().equals(Email.class)) {
                    boolean b = checkToValidateEmail(a, field, (Email) annotation);
                    if (b) {
                        valid = b;
                    }
                }
                if (annotation.annotationType().equals(EqualsWith.class)) {
                    boolean b = checkToValidateEquals(a, field, (EqualsWith) annotation);
                    if (b) {
                        valid = b;
                    }
                }
            }
        }
        return valid;
    }

    public boolean validate(Fragment a) {
        Field[] declaredFields = a.getClass().getDeclaredFields();
        boolean valid = false;
        for (Field field : declaredFields) {
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(Length.class)) {
                    boolean b = checkToValidate(a, field, (Length) annotation);
                    if (b) {
                        valid = b;
                    }
                }
                if (annotation.annotationType().equals(Email.class)) {
                    boolean b = checkToValidateEmail(a, field, (Email) annotation);
                    if (b) {
                        valid = b;
                    }
                }
                if (annotation.annotationType().equals(EqualsWith.class)) {
                    boolean b = checkToValidateEquals(a.getActivity(), field, (EqualsWith) annotation);
                    if (b) {
                        valid = b;
                    }
                }
            }
        }
        return valid;
    }

    private boolean checkToValidateEquals(Activity a, Field field, EqualsWith annotation) {
        boolean valid = false;
        EqualsWith length = annotation;
        field.setAccessible(true);
        try {
            Object o = field.get(a);
            if (o instanceof EditText) {
                boolean b = checkFiledToEquals(a, length, (EditText) o);
                valid = b;
            }
        } catch (IllegalAccessException ignored) {
        }
        return valid;
    }

    private boolean checkFiledToEquals(Activity a, EqualsWith length, EditText o) {
        o.setError(null);
        String s = o.getText().toString();
        boolean matches = ((EditText) a.findViewById(length.value())).getText().toString().equals(s);
        if (!matches) {
            o.setError(a.getString(length.message()));
        }

        return !matches;
    }

    private static boolean checkToValidateEmail(Activity a, Field field, Email annotation) {
        boolean valid = false;
        Email length = annotation;
        field.setAccessible(true);
        try {
            Object o = field.get(a);
            if (o instanceof EditText) {
                boolean b = checkFiledToEmail(a, length, (EditText) o);
                if (!b) {
                    valid = b;
                }
            }
        } catch (IllegalAccessException ignored) {
        }
        return valid;
    }

    private boolean checkToValidateEmail(Fragment a, Field field, Email annotation) {
        boolean valid = false;
        Email length = annotation;
        field.setAccessible(true);
        try {
            Object o = field.get(a);
            if (o instanceof EditText) {
                boolean b = checkFiledToEmail(a, length.value(), (EditText) o);
                if (!b) {
                    valid = b;
                }
            }
        } catch (IllegalAccessException ignored) {
        }
        return valid;
    }

    private static boolean checkFiledToEmail(Activity a, Email length, EditText o) {
        o.setError(null);
        String s = o.getText().toString();
        boolean matches = pattern.matcher(s).matches();
        if (!matches) {
            o.setError(a.getString(length.value()));
        }

        return matches;
    }

    public static boolean checkFiledToEmail(Fragment a, int length, EditText o) {
        o.setError(null);
        String s = o.getText().toString();
        boolean matches = pattern.matcher(s).matches();
        if (!matches) {
            o.setError(a.getString(length));
        }

        return matches;
    }

    private boolean checkToValidate(Activity a, Field field, Length annotation) {
        boolean valid = false;
        Length length = annotation;
        field.setAccessible(true);
        try {
            Object o = field.get(a);
            if (o instanceof EditText) {
                boolean b = checkFiled(a, length, (EditText) o);
                if (b) {
                    valid = b;
                }
            }
        } catch (IllegalAccessException ignored) {
        }
        return valid;
    }

    private boolean checkToValidate(Fragment a, Field field, Length annotation) {
        boolean valid = false;
        Length length = annotation;
        field.setAccessible(true);
        try {
            Object o = field.get(a);
            if (o instanceof EditText) {
                boolean b = checkFiled(a, length, (EditText) o);
                if (b) {
                    valid = b;
                }
            }
        } catch (IllegalAccessException ignored) {
        }
        return valid;
    }

    private boolean checkFiled(Activity a, Length length, EditText o) {
        o.setError(null);
        String text = o.getText().toString();
        if (text.length() == 0 && length.required()) {
            o.setError(a.getString(length.messageRequired()));
            return true;
        }
        if (text.length() < length.min()) {
            o.setError(a.getString(length.messageMin()));
            return true;
        }

        if (text.length() > length.max()) {
            o.setError(a.getString(length.messageMax()));
            return true;
        }
        return false;
    }

    private boolean checkFiled(Fragment a, Length length, EditText o) {
        o.setError(null);
        String text = o.getText().toString();
        if (text.length() == 0 && length.required()) {
            o.setError(a.getString(length.messageRequired()));
            return true;
        }
        if (text.length() < length.min()) {
            o.setError(a.getString(length.messageMin()));
            return true;
        }

        if (text.length() > length.max()) {
            o.setError(a.getString(length.messageMax()));
            return true;
        }
        return false;
    }
}
