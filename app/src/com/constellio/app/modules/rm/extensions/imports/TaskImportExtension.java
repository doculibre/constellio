package com.constellio.app.modules.rm.extensions.imports;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.model.entities.records.wrappers.structure.ReportedMetadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.extensions.behaviors.RecordImportExtension;
import com.constellio.model.extensions.events.recordsImport.BuildParams;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by constellios on 2017-05-16.
 */
public class TaskImportExtension extends RecordImportExtension {

    public static final String FIXED_DATE = "fixedDate";
    public static final String NUMBER_OF_DAYS_TO_RELATIVE_DATE = "numberOfDaysToRelativeDate";
    public static final String BEFORE_RELATIVE_DATE = "beforeRelativeDate";
    public static final String RELATIVE_DATE_METADATA_CODE = "relativeDateMetadataCode";
    public static final String PROCESSED = "processed";

    public static final String FOLLOWER_ID = "followerId";
    public static final String FOLLOW_TASK_STATUS_MODIFIED = "followTaskStatusModified";
    public static final String FOLLOW_TASK_ASSIGNEE_MODIFIED = "followTaskAssigneeModified";
    public static final String FOLLOW_SUB_TASKS_MODIFIED = "followSubTasksModified";
    public static final String FOLLOW_TASK_COMPLETED = "followTaskCompleted";
    public static final String FOLLOW_TASK_DELETE = "followTaskDeleted";


    ModelLayerFactory modelLayerFactory;
    String collection;

    public TaskImportExtension(String collection, ModelLayerFactory modelLayerFactory) {
        this.modelLayerFactory = modelLayerFactory;
        this.collection = collection;
    }

    @Override
    public void build(BuildParams event) {
        Task task = new Task(event.getRecord(), getTypes());

        List<Map<String, String>> mapTaskReminderList = event.getImportRecord().getList(Task.REMINDERS);
        List<TaskReminder> taskReminderList = new ArrayList<>();

        for (Map<String,String> mapTaskReminder : mapTaskReminderList) {
            taskReminderList.add(readTaskReminder(mapTaskReminder));
        }

        task.setReminders(taskReminderList);

        List<Map<String, String>> mapTaskFollowersList = event.getImportRecord().getList(Task.TASK_FOLLOWERS);
        List<TaskFollower> taskFollowerList = new ArrayList<>();

        for (Map<String,String> mapTaskFollower : mapTaskFollowersList)
        {
            TaskFollower taskFollower = readTaskFollower(mapTaskFollower);
            taskFollowerList.add(taskFollower);
        }

        task.setTaskFollowers(taskFollowerList);
    }

    private TaskReminder readTaskReminder(Map<String, String> mapTaskReminder) {
        TaskReminder taskReminder = new TaskReminder();

        if(mapTaskReminder.get(TaskImportExtension.FIXED_DATE) != null) {
            taskReminder.setFixedDate(LocalDate.parse(mapTaskReminder.get(TaskImportExtension.FIXED_DATE)));
        }

        taskReminder.setNumberOfDaysToRelativeDate(Integer.parseInt(mapTaskReminder.get(TaskImportExtension.NUMBER_OF_DAYS_TO_RELATIVE_DATE)));
        taskReminder.setBeforeRelativeDate(Boolean.parseBoolean(mapTaskReminder.get(TaskImportExtension.BEFORE_RELATIVE_DATE)));

        if(mapTaskReminder.get(TaskImportExtension.RELATIVE_DATE_METADATA_CODE) != null) {
            taskReminder.setRelativeDateMetadataCode(mapTaskReminder.get(TaskImportExtension.RELATIVE_DATE_METADATA_CODE));
        }

        taskReminder.setProcessed(Boolean.parseBoolean(mapTaskReminder.get(TaskImportExtension.PROCESSED)));

        return taskReminder;
    }

    private TaskFollower readTaskFollower(Map<String, String> mapTaskFollower) {
        TaskFollower taskFollower = new TaskFollower();

        if(mapTaskFollower.get(TaskImportExtension.FOLLOWER_ID) != null) {
            taskFollower.setFollowerId(mapTaskFollower.get(TaskImportExtension.FOLLOWER_ID));
        }

        taskFollower.setFollowTaskStatusModified(Boolean.parseBoolean(mapTaskFollower.get(TaskImportExtension.FOLLOW_TASK_STATUS_MODIFIED)));
        taskFollower.setFollowTaskAssigneeModified(Boolean.parseBoolean(mapTaskFollower.get(TaskImportExtension.FOLLOW_TASK_ASSIGNEE_MODIFIED)));
        taskFollower.setFollowSubTasksModified(Boolean.parseBoolean(mapTaskFollower.get(TaskImportExtension.FOLLOW_SUB_TASKS_MODIFIED)));
        taskFollower.setFollowTaskCompleted(Boolean.parseBoolean(mapTaskFollower.get(TaskImportExtension.FOLLOW_TASK_COMPLETED)));
        taskFollower.setFollowTaskDeleted(Boolean.parseBoolean(mapTaskFollower.get(TaskImportExtension.FOLLOW_TASK_DELETE)));

        return taskFollower;
    }

    public MetadataSchemaTypes getTypes() {
        return modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
    }

    @Override
    public String getDecoratedSchemaType() {
        return Task.SCHEMA_TYPE;
    }
}
