package com.abc.employeehub.core.models;

import java.util.Collections;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(
        adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class EventsCarouselModel {

    @ValueMapValue
    private String heading;

    @ChildResource(name = "events")
    private List<EventItemModel> events;

    public String getHeading() {
        return heading != null && !heading.isEmpty() ? heading : "Upcoming Events";
    }

    public List<EventItemModel> getEvents() {
        return events != null ? events : Collections.emptyList();
    }
}
