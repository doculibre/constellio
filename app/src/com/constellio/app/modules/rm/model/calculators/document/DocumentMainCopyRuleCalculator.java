package com.constellio.app.modules.rm.model.calculators.document;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleInRule;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.List;

import static java.util.Arrays.asList;

public class DocumentMainCopyRuleCalculator extends AbstractMetadataValueCalculator<CopyRetentionRule> {

	ConfigDependency<Boolean> documentRetentionRulesEnabledParam = RMConfigs.DOCUMENT_RETENTION_RULES.dependency();

	ReferenceDependency<CopyRetentionRule> folderMainCopyRuleParam = ReferenceDependency
			.toAStructure(Document.FOLDER, Folder.MAIN_COPY_RULE);

	LocalDependency<List<CopyRetentionRuleInRule>> applicableCopyRulesParam = LocalDependency
			.toAStructure(Document.APPLICABLE_COPY_RULES)
			.whichIsMultivalue();

	@Override
	public CopyRetentionRule calculate(CalculatorParameters parameters) {

		CalculatorInput input = new CalculatorInput(parameters);

		if (input.documentRetentionRulesEnabled && !input.applicableCopyRules.isEmpty()) {
			return input.applicableCopyRules.get(0).getCopyRetentionRule();
		} else {
			return input.folderMainCopyRule;
		}

	}

	@Override
	public CopyRetentionRule getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.STRUCTURE;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(folderMainCopyRuleParam, documentRetentionRulesEnabledParam, applicableCopyRulesParam);
	}

	private class CalculatorInput {

		boolean documentRetentionRulesEnabled;
		CopyRetentionRule folderMainCopyRule;
		List<CopyRetentionRuleInRule> applicableCopyRules;

		public CalculatorInput(CalculatorParameters parameters) {
			documentRetentionRulesEnabled = parameters.get(documentRetentionRulesEnabledParam);
			folderMainCopyRule = parameters.get(folderMainCopyRuleParam);
			applicableCopyRules = parameters.get(applicableCopyRulesParam);
		}

	}
}
