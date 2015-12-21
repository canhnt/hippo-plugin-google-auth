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
package org.hippoecm.frontend.util;

import javax.servlet.ServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.http.WebRequest;

/**
 * Wicket {@link Request} related utilities.
 */
public class RequestUtils {

    private RequestUtils() {
    }

    /**
     * Returns the remote client address or null if remote client address information is unavailable.
     * @param request wicket request
     * @return the remote client address or null if remote client address information is unavailable
     */
    public static String getFarthestRemoteAddr(final Request request) {
        String [] remoteAddrs = getRemoteAddrs(request);

        if (ArrayUtils.isNotEmpty(remoteAddrs)) {
            return remoteAddrs[0];
        }

        return null;
    }

    /**
     * Returns the remote host addresses related to this request.
     * If there's any proxy server between the client and the server,
     * then the proxy addresses are contained in the returned array.
     * The lowest indexed element is the farthest downstream client and
     * each successive proxy addresses are the next elements.
     * @param request wicket request
     * @return remote host addresses as non-null string array
     */
    public static String [] getRemoteAddrs(final Request request) {
        if (request instanceof WebRequest) {
            WebRequest webRequest = (WebRequest) request;

            String xff = webRequest.getHeader("X-Forwarded-For");

            if (xff != null) {
                String [] addrs = xff.split(",");

                for (int i = 0; i < addrs.length; i++) {
                    addrs[i] = addrs[i].trim();
                }

                return addrs;
            } else if (webRequest.getContainerRequest() instanceof ServletRequest) {
                final ServletRequest servletRequest = (ServletRequest) webRequest.getContainerRequest();
                return new String [] { servletRequest.getRemoteAddr() };
            }
        }

        return ArrayUtils.EMPTY_STRING_ARRAY;
    }
}
