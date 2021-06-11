package com.constellio.app.services.records;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.MediumTypeDate;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetailFactory;
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidation;
import com.constellio.app.modules.rm.wrappers.structures.RetentionRuleDocumentType;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.data.dao.services.sequence.SequencesManager;
import com.constellio.data.utils.SolrDataUtils;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.RecordAuthorization;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.services.contents.ContentFactory;
import com.constellio.model.services.records.GetRecordOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.cache.PersistedIdsServices;
import com.constellio.model.services.records.cache.PersistedSortValuesServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.constellio.app.modules.rm.wrappers.Document.CONTENT;
import static com.constellio.data.dao.dto.records.OptimisticLockingResolution.EXCEPTION;
import static com.constellio.model.entities.records.RecordUpdateOptions.validationExceptionSafeOptions;
import static com.constellio.model.entities.schemas.entries.DataEntryType.MANUAL;
import static com.constellio.model.services.records.GetRecordOptions.SILENT_IF_DOES_NOT_EXIST;
import static com.constellio.model.services.search.VisibilityStatusFilter.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.QueryExecutionMethod.USE_SOLR;
import static org.apache.ignite.internal.util.lang.GridFunc.asList;

public class FixRecordIdsExecutor {

	private static final Logger LOGGER = LoggerFactory.getLogger(FixRecordIdsExecutor.class);

	AppLayerFactory appLayerFactory;
	RecordServices recordServices;
	SearchServices searchServices;
	MetadataSchemasManager schemasManager;
	SequencesManager sequencesManager;
	UniqueIdGenerator uniqueIdGenerator;
	SolrClient solrClient;

	boolean allowsIdTransformationOfFolderAndDocuments;
	Set<RecordId> goodIds;
	Map<RecordId, MetadataSchemaType> badNumericIds;
	Map<RecordId, MetadataSchemaType> badStringIds;
	Map<RecordId, RecordId> mapping;

	private int currentSequenceId;

	List<String> STRUCTURES_FACTORIES_CONTAINING_FOLDERS = new ArrayList<>();
	List<String> STRUCTURES_FACTORIES_CONTAINING_DOCUMENTS = new ArrayList<>();

	public FixRecordIdsExecutor(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		this.schemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		this.sequencesManager = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getSequencesManager();
		this.uniqueIdGenerator = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getUniqueIdGenerator();
		this.solrClient = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getRecordsVaultServer().getNestedSolrServer();
		STRUCTURES_FACTORIES_CONTAINING_FOLDERS.add(DecomListFolderDetailFactory.class.getName());
		STRUCTURES_FACTORIES_CONTAINING_FOLDERS.add(com.constellio.app.modules.rm.services.borrowingServices.BorrowingFactory.class.getName());
		STRUCTURES_FACTORIES_CONTAINING_DOCUMENTS.add(RetentionRuleDocumentType.class.getName());
	}

	public synchronized Map<RecordId, Record> fixAllIds(boolean allowsIdTransformationOfFolderAndDocuments,
														boolean run) {
		this.allowsIdTransformationOfFolderAndDocuments = allowsIdTransformationOfFolderAndDocuments;
		prepareIdsToMigrate();

		if (run) {
			migrate();

			return extractOldRecordIdToRecordMap(mapping);
		} else {
			return Collections.emptyMap();
		}

	}

	private Map<RecordId, Record> extractOldRecordIdToRecordMap(final Map<RecordId, RecordId> fromToIdMap) {
		final List<String> newRecordIds = mapping.values().stream().map(RecordId::stringValue).collect(Collectors.toList());
		final List<Record> recordsFromNewIds = recordServices.get(newRecordIds, GetRecordOptions.RETURNING_SUMMARY);
		final Map<String, Record> newRecordIdMap = recordsFromNewIds.stream().collect(Collectors.toMap(Record::getId, record -> record));

		final Map<RecordId, Record> oldRecordIdToRecordWithNewIdMap = fromToIdMap.entrySet().stream().collect(Collectors.toMap(
				Entry::getKey,
				entry -> newRecordIdMap.get(entry.getValue().stringValue())
		));

		final BiConsumer<RecordId, RecordId> integrityValidation = (oldId, newId) -> {
			Record record = oldRecordIdToRecordWithNewIdMap.get(oldId);

			if (record == null && newId != null) {
				throw new RuntimeException("Mapping of old Id " + oldId.stringValue() + " to Record failed. It is null when it should has been mapped to record with id " + newId.stringValue());
			} else if (record != null && newId == null) {
				throw new RuntimeException("Mapping of old Id " + oldId.stringValue() + " to Record failed. It was mapped to record with id " + record.getId() + " when it should has been null");
			} else if (record != null && !record.getId().equals(newId.stringValue())) {
				throw new RuntimeException("Mapping of old Id " + oldId.stringValue() + " to Record failed. It was mapped to record with id " + record.getId() + " when it should has been " + newId.stringValue());
			}
		};

		fromToIdMap.forEach(integrityValidation);

		return oldRecordIdToRecordWithNewIdMap;
	}

	private void migrate() {
		//Cache and system will be quite shaken after that
		int totalRecordsToMigrate = badNumericIds.size() + badStringIds.size();
		int numberOfRecordsMigrated = 0;
		Toggle.USE_CACHE_FOR_QUERY_EXECUTION.disable();
		Toggle.CACHES_ENABLED.disable();
		try {
			Thread.sleep(10_000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
		appLayerFactory.getSystemGlobalConfigsManager().markLocalCachesAsRequiringRebuild();
		appLayerFactory.getModelLayerFactory().getRecordsCaches().disableVolatileCache();

		for (Map.Entry<RecordId, MetadataSchemaType> entry : badNumericIds.entrySet()) {
			LOGGER.info("Progression of id migration: " + ++numberOfRecordsMigrated + " / " + totalRecordsToMigrate);
			Record record = recordServices.get(entry.getKey());
			migrate(record, entry.getValue(), getNewId(record.getRecordId()));
		}

		for (Map.Entry<RecordId, MetadataSchemaType> entry : badStringIds.entrySet()) {
			LOGGER.info("Progression of id migration: " + ++numberOfRecordsMigrated + " / " + totalRecordsToMigrate);
			Record record = recordServices.get(entry.getKey());
			migrate(record, entry.getValue(), getNewId(record.getRecordId()));
		}

		LOGGER.info("Started deletion of bad ids");
		for (Map.Entry<RecordId, MetadataSchemaType> entry : badNumericIds.entrySet()) {
			try {
				solrClient.deleteById(entry.getKey().stringValue());
			} catch (SolrServerException | IOException e) {
				throw new RuntimeException(e);
			}
			try {
				solrClient.commit(true, true, true);
			} catch (SolrServerException | IOException e) {
				throw new RuntimeException(e);
			}
		}

		for (Map.Entry<RecordId, MetadataSchemaType> entry : badStringIds.entrySet()) {
			try {
				solrClient.deleteById(entry.getKey().stringValue());
			} catch (SolrServerException | IOException e) {
				throw new RuntimeException(e);
			}
			try {
				solrClient.commit(true, true, true);
			} catch (SolrServerException | IOException e) {
				throw new RuntimeException(e);
			}
		}

		new PersistedIdsServices(appLayerFactory.getModelLayerFactory()).clear();
		new PersistedSortValuesServices(appLayerFactory.getModelLayerFactory()).clear();
	}

	private void copySolrDocument(RecordId fromId, RecordId toId) {
		try {
			SolrDocument solrDocument = solrClient.getById(fromId.stringValue());
			SolrInputDocument inputDocument = SolrDataUtils.toInputDocuments(asList(solrDocument)).get(0);
			inputDocument.setField("id", toId.stringValue());
			solrClient.add(inputDocument);
			solrClient.commit(true, true, true);

		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void migrate(Record record, MetadataSchemaType schemaType, RecordId toId) {

		RecordId fromId = record.getRecordId();

		LOGGER.info("Migrating record '" + fromId.stringValue() + "' to id '" + toId.stringValue() + "'...");

		//Create an identical solr document with the new id
		copySolrDocument(fromId, toId);

		try {
			fixSequences(fromId.stringValue(), toId.stringValue());

			Transaction tx = new Transaction();
			tx.setOptions(validationExceptionSafeOptions());
			tx.getRecordUpdateOptions().setMarkIdsForReindexing(false);
			tx.getRecordUpdateOptions().setUpdateCalculatedMetadatas(false);
			tx.getRecordUpdateOptions().setOverwriteModificationDateAndUser(false);
			//Increase tx size to 100k
			tx.setOptimisticLockingResolution(EXCEPTION);
			for (ImpactHandler handler : newImpactHandler(schemaType, fromId.stringValue(), toId.stringValue())) {
				LogicalSearchQuery query = handler.getQuery();
				query.setQueryExecutionMethod(USE_SOLR);
				query.filteredByVisibilityStatus(ALL);
				List<Record> records = searchServices.search(query);

				for (Record queryRecord : records) {
					if (!queryRecord.getId().equals(fromId.stringValue())) {
						Record txRecord = tx.getRecord(queryRecord.getId());
						if (txRecord == null) {
							txRecord = queryRecord;
							tx.add(queryRecord);
						}
						handler.handle(txRecord);
					}
				}
			}

			try {
				recordServices.executeWithoutImpactHandling(tx);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}

			LOGGER.info("Migrating record '" + fromId.stringValue() + "' to id '" + toId.stringValue() + "' done");
		} catch(RuntimeException e) {
			//Failed, deleting the copied document
			try {
				solrClient.deleteById(toId.stringValue());
			} catch (SolrServerException | IOException e2) {
				throw new RuntimeException(e2);
			}
			throw e;
		}

		try {
			solrClient.deleteById(fromId.stringValue());
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
		try {
			solrClient.commit(true, true, true);
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
		fixEvents(schemaType, fromId.stringValue(), toId.stringValue());

	}

	private void fixEvents(MetadataSchemaType schemaType, String fromId, String toId) {



		if (schemaType.getSchemaTypes().hasSchema("document_default")) {
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(schemaType.getCollection(), appLayerFactory.getModelLayerFactory());
			LogicalSearchQuery query = new LogicalSearchQuery(from(rm.event.schemaType()).where(rm.event.recordIdentifier()).isEqualTo(fromId));

			query.setQueryExecutionMethod(USE_SOLR);
			SearchResponseIterator<List<Record>> eventsIterator = searchServices.recordsIterator(query, 10000).inBatches();

			while (eventsIterator.hasNext()) {
				List<Event> events =rm.wrapEvents(eventsIterator.next());
				Transaction tx = new Transaction();
				events.forEach(event -> tx.add(event.setRecordId(toId)));


				try {
					recordServices.execute(tx);
				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				}

			}

		}
	}

	private void fixSequences(String fromId, String toId) {
		for(Map.Entry<String, Long> entry : sequencesManager.getSequences().entrySet()) {
			//TODO TEST!

			if (entry.getKey().contains(fromId)) {
				String newSequence = entry.getKey().replace(fromId, toId);

				if (sequencesManager.getLastSequenceValue(newSequence) != -1L) {
					throw new RuntimeException("New sequence '" + newSequence + "' is already used");
				}
				sequencesManager.set(newSequence, entry.getValue() );
			}

		}
	}

	private List<ImpactHandler> newImpactHandler(MetadataSchemaType schemaType, String fromId,
												 String toId) {

		List<ImpactHandler> handlers = new ArrayList<>();

		//Update references
		for (MetadataSchemaType aSchemaType : schemaType.getSchemaTypes().getSchemaTypes()) {
			for (Metadata metadata : aSchemaType.getAllMetadatas().only(m -> m.getDataEntry().getType() == MANUAL
																			 && m.getType() == MetadataValueType.REFERENCE
																			 && m.getReferencedSchemaTypeCode().equals(schemaType.getCode()))) {
				LogicalSearchQuery query = new LogicalSearchQuery(from(aSchemaType).where(metadata).isEqualTo(fromId));

				if (metadata.isMultivalue()) {
					handlers.add(new ImpactHandler(query) {

						@Override
						public void handle(Record record) {
							record.set(metadata, newListSwapping(record.getList(metadata), fromId, toId));

						}
					});
				} else {
					handlers.add(new ImpactHandler(query) {

						@Override
						public void handle(Record record) {
							record.set(metadata, toId);
						}
					});
				}
			}
		}

		//Update structures
		for (MetadataSchemaType aSchemaType : schemaType.getSchemaTypes().getSchemaTypes()) {
			for (Metadata metadata : aSchemaType.getAllMetadatas().only(m -> ((m.getDataEntry().getType() == MANUAL
																			   && m.getType() == MetadataValueType.STRUCTURE) || m.getLocalCode().equals(CONTENT)))) {

				boolean shouldSearchForThisMetadata;
				if (schemaType.getCode().equals("folder")) {
					shouldSearchForThisMetadata = STRUCTURES_FACTORIES_CONTAINING_FOLDERS.contains(metadata.getStructureFactory().getClass().getName());
				} else if (schemaType.getCode().equals("document")) {
					shouldSearchForThisMetadata = STRUCTURES_FACTORIES_CONTAINING_DOCUMENTS.contains(metadata.getStructureFactory().getClass().getName());
				} else {
					shouldSearchForThisMetadata = true;
				}

				if (shouldSearchForThisMetadata == false) {
					continue;
				}

				LogicalSearchQuery query = new LogicalSearchQuery(from(aSchemaType).where(metadata).isContainingText(fromId));
				if (metadata.isMultivalue()) {
					handlers.add(new ImpactHandler(query) {

						@Override
						public void handle(Record record) {
							List<ModifiableStructure> values = new ArrayList<>(record.getList(metadata));
							for (int i = 0; i < values.size(); i++) {
								final int finalI = i;
								ModifiableStructure value = values.get(i);
								updateStructure(value, (v)->values.set(finalI, v), fromId, toId);

							}
							record.set(metadata, values);

						}
					});
				} else {
					handlers.add(new ImpactHandler(query) {

						@Override
						public void handle(Record record) {
							updateStructure(record.get(metadata), (v)->record.set(metadata, v), fromId, toId);
						}
					});
				}
			}
		}

		if (schemaType.getSchemaTypes().hasSchema("document_default")) {
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(schemaType.getCollection(), appLayerFactory.getModelLayerFactory());
			handlers.add(new ImpactHandler(new LogicalSearchQuery(from(rm.authorizationDetails.schemaType())
					.where(rm.authorizationDetails.target()).isEqualTo(fromId))) {
				@Override
				void handle(Record record) {
					RecordAuthorization authorization = new RecordAuthorization(record, schemaType.getSchemaTypes());
					authorization.setTarget(toId);
				}
			});
		}




		return handlers;
	}

	private List<String> newListSwapping(List<String> list, String fromId, String toId) {
		List<String> values = new ArrayList<>(list);

		for (int i = 0; i < values.size(); i++) {
			if (fromId.equals(values.get(i))) {
				values.set(i, toId);
			}
		}
		return values;
	}

	private void updateStructure(ModifiableStructure structure, Consumer<ModifiableStructure> newValueConsumer, String fromId, String toId) {


		if (structure instanceof CopyRetentionRule) {

			CopyRetentionRule copyRetentionRule = (CopyRetentionRule) structure;
			if (copyRetentionRule.getMediumTypeIds().contains(fromId)) {
				copyRetentionRule.setMediumTypeIds(newListSwapping(copyRetentionRule.getMediumTypeIds(), fromId, toId));
			}

			if (fromId.equals(copyRetentionRule.getTypeId())) {
				copyRetentionRule.setTypeId(toId);
			}

			if (fromId.equals(copyRetentionRule.getSemiActiveYearTypeId())) {
				copyRetentionRule.setSemiActiveYearTypeId(toId);
			}

			if (fromId.equals(copyRetentionRule.getInactiveYearTypeId())) {
				copyRetentionRule.setInactiveYearTypeId(toId);
			}

		} else if (structure instanceof MediumTypeDate) {
			MediumTypeDate mediumTypeDate = (MediumTypeDate) structure;
			if (fromId.equals(mediumTypeDate.getMediumTypeId())) {
				mediumTypeDate.setMediumTypeId(toId);
			}

		} else if (structure instanceof com.constellio.app.modules.rm.services.borrowingServices.Borrowing) {
			com.constellio.app.modules.rm.services.borrowingServices.Borrowing borrowing = (com.constellio.app.modules.rm.services.borrowingServices.Borrowing) structure;
			if (fromId.equals(borrowing.getBorrowerId())) {
				borrowing.setBorrowerId(toId);
			}
			if (fromId.equals(borrowing.getReturnerId())) {
				borrowing.setReturnerId(toId);
			}

		} else if (structure instanceof DecomListContainerDetail) {
			DecomListContainerDetail detail = (DecomListContainerDetail)structure;
			if (fromId.equals(detail.getContainerRecordId())) {
				detail.setContainerRecordId(toId);
			}

		} else if (structure instanceof DecomListFolderDetail) {
			DecomListFolderDetail detail = (DecomListFolderDetail)structure;
			if (fromId.equals(detail.getFolderId())) {
				detail.setFolderId(toId);
			}
			if (fromId.equals(detail.getContainerRecordId())) {
				detail.setContainerRecordId(toId);
			}

		} else if (structure instanceof DecomListValidation) {
			DecomListValidation validation = (DecomListValidation)structure;
			if (fromId.equals(validation.getUserId())) {
				validation.setUserId(toId);
			}

		} else if (structure instanceof RetentionRuleDocumentType) {
			RetentionRuleDocumentType documentType = (RetentionRuleDocumentType)structure;
			if (fromId.equals(documentType.getDocumentTypeId())) {
				documentType.setDocumentTypeId(toId);
			}

		} else if (structure instanceof TaskFollower) {
			TaskFollower follower = (TaskFollower)structure;
			if (fromId.equals(follower.getFollowerId())) {
				follower.setFollowerId(toId);
			}

		} else if (structure instanceof Comment) {
			Comment comment = (Comment)structure;
			if (fromId.equals(comment.getUserId())) {
				comment.setUserId(toId);
			}

		} else if (structure instanceof Content) {
			Content content = (Content)structure;
			ContentFactory contentFactory = new ContentFactory();
			String strValue = contentFactory.toString(content);
			strValue = strValue.replace(":u=" + fromId + ":", ":u=" + toId + ":")
					.replace("::" + fromId + "::", "::" + toId + "::")
					.replace(":u=" + fromId + ":", ":u=" + toId + ":")
					.replace(":u=" + fromId + ":", ":u=" + toId + ":");
			ModifiableStructure newStructure = contentFactory.build(strValue);
			newValueConsumer.accept(newStructure);

		}
	}

	@AllArgsConstructor
	private abstract class ImpactHandler {

		@Getter
		LogicalSearchQuery query;

		abstract void handle(Record record);

	}

	private void prepareIdsToMigrate() {
		goodIds = new HashSet<>();
		badNumericIds = new HashMap<>();
		badStringIds = new HashMap<>();
		mapping = new HashMap<>();
		currentSequenceId = RecordId.toIntId(uniqueIdGenerator.next());

		Iterator<RecordId> idsIterator = new PersistedIdsServices(appLayerFactory.getModelLayerFactory()).getRecordIds().getIterator();

		while (idsIterator.hasNext()) {
			RecordId id = idsIterator.next();

			if (!id.stringValue().equals("the_private_key") ) {
				if (id.isInteger()) {
					goodIds.add(id);
				} else {
					Record record = null;
					try {
						record = recordServices.get(id, SILENT_IF_DOES_NOT_EXIST);
					} catch (Exception e) {
						//record does not exist or does not have a schemaType
					}
					if (record != null) {
						MetadataSchemaType schemaType = schemasManager.getSchemaTypeOf(record);

						if (shouldFixId(schemaType)) {
							//Will throw exception is operation is not possible, everything is aborted
							ensureCanChangeId(record, schemaType);

							if (isNumeric(record.getId())) {
								badNumericIds.put(record.getRecordId(), schemaType);
							} else {
								badStringIds.put(record.getRecordId(), schemaType);
							}
						}
					}
				}
			}
		}
	}

	private boolean shouldFixId(MetadataSchemaType schemaType) {
		String code = schemaType.getCode();
		return !code.equals(Event.SCHEMA_TYPE) && !code.equals(SavedSearch.SCHEMA_TYPE) && !code.equals(Collection.SCHEMA_TYPE);
	}

	private void ensureCanChangeId(Record record, MetadataSchemaType schemaType) {
		String code = schemaType.getCode();

		if (code.equals(Folder.SCHEMA_TYPE) || code.equals(Document.SCHEMA_TYPE)) {
			if (isNumeric(record.getId())) {
				int preferedIntValue = Integer.valueOf(record.getId());

				if (preferedIntValue >= currentSequenceId) {
					if (!allowsIdTransformationOfFolderAndDocuments) {
						throw new RuntimeException(code + " id '" + record.getId() + "' requires transformation, since numeric id is superior to sequence table next id. Operation aborted without changes.");
					}
				} else {

					if (goodIds.contains(RecordId.id(preferedIntValue))) {
						if (!allowsIdTransformationOfFolderAndDocuments) {
							throw new RuntimeException(code + " id '" + record.getId() + "' requires transformation, since already used. Operation aborted without changes.");
						}
					} else {
						//Priority reservation!
						mapping.put(record.getRecordId(), RecordId.id(preferedIntValue));
					}


				}


			} else {
				if (!allowsIdTransformationOfFolderAndDocuments) {
					throw new RuntimeException(code + " id '" + record.getId() + "' requires transformation, since not a number. Operation aborted without changes.");
				}
			}


		}

		if (code.equals(Document.SCHEMA_TYPE)) {
			Document document = new Document(record, schemaType.getSchemaTypes());
			if (document.isPublished()) {
				throw new RuntimeException(code + " id '" + record.getId() + "' cannot be migrated, since it is pubished");
			}
		}

	}

	private RecordId getNewId(RecordId recordId) {
		RecordId newId = mapping.get(recordId);
		if (newId == null) {
			newId = RecordId.toId(uniqueIdGenerator.next());
			LOGGER.info("Id '" + recordId + "' is mapped to '" + newId + "'");
			mapping.put(recordId, newId);
		}
		return newId;
	}


	private static boolean isNumeric(String s) {
		try {
			Integer.valueOf(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
