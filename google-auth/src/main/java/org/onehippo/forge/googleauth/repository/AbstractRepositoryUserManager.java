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

import java.security.Principal;
import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.AuthorizableExistsException;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.hippoecm.repository.security.ManagerContext;
import org.hippoecm.repository.security.user.HippoUserManager;

public abstract class AbstractRepositoryUserManager implements HippoUserManager {
    /**
     * The system/root session
     */
    protected Session session;

    /**
     * The path from the root containing the users
     */
    protected String usersPath;

    /**
     * The path from the root containing the users
     */
    protected String providerPath;

    /**
     * Is the class initialized
     */
    protected boolean initialized = false;

    /**
     * The id of the provider that this manager instance belongs to
     */
    protected String providerId;

    /**
     * Don't use queries for now. It's too slow :(
     */
    private final boolean useQueries = false;

    private boolean maintenanceMode = false;

    public Authorizable getAuthorizable(String id) throws RepositoryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Authorizable getAuthorizable(Principal principal) throws RepositoryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Iterator<Authorizable> findAuthorizables(String propertyName, String value) throws RepositoryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Iterator<Authorizable> findAuthorizables(String propertyName, String value, int searchType) throws RepositoryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Iterator<Authorizable> findAuthorizables(org.apache.jackrabbit.api.security.user.Query query) throws RepositoryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public User createUser(String userID, String password) throws AuthorizableExistsException, RepositoryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public User createUser(String userID, String password, Principal principal, String intermediatePath) throws AuthorizableExistsException, RepositoryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Group createGroup(Principal principal) throws AuthorizableExistsException, RepositoryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Group createGroup(Principal principal, String intermediatePath) throws AuthorizableExistsException, RepositoryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Group createGroup(String string) throws AuthorizableExistsException, RepositoryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Group createGroup(String string, Principal prncpl, String string1) throws AuthorizableExistsException, RepositoryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void autoSave(boolean enable) throws UnsupportedRepositoryOperationException, RepositoryException {
    }

    @Override
    public <T extends Authorizable> T getAuthorizable(final String id, final Class<T> authorizableClass) throws RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    @Override
    public Authorizable getAuthorizableByPath(final String path) throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    @Override
    public User createSystemUser(final String userID, final String intermediatePath) throws AuthorizableExistsException, RepositoryException {
        throw new UnsupportedRepositoryOperationException("Not yet implemented.");
    }

    public boolean isAutoSave() {
        return false;
    }

    public boolean isCaseSensitive() {
        return true;
    }

    public final void init(ManagerContext context) throws RepositoryException {
        this.session = context.getSession();
        this.usersPath = context.getPath();
        this.providerId = context.getProviderId();
        this.providerPath = context.getProviderPath();
        this.maintenanceMode = context.isMaintenanceMode();
    }
}
