package com.abc.employeehub.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(
        adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TestimonialItemModel {

    @ValueMapValue
    private String employeeName;

    @ValueMapValue
    private String designation;

    @ValueMapValue
    private String feedback;

    @ValueMapValue
    private String photo;

    public String getEmployeeName() {
        return employeeName;
    }

    public String getDesignation() {
        return designation;
    }

    public String getFeedback() {
        return feedback;
    }

    public String getPhoto() {
        return photo;
    }
}
