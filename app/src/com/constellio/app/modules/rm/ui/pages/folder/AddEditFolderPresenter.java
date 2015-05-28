/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.ui.pages.folder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
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
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCopyStatusEnteredField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderFilingSpaceField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderParentFolderField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderRetentionRuleField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderUniformSubdivisionField;
import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.services.search.StatusFilter;

public class AddEditFolderPresenter extends SingleSchemaBasePresenter<AddEditFolderView> {
	private FolderToVOBuilder voBuilder = new FolderToVOBuilder();
	private boolean addView;
	private boolean folderHadAParent;
	private String currentSchemaCode;
	private FolderVO folderVO;

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
		folderVO = voBuilder.build(record, VIEW_MODE.FORM);
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
			FolderStatus status = restrictedFolder.getArchivisticStatus();
			if (status != null && status.isSemiActive()) {
				requiredPermissions.add(RMPermissionsTo.CREATE_SUB_FOLDERS_IN_SEMIACTIVE_FOLDERS);
			}

			if (status != null && status.isInactive()) {
				requiredPermissions.add(RMPermissionsTo.CREATE_SUB_FOLDERS_IN_INACTIVE_FOLDERS);
			}

			return user.hasAll(requiredPermissions).on(restrictedFolder) && user.hasWriteAccess().on(restrictedFolder);
		} else {
			List<String> requiredPermissions = new ArrayList<>();
			FolderStatus status = restrictedFolder.getArchivisticStatus();
			if (status != null && status.isSemiActive()) {
				requiredPermissions.add(RMPermissionsTo.MODIFY_SEMIACTIVE_FOLDERS);
			}

			if (status != null && status.isInactive()) {
				requiredPermissions.add(RMPermissionsTo.MODIFY_INACTIVE_FOLDERS);
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
		adjustCustomFields();
	}

	public boolean isAddView() {
		return addView;
	}

	public void cancelButtonClicked() {
		if (addView) {
			String parentId = folderVO.getParentFolder();
			if (parentId != null) {
				view.navigateTo().displayFolder(parentId);
			} else {
				view.navigateTo().recordsManagement();
			}
		} else {
			view.navigateTo().displayFolder(folderVO.getId());
		}
	}

	public void saveButtonClicked() {
		Record record = toRecord(folderVO);
		addOrUpdate(record);
		view.navigateTo().displayFolder(record.getId());
	}

	public void customFieldValueChanged(CustomFolderField<?> customField) {
		adjustCustomFields();
	}

	String getSchemaCodeForFolderTypeRecordId(String folderTypeRecordId) {
		Record schemaRecord = getRecord(folderTypeRecordId);
		FolderType folderType = new FolderType(schemaRecord, types());
		String linkedSchemaCode = folderType.getLinkedSchema();
		return linkedSchemaCode;
	}

	boolean isReloadRequiredAfterFolderTypeChange() {
		boolean reload;
		if (addView) {
			String currentSchemaCode = getSchemaCode();
			String folderTypeRecordId = (String) view.getForm().getCustomField(Folder.TYPE).getFieldValue();
			if (StringUtils.isNotBlank(folderTypeRecordId)) {
				String schemaCodeForFolderTypeRecordId = rmSchemasRecordsServices.getSchemaCodeForFolderTypeRecordId(folderTypeRecordId);
				if (schemaCodeForFolderTypeRecordId != null) {
					reload = !currentSchemaCode.equals(schemaCodeForFolderTypeRecordId);
				} else if (!currentSchemaCode.equals(Folder.DEFAULT_SCHEMA)) {	
					reload = true;
				} else {
					reload = false;
				}
			} else {
				reload = !currentSchemaCode.equals(Folder.DEFAULT_SCHEMA);
			}
		} else {
			reload = false;
		}
		return reload;
	}

	void reloadFormAfterFolderTypeChange() {
		String folderTypeId = (String) view.getForm().getCustomField(Folder.TYPE).getFieldValue();
		
		Folder folder;
		if (folderTypeId != null) {
			folder = rmSchemasRecordsServices.newFolderWithType(folderTypeId);
		} else {
			folder = rmSchemasRecordsServices.newFolder();
		}
		
		MetadataSchema folderSchema = folder.getSchema();
		String currentSchemaCode = folderSchema.getCode();
		setSchemaCode(currentSchemaCode);
		
		List<String> ignoredMetadataCodes = Arrays.asList(Folder.TYPE);
		// Populate new record with previous record's metadata values
		
		view.getForm().commit();
		for (MetadataVO metadataVO : folderVO.getMetadatas()) {
			String metadataCode = metadataVO.getCode();
			String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
			if (!ignoredMetadataCodes.contains(metadataCodeWithoutPrefix)) {
				try {
					Metadata matchingMetadata = folderSchema.getMetadata(metadataCodeWithoutPrefix);
					Object metadataValue = folderVO.get(metadataVO);
					folder.getWrappedRecord().set(matchingMetadata, metadataValue);
				} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
					// Ignore
				}
			}
		}

		folderVO = voBuilder.build(folder.getWrappedRecord(), VIEW_MODE.FORM);
		
		view.setRecord(folderVO);
	 	view.getForm().reload();
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

	void adjustCustomFields() {
		adjustTypeField();
		boolean reload = isReloadRequiredAfterFolderTypeChange();
		if (reload) {
			reloadFormAfterFolderTypeChange();
		} else {
			adjustParentFolderField();
			adjustAdministrativeUnitField();
			adjustFilingSpaceField();
			adjustCategoryField();
			adjustUniformSubdivisionField();
			adjustRetentionRuleField();
			adjustStatusCopyEnteredField();
			adjustActualTransferDateField();
			adjustActualDepositDateField();
			adjustActualDestructionDateField();
			adjustContainerField();
		}
	}

	void adjustTypeField() {
		// Nothing to adjust
	}

	void adjustParentFolderField() {
		FolderParentFolderField parentFolderField = (FolderParentFolderField) view.getForm().getCustomField(Folder.PARENT_FOLDER);
		parentFolderField.setVisible(folderHadAParent);
	}

	List<String> getCurrentUserAdministrativeUnits() {
		return decommissioningService().getAdministrativeUnitsForUser(getCurrentUser());
	}

	List<String> getAdministrativeUnitsForFilingSpace(String recordId) {
		FilingSpace filingSpace = rmSchemas().getFilingSpace(recordId);
		return decommissioningService().getAdministrativeUnitsWithFilingSpaceForUser(filingSpace, getCurrentUser());
	}

	@SuppressWarnings("unchecked")
	void adjustAdministrativeUnitField() {
		FolderAdministrativeUnitField administrativeUnitField = (FolderAdministrativeUnitField) view.getForm().getCustomField(
				Folder.ADMINISTRATIVE_UNIT_ENTERED);
		FolderFilingSpaceField filingSpaceField = (FolderFilingSpaceField) view.getForm().getCustomField(
				Folder.FILING_SPACE_ENTERED);
		FolderParentFolderField parentFolderField = (FolderParentFolderField) view.getForm().getCustomField(Folder.PARENT_FOLDER);
		if (administrativeUnitField != null) {
			String currentValue = administrativeUnitField.getFieldValue();

			String parentId = parentFolderField.getFieldValue();
			if (parentId == null) {

				List<String> availableOptions = new ArrayList<String>();
				List<String> currentUserAdministrativeUnits = getCurrentUserAdministrativeUnits();
				availableOptions.addAll(currentUserAdministrativeUnits);
				if (currentValue != null && !availableOptions.contains(currentValue)) {
					availableOptions.add(0, currentValue);
				}

				// Discover what options are available
				if (filingSpaceField != null && filingSpaceField.getFieldValue() != null) {
					String currentFilingSpaceValue = filingSpaceField.getFieldValue();
					List<String> administrativeUnitsForFilingSpace = getAdministrativeUnitsForFilingSpace(
							currentFilingSpaceValue);
					availableOptions = (List<String>) ListUtils.retainAll(availableOptions, administrativeUnitsForFilingSpace);
				}

				// Set the options if they changed
				if (!administrativeUnitField.getOptions().equals(availableOptions)) {
					administrativeUnitField.setOptions(availableOptions);
				}

				// Set the value if necessary
				if (availableOptions.size() > 1) {
					if (currentValue != null && !availableOptions.contains(currentValue)) {
						folderVO.setAdministrativeUnit((String) null);
						administrativeUnitField.setFieldValue(null);
					}
					if (!administrativeUnitField.isVisible()) {
						setFieldVisible(administrativeUnitField, true, Folder.ADMINISTRATIVE_UNIT_ENTERED);
					}
				} else if (availableOptions.size() == 1) {
					if (!availableOptions.get(0).equals(currentValue)) {
						String onlyAvailableOption = availableOptions.get(0);
						folderVO.setAdministrativeUnit(onlyAvailableOption);
						administrativeUnitField.setFieldValue(onlyAvailableOption);

					}
				} else {
					if (currentValue != null) {
						folderVO.setAdministrativeUnit((String) null);
						administrativeUnitField.setFieldValue(null);
					}
				}
			} else {
				setFieldVisible(administrativeUnitField, false, Folder.ADMINISTRATIVE_UNIT_ENTERED);
			}
		}
	}

	List<String> getCurrentUserFilingSpaces() {
		return decommissioningService().getUserFilingSpaces(getCurrentUser(), StatusFilter.ACTIVES);
	}

	void adjustFilingSpaceField() {
		FolderFilingSpaceField filingSpaceField = (FolderFilingSpaceField) view.getForm().getCustomField(
				Folder.FILING_SPACE_ENTERED);
		FolderParentFolderField parentFolderField = (FolderParentFolderField) view.getForm().getCustomField(Folder.PARENT_FOLDER);
		if (filingSpaceField != null) {
			String currentValue = filingSpaceField.getFieldValue();

			String parentId = parentFolderField.getFieldValue();
			if (parentId == null) {

				List<String> availableOptions = new ArrayList<String>();
				List<String> currentUserFilingSpaces = getCurrentUserFilingSpaces();
				availableOptions.addAll(currentUserFilingSpaces);
				if (currentValue != null && !availableOptions.contains(currentValue)) {
					availableOptions.add(0, currentValue);
				}

				// Set the options if they changed
				if (!filingSpaceField.getOptions().equals(availableOptions)) {
					filingSpaceField.setOptions(availableOptions);
				}

				// Set the value if necessary
				if (availableOptions.size() > 1) {
					if (currentValue != null && !availableOptions.contains(currentValue)) {
						folderVO.setFilingSpace((String) null);
						filingSpaceField.setFieldValue(null);
					}
					if (!filingSpaceField.isVisible()) {
						setFieldVisible(filingSpaceField, true, Folder.FILING_SPACE_ENTERED);
					}
				} else if (availableOptions.size() == 1) {
					if (!availableOptions.get(0).equals(currentValue)) {
						String onlyAvailableOption = availableOptions.get(0);
						folderVO.setFilingSpace(onlyAvailableOption);
						filingSpaceField.setFieldValue(onlyAvailableOption);
					}
				} else {
					if (currentValue != null) {
						folderVO.setFilingSpace((String) null);
						filingSpaceField.setFieldValue(null);
					}
				}
			} else {
				setFieldVisible(filingSpaceField, false, Folder.FILING_SPACE_ENTERED);
			}
		}
	}

	void adjustCategoryField() {
		FolderCategoryField categoryField = (FolderCategoryField) view.getForm().getCustomField(Folder.CATEGORY_ENTERED);
		FolderParentFolderField parentFolderField = (FolderParentFolderField) view.getForm().getCustomField(Folder.PARENT_FOLDER);
		if (categoryField != null && parentFolderField != null) {
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
		FolderParentFolderField parentFolderField = (FolderParentFolderField) view.getForm().getCustomField(Folder.PARENT_FOLDER);
		FolderUniformSubdivisionField uniformSubdivisionField = (FolderUniformSubdivisionField) view.getForm().getCustomField(
				Folder.UNIFORM_SUBDIVISION_ENTERED);

		if (retentionRuleField != null) {
			String currentValue = retentionRuleField.getFieldValue();
			String parentFolderId = parentFolderField.getFieldValue();
			if (parentFolderId == null) {
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
				if (retentionRuleField.isVisible() && !categoryField.isVisible()) {
					setFieldVisible(retentionRuleField, false, Folder.RETENTION_RULE_ENTERED);
				}
			} else {
				setFieldVisible(retentionRuleField, false, Folder.RETENTION_RULE_ENTERED);
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
		return decommissioningService().isCopyStatusInputPossible(folder);
	}

	void adjustStatusCopyEnteredField() {
		FolderCopyStatusEnteredField copyStatusEnteredField = (FolderCopyStatusEnteredField) view.getForm().getCustomField(
				Folder.COPY_STATUS_ENTERED);
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

	boolean isTransferDateInputPossibleForUser() {
		Folder folder = rmSchemas().wrapFolder(toRecord(folderVO));
		return decommissioningService().isTransferDateInputPossibleForUser(folder, getCurrentUser());
	}

	void adjustActualTransferDateField() {
		FolderActualTransferDateField actualTransferDateField = (FolderActualTransferDateField) view.getForm().getCustomField(
				Folder.ACTUAL_TRANSFER_DATE);
		if (actualTransferDateField != null) {
			if (isTransferDateInputPossibleForUser()) {
				if (!actualTransferDateField.isVisible()) {
					setFieldVisible(actualTransferDateField, true, Folder.ACTUAL_TRANSFER_DATE);
				}
			} else if (actualTransferDateField.isVisible()) {
				setFieldVisible(actualTransferDateField, false, Folder.ACTUAL_TRANSFER_DATE);
			}
		}
	}

	boolean isDepositDateInputPossibleForUser() {
		Folder folder = rmSchemas().wrapFolder(toRecord(folderVO));
		return decommissioningService().isDepositDateInputPossibleForUser(folder, getCurrentUser());
	}

	void adjustActualDepositDateField() {
		FolderActualDepositDateField actualDepositDateField = (FolderActualDepositDateField) view.getForm().getCustomField(
				Folder.ACTUAL_DEPOSIT_DATE);
		if (actualDepositDateField != null) {
			if (isDepositDateInputPossibleForUser()) {
				if (!actualDepositDateField.isVisible()) {
					setFieldVisible(actualDepositDateField, true, Folder.ACTUAL_DEPOSIT_DATE);
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

	void adjustActualDestructionDateField() {
		FolderActualDestructionDateField actualDestructionDateField = (FolderActualDestructionDateField) view.getForm()
				.getCustomField(Folder.ACTUAL_DESTRUCTION_DATE);
		if (actualDestructionDateField != null) {
			if (isDestructionDateInputPossibleForUser()) {
				if (!actualDestructionDateField.isVisible()) {
					setFieldVisible(actualDestructionDateField, true, Folder.ACTUAL_DESTRUCTION_DATE);
				}
			} else if (actualDestructionDateField.isVisible()) {
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

	private DecommissioningService decommissioningService() {
		return new DecommissioningService(collection, modelLayerFactory);
	}

	private RMSchemasRecordsServices rmSchemas() {
		return new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

}
