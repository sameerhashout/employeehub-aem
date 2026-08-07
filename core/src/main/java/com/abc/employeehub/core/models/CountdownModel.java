package com.abc.employeehub.core.models;

import java.time.Instant;
import java.util.Calendar;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(
        adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CountdownModel {

    @ValueMapValue
    private Calendar targetDate;

    @ValueMapValue
    private String heading;

    public String getTargetIso() {
        if (targetDate == null) {
            return "";
        }
        return Instant.ofEpochMilli(targetDate.getTimeInMillis()).toString();
    }

    public boolean isConfigured() {
        return targetDate != null;
    }

    public String getHeading() {
        return heading != null && !heading.isEmpty() ? heading : "Next Company Event In";
    }
}
