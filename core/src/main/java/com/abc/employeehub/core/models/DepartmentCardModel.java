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
public class DepartmentCardModel {

    @Self
    private Resource resource;

    @ValueMapValue
    private String departmentName;

    @ValueMapValue
    private String manager;

    @ValueMapValue
    private String employeeCount;

    @ValueMapValue
    private String departmentImage;

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

    public String getDepartmentName() {
        return departmentName;
    }

    public String getManager() {
        return manager;
    }

    public String getEmployeeCount() {
        return employeeCount;
    }

    public String getDepartmentImage() {
        return departmentImage;
    }

    public String[] getTags() {
        return tags != null ? tags : new String[0];
    }

    public List<String> getTagTitles() {
        return tagTitles;
    }

    public String getBannerEmoji() {
        if (departmentName == null) {
            return "🏢";
        }
        String dept = departmentName.toLowerCase();
        if (dept.contains("information") || dept.contains("technology")) {
            return "💻";
        }
        if (dept.contains("human") || dept.equals("hr")) {
            return "👥";
        }
        if (dept.contains("finance")) {
            return "📊";
        }
        return "🏢";
    }

    public String getBannerClass() {
        if (departmentName == null) {
            return "eh-department-card__banner";
        }
        String dept = departmentName.toLowerCase();
        if (dept.contains("information") || dept.contains("technology")) {
            return "eh-department-card__banner eh-department-card__banner--it";
        }
        if (dept.contains("human") || dept.equals("hr")) {
            return "eh-department-card__banner eh-department-card__banner--hr";
        }
        if (dept.contains("finance")) {
            return "eh-department-card__banner eh-department-card__banner--finance";
        }
        return "eh-department-card__banner";
    }
}
