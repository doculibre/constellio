package com.constellio.app.modules.tasks.model.wrappers.structures;

import com.constellio.model.entities.schemas.CombinedStructureFactory;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.IOException;

public class TaskFollowerFactory implements CombinedStructureFactory {
	transient private GsonBuilder gsonBuilder;
	transient private Gson gson;

	public TaskFollowerFactory() {
		initTransientObjects();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		ExclusionStrategy strategy = new ExclusionStrategy() {
			@Override
			public boolean shouldSkipField(FieldAttributes f) {
				if (f.getName().equals("dirty")) {
					return true;
				}
				return false;
			}

			@Override
			public boolean shouldSkipClass(Class<?> clazz) {
				return false;
			}
		};
		gsonBuilder = new GsonBuilder().setExclusionStrategies(strategy);
		gson = gsonBuilder.create();
	}

	@Override
	public ModifiableStructure build(String serializedCriterion) {
		TaskFollower returnFollower = new TaskFollower();
		if (StringUtils.isNotBlank(serializedCriterion)) {
			returnFollower = gson.fromJson(serializedCriterion, TaskFollower.class);
			returnFollower = rebuildTaskFollower(returnFollower);
		}
		return returnFollower;
	}

	public TaskFollower rebuildTaskFollower(TaskFollower taskFollower) {
		String followerID = taskFollower.getFollowerId();
		Boolean assigneeModified = taskFollower.getFollowTaskAssigneeModified();
		Boolean subTasksModified = taskFollower.getFollowSubTasksModified();
		Boolean statusModified = taskFollower.getFollowTaskStatusModified();
		Boolean taskCompleted = taskFollower.getFollowTaskCompleted();
		Boolean taskDeleted = taskFollower.getFollowTaskDeleted();
		return new TaskFollower().setFollowerId(followerID).setFollowTaskAssigneeModified(assigneeModified)
				.setFollowSubTasksModified(subTasksModified).setFollowTaskStatusModified(statusModified)
				.setFollowTaskDeleted(taskDeleted).setFollowTaskCompleted(taskCompleted)
				.setDirty(false);
	}

	@Override
	public String toString(ModifiableStructure structure) {
		return gson.toJson(structure);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

}
