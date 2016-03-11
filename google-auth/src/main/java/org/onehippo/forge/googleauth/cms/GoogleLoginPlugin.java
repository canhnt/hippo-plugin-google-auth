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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.login.LoginHandler;
import org.hippoecm.frontend.plugins.login.SimpleLoginPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GoogleLoginPlugin extends SimpleLoginPlugin {
    private static final Logger log = LoggerFactory.getLogger(GoogleLoginPlugin.class);

    public static final String GOOGLE_SIGNIN_CLIENTID = "google.signin.clientid";
    public static final String GOOGLE_SIGNIN_SCOPE = "google.signin.scope";

    private final String clientId;
    private final String scope;

    public GoogleLoginPlugin(final IPluginContext context, final IPluginConfig config) {
        super(context, config);
        clientId = config.getString(GOOGLE_SIGNIN_CLIENTID);
        scope = config.getString(GOOGLE_SIGNIN_SCOPE);

        if (StringUtils.isEmpty(clientId)) {
            log.error("Missing plugin paramemter '{}'", GOOGLE_SIGNIN_CLIENTID);
        }

        if (StringUtils.isEmpty(scope)) {
            log.error("Missing plugin paramemter '{}'", GOOGLE_SIGNIN_SCOPE);
        }
    }

    @Override
    protected Component createLoginPanelHeader(final String id,
                                               final boolean autoComplete,
                                               final List<String> locales,
                                               final LoginHandler handler) {
//        return super.createLoginPanelHeader(id, autoComplete, locales, handler);
        return new GoogleLoginPanel(id, autoComplete, locales, handler, clientId, scope);
    }
}
