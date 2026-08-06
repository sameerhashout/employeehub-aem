package com.abc.employeehub.core.servlets;

import com.abc.employeehub.core.services.DepartmentService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/employeehub/department-search",
                "sling.servlet.methods=GET",
                "sling.servlet.extensions=json"
        }
)
public class DepartmentSearchServlet extends SlingSafeMethodsServlet {

    @Reference
    private transient DepartmentService departmentService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String departmentName = request.getParameter("department");
        JSONObject result = new JSONObject();

        try {
            if (departmentName != null && !departmentName.trim().isEmpty()) {
                Map<String, Object> department = departmentService.getManagerDetails(
                    request.getResourceResolver(), departmentName.trim());
                result.put("success", !department.isEmpty());
                result.put("department", toJsonObject(department));
            } else {
                List<Map<String, Object>> departments = departmentService.getAllDepartments(
                    request.getResourceResolver());
                result.put("success", true);
                result.put("count", departments.size());
                result.put("departments", toJsonArray(departments));
            }
            response.getWriter().write(result.toString());
        } catch (Exception e) {
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"Internal server error\"}");
        }
    }

    private JSONArray toJsonArray(List<Map<String, Object>> items) throws Exception {
        JSONArray array = new JSONArray();
        for (Map<String, Object> item : items) {
            array.put(toJsonObject(item));
        }
        return array;
    }

    private JSONObject toJsonObject(Map<String, Object> map) throws Exception {
        JSONObject obj = new JSONObject();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            obj.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : "");
        }
        return obj;
    }
}
