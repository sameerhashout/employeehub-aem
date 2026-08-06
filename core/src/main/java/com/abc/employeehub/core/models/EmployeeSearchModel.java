package com.abc.employeehub.core.models;

import com.abc.employeehub.core.services.EmployeeService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(
        adaptables = {Resource.class, SlingHttpServletRequest.class},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class EmployeeSearchModel {

    @ValueMapValue
    private String placeholder;

    @ValueMapValue
    private String buttonText;

    @ValueMapValue
    private int maximumResults;

    @OSGiService
    private EmployeeService employeeService;

    public String getPlaceholder() {
        return placeholder != null ? placeholder : "Search employees by name, email, or department...";
    }

    public String getButtonText() {
        return buttonText != null ? buttonText : "Search";
    }

    public int getMaximumResults() {
        return maximumResults > 0 ? maximumResults : 20;
    }

    public String getSearchEndpoint() {
        return "/bin/employeehub/employee-search.json";
    }
}
