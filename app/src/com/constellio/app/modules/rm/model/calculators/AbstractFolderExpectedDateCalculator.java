package com.constellio.app.modules.rm.model.calculators;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CalculatorWithManualMetadataChoice;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public abstract class AbstractFolderExpectedDateCalculator implements MetadataValueCalculator<LocalDate> {

	LocalDependency<List<CopyRetentionRule>> applicableCopyRulesParam = LocalDependency
			.toAStructure(Folder.APPLICABLE_COPY_RULES).whichIsMultivalue().whichIsRequired();

	LocalDependency<CopyRetentionRule> mainCopyRuleParam = LocalDependency
			.toAStructure(Folder.MAIN_COPY_RULE).whichIsRequired();

	ConfigDependency<CalculatorWithManualMetadataChoice> manualMetadataChoiceConfigDependency
			= RMConfigs.ARCHIVISTIC_CALCULATORS_WITH_MANUAL_METADATA.dependency();

	@Override
	public LocalDate calculate(CalculatorParameters parameters) {
		CalculatorWithManualMetadataChoice manualMetadataChoice = parameters.get(manualMetadataChoiceConfigDependency);
		if (manualMetadataChoice == null || manualMetadataChoice == CalculatorWithManualMetadataChoice.DISABLE) {
			return calculateWithoutConsideringManualMetadata(parameters);
		} else {
			LocalDate manualMetadata = parameters.get(getManualDateDependency());
			if (manualMetadata == null) {
				return calculateWithoutConsideringManualMetadata(parameters);
			} else {
				return manualMetadata;
			}
		}
	}

	private LocalDate calculateWithoutConsideringManualMetadata(CalculatorParameters parameters) {
		List<LocalDate> dates = parameters.get(getDatesDependency());
		List<CopyRetentionRule> applicableCopyRules = parameters.get(applicableCopyRulesParam);
		CopyRetentionRule mainCopyRule = parameters.get(mainCopyRuleParam);

		int index = applicableCopyRules.indexOf(mainCopyRule);
		if (index == 0 && dates.isEmpty()) {
			return null;
		} else {
			return !dates.isEmpty() ? dates.get(index) : null;
		}
	}

	abstract LocalDependency<List<LocalDate>> getDatesDependency();

	@Override
	public LocalDate getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.DATE;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(manualMetadataChoiceConfigDependency, applicableCopyRulesParam, mainCopyRuleParam, getDatesDependency(), getManualDateDependency());
	}

	protected abstract LocalDependency<LocalDate> getManualDateDependency();

}
