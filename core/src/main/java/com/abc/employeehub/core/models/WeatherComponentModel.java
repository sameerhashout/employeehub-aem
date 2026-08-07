package com.abc.employeehub.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(
        adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class WeatherComponentModel {

    @ValueMapValue
    private String heading;

    @ValueMapValue
    private String city;

    public String getHeading() {
        return heading != null && !heading.isEmpty() ? heading : "Local Weather";
    }

    public String getCity() {
        return city != null ? city : "";
    }
}
