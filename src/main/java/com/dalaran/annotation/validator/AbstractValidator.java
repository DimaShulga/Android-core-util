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

import android.content.Context;

/**
 * Class for creating new Validators
 */
public abstract class AbstractValidator {

    protected Context mContext;

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Can check if the value passed in parameter is valid or not.
     * @param value
     *                 {@link String} : the value to validate
     * @return
     *                 boolean : true if valid, false otherwise.
     */
    public abstract boolean isValid(String value) throws ValidatorException;

    /**
     * Used to retrieve the error message corresponding to the validator.
     * @return
     *                 String : the error message
     */
    public String getMessage(int mErrorMessage) {
        return mContext.getString(mErrorMessage);
    }
}