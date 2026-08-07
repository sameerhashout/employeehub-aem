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
public class PollModel {

    @ValueMapValue
    private String pollId;

    @ValueMapValue
    private String question;

    @ChildResource(name = "options")
    private List<PollOptionModel> options;

    public String getPollId() {
        return pollId != null ? pollId : "";
    }

    public String getQuestion() {
        return question != null && !question.isEmpty() ? question : "Quick Poll";
    }

    public List<PollOptionModel> getOptions() {
        return options != null ? options : Collections.emptyList();
    }
}
