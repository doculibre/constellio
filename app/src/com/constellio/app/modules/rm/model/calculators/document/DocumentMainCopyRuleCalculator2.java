package com.constellio.app.modules.rm.model.calculators.document;

import static java.util.Arrays.asList;

import java.util.List;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
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

public class DocumentMainCopyRuleCalculator2 implements MetadataValueCalculator<CopyRetentionRule> {

	LocalDependency<String> enteredCopyRuleParam = LocalDependency.toAString(Folder.MAIN_COPY_RULE_ID_ENTERED);

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
			if (input.applicableCopyRules.size() == 1) {
				return input.applicableCopyRules.get(0).getCopyRetentionRule();
			} else {
				if (input.enteredCopyRule == null) {
					return null;
				} else {
					for (CopyRetentionRuleInRule inRule : input.applicableCopyRules) {
						if (input.enteredCopyRule.equals(inRule.getCopyRetentionRule().getId())) {
							return inRule.getCopyRetentionRule();
						}

					}
					return null;
				}

			}
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
		return asList(enteredCopyRuleParam, folderMainCopyRuleParam, documentRetentionRulesEnabledParam,
				applicableCopyRulesParam);
	}

	private class CalculatorInput {

		String enteredCopyRule;
		boolean documentRetentionRulesEnabled;
		CopyRetentionRule folderMainCopyRule;
		List<CopyRetentionRuleInRule> applicableCopyRules;

		public CalculatorInput(CalculatorParameters parameters) {
			enteredCopyRule = parameters.get(enteredCopyRuleParam);
			documentRetentionRulesEnabled = parameters.get(documentRetentionRulesEnabledParam);
			folderMainCopyRule = parameters.get(folderMainCopyRuleParam);
			applicableCopyRules = parameters.get(applicableCopyRulesParam);
		}

	}
}
