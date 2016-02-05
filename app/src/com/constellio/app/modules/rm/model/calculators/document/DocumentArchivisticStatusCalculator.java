package com.constellio.app.modules.rm.model.calculators.document;

import static com.constellio.app.modules.rm.wrappers.Document.FOLDER_ACTUAL_DEPOSIT_DATE;
import static com.constellio.app.modules.rm.wrappers.Document.FOLDER_ACTUAL_DESTRUCTION_DATE;
import static com.constellio.app.modules.rm.wrappers.Document.FOLDER_ACTUAL_TRANSFER_DATE;
import static java.util.Arrays.asList;

import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class DocumentArchivisticStatusCalculator implements MetadataValueCalculator<FolderStatus> {

	ConfigDependency<Boolean> documentRetentionRulesEnabledParam = RMConfigs.DOCUMENT_RETENTION_RULES.dependency();

	LocalDependency<LocalDate> folderActualTransferDateParam = LocalDependency.toADate(FOLDER_ACTUAL_TRANSFER_DATE);
	LocalDependency<LocalDate> folderActualDepositDateParam = LocalDependency.toADate(FOLDER_ACTUAL_DEPOSIT_DATE);
	LocalDependency<LocalDate> folderActualDestructionDateParam = LocalDependency.toADate(FOLDER_ACTUAL_DESTRUCTION_DATE);

	ReferenceDependency<FolderStatus> folderStatusParam = ReferenceDependency
			.toAnEnum(Document.FOLDER, Folder.ARCHIVISTIC_STATUS);

	@Override
	public FolderStatus calculate(CalculatorParameters parameters) {

		CalculatorInput input = new CalculatorInput(parameters);

		if (input.documentRetentionRulesEnabled) {
			if (input.folderActualDepositDate != null) {
				return FolderStatus.INACTIVE_DEPOSITED;

			} else if (input.folderActualDestructionDate != null) {
				return FolderStatus.INACTIVE_DESTROYED;

			} else if (input.folderActualTransferDate != null) {
				return FolderStatus.SEMI_ACTIVE;

			} else {
				return FolderStatus.ACTIVE;
			}
		} else {
			return input.folderStatus;
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
				folderActualDestructionDateParam, documentRetentionRulesEnabledParam);
	}

	class CalculatorInput {

		boolean documentRetentionRulesEnabled;

		LocalDate folderActualTransferDate, folderActualDepositDate, folderActualDestructionDate;

		FolderStatus folderStatus;

		CalculatorInput(CalculatorParameters parameters) {
			this.documentRetentionRulesEnabled = parameters.get(documentRetentionRulesEnabledParam);
			this.folderActualTransferDate = parameters.get(folderActualTransferDateParam);
			this.folderActualDepositDate = parameters.get(folderActualDepositDateParam);
			this.folderActualDestructionDate = parameters.get(folderActualDestructionDateParam);
			this.folderStatus = parameters.get(folderStatusParam);
		}

	}
}
