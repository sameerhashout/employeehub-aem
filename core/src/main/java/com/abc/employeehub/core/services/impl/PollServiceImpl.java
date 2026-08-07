package com.abc.employeehub.core.services.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.abc.employeehub.core.services.PollService;

@Component(service = PollService.class, immediate = true)
public class PollServiceImpl implements PollService {

    private static final Logger LOG = LoggerFactory.getLogger(PollServiceImpl.class);
    private static final String POLL_ROOT = "/content/employeehub/data/polls";

    @Override
    public Map<String, Long> vote(ResourceResolver resolver, String pollId, String option) {
        if (resolver == null || isBlank(pollId) || isBlank(option)) {
            return new HashMap<>();
        }
        try {
            Resource pollResource = getOrCreate(resolver, POLL_ROOT + "/" + sanitize(pollId), "nt:unstructured");
            String optionKey = sanitize(option);
            long updated = pollResource.getValueMap().get(optionKey, 0L) + 1;

            ModifiableValueMap mvm = pollResource.adaptTo(ModifiableValueMap.class);
            if (mvm != null) {
                mvm.put(optionKey, updated);
                resolver.commit();
            }
        } catch (PersistenceException e) {
            LOG.error("Failed to record vote for poll {} option {}", pollId, option, e);
        }
        return getResults(resolver, pollId);
    }

    @Override
    public Map<String, Long> getResults(ResourceResolver resolver, String pollId) {
        Map<String, Long> tallies = new HashMap<>();
        if (resolver == null || isBlank(pollId)) {
            return tallies;
        }
        Resource pollResource = resolver.getResource(POLL_ROOT + "/" + sanitize(pollId));
        if (pollResource == null) {
            return tallies;
        }
        ValueMap props = pollResource.getValueMap();
        for (Map.Entry<String, Object> entry : props.entrySet()) {
            String key = entry.getKey();
            if (key.contains(":")) {
                continue;
            }
            Object value = entry.getValue();
            if (value instanceof Number) {
                tallies.put(key, ((Number) value).longValue());
            }
        }
        return tallies;
    }

    private Resource getOrCreate(ResourceResolver resolver, String path, String primaryType)
            throws PersistenceException {
        Resource existing = resolver.getResource(path);
        if (existing != null) {
            return existing;
        }
        int lastSlash = path.lastIndexOf('/');
        Resource parent = getOrCreate(resolver, path.substring(0, lastSlash), "sling:Folder");
        Map<String, Object> props = new HashMap<>();
        props.put("jcr:primaryType", primaryType);
        return resolver.create(parent, path.substring(lastSlash + 1), props);
    }

    private String sanitize(String value) {
        return value.trim().toLowerCase().replaceAll("[^a-z0-9_-]", "-");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
