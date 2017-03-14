package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.builders.FolderToVOBuilder;
import com.constellio.app.modules.rm.ui.components.folder.fields.*;
import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserPermissionsChecker;
import com.constellio.model.entities.schemas.*;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.modules.rm.wrappers.Folder.*;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class AddEditFolderPresenter extends SingleSchemaBasePresenter<AddEditFolderView> {
    private static final String ID = "id";
    private static final String PARENT_ID = "parentId";
    private static final String DUPLICATE = "duplicate";
    private static final String STRUCTURE = "structure";

    private FolderToVOBuilder voBuilder = new FolderToVOBuilder();
	private boolean addView;
	private boolean folderHadAParent;
    private String currentSchemaCode;
	private FolderVO folderVO;
	private Map<CustomFolderField<?>, Object> customContainerDependencyFields = new HashMap<>();
    boolean isDuplicateAction;
    boolean isDuplicateStructureAction;

	private transient RMSchemasRecordsServices rmSchemasRecordsServices;
	private transient BorrowingServices borrowingServices;

	public AddEditFolderPresenter(AddEditFolderView view) {
		super(view, Folder.DEFAULT_SCHEMA);
		initTransientObjects();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
		borrowingServices = new BorrowingServices(collection, modelLayerFactory);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public void forParams(String params) {
		Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
		String id = paramsMap.get(ID);
		String parentId = paramsMap.get(PARENT_ID);

        Record record;
		if (StringUtils.isNotBlank(id)) {
			record = getRecord(id);
			addView = false;
		} else if (parentId == null) {
			record = newRecord();
			addView = true;
		} else {
			Folder folder = new RMSchemasRecordsServices(collection, appLayerFactory).getFolder(parentId);
			record = new DecommissioningService(collection, appLayerFactory).newSubFolderIn(folder).getWrappedRecord();
			addView = true;
		}

        isDuplicateAction = paramsMap.containsKey(DUPLICATE);
        isDuplicateStructureAction = isDuplicateAction && paramsMap.containsKey(STRUCTURE);
        if (isDuplicateStructureAction) {
            Folder folder = rmSchemas().wrapFolder(record);
            record = decommissioningService().duplicateStructure(folder, getCurrentUser(), false).getWrappedRecord();
        } else if (isDuplicateAction) {
            Folder folder = rmSchemas().wrapFolder(record);
            record = decommissioningService().duplicate(folder, getCurrentUser(), false).getWrappedRecord();
        }

        folderVO = voBuilder.build(record, VIEW_MODE.FORM, view.getSessionContext());
		folderHadAParent = folderVO.getParentFolder() != null;
		currentSchemaCode = folderVO.getSchema().getCode();
		setSchemaCode(currentSchemaCode);

		view.setRecord(folderVO);
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
				view.navigate().to(RMViews.class).displayFolder(parentId);
			} else {
				view.navigate().to().recordsManagement();
			}
		} else {
			view.navigate().to(RMViews.class).displayFolder(folderVO.getId());
		}
	}

	public void saveButtonClicked() {
		Folder folder = rmSchemas().wrapFolder(toRecord(folderVO));
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
			String borrowingErrorMessage = borrowingServices.validateBorrowingInfos(borrowingUserId, borrowingDate, previewReturnDate, borrowingType, returnDate);
			if (borrowingErrorMessage != null) {
				view.showErrorMessage($(borrowingErrorMessage));
				return;
			}
		}
		LocalDateTime time = TimeProvider.getLocalDateTime();
		if (isAddView()) {
			folder.setFormCreatedBy(getCurrentUser()).setFormCreatedOn(time);
		}
		folder.setFormModifiedBy(getCurrentUser()).setFormModifiedOn(time);
		addOrUpdate(folder.getWrappedRecord(), RecordsFlushing.WITHIN_SECONDS(modelLayerFactory.getSystemConfigs().getTransactionDelay()));
		view.navigate().to(RMViews.class).displayFolder(folder.getId());
	}

	public void customFieldValueChanged(CustomFolderField<?> customField) {
		adjustCustomFields(customField, false);
	}

	boolean isReloadRequiredAfterFolderTypeChange() {
		boolean reload;
		String currentSchemaCode = getSchemaCode();
		String folderTypeRecordId = getTypeFieldValue();
		if (StringUtils.isNotBlank(folderTypeRecordId)) {
			String schemaCodeForFolderTypeRecordId = rmSchemasRecordsServices.getSchemaCodeForFolderTypeRecordId(folderTypeRecordId);
			if (schemaCodeForFolderTypeRecordId != null) {
				reload = !currentSchemaCode.equals(schemaCodeForFolderTypeRecordId);
			} else
				reload = !currentSchemaCode.equals(Folder.DEFAULT_SCHEMA);
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
				if (matchingMetadata.getDataEntry().getType() == DataEntryType.MANUAL && !matchingMetadata.isSystemReserved()) {
					Object voMetadataValue = folderVO.get(metadataVO);
					Object defaultValue = matchingMetadata.getDefaultValue();
					Object voDefaultValue = metadataVO.getDefaultValue();
					if (voMetadataValue == null && defaultValue == null) {
						folder.getWrappedRecord().set(matchingMetadata, voMetadataValue);
					} else if (voMetadataValue != null && !voMetadataValue.equals(voDefaultValue)) {
						folder.getWrappedRecord().set(matchingMetadata, voMetadataValue);
					}
				}
			} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
				// Ignore
			}
		}

		folderVO = voBuilder.build(folderRecord, VIEW_MODE.FORM, view.getSessionContext());

		view.setRecord(folderVO);
		reloadForm();
	}

	void reloadFormAfterFieldChanged() {
		commitForm();
		reloadForm();
	}

	void reloadForm() {
		view.getForm().reload();
	}

	void commitForm() {
		view.getForm().commit();
	}

	String getTypeFieldValue() {
		return (String) view.getForm().getCustomField(Folder.TYPE).getFieldValue();
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
		if (reload) {
			reloadFormAfterFolderTypeChange();
		}
		adjustParentFolderField();
		adjustAdministrativeUnitField();
		adjustCategoryField();
		adjustUniformSubdivisionField();
		adjustRetentionRuleField();
		adjustStatusCopyEnteredField(firstDraw);
		adjustCopyRetentionRuleField();
		adjustLinearSizeField();
		adjustActualTransferDateField(customField);
		adjustActualDepositDateField(customField);
		adjustActualDestructionDateField(customField);
		adjustContainerField();
		adjustPreviewReturnDateField();
		adjustOpeningDateField();
	}

	void adjustTypeField() {
		// Nothing to adjust
	}

	void adjustParentFolderField() {
		FolderParentFolderField parentFolderField = (FolderParentFolderField) view.getForm().getCustomField(Folder.PARENT_FOLDER);
		parentFolderField.setVisible(folderHadAParent);
	}

	void adjustAdministrativeUnitField() {
		FolderAdministrativeUnitField administrativeUnitField = (FolderAdministrativeUnitField) view.getForm().getCustomField(
				Folder.ADMINISTRATIVE_UNIT_ENTERED);
		FolderParentFolderField parentFolderField = (FolderParentFolderField) view.getForm().getCustomField(Folder.PARENT_FOLDER);
		if (administrativeUnitField != null) {
			String parentId = parentFolderField.getFieldValue();
			setFieldVisible(administrativeUnitField, parentId == null, Folder.ADMINISTRATIVE_UNIT_ENTERED);
		}
	}

	void adjustCategoryField() {
		FolderCategoryField categoryField = (FolderCategoryField) view.getForm().getCustomField(Folder.CATEGORY_ENTERED);
		FolderParentFolderField parentFolderField = (FolderParentFolderField) view.getForm().getCustomField(Folder.PARENT_FOLDER);
		if (categoryField != null && parentFolderField != null) {
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
	}

	void adjustUniformSubdivisionField() {
		FolderUniformSubdivisionField uniformSubdivisionField = (FolderUniformSubdivisionField) view.getForm().getCustomField(
				Folder.UNIFORM_SUBDIVISION_ENTERED);
		FolderParentFolderField parentFolderField = (FolderParentFolderField) view.getForm().getCustomField(Folder.PARENT_FOLDER);
		if (uniformSubdivisionField != null) {

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

	void adjustRetentionRuleField() {
		FolderRetentionRuleField retentionRuleField = (FolderRetentionRuleField) view.getForm().getCustomField(
				Folder.RETENTION_RULE_ENTERED);
		FolderCategoryField categoryField = (FolderCategoryField) view.getForm().getCustomField(Folder.CATEGORY_ENTERED);
		FolderUniformSubdivisionField uniformSubdivisionField = (FolderUniformSubdivisionField) view.getForm().getCustomField(
				Folder.UNIFORM_SUBDIVISION_ENTERED);

		if (retentionRuleField != null) {
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
	}

	boolean isCopyStatusInputPossible(boolean firstDraw) {
		Folder folder = rmSchemas().wrapFolder(toRecord(folderVO));
		FolderRetentionRuleField retentionRuleField = (FolderRetentionRuleField) view.getForm().getCustomField(
				Folder.RETENTION_RULE_ENTERED);
		if (retentionRuleField != null && retentionRuleField.getFieldValue() != null) {
			folder.setRetentionRuleEntered(retentionRuleField.getFieldValue());
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
					setFieldVisible(copyStatusEnteredField, true, Folder.COPY_STATUS_ENTERED);
				}
			} else {
				if (currentValue != null) {
					folderVO.setCopyStatusEntered(null);
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
		List<CopyRetentionRule> rules = folder.getApplicableCopyRules();
		folderVO.set(Folder.APPLICABLE_COPY_RULES, rules);
		field.setFieldChoices(rules);
		field.setVisible(rules.size() > 1);
	}

	boolean isTransferDateInputPossibleForUser() {
		Folder folder = rmSchemas().wrapFolder(toRecord(folderVO));
		return decommissioningService().isTransferDateInputPossibleForUser(folder, getCurrentUser());
	}

	void adjustLinearSizeField() {
		FolderLinearSizeField linearSizeField = (FolderLinearSizeField) view.getForm().getCustomField(Folder.LINEAR_SIZE);
		if (linearSizeField != null) {
			linearSizeField.setVisible(folderVO.getContainer() != null);
		}
	}

	void adjustActualTransferDateField(CustomFolderField<?> changedCustomField) {
		FolderActualTransferDateField actualTransferDateField = (FolderActualTransferDateField) view.getForm().getCustomField(
				Folder.ACTUAL_TRANSFER_DATE);
		customContainerDependencyFields.put(actualTransferDateField, actualTransferDateField.getFieldValue());
		if (actualTransferDateField != null) {
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
		boolean clearContainerField = true;
		for (Map.Entry customContainerDependencyField : customContainerDependencyFields.entrySet()) {
			if (customContainerDependencyField.getValue() != null) {
				clearContainerField = false;
			}
		}
		if (currentField.equals(changedCustomField) && clearContainerField) {
			reloadFormAfterFieldChanged();
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
		customContainerDependencyFields.put(actualDepositDateField, actualDepositDateField.getFieldValue());
		if (actualDepositDateField != null) {
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
		customContainerDependencyFields.put(actualDestructionDateField, actualDestructionDateField.getFieldValue());
		if (actualDestructionDateField != null) {
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
		boolean hasPermission = userPermissionsChecker.on(toRecord(folderVO));
		if (!addView && !hasPermission) {
			FolderOpeningDateField openingDateField = (FolderOpeningDateField) view.getForm()
					.getCustomField(Folder.OPENING_DATE);
			setFieldReadonly(openingDateField, true);
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
        LogicalSearchQuery visibleAdministrativeUnitsQuery = new LogicalSearchQuery();
        visibleAdministrativeUnitsQuery.filteredWithUserWrite(currentUser);
        LogicalSearchCondition visibleAdministrativeUnitsCondition = from(administrativeUnitSchemaType).returnAll();
        visibleAdministrativeUnitsQuery.setCondition(visibleAdministrativeUnitsCondition);
        if (searchServices.getResultsCount(visibleAdministrativeUnitsQuery) == 1) {
        	Record defaultAdministrativeUnitRecord = searchServices.search(visibleAdministrativeUnitsQuery).get(0);
        	folder.setAdministrativeUnitEntered(defaultAdministrativeUnitRecord);
        }
		return record;
	}
}
