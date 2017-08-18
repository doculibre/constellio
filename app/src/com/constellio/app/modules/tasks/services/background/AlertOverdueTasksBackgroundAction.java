package com.constellio.app.modules.tasks.services.background;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.start.ApplicationStarter;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.*;

public class AlertOverdueTasksBackgroundAction implements Runnable {

	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private SearchServices searchServices;
	private RecordServices recordServices;
	private CollectionsListManager collectionsListManager;
	private String collection;

	public AlertOverdueTasksBackgroundAction(AppLayerFactory appLayerFactory, String collection) {
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
		this.appLayerFactory = appLayerFactory;
		this.collection = collection;
	}

	@Override
	public synchronized void run() {
		TasksSchemasRecordsServices tasksSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		LogicalSearchQuery query = new LogicalSearchQuery().setCondition(from(tasksSchemas.userTask.schemaType())
				.where(tasksSchemas.userTask.dueDate()).isLessThan(LocalDate.now())
				.andWhere(tasksSchemas.userTask.status()).isNotIn(tasksSchemas.getFinishedOrClosedStatuses())
				.andWhere(tasksSchemas.userTask.reminderFrequency()).isNotNull());

		SearchResponseIterator<Record> overdueTaskIterator = searchServices.recordsIterator(query, 1000);
		while(overdueTaskIterator.hasNext()) {
			Task task = tasksSchemas.wrapTask(overdueTaskIterator.next());
			LocalDate dueDate = task.getDueDate();
			String reminderFrequency = task.getReminderFrequency();
			LocalDateTime reminderDate = addFrequencyToDueDate(dueDate.toLocalDateTime(LocalTime.MIDNIGHT), reminderFrequency, LocalDateTime.now());
			if(reminderDate.isBefore(LocalDateTime.now())) {
				//TODO sendEmail, reminderFrequency can be either on creation date or on dueDate?
				//sendEmail
				try {
					recordServices.update(task.setLastReminder(LocalDateTime.now()));
				} catch (RecordServicesException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private LocalDateTime addFrequencyToDueDate(LocalDateTime dueDate, String reminderFrequency, LocalDateTime lastReminder) {
		String[] parameters = reminderFrequency.split(":");
		LocalDateTime returnedValue = null;
		LocalDateTime reminderDate = lastReminder == null? dueDate:lastReminder;
		if(parameters.length == 2) {
			String reminderFrequencyType = parameters[0];
			String reminderFrequencyValue = parameters[1];

			switch (reminderFrequencyType) {
				case "mm":
					returnedValue = reminderDate.plusMinutes(Integer.parseInt(reminderFrequencyValue));
					break;
				case "hh":
					returnedValue = reminderDate.plusHours(Integer.parseInt(reminderFrequencyValue));
					break;
				case "DD":
					returnedValue = reminderDate.plusDays(Integer.parseInt(reminderFrequencyValue));
					break;
			}
		}
		return returnedValue;
	}
}
