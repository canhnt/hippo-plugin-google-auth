/*
 * Copyright 2016 Canh Ngo (canhnt [at] gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.forge.googleauth.repository;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.jcr.RepositoryException;
import javax.jcr.SimpleCredentials;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import org.hippoecm.repository.security.user.RepositoryUserManager;
import org.onehippo.forge.googleauth.GHippoCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GoogleSignInUserManager extends RepositoryUserManager {
    private static final Logger log = LoggerFactory.getLogger(GoogleSignInUserManager.class);

    private GoogleIdTokenVerifier gVerifier;

    @Override
    public boolean authenticate(final SimpleCredentials creds) throws RepositoryException {
        final Object idToken = creds.getAttribute(GHippoCredential.ATTRIBUTE_IDTOKEN);
        if (idToken != null) {
            return authenticate((String) idToken);
        }

        return super.authenticate(creds);
    }

    private boolean authenticate(String googleIdToken) throws RepositoryException {
        log.debug("Authenticating with Google OAuth2");
        if (gVerifier == null) {
            NetHttpTransport transport = new NetHttpTransport();
            final JsonFactory jsonFactory = new GsonFactory();
            gVerifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory).build();
        }
        try {
            final GoogleIdToken token = gVerifier.verify(googleIdToken);
            final GoogleIdToken.Payload payload = token.getPayload();
            log.debug("Hosted domain: {}\nSubject: {}\nEmail:{}", payload.getHostedDomain(), payload.getSubject(), payload.getEmail());
            return true;
        } catch (GeneralSecurityException | IOException e) {
            log.error("Invalid Google SignIn token", e);
        }
        return false;
    }
}
