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

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.TasksSearchServices;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.StructureFactory;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TaskFollowerFactory implements StructureFactory {
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
