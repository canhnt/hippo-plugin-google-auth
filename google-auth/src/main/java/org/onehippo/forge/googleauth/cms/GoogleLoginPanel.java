/*
 *  Copyright 2015 Canh Ngo (canhnt [at] gmail.com)
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
import java.util.MissingResourceException;

import javax.jcr.SimpleCredentials;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Application;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.MetaDataHeaderItem;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.template.PackageTextTemplate;
import org.hippoecm.frontend.Main;
import org.hippoecm.frontend.model.UserCredentials;
import org.hippoecm.frontend.plugins.login.ConcurrentLoginFilter;
import org.hippoecm.frontend.plugins.login.LoginHandler;
import org.hippoecm.frontend.session.LoginException;
import org.hippoecm.frontend.session.PluginUserSession;
import org.hippoecm.frontend.util.WebApplicationHelper;
import org.onehippo.forge.googleauth.GHippoCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GoogleLoginPanel extends Panel {
    private static final Logger log = LoggerFactory.getLogger(GoogleLoginPanel.class);

    public static final String JS_SCRIPT = "g-signin-config.js";
    private static final ResourceReference STYLE_CSS = new CssResourceReference(GoogleLoginPanel.class, "google-login-panel.css");;

    public static final String METATAG_GOOGLE_SIGNIN_SCOPE = "google-signin-scope";
    public static final String METATAG_GOOGLE_SIGNIN_CLIENT_ID = "google-signin-client_id";
    private static final char[] EMPTY_PASSWORD = new char[]{0};

    private final static String DEFAULT_KEY = "invalid.login";

    private final FeedbackPanel feedback;

    private final String googleSignInClientId;
    private final String googleSignInScope;
    private final LoginHandler handler;
    private final List<String> locales;

    private AbstractDefaultAjaxBehavior ajaxCallBackGoogleSignIn;
    private GHippoCredential gCredential;
    private String username;

    public GoogleLoginPanel(final String id,
                            final boolean autoComplete,
                            final List<String> locales,
                            final LoginHandler handler,
                            final String googleSignInClientId,
                            final String googleSignInScope) {
        super(id);


        this.handler = handler;
        this.locales = locales;

        this.googleSignInClientId = googleSignInClientId;
        this.googleSignInScope = googleSignInScope;

        add(feedback = new FeedbackPanel("feedback"));
        feedback.setOutputMarkupId(true);
        feedback.setEscapeModelStrings(false);
        feedback.setFilter(message -> !message.isRendered());

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

                log.debug("Google user information:\nid={}\nname={}\nemail={}\nid_token={}",
                        id, name, email, id_token);

                try {
                    loginWithGSignIn();
                    loginSuccess();
                } catch (LoginException le) {
                    log.debug("Login failure!", le);
                    loginFailed(le.getLoginExceptionCause());
                    target.add(feedback);
                } catch (AccessControlException ace) {
                    // Invalidate the current obtained JCR session and create an anonymous one
                    PluginUserSession.get().login();
                    loginFailed(LoginException.Cause.ACCESS_DENIED);
                    target.add(feedback);
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

        response.render(CssHeaderItem.forReference(STYLE_CSS));
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

        HttpSession session = WebApplicationHelper.retrieveWebRequest().getContainerRequest().getSession(true);
        ConcurrentLoginFilter.validateSession(session, username, false);
        userSession.setLocale(new Locale(locales.get(0)));

    }

    protected  void loginSuccess() {
        if (handler != null) {
            handler.loginSuccess();
        }
    }

    protected void loginFailed(final LoginException.Cause cause) {
        Main main = (Main) Application.get();
        main.resetConnection();

        info(getReason(cause));
    }

    private String getReason(final LoginException.Cause cause) {
        if (cause != null) {
            try {
                final String reason = getString(cause.getKey());
                if (reason != null) {
                    return reason;
                }
            } catch (MissingResourceException ignore) {
            }
        }
        return getString(DEFAULT_KEY);
    }
}
