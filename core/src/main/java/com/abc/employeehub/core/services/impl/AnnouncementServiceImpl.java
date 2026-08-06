package com.abc.employeehub.core.services.impl;

import com.abc.employeehub.core.configurations.AnnouncementConfiguration;
import com.abc.employeehub.core.services.AnnouncementService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

@Component(service = AnnouncementService.class, immediate = true)
@Designate(ocd = AnnouncementConfiguration.class)
public class AnnouncementServiceImpl implements AnnouncementService {

    private String announcementMessage;
    private String bannerColor;
    private boolean enableBanner;

    @Activate
    @Modified
    protected void activate(AnnouncementConfiguration config) {
        this.announcementMessage = config.announcementMessage();
        this.bannerColor = config.bannerColor();
        this.enableBanner = config.enableBanner();
    }

    @Override
    public String getAnnouncementTitle() {
        return "Company Announcement";
    }

    @Override
    public String getAnnouncementMessage() {
        return announcementMessage;
    }

    @Override
    public String getBannerColor() {
        return bannerColor;
    }

    @Override
    public boolean isBannerEnabled() {
        return enableBanner;
    }
}
