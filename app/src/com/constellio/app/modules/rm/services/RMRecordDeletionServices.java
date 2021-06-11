package com.constellio.app.modules.rm.services;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.containingText;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;

/**
 * Created by Constelio on 2016-10-31.
 */
public class RMRecordDeletionServices {
	private static final Logger LOGGER = LoggerFactory.getLogger(RMRecordDeletionServices.class);

	static public void cleanAllAdministrativeUnits(String collection, AppLayerFactory appLayerFactory) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		List<AdministrativeUnit> administrativeUnitList = rm.searchAdministrativeUnits(ALL);
		for (AdministrativeUnit administrativeUnit : administrativeUnitList) {
			cleanAdministrativeUnit(collection, administrativeUnit, appLayerFactory);
		}
	}

	static public void cleanAdministrativeUnit(String collection, AdministrativeUnit administrativeUnit,
											   AppLayerFactory appLayerFactory) {
		cleanTaskInAdministrativeUnitRecursively(collection, administrativeUnit, appLayerFactory);
		cleanFoldersInAdministrativeUnitRecursively(collection, administrativeUnit, appLayerFactory);
		cleanContainersInAdministrativeUnitRecursively(collection, administrativeUnit, appLayerFactory);
		cleanDecommissioningListInAdministrativeUnit(collection, administrativeUnit, appLayerFactory);
		cleanRetentionRuleInAdministrativeUnit(collection, administrativeUnit, appLayerFactory);
	}

	static public void cleanAdministrativeUnit(String collection, String administrativeUnitID,
											   AppLayerFactory appLayerFactory) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		AdministrativeUnit administrativeUnit = rm.wrapAdministrativeUnit(searchServices.
				searchSingleResult(from(rm.administrativeUnit.schemaType()).where(Schemas.IDENTIFIER)
						.isEqualTo(administrativeUnitID)));
		cleanAdministrativeUnit(collection, administrativeUnit, appLayerFactory);
	}

	static private void cleanFoldersInAdministrativeUnitRecursively(String collection,
																	AdministrativeUnit administrativeUnit,
																	AppLayerFactory appLayerFactory) {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		TasksSchemasRecordsServices taskSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);

		SearchResponseIterator<Record> documentIterator = searchServices.recordsIterator(new LogicalSearchQuery().setCondition(from(rm.document.schemaType())
				.where(Schemas.PRINCIPAL_PATH).isContainingText(administrativeUnit.getId())).sortDesc(Schemas.PRINCIPAL_PATH));
		SearchResponseIterator<Record> folderIterator = searchServices.recordsIterator(new LogicalSearchQuery().setCondition(from(rm.folder.schemaType())
				.where(Schemas.PRINCIPAL_PATH).isContainingText(administrativeUnit.getId())).sortDesc(Schemas.PRINCIPAL_PATH));
		List<Record> taskList = searchServices.search(new LogicalSearchQuery().setCondition(from(taskSchemas.userTask.schemaType())
				.where(Schemas.PRINCIPAL_PATH).isNot(containingText(administrativeUnit.getId()))).sortDesc(Schemas.PRINCIPAL_PATH));

		Set<String> recordIDs = new HashSet<>();
		while (documentIterator.hasNext()) {
			Record document = documentIterator.next();
			unlinkDocumentFromDecommissioningLists(document, collection, appLayerFactory);
			unlinkDocumentFromTasks(document, taskList, collection, appLayerFactory);

			if (recordIDs.add(document.getId())) {
				try {
					recordServices.physicallyDeleteNoMatterTheStatus(document, User.GOD, new RecordPhysicalDeleteOptions());
				} catch (Exception e) {
					LOGGER.info("Could not delete document " + document.getId());
				}
			}
		}
		while (folderIterator.hasNext()) {
			Record folder = folderIterator.next();
			unlinkFolderFromDecommissioningLists(folder, collection, appLayerFactory);
			unlinkFolderFromTasks(folder, taskList, collection, appLayerFactory);

			if (recordIDs.add(folder.getId())) {
				try {
					recordServices.physicallyDeleteNoMatterTheStatus(folder, User.GOD, new RecordPhysicalDeleteOptions());
				} catch (Exception e) {
					LOGGER.info("Could not delete folder " + folder.getId());
				}
			}
		}
	}

	static private void cleanContainersInAdministrativeUnitRecursively(String collection,
																	   AdministrativeUnit administrativeUnit,
																	   AppLayerFactory appLayerFactory) {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();

		SearchResponseIterator<Record> containerIterator = searchServices.recordsIterator(new LogicalSearchQuery().setCondition(from(rm.containerRecord.schemaType())
				.where(Schemas.PRINCIPAL_PATH).isContainingText(administrativeUnit.getId())).sortDesc(Schemas.PRINCIPAL_PATH));

		Set<String> recordIDs = new HashSet<>();
		while (containerIterator.hasNext()) {
			Record container = containerIterator.next();
			unlinkContainerFromDecommissioningLists(container, collection, appLayerFactory);

			if (recordIDs.add(container.getId())) {
				try {
					recordServices.physicallyDeleteNoMatterTheStatus(container, User.GOD, new RecordPhysicalDeleteOptions());
				} catch (Exception e) {
					LOGGER.info("Could not delete container " + container.getId());
				}
			}
		}
	}

	static private void cleanTaskInAdministrativeUnitRecursively(String collection,
																 AdministrativeUnit administrativeUnit,
																 AppLayerFactory appLayerFactory) {

		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		TasksSchemasRecordsServices schemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		LogicalSearchQuery query = new LogicalSearchQuery().setCondition(from(schemas.userTask.schemaType())
				.where(Schemas.PRINCIPAL_PATH).isContainingText(administrativeUnit.getId())).sortDesc(Schemas.PRINCIPAL_PATH);

		SearchResponseIterator<Record> userTaskIterator = searchServices.recordsIterator(query);
		Set<String> recordIDs = new HashSet<>();

		while (userTaskIterator.hasNext()) {
			Record userTask = userTaskIterator.next();
			if (recordIDs.add(userTask.getId())) {
				try {
					recordServices.physicallyDeleteNoMatterTheStatus(userTask, User.GOD, new RecordPhysicalDeleteOptions());
				} catch (Exception e) {
					LOGGER.info("Could not delete task " + userTask.getId());
				}
			}
		}
	}

	static private void cleanDecommissioningListInAdministrativeUnit(String collection,
																	 AdministrativeUnit administrativeUnit,
																	 AppLayerFactory appLayerFactory) {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		LogicalSearchQuery query = new LogicalSearchQuery().setCondition(from(rm.decommissioningList.schemaType())
				.where(rm.decommissioningList.administrativeUnit()).isEqualTo(administrativeUnit.getId()));

		SearchResponseIterator<Record> decommissioningListIterator = searchServices.recordsIterator(query);
		Set<String> recordIDs = deleteRecordFromIterator(decommissioningListIterator, recordServices);
	}

	static private void cleanRetentionRuleInAdministrativeUnit(String collection, AdministrativeUnit administrativeUnit,
															   AppLayerFactory appLayerFactory) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		LogicalSearchCondition condition = from(rm.retentionRule.schemaType())
				.where(rm.retentionRule.administrativeUnits()).isContaining(Collections.singletonList(administrativeUnit.getId()));

		SearchResponseIterator<Record> retentionRuleIterator = searchServices.recordsIterator(new LogicalSearchQuery(condition));
		while (retentionRuleIterator.hasNext()) {
			RetentionRule currentRetentionRule = rm.wrapRetentionRule(retentionRuleIterator.next());
			List<AdministrativeUnit> currentAdministrativeUnits = rm.getAdministrativeUnits(currentRetentionRule.getAdministrativeUnits());
			currentAdministrativeUnits.remove(currentAdministrativeUnits.indexOf(administrativeUnit));
			currentRetentionRule.setAdministrativeUnits(currentAdministrativeUnits);
			currentRetentionRule.setResponsibleAdministrativeUnits(currentAdministrativeUnits.isEmpty());
			try {
				recordServices.update(currentRetentionRule);
			} catch (RecordServicesException e) {
				LOGGER.info("Error while updating retention rule" + currentRetentionRule.getId());
			}
		}
	}

	static private void unlinkDocumentFromDecommissioningLists(Record document, String collection,
															   AppLayerFactory appLayerFactory) {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		List<DecommissioningList> decommissioningLists = rm.searchDecommissioningLists(
				where(rm.decommissioningList.documents()).isContaining(asList(document.getId())));
		for (DecommissioningList decommissioningList : decommissioningLists) {
			ArrayList<String> decommissioningListDocuments = new ArrayList<>(decommissioningList.getDocuments());
			decommissioningListDocuments.remove(document.getId());
			decommissioningList.setDocuments(decommissioningListDocuments);
			try {
				recordServices.update(decommissioningList.getWrappedRecord());
			} catch (RecordServicesException e) {
				LOGGER.info("Could not unlink document from decommissioningList");
			}
		}
	}

	static private void unlinkFolderFromDecommissioningLists(Record folder, String collection,
															 AppLayerFactory appLayerFactory) {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		List<DecommissioningList> decommissioningLists = rm.searchDecommissioningLists(
				where(rm.decommissioningList.folders()).isContaining(asList(folder.getId())));
		for (DecommissioningList decommissioningList : decommissioningLists) {
			decommissioningList.removeFolderDetail(folder.getId());

			try {
				recordServices.update(decommissioningList.getWrappedRecord());
			} catch (RecordServicesException e) {
				LOGGER.info("Could not unlink folder from decommissioningList");
			}
		}
	}

	static private void unlinkContainerFromDecommissioningLists(Record container, String collection,
																AppLayerFactory appLayerFactory) {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		List<DecommissioningList> decommissioningLists = rm.searchDecommissioningLists(
				where(rm.decommissioningList.containers()).isContaining(asList(container.getId())));
		for (DecommissioningList decommissioningList : decommissioningLists) {
			decommissioningList.removeContainerDetail(container.getId());

			try {
				recordServices.update(decommissioningList.getWrappedRecord());
			} catch (RecordServicesException e) {
				LOGGER.info("Could not unlink container from decommissioningList");
			}
		}
	}

	static private void unlinkDocumentFromTasks(Record document, List<Record> taskList, String collection,
												AppLayerFactory appLayerFactory) {

		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		for (Record task : taskList) {
			MetadataSchema curTaskSchema = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager()
					.getSchemaTypes(collection).getSchema(task.getSchemaCode());
			List<String> linkedDocumentsIDs = task.get(curTaskSchema.getMetadata(RMTask.LINKED_DOCUMENTS));
			linkedDocumentsIDs = new ArrayList<>(linkedDocumentsIDs);
			if (linkedDocumentsIDs.contains(document.getId())) {
				linkedDocumentsIDs.remove(document.getId());
				task.set(curTaskSchema.getMetadata(RMTask.LINKED_DOCUMENTS), linkedDocumentsIDs);
				try {
					recordServices.update(task);
				} catch (RecordServicesException e) {
					LOGGER.info("Could not unlink document from task");
				}
			}
		}
	}

	static private void unlinkFolderFromTasks(Record folder, List<Record> taskList, String collection,
											  AppLayerFactory appLayerFactory) {

		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		for (Record task : taskList) {
			MetadataSchema curTaskSchema = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager()
					.getSchemaTypes(collection).getSchema(task.getSchemaCode());
			List<String> linkedDocumentsIDs = task.get(curTaskSchema.getMetadata(RMTask.LINKED_DOCUMENTS));
			linkedDocumentsIDs = new ArrayList<>(linkedDocumentsIDs);
			if (linkedDocumentsIDs.contains(folder.getId())) {
				linkedDocumentsIDs.remove(folder.getId());
				task.set(curTaskSchema.getMetadata(RMTask.LINKED_DOCUMENTS), linkedDocumentsIDs);
				try {
					recordServices.update(task);
				} catch (RecordServicesException e) {
					LOGGER.info("Could not unlink folder from task");
				}
			}
		}
	}

	static private Set<String> deleteRecordFromIterator(Iterator<Record> recordIterator,
														RecordServices recordServices) {
		Set<String> recordIDs = new HashSet<>();
		while (recordIterator.hasNext()) {
			Record currentRecord = recordIterator.next();
			if (recordIDs.add(currentRecord.getId())) {
				try {
					recordServices.physicallyDeleteNoMatterTheStatus(currentRecord, User.GOD, new RecordPhysicalDeleteOptions());
				} catch (Exception e) {
					e.printStackTrace();
					LOGGER.info("Could not delete " + currentRecord.getTypeCode() + " " + currentRecord.getId());
				}
			}
		}
		return recordIDs;
	}
}
