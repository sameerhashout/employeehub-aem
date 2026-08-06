package com.abc.employeehub.core.servlets;

import com.abc.employeehub.core.services.EmployeeService;
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
                "sling.servlet.paths=/bin/employeehub/employee-search",
                "sling.servlet.methods=GET",
                "sling.servlet.extensions=json"
        }
)
public class EmployeeSearchServlet extends SlingSafeMethodsServlet {

    @Reference
    private transient EmployeeService employeeService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String query = request.getParameter("q");
        int maxResults = parseMaxResults(request.getParameter("limit"));

        JSONObject result = new JSONObject();
        try {
            if (query == null || query.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Search query parameter 'q' is required");
                result.put("employees", new JSONArray());
            } else {
                List<Map<String, Object>> employees = employeeService.searchEmployees(
                        request.getResourceResolver(), query.trim(), maxResults);
                result.put("success", true);
                result.put("count", employees.size());
                result.put("employees", toJsonArray(employees));
            }
            response.getWriter().write(result.toString());
        } catch (Exception e) {
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"Internal server error\"}");
        }
    }

    private JSONArray toJsonArray(List<Map<String, Object>> employees) throws Exception {
        JSONArray array = new JSONArray();
        for (Map<String, Object> employee : employees) {
            JSONObject obj = new JSONObject();
            for (Map.Entry<String, Object> entry : employee.entrySet()) {
                obj.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : "");
            }
            array.put(obj);
        }
        return array;
    }

    private int parseMaxResults(String limitParam) {
        if (limitParam == null) {
            return 20;
        }
        try {
            return Integer.parseInt(limitParam);
        } catch (NumberFormatException e) {
            return 20;
        }
    }
}
