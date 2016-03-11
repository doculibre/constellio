package com.constellio.app.modules.rm.ui.pages.folder;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.builders.FolderToVOBuilder;
import com.constellio.app.modules.rm.ui.components.folder.fields.CustomFolderField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderActualDepositDateField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderActualDestructionDateField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderActualTransferDateField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderAdministrativeUnitField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCategoryField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderContainerField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCopyRuleField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCopyStatusEnteredField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderLinearSizeField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderOpeningDateField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderParentFolderField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderPreviewReturnDateField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderRetentionRuleField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderUniformSubdivisionField;
import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserPermissionsChecker;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.search.StatusFilter;

public class AddEditFolderPresenter extends SingleSchemaBasePresenter<AddEditFolderView> {
	private FolderToVOBuilder voBuilder = new FolderToVOBuilder();
	private boolean addView;
	private boolean folderHadAParent;
	private String currentSchemaCode;
	private FolderVO folderVO;
	private Map<CustomFolderField<?>, Object> customContainerDependencyFields = new HashMap<>();

	private transient RMSchemasRecordsServices rmSchemasRecordsServices;

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
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public void forParams(String params) {
		Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
		String id = paramsMap.get("id");
		String parentId = paramsMap.get("parentId");

		Record record;
		if (StringUtils.isNotBlank(id)) {
			record = getRecord(id);
			addView = false;
		} else if (parentId == null) {
			record = newRecord();
			addView = true;
		} else {
			Folder folder = new RMSchemasRecordsServices(collection, modelLayerFactory).getFolder(parentId);
			record = new DecommissioningService(collection, modelLayerFactory).newSubFolderIn(folder).getWrappedRecord();
			addView = true;
		}
		folderVO = voBuilder.build(record, VIEW_MODE.FORM, view.getSessionContext());
		folderHadAParent = folderVO.getParentFolder() != null;
		this.currentSchemaCode = folderVO.getSchema().getCode();
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
		String parentId = paramsMap.get("parentId");
		List<String> ids = new ArrayList<>();
		if (!addView) {
			ids.add(folderVO.getId());
		} else if (parentId != null) {
			ids.add(parentId);
		}
		return ids;
	}

	public void viewAssembled() {
		adjustCustomFields(null);
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
				view.navigateTo().recordsManagement();
			}
		} else {
			view.navigate().to(RMViews.class).displayFolder(folderVO.getId());
		}
	}

	public void saveButtonClicked() {
		Folder record = rmSchemas().wrapFolder(toRecord(folderVO));
		if (!canSaveFolder(record, getCurrentUser())) {
			view.showMessage($("AddEditDocumentView.noPermissionToSaveDocument"));
			return;
		}
		LocalDateTime time = TimeProvider.getLocalDateTime();
		if (isAddView()) {
			record.setFormCreatedBy(getCurrentUser()).setFormCreatedOn(time);
		}
		record.setFormModifiedBy(getCurrentUser()).setFormModifiedOn(time);
		addOrUpdate(record.getWrappedRecord());
		view.navigate().to(RMViews.class).displayFolder(record.getId());
	}

	public void customFieldValueChanged(CustomFolderField<?> customField) {
		adjustCustomFields(customField);
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
				if (matchingMetadata.getDataEntry().getType() == DataEntryType.MANUAL) {
					Object metadataValue = folderVO.get(metadataVO);
					Object defaultValue = metadataVO.getDefaultValue();
					if (metadataValue == null || !metadataValue.equals(defaultValue)) {
						folder.getWrappedRecord().set(matchingMetadata, metadataValue);
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

	private boolean isFieldRequired(String metadataCode) {
		return folderVO.getMetadata(metadataCode).isRequired();
	}

	private void setFieldVisible(CustomFolderField<?> field, boolean visible, String metadataCode) {
		if (visible) {
			field.setRequired(isFieldRequired(metadataCode));
		} else {
			field.setRequired(false);
		}
		field.setVisible(visible);
	}

	private void setFieldReadonly(CustomFolderField<?> field, boolean readOnly) {
		field.setReadOnly(readOnly);
	}

	void adjustCustomFields(CustomFolderField<?> customField) {
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
		adjustStatusCopyEnteredField();
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

	@SuppressWarnings("unchecked")
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
		FolderCategoryField categoryField = (FolderCategoryField) view.getForm().getCustomField(Folder.CATEGORY_ENTERED);
		FolderParentFolderField parentFolderField = (FolderParentFolderField) view.getForm().getCustomField(Folder.PARENT_FOLDER);
		if (uniformSubdivisionField != null && parentFolderField != null) {
			String parentFolderId = parentFolderField.getFieldValue();
			if (parentFolderId != null) {
				Record parentFolder = getRecord(parentFolderId);
				Folder parentFolderWrapper = new Folder(parentFolder, types());
				String parentFolderUniformSubdivisionId = parentFolderWrapper.getUniformSubdivisionEntered();
				String folderUniformSubdivisionId = folderVO.getUniformSubdivision();

				// The child folder must be linked to the same category as its parent
				if (parentFolderUniformSubdivisionId != null) {
					if (!parentFolderUniformSubdivisionId.equals(folderUniformSubdivisionId)) {
						folderVO.setUniformSubdivision(parentFolderUniformSubdivisionId);
					}
					// No need to display the field
					if (uniformSubdivisionField.isVisible()) {
						setFieldVisible(uniformSubdivisionField, false, Folder.UNIFORM_SUBDIVISION_ENTERED);
					}
				} else if (!uniformSubdivisionField.isVisible()) {
					setFieldVisible(uniformSubdivisionField, true, Folder.UNIFORM_SUBDIVISION_ENTERED);
				}
			} else {
				setFieldVisible(uniformSubdivisionField, false, Folder.UNIFORM_SUBDIVISION_ENTERED);
			}
			if (uniformSubdivisionField.isVisible() && (categoryField == null || !categoryField.isVisible())) {
				setFieldVisible(uniformSubdivisionField, false, Folder.UNIFORM_SUBDIVISION_ENTERED);
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

	boolean isCopyStatusInputPossible() {
		Folder folder = rmSchemas().wrapFolder(toRecord(folderVO));
		FolderRetentionRuleField retentionRuleField = (FolderRetentionRuleField) view.getForm().getCustomField(
				Folder.RETENTION_RULE_ENTERED);
		if (retentionRuleField != null) {
			folder.setRetentionRuleEntered(retentionRuleField.getFieldValue());
		}
		return decommissioningService().isCopyStatusInputPossible(folder, getCurrentUser());
	}

	void adjustStatusCopyEnteredField() {
		FolderCopyStatusEnteredField copyStatusEnteredField = (FolderCopyStatusEnteredField) view.getForm()
				.getCustomField(Folder.COPY_STATUS_ENTERED);
		if (copyStatusEnteredField != null) {
			CopyType currentValue = copyStatusEnteredField.getFieldValue();
			if (isCopyStatusInputPossible()) {
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
		if (areDocumentRetentionRulesEnabled()) {
			Folder folder = rmSchemas().wrapFolder(toRecord(folderVO));
			recordServices().refresh(folder);
			List<CopyRetentionRule> rules = folder.getApplicableCopyRules();
			folderVO.set(Folder.APPLICABLE_COPY_RULES, rules);
			field.setVisible(rules.size() > 1);
		} else {
			field.setVisible(false);
		}
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
		if (previewReturnDateField != null && folder.hasAnalogicalMedium() && folder.getBorrowed() != null
				&& folder.getBorrowed() != false) {
			setFieldVisible(previewReturnDateField, true, Folder.BORROW_PREVIEW_RETURN_DATE);
		} else {
			setFieldVisible(previewReturnDateField, false, Folder.BORROW_PREVIEW_RETURN_DATE);
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
		return new DecommissioningService(collection, modelLayerFactory);
	}

	private RMSchemasRecordsServices rmSchemas() {
		return new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

	private boolean areDocumentRetentionRulesEnabled() {
		return new RMConfigs(modelLayerFactory.getSystemConfigurationsManager()).areDocumentRetentionRulesEnabled();
	}
}
