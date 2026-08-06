package com.abc.employeehub.core.services.impl;

import com.abc.employeehub.core.services.DepartmentService;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(service = DepartmentService.class, immediate = true)
public class DepartmentServiceImpl implements DepartmentService {

    private static final String DEPARTMENT_DATA_PATH = "/content/employeehub/data/departments";

    @Override
    public Map<String, Object> getDepartment(ResourceResolver resolver, String departmentPath) {
        if (resolver == null) {
            return Collections.emptyMap();
        }
        Resource resource = resolver.getResource(departmentPath);
        return resource != null ? mapDepartmentResource(resource) : Collections.emptyMap();
    }

    @Override
    public int getDepartmentCount(ResourceResolver resolver) {
        if (resolver == null) {
            return 0;
        }
        Resource root = resolver.getResource(DEPARTMENT_DATA_PATH);
        if (root == null) {
            return 0;
        }
        int count = 0;
        for (Resource ignored : root.getChildren()) {
            count++;
        }
        return count;
    }

    @Override
    public Map<String, Object> getManagerDetails(ResourceResolver resolver, String departmentName) {
        if (resolver == null) {
            return Collections.emptyMap();
        }
        Resource root = resolver.getResource(DEPARTMENT_DATA_PATH);
        if (root == null) {
            return Collections.emptyMap();
        }
        for (Resource child : root.getChildren()) {
            ValueMap props = child.getValueMap();
            if (departmentName.equalsIgnoreCase(props.get("departmentName", String.class))) {
                Map<String, Object> details = mapDepartmentResource(child);
                Map<String, Object> manager = new HashMap<>();
                manager.put("manager", details.get("manager"));
                manager.put("departmentName", details.get("departmentName"));
                return manager;
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public List<Map<String, Object>> getAllDepartments(ResourceResolver resolver) {
        List<Map<String, Object>> departments = new ArrayList<>();
        if (resolver == null) {
            return departments;
        }
        Resource root = resolver.getResource(DEPARTMENT_DATA_PATH);
        if (root != null) {
            for (Resource child : root.getChildren()) {
                departments.add(mapDepartmentResource(child));
            }
        }
        return departments;
    }

    private Map<String, Object> mapDepartmentResource(Resource resource) {
        ValueMap props = resource.getValueMap();
        Map<String, Object> department = new HashMap<>();
        department.put("path", resource.getPath());
        department.put("departmentName", props.get("departmentName", ""));
        department.put("manager", props.get("manager", ""));
        department.put("employeeCount", props.get("employeeCount", "0"));
        department.put("departmentImage", props.get("departmentImage", ""));
        return department;
    }
}
