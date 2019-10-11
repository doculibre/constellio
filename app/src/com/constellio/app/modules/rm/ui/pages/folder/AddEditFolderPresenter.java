package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.extensions.records.params.GetDynamicFieldMetadatasParams;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.services.mediumType.MediumTypeService;
import com.constellio.app.modules.rm.ui.builders.FolderToVOBuilder;
import com.constellio.app.modules.rm.ui.components.folder.FolderForm;
import com.constellio.app.modules.rm.ui.components.folder.FolderFormImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.CustomFolderField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderActualDepositDateField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderActualDestructionDateField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderActualTransferDateField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderAdministrativeUnitField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCategoryField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderContainerField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCopyRuleField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCopyStatusEnteredField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderDisposalTypeField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderLinearSizeField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderOpeningDateField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderParentFolderField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderParentFolderFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderPreviewReturnDateField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderRetentionRuleField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderUniformSubdivisionField;
import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.modules.rm.util.RMNavigationUtils;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMUser;
import com.constellio.app.modules.rm.wrappers.RMUserFolder;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.components.fields.record.RecordOptionGroup;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserPermissionsChecker;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.modules.rm.wrappers.Folder.ADMINISTRATIVE_UNIT;
import static com.constellio.app.modules.rm.wrappers.Folder.ADMINISTRATIVE_UNIT_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.CATEGORY;
import static com.constellio.app.modules.rm.wrappers.Folder.CATEGORY_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.COPY_STATUS;
import static com.constellio.app.modules.rm.wrappers.Folder.COPY_STATUS_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.MAIN_COPY_RULE;
import static com.constellio.app.modules.rm.wrappers.Folder.MAIN_COPY_RULE_ID_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.RETENTION_RULE;
import static com.constellio.app.modules.rm.wrappers.Folder.RETENTION_RULE_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.UNIFORM_SUBDIVISION;
import static com.constellio.app.modules.rm.wrappers.Folder.UNIFORM_SUBDIVISION_ENTERED;
import static com.constellio.app.ui.i18n.i18n.$;
import static org.apache.ignite.internal.util.lang.GridFunc.asList;

public class AddEditFolderPresenter extends SingleSchemaBasePresenter<AddEditFolderView> {

	private static Logger LOGGER = LoggerFactory.getLogger(AddEditFolderPresenter.class);

	private static final String ID = "id";
	private static final String PARENT_ID = "parentId";
	private static final String DUPLICATE = "duplicate";
	private static final String STRUCTURE = "structure";
	private static final String USER_FOLDER_ID = "userFolderId";

	protected FolderToVOBuilder voBuilder = new FolderToVOBuilder();
	protected boolean addView;
	protected boolean folderHadAParent;
	protected boolean alwaysShowParentField = false;
	private boolean folderHasParent;
	protected String currentSchemaCode;
	protected FolderVO folderVO;
	protected Map<CustomFolderField<?>, Object> customContainerDependencyFields = new HashMap<>();
	protected boolean isDuplicateAction;
	protected boolean isDuplicateStructureAction;
	protected String userFolderId;

	private transient RMSchemasRecordsServices rmSchemasRecordsServices;
	private transient BorrowingServices borrowingServices;
	private Map<String, String> params;
	private RMModuleExtensions rmModuleExtensions;
	private transient MediumTypeService mediumTypeService;

	public AddEditFolderPresenter(AddEditFolderView view, RecordVO recordVO) {
		super(view, Folder.DEFAULT_SCHEMA);
		rmModuleExtensions = view.getConstellioFactories().getAppLayerFactory().getExtensions()
				.forCollection(view.getCollection()).forModule(ConstellioRMModule.ID);

		initTransientObjects();

		if (recordVO != null) {
			FolderVO folderVO;
			if (recordVO instanceof FolderVO) {
				folderVO = (FolderVO) recordVO;
			} else {
				folderVO = voBuilder.build(rmSchemasRecordsServices.getFolder(recordVO.getId()).getWrappedRecord(), VIEW_MODE.FORM, view.getSessionContext());
			}
			setFolderVO(folderVO);
		}
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
		borrowingServices = new BorrowingServices(collection, modelLayerFactory);
		mediumTypeService = new MediumTypeService(collection, appLayerFactory);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public String getFavGroup() {
		if (params != null) {
			return params.get(RMViews.FAV_GROUP_ID_KEY);
		} else {
			return null;
		}
	}

	public void forParams(String params) {
		if (params != null) {
			Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
			String id = paramsMap.get(ID);
			String parentId = paramsMap.get(PARENT_ID);
			userFolderId = paramsMap.get(USER_FOLDER_ID);
			this.params = paramsMap;

			Record record;
			if (StringUtils.isNotBlank(id)) {
				record = getRecord(id);
				addView = false;

			} else if (parentId == null) {
				record = newRecord();
				addView = true;
				if (StringUtils.isNotBlank(userFolderId)) {
					populateFromUserFolder(record);
				}
			} else {
				Folder folder = new RMSchemasRecordsServices(collection, appLayerFactory).getFolder(parentId);
				record = new DecommissioningService(collection, appLayerFactory).newSubFolderIn(folder).getWrappedRecord();
				addView = true;
			}

			isDuplicateAction = paramsMap.containsKey(DUPLICATE);
			isDuplicateStructureAction = isDuplicateAction && paramsMap.containsKey(STRUCTURE);
			if (isDuplicateStructureAction) {
				Folder folder = rmSchemas().wrapFolder(record);
				try {
					record = decommissioningService().duplicateStructure(folder, getCurrentUser(), false).getWrappedRecord();
				} catch (RecordServicesException.ValidationException e) {
					view.showErrorMessage($(e.getErrors()));
					view.navigate().to().home();
				} catch (Exception e) {
					view.showErrorMessage(e.getMessage());
					view.navigate().to().home();
				}

			} else if (isDuplicateAction) {
				Folder folder = rmSchemas().wrapFolder(record);
				record = decommissioningService().duplicate(folder, getCurrentUser(), false).getWrappedRecord();
			}
			FolderVO folderVO = voBuilder.build(record, VIEW_MODE.FORM, view.getSessionContext());
			setFolderVO(folderVO);
		}
	}

	private void setFolderVO(FolderVO folderVO) {
		this.folderVO = folderVO;
		folderHadAParent = folderVO.getParentFolder() != null;
		folderHasParent = folderHadAParent;
		currentSchemaCode = folderVO.getSchema().getCode();
		setSchemaCode(currentSchemaCode);
		view.setRecord(folderVO);
	}

	private void populateFromUserFolder(Record folderRecord) {
		User currentUser = getCurrentUser();
		Folder folder = rmSchemas().wrapFolder(folderRecord);
		RMUserFolder userFolder = rmSchemas().getUserFolder(userFolderId);
		decommissioningService().populateFolderFromUserFolder(folder, userFolder, currentUser);
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		Folder restrictedFolder = rmSchemas().wrapFolder(restrictedRecord);

		if (addView) {
			List<String> requiredPermissions = new ArrayList<>();
			requiredPermissions.add(RMPermissionsTo.CREATE_SUB_FOLDERS);
			FolderStatus status = restrictedFolder.getPermissionStatus();
			if (status != null && status.isSemiActive()) {
				requiredPermissions.add(RMPermissionsTo.CREATE_SUB_FOLDERS_IN_SEMIACTIVE_FOLDERS);
			}

			if (status != null && status.isInactive()) {
				requiredPermissions.add(RMPermissionsTo.CREATE_SUB_FOLDERS_IN_INACTIVE_FOLDERS);
			}

			return user.hasAll(requiredPermissions).on(restrictedFolder) && user.hasWriteAccess().on(restrictedFolder);
		} else {
			List<String> requiredPermissions = new ArrayList<>();
			FolderStatus status = restrictedFolder.getPermissionStatus();
			if (status != null && status.isSemiActive()) {
				requiredPermissions.add(RMPermissionsTo.MODIFY_SEMIACTIVE_FOLDERS);
				if (restrictedFolder.getBorrowed() != null && restrictedFolder.getBorrowed()) {
					requiredPermissions.add(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER);
				}
			}

			if (status != null && status.isInactive()) {
				requiredPermissions.add(RMPermissionsTo.MODIFY_INACTIVE_FOLDERS);
				if (restrictedFolder.getBorrowed() != null && restrictedFolder.getBorrowed()) {
					requiredPermissions.add(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER);
				}
			}

			return user.hasAll(requiredPermissions).on(restrictedFolder) && user.hasWriteAccess().on(restrictedFolder);
		}

	}

	@Override
	protected List<String> getRestrictedRecordIds(String params) {
		Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
		String parentId = paramsMap.get(PARENT_ID);
		List<String> ids = new ArrayList<>();
		if (addView) {
			if (parentId != null) {
				ids.add(parentId);
			}
		} else if (isDuplicateAction) {
			ids.add(paramsMap.get(ID));
		} else {
			ids.add(folderVO.getId());
		}
		return ids;
	}

	public void viewAssembled() {
		adjustCustomFields(null, true);
		adjustMediumTypesField();
	}

	public boolean isSubfolder() {
		return folderHadAParent;
	}

	public boolean isAddView() {
		return addView;
	}

	public void cancelButtonClicked() {
		if (addView) {
			String parentId = folderVO.getParentFolder();
			if (parentId != null) {
				navigateToFolderDisplay(parentId);
			} else if (userFolderId != null) {
				view.navigate().to(RMViews.class).listUserDocuments();
			} else {
				view.navigate().to().recordsManagement();
			}
		} else {
			if (!isDuplicateStructureAction) {
				navigateToFolderDisplay(params.get("id"));
			} else {
				navigateToFolderDisplay(folderVO.getId());
			}
		}
	}

	protected void navigateToFolderDisplay(String id) {
		RMNavigationUtils.navigateToDisplayFolder(id, params, appLayerFactory, view.getCollection());
	}

	public FolderVO getFolderVO() {
		return folderVO;
	}

	@Override
	protected Record toRecord(RecordVO recordVO) {
		if (addView) {
			return super.toNewRecord(recordVO);
		} else {
			return super.toRecord(recordVO);
		}
	}

	public void saveButtonClicked() {
		Folder folder = rmSchemas().wrapFolder(toRecord(getFolderVO()));
		if (!canSaveFolder(folder, getCurrentUser())) {
			view.showMessage($("AddEditDocumentView.noPermissionToSaveDocument"));
			return;
		} else if (Boolean.TRUE.equals(folder.getBorrowed())) {
			String borrowingUserId = folder.getBorrowUser();
			LocalDateTime borrowingDateTime = folder.getBorrowDate();
			LocalDate borrowingDate = borrowingDateTime != null ? borrowingDateTime.toLocalDate() : null;
			LocalDate previewReturnDate = folder.getBorrowPreviewReturnDate();
			BorrowingType borrowingType = folder.getBorrowType();
			LocalDateTime returnDateTime = folder.getBorrowReturnDate();
			LocalDate returnDate = returnDateTime != null ? returnDateTime.toLocalDate() : null;
			String borrowingErrorMessage = borrowingServices
					.validateBorrowingInfos(borrowingUserId, borrowingDate, previewReturnDate, borrowingType, returnDate);
			if (borrowingErrorMessage != null) {
				view.showErrorMessage($(borrowingErrorMessage));
				return;
			}
		}
		if (!canContainerContainFolder(folder)) {
			view.showErrorMessage($("AddEditFolderViewImpl.notEnoughSpaceInContainer"));
			return;
		}
		User currentUser = getCurrentUser();
		LocalDateTime time = TimeProvider.getLocalDateTime();
		if (isAddView() || isDuplicateAction) {
			folder.setFormCreatedBy(currentUser);
			if (folder.getFormCreatedOn() == null) {
				folder.setFormCreatedOn(time);
			}
		}
		folder.setFormModifiedBy(currentUser);
		folder.setFormModifiedOn(time);
		addOrUpdate(folder.getWrappedRecord(),
				RecordsFlushing.WITHIN_SECONDS(modelLayerFactory.getSystemConfigs().getTransactionDelay()));

		if (userFolderId != null) {
			RMUserFolder userFolder = rmSchemas().getUserFolder(userFolderId);
			try {
				decommissioningService().duplicateSubStructureAndSave(folder, userFolder, currentUser);
				decommissioningService().deleteUserFolder(userFolder, currentUser);
			} catch (RecordServicesException e) {
				LOGGER.error("Error while trying to recreate user folder structure", e);
				view.showErrorMessage(e.getMessage());
			} catch (IOException e) {
				LOGGER.error("Error while trying to recreate user folder structure", e);
				view.showErrorMessage(e.getMessage());
			}
		}

		navigateToFolderDisplay(folder.getId());
	}

	public void customFieldValueChanged(CustomFolderField<?> customField) {
		adjustCustomFields(customField, false);
	}

	boolean isReloadRequiredAfterFolderTypeChange() {
		boolean reload;
		String currentSchemaCode = getSchemaCode();
		String folderTypeRecordId = getTypeFieldValue();
		if (StringUtils.isNotBlank(folderTypeRecordId)) {
			String schemaCodeForFolderTypeRecordId = rmSchemasRecordsServices
					.getSchemaCodeForFolderTypeRecordId(folderTypeRecordId);
			if (schemaCodeForFolderTypeRecordId != null) {
				reload = !currentSchemaCode.equals(schemaCodeForFolderTypeRecordId);
			} else {
				reload = !currentSchemaCode.equals(Folder.DEFAULT_SCHEMA);
			}
		} else {
			reload = !currentSchemaCode.equals(Folder.DEFAULT_SCHEMA);
		}
		return reload;
	}

	void reloadFormAfterFolderTypeChange() {
		String folderTypeId = getTypeFieldValue();
		String newSchemaCode;
		if (folderTypeId != null) {
			newSchemaCode = rmSchemasRecordsServices.getSchemaCodeForFolderTypeRecordId(folderTypeId);
		} else {
			newSchemaCode = Folder.DEFAULT_SCHEMA;
		}
		if (newSchemaCode == null) {
			newSchemaCode = Folder.DEFAULT_SCHEMA;
		}

		Record folderRecord = toRecord(folderVO);
		Folder folder = new Folder(folderRecord, types());

		setSchemaCode(newSchemaCode);
		folder.changeSchemaTo(newSchemaCode);
		MetadataSchema newSchema = folder.getSchema();

		commitForm();
		for (MetadataVO metadataVO : folderVO.getMetadatas()) {
			String metadataCode = metadataVO.getCode();
			String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);

			try {
				Metadata matchingMetadata = newSchema.getMetadata(metadataCodeWithoutPrefix);
				if (matchingMetadata.getDataEntry().getType() == DataEntryType.MANUAL && !matchingMetadata.isSystemReserved()
					|| matchingMetadata.hasSameCode(Schemas.LEGACY_ID)) {
					Object formValue = folderVO.get(metadataVO);
					Object newDefaultValue = matchingMetadata.getDefaultValue();
					Object oldDefaultValue = metadataVO.getDefaultValue();
					boolean formValueIsEmpty = formValue == null || (formValue instanceof Collection && ((Collection) formValue).isEmpty());
					if (formValueIsEmpty || formValue.equals(oldDefaultValue)) {
						folder.getWrappedRecord().set(matchingMetadata, newDefaultValue);
					} else {
						folder.getWrappedRecord().set(matchingMetadata, formValue);
					}
				}
			} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
				// Ignore
			}
		}

		//TODO REMOVE DUPLICATION OF CODE WITH reloadForm
		folderVO = voBuilder.build(folderRecord, VIEW_MODE.FORM, view.getSessionContext());
		view.setRecord(folderVO);

		reloadForm();
	}

	void reloadFormAndPopulateCurrentMetadatasExcept(List<String> ignoredMetadataCodes) {
		// Populate new record with previous record's metadata values

		commitForm();

		for (MetadataVO metadataVO : folderVO.getMetadatas()) {
			String metadataCode = metadataVO.getCode();
			String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
			if (!ignoredMetadataCodes.contains(metadataCodeWithoutPrefix)) {
				try {
					MetadataVO matchingMetadata = folderVO.getMetadata(metadataCodeWithoutPrefix);
					Object metadataValue = folderVO.get(metadataVO);
					folderVO.set(matchingMetadata, metadataValue);
				} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
					// Ignore
				}
			}
		}

		view.setRecord(folderVO);
		reloadForm();
	}

	void reloadFormAfterParentFolderChanged() {
		commitForm();
		reloadForm();
	}

	void reloadForm() {
		reloadForm(toRecord(folderVO));
	}

	private void reloadForm(Record folderRecord) {
		folderVO = voBuilder.build(folderRecord, VIEW_MODE.FORM, view.getSessionContext());
		view.setRecord(folderVO);
		view.getForm().reload();
	}

	void commitForm() {
		view.getForm().commit();
	}

	String getTypeFieldValue() {
		CustomFolderField field = view.getForm().getCustomField(Folder.TYPE);
		if (field == null) {
			return "";
		}
		return (String) field.getFieldValue();
	}

	private Metadata getNotEnteredMetadata(String metadataCode) {
		String adjustedMetadataCode;
		switch (metadataCode) {
			case ADMINISTRATIVE_UNIT_ENTERED:
				adjustedMetadataCode = ADMINISTRATIVE_UNIT;
				break;
			case CATEGORY_ENTERED:
				adjustedMetadataCode = CATEGORY;
				break;
			case UNIFORM_SUBDIVISION_ENTERED:
				adjustedMetadataCode = UNIFORM_SUBDIVISION;
				break;
			case RETENTION_RULE_ENTERED:
				adjustedMetadataCode = RETENTION_RULE;
				break;
			case COPY_STATUS_ENTERED:
				adjustedMetadataCode = COPY_STATUS;
				break;
			case MAIN_COPY_RULE_ID_ENTERED:
				adjustedMetadataCode = MAIN_COPY_RULE;
				break;
			default:
				adjustedMetadataCode = metadataCode;
		}

		MetadataSchema folderSchema;
		String folderTypeRecordId = getTypeFieldValue();
		if (StringUtils.isNotBlank(folderTypeRecordId)) {
			folderSchema = rmSchemasRecordsServices.folderSchemaFor(folderTypeRecordId);
			if (folderSchema == null) {
				folderSchema = rmSchemasRecordsServices.defaultFolderSchema();
			}
		} else {
			folderSchema = rmSchemasRecordsServices.defaultFolderSchema();
		}
		return folderSchema.get(adjustedMetadataCode);
	}

	private boolean isFieldRequired(String metadataCode) {
		return getNotEnteredMetadata(metadataCode).isDefaultRequirement();
	}

	private String getFieldLabel(String metadataCode) {
		Language language = Language.withCode(view.getSessionContext().getCurrentLocale().getLanguage());
		return getNotEnteredMetadata(metadataCode).getLabel(language);
	}

	private void setFieldVisible(CustomFolderField<?> field, boolean visible, String metadataCode) {
		if (visible) {
			field.setRequired(isFieldRequired(metadataCode));
			field.setCaption(getFieldLabel(metadataCode));
		} else {
			field.setRequired(false);
		}
		field.setVisible(visible);
	}

	private void setFieldReadonly(CustomFolderField<?> field, boolean readOnly) {
		field.setReadOnly(readOnly);
	}

	void adjustCustomFields(CustomFolderField<?> customField, boolean firstDraw) {
		adjustTypeField();
		boolean reload = isReloadRequiredAfterFolderTypeChange();
		boolean parentRemoved = customField instanceof FolderParentFolderFieldImpl && customField.getFieldValue() == null;
		boolean parentAdded = customField instanceof FolderParentFolderFieldImpl && !folderHasParent &&
							  customField.getFieldValue() != null;
		if (reload) {
			reloadFormAfterFolderTypeChange();
		} else if (parentAdded || parentRemoved) {
			reloadFormAfterParentFolderChanged();
		}
		adjustParentFolderField();
		adjustAdministrativeUnitField();
		adjustCategoryField(parentRemoved);
		adjustUniformSubdivisionField(parentRemoved);
		adjustRetentionRuleField(customField);
		adjustStatusCopyEnteredField(firstDraw);
		adjustCopyRetentionRuleField();
		adjustLinearSizeField();
		adjustActualTransferDateField(customField);
		adjustActualDepositDateField(customField);
		adjustActualDestructionDateField(customField);
		adjustContainerField();
		adjustPreviewReturnDateField();
		adjustOpeningDateField();
		adjustDisposalTypeField();
		adjustCustomDynamicFields();

		if (customField instanceof FolderParentFolderFieldImpl) {
			folderHasParent = customField.getFieldValue() != null;
		}
		adjustClosingDateField();
	}

	void adjustTypeField() {
		// Nothing to adjust
	}

	void adjustMediumTypesField() {
		FolderFormImpl folderForm = (FolderFormImpl) view.getForm();
		RecordOptionGroup recordOptionGroup = (RecordOptionGroup) folderForm.getField(Folder.MEDIUM_TYPES);
		for (MediumType mediumType : mediumTypeService.getActivatedOnContentMediumTypes()) {
			recordOptionGroup.setItemEnabled(mediumType.getId(), false);
		}
	}

	protected FolderParentFolderField adjustParentFolderField() {
		FolderParentFolderField parentFolderField = (FolderParentFolderField) view.getForm().getCustomField(Folder.PARENT_FOLDER);
		parentFolderField.setVisible(alwaysShowParentField || folderHadAParent);

		return parentFolderField;
	}

	protected FolderAdministrativeUnitField adjustAdministrativeUnitField() {
		FolderAdministrativeUnitField administrativeUnitField = (FolderAdministrativeUnitField) view.getForm().getCustomField(
				Folder.ADMINISTRATIVE_UNIT_ENTERED);
		FolderParentFolderField parentFolderField = (FolderParentFolderField) view.getForm().getCustomField(Folder.PARENT_FOLDER);
		if (administrativeUnitField != null) {
			String parentId = parentFolderField.getFieldValue();
			setFieldVisible(administrativeUnitField, parentId == null, Folder.ADMINISTRATIVE_UNIT_ENTERED);
		}

		return administrativeUnitField;
	}

	protected FolderCategoryField adjustCategoryField(boolean wasParentRemoved) {
		FolderCategoryField categoryField = (FolderCategoryField) view.getForm().getCustomField(Folder.CATEGORY_ENTERED);
		FolderParentFolderField parentFolderField = (FolderParentFolderField) view.getForm().getCustomField(Folder.PARENT_FOLDER);
		if (categoryField != null && parentFolderField != null) {
			if (wasParentRemoved) {
				folderVO.setCategory((String) null);
				categoryField.setFieldValue(null);
			}
			String categoryId = categoryField.getFieldValue();
			if (categoryId != null) {
				// Discover what options are available
				List<String> availableOptions = decommissioningService().getRetentionRulesForCategory(
						categoryId, null, StatusFilter.ACTIVES);
				if (availableOptions.isEmpty()) {
					view.showErrorMessage($("AddEditFolderView.noRetentionRulesForCategory"));
				}
			}

			String parentFolderId = parentFolderField.getFieldValue();
			if (parentFolderId != null) {
				Record parentFolder = getRecord(parentFolderId);
				Folder parentFolderWrapper = new Folder(parentFolder, types());
				String parentFolderCategoryId = parentFolderWrapper.getCategory();
				String folderCategoryId = folderVO.getCategory();

				// The child folder must be linked to the same category as its parent
				if (parentFolderCategoryId != null) {
					if (!parentFolderCategoryId.equals(folderCategoryId)) {
						folderVO.setCategory(parentFolderCategoryId);
						categoryField.setFieldValue(parentFolderCategoryId);
					}
					// No need to display the field
					if (categoryField.isVisible()) {
						setFieldVisible(categoryField, false, Folder.CATEGORY_ENTERED);
					}
				} else if (!categoryField.isVisible()) {
					setFieldVisible(categoryField, true, Folder.CATEGORY_ENTERED);
				}
			} else {
				setFieldVisible(categoryField, true, Folder.CATEGORY_ENTERED);
			}
		}

		return categoryField;
	}

	void adjustUniformSubdivisionField(boolean wasParentRemoved) {
		FolderUniformSubdivisionField uniformSubdivisionField = (FolderUniformSubdivisionField) view.getForm().getCustomField(
				Folder.UNIFORM_SUBDIVISION_ENTERED);
		FolderParentFolderField parentFolderField = (FolderParentFolderField) view.getForm().getCustomField(Folder.PARENT_FOLDER);
		if (uniformSubdivisionField != null) {
			if (wasParentRemoved) {
				folderVO.setUniformSubdivision((String) null);
				uniformSubdivisionField.setFieldValue(null);
			}
			if (new RMConfigs(modelLayerFactory.getSystemConfigurationsManager()).areUniformSubdivisionEnabled()) {
				uniformSubdivisionField.setVisible(true);
			} else {
				uniformSubdivisionField.setVisible(false);
			}

			String parentFolderId = parentFolderField.getFieldValue();
			if (parentFolderId != null) {
				Record parentFolder = getRecord(parentFolderId);
				Folder parentFolderWrapper = new Folder(parentFolder, types());
				String parentFolderUniformSubdivisionId = parentFolderWrapper.getUniformSubdivisionEntered();

				// The child folder must be linked to the same category as its parent
				if (parentFolderUniformSubdivisionId != null) {
					folderVO.setUniformSubdivision(parentFolderUniformSubdivisionId);
					uniformSubdivisionField.setFieldValue(parentFolderUniformSubdivisionId);
					// No need to display the field
					if (uniformSubdivisionField.isVisible()) {
						setFieldVisible(uniformSubdivisionField, false, Folder.UNIFORM_SUBDIVISION_ENTERED);
					}
				}
			}
		}
	}

	void adjustRetentionRuleField(CustomFolderField<?> changedCustomField) {
		FolderRetentionRuleField retentionRuleField = (FolderRetentionRuleField) view.getForm().getCustomField(
				Folder.RETENTION_RULE_ENTERED);
		FolderCategoryField categoryField = (FolderCategoryField) view.getForm().getCustomField(Folder.CATEGORY_ENTERED);
		FolderUniformSubdivisionField uniformSubdivisionField = (FolderUniformSubdivisionField) view.getForm().getCustomField(
				Folder.UNIFORM_SUBDIVISION_ENTERED);

		if (retentionRuleField != null && uniformSubdivisionField != null && categoryField != null) {
			if (folderVO.getParentFolder() != null) {
				setFieldVisible(retentionRuleField, false, Folder.RETENTION_RULE_ENTERED);
			} else {
				String currentValue = retentionRuleField.getFieldValue();
				// Discover what options are available
				List<String> availableOptions = decommissioningService().getRetentionRulesForCategory(
						categoryField.getFieldValue(), uniformSubdivisionField.getFieldValue(), StatusFilter.ACTIVES);

				// Set the options if they changed
				if (!retentionRuleField.getOptions().equals(availableOptions)) {
					retentionRuleField.setOptions(availableOptions);
				}

				// Set the value if necessary
				if (availableOptions.size() > 1) {
					if (currentValue != null && !availableOptions.contains(currentValue)) {
						folderVO.setRetentionRule((String) null);
						retentionRuleField.setFieldValue(null);
					}
					if (!retentionRuleField.isVisible()) {
						setFieldVisible(retentionRuleField, true, Folder.RETENTION_RULE_ENTERED);
					}
				} else if (availableOptions.size() == 1) {
					if (!availableOptions.get(0).equals(currentValue)) {
						String onlyAvailableOption = availableOptions.get(0);
						folderVO.setRetentionRule(onlyAvailableOption);
						retentionRuleField.setFieldValue(availableOptions.get(0));
					}
					setFieldVisible(retentionRuleField, false, Folder.RETENTION_RULE_ENTERED);
				} else {
					if (currentValue != null) {
						folderVO.setRetentionRule((String) null);
					}
					if (retentionRuleField.isVisible()) {
						setFieldVisible(retentionRuleField, false, Folder.RETENTION_RULE_ENTERED);
					}
				}
			}
		} else {
			if (categoryField == null) {
				if (retentionRuleField != null) {
					retentionRuleField.setVisible(false);
				}

				if (uniformSubdivisionField != null) {
					uniformSubdivisionField.setRequired(false);
				}
			}
		}

		if (changedCustomField instanceof FolderRetentionRuleField) {
			String ruleId = (String) view.getForm().getCustomField(RETENTION_RULE_ENTERED).getFieldValue();
			folderVO.setRetentionRule(ruleId);
			if (areDocumentRetentionRulesEnabled()) {
				Folder record = rmSchemas().wrapFolder(toRecord(folderVO));
				recordServices().recalculate(record);
				folderVO.set(Folder.APPLICABLE_COPY_RULES, record.getApplicableCopyRules());
			}
			List<String> ignoredMetadataCodes = asList(RETENTION_RULE_ENTERED);
			reloadFormAndPopulateCurrentMetadatasExcept(ignoredMetadataCodes);
			view.getForm().getCustomField(RETENTION_RULE_ENTERED).focus();
		}
	}

	boolean isCopyStatusInputPossible(boolean firstDraw) {
		Folder folder = rmSchemas().wrapFolder(toRecord(folderVO));
		FolderRetentionRuleField retentionRuleField = (FolderRetentionRuleField) view.getForm().getCustomField(
				Folder.RETENTION_RULE_ENTERED);
		FolderAdministrativeUnitField administrativeUnitField = (FolderAdministrativeUnitField) view.getForm().getCustomField(
				Folder.ADMINISTRATIVE_UNIT_ENTERED);
		if (retentionRuleField != null && retentionRuleField.getFieldValue() != null) {
			folder.setRetentionRuleEntered(retentionRuleField.getFieldValue());
		}
		if (administrativeUnitField != null && administrativeUnitField.getFieldValue() != null) {
			folder.setAdministrativeUnitEntered(administrativeUnitField.getFieldValue());
		}

		return decommissioningService().isCopyStatusInputPossible(folder, getCurrentUser());
	}

	void adjustStatusCopyEnteredField(boolean firstDraw) {
		FolderCopyStatusEnteredField copyStatusEnteredField = (FolderCopyStatusEnteredField) view.getForm()
				.getCustomField(Folder.COPY_STATUS_ENTERED);
		if (copyStatusEnteredField != null) {
			CopyType currentValue = copyStatusEnteredField.getFieldValue();
			if (isCopyStatusInputPossible(firstDraw)) {
				if (!copyStatusEnteredField.isVisible()) {
					copyStatusEnteredField.setFieldValue(CopyType.PRINCIPAL);
					setFieldVisible(copyStatusEnteredField, true, Folder.COPY_STATUS_ENTERED);
				}
			} else {
				if (currentValue != null) {
					folderVO.setCopyStatusEntered(null);
					copyStatusEnteredField.setFieldValue(null);
				}
				if (copyStatusEnteredField.isVisible()) {
					setFieldVisible(copyStatusEnteredField, false, Folder.COPY_STATUS_ENTERED);
				}
			}
		}
	}

	private void adjustCopyRetentionRuleField() {
		FolderCopyRuleField field = (FolderCopyRuleField) view.getForm().getCustomField(Folder.MAIN_COPY_RULE_ID_ENTERED);
		if (field == null) {
			return;
		}
		commitForm();
		Folder folder = rmSchemas().wrapFolder(toRecord(folderVO));
		recordServices().recalculate(folder);
		List<CopyRetentionRule> applicableCopyRules = folder.getApplicableCopyRules();
		folderVO.set(Folder.APPLICABLE_COPY_RULES, applicableCopyRules);
		if (applicableCopyRules.isEmpty()) {
			folderVO.setMainCopyRuleEntered(null);
			field.setFieldValue(null);
		} else if (applicableCopyRules.size() == 1) {
			CopyRetentionRule mainCopyRule = applicableCopyRules.get(0);
			folderVO.setMainCopyRuleEntered(mainCopyRule.getId());
			field.setFieldValue(mainCopyRule.getId());
		} else if (folder.getMainCopyRule() != null) {
			boolean validEnteredCopyRule = false;
			for (CopyRetentionRule applicableCopyRule : applicableCopyRules) {
				if (applicableCopyRule.getId().equals(folder.getMainCopyRule().getId())) {
					if (StringUtils.isBlank(field.getFieldValue())) {
						field.setFieldValue(folder.getMainCopyRule().getId());
					}
					validEnteredCopyRule = true;
					break;
				}
			}
			if (!validEnteredCopyRule) {
				folderVO.setMainCopyRuleEntered(null);
				field.setFieldValue(null);
			}
		}
		field.setFieldChoices(applicableCopyRules);

		boolean fieldVisible;
		if (!applicableCopyRules.isEmpty() && (isCopyRulesAlwaysVisibleInAddForm() || applicableCopyRules.size() > 1)) {
			fieldVisible = true;
		} else {
			fieldVisible = false;
		}
		field.setVisible(fieldVisible);
	}

	boolean isTransferDateInputPossibleForUser() {
		Folder folder = rmSchemas().wrapFolder(toRecord(folderVO));
		return decommissioningService().isTransferDateInputPossibleForUser(folder, getCurrentUser());
	}

	void adjustLinearSizeField() {
		FolderLinearSizeField linearSizeField = (FolderLinearSizeField) view.getForm().getCustomField(Folder.LINEAR_SIZE);
		if (linearSizeField != null) {
			linearSizeField.setVisible(true);//folderVO.getContainer() != null);
		}
	}

	void adjustActualTransferDateField(CustomFolderField<?> changedCustomField) {
		FolderActualTransferDateField actualTransferDateField = (FolderActualTransferDateField) view.getForm().getCustomField(
				Folder.ACTUAL_TRANSFER_DATE);

		if (actualTransferDateField != null) {
			customContainerDependencyFields.put(actualTransferDateField, actualTransferDateField.getFieldValue());
			if (isTransferDateInputPossibleForUser()) {
				if (!actualTransferDateField.isVisible()) {
					setFieldVisible(actualTransferDateField, true, Folder.ACTUAL_TRANSFER_DATE);
				} else {
					configureIgnoreContainerFieldWhenReloadForm(changedCustomField, actualTransferDateField);
				}
			} else if (actualTransferDateField.isVisible()) {
				setFieldVisible(actualTransferDateField, false, Folder.ACTUAL_TRANSFER_DATE);
			}
		}
	}

	private void configureIgnoreContainerFieldWhenReloadForm(CustomFolderField<?> changedCustomField,
															 CustomFolderField currentField) {
		FolderContainerField containerField = (FolderContainerField) view.getForm().getCustomField(Folder.CONTAINER);

		if (containerField == null) {
			return;
		}

		boolean clearContainerField = true;
		for (Map.Entry customContainerDependencyField : customContainerDependencyFields.entrySet()) {
			if (customContainerDependencyField.getValue() != null) {
				clearContainerField = false;
			}
		}
		if (currentField.equals(changedCustomField) && clearContainerField) {
			//			reloadFormAfterFieldChanged();
		} else if (currentField.equals(changedCustomField) && currentField.getFieldValue() != null && !containerField
				.isVisible()) {
			commitForm();
		}
	}

	boolean isDepositDateInputPossibleForUser() {
		Folder folder = rmSchemas().wrapFolder(toRecord(folderVO));
		return decommissioningService().isDepositDateInputPossibleForUser(folder, getCurrentUser());
	}

	void adjustActualDepositDateField(CustomFolderField<?> changedCustomField) {
		FolderActualDepositDateField actualDepositDateField = (FolderActualDepositDateField) view.getForm().getCustomField(
				Folder.ACTUAL_DEPOSIT_DATE);
		if (actualDepositDateField != null) {
			customContainerDependencyFields.put(actualDepositDateField, actualDepositDateField.getFieldValue());
			if (isDepositDateInputPossibleForUser()) {
				if (!actualDepositDateField.isVisible()) {
					setFieldVisible(actualDepositDateField, true, Folder.ACTUAL_DEPOSIT_DATE);
				} else {
					configureIgnoreContainerFieldWhenReloadForm(changedCustomField, actualDepositDateField);
				}
			} else if (actualDepositDateField.isVisible()) {
				setFieldVisible(actualDepositDateField, false, Folder.ACTUAL_DEPOSIT_DATE);
			}
		}
	}

	boolean isDestructionDateInputPossibleForUser() {
		Folder folder = rmSchemas().wrapFolder(toRecord(folderVO));
		return decommissioningService().isDestructionDateInputPossibleForUser(folder, getCurrentUser());
	}

	void adjustActualDestructionDateField(CustomFolderField<?> changedCustomField) {
		FolderActualDestructionDateField actualDestructionDateField = (FolderActualDestructionDateField) view.getForm()
				.getCustomField(Folder.ACTUAL_DESTRUCTION_DATE);

		if (actualDestructionDateField != null) {
			customContainerDependencyFields.put(actualDestructionDateField, actualDestructionDateField.getFieldValue());
			if (isDestructionDateInputPossibleForUser()) {
				if (!actualDestructionDateField.isVisible()) {
					setFieldVisible(actualDestructionDateField, true, Folder.ACTUAL_DESTRUCTION_DATE);
				} else {
					configureIgnoreContainerFieldWhenReloadForm(changedCustomField, actualDestructionDateField);
				}
			} else if (actualDestructionDateField.isVisible()) {
				actualDestructionDateField.setFieldValue(null);
				setFieldVisible(actualDestructionDateField, false, Folder.ACTUAL_DESTRUCTION_DATE);
			}
		}
	}

	boolean isContainerInputPossibleForUser() {
		Folder folder = rmSchemas().wrapFolder(toRecord(folderVO));
		return decommissioningService().isContainerInputPossibleForUser(folder, getCurrentUser());
	}

	void adjustContainerField() {
		FolderContainerField containerField = (FolderContainerField) view.getForm().getCustomField(Folder.CONTAINER);
		if (containerField != null) {
			if (isContainerInputPossibleForUser()) {
				if (!containerField.isVisible()) {
					setFieldVisible(containerField, true, Folder.CONTAINER);
				}
			} else if (containerField.isVisible()) {
				setFieldVisible(containerField, false, Folder.CONTAINER);
			}
		}
	}

	void adjustPreviewReturnDateField() {
		FolderPreviewReturnDateField previewReturnDateField = (FolderPreviewReturnDateField) view.getForm()
				.getCustomField(Folder.BORROW_PREVIEW_RETURN_DATE);
		Folder folder = rmSchemas().wrapFolder(toRecord(folderVO));
		if (previewReturnDateField != null) {
			if (folder.hasAnalogicalMedium() && folder.getBorrowed() != null && folder.getBorrowed() != false) {
				setFieldVisible(previewReturnDateField, true, Folder.BORROW_PREVIEW_RETURN_DATE);
			} else {
				setFieldVisible(previewReturnDateField, false, Folder.BORROW_PREVIEW_RETURN_DATE);
			}
		}
	}

	void adjustOpeningDateField() {
		UserPermissionsChecker userPermissionsChecker = getCurrentUser().has(RMPermissionsTo.MODIFY_OPENING_DATE_FOLDER);
		Record folderRecord = toRecord(folderVO);
		recordServices().recalculate(folderRecord);
		boolean hasPermission = userPermissionsChecker.on(folderRecord);
		if (!addView && !hasPermission) {
			FolderOpeningDateField openingDateField = (FolderOpeningDateField) view.getForm()
					.getCustomField(Folder.OPENING_DATE);
			if (openingDateField != null) {
				setFieldReadonly(openingDateField, true);
			}
		}
	}

	void adjustClosingDateField() {
		boolean visible = isSubfolderDecommissioningSeparatelyEnabled() || !folderHasParent;
		view.getForm().setFieldVisible(Folder.ENTERED_CLOSING_DATE, visible);
	}

	void adjustDisposalTypeField() {
		FolderDisposalTypeField disposalTypeField = (FolderDisposalTypeField) view.getForm()
				.getCustomField(Folder.MANUAL_DISPOSAL_TYPE);
		if (disposalTypeField != null) {
			boolean visible;
			Record folerRecord = toRecord(folderVO);
			RecordServices recordServices = recordServices();
			recordServices.recalculate(folerRecord);
			Folder folder = rmSchemasRecordsServices.wrapFolder(folerRecord);
			CopyRetentionRule mainCopyRule = folder.getMainCopyRule();
			if (mainCopyRule != null) {
				visible = mainCopyRule.getInactiveDisposalType() == DisposalType.SORT;
			} else {
				visible = false;
			}
			view.getForm().setFieldVisible(Folder.MANUAL_DISPOSAL_TYPE, visible);
		}
	}

	void adjustCustomDynamicFields() {
		FolderForm folderForm = view.getForm();
		GetDynamicFieldMetadatasParams params = new GetDynamicFieldMetadatasParams(Folder.SCHEMA_TYPE, collection);
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(collection);
		List<String> dynamicFieldMetadatas = extensions.getDynamicFieldMetadatas(params);
		for (String dynamicFieldMetadata : dynamicFieldMetadatas) {
			MetadataVO metadataVO = folderVO.getMetadata(dynamicFieldMetadata);
			boolean visible = extensions.isMetadataEnabledInRecordForm(folderVO, metadataVO);
			folderForm.setFieldVisible(dynamicFieldMetadata, visible);
		}
	}

	private boolean canSaveFolder(Folder folder, User user) {
		if (!addView) {
			return true;
		}

		if (folder.getParentFolder() == null) {
			if (folder.getAdministrativeUnitEntered() == null) {
				return true;
			} else {
				AdministrativeUnit unit = rmSchemas().getAdministrativeUnit(folder.getAdministrativeUnitEntered());
				return user.has(RMPermissionsTo.CREATE_FOLDERS).on(unit);
			}
		}

		Folder parent = rmSchemas().getFolder(folder.getParentFolder());
		switch (parent.getPermissionStatus()) {
			case ACTIVE:
				return user.has(RMPermissionsTo.CREATE_SUB_FOLDERS).on(parent);
			case SEMI_ACTIVE:
				return user.has(RMPermissionsTo.CREATE_SUB_FOLDERS_IN_SEMIACTIVE_FOLDERS).on(parent);
			default:
				return user.has(RMPermissionsTo.CREATE_SUB_FOLDERS_IN_INACTIVE_FOLDERS).on(parent);
		}
	}

	private DecommissioningService decommissioningService() {
		return new DecommissioningService(collection, appLayerFactory);
	}

	private RMSchemasRecordsServices rmSchemas() {
		return new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	private boolean areDocumentRetentionRulesEnabled() {
		return new RMConfigs(modelLayerFactory.getSystemConfigurationsManager()).areDocumentRetentionRulesEnabled();
	}

	private boolean isCopyRulesAlwaysVisibleInAddForm() {
		return new RMConfigs(modelLayerFactory.getSystemConfigurationsManager()).isCopyRulesAlwaysVisibleInAddForm();
	}

	private boolean canContainerContainFolder(Folder folder) {
		if (addView) {
			return canContainerContainNewFolder(folder);
		} else {
			return canContainerContainEditedFolder(folder);
		}
	}

	private boolean canContainerContainNewFolder(Folder folder) {
		if (folder.getContainer() != null && folder.getLinearSize() != null) {
			ContainerRecord containerRecord = rmSchemas()
					.wrapContainerRecord(presenterService().getRecord(folder.getContainer()));
			if (containerRecord.getAvailableSize() < folder.getLinearSize()) {
				return false;
			}
		}
		return true;
	}

	private boolean canContainerContainEditedFolder(Folder folder) {
		Record wrappedRecord = folder.getWrappedRecord();
		if (wrappedRecord.isModified(rmSchemas().folder.container())) {
			return canContainerContainNewFolder(folder);
		} else {
			if (folder.getContainer() != null && folder.getLinearSize() != null && wrappedRecord
					.isModified(rmSchemas().folder.linearSize())) {
				ContainerRecord containerRecord = rmSchemas()
						.wrapContainerRecord(presenterService().getRecord(folder.getContainer()));
				Double originalSize = wrappedRecord.getCopyOfOriginalRecord().get(rmSchemas().folder.linearSize());
				double filterOriginalSize = originalSize == null ? 0.0 : originalSize;
				if (containerRecord.getAvailableSize() < folder.getLinearSize() - filterOriginalSize) {
					return false;
				}
			}
			return true;
		}
	}

	@Override
	protected Record newRecord() {
		Record record = super.newRecord();
		Folder folder = rmSchemas().wrapFolder(record);
		folder.setOpenDate(new LocalDate());

		// If the current user is only attached to one administrative unit, set it as the field value.
		User currentUser = getCurrentUser();
		SearchServices searchServices = searchServices();
		MetadataSchemaTypes types = types();
		MetadataSchemaType administrativeUnitSchemaType = types.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
		//visibleAdministrativeUnitsQuery.filteredWithUserWrite(currentUser);

		String defaultAdministrativeUnit = getCurrentUser().get(RMUser.DEFAULT_ADMINISTRATIVE_UNIT);
		RMConfigs rmConfigs = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		if (rmConfigs.isFolderAdministrativeUnitEnteredAutomatically()) {
			if (StringUtils.isNotBlank(defaultAdministrativeUnit)) {
				try {
					Record defaultAdministrativeUnitRecord = recordServices().getDocumentById(defaultAdministrativeUnit);
					if (currentUser.hasWriteAccess().on(defaultAdministrativeUnitRecord)) {
						folder.setAdministrativeUnitEntered(defaultAdministrativeUnitRecord);
					} else {
						LOGGER.error("User " + getCurrentUser().getUsername()
									 + " has no longer write access to default administrative unit " + defaultAdministrativeUnit);
					}
				} catch (Exception e) {
					LOGGER.error("Default administrative unit for user " + getCurrentUser().getUsername() + " is invalid: "
								 + defaultAdministrativeUnit);
				}
			} else {
				List<Record> records = new ArrayList<>(searchServices.getAllRecords(administrativeUnitSchemaType));
				Collections.sort(records, new Comparator<Record>() {
					@Override
					public int compare(Record o1, Record o2) {
						String p1 = o1.get(Schemas.PRINCIPAL_PATH);
						String p2 = o2.get(Schemas.PRINCIPAL_PATH);
						return -1 * LangUtils.compareStrings(p1, p2);
					}
				});
				for (Record anAdministrativeUnit : records) {
					if (currentUser.hasWriteAccess().on(anAdministrativeUnit)) {
						folder.setAdministrativeUnitEntered(anAdministrativeUnit);
						break;
					}
				}

			}
		}
		return record;
	}

	private boolean isSubfolderDecommissioningSeparatelyEnabled() {
		return modelLayerFactory.getSystemConfigurationsManager().getValue(RMConfigs.SUB_FOLDER_DECOMMISSIONING);
	}
}
