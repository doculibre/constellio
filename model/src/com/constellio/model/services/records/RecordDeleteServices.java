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
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Authorization;
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
import com.constellio.model.services.records.preparation.RecordsLinksResolver;
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
import com.constellio.model.services.search.VisibilityStatusFilter;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import static java.util.Arrays.asList;

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

	public ValidationErrors validateRestorable(Record record, User user) {
		ValidationErrors validationErrors = new ValidationErrors();
		ensureSameCollection(user, record);
		List<Record> recordsHierarchy = loadRecordsHierarchyOf(record);
		String parentId = record.getParentId();
		boolean parentActiveOrNull;
		if (parentId != null) {
			Record parent = recordServices.getDocumentById(parentId);
			parentActiveOrNull = !TRUE.equals(parent.get(Schemas.LOGICALLY_DELETED_STATUS));
		} else {
			parentActiveOrNull = true;
		}

		if (!parentActiveOrNull) {
			validationErrors.add(RecordDeleteServices.class, "parentNotActiveNorNull");
			return validationErrors;
		}
		String typeCode = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(record.getCollection()).getSchemaType(typeCode);
		boolean hasSecurity = schemaType.hasSecurity();
		boolean hasRestaurationPermission = authorizationsServices.hasRestaurationPermissionOnHierarchy(user, record, recordsHierarchy);
		if (hasSecurity && user != User.GOD && !hasRestaurationPermission) {
			if (hasSecurity) {
				validationErrors.add(RecordDeleteServices.class, "hasSecurity");
				return validationErrors;
			}
			if (!hasRestaurationPermission) {
				validationErrors.add(RecordDeleteServices.class, "doesNotHaveRestaurationPermission");
			}
		}

		return validationErrors;
	}

	public void restore(Record record, User user) {
		ValidationErrors validationErrors = validateRestorable(record, user);
		if (!validationErrors.isEmpty()) {
			throw new RecordServicesRuntimeException_CannotRestoreRecord(validationErrors.getValidationErrors().get(0).getCode());
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

	public ValidationErrors validateLogicallyThenPhysicallyDeletable(Record record, User user) {
		return validateLogicallyThenPhysicallyDeletable(record, user, new RecordPhysicalDeleteOptions());
	}

	public ValidationErrors validateLogicallyThenPhysicallyDeletable(Record record, User user,
																	 RecordPhysicalDeleteOptions options) {
		return validatePhysicallyDeletableNoMatterTheStatus(record, user, options);
	}

	public ValidationErrors validatePhysicallyDeletable(Record record, User user) {
		return validatePhysicallyDeletable(record, user, new RecordPhysicalDeleteOptions());
	}

	public ValidationErrors validatePhysicallyDeletable(Record record, User user, RecordPhysicalDeleteOptions options) {
		ensureSameCollection(user, record);
		List<Record> recordsHierarchy = loadRecordsHierarchyOf(record);
		String typeCode = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(record.getCollection()).getSchemaType(typeCode);

		boolean correctStatus = TRUE.equals(record.get(Schemas.LOGICALLY_DELETED_STATUS));
		List<Record> activeRecords = getActiveRecords(record);
		boolean hasPermissions =
				!schemaType.hasSecurity() || authorizationsServices
						.hasRestaurationPermissionOnHierarchy(user, record, recordsHierarchy);
		ValidationErrors validationErrors = new ValidationErrors();
		if (!correctStatus) {
			validationErrors.add(RecordDeleteServices.class, "recordIsNotLogicallyDeleted");
			return validationErrors;

		} else if (!activeRecords.isEmpty()) {
			validationErrors.add(RecordDeleteServices.class, "activeRecordInHierarchy", toParameter("records", activeRecords));
			return validationErrors;

		} else if (!hasPermissions) {
			validationErrors.add(RecordDeleteServices.class, "noSufficientPermissionsOnHierarchy");
			return validationErrors;

		} else {
			return validatePhysicallyDeletableNoMatterTheStatus(record, user, options);
		}

	}

	private ValidationErrors validatePhysicallyDeletableNoMatterTheStatus(final Record record, User user,
																		  RecordPhysicalDeleteOptions options) {
		ensureSameCollection(user, record);
		final List<Record> recordsHierarchy = loadRecordsHierarchyOf(record);
		String typeCode = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(record.getCollection()).getSchemaType(typeCode);

		boolean hasPermissions =
				!schemaType.hasSecurity() || authorizationsServices
						.hasDeletePermissionOnHierarchyNoMatterTheStatus(user, record, recordsHierarchy);
		Metadata referenceMetadata = referenceMetadata(record);
		boolean referencesInConfigs = referenceMetadata != null;

		final Set<String> recordsInHierarchyWithDependency = getRecordsInHierarchyWithDependency(record, recordsHierarchy);
		boolean referencesUnhandled = !recordsInHierarchyWithDependency.isEmpty() && !options.isSetMostReferencesToNull();

		ValidationErrors validationErrors = new ValidationErrors();
		if (referencesInConfigs) {
			validationErrors.add(RecordDeleteServices.class, "recordReferencedByConfigs", toParameter("metadata", referenceMetadata.getLocalCode()));
		}

		if (!hasPermissions) {
			validationErrors.add(RecordDeleteServices.class, "noSufficientPermissionsOnHierarchy");
		}

		if (referencesUnhandled) {
			validationErrors.add(RecordDeleteServices.class, "recordInHierarchyReferencedOutsideOfHierarchy", toParameter("records", getTenFirstRecords(recordsInHierarchyWithDependency, record.getCollection())));
		}

		boolean physicallyDeletable = hasPermissions && !referencesUnhandled && !referencesInConfigs;

		Factory<Boolean> referenced = new Factory<Boolean>() {
			@Override
			public Boolean get() {
				return !recordsInHierarchyWithDependency.isEmpty();
			}
		};

		if (physicallyDeletable) {
			RecordLogicalDeletionValidationEvent event = new RecordLogicalDeletionValidationEvent(record, user, referenced, true);
			validationErrors = extensions.forCollectionOf(record).validateLogicallyDeletable(event);
			physicallyDeletable = validationErrors.isEmpty();
		}

		if (physicallyDeletable) {
			RecordPhysicalDeletionValidationEvent event = new RecordPhysicalDeletionValidationEvent(record, user);
			validationErrors = extensions.forCollectionOf(record).validatePhysicallyDeletable(event);
		}

		return validationErrors;
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

	public void physicallyDelete(final Record record, User user, final RecordPhysicalDeleteOptions options) {
		final Set<String> recordsWithUnremovableReferences = new HashSet<>();
		final Set<String> recordsIdsTitlesWithUnremovableReferences = new HashSet<>();

		ValidationErrors errors = validatePhysicallyDeletable(record, user, options);
		if (!errors.getValidationErrors().isEmpty()) {
			throw new RecordServicesRuntimeException_CannotPhysicallyDeleteRecord(errors.toMultilineErrorsSummaryString());
		}

		List<Record> records = couldHaveHierarchyRecords(record) ?
							   getAllRecordsInHierarchyForPhysicalDeletion(record, options) :
							   new ArrayList<>(Collections.singletonList(record));

		SchemasRecordsServices schemas = new SchemasRecordsServices(record.getCollection(), modelLayerFactory);
		if (schemas.getTypes().hasType(Authorization.SCHEMA_TYPE)) {
			for (Record recordInHierarchy : records) {
				for (Authorization details : schemas.searchSolrAuthorizationDetailss(
						where(schemas.authorizationDetails.target()).isEqualTo(recordInHierarchy.getId()))) {

					authorizationsServices.execute(authorizationDeleteRequest(details));
				}
			}
			for (Authorization details : schemas.searchSolrAuthorizationDetailss(
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

								Transaction transaction = options.isSkipValidations() ?
														  new Transaction(RecordUpdateOptions.validationExceptionSafeOptions()) :
														  new Transaction();

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
										if (!options.isSkipValidations()) {
											recordServices.validateRecordInTransaction(recordWithRef, transaction);
										}
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
				ids.addAll(new RecordsLinksResolver(types).findRecordsToReindexFromRecord(aRecord, true));
			}

			if (!records.isEmpty()) {
				deleteContents(records);
			}
			List<RecordDTO> recordsDTO = newRecordUtils().toRecordDTOList(records);

			try {
				recordDao.execute(
						new TransactionDTO(RecordsFlushing.NOW).withDeletedRecords(recordsDTO));

			} catch (OptimisticLocking optimisticLocking) {
				throw new RecordServicesRuntimeException_CannotPhysicallyDeleteRecord(record.getId(), optimisticLocking);
			}

			for (RecordDTO recordDTO : recordsDTO) {
				ids.remove(recordDTO.getId());
				recordServices.getRecordsCaches().getCache(record.getCollection()).removeFromAllCaches(recordDTO.getId());
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

	public ValidationErrors validateLogicallyDeletable(final Record record, User user) {
		ValidationErrors validationErrors = new ValidationErrors();
		ensureSameCollection(user, record);

		String typeCode = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(record.getCollection()).getSchemaType(typeCode);

		if (user != null && schemaType.hasSecurity() && !user.hasDeleteAccess().on(record)) {
			//The record has security, before going further with validations, we check if the user can delete it
			validationErrors.add(RecordDeleteServices.class, "recordHasSecurityOrUserDoesNotHaveDeleteAccess");
			return validationErrors;
		}

		final List<Record> recordsHierarchy = loadRecordsHierarchyOf(record);

		boolean logicallyDeletable =
				!schemaType.hasSecurity() || authorizationsServices
						.hasDeletePermissionOnHierarchy(user, record, recordsHierarchy);
		if (!logicallyDeletable) {
			validationErrors.add(RecordDeleteServices.class, "userDoesNotHavePermissionOnHierarchy", toParameter("record", recordsHierarchy));
		}
		Metadata referenceMetadata = referenceMetadata(record);
		if (referenceMetadata != null) {
			logicallyDeletable = false;
			validationErrors.add(RecordDeleteServices.class, "recordReferencedByConfigs", toParameter("metadata", referenceMetadata.getLocalCode()));
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
			validationErrors = extensions.forCollectionOf(record).validateLogicallyDeletable(event);
		}

		return validationErrors;
	}

	private Map<String, Object> toParameter(String key, Object value) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(key, value);
		return parameters;
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
		if (!options.isSkipValidations() && !validateLogicallyDeletable(record, user).isEmpty()) {
			throw new RecordServicesRuntimeException_CannotLogicallyDeleteRecord(validateLogicallyDeletable(record, user).getValidationErrors().get(0).getCode());
		}

		Transaction transaction = new Transaction().setSkippingRequiredValuesValidation(true);

		if (!options.isCheckForValidationErrorEnable()) {
			transaction.getRecordUpdateOptions().setValidationsEnabled(false);
		}

		List<Record> hierarchyRecords = couldHaveHierarchyRecords(record) ?
										new ArrayList<>(getAllRecordsInHierarchyForLogicalDeletion(record, options)) :
										new ArrayList<>();
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

	private boolean couldHaveHierarchyRecords(Record record) {
		MetadataSchemaTypes allSchemaTypes = metadataSchemasManager.getSchemaTypes(record.getCollection());
		for (MetadataSchemaType metadataSchemaType : allSchemaTypes.getSchemaTypes()) {
			for (Metadata metadata : metadataSchemaType.getAllParentReferences()) {
				if (record.isOfSchemaType(metadata.getReferencedSchemaType())) {
					return true;
				}
			}
		}
		return false;
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
			return asList(record);

		} else {
			LogicalSearchQuery query = new LogicalSearchQuery();
			query.filteredByVisibilityStatus(VisibilityStatusFilter.ALL);
			List<String> paths = record.getList(Schemas.PATH);
			query.setCondition(fromAllSchemasIn(record.getCollection())
					.where(Schemas.PATH).isStartingWithText(paths.get(0) + "/")
					.orWhere(Schemas.PATH).isEqualTo(paths.get(0)));
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
			return asList(record);

		} else {
			LogicalSearchQuery query = new LogicalSearchQuery();
			query.filteredByVisibilityStatus(VisibilityStatusFilter.ALL);
			List<String> paths = record.getList(Schemas.PATH);
			List<MetadataSchemaType> taxonomySchemaTypes = metadataSchemasManager.getSchemaTypes(record.getCollection())
					.getSchemaTypesWithCode(taxonomy.getSchemaTypes());
			query.setCondition(from(taxonomySchemaTypes)
					.where(Schemas.PATH).isStartingWithText(paths.get(0) + "/")
					.orWhere(Schemas.PATH).isEqualTo(paths.get(0)));
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
			query.setCondition(from(schemaType)
					.where(Schemas.PATH).isStartingWithText(paths.get(0) + "/")
					.orWhere(Schemas.PATH).isEqualTo(paths.get(0)));
			records.addAll(searchServices.search(query));
		}
		return records;
	}

	List<Record> getActiveRecords(Record record) {
		Taxonomy taxonomy = taxonomiesManager.getTaxonomyOf(record);
		if (taxonomy == null && !couldHaveHierarchyRecords(record)) {
			return Collections.emptyList();
		}

		Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(record.getCollection());

		LogicalSearchQuery query = new LogicalSearchQuery().filteredByStatus(StatusFilter.ACTIVES);
		query = filteredQueryForErrorMessages(query);
		if (taxonomy != null && !taxonomy.hasSameCode(principalTaxonomy)) {
			List<MetadataSchemaType> taxonomySchemaTypes = metadataSchemasManager.getSchemaTypes(record.getCollection())
					.getSchemaTypesWithCode(taxonomy.getSchemaTypes());
			query.setCondition(from(taxonomySchemaTypes).where(Schemas.PATH).isContainingText("/" + record.getId() + "/"));
		} else {
			query.setCondition(
					fromAllSchemasIn(record.getCollection()).where(Schemas.PATH).isContainingText("/" + record.getId() + "/"));
		}
		return searchServices.search(query);
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

	private List<Record> getTenFirstRecords(Set<String> recordIds, String collection) {
		List<String> records = new ArrayList<>();
		int recordSize = 1;
		Iterator<String> recordsIdsIterator = recordIds.iterator();
		while (recordSize <= 10 && recordsIdsIterator.hasNext()) {
			records.add(recordsIdsIterator.next());
			recordSize++;
		}
		return recordServices.getRecordsById(collection, records);
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

	public Metadata referenceMetadata(Record record) {
		for (MetadataSchemaType schemaType : metadataSchemasManager.getSchemaTypes(record.getCollection()).getSchemaTypes()) {
			for (MetadataSchema schema : schemaType.getAllSchemas()) {
				for (Metadata metadata : schema.getMetadatas()) {
					if (metadata.getType() == MetadataValueType.REFERENCE && metadata.getDefaultValue() != null) {
						if (metadata.getDefaultValue() instanceof List) {
							if (((List) metadata.getDefaultValue()).contains(record.getId())) {
								return metadata;
							}
						} else if (metadata.getDefaultValue().equals(record.getId())) {
							return metadata;
						}
					}
				}
			}
		}
		return null;
	}

	public boolean isReferencedByOtherRecords(Record record, List<Record> recordsHierarchy) {
		return !getRecordsInHierarchyWithDependency(record, recordsHierarchy).isEmpty();

	}

	public List<Record> loadRecordsHierarchyOf(Record record) {
		Taxonomy taxonomy = taxonomiesManager.getTaxonomyOf(record);
		List<Record> recordsHierarchy;
		if (taxonomy == null) {
			recordsHierarchy = couldHaveHierarchyRecords(record) ?
							   new ArrayList<>(getAllRecordsInHierarchy(record)) :
							   new ArrayList<>(Collections.singletonList(record));
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
		modelLayerFactory.getRecordsCaches().getCache(type.getCollection()).invalidateVolatileReloadPermanent(asList(type.getCode()));
	}

	public void totallyDeleteSchemaTypeRecordsSkippingValidation_WARNING_CANNOT_BE_REVERTED(MetadataSchemaType type) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "schema_s:" + type.getCode() + "_*");

		try {
			recordDao.execute(new TransactionDTO(RecordsFlushing.NOW).withDeletedByQueries(params));
		} catch (OptimisticLocking optimisticLocking) {
			throw new RuntimeException(optimisticLocking);
		}

		recordServices.getRecordsCaches().getCache(type.getCollection())
				.invalidateVolatileReloadPermanent(asList((type.getCode())));

	}

	public boolean isLogicallyDeletableAndIsSkipValidation(Record record, User user,
														   RecordLogicalDeleteOptions options) {
		return !options.isSkipValidations() && !validateLogicallyDeletable(record, user).isEmpty();
	}

	private LogicalSearchQuery filteredQueryForErrorMessages(LogicalSearchQuery query) {
		return query.setNumberOfRows(10).setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(Schemas.TITLE, Schemas.LOGICALLY_DELETED_STATUS));
	}
}
