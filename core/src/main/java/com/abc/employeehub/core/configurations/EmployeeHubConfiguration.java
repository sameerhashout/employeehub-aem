package com.abc.employeehub.core.configurations;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Employee Hub Configuration",
        description = "Global configuration for the Employee Hub Portal"
)
public @interface EmployeeHubConfiguration {

    @AttributeDefinition(name = "Portal Name", description = "Display name of the Employee Hub Portal")
    String portalName() default "ABC Employee Hub";

    @AttributeDefinition(name = "Environment Name", description = "Current environment (dev, stage, prod)")
    String environmentName() default "dev";

    @AttributeDefinition(name = "Default Department", description = "Default department filter for search")
    String defaultDepartment() default "all";

    @AttributeDefinition(name = "Maximum Search Results", description = "Max number of search results returned")
    int maximumSearchResults() default 20;
}
