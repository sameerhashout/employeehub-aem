package com.abc.employeehub.core.servlets;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.abc.employeehub.core.services.WeatherService;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/employeehub/weather",
                "sling.servlet.methods=GET",
                "sling.servlet.extensions=json"
        }
)
public class WeatherServlet extends SlingSafeMethodsServlet {

    @Reference
    private transient WeatherService weatherService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String city = request.getParameter("city");
        try {
            Map<String, Object> weather = weatherService.getCurrentWeather(city);
            JSONObject result = new JSONObject();
            for (Map.Entry<String, Object> entry : weather.entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }
            response.getWriter().write(result.toString());
        } catch (Exception e) {
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"Internal server error\"}");
        }
    }
}
