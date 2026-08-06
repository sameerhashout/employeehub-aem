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
public class FAQModel {

    @ChildResource(name = "faqItems")
    private List<FAQItemModel> faqItems;

    public List<FAQItemModel> getFaqItems() {
        return faqItems != null ? faqItems : Collections.emptyList();
    }
}
