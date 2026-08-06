package com.abc.employeehub.core.models;

import com.abc.employeehub.core.configurations.AnnouncementConfiguration;
import com.abc.employeehub.core.services.AnnouncementService;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(
        adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class AnnouncementBannerModel {

    @ValueMapValue
    private String title;

    @ValueMapValue
    private String message;

    @OSGiService
    private AnnouncementService announcementService;

    public String getTitle() {
        if (title != null && !title.isEmpty()) {
            return title;
        }
        return announcementService != null ? announcementService.getAnnouncementTitle() : "Announcement";
    }

    public String getMessage() {
        if (message != null && !message.isEmpty()) {
            return message;
        }
        return announcementService != null ? announcementService.getAnnouncementMessage() : "";
    }

    public String getBannerColor() {
        return announcementService != null ? announcementService.getBannerColor() : "#0066cc";
    }

    public boolean isEnabled() {
        return announcementService == null || announcementService.isBannerEnabled();
    }
}
