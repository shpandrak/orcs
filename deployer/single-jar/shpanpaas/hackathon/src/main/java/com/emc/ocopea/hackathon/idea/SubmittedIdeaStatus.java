package com.emc.ocopea.hackathon.idea;

/**
 * Created by liebea on 5/29/15.
 * Drink responsibly
 */
public class SubmittedIdeaStatus {
    private final String id;
    private final IdeaStatusEnum status;

    private SubmittedIdeaStatus() {
        this(null, null);
    }

    public SubmittedIdeaStatus(String id, IdeaStatusEnum status) {
        this.id = id;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public IdeaStatusEnum getStatus() {
        return status;
    }
}
