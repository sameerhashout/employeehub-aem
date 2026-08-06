package com.abc.employeehub.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(
        adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class EmployeeSpotlightModel {

    @ValueMapValue
    private String employeeName;

    @ValueMapValue
    private String designation;

    @ValueMapValue
    private String spotlightText;

    public String getEmployeeName() {
        return employeeName;
    }

    public String getDesignation() {
        return designation;
    }

    public String getSpotlightText() {
        return spotlightText;
    }

    public String getInitials() {
        if (employeeName == null || employeeName.isEmpty()) {
            return "S";
        }
        return employeeName.trim().substring(0, 1).toUpperCase();
    }
}
