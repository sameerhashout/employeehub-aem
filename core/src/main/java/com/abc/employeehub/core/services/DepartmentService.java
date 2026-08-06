package com.abc.employeehub.core.services;

import org.apache.sling.api.resource.ResourceResolver;

import java.util.List;
import java.util.Map;

public interface DepartmentService {

    Map<String, Object> getDepartment(ResourceResolver resolver, String departmentPath);

    int getDepartmentCount(ResourceResolver resolver);

    Map<String, Object> getManagerDetails(ResourceResolver resolver, String departmentName);

    List<Map<String, Object>> getAllDepartments(ResourceResolver resolver);
}
