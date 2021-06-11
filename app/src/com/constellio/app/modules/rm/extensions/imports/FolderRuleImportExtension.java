package com.constellio.app.modules.rm.extensions.imports;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMObject;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.RecordImportExtension;
import com.constellio.model.extensions.events.recordsImport.BuildParams;
import com.constellio.model.extensions.events.recordsImport.PrevalidationParams;
import com.constellio.model.extensions.events.recordsImport.ValidationParams;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.HashMap;
import java.util.Map;

public class FolderRuleImportExtension extends RecordImportExtension {

	private static final String ACTIVE_WITH_TRANSFERT_DATE = "activeWithTransfertDate";
	private static final String ACTIVE_WITH_DEPOSIT_DATE = "activeWithDepositDate";
	private static final String ACTIVE_WITH_DESTRUCTION_DATE = "activeWithDestructionDate";
	private static final String SEMI_ACTIVE_WITH_DEPOSIT_DATE = "semiActiveWithDepositDate";
	private static final String SEMI_ACTIVE_WITH_DESTRUCTION_DATE = "semiActiveWithDestructionDate";
	private static final String SEMI_ACTIVE_WITHOUT_TRANSFERT_DATE = "semiActiveWithoutTransfertDate";
	private static final String DESTROYED_WITH_DEPOSIT_DATE = "destroyedWithDepositDate";
	private static final String DESTROYED_WITHOUT_DESTRUCTION_DATE = "destroyedWithoutDestructionDate";
	private static final String DEPOSITED_WITH_DESTRUCTION_DATE = "depositedWithDestructionDate";
	private static final String DEPOSITED_WITHOUT_DEPOSIT_DATE = "depositedWithoutDepositDate";
	public static boolean ajustManualDepositAndDestructionDates = true;

	RMSchemasRecordsServices rm;

	public FolderRuleImportExtension(String collection, ModelLayerFactory modelLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

	@Override
	public String getDecoratedSchemaType() {
		return Folder.SCHEMA_TYPE;
	}

	@Override
	public void build(BuildParams event) {
		Map<String, Object> fields = event.getImportRecord().getFields();
		Folder folder = rm.wrapFolder(event.getRecord());
		ajustCreationModificationDates(fields, folder);
		setEnteredMainCopyId(folder);
		autoCalculateDates(folder, event);
		if (ajustManualDepositAndDestructionDates) {
			ajusteManualDepositAndDestructionDates(folder);
		}
	}

	@Override
	public void validate(ValidationParams event) {
		ensureStatus(event);
	}

	@Override
	public ExtensionBooleanResult skipPrevalidation(PrevalidationParams event) {
		Map<String, Object> fields = event.getImportRecord().getFields();
		Object isModel = fields.get(Folder.IS_MODEL);
		if (isModel != null) {
			return skipValidations((String) isModel);
		}
		return super.skipPrevalidation(event);
	}

	@Override
	public ExtensionBooleanResult skipValidation(ValidationParams event) {
		Map<String, Object> fields = event.getImportRecord().getFields();
		Object isModel = fields.get(Folder.IS_MODEL);
		if (isModel != null) {
			return skipValidations((String) isModel);
		}
		return super.skipValidation(event);
	}

	private ExtensionBooleanResult skipValidations(String isModel) {
		return "true".equals(isModel) ? ExtensionBooleanResult.FORCE_TRUE : ExtensionBooleanResult.FALSE;
	}

	private void ensureStatus(ValidationParams event) {
		ValidationErrors errors = event.getErrors();
		Map<String, Object> fields = event.getImportRecord().getFields();
		String folderId = event.getImportRecord().getLegacyId();
		Object transfertDate = fields.get(Folder.ACTUAL_TRANSFER_DATE);
		Object destructionDate = fields.get(Folder.ACTUAL_DESTRUCTION_DATE);
		Object depositDate = fields.get(Folder.ACTUAL_DEPOSIT_DATE);
		String ensureStatus = event.getImportRecord().getOption("ensureStatus");
		if (ensureStatus != null) {
			switch (ensureStatus) {
				case "a":
					if (transfertDate != null) {
						errors.add(FolderRuleImportExtension.class, ACTIVE_WITH_TRANSFERT_DATE, asMap("folderId", folderId));
					}
					if (depositDate != null) {
						errors.add(FolderRuleImportExtension.class, ACTIVE_WITH_DEPOSIT_DATE, asMap("folderId", folderId));
					}
					if (destructionDate != null) {
						errors.add(FolderRuleImportExtension.class, ACTIVE_WITH_DESTRUCTION_DATE, asMap("folderId", folderId));
					}
					break;
				case "s":
					if (depositDate != null) {
						errors.add(FolderRuleImportExtension.class, SEMI_ACTIVE_WITH_DEPOSIT_DATE, asMap("folderId", folderId));
					}
					if (destructionDate != null) {
						errors.add(FolderRuleImportExtension.class, SEMI_ACTIVE_WITH_DESTRUCTION_DATE, asMap("folderId", folderId));
					}
					if (transfertDate == null) {
						errors.addWarning(FolderRuleImportExtension.class, SEMI_ACTIVE_WITHOUT_TRANSFERT_DATE, asMap("folderId", folderId));
					}
					break;
				case "d":
					if (depositDate != null) {
						errors.add(FolderRuleImportExtension.class, DESTROYED_WITH_DEPOSIT_DATE, asMap("folderId", folderId));
					}
					if (destructionDate == null) {
						errors.addWarning(FolderRuleImportExtension.class, DESTROYED_WITHOUT_DESTRUCTION_DATE, asMap("folderId", folderId));
					}
					break;
				case "v":
					if (destructionDate != null) {
						errors.add(FolderRuleImportExtension.class, DEPOSITED_WITH_DESTRUCTION_DATE, asMap("folderId", folderId));
					}
					if (depositDate == null) {
						errors.addWarning(FolderRuleImportExtension.class, DEPOSITED_WITHOUT_DEPOSIT_DATE, asMap("folderId", folderId));
					}
					break;
			}
		}
	}

	private Map<String, Object> asMap(String key1, String value1) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(key1, value1);
		return parameters;
	}

	private void autoCalculateDates(Folder folder, BuildParams event) {
		String autoSetStatusTo = event.getImportRecord().getOption("autoSetStatusTo");
		if (autoSetStatusTo != null) {
			rm.getModelLayerFactory().newRecordServices().recalculate(folder);

			if (FolderStatus.SEMI_ACTIVE.getCode().equals(autoSetStatusTo.toLowerCase())) {
				folder.setActualTransferDate(folder.getExpectedTransferDate());

			} else if (FolderStatus.INACTIVE_DEPOSITED.getCode().equals(autoSetStatusTo.toLowerCase())) {
				folder.setActualTransferDate(folder.getExpectedTransferDate());
				folder.setActualDepositDate(folder.getExpectedDepositDate());

			} else if (FolderStatus.INACTIVE_DESTROYED.getCode().equals(autoSetStatusTo.toLowerCase())) {
				folder.setActualTransferDate(folder.getExpectedTransferDate());
				folder.setActualDestructionDate(folder.getExpectedDestructionDate());
			}

		}
	}

	private void ajusteManualDepositAndDestructionDates(Folder folder) {
		LocalDate manualExpectedDeposit = folder.getManualExpectedDepositDate();
		LocalDate manualExpectedDestruction = folder.getManualExpectedDestructionDate();
		LocalDate actualTransfer = folder.getActualTransferDate();
		LocalDate manualExpectedTransfer = folder.getManualExpecteTransferdDate();

		Map<String, Object> errorParams = new HashMap<>();
		errorParams.put("idTitle", folder.getId() + "-" + folder.getTitle());

		if (manualExpectedDeposit != null && actualTransfer != null && manualExpectedDeposit.isBefore(actualTransfer)) {
			folder.setManualExpectedDepositDate(null);
		}

		if (manualExpectedDeposit != null && manualExpectedTransfer != null
			&& manualExpectedDeposit.isBefore(manualExpectedTransfer)) {
			folder.setManualExpectedDepositDate(null);
		}
		if (manualExpectedDestruction != null && actualTransfer != null
			&& manualExpectedDestruction.isBefore(actualTransfer)) {
			folder.setManualExpectedDestructionDate(null);
		}

		if (manualExpectedDestruction != null && manualExpectedTransfer != null
			&& manualExpectedDestruction.isBefore(manualExpectedTransfer)) {
			folder.setManualExpectedDestructionDate(null);
		}

	}

	private void setEnteredMainCopyId(Folder folder) {
		String enteredMainCopyId = folder.getMainCopyRuleIdEntered();
		if (enteredMainCopyId != null && folder.getRetentionRuleEntered() != null) {
			RetentionRule rule = rm.getRetentionRule(folder.getRetentionRuleEntered());

			String validId = null;
			for (CopyRetentionRule copy : rule.getCopyRetentionRules()) {
				if (copy.getId().equals(enteredMainCopyId) || (copy.getCode() != null && copy.getCode()
						.equals(enteredMainCopyId))) {
					validId = copy.getId();
					break;
				}
			}
			folder.setMainCopyRuleEntered(validId);
		}
	}

	private void ajustCreationModificationDates(Map<String, Object> fields, Folder folder) {


		String createdBy = (String) fields.get(Schemas.CREATED_BY.getLocalCode());
		if (createdBy != null && !fields.containsKey(RMObject.FORM_CREATED_BY)) {
			folder.setFormCreatedBy(folder.getCreatedBy());
		}

		LocalDateTime createdOn = (LocalDateTime) fields.get(Schemas.CREATED_ON.getLocalCode());

		if (createdOn != null && !fields.containsKey(RMObject.FORM_CREATED_ON)) {
			folder.setFormCreatedOn(createdOn);
		}

		String modifiedBy = (String) fields.get(Schemas.MODIFIED_BY.getLocalCode());
		if (modifiedBy != null && !fields.containsKey(RMObject.FORM_MODIFIED_BY)) {
			folder.setFormModifiedBy(folder.getModifiedBy());
		}

		LocalDateTime modifiedOn = (LocalDateTime) fields.get(Schemas.MODIFIED_ON.getLocalCode());

		if (modifiedOn != null && !fields.containsKey(RMObject.FORM_MODIFIED_ON)) {
			folder.setFormModifiedOn(modifiedOn);
		}
	}

}
