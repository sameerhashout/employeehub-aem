package com.abc.employeehub.core.configurations;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Announcement Configuration",
        description = "Configuration for the company-wide announcement banner"
)
public @interface AnnouncementConfiguration {

    @AttributeDefinition(name = "Announcement Message", description = "Default announcement message")
    String announcementMessage() default "Welcome to ABC Employee Hub!";

    @AttributeDefinition(name = "Banner Color", description = "CSS background color for the banner")
    String bannerColor() default "#0066cc";

    @AttributeDefinition(name = "Enable Banner", description = "Toggle announcement banner visibility")
    boolean enableBanner() default true;
}
