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

import org.apache.jackrabbit.util.Text;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.SimpleCredentials;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;


import org.apache.commons.lang.StringUtils;
import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.util.JcrUtils;
import org.onehippo.forge.googleauth.GHippoCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleSignInUserManager extends AbstractRepositoryUserManager {
    private static final Logger log = LoggerFactory.getLogger(GoogleSignInUserManager.class);
    public static final String PROP_EMAIL = "hipposys:email";

    private GoogleIdTokenVerifier gVerifier;

    @Override
    public boolean hasUser(final String userEmailId) throws RepositoryException {
        final Node userNode = getUser(userEmailId);
        return userNode != null;
    }

    /**
     * Check if the given node contains a property 'PROP_EMAIL' with value in <code>expectedUserEmailId</code>
     */
    private static boolean hasEmail(final Node userNode, final String expectedUserEmailId) {
        try {
            if (userNode == null) {
                log.error("User node is null");
                return false;
            }
            return StringUtils.equals(expectedUserEmailId, JcrUtils.getStringProperty(userNode, PROP_EMAIL, StringUtils.EMPTY));
        } catch (RepositoryException e) {
            log.warn("Failed to query email", e);
        }
        return false;
    }

    private String getUserRootPath() {
        return "hippo:configuration/hippo:users";
    }

    /**
     * Return the node having email address as the userId
     * @param userEmailId  the google email address identifying the user
     * @return
     * @throws RepositoryException
     */
    @Override
    public Node getUser(final String userEmailId) throws RepositoryException {
        final String userNodePath = getUserRootPath() + "/" + Text.escapeIllegalJcrChars(userEmailId);

        if (session.getRootNode().hasNode(userNodePath)) {
            final Node userNode = session.getRootNode().getNode(userNodePath);
            if (userNode.getPrimaryNodeType().isNodeType(HippoNodeType.NT_USER) && hasEmail(userNode, userEmailId)) {
                return userNode;
            }
        }
        return null;
    }

    @Override
    public Node createUser(final String userId) throws RepositoryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public NodeIterator listUsers(final long offset, final long limit) throws RepositoryException {
       return listUsers(null, offset, limit);
    }

    @Override
    public NodeIterator listUsers(final String providerId, final long offset, final long limit) throws RepositoryException {
        StringBuilder statement = new StringBuilder();
        statement.append("//element");
        statement.append("(*, ").append(HippoNodeType.NT_USER).append(")");
        if (providerId != null) {
            statement.append('[');
            statement.append("@");
            statement.append(HippoNodeType.HIPPO_SECURITYPROVIDER).append("= '").append(providerId).append("'");
            statement.append(']');
        }
        statement.append(" order by @jcr:name");

        Query q = session.getWorkspace().getQueryManager().createQuery(statement.toString(), Query.XPATH);
        if (offset > 0) {
            q.setOffset(offset);
        }
        if (limit > 0) {
            q.setLimit(limit);
        }
        QueryResult result = q.execute();
        return result.getNodes();
    }

    @Override
    public boolean isActive(final String userEmailId) throws RepositoryException {
        return JcrUtils.getBooleanProperty(getUser(userEmailId), HippoNodeType.HIPPO_ACTIVE, true);
    }

    @Override
    public boolean isPasswordExpired(final String userId) throws RepositoryException {
        return false;
    }

    @Override
    public void syncUserInfo(final String userId) {
        log.info("Syncing user info of '{}'", userId);
    }

    @Override
    public void updateLastLogin(final String userId) {
        log.info("Updating last login for user '{}'", userId);
    }

    @Override
    public void saveUsers() throws RepositoryException {
        log.info("Saving users");
    }

    @Override
    public boolean authenticate(final SimpleCredentials creds) throws RepositoryException {
        if (!hasUser(creds.getUserID())) {
            return false;
        }

        final Object idToken = creds.getAttribute(GHippoCredential.ATTRIBUTE_IDTOKEN);
        if (idToken == null || !(idToken instanceof String)) {
            return false;
        }

        return verifyToken((String)idToken);
    }

    private boolean verifyToken(String googleIdToken) throws RepositoryException {
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
