/*
 * Copyright 2015 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hippoecm.frontend.plugins.standards.diff;

import org.apache.wicket.util.io.IClusterable;

/**
 * @author cngo
 * @version $Id: DiffService.java 48555 2015-02-03 13:39:06Z cngo $
 * @since 2015-02-02
 */
public interface DiffService extends IClusterable {
    String SERVICE_ID = "diffservice.id";

    String diff(final String originalValue, final String currentValue);
}
