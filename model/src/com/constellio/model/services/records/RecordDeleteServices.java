package com.constellio.model.services.records;

import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.Factory;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionValidationEvent;
import com.constellio.model.extensions.events.records.RecordPhysicalDeletionEvent;
import com.constellio.model.extensions.events.records.RecordPhysicalDeletionValidationEvent;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentModificationsBuilder;
import com.constellio.model.services.extensions.ModelLayerExtensions;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordDeleteServicesRuntimeException.RecordDeleteServicesRuntimeException_CannotDeleteRecordWithUserFromOtherCollection;
import com.constellio.model.services.records.RecordDeleteServicesRuntimeException.RecordDeleteServicesRuntimeException_CannotTotallyDeleteSchemaType;
import com.constellio.model.services.records.RecordDeleteServicesRuntimeException.RecordDeleteServicesRuntimeException_RecordServicesErrorDuringOperation;
import com.constellio.model.services.records.RecordDeleteServicesRuntimeException.RecordServicesRuntimeException_CannotPhysicallyDeleteRecord_CannotSetNullOnRecords;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotPhysicallyDeleteRecord;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotRestoreRecord;
import com.constellio.model.services.records.preparation.RecordsToReindexResolver;
import com.constellio.model.services.records.utils.SortOrder;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.constellio.model.entities.schemas.Schemas.ALL_REFERENCES;
import static com.constellio.model.entities.security.global.AuthorizationDeleteRequest.authorizationDeleteRequest;
import static com.constellio.model.services.records.RecordLogicalDeleteOptions.LogicallyDeleteTaxonomyRecordsBehavior.LOGICALLY_DELETE_THEM;
import static com.constellio.model.services.records.RecordLogicalDeleteOptions.LogicallyDeleteTaxonomyRecordsBehavior.LOGICALLY_DELETE_THEM_ONLY_IF_PRINCIPAL_TAXONOMY;
import static com.constellio.model.services.records.RecordPhysicalDeleteOptions.PhysicalDeleteTaxonomyRecordsBehavior.PHYSICALLY_DELETE_THEM;
import static com.constellio.model.services.records.RecordPhysicalDeleteOptions.PhysicalDeleteTaxonomyRecordsBehavior.PHYSICALLY_DELETE_THEM_ONLY_IF_PRINCIPAL_TAXONOMY;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.lang.Boolean.TRUE;

public class RecordDeleteServices {

	private final Logger LOGGER = LoggerFactory.getLogger(RecordDeleteServices.class);

	private final RecordDao recordDao;

	private final SearchServices searchServices;

	private final RecordServices recordServices;

	private final AuthorizationsServices authorizationsServices;

	private final TaxonomiesManager taxonomiesManager;

	private final MetadataSchemasManager metadataSchemasManager;

	private final ContentManager contentManager;

	private final ModelLayerExtensions extensions;

	private final ModelLayerFactory modelLayerFactory;

	public RecordDeleteServices(ModelLayerFactory modelLayerFactory) {
		this(modelLayerFactory.getDataLayerFactory().newRecordDao(), modelLayerFactory);
	}

	public RecordDeleteServices(RecordDao recordDao, ModelLayerFactory modelLayerFactory) {
		this.recordDao = recordDao;
		this.searchServices = modelLayerFactory.newSearchServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		this.taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.contentManager = modelLayerFactory.getContentManager();
		this.extensions = modelLayerFactory.getExtensions();
		this.modelLayerFactory = modelLayerFactory;
	}

	public boolean isRestorable(Record record, User user) {
		ensureSameCollection(user, record);
		List<Record> recordsHierarchy = loadRecordsHierarchyOf(record);
		String parentId = record.getParentId();
		boolean parentActiveOrNull;
		if (parentId != null) {
			Record parent = recordServices.getDocumentById(parentId);
			parentActiveOrNull = TRUE != parent.get(Schemas.LOGICALLY_DELETED_STATUS);
		} else {
			parentActiveOrNull = true;
		}

		String typeCode = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(record.getCollection()).getSchemaType(typeCode);
		return parentActiveOrNull && (!schemaType.hasSecurity() || user == User.GOD ||
									  authorizationsServices.hasRestaurationPermissionOnHierarchy(user, record, recordsHierarchy));
	}

	public void restore(Record record, User user) {
		if (!isRestorable(record, user)) {
			throw new RecordServicesRuntimeException_CannotRestoreRecord(record.getId());
		}

		Transaction transaction = new Transaction();
		transaction.getRecordUpdateOptions().setValidationsEnabled(false);
		transaction.getRecordUpdateOptions().setSkipMaskedMetadataValidations(true);
		transaction.getRecordUpdateOptions().setSkippingRequiredValuesValidation(true);
		transaction.getRecordUpdateOptions().setSkipUSRMetadatasRequirementValidations(true);
		transaction.getRecordUpdateOptions().setSkippingReferenceToLogicallyDeletedValidation(true);

		for (Record hierarchyRecord : getAllRecordsInHierarchy(record, SortOrder.ASCENDING)) {
			if (Boolean.FALSE.equals(hierarchyRecord.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS))) {
				continue;
			}
			hierarchyRecord.set(Schemas.LOGICALLY_DELETED_STATUS, false);
			hierarchyRecord.set(Schemas.LOGICALLY_DELETED_ON, null);
			if (!transaction.getRecordIds().contains(hierarchyRecord.getId())) {
				transaction.add(hierarchyRecord);
			}
		}
		if (!transaction.getRecordIds().contains(record.getId())) {
			record.set(Schemas.LOGICALLY_DELETED_STATUS, false);
			record.set(Schemas.LOGICALLY_DELETED_ON, null);
			transaction.add(record);
		}
		try {
			recordServices.executeInBatch(transaction);
		} catch (RecordServicesException e) {
			throw new RecordDeleteServicesRuntimeException_RecordServicesErrorDuringOperation("restore", e);
		}

	}

	public boolean isLogicallyThenPhysicallyDeletable(Record record, User user) {
		return isLogicallyThenPhysicallyDeletable(record, user, new RecordPhysicalDeleteOptions());
	}

	public boolean isLogicallyThenPhysicallyDeletable(Record record, User user, RecordPhysicalDeleteOptions options) {
		return isPhysicallyDeletableNoMatterTheStatus(record, user, options);
	}

	public boolean isPhysicallyDeletable(Record record, User user) {
		return isPhysicallyDeletable(record, user, new RecordPhysicalDeleteOptions());
	}

	public boolean isPhysicallyDeletable(Record record, User user, RecordPhysicalDeleteOptions options) {
		ensureSameCollection(user, record);
		List<Record> recordsHierarchy = loadRecordsHierarchyOf(record);
		String typeCode = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(record.getCollection()).getSchemaType(typeCode);

		boolean correctStatus = TRUE == record.get(Schemas.LOGICALLY_DELETED_STATUS);
		boolean noActiveRecords = containsNoActiveRecords(record);
		boolean hasPermissions =
				!schemaType.hasSecurity() || authorizationsServices
						.hasRestaurationPermissionOnHierarchy(user, record, recordsHierarchy);
		if (!correctStatus) {
			LOGGER.info("Not physically deletable : Record is not logically deleted");
			return false;

		} else if (!noActiveRecords) {
			LOGGER.info("Not physically deletable : There is active records in the hierarchy");
			return false;

		} else if (!hasPermissions) {
			LOGGER.info("Not physically deletable : No sufficient permissions on hierarchy");
			return false;

		} else {
			return isPhysicallyDeletableNoMatterTheStatus(record, user, options);
		}

	}

	private boolean isPhysicallyDeletableNoMatterTheStatus(final Record record, User user,
														   RecordPhysicalDeleteOptions options) {
		ensureSameCollection(user, record);
		final List<Record> recordsHierarchy = loadRecordsHierarchyOf(record);
		String typeCode = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(record.getCollection()).getSchemaType(typeCode);

		boolean hasPermissions =
				!schemaType.hasSecurity() || authorizationsServices
						.hasDeletePermissionOnHierarchyNoMatterTheStatus(user, record, recordsHierarchy);
		boolean referencesInConfigs = isReferencedByConfigs(record);
		boolean referencesUnhandled =
				isReferencedByOtherRecords(record, recordsHierarchy) && !options.isSetMostReferencesToNull();

		if (referencesInConfigs) {
			LOGGER.info("Not physically deletable : Record is used in configs");
		}

		if (!hasPermissions) {
			LOGGER.info("Not physically deletable : No sufficient permissions on hierarchy");
		}

		if (referencesUnhandled) {
			LOGGER.info("Not physically deletable : A record in the hierarchy is referenced outside of the hierarchy");
		}

		boolean physicallyDeletable = hasPermissions && !referencesUnhandled && !referencesInConfigs;

		Factory<Boolean> referenced = new Factory<Boolean>() {
			@Override
			public Boolean get() {
				return !getRecordsInHierarchyWithDependency(record, recordsHierarchy).isEmpty();
			}
		};

		if (physicallyDeletable) {
			RecordLogicalDeletionValidationEvent event = new RecordLogicalDeletionValidationEvent(record, user, referenced, true);
			physicallyDeletable = extensions.forCollectionOf(record).isLogicallyDeletable(event);
		}

		if (physicallyDeletable) {
			RecordPhysicalDeletionValidationEvent event = new RecordPhysicalDeletionValidationEvent(record, user);
			physicallyDeletable = extensions.forCollectionOf(record).isPhysicallyDeletable(event);
		}

		return physicallyDeletable;
	}

	public void physicallyDeleteNoMatterTheStatus(Record record, User user, RecordPhysicalDeleteOptions options) {
		if (TRUE.equals(record.get(Schemas.LOGICALLY_DELETED_STATUS))) {
			physicallyDelete(record, user, options);

		} else {
			logicallyDelete(record, user);
			recordServices.refreshUsingCache(record);
			try {
				physicallyDelete(record, user, options);
			} catch (RecordServicesRuntimeException e) {
				recordServices.refreshUsingCache(record);
				restore(record, user);
				throw e;
			}
		}
	}

	public void physicallyDelete(Record record, User user) {
		physicallyDelete(record, user, new RecordPhysicalDeleteOptions());
	}

	public void physicallyDelete(final Record record, User user, RecordPhysicalDeleteOptions options) {
		final Set<String> recordsWithUnremovableReferences = new HashSet<>();
		final Set<String> recordsIdsTitlesWithUnremovableReferences = new HashSet<>();
		if (!isPhysicallyDeletable(record, user, options)) {
			throw new RecordServicesRuntimeException_CannotPhysicallyDeleteRecord(record.getId());
		}

		List<Record> records = getAllRecordsInHierarchyForPhysicalDeletion(record, options);

		SchemasRecordsServices schemas = new SchemasRecordsServices(record.getCollection(), modelLayerFactory);
		if (schemas.getTypes().hasType(SolrAuthorizationDetails.SCHEMA_TYPE)) {
			for (Record recordInHierarchy : records) {
				for (SolrAuthorizationDetails details : schemas.searchSolrAuthorizationDetailss(
						where(schemas.authorizationDetails.target()).isEqualTo(recordInHierarchy.getId()))) {

					authorizationsServices.execute(authorizationDeleteRequest(details));
				}
			}
			for (SolrAuthorizationDetails details : schemas.searchSolrAuthorizationDetailss(
					where(schemas.authorizationDetails.target()).isEqualTo(record.getId()))) {
				authorizationsServices.execute(authorizationDeleteRequest(details));
			}
		}

		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(record.getCollection());

		if (options.isSetMostReferencesToNull()) {

			//Collections.sort(records, sortByLevelFromLeafToRoot());

			for (final Record recordInHierarchy : records) {
				String type = new SchemaUtils().getSchemaTypeCode(recordInHierarchy.getSchemaCode());
				final List<Metadata> metadatas = types.getAllMetadatas().onlyReferencesToType(type).onlyNonParentReferences()
						.onlyManuals();

				if (!metadatas.isEmpty()) {
					try {
						new ActionExecutorInBatch(searchServices, "Remove references to '" + recordInHierarchy.getId() + "'",
								1000) {

							@Override
							public void doActionOnBatch(List<Record> recordsWithRef)
									throws Exception {

								Transaction transaction = new Transaction();

								for (Record recordWithRef : recordsWithRef) {
									String recordWithRefType = new SchemaUtils().getSchemaTypeCode(recordWithRef.getSchemaCode());
									for (Metadata metadata : metadatas) {
										String metadataType = new SchemaUtils().getSchemaTypeCode(metadata);
										if (recordWithRefType.equals(metadataType)) {
											if (metadata.isMultivalue()) {
												List<String> values = new ArrayList<>(recordWithRef.<String>getList(metadata));
												int sizeBefore = values.size();
												values.removeAll(Collections.singletonList(recordInHierarchy.getId()));
												if (sizeBefore != values.size()) {
													recordWithRef.set(metadata, values);
												}
											} else {
												String value = recordWithRef.get(metadata);
												if (recordInHierarchy.getId().equals(value)) {
													recordWithRef.set(metadata, null);
												}
											}
										}
									}

									try {
										recordServices.validateRecordInTransaction(recordWithRef, transaction);
										transaction.add(recordWithRef);
									} catch (ValidationException e) {
										e.printStackTrace();
										recordsWithUnremovableReferences.add(recordWithRef.getId());
										recordsIdsTitlesWithUnremovableReferences.add(recordWithRef.getTitle());
									}

								}

								recordServices.execute(transaction);
							}
						}.execute(fromAllSchemasIn(record.getCollection()).whereAny(metadatas).isEqualTo(recordInHierarchy));
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

		if (recordsWithUnremovableReferences.isEmpty()) {

			Set<String> ids = new HashSet<>();
			for (Record aRecord : records) {
				ids.addAll(new RecordsToReindexResolver(types).findRecordsToReindexFromRecord(aRecord, true));
			}

			deleteContents(records);
			List<RecordDTO> recordsDTO = newRecordUtils().toRecordDTOList(records);

			try {
				recordDao.execute(
						new TransactionDTO(RecordsFlushing.NOW).withDeletedRecords(recordsDTO));

			} catch (OptimisticLocking optimisticLocking) {
				throw new RecordServicesRuntimeException_CannotPhysicallyDeleteRecord(record.getId(), optimisticLocking);
			}

			for (RecordDTO recordDTO : recordsDTO) {
				ids.remove(recordDTO.getId());
				recordServices.getRecordsCaches().getCache(record.getCollection()).invalidate(recordDTO.getId());
			}

			Transaction transaction = new Transaction();
			transaction.add(recordServices.getDocumentById(record.getCollection()));
			transaction.addAllRecordsToReindex(ids);

			try {
				recordServices.executeInBatch(transaction);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}

			for (Record hierarchyRecord : records) {
				RecordPhysicalDeletionEvent event = new RecordPhysicalDeletionEvent(hierarchyRecord);
				extensions.forCollectionOf(record).callRecordPhysicallyDeleted(event);
			}
		} else {
			throw new RecordServicesRuntimeException_CannotPhysicallyDeleteRecord_CannotSetNullOnRecords(record.getId(),
					recordsWithUnremovableReferences, recordsIdsTitlesWithUnremovableReferences);
		}
	}

	private List<Record> getAllRecordsInHierarchyForLogicalDeletion(Record record, RecordLogicalDeleteOptions options) {
		Taxonomy taxonomy = taxonomiesManager.getTaxonomyOf(record);
		Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(record.getCollection());

		boolean includeRecords = false;
		if (taxonomy != null) {
			if (options.behaviorForRecordsAttachedToTaxonomy == LOGICALLY_DELETE_THEM) {
				includeRecords = true;
			} else if (taxonomy.hasSameCode(principalTaxonomy)
					   && options.behaviorForRecordsAttachedToTaxonomy == LOGICALLY_DELETE_THEM_ONLY_IF_PRINCIPAL_TAXONOMY) {
				includeRecords = true;
			}
		}

		if (taxonomy != null && !includeRecords) {
			return getAllTaxonomyRecordsInHierarchy(record, taxonomy);
		} else {
			return getAllRecordsInHierarchy(record, SortOrder.DESCENDING);
		}
	}

	private List<Record> getAllRecordsInHierarchyForPhysicalDeletion(Record record,
																	 RecordPhysicalDeleteOptions options) {

		Taxonomy taxonomy = taxonomiesManager.getTaxonomyOf(record);
		Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(record.getCollection());

		boolean includeRecords = false;
		if (taxonomy != null) {
			if (options.behaviorForRecordsAttachedToTaxonomy == PHYSICALLY_DELETE_THEM) {
				includeRecords = true;
			} else if (taxonomy.hasSameCode(principalTaxonomy)
					   && options.behaviorForRecordsAttachedToTaxonomy == PHYSICALLY_DELETE_THEM_ONLY_IF_PRINCIPAL_TAXONOMY) {
				includeRecords = true;
			}
		}

		if (taxonomy != null && !includeRecords) {
			return getAllTaxonomyRecordsInHierarchy(record, taxonomy);
		} else {
			return getAllRecordsInHierarchy(record, SortOrder.DESCENDING);
		}
	}

	void deleteContents(List<Record> records) {
		String collection = records.get(0).getCollection();
		for (String potentiallyDeletableHash : newContentModificationsBuilder(collection).buildForDeletedRecords(records)) {
			contentManager.silentlyMarkForDeletionIfNotReferenced(potentiallyDeletableHash);
		}
	}

	ContentModificationsBuilder newContentModificationsBuilder(String collection) {
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
		return new ContentModificationsBuilder(types);
	}

	public ValidationErrors isLogicallyDeletable(final Record record, User user) {
		ValidationErrors validationErrors = new ValidationErrors();
		ensureSameCollection(user, record);

		String typeCode = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(record.getCollection()).getSchemaType(typeCode);

		if (user != null && schemaType.hasSecurity() && !user.hasDeleteAccess().on(record)) {
			//The record has security, before going further with validations, we check if the user can delete it
			validationErrors.add(RecordDeleteServices.class, "The record has security or user does not have delete access");
			return validationErrors;
		}

		final List<Record> recordsHierarchy = loadRecordsHierarchyOf(record);

		boolean logicallyDeletable =
				!schemaType.hasSecurity() || authorizationsServices
						.hasDeletePermissionOnHierarchy(user, record, recordsHierarchy);

		if (isReferencedByConfigs(record)) {
			logicallyDeletable = false;
			validationErrors.add(RecordDeleteServices.class, "The record is referenced by configs");
		}

		if (logicallyDeletable) {
			Factory<Boolean> referenced = new Factory<Boolean>() {
				@Override
				public Boolean get() {
					return !getRecordsInHierarchyWithDependency(record, recordsHierarchy).isEmpty();
				}
			};
			RecordLogicalDeletionValidationEvent event = new RecordLogicalDeletionValidationEvent(record, user, referenced,
					false);
			logicallyDeletable = extensions.forCollectionOf(record).isLogicallyDeletable(event);
			if (!logicallyDeletable) {
				validationErrors.add(RecordDeleteServices.class, "The record is not logically deletable");
			}
		}

		return validationErrors;
	}

	private void removedDefaultValues(String collection, List<Record> records) {
		List<String> defaultValuesIds = metadataSchemasManager.getSchemaTypes(collection).getReferenceDefaultValues();
		final List<String> defaultValuesIdsToRemove = new ArrayList<>();
		for (Record record : records) {
			if (defaultValuesIds.contains(record.getId())) {
				defaultValuesIdsToRemove.add(record.getId());
			}
		}

		if (!defaultValuesIdsToRemove.isEmpty()) {
			metadataSchemasManager.modify(collection, new MetadataSchemaTypesAlteration() {
				@Override
				public void alter(MetadataSchemaTypesBuilder types) {
					for (MetadataSchemaTypeBuilder typeBuilder : types.getTypes()) {
						for (MetadataSchemaBuilder schemaBuilder : typeBuilder.getAllSchemas()) {
							for (MetadataBuilder metadataBuilder : schemaBuilder.getMetadatas()) {
								Object defaultValue = metadataBuilder.getDefaultValue();
								if (metadataBuilder.getType() == MetadataValueType.REFERENCE && defaultValue != null) {
									if (defaultValue instanceof String && defaultValuesIdsToRemove.contains(defaultValue)) {
										metadataBuilder.setDefaultValue(null);
									} else if (defaultValue instanceof List) {
										List<String> withoutRemovedDefaultValues = null;
										for (Object item : (List) defaultValue) {
											if (defaultValuesIdsToRemove.contains(item)) {
												if (withoutRemovedDefaultValues == null) {
													withoutRemovedDefaultValues = new ArrayList<String>((List) defaultValue);
												}
												withoutRemovedDefaultValues.remove(item);
											}
										}
										if (withoutRemovedDefaultValues != null) {
											if (withoutRemovedDefaultValues.isEmpty()) {
												withoutRemovedDefaultValues = null;
											}
											metadataBuilder.setDefaultValue(withoutRemovedDefaultValues);
										}
									}
								}
							}
						}
					}
				}
			});
		}
	}

	public void logicallyDelete(Record record, User user) {
		logicallyDelete(record, user, new RecordLogicalDeleteOptions());
	}

	public void logicallyDelete(Record record, User user, RecordLogicalDeleteOptions options) {
		if (!options.isSkipValidations() && !isLogicallyDeletable(record, user).isEmpty()) {
			throw new RecordServicesRuntimeException_CannotLogicallyDeleteRecord(record.getId());
		}

		Transaction transaction = new Transaction().setSkippingRequiredValuesValidation(true);

		if (!options.isCheckForValidationErrorEnable()) {
			transaction.getRecordUpdateOptions().setValidationsEnabled(false);
		}

		List<Record> hierarchyRecords = new ArrayList<>(getAllRecordsInHierarchyForLogicalDeletion(record, options));
		if (!new RecordUtils().toIdList(hierarchyRecords).contains(record.getId())) {
			hierarchyRecords.add(record);
		}
		removedDefaultValues(record.getCollection(), hierarchyRecords);
		LocalDateTime now = TimeProvider.getLocalDateTime();
		for (Record hierarchyRecord : hierarchyRecords) {
			hierarchyRecord.set(Schemas.LOGICALLY_DELETED_STATUS, true);
			hierarchyRecord.set(Schemas.LOGICALLY_DELETED_ON, now);
			transaction.add(hierarchyRecord);
		}
		transaction.setRecordFlushing(options.getRecordsFlushing());
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		transaction.setUser(user);
		try {
			transaction.setRecordFlushing(options.getRecordsFlushing());
			recordServices.executeInBatch(transaction);
		} catch (RecordServicesException e) {
			throw new RecordDeleteServicesRuntimeException_RecordServicesErrorDuringOperation("logicallyDelete", e);
		}
	}

	boolean isPrincipalConceptLogicallyDeletableExcludingContent(Record principalConcept, User user) {
		List<Record> recordsHierarchy = loadRecordsHierarchyOf(principalConcept);
		return authorizationsServices
				.hasDeletePermissionOnPrincipalConceptHierarchy(user, principalConcept, false, recordsHierarchy,
						metadataSchemasManager);
	}

	List<Record> getAllRecordsInHierarchy(Record record) {
		return getAllRecordsInHierarchy(record, SortOrder.NONE);
	}

	List<Record> getAllRecordsInHierarchy(Record record, SortOrder sortOrder) {

		if (record.getList(Schemas.PATH).isEmpty()) {
			return Arrays.asList(record);

		} else {
			LogicalSearchQuery query = new LogicalSearchQuery();
			List<String> paths = record.getList(Schemas.PATH);
			query.setCondition(fromAllSchemasIn(record.getCollection()).where(Schemas.PATH).isStartingWithText(paths.get(0)));
			if (sortOrder == SortOrder.ASCENDING) {
				query.sortAsc(Schemas.PRINCIPAL_PATH);
			} else if (sortOrder == SortOrder.DESCENDING) {
				query.sortDesc(Schemas.PRINCIPAL_PATH);
			}
			return searchServices.search(query);
		}
	}

	List<Record> getAllTaxonomyRecordsInHierarchy(Record record, Taxonomy taxonomy) {
		if (record.getList(Schemas.PATH).isEmpty()) {
			return Arrays.asList(record);

		} else {
			LogicalSearchQuery query = new LogicalSearchQuery();
			List<String> paths = record.getList(Schemas.PATH);
			List<MetadataSchemaType> taxonomySchemaTypes = metadataSchemasManager.getSchemaTypes(record.getCollection())
					.getSchemaTypesWithCode(taxonomy.getSchemaTypes());
			query.setCondition(from(taxonomySchemaTypes).where(Schemas.PATH).isStartingWithText(paths.get(0)));
			return searchServices.search(query);
		}
	}

	List<Record> getAllPrincipalConceptsRecordsInHierarchy(Record principalConcept, Taxonomy principalTaxonomy) {
		List<Record> records = new ArrayList<>();
		for (String schemaTypeCode : principalTaxonomy.getSchemaTypes()) {
			MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(principalConcept.getCollection())
					.getSchemaType(schemaTypeCode);

			List<String> paths = principalConcept.getList(Schemas.PATH);
			LogicalSearchQuery query = new LogicalSearchQuery();
			query.setCondition(from(schemaType).where(Schemas.PATH).isStartingWithText(paths.get(0)));
			records.addAll(searchServices.search(query));
		}
		return records;
	}

	boolean containsNoActiveRecords(Record record) {

		Taxonomy taxonomy = taxonomiesManager.getTaxonomyOf(record);
		Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(record.getCollection());

		LogicalSearchQuery query = new LogicalSearchQuery().filteredByStatus(StatusFilter.ACTIVES);
		if (taxonomy != null && !taxonomy.hasSameCode(principalTaxonomy)) {
			List<MetadataSchemaType> taxonomySchemaTypes = metadataSchemasManager.getSchemaTypes(record.getCollection())
					.getSchemaTypesWithCode(taxonomy.getSchemaTypes());
			query.setCondition(from(taxonomySchemaTypes).where(Schemas.PATH).isContainingText(record.getId()));
		} else {
			query.setCondition(
					fromAllSchemasIn(record.getCollection()).where(Schemas.PATH).isContainingText("/" + record.getId() + "/"));
		}
		return !searchServices.hasResults(query);
	}

	public RecordUtils newRecordUtils() {
		return new RecordUtils();
	}

	List<Record> getVisibleRecordsWithReferenceToRecordInHierarchy(Record record, User user,
																   List<Record> recordHierarchy) {
		//1 - Find all hierarchy records (including the given record) that are referenced (using the counter index)
		List<Record> returnedRecords = new ArrayList<>();
		List<String> recordsWithReferences = new ArrayList<>(getRecordsInHierarchyWithDependency(record, recordHierarchy));

		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(record.getCollection());
		for (String id : recordsWithReferences) {
			Record referencingRecord = recordServices.getDocumentById(id);

			boolean securedRecord = types.getSchemaType(referencingRecord.getTypeCode()).hasSecurity();
			if (user == null || !securedRecord || user.hasReadAccess().on(referencingRecord)) {
				returnedRecords.add(referencingRecord);
			}
		}

		return Collections.unmodifiableList(returnedRecords);
	}

	List<Record> getRecordsInTypeWithReferenceTo(User user, List<String> recordsWithReferences, MetadataSchemaType type,
												 List<Metadata> references) {

		LogicalSearchQuery query = new LogicalSearchQuery().filteredWithUser(user);
		query.setCondition(from(type).whereAny(references).isIn(recordsWithReferences));
		return searchServices.search(query);
	}

	private Set<String> getRecordsInHierarchyWithDependency(Record record, List<Record> recordsHierarchy) {

		List<String> recordsHierarchyIds = new RecordUtils().toIdList(recordsHierarchy);
		Set<String> references = new HashSet<>();

		if (!recordsHierarchyIds.contains(record.getId())) {
			recordsHierarchy.add(record);
		}

		for (Record aHierarchyRecord : recordsHierarchy) {

			boolean mayBeReferencedOutsideHierarchy = false;
			for (MetadataSchemaType schemaType : metadataSchemasManager.getSchemaTypes(record.getCollection()).getSchemaTypes()) {

				List<Metadata> referencesMetadata = schemaType.getDefaultSchema().getMetadatas()
						.onlyReferencesToType(aHierarchyRecord.getTypeCode()).onlyNonParentReferences();
				mayBeReferencedOutsideHierarchy |= !referencesMetadata.isEmpty();
			}

			if (mayBeReferencedOutsideHierarchy) {
				LogicalSearchCondition condition = fromAllSchemasIn(record.getCollection())
						.where(ALL_REFERENCES).isEqualTo(aHierarchyRecord.getId());

				if (taxonomiesManager.getTaxonomyFor(record.getCollection(), record.getTypeCode()) == null) {
					condition = condition.andWhere(Schemas.PATH_PARTS).isNotEqual(aHierarchyRecord.getId());
				}

				Iterator<String> iterator = searchServices.recordsIdsIterator(new LogicalSearchQuery(condition));

				while (iterator.hasNext()) {
					String referencingRecord = iterator.next();
					if (!recordsHierarchyIds.contains(referencingRecord)) {
						references.add(referencingRecord);
					}
				}
			}
		}

		return references;
	}

	public boolean isReferencedByConfigs(Record record) {
		for (MetadataSchemaType schemaType : metadataSchemasManager.getSchemaTypes(record.getCollection()).getSchemaTypes()) {
			for (MetadataSchema schema : schemaType.getAllSchemas()) {
				for (Metadata metadata : schema.getMetadatas()) {
					if (metadata.getType() == MetadataValueType.REFERENCE && metadata.getDefaultValue() != null) {
						if (metadata.getDefaultValue() instanceof List) {
							if (((List) metadata.getDefaultValue()).contains(record.getId())) {
								return true;
							}
						} else if (metadata.getDefaultValue().equals(record.getId())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public boolean isReferencedByOtherRecords(Record record, List<Record> recordsHierarchy) {
		return !getRecordsInHierarchyWithDependency(record, recordsHierarchy).isEmpty();

	}

	public List<Record> loadRecordsHierarchyOf(Record record) {
		Taxonomy taxonomy = taxonomiesManager.getTaxonomyOf(record);
		List<Record> recordsHierarchy;
		if (taxonomy == null) {
			recordsHierarchy = new ArrayList<>(getAllRecordsInHierarchy(record));
		} else {
			recordsHierarchy = new ArrayList<>(getAllTaxonomyRecordsInHierarchy(record, taxonomy));
		}

		return recordsHierarchy;
	}

	private void ensureSameCollection(User user, Record record) {

		if (user != User.GOD && !user.getCollection().equals(record.getCollection())) {
			throw new RecordDeleteServicesRuntimeException_CannotDeleteRecordWithUserFromOtherCollection(record.getCollection(),
					user.getCollection());
		}
	}

	public void totallyDeleteSchemaTypeRecords(MetadataSchemaType type) {
		if (type.isInTransactionLog()) {
			throw new RecordDeleteServicesRuntimeException_CannotTotallyDeleteSchemaType(type.getCode());
		}

		totallyDeleteSchemaTypeRecordsSkippingValidation_WARNING_CANNOT_BE_REVERTED(type);
	}

	public void totallyDeleteSchemaTypeRecordsSkippingValidation_WARNING_CANNOT_BE_REVERTED(MetadataSchemaType type) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "schema_s:" + type.getCode() + "_*");

		try {
			recordDao.execute(new TransactionDTO(RecordsFlushing.NOW).withDeletedByQueries(params));
		} catch (OptimisticLocking optimisticLocking) {
			throw new RuntimeException(optimisticLocking);
		}

	}

	public boolean isLogicallyDeletableAndIsSkipValidation(Record record, User user,
														   RecordLogicalDeleteOptions options) {
		return !options.isSkipValidations() && !isLogicallyDeletable(record, user).isEmpty();
	}
}
