package com.abc.employeehub.core.services;

import java.util.Map;

import org.apache.sling.api.resource.ResourceResolver;

public interface PollService {

    /**
     * Records a vote for the given option under the given poll.
     *
     * @return the updated tallies (option -&gt; count)
     */
    Map<String, Long> vote(ResourceResolver resolver, String pollId, String option);

    /**
     * Returns the current tallies for a poll (option -&gt; count).
     */
    Map<String, Long> getResults(ResourceResolver resolver, String pollId);
}
