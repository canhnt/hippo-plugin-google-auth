/*
 *  Copyright 2015 Hippo B.V. (http://www.onehippo.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onehippo.forge.googleauth.cms;

import java.security.AccessControlException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jcr.SimpleCredentials;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.MetaDataHeaderItem;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.template.PackageTextTemplate;
import org.hippoecm.frontend.model.UserCredentials;
import org.hippoecm.frontend.plugins.login.LoginHandler;
import org.hippoecm.frontend.plugins.login.LoginPanel;
import org.hippoecm.frontend.session.LoginException;
import org.hippoecm.frontend.session.PluginUserSession;
import org.onehippo.forge.googleauth.GHippoCredential;


public class GoogleLoginPanel extends LoginPanel {
    public static final String JS_SCRIPT = "g-signin-config.js";

    public static final String METATAG_GOOGLE_SIGNIN_SCOPE = "google-signin-scope";
    public static final String METATAG_GOOGLE_SIGNIN_CLIENT_ID = "google-signin-client_id";
    private static final char[] EMPTY_PASSWORD = new char[]{0};

    private final String googleSignInClientId;
    private final String googleSignInScope;

    private AbstractDefaultAjaxBehavior ajaxCallBackGoogleSignIn;
    private GHippoCredential gCredential;

    public GoogleLoginPanel(final String id,
                            final boolean autoComplete,
                            final List<String> locales,
                            final LoginHandler handler,
                            final String googleSignInClientId,
                            final String googleSignInScope) {
        super(id, autoComplete, locales, handler);

        this.googleSignInClientId = googleSignInClientId;
        this.googleSignInScope = googleSignInScope;

        add(ajaxCallBackGoogleSignIn = new AbstractDefaultAjaxBehavior() {
            @Override
            protected void respond(final AjaxRequestTarget target) {
                final IRequestParameters parameters = RequestCycle.get().getRequest().getRequestParameters();
                final String id = parameters.getParameterValue("id").toString();
                final String name = parameters.getParameterValue("name").toString();
                final String email = parameters.getParameterValue("email").toString();
                final String id_token = parameters.getParameterValue("id_token").toString();

                gCredential = new GHippoCredential(id, name, email, id_token);
                //FIXME: should not be as simple as this
                username = getUsernameFromEmail(email);

                log.warn("Google user information:\nid={}\nname={}\nemail={}\nid_token={}",
                        id, name, email, id_token);

                try {
                    loginWithGSignIn();
                    loginSuccess();
                } catch (LoginException le) {
                    log.debug("Login failure!", le);
                    loginFailed(le.getLoginExceptionCause());
                } catch (AccessControlException ace) {
                    // Invalidate the current obtained JCR session and create an anonymous one
                    PluginUserSession.get().login();
                    loginFailed(LoginException.Cause.ACCESS_DENIED);
                }
            }
        });
    }

    private String getUsernameFromEmail(final String email) {
        return StringUtils.substringBefore(email, "@");
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        renderSignInParameters(response);

        PackageTextTemplate jsTmpl = new PackageTextTemplate(GoogleLoginPanel.class, JS_SCRIPT);
        final Map<String, String> variables = new HashMap<>();

        variables.put("callbackUrl", ajaxCallBackGoogleSignIn.getCallbackUrl().toString());
        String s = jsTmpl.asString(variables);
        response.render(JavaScriptReferenceHeaderItem.forScript(s, this.getMarkupId()));
    }

    private void renderSignInParameters(final IHeaderResponse response) {
        response.render(MetaDataHeaderItem.forMetaTag(METATAG_GOOGLE_SIGNIN_SCOPE, googleSignInScope));
        response.render(MetaDataHeaderItem.forMetaTag(METATAG_GOOGLE_SIGNIN_CLIENT_ID, googleSignInClientId));
    }


    private void loginWithGSignIn() throws LoginException {
        PluginUserSession userSession = PluginUserSession.get();

        SimpleCredentials creds = new SimpleCredentials(gCredential.getEmail(), EMPTY_PASSWORD);
        creds.setAttribute(GHippoCredential.ATTRIBUTE_IDTOKEN, gCredential.getIdToken());

        userSession.login(new UserCredentials(creds));

        validateSession();
        userSession.setLocale(new Locale(selectedLocale));

    }
}
