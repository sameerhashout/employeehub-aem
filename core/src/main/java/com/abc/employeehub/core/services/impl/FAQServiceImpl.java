package com.abc.employeehub.core.services.impl;

import com.abc.employeehub.core.services.FAQService;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(service = FAQService.class, immediate = true)
public class FAQServiceImpl implements FAQService {

    private static final Logger LOG = LoggerFactory.getLogger(FAQServiceImpl.class);
    private static final String SERVICE_USER = "employeehub-service";

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public List<Map<String, String>> loadFAQs(String faqPath) {
        List<Map<String, String>> faqs = new ArrayList<>();
        try (ResourceResolver resolver = getServiceResourceResolver()) {
            Resource faqResource = resolver.getResource(faqPath);
            if (faqResource == null) {
                return faqs;
            }
            Resource itemsNode = faqResource.getChild("faqItems");
            if (itemsNode != null) {
                for (Resource item : itemsNode.getChildren()) {
                    ValueMap props = item.getValueMap();
                    Map<String, String> faq = new HashMap<>();
                    faq.put("question", props.get("question", ""));
                    faq.put("answer", props.get("answer", ""));
                    faqs.add(faq);
                }
            }
        } catch (Exception e) {
            LOG.error("Error loading FAQs from path: {}", faqPath, e);
        }
        return faqs;
    }

    @Override
    public List<Map<String, String>> sortFAQs(List<Map<String, String>> faqs) {
        if (faqs == null || faqs.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, String>> sorted = new ArrayList<>(faqs);
        sorted.sort(Comparator.comparing(f -> f.getOrDefault("question", "")));
        return sorted;
    }

    private ResourceResolver getServiceResourceResolver() throws Exception {
        Map<String, Object> authInfo = Collections.singletonMap(
                ResourceResolverFactory.SUBSERVICE, SERVICE_USER);
        return resourceResolverFactory.getServiceResourceResolver(authInfo);
    }
}
