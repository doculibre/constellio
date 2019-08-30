package com.constellio.app.modules.tasks.extensions;

import com.constellio.app.api.extensions.RecordExportExtension;
import com.constellio.app.api.extensions.params.OnWriteRecordParams;
import com.constellio.app.modules.rm.extensions.imports.TaskImportExtension;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.search.SearchServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class TasksRecordExportExtension extends RecordExportExtension {

	String collection;
	AppLayerFactory appLayerFactory;

	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public TasksRecordExportExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public void onWriteRecord(OnWriteRecordParams params) {

		if (params.isRecordOfType(Task.SCHEMA_TYPE)) {
			manageUserTask(params);
		}

	}

	@Override
	public Set<String> getHashsToInclude() {

		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		Set<String> hashes = new HashSet<>();

		TasksSchemasRecordsServices tasks = new TasksSchemasRecordsServices(collection, appLayerFactory);
		hashes.addAll(searchServices.getHashesOf(from(tasks.taskSchemaType()).where(tasks.userTask.isModel()).isTrue()));

		return hashes;
	}


	private void manageUserTask(OnWriteRecordParams params) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		Task task = new Task(params.getRecord(), getTypes());

		List<Map<String, String>> listTaskReminder = new ArrayList<>();

		for (TaskReminder taskReminder : task.getReminders()) {
			Map<String, String> map = writeTaskReminder(taskReminder);
			listTaskReminder.add(map);
		}

		params.getModifiableImportRecord().addField(Task.REMINDERS, listTaskReminder);

		List<Map<String, String>> listTaskFollowers = new ArrayList<>();

		for (TaskFollower taskFollower : task.getTaskFollowers()) {
			listTaskFollowers.add(writeTaskFollowers(taskFollower));
		}

		params.getModifiableImportRecord().addField(Task.TASK_FOLLOWERS, listTaskFollowers);
	}


	private Map<String, String> writeTaskFollowers(TaskFollower taskFollower) {
		Map<String, String> map = new HashMap();

		map.put(TaskImportExtension.FOLLOWER_ID, taskFollower.getFollowerId());
		map.put(TaskImportExtension.FOLLOW_TASK_STATUS_MODIFIED, Boolean.toString(taskFollower.getFollowTaskStatusModified()));
		map.put(TaskImportExtension.FOLLOW_TASK_ASSIGNEE_MODIFIED, Boolean.toString(taskFollower.getFollowTaskAssigneeModified()));
		map.put(TaskImportExtension.FOLLOW_SUB_TASKS_MODIFIED, Boolean.toString(taskFollower.getFollowSubTasksModified()));
		map.put(TaskImportExtension.FOLLOW_TASK_COMPLETED, Boolean.toString(taskFollower.getFollowTaskCompleted()));
		map.put(TaskImportExtension.FOLLOW_TASK_DELETE, Boolean.toString(taskFollower.getFollowTaskDeleted()));

		return map;
	}

	private Map<String, String> writeTaskReminder(TaskReminder taskReminder) {
		Map<String, String> map = new HashMap();

		map.put(TaskImportExtension.FIXED_DATE, taskReminder.getFixedDate() != null ? taskReminder.getFixedDate().toString("yyyy-MM-dd") : null);
		map.put(TaskImportExtension.NUMBER_OF_DAYS_TO_RELATIVE_DATE, Integer.toString(taskReminder.getNumberOfDaysToRelativeDate()));
		map.put(TaskImportExtension.BEFORE_RELATIVE_DATE, convertBooleanToString(taskReminder.isBeforeRelativeDate()));
		map.put(TaskImportExtension.RELATIVE_DATE_METADATA_CODE, taskReminder.getRelativeDateMetadataCode());
		map.put(TaskImportExtension.PROCESSED, convertBooleanToString(taskReminder.isBeforeRelativeDate()));

		return map;
	}

	public MetadataSchemaTypes getTypes() {
		return appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
	}

	// N'as pas été fait encore. pusiqu'on ne peut pas vraiment sérialisé un Object et qu'il n'est pas vraiment essentiel.
	public static final String SCHEMA_TYPE = "schemaType";
	public static final String METADATA_CODE = "metadataCode";
	public static final String VALUE = "value";


	public String convertBooleanToString(Boolean b) {
		if (b == null) {
			return null;
		}
		return b.toString();
	}


}
