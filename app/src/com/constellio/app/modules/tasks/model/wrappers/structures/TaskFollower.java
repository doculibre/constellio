package com.constellio.app.modules.tasks.model.wrappers.structures;

import com.constellio.model.entities.schemas.ModifiableStructure;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class TaskFollower implements ModifiableStructure {
    private boolean dirty;
    String followerId;
    boolean followTaskStatusModified;
    boolean followTaskAssigneeModified;
    boolean followSubTasksModified;
    boolean followTaskCompleted;
    boolean followTaskDeleted;

    public TaskFollower setDirty(boolean dirty) {
        this.dirty = dirty;
        return this;
    }

    public String getFollowerId() {
        return followerId;
    }

    public TaskFollower setFollowerId(String followerId) {
        dirty = true;
        this.followerId = followerId;
        return this;
    }

    public Boolean getFollowTaskStatusModified() {
        return followTaskStatusModified;
    }

    public TaskFollower setFollowTaskStatusModified(Boolean followTaskStatusModified) {
        dirty = true;
        this.followTaskStatusModified = followTaskStatusModified;
        return this;
    }

    public Boolean getFollowTaskAssigneeModified() {
        return followTaskAssigneeModified;
    }

    public TaskFollower setFollowTaskAssigneeModified(Boolean followTaskAssigneeModified) {
        dirty = true;
        this.followTaskAssigneeModified = followTaskAssigneeModified;
        return this;
    }

    public Boolean getFollowSubTasksModified() {
        return followSubTasksModified;
    }

    public TaskFollower setFollowSubTasksModified(Boolean followSubTasksModified) {
        dirty = true;
        this.followSubTasksModified = followSubTasksModified;
        return this;
    }

    public Boolean getFollowTaskCompleted() {
        return followTaskCompleted;
    }

    public TaskFollower setFollowTaskCompleted(Boolean followTaskCompleted) {
        dirty = true;
        this.followTaskCompleted = followTaskCompleted;
        return this;
    }

    public Boolean getFollowTaskDeleted() {
        return followTaskDeleted;
    }

    public TaskFollower setFollowTaskDeleted(Boolean followTaskDeleted) {
        dirty = true;
        this.followTaskDeleted = followTaskDeleted;
        return this;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, "dirty");
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, "dirty");
    }
}
