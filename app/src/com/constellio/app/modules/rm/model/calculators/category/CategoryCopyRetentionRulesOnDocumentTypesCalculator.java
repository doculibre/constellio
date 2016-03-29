package com.constellio.app.modules.rm.model.calculators.category;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleInRule;
import com.constellio.app.modules.rm.model.enums.RetentionRuleScope;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class CategoryCopyRetentionRulesOnDocumentTypesCalculator
		implements MetadataValueCalculator<List<CopyRetentionRuleInRule>> {

	SpecialDependency<String> idParam = SpecialDependencies.IDENTIFIER;
	LocalDependency<Double> levelParam = LocalDependency.toANumber(Category.LEVEL);

	ReferenceDependency<List<CopyRetentionRuleInRule>> parentCopyRetentionRuleParam = ReferenceDependency.toAStructure(
			Category.PARENT, Category.COPY_RETENTION_RULES_ON_DOCUMENT_TYPES).whichIsMultivalue();

	ReferenceDependency<SortedMap<String, List<CopyRetentionRule>>> copyRetentionRulesParam = ReferenceDependency
			.toAStructure(Category.RETENTION_RULES, RetentionRule.DOCUMENT_COPY_RETENTION_RULES)
			.whichAreReferencedMultiValueGroupedByReference();

	ReferenceDependency<SortedMap<String, RetentionRuleScope>> scopesParam = ReferenceDependency
			.toAnEnum(Category.RETENTION_RULES, RetentionRule.SCOPE)
			.whichAreReferencedSingleValueGroupedByReference();

	@Override
	public List<CopyRetentionRuleInRule> calculate(CalculatorParameters parameters) {

		CalculatorInput input = new CalculatorInput(parameters);

		List<CopyRetentionRuleInRule> retentionRuleInRuleOfCategory = getRetentionRuleOnDocumentTypeInRuleOfCategory(input);
		Set<String> documentTypesWithRetentionRule = getDocumentTypesWithRetentionRule(retentionRuleInRuleOfCategory);

		for (CopyRetentionRuleInRule parentCopyRetentionRule : input.parentCopyRetentionRules) {
			String documentType = parentCopyRetentionRule.getCopyRetentionRule().getTypeId();
			if (documentType != null && !documentTypesWithRetentionRule.contains(documentType)) {
				retentionRuleInRuleOfCategory.add(parentCopyRetentionRule);
			}
		}

		return retentionRuleInRuleOfCategory;
	}

	private Set<String> getDocumentTypesWithRetentionRule(List<CopyRetentionRuleInRule> copyRetentionRuleInRules) {
		Set<String> documentTypes = new HashSet<>();

		for (CopyRetentionRuleInRule copyRetentionRuleInRule : copyRetentionRuleInRules) {
			if (copyRetentionRuleInRule.getCopyRetentionRule().getTypeId() != null) {
				documentTypes.add(copyRetentionRuleInRule.getCopyRetentionRule().getTypeId());
			}
		}

		return documentTypes;
	}

	private List<CopyRetentionRuleInRule> getRetentionRuleOnDocumentTypeInRuleOfCategory(CalculatorInput input) {
		List<CopyRetentionRuleInRule> copyRetentionRuleInRules = new ArrayList<>();

		for (Map.Entry<String, List<CopyRetentionRule>> copiesInRule : input.copyRetentionRules.entrySet()) {
			String ruleId = copiesInRule.getKey();
			RetentionRuleScope ruleScope = input.scopes.get(ruleId);
			if (RetentionRuleScope.DOCUMENTS == ruleScope) {
				for (CopyRetentionRule copyRetentionRule : copiesInRule.getValue()) {
					if (copyRetentionRule.getTypeId() != null) {
						copyRetentionRuleInRules.add(copyRetentionRule.in(ruleId, input.id, input.level));
					}
				}
			}
		}
		return copyRetentionRuleInRules;
	}

	@Override
	public List<CopyRetentionRuleInRule> getDefaultValue() {
		return new ArrayList<>();
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.STRUCTURE;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(parentCopyRetentionRuleParam, copyRetentionRulesParam, idParam, levelParam, scopesParam);
	}

	private class CalculatorInput {

		CalculatorParameters parameters;
		String id;
		int level;
		List<CopyRetentionRuleInRule> parentCopyRetentionRules;
		SortedMap<String, List<CopyRetentionRule>> copyRetentionRules;
		SortedMap<String, RetentionRuleScope> scopes;

		public CalculatorInput(CalculatorParameters parameters) {
			this.parameters = parameters;
			this.id = parameters.get(idParam);
			this.level = toInt(parameters.get(levelParam));
			this.parentCopyRetentionRules = parameters.get(parentCopyRetentionRuleParam);
			this.copyRetentionRules = parameters.get(copyRetentionRulesParam);
			this.scopes = parameters.get(scopesParam);
		}

		private int toInt(Double aDouble) {
			return aDouble == null ? 0 : aDouble.intValue();
		}

	}
}
