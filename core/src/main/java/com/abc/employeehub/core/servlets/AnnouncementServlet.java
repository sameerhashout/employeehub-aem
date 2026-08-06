package com.abc.employeehub.core.servlets;

import com.abc.employeehub.core.services.AnnouncementService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/employeehub/announcement",
                "sling.servlet.methods=GET",
                "sling.servlet.extensions=json"
        }
)
public class AnnouncementServlet extends SlingSafeMethodsServlet {

    @Reference
    private transient AnnouncementService announcementService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            JSONObject announcement = new JSONObject();
            announcement.put("title", announcementService.getAnnouncementTitle());
            announcement.put("message", announcementService.getAnnouncementMessage());
            announcement.put("bannerColor", announcementService.getBannerColor());
            announcement.put("enabled", announcementService.isBannerEnabled());
            response.getWriter().write(announcement.toString());
        } catch (Exception e) {
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"Internal server error\"}");
        }
    }
}
