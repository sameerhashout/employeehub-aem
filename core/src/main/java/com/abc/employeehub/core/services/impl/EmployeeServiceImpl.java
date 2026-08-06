package com.abc.employeehub.core.services.impl;

import com.abc.employeehub.core.configurations.EmployeeHubConfiguration;
import com.abc.employeehub.core.services.EmployeeService;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(service = EmployeeService.class, immediate = true)
@Designate(ocd = EmployeeHubConfiguration.class)
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);
    private static final String EMPLOYEE_DATA_PATH = "/content/employeehub/data/employees";

    private int maximumSearchResults;

    @Activate
    @Modified
    protected void activate(EmployeeHubConfiguration config) {
        this.maximumSearchResults = config.maximumSearchResults();
    }

    @Override
    public Map<String, Object> getEmployee(ResourceResolver resolver, String employeePath) {
        if (resolver == null) {
            return Collections.emptyMap();
        }
        Resource resource = resolver.getResource(employeePath);
        return resource != null ? mapEmployeeResource(resource) : Collections.emptyMap();
    }

    @Override
    public List<Map<String, Object>> searchEmployees(ResourceResolver resolver, String query, int maxResults) {
        int limit = maxResults > 0 ? maxResults : maximumSearchResults;
        List<Map<String, Object>> results = new ArrayList<>();

        if (resolver == null || query == null || query.trim().isEmpty()) {
            return results;
        }

        Resource root = resolver.getResource(EMPLOYEE_DATA_PATH);
        if (root == null) {
            LOG.warn("Employee data path not found: {}", EMPLOYEE_DATA_PATH);
            return results;
        }

        String lowerQuery = query.toLowerCase();
        for (Resource child : root.getChildren()) {
            if (results.size() >= limit) {
                break;
            }
            ValueMap props = child.getValueMap();
            String name = props.get("employeeName", "");
            String email = props.get("email", "");
            String department = props.get("department", "");

            if (name.toLowerCase().contains(lowerQuery)
                    || email.toLowerCase().contains(lowerQuery)
                    || department.toLowerCase().contains(lowerQuery)) {
                results.add(mapEmployeeResource(child));
            }
        }
        return results;
    }

    @Override
    public Map<String, Object> getEmployeeDetails(ResourceResolver resolver, String employeeId) {
        if (resolver == null) {
            return Collections.emptyMap();
        }
        Resource root = resolver.getResource(EMPLOYEE_DATA_PATH);
        if (root == null) {
            return Collections.emptyMap();
        }
        for (Resource child : root.getChildren()) {
            ValueMap props = child.getValueMap();
            if (employeeId.equals(props.get("employeeId", String.class))) {
                return mapEmployeeResource(child);
            }
        }
        return Collections.emptyMap();
    }

    private Map<String, Object> mapEmployeeResource(Resource resource) {
        ValueMap props = resource.getValueMap();
        Map<String, Object> employee = new HashMap<>();
        employee.put("path", resource.getPath());
        employee.put("employeeName", props.get("employeeName", ""));
        employee.put("email", props.get("email", ""));
        employee.put("department", props.get("department", ""));
        employee.put("photo", props.get("photo", ""));
        employee.put("employeeId", props.get("employeeId", ""));
        return employee;
    }
}
