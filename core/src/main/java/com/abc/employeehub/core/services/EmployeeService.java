package com.abc.employeehub.core.services;

import org.apache.sling.api.resource.ResourceResolver;

import java.util.List;
import java.util.Map;

public interface EmployeeService {

    Map<String, Object> getEmployee(ResourceResolver resolver, String employeePath);

    List<Map<String, Object>> searchEmployees(ResourceResolver resolver, String query, int maxResults);

    Map<String, Object> getEmployeeDetails(ResourceResolver resolver, String employeeId);
}
