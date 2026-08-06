package com.abc.employeehub.core.models;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Model(
        adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class EmployeeCardModel {

    @Self
    private Resource resource;

    @ValueMapValue
    private String employeeName;

    @ValueMapValue
    private String email;

    @ValueMapValue
    private String department;

    @ValueMapValue
    private String photo;

    @ValueMapValue
    private String employeeId;

    @ValueMapValue(name = "cq:tags")
    private String[] tags;

    private List<String> tagTitles = Collections.emptyList();

    @PostConstruct
    protected void init() {
        if (tags == null || tags.length == 0) {
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
            if (tag != null) {
                titles.add(tag.getTitle());
            }
        }
        tagTitles = titles;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getEmail() {
        return email;
    }

    public String getDepartment() {
        return department;
    }

    public String getPhoto() {
        return photo;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getInitials() {
        if (employeeName == null || employeeName.isEmpty()) {
            return "?";
        }
        String[] parts = employeeName.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        }
        return employeeName.substring(0, 1).toUpperCase();
    }

    public String getAvatarClass() {
        if (department == null) {
            return "eh-avatar";
        }
        String dept = department.toLowerCase();
        if (dept.contains("human") || dept.contains("hr")) {
            return "eh-avatar eh-avatar--hr";
        }
        if (dept.contains("finance")) {
            return "eh-avatar eh-avatar--finance";
        }
        return "eh-avatar eh-avatar--it";
    }

    public String[] getTags() {
        return tags != null ? tags : new String[0];
    }

    public List<String> getTagTitles() {
        return tagTitles;
    }
}
