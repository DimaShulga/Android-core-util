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

package com.dalaran.annotation.validator.logic;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Patterns;

import com.dalaran.annotation.validator.AbstractValidator;
import com.dalaran.annotation.validator.ValidatorException;

import java.util.regex.Pattern;


/**
 * Validator to check if Phone number is correct.
 * Created by throrin19 on 13/06/13.
 */
@TargetApi(Build.VERSION_CODES.FROYO)
public class PhoneValidator extends AbstractValidator {

    private static final Pattern mPattern = Patterns.PHONE;

    @Override
    public boolean isValid(String value) throws ValidatorException {
        return mPattern.matcher(value).matches();
    }
}