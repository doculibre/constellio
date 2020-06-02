package com.constellio.app.modules.tasks.model.wrappers;

import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.security.roles.Roles;

/**
 * Created by Constellio on 2017-04-26.
 */
public class TaskUser extends User{
    public static final String DEFAULT_FOLLOWER_WHEN_CREATING_TASK = "defaultFollowerWhenCreatingTask";
    public static final String ASSIGN_TASK_AUTOMATICALLY = "assignTaskAutomatically";
    public static final String DELEGATION_TASK_USER = "delegationTaskUser";

    public TaskUser(Record record, MetadataSchemaTypes types, Roles roles) {
        super(record, types, roles);
    }

    public TaskFollower getDefaultFollowerWhenCreatingTask() {
        return get(DEFAULT_FOLLOWER_WHEN_CREATING_TASK);
    }

    public TaskUser setDefaultFollowerWhenCreatingTask(TaskFollower taskFollower) {
        set(DEFAULT_FOLLOWER_WHEN_CREATING_TASK, taskFollower);
        return this;
    }

    public Boolean getAssignTaskAutomatically() {
        return get(ASSIGN_TASK_AUTOMATICALLY);
    }

    public TaskUser setAssignTaskAutomatically(Boolean isAssigningTaskAutomatically) {
        set(ASSIGN_TASK_AUTOMATICALLY, isAssigningTaskAutomatically);
        return this;
    }
}
