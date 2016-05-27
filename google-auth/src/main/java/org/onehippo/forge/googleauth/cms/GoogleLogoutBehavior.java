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

package org.onehippo.forge.googleauth.cms;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.MetaDataHeaderItem;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.UrlResourceReference;

public class GoogleLogoutBehavior extends AbstractAjaxBehavior{
    public static final JavaScriptResourceReference GOOGLE_SIGNIN_LOGOUT_SCRIPT =
            new JavaScriptResourceReference(GoogleLogoutBehavior.class, "google-logout.js");
    private static final String GOOGLE_SIGNIN_CLIENTID = "832528604166-nt3qbcpn7kh1knntmeu7thapnhoadf5o.apps.googleusercontent.com";

    public final String GOOGLE_SIGNIN_LIBRARY = "https://apis.google.com/js/platform.js?onload=onLoad";

    @Override
    public void renderHead(final Component component, final IHeaderResponse response) {
        super.renderHead(component, response);
        response.render(MetaDataHeaderItem.forMetaTag("google-signin-scope", "profile email"));
        response.render(MetaDataHeaderItem.forMetaTag("google-signin-client_id", getGoogleSignInClientId()));
        response.render(JavaScriptHeaderItem.forReference(GOOGLE_SIGNIN_LOGOUT_SCRIPT));

        final UrlResourceReference googleSignInJsReference = new UrlResourceReference(Url.parse(GOOGLE_SIGNIN_LIBRARY));
        response.render(JavaScriptReferenceHeaderItem.forReference(googleSignInJsReference).setAsync(true).setDefer(true));
    }

    @Override
    public void onRequest() {
    }

    public String getGoogleSignInClientId() {
        return GOOGLE_SIGNIN_CLIENTID;
    }
}
