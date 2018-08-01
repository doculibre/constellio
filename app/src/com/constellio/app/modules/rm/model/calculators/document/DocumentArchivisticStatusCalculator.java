package com.constellio.app.modules.rm.model.calculators.document;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.model.enums.RetentionRuleScope;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import org.joda.time.LocalDate;

import java.util.List;

import static com.constellio.app.modules.rm.model.enums.RetentionRuleScope.DOCUMENTS;
import static com.constellio.app.modules.rm.wrappers.Document.*;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.SCOPE;
import static java.util.Arrays.asList;

public class DocumentArchivisticStatusCalculator implements MetadataValueCalculator<FolderStatus> {

	ConfigDependency<Boolean> documentRetentionRulesEnabledParam = RMConfigs.DOCUMENT_RETENTION_RULES.dependency();

	LocalDependency<LocalDate> folderActualTransferDateParam = LocalDependency.toADate(FOLDER_ACTUAL_TRANSFER_DATE);
	LocalDependency<LocalDate> folderActualDepositDateParam = LocalDependency.toADate(FOLDER_ACTUAL_DEPOSIT_DATE);
	LocalDependency<LocalDate> folderActualDestructionDateParam = LocalDependency.toADate(FOLDER_ACTUAL_DESTRUCTION_DATE);

	ReferenceDependency<FolderStatus> folderStatusParam = ReferenceDependency
			.toAnEnum(Document.FOLDER, Folder.ARCHIVISTIC_STATUS);

	LocalDependency<String> documentTypeParam = LocalDependency.toAReference(Document.TYPE);
	ReferenceDependency<RetentionRuleScope> retentionRuleScopeParam = ReferenceDependency
			.toAnEnum(Document.FOLDER_RETENTION_RULE, SCOPE);

	@Override
	public FolderStatus calculate(CalculatorParameters parameters) {

		CalculatorInput input = new CalculatorInput(parameters);

		//if (!input.documentRetentionRulesEnabled) {
		if (!input.documentRetentionRulesEnabled || (input.retentionRuleScope != DOCUMENTS && input.documentType == null)) {
			return input.folderStatus;
		} else {
			if (input.folderActualDepositDate != null) {
				return FolderStatus.INACTIVE_DEPOSITED;

			} else if (input.folderActualDestructionDate != null) {
				return FolderStatus.INACTIVE_DESTROYED;

			} else if (input.folderActualTransferDate != null) {
				return FolderStatus.SEMI_ACTIVE;

			} else {
				return FolderStatus.ACTIVE;
			}
		}

	}

	@Override
	public FolderStatus getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.ENUM;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(folderStatusParam, folderActualTransferDateParam, folderActualDepositDateParam,
				folderActualDestructionDateParam, documentRetentionRulesEnabledParam, documentTypeParam, retentionRuleScopeParam);
	}

	class CalculatorInput {

		boolean documentRetentionRulesEnabled;

		LocalDate folderActualTransferDate, folderActualDepositDate, folderActualDestructionDate;

		FolderStatus folderStatus;

		RetentionRuleScope retentionRuleScope;
		String documentType;

		CalculatorInput(CalculatorParameters parameters) {
			this.documentRetentionRulesEnabled = parameters.get(documentRetentionRulesEnabledParam);
			this.folderActualTransferDate = parameters.get(folderActualTransferDateParam);
			this.folderActualDepositDate = parameters.get(folderActualDepositDateParam);
			this.folderActualDestructionDate = parameters.get(folderActualDestructionDateParam);
			this.folderStatus = parameters.get(folderStatusParam);
			this.retentionRuleScope = parameters.get(retentionRuleScopeParam);
			this.documentType = parameters.get(documentTypeParam);
		}

	}
}
