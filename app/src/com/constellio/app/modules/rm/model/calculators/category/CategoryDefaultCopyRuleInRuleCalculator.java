package com.constellio.app.modules.rm.model.calculators.category;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.apache.poi.ss.formula.functions.T;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleInRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public abstract class CategoryDefaultCopyRuleInRuleCalculator<T> implements MetadataValueCalculator<T> {

	LocalDependency<String> copyRuleIdDependency = LocalDependency.toAString(Category.DEFAULT_COPY_RULE_ID).whichIsRequired();
	ReferenceDependency<SortedMap<String, List<CopyRetentionRule>>> copyRulesDependency = ReferenceDependency.toAStructure(
			Category.RETENTION_RULES, RetentionRule.COPY_RETENTION_RULES).whichAreReferencedMultiValueGroupedByReference();

	public CopyRetentionRuleInRule getCopyRetentionRule(CalculatorParameters parameters) {
		CategoryDefaultRetentionRuleCalculatorInput input = new CategoryDefaultRetentionRuleCalculatorInput(parameters);

		for (Entry<String, List<CopyRetentionRule>> copyRule : input.copyRules.entrySet()) {
			String ruleId = copyRule.getKey();
			List<CopyRetentionRule> copies = copyRule.getValue();

			for (CopyRetentionRule copy : copies) {
				if (copy != null && copy.getCopyType() == CopyType.PRINCIPAL && copy.getId() != null && copy.getId()
						.equals(input.copyRuleId)) {
					//Level is not important, sice the returned object is not persisted, but only used by
					// other calculators which are not using the level attribute
					return new CopyRetentionRuleInRule(ruleId, parameters.getId(), 0, copy);
				}
			}

		}

		return null;
	}

	@Override
	public T getDefaultValue() {
		return null;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(copyRuleIdDependency, copyRulesDependency);
	}

	private class CategoryDefaultRetentionRuleCalculatorInput {
		String copyRuleId;
		SortedMap<String, List<CopyRetentionRule>> copyRules;

		public CategoryDefaultRetentionRuleCalculatorInput(CalculatorParameters parameters) {
			this.copyRuleId = parameters.get(copyRuleIdDependency);
			this.copyRules = parameters.get(copyRulesDependency);
		}
	}

	public static class CategoryDefaultRetentionRuleCalculator extends CategoryDefaultCopyRuleInRuleCalculator<String>
			implements MetadataValueCalculator<String> {

		@Override
		public String calculate(CalculatorParameters parameters) {
			CopyRetentionRuleInRule rule = getCopyRetentionRule(parameters);
			return rule == null ? null : rule.getRuleId();
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.REFERENCE;
		}

	}

	public static class CategoryDefaultCopyRuleCalculator extends CategoryDefaultCopyRuleInRuleCalculator<CopyRetentionRule>
			implements MetadataValueCalculator<CopyRetentionRule> {

		@Override
		public CopyRetentionRule calculate(CalculatorParameters parameters) {
			CopyRetentionRuleInRule rule = getCopyRetentionRule(parameters);
			return rule == null ? null : rule.getCopyRetentionRule();
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.STRUCTURE;
		}

	}
}
