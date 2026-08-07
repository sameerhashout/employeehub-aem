package com.abc.employeehub.core.servlets;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.abc.employeehub.core.services.PollService;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/employeehub/poll",
                "sling.servlet.methods=GET",
                "sling.servlet.methods=POST",
                "sling.servlet.extensions=json"
        }
)
public class PollServlet extends SlingAllMethodsServlet {

    @Reference
    private transient PollService pollService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws IOException {
        String pollId = request.getParameter("pollId");
        writeTallies(response, pollService.getResults(request.getResourceResolver(), pollId));
    }

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws IOException {
        String pollId = request.getParameter("pollId");
        String option = request.getParameter("option");
        writeTallies(response, pollService.vote(request.getResourceResolver(), pollId, option));
    }

    private void writeTallies(SlingHttpServletResponse response, Map<String, Long> tallies)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            JSONObject result = new JSONObject();
            JSONObject counts = new JSONObject();
            long total = 0;
            for (Map.Entry<String, Long> entry : tallies.entrySet()) {
                counts.put(entry.getKey(), entry.getValue());
                total += entry.getValue();
            }
            result.put("success", true);
            result.put("total", total);
            result.put("results", counts);
            response.getWriter().write(result.toString());
        } catch (Exception e) {
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"Internal server error\"}");
        }
    }
}
