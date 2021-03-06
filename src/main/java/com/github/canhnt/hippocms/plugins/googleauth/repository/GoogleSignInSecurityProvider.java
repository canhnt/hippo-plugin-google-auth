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

package com.github.canhnt.hippocms.plugins.googleauth.repository;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.hippoecm.repository.security.AbstractSecurityProvider;
import org.hippoecm.repository.security.ManagerContext;
import org.hippoecm.repository.security.SecurityProviderContext;
import org.hippoecm.repository.security.group.RepositoryGroupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleSignInSecurityProvider extends AbstractSecurityProvider {
    private static final Logger log = LoggerFactory.getLogger(GoogleSignInSecurityProvider.class);

    private SecurityProviderContext context;

    @Override
    public void init(final SecurityProviderContext context) throws RepositoryException {
        this.context = context;

        ManagerContext mgrContext;

        mgrContext = new ManagerContext(context.getSession(), context.getProviderPath(), context.getUsersPath(), context.isMaintenanceMode());
        createUserManager(mgrContext);
        createGroupManager(context);
    }

    @Override
    public AbstractRepositoryUserManager getUserManager(final Session session) throws RepositoryException {
        final ManagerContext mgrContext = new ManagerContext(session, context.getProviderPath(),
                context.getUsersPath(), context.isMaintenanceMode());

        createUserManager(mgrContext);

        return (AbstractRepositoryUserManager) userManager;
    }


    private void createUserManager(final ManagerContext mgrContext) throws RepositoryException {
        final AbstractRepositoryUserManager abstractUserManager = new GoogleSignInUserManager();
        userManager = abstractUserManager;
        abstractUserManager.init(mgrContext);
    }

    private void createGroupManager(final SecurityProviderContext context) throws RepositoryException {
        final ManagerContext mgrContext;
        mgrContext = new ManagerContext(context.getSession(), context.getProviderPath(), context.getGroupsPath(), context.isMaintenanceMode());
        groupManager = new RepositoryGroupManager();
        groupManager.init(mgrContext);
    }
}
