package com.abc.employeehub.core.models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;

@Model(
        adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class EventItemModel {

    @Self
    private Resource resource;

    @ValueMapValue
    private String image;

    @ValueMapValue
    private String title;

    @ValueMapValue
    private String description;

    @ValueMapValue
    private Calendar eventDate;

    @ValueMapValue(name = "cq:tags")
    private String[] tags;

    private List<String> tagTitles = Collections.emptyList();

    @PostConstruct
    protected void init() {
        if (tags == null || tags.length == 0 || resource == null) {
            return;
        }
        ResourceResolver resolver = resource.getResourceResolver();
        TagManager tagManager = resolver.adaptTo(TagManager.class);
        if (tagManager == null) {
            return;
        }
        List<String> titles = new ArrayList<>();
        for (String tagId : tags) {
            Tag tag = tagManager.resolve(tagId);
            titles.add(tag != null ? tag.getTitle() : tagId);
        }
        tagTitles = titles;
    }

    public String getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getFormattedDate() {
        if (eventDate == null) {
            return "";
        }
        return new SimpleDateFormat("EEE, dd MMM yyyy • hh:mm a").format(eventDate.getTime());
    }

    public List<String> getTagTitles() {
        return tagTitles;
    }
}
