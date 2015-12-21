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
package org.hippoecm.frontend.plugins.yui.datetime;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.datetime.DateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.hippoecm.frontend.plugins.standards.icon.HippoIcon;
import org.hippoecm.frontend.skin.Icon;
import org.hippoecm.frontend.widgets.UpdateFeedbackInfo;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Semi-fork of YUI DateTimeField from Wicket extensions. Replaces Wicket extensions YUI behaviors with a {@link YuiDatePicker}
 * so it fit's in the Hippo ECM YUI framework.
 *
 * DatePicker can be configured using a frontend:pluginconfig node with name <code>datepicker</code>.
 *
 * @see YuiDatePickerSettings for all configuration options
 */

public class YuiDateTimeField extends DateTimeField {

    public static final String DATE_LABEL = "date-label";
    public static final String HOURS_LABEL = "hours-label";
    public static final String MINUTES_LABEL = "minutes-label";

    private YuiDatePickerSettings settings;

    private boolean todayLinkVisible = true;

    public YuiDateTimeField(String id, IModel<Date> model) {
        this(id, model, null);
    }

    public YuiDateTimeField(String id, IModel<Date> model, YuiDatePickerSettings settings) {
        super(id, model);

        if (settings == null) {
            settings = new YuiDatePickerSettings();
            settings.setLanguage(getLocale().getLanguage());
        }
        this.settings = settings;

        setOutputMarkupId(true);

        final DateTextField dateField = (DateTextField) get("date");
        dateField.setLabel(Model.of(getString(DATE_LABEL)));

        // Remove existing behaviors from dateField
        dateField.getBehaviors().forEach(dateField::remove);
        // And add our own YuiDatePicker instead
        dateField.add(new YuiDatePicker(settings));

        // Restrict the size of the input field to match the date pattern
        final int dateLength = calculateDateLength();

        dateField.add(AttributeModifier.replace("size", dateLength));
        dateField.add(AttributeModifier.replace("maxlength", dateLength));

        final TextField hoursField = (TextField) get("hours");
        hoursField.setLabel(Model.of(getString(HOURS_LABEL)));

        final TextField minutesField = (TextField) get("minutes");
        minutesField.setLabel(Model.of(getString(MINUTES_LABEL)));

        //add "Now" link
        AjaxLink<Date> today;
        add(today = new AjaxLink<Date>("today") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                YuiDateTimeField.this.setDefaultModelObject(new Date());
                dateField.clearInput();
                hoursField.clearInput();
                minutesField.clearInput();
                if (target != null) {
                    target.add(YuiDateTimeField.this);
                }
            }

            @Override
            public boolean isVisible() {
                return todayLinkVisible;
            }
        });

        today.add(HippoIcon.fromSprite("current-date-icon", Icon.RESTORE));

        //Add change behavior to super fields
        for (String name : new String[] { "date", "hours", "minutes", "amOrPmChoice" }) {
            get(name).add(new AjaxFormComponentUpdatingBehavior("onChange") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    updateDateTime();
                    send(YuiDateTimeField.this, Broadcast.BUBBLE, new UpdateFeedbackInfo(target));
                }

                protected void onError(AjaxRequestTarget target, RuntimeException e){
                    super.onError(target, e);
                    send(YuiDateTimeField.this, Broadcast.BUBBLE, new UpdateFeedbackInfo(target));
                }
            });
        }
    }

    private int calculateDateLength() {
        return settings.getDatePattern().length() + 2;
    }

    private void updateDateTime() {
        Date date = getDate();
        if (date != null) {
            MutableDateTime datetime = new MutableDateTime(date);
            try {
                TimeZone zone = getClientTimeZone();
                if (zone != null) {
                    datetime.setZone(DateTimeZone.forTimeZone(zone));
                }

                Integer hours = getHours();
                if (hours != null) {
                    datetime.set(DateTimeFieldType.hourOfDay(), hours % 24);

                    Integer minutes = getMinutes();
                    datetime.setMinuteOfHour(minutes != null ? minutes : 0);
                }

                // the date will be in the server's timezone
                setDate(datetime.toDate());
                //setModelObject(datetime.toDate());
            } catch (RuntimeException e) {
                error(e.getMessage());
                invalid();
            }
        }
    }

    @Override
    protected boolean use12HourFormat() {
        return false;
    }

    public void setTodayLinkVisible(boolean todayLinkVisible) {
        this.todayLinkVisible = todayLinkVisible;
    }

    protected String getDatePattern() {
        return settings.getDatePattern();
    }

    @Override
    protected DatePicker newDatePicker() {
        return new DatePicker() {
            @Override
            protected String getDatePattern() {
                return "This string is non-null to please the base class.  Please ignore it."
                        + "  The date-picker behavior is overridden later in the construction phase by the YuiDatePicker.";
            }
        };
    }

    @Override
    protected DateTextField newDateTextField(String id, PropertyModel<Date> dateFieldModel) {
        return new DateTextField(id, dateFieldModel, new DateConverter(false) {
            @Override
            public String getDatePattern(Locale locale) {
                return YuiDateTimeField.this.getDatePattern();
            }

            @Override
            protected DateTimeFormatter getFormat(Locale locale) {
                return DateTimeFormat.forPattern(YuiDateTimeField.this.getDatePattern()).withLocale(getLocale());
            }
        });
    }
}
