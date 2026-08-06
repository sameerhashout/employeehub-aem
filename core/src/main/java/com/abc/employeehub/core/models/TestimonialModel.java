package com.abc.employeehub.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;

import java.util.Collections;
import java.util.List;

@Model(
        adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TestimonialModel {

    @ChildResource(name = "testimonialItems")
    private List<TestimonialItemModel> testimonialItems;

    public List<TestimonialItemModel> getTestimonialItems() {
        return testimonialItems != null ? testimonialItems : Collections.emptyList();
    }
}
