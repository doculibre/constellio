package com.constellio.app.modules.rm.model.calculators.document;

import static java.util.Arrays.asList;

import java.util.List;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.CopyRetentionRuleInRule;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class DocumentRetentionRuleCalculator implements MetadataValueCalculator<String> {

	ConfigDependency<Boolean> documentRetentionRulesEnabledParam = RMConfigs.DOCUMENT_RETENTION_RULES.dependency();

	ReferenceDependency<String> folderRetentionRuleParam = ReferenceDependency
			.toAReference(Document.FOLDER, Folder.RETENTION_RULE);

	LocalDependency<List<CopyRetentionRuleInRule>> applicableCopyRulesParam = LocalDependency
			.toAStructure(Document.APPLICABLE_COPY_RULES)
			.whichIsMultivalue();

	@Override
	public String calculate(CalculatorParameters parameters) {
		CalculatorInput input = new CalculatorInput(parameters);

		if (input.documentRetentionRulesEnabled && !input.applicableCopyRules.isEmpty()) {
			return input.applicableCopyRules.get(0).getRuleId();
		} else {
			return input.folderRetentionRule;
		}
	}

	@Override
	public String getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.REFERENCE;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(folderRetentionRuleParam, documentRetentionRulesEnabledParam, applicableCopyRulesParam);
	}

	class CalculatorInput {
		Boolean documentRetentionRulesEnabled;

		String folderRetentionRule;

		List<CopyRetentionRuleInRule> applicableCopyRules;

		CalculatorInput(CalculatorParameters parameters) {
			documentRetentionRulesEnabled = parameters.get(documentRetentionRulesEnabledParam);
			folderRetentionRule = parameters.get(folderRetentionRuleParam);
			applicableCopyRules = parameters.get(applicableCopyRulesParam);
		}

	}
}
