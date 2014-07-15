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

import com.dalaran.annotation.validator.AbstractValidator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EmailValidator extends AbstractValidator {


    private String domainName = "";

    @Override
    public boolean isValid(String email) {
        if (email.length() > 0) {
            boolean matchFound;
            if (domainName != null && domainName.length() > 0) {
                Pattern p = Pattern.compile(".+@" + domainName);
                Matcher m = p.matcher(email);
                matchFound = m.matches();
            } else {
                Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
                Matcher m = p.matcher(email);
                matchFound = m.matches();
            }
            return matchFound;
        } else {
            return true;
        }
    }

    /**
     * Lets say that the email address must be valid for such domain.
     * This function only accepts strings of Regexp
     *
     * @param name Regexp Domain Name
     *             <p/>
     *             example : gmail.com
     */
    public void setDomainName(String name) {
        domainName = name;
    }
}