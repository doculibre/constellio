/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
