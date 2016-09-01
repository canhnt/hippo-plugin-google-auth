/*
 * Copyright 2016 Canh Ngo (canhnt [at] gmail.com)
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
 *
 */

package com.github.canhnt.hippocms.plugins.googleauth;

import java.io.Serializable;

public class GHippoCredential implements Serializable {
    public static final String ATTRIBUTE_IDTOKEN = "GOOGLE_SIGNIN_IDTOKEN";

    private String id;
    private String name;
    private String email;
    private String idToken;

    public GHippoCredential(final String id, final String email, final String idToken) {
        this(id, null, email, idToken);
    }

    public GHippoCredential(final String id, final String name, final String email, final String idToken) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.idToken = idToken;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getIdToken() {
        return idToken;
    }
}
