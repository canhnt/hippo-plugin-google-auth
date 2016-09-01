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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.login.DefaultLoginPlugin;
import org.hippoecm.frontend.plugins.login.LoginHandler;
import org.hippoecm.frontend.plugins.login.LoginPanel;
import org.hippoecm.frontend.session.LoginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GoogleLoginPlugin extends DefaultLoginPlugin {
    private static final Logger log = LoggerFactory.getLogger(GoogleLoginPlugin.class);

    public static final String GOOGLE_SIGNIN_CLIENTID = "google.signin.clientid";
    public static final String GOOGLE_SIGNIN_SCOPE = "google.signin.scope";

    private final String clientId;
    private final String scope;

    public GoogleLoginPlugin(final IPluginContext context, final IPluginConfig config) {
        super(context, config);
        clientId = config.getString(GOOGLE_SIGNIN_CLIENTID);
        scope = config.getString(GOOGLE_SIGNIN_SCOPE);

        if (StringUtils.isBlank(clientId)) {
            log.error("Missing plugin parameter '{}'", GOOGLE_SIGNIN_CLIENTID);
        }

        if (StringUtils.isBlank(scope)) {
            log.error("Missing plugin parameter '{}'", GOOGLE_SIGNIN_SCOPE);
        }
    }

    @Override
    protected LoginPanel createLoginPanel(final String id, final boolean autoComplete, final List<String> locales, final LoginHandler handler) {
        return new ExtendedLoginForm(id, autoComplete, locales, handler);
    }

    private class ExtendedLoginForm extends LoginForm {
        private final List<String> locales;

        public ExtendedLoginForm(final String id, final boolean autoComplete, final List<String> locales, final LoginHandler handler) {
            super(id, autoComplete, locales, handler);
            this.locales = locales;
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();

            form.add(new GoogleLoginPanel("google-login-panel", locales, clientId, scope) {
                @Override
                protected void loginSuccess() {
                    ExtendedLoginForm.this.loginSuccess();
                }

                @Override
                protected void loginFailed(final LoginException.Cause cause) {
                    ExtendedLoginForm.this.loginFailed(cause);
                }
            });
        }

        @Override
        public void renderHead(final IHeaderResponse response) {
            super.renderHead(response);

            final String script = getRepositionScript();
            response.render(OnDomReadyHeaderItem.forScript(script));
        }

        private String getRepositionScript() {
            final StringBuilder script = new StringBuilder();
            script.append("loginpanel = $('.hippo-login-form-container .hippo-google-signin-container');")
                    .append("loginpanel.parent().prepend(loginpanel)");
            return script.toString();
        }

    }
}
