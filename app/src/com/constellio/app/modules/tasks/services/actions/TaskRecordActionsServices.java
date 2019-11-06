package com.constellio.app.modules.tasks.services.actions;

import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.modules.tasks.extensions.api.TaskModuleExtensions;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TaskPresenterServices;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.TasksSearchServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordUtils;

import java.util.List;

public class TaskRecordActionsServices {

	private TasksSchemasRecordsServices task;
	private TaskModuleExtensions taskModuleExtensions;
	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private String collection;
	private RecordServices recordServices;
	private BorrowingServices borrowingServices;
	private TaskPresenterServices taskPresenterServices;
	private TasksSchemasRecordsServices tasksSchemas;
	private TasksSearchServices tasksSearchServices;

	public TaskRecordActionsServices(String collection, AppLayerFactory appLayerFactory) {
		this.task = new TasksSchemasRecordsServices(collection, appLayerFactory);
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.taskModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(TaskModule.ID);
		this.borrowingServices = new BorrowingServices(collection, appLayerFactory.getModelLayerFactory());

		this.tasksSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		this.tasksSearchServices = new TasksSearchServices(tasksSchemas);
		this.taskPresenterServices = new TaskPresenterServices(tasksSchemas, recordServices, tasksSearchServices, modelLayerFactory.newLoggingServices());
	}

	public boolean isConsultActionPossible(Record record, User user) {
		return user.hasReadAccess().on(record)
			   && isNotLogicallyDeleted(record)
			   && taskModuleExtensions.isConsultActionPossibleOnTask(tasksSchemas.wrapTask(record), user);
	}

	public boolean isEditActionPossible(Record record, User user) {
		return user.hasWriteAccess().on(record)
			   && isNotLogicallyDeleted(record)
			   && !isClosedOrTerminated(tasksSchemas.wrapTask(record))
			   && taskModuleExtensions.isEditActionPossibleOnTask(tasksSchemas.wrapTask(record), user);
	}

	public boolean isClosedOrTerminated(Task task) {
		String closed = task.getStatus();
		boolean isClosedOrTerminated = getFinishedOrClosedStatuses().contains(closed);

		return isClosedOrTerminated;
	}

	private List<String> getFinishedOrClosedStatuses() {
		return new RecordUtils().toWrappedRecordIdsList(tasksSchemas.getFinishedOrClosedStatuses());
	}

	public boolean isNotLogicallyDeleted(Record record) {
		Task task = tasksSchemas.wrapTask(record);

		return !task.isLogicallyDeletedStatus();
	}

	public boolean isAutoAssignActionPossible(Record record, User user) {
		return isNotLogicallyDeleted(record)
			   && taskPresenterServices.isAutoAssignButtonEnabled(record, user)
			   && taskModuleExtensions.isAutoAssignActionPossibleOnTask(task.wrapTask(record), user);
	}

	public boolean isCompleteTaskActionPossible(Record record, User user) {
		return isNotLogicallyDeleted(record)
			   && taskPresenterServices.isCompleteTaskButtonVisible(record, user)
			   && taskModuleExtensions.isCompleteTaskActionPossibleOnTask(task.wrapTask(record), user);
	}

	public boolean isCloseTaskActionPossible(Record record, User user) {
		return isNotLogicallyDeleted(record)
			   && taskPresenterServices.isCloseTaskButtonVisible(record, user)
			   && taskModuleExtensions.isCloseTaskActionPossibleOnTask(task.wrapTask(record), user);
	}


	public boolean isCreateSubTaskActionPossible(Record record, User user) {
		return isNotLogicallyDeleted(record)
			   && user.hasWriteAccess().on(record)
			   && !isClosedOrTerminated(tasksSchemas.wrapTask(record))
			   && taskModuleExtensions.isCreateSubTaskActionPossibleOnTask(task.wrapTask(record), user);
	}

	public boolean isDeleteActionPossible(Record record, User user) {
		return isNotLogicallyDeleted(record)
			   && taskPresenterServices.isDeleteTaskButtonVisible(record, user)
			   && taskModuleExtensions.isDeleteActionPossibleOnTask(task.wrapTask(record), user);
	}

	public boolean isGenerateReportActionPossible(Record record, User user) {
		return isNotLogicallyDeleted(record) &&
			   taskModuleExtensions.isGenerateReportActionPossibleOnTask(task.wrapTask(record), user);
	}

	public boolean isConsultLinkActionPossible(Record record, User user) {
		return user.hasReadAccess().on(record)
			   && taskModuleExtensions.isConsultLinkActionPossibleOnTask(task.wrapTask(record), user);
	}
}
