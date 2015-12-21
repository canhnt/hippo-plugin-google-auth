/*
 *  Copyright 2008-2015 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.frontend.editor.plugins;

import java.util.Date;

import org.apache.wicket.datetime.StyleDateConverter;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.yui.datetime.DateFieldWidget;
import org.hippoecm.frontend.service.IEditor.Mode;
import org.hippoecm.frontend.service.render.RenderPlugin;

public class DatePickerPlugin extends RenderPlugin<Date> {

    private static final long serialVersionUID = 1L;

    public static final String DATESTYLE = "LS";

    /**
     * @deprecated use {@link Mode#EDIT} instead.
     */
    @Deprecated
    public static final String EDIT = "edit";

    /***
     * @deprecated use {@link Mode#VIEW} instead.
     */
    @Deprecated
    public static final String VIEW = "view";

    public static final String MODE = "mode";
    public static final String VALUE = "value";

    public DatePickerPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);

        IModel<Date> valueModel = getModel();
        final Mode mode = Mode.fromString(config.getString(MODE), Mode.VIEW);
        if (mode == Mode.EDIT) {
            add(new DateFieldWidget(VALUE, valueModel, context, config));
        } else {
            add(new DateLabel(VALUE, valueModel, new StyleDateConverter(DATESTYLE, true)));
        }
        setOutputMarkupId(true);
    }

}
