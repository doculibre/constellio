package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.SystemCheckExtension;
import com.constellio.app.api.extensions.params.CollectionSystemCheckParams;
import com.constellio.app.api.extensions.params.TryRepairAutomaticValueParams;
import com.constellio.app.api.extensions.params.ValidateRecordsCheckParams;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.modules.rm.wrappers.utils.DecomListUtil;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.search.SearchServices;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQuery.query;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;

public class RMSystemCheckExtension extends SystemCheckExtension {

	private static final String DEPOSIT_DATE_BEFORE_TRANSFER_DATE = "depositDateBeforeTransferDate";
	private static final String DESTRUCTION_DATE_BEFORE_TRANSFER_DATE = "destructionDateBeforeTransferDate";
	private static final String ERROR_MESSAGE_USR_NOT_PRESENT_IN_SCHEMA_NAME = "messageUSRNotPresentInSchemaName";
	private static final String ERROR_TYPE_CANNOT_BE_CHANGE_FOR_TYPE_TYPE = "typeCannotBeChangeForTypeType";
	private static Logger LOGGER = LoggerFactory.getLogger(RMSystemCheckExtension.class);

	private static final List<String> ALLOWED_SCHEMAS_NOT_STARTING_WITH_USR = asList(Email.SCHEMA);

	String collection;

	AppLayerFactory appLayerFactory;
	ContentDao contentDao;
	ContentManager contentManager;
	SearchServices searchServices;
	RecordServices recordServices;
	IOServices ioServices;

	public static final String EMAIL_CHECKOUTED = "rm.recordValidation.emailCheckoutedList";
	public static final String METRIC_EMAIL_CHECKOUTED = "rm.recordValidation.emailCheckouted";
	public static final String METRIC_LOGICALLY_DELETED_ADM_UNITS = "rm.admUnits.logicallyDeleted";
	public static final String METRIC_LOGICALLY_DELETED_CATEGORIES = "rm.categories.logicallyDeleted";
	public static final String METRIC_SUB_FOLDER_WITH_NULL_FIELD_NOT_NULL = "rm.recordValidation.subFolderWithNullFieldsNotNulls";
	public static final String METRIC_TYPE_DO_NOT_CORRESPOND_TO_TYPE_TYPE = "rm.recordValidation.typeDoNotCorrespondToTypeType";

	public static final String DELETED_ADM_UNITS = "rm.admUnit.deleted";
	public static final String RESTORED_ADM_UNITS = "rm.admUnit.restored";
	public static final String DELETED_CATEGORIES = "rm.category.deleted";
	public static final String RESTORED_CATEGORIES = "rm.category.restored";

	RMSchemasRecordsServices rm;

	RMConfigs configs;

	public RMSystemCheckExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		this.contentDao = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getContentsDao();
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();
		this.contentManager = appLayerFactory.getModelLayerFactory().getContentManager();
		this.configs = new RMConfigs(appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager());
	}

	@Override
	public boolean tryRepairAutomaticValue(TryRepairAutomaticValueParams params) {
		if (params.isMetadata(DecommissioningList.SCHEMA_TYPE, "folders")) {
			DecommissioningList list = rm.wrapDecommissioningList(params.getRecord());
			DecomListUtil.removeFolderDetailsInDecomList(collection, appLayerFactory, list, params.getValuesToRemove());

			return true;
		} else if (params.isMetadata(DecommissioningList.SCHEMA_TYPE, DecommissioningList.CONTAINERS)) {
			DecommissioningList list = rm.wrapDecommissioningList(params.getRecord());
			List<DecomListFolderDetail> folderDetails = list.getFolderDetails();
			for (String containerToRemove : params.getValuesToRemove()) {
				list.removeContainerDetail(containerToRemove);

				if (folderDetails != null) {
					for (DecomListFolderDetail folder : folderDetails) {
						if (containerToRemove.equals(folder.getContainerRecordId())) {
							folder.setContainerRecordId(null);
						}
					}
				}
			}

			return true;
		}
		return false;
	}

	@Override
	public boolean validateRecord(ValidateRecordsCheckParams validateRecordsCheckParams) {

		Record record = validateRecordsCheckParams.getRecord();
		boolean isRepair = validateRecordsCheckParams.isRepair();
		boolean isToBeSaved = false;

		if (record.getSchemaCode().equals(Email.SCHEMA)) // Vérifier qu'il est checkouter.
		{

			Email email = rm.wrapEmail(record);

			if (email.getContent() != null && email.getContent().getCurrentCheckedOutVersion() != null) {
				validateRecordsCheckParams.getResultsBuilder().incrementMetric(METRIC_EMAIL_CHECKOUTED);
				validateRecordsCheckParams.getResultsBuilder().addListItem(EMAIL_CHECKOUTED, record.getId());
				if (validateRecordsCheckParams.isRepair()) {
					if (email.getContent() != null) {
						email.getContent().checkIn();

						isToBeSaved = true;
					}
				}
			}
		} else if (record.getTypeCode().equals(Folder.SCHEMA_TYPE)) {
			Folder folder = rm.wrapFolder(record);
			boolean incrementMetric = false;
			FolderType folderType = null;
			if (folder.getType() != null) {
				folderType = rm.getFolderType(folder.getType());
			}
			if (folderType != null && folderType.getLinkedSchema() != null && !record.getSchemaCode()
					.equals(folderType.getLinkedSchema())) {
				boolean isSavechangeSchema;
				validateRecordsCheckParams.getResultsBuilder().incrementMetric(METRIC_TYPE_DO_NOT_CORRESPOND_TO_TYPE_TYPE);
				if (isRepair) {
					isSavechangeSchema = record.changeSchema(rm.getTypes().getSchema(record.getSchemaCode()),
							rm.getTypes().getSchema(folderType.getLinkedSchema()));
					if (!isSavechangeSchema) {
						isToBeSaved = true;
					} else {
						Map<String, Object> parameter = new HashMap<>();
						parameter.put("recordId", record.getId());
						validateRecordsCheckParams.getResultsBuilder()
								.addNewValidationError(RMSystemCheckExtension.class, ERROR_TYPE_CANNOT_BE_CHANGE_FOR_TYPE_TYPE,
										parameter);
					}
				}
			}

			if (folder.getParentFolder() != null) {
				if (folder.getMainCopyRuleIdEntered() != null) {
					incrementMetric = true;
					if (isRepair) {
						folder.setMainCopyRuleEntered(null);
						isToBeSaved = true;
					}
				}

				if (folder.getUniformSubdivisionEntered() != null) {
					incrementMetric = true;
					if (isRepair) {
						folder.setUniformSubdivisionEntered((UniformSubdivision) null);
						isToBeSaved = true;
					}
				}

				if (folder.getAdministrativeUnitEntered() != null) {
					incrementMetric = true;
					if (isRepair) {
						folder.setAdministrativeUnitEntered((AdministrativeUnit) null);
						isToBeSaved = true;
					}
				}

				if (folder.getCategoryEntered() != null) {
					incrementMetric = true;
					if (isRepair) {
						folder.setCategoryEntered((Category) null);
						isToBeSaved = true;
					}
				}

				if (folder.getRetentionRuleEntered() != null) {
					incrementMetric = true;
					if (isRepair) {
						folder.setRetentionRuleEntered((RetentionRule) null);
						isToBeSaved = true;
					}
				}

				if (folder.getCopyStatusEntered() != null) {
					incrementMetric = true;
					if (isRepair) {
						folder.setCopyStatusEntered(null);
						isToBeSaved = true;
					}
				}

				if (incrementMetric) {
					validateRecordsCheckParams.getResultsBuilder().incrementMetric(METRIC_SUB_FOLDER_WITH_NULL_FIELD_NOT_NULL);
				}
			}
		} else if (record.getTypeCode().equals(Document.SCHEMA_TYPE)) {
			Document document = rm.wrapDocument(record);

			DocumentType documentType = null;
			if (document.getType() != null) {
				documentType = rm.getDocumentType(document.getType());
			}
			if (documentType != null && documentType.getLinkedSchema() != null && !record.getSchemaCode()
					.equals(documentType.getLinkedSchema())) {
				boolean isSavechangeSchema;
				validateRecordsCheckParams.getResultsBuilder().incrementMetric(METRIC_TYPE_DO_NOT_CORRESPOND_TO_TYPE_TYPE);
				if (isRepair) {
					isSavechangeSchema = record.changeSchema(rm.getTypes().getSchema(record.getSchemaCode()),
							rm.getTypes().getSchema(documentType.getLinkedSchema()));
					if (!isSavechangeSchema) {
						isToBeSaved = true;
					} else {
						Map<String, Object> parameter = new HashMap<>();
						parameter.put("recordId", record.getId());
						validateRecordsCheckParams.getResultsBuilder()
								.addNewValidationError(RMSystemCheckExtension.class, ERROR_TYPE_CANNOT_BE_CHANGE_FOR_TYPE_TYPE,
										parameter);
					}
				}
			}
		}

		return isToBeSaved;
	}

	@Override
	public void checkCollection(CollectionSystemCheckParams params) {
		boolean markedForReindexing = false;
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		for (AdministrativeUnit unit : rm.searchAdministrativeUnits(where(Schemas.LOGICALLY_DELETED_STATUS).isTrue())) {
			String label = unit.getCode() + " - " + unit.getTitle();
			params.getResultsBuilder().incrementMetric(METRIC_LOGICALLY_DELETED_ADM_UNITS);
			params.getResultsBuilder().markLogicallyDeletedRecordAsError(unit);
			if (params.isRepair()) {
				params.getResultsBuilder().markAsRepaired(unit.getId());
				try {
					recordServices.refreshUsingCache(unit);
					if (!unit.getWrappedRecord().isDisconnected()) {
						recordServices
								.add(unit.<Boolean, RecordWrapper>set(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode(), false));

						if (recordServices.validateLogicallyThenPhysicallyDeletable(unit.getWrappedRecord(), User.GOD).isEmpty()) {
							recordServices.logicallyDelete(unit.getWrappedRecord(), User.GOD);
							recordServices.physicallyDelete(unit.getWrappedRecord(), User.GOD);
							params.getResultsBuilder().addListItem(DELETED_ADM_UNITS, label);

						} else {
							params.getResultsBuilder().addListItem(RESTORED_ADM_UNITS, label);
						}
					} else {
						params.getResultsBuilder().addListItem(DELETED_ADM_UNITS, label);
					}
				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				} catch (NoSuchRecordWithId e) {
					//OK
				}
				markedForReindexing = true;
			}
		}

		MetadataSchemaTypes metadataSchemaTypes = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(collection);
		for (MetadataSchemaType metadataSchemaType : metadataSchemaTypes.getSchemaTypes()) {
			if (metadataSchemaType.getCode().toLowerCase().equals("document") || metadataSchemaType.getCode().toLowerCase()
					.equals("folder")) {
				for (MetadataSchema metadataSchema : metadataSchemaType.getAllSchemas()) {
					if (!metadataSchema.getLocalCode().startsWith("USR") && !metadataSchema.getLocalCode().equals("default") &&
						!ALLOWED_SCHEMAS_NOT_STARTING_WITH_USR.contains(metadataSchema.getCode())) {
						Map<String, Object> parameter = new HashMap<>();
						parameter.put("schemaCode", metadataSchema.getLocalCode());
						parameter.put("metadataSchemaType", metadataSchemaType.getCode());
						params.getResultsBuilder()
								.addNewValidationError(RMSystemCheckExtension.class, ERROR_MESSAGE_USR_NOT_PRESENT_IN_SCHEMA_NAME,
										parameter);
					}
				}
			}
		}

		List<Category> categories = rm.searchCategorys(where(Schemas.LOGICALLY_DELETED_STATUS).isTrue());

		for (Category category : categories) {
			String label = category.getCode() + " - " + category.getTitle();
			params.getResultsBuilder().incrementMetric(METRIC_LOGICALLY_DELETED_CATEGORIES);
			params.getResultsBuilder().markLogicallyDeletedRecordAsError(category);
			if (params.isRepair()) {
				params.getResultsBuilder().markAsRepaired(category.getId());
				try {
					recordServices.refreshUsingCache(category);
					if (!category.getWrappedRecord().isDisconnected()) {
						recordServices.add(category.<Boolean, RecordWrapper>set(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode(),
								false));
						if (recordServices.validateLogicallyThenPhysicallyDeletable(category.getWrappedRecord(), User.GOD).isEmpty()) {
							recordServices.logicallyDelete(category.getWrappedRecord(), User.GOD);
							recordServices.physicallyDelete(category.getWrappedRecord(), User.GOD);
							params.getResultsBuilder().addListItem(DELETED_CATEGORIES, label);
						} else {
							params.getResultsBuilder().addListItem(RESTORED_CATEGORIES, label);
						}
					} else {
						params.getResultsBuilder().addListItem(DELETED_CATEGORIES, label);
					}

				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				} catch (NoSuchRecordWithId e) {
					//OK
				}
				markedForReindexing = true;
			}
		}

		if (configs.allowModificationOfArchivisticStatusAndExpectedDates().isAlwaysEnabledOrDuringImportOnly()) {
			Iterator<Record> foldersIterator = searchServices.recordsIterator(query(from(rm.folder.schemaType())
					.where(rm.folder.manualExpectedDepositDate()).isNotNull()
					.orWhere(rm.folder.manualExpectedDesctructionDate()).isNotNull()));

			while (foldersIterator.hasNext()) {
				Folder folder = rm.wrapFolder(foldersIterator.next());

				LocalDate manualExpectedDeposit = folder.getManualExpectedDepositDate();
				LocalDate manualExpectedDestruction = folder.getManualExpectedDestructionDate();
				LocalDate actualTransfer = folder.getActualTransferDate();
				LocalDate manualExpectedTransfer = folder.getExpectedTransferDate();

				boolean fixDeposit = false;
				boolean fixDestruction = false;

				Map<String, Object> errorParams = new HashMap<>();
				errorParams.put("idTitle", folder.getId() + "-" + folder.getTitle());

				if (manualExpectedDeposit != null && actualTransfer != null && manualExpectedDeposit.isBefore(actualTransfer)) {
					params.getResultsBuilder().addNewValidationError(RMSystemCheckExtension.class,
							DEPOSIT_DATE_BEFORE_TRANSFER_DATE, errorParams);
					fixDeposit = params.isRepair();
				}

				if (manualExpectedDeposit != null && manualExpectedTransfer != null
					&& manualExpectedDeposit.isBefore(manualExpectedTransfer)) {
					params.getResultsBuilder().addNewValidationError(RMSystemCheckExtension.class,
							DEPOSIT_DATE_BEFORE_TRANSFER_DATE, errorParams);
					fixDeposit = params.isRepair();
				}
				if (manualExpectedDestruction != null && actualTransfer != null
					&& manualExpectedDestruction.isBefore(actualTransfer)) {
					params.getResultsBuilder().addNewValidationError(RMSystemCheckExtension.class,
							DESTRUCTION_DATE_BEFORE_TRANSFER_DATE, errorParams);
					fixDestruction = params.isRepair();
				}

				if (manualExpectedDestruction != null && manualExpectedTransfer != null
					&& manualExpectedDestruction.isBefore(manualExpectedTransfer)) {
					params.getResultsBuilder().addNewValidationError(RMSystemCheckExtension.class,
							DESTRUCTION_DATE_BEFORE_TRANSFER_DATE, errorParams);
					fixDestruction = params.isRepair();
				}

				if (fixDeposit || fixDestruction) {

					if (fixDeposit) {
						folder.setManualExpectedDepositDate(null);
					}
					if (fixDestruction) {
						folder.setManualExpectedDestructionDate(null);
					}
					try {
						recordServices.update(folder);
					} catch (RecordServicesException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

		if (markedForReindexing) {
			appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
		}

	}

}
