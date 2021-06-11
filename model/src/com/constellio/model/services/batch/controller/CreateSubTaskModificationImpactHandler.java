package com.constellio.model.services.batch.controller;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.BatchProcessReport;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.HierarchyReindexingRecordsModificationImpact;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.ModificationImpact;
import com.constellio.model.entities.schemas.QueryBasedReindexingBatchProcessModificationImpact;
import com.constellio.model.entities.schemas.ReindexingRecordsModificationImpact;
import com.constellio.model.services.batch.actions.ReindexMetadatasBatchProcessAction;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.model.services.records.reindexing.ReindexationParams;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CreateSubTaskModificationImpactHandler {

	List<QueryBasedReindexingBatchProcessModificationImpact> impacts = new ArrayList<>();

	SearchServices searchServices;

	RecordServices recordServices;

	MetadataSchemaTypes metadataSchemaTypes;

	TaskList taskList;

	User user;

	BatchProcessReport report;

	ModelLayerFactory modelLayerFactory;

	Transaction transaction;

	public CreateSubTaskModificationImpactHandler(SearchServices searchServices, RecordServices recordServices,
												  MetadataSchemaTypes metadataSchemaTypes, TaskList taskList, User user,
												  ModelLayerFactory modelLayerFactory) {
		this(searchServices, recordServices, metadataSchemaTypes, taskList, user, null, modelLayerFactory);
	}

	public CreateSubTaskModificationImpactHandler(SearchServices searchServices, RecordServices recordServices,
												  MetadataSchemaTypes metadataSchemaTypes, TaskList taskList, User user,
												  BatchProcessReport report, ModelLayerFactory modelLayerFactory) {
		this.searchServices = searchServices;
		this.recordServices = recordServices;
		this.metadataSchemaTypes = metadataSchemaTypes;
		this.taskList = taskList;
		this.user = user;
		this.report = report;
		this.modelLayerFactory = modelLayerFactory;
	}

	public void handle(List<ModificationImpact> modificationImpacts) {
		for (ModificationImpact modificationImpact : modificationImpacts) {
			handleModificationImpact((QueryBasedReindexingBatchProcessModificationImpact) modificationImpact);
		}
	}

	void handleModificationImpact(ModificationImpact modificationImpact) {
		List<String> metadatas = newSchemaUtils().toMetadataCodes(modificationImpact.getMetadataToReindex());

		if (modificationImpact instanceof QueryBasedReindexingBatchProcessModificationImpact) {
			Iterator<List<Record>> batchIterator = getBatchsIterator((QueryBasedReindexingBatchProcessModificationImpact) modificationImpact);
			while (batchIterator.hasNext()) {
				List<Record> records = batchIterator.next();
				createSubTask(records, metadatas);
			}

		} else if (modificationImpact instanceof ReindexingRecordsModificationImpact) {
			ReindexingRecordsModificationImpact impact = (ReindexingRecordsModificationImpact) modificationImpact;
			List<Record> records = impact.getRecordsSupplier().get();
			createSubTask(records, metadatas);

		} else if (modificationImpact instanceof HierarchyReindexingRecordsModificationImpact) {

			taskList.addSubTask(() -> {
				RecordId id = ((HierarchyReindexingRecordsModificationImpact) modificationImpact).getRootIdToReindex();
				ReindexationParams params = new ReindexationParams(ReindexationMode.RECALCULATE);
				params.setMultithreading(false);
				params.setRepopulate(false);
				params.setLimitToHierarchyOf(Arrays.asList(id));
				modelLayerFactory.newReindexingServices().reindexCollection(modificationImpact.getCollection(), params);
			});

		}
	}

	void createSubTask(List<Record> subRecords, List<String> metadatas) {
		BatchProcessAction action = new ReindexMetadatasBatchProcessAction(metadatas);
		BatchProcessTask task = new BatchProcessTask(taskList, subRecords, action, recordServices,
				metadataSchemaTypes, searchServices, user, report, modelLayerFactory);
		taskList.addSubTask(task);
	}

	Iterator<List<Record>> getBatchsIterator(QueryBasedReindexingBatchProcessModificationImpact modificationImpact) {
		LogicalSearchCondition condition = modificationImpact.getLogicalSearchCondition();
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		//Iterator<Record> iterator = searchServices.optimizedRecordsIterator(query, 10000);
		Iterator<Record> iterator = searchServices.recordsIterator(query, 10000);
		return new BatchBuilderIterator<>(iterator, 1000);
	}

	SchemaUtils newSchemaUtils() {
		return new SchemaUtils();
	}
}
