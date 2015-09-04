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
package com.constellio.model.entities.records.wrappers.structure;

import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollowerFactory;
import com.constellio.sdk.tests.ConstellioTest;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

//AFTER Move to task module
public class TaskFollowerFactoryAcceptanceTest extends ConstellioTest {

	TaskFollowerFactory factory;
	String followerId = "zeFollower";
	Boolean followTaskStatusModified = true;
	Boolean followTaskAssigneeModified = false;
	Boolean followSubTasksModified = true;
	Boolean followTaskCompleted = false;
	Boolean followTaskDeleted = false;

	@Before
	public void setUp()
			throws Exception {
		factory = new TaskFollowerFactory();
	}

	@Test
	public void whenSetAttributeValueThenBecomesDirtyAndValueSet() {
		TaskFollower taskFollower = new TaskFollower();
		assertThat(taskFollower.isDirty()).isFalse();

		taskFollower = new TaskFollower();
		taskFollower.setFollowerId(followerId);
		assertThat(taskFollower.isDirty()).isTrue();
		assertThat(taskFollower.getFollowerId()).isEqualTo(followerId);

		taskFollower = new TaskFollower();
		taskFollower.setFollowTaskStatusModified(true);
		assertThat(taskFollower.isDirty()).isTrue();
		assertThat(taskFollower.getFollowTaskStatusModified()).isEqualTo(true);
		assertThat(taskFollower.getFollowSubTasksModified()).isFalse();
		assertThat(taskFollower.getFollowTaskAssigneeModified()).isFalse();
		assertThat(taskFollower.getFollowTaskCompleted()).isFalse();
		assertThat(taskFollower.getFollowTaskDeleted()).isFalse();

		taskFollower = new TaskFollower();
		taskFollower.setFollowTaskAssigneeModified(true);
		assertThat(taskFollower.isDirty()).isTrue();
		assertThat(taskFollower.getFollowTaskStatusModified()).isFalse();
		assertThat(taskFollower.getFollowSubTasksModified()).isFalse();
		assertThat(taskFollower.getFollowTaskAssigneeModified()).isEqualTo(true);
		assertThat(taskFollower.getFollowTaskCompleted()).isFalse();
		assertThat(taskFollower.getFollowTaskDeleted()).isFalse();

		taskFollower = new TaskFollower();
		taskFollower.setFollowSubTasksModified(true);
		assertThat(taskFollower.isDirty()).isTrue();
		assertThat(taskFollower.getFollowTaskStatusModified()).isFalse();
		assertThat(taskFollower.getFollowSubTasksModified()).isEqualTo(true);
		assertThat(taskFollower.getFollowTaskAssigneeModified()).isFalse();
		assertThat(taskFollower.getFollowTaskCompleted()).isFalse();
		assertThat(taskFollower.getFollowTaskDeleted()).isFalse();

		taskFollower = new TaskFollower();
		taskFollower.setFollowTaskCompleted(true);
		assertThat(taskFollower.isDirty()).isTrue();
		assertThat(taskFollower.getFollowTaskStatusModified()).isFalse();
		assertThat(taskFollower.getFollowSubTasksModified()).isFalse();
		assertThat(taskFollower.getFollowTaskAssigneeModified()).isFalse();
		assertThat(taskFollower.getFollowTaskCompleted()).isEqualTo(true);
		assertThat(taskFollower.getFollowTaskDeleted()).isFalse();

		taskFollower = new TaskFollower();
		taskFollower.setFollowTaskDeleted(true);
		assertThat(taskFollower.isDirty()).isTrue();
		assertThat(taskFollower.getFollowTaskStatusModified()).isFalse();
		assertThat(taskFollower.getFollowSubTasksModified()).isFalse();
		assertThat(taskFollower.getFollowTaskAssigneeModified()).isFalse();
		assertThat(taskFollower.getFollowTaskCompleted()).isFalse();
		assertThat(taskFollower.getFollowTaskDeleted()).isEqualTo(true);
	}

	@Test
	public void whenConvertingStructureWithAllValuesThenRemainsEqual()
			throws Exception {

		TaskFollower taskFollower = new TaskFollower();
		taskFollower.setFollowerId(followerId);
		taskFollower.setFollowTaskStatusModified(followTaskStatusModified);
		taskFollower.setFollowTaskAssigneeModified(followTaskAssigneeModified);
		taskFollower.setFollowSubTasksModified(followSubTasksModified);
		taskFollower.setFollowTaskCompleted(followTaskCompleted);
		taskFollower.setFollowTaskDeleted(followTaskDeleted);

		String stringValue = factory.toString(taskFollower);
		TaskFollower builtTaskFollower = (TaskFollower) factory.build(stringValue);
		String stringValue2 = factory.toString(builtTaskFollower);

		assertThat(builtTaskFollower).isEqualTo(taskFollower);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtTaskFollower.isDirty()).isFalse();
	}

	@Test
	public void whenConvertingStructureWithNullValuesThenRemainsEqual()
			throws Exception {

		TaskFollower taskFollower = new TaskFollower();

		String stringValue = factory.toString(taskFollower);
		TaskFollower builtTaskFollower = (TaskFollower) factory.build(stringValue);
		String stringValue2 = factory.toString(builtTaskFollower);

		assertThat(builtTaskFollower).isEqualTo(taskFollower);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtTaskFollower.isDirty()).isFalse();
		assertThat(builtTaskFollower.getFollowSubTasksModified()).isFalse();
		assertThat(builtTaskFollower.getFollowerId()).isNull();
	}

	@Test
	public void whenConvertingStructureWithoutSetValuesThenRemainsEqual()
			throws Exception {

		TaskFollower TaskFollower = new TaskFollower();

		String stringValue = factory.toString(TaskFollower);
		TaskFollower builtTaskFollower = (TaskFollower) factory.build(stringValue);
		String stringValue2 = factory.toString(builtTaskFollower);

		assertThat(builtTaskFollower).isEqualTo(TaskFollower);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtTaskFollower.isDirty()).isFalse();
		assertThat(builtTaskFollower.getFollowSubTasksModified()).isFalse();
		assertThat(builtTaskFollower.getFollowerId()).isNull();
	}
}

