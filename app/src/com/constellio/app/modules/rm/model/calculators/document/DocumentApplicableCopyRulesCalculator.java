package com.constellio.app.modules.rm.model.calculators.document;

import static com.constellio.data.utils.LangUtils.withoutNulls;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleInRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class DocumentApplicableCopyRulesCalculator implements MetadataValueCalculator<List<CopyRetentionRuleInRule>> {

	ConfigDependency<Boolean> documentRetentionRulesEnabledParam = RMConfigs.DOCUMENT_RETENTION_RULES.dependency();
	ReferenceDependency<List<CopyRetentionRuleInRule>> rubricDocumentCopyRetentionRulesParam = ReferenceDependency.toAStructure(
			Document.FOLDER_CATEGORY, Category.COPY_RETENTION_RULES_ON_DOCUMENT_TYPES);
	ReferenceDependency<CopyType> copyTypeParam = ReferenceDependency.toAnEnum(Document.FOLDER, Folder.COPY_STATUS);

	ReferenceDependency<List<CopyRetentionRule>> documentCopyRetentionRulesParam = ReferenceDependency
			.toAStructure(Document.INHERITED_FOLDER_RETENTION_RULE, RetentionRule.DOCUMENT_COPY_RETENTION_RULES)
			.whichIsMultivalue();

	ReferenceDependency<CopyRetentionRule> inheritedDefaultDocumentPrincipalCopyRetentionRuleParam = ReferenceDependency
			.toAStructure(Document.INHERITED_FOLDER_RETENTION_RULE, RetentionRule.PRINCIPAL_DEFAULT_DOCUMENT_COPY_RETENTION_RULE);

	ReferenceDependency<CopyRetentionRule> inheritedDefaultDocumentSecondaryCopyRetentionRuleParam = ReferenceDependency
			.toAStructure(Document.INHERITED_FOLDER_RETENTION_RULE, RetentionRule.SECONDARY_DEFAULT_DOCUMENT_COPY_RETENTION_RULE);

	LocalDependency<String> documentTypeParam = LocalDependency.toAReference(Document.TYPE);
	LocalDependency<String> folderCategoryParam = LocalDependency.toAReference(Document.FOLDER_CATEGORY);
	ReferenceDependency<Double> folderCategoryLevelParam = ReferenceDependency
			.toANumber(Document.FOLDER_CATEGORY, Category.LEVEL);

	LocalDependency<String> inheritedFolderRetentionRuleParam = LocalDependency
			.toAReference(Document.INHERITED_FOLDER_RETENTION_RULE);

	ReferenceDependency<CopyRetentionRule> folderMainCopyRuleParam = ReferenceDependency
			.toAStructure(Document.FOLDER, Folder.MAIN_COPY_RULE);

	@Override
	public List<CopyRetentionRuleInRule> calculate(CalculatorParameters parameters) {

		CalculatorInput input = new CalculatorInput(parameters);
		CopyRetentionRuleInRule folderMainCopyRule = input.folderMainCopyRule;

		if (input.documentRetentionRulesEnabled) {
			return withoutNulls(findApplicableCopyRuleForDocument(input));
		} else {
			return withoutNulls(asList(folderMainCopyRule));
		}
	}

	private List<CopyRetentionRuleInRule> findApplicableCopyRuleForDocument(CalculatorInput input) {

		if (input.copyType == CopyType.SECONDARY) {
			CopyRetentionRuleInRule defaultInherited = input.inheritedDefaultDocumentSecondaryCopyRetentionRule;
			if (defaultInherited != null) {
				return asList(defaultInherited);

			}
		} else {
			List<CopyRetentionRuleInRule> copyRetentionRuleInRulesWithType = findCopyRetentionRulesWithType(input);
			CopyRetentionRuleInRule defaultInherited = input.inheritedDefaultDocumentPrincipalCopyRetentionRule;

			if (!copyRetentionRuleInRulesWithType.isEmpty()) {
				return copyRetentionRuleInRulesWithType;

			} else if (defaultInherited != null) {
				return asList(defaultInherited);

			}

		}

		return asList(input.folderMainCopyRule);
	}

	private List<CopyRetentionRuleInRule> findCopyRetentionRulesWithType(CalculatorInput input) {

		String documentType = input.documentType;
		int copiesFromDocumentRulesInCategoryHierarchyLevel = 0, copiesFromInheritedRuleLevel = 0;

		List<CopyRetentionRuleInRule> copiesFromDocumentRulesInCategoryHierarchy = new ArrayList<>();

		if (input.rubricDocumentCopies != null) {
			for (CopyRetentionRuleInRule copy : input.rubricDocumentCopies) {
				if (copy != null) {
					String copyDocumentTypeId = copy.getCopyRetentionRule().getDocumentTypeId();
					if (copyDocumentTypeId != null && copyDocumentTypeId.equals(documentType)) {
						copiesFromDocumentRulesInCategoryHierarchy.add(copy);
						copiesFromDocumentRulesInCategoryHierarchyLevel = copy.getCategoryLevel();
					}
				}
			}
		}

		List<CopyRetentionRuleInRule> copiesFromInheritedRule = new ArrayList<>();
		for (CopyRetentionRuleInRule copy : input.documentCopyRetentionRules) {
			String copyDocumentTypeId = copy.getCopyRetentionRule().getDocumentTypeId();
			if (copyDocumentTypeId != null && copyDocumentTypeId.equals(documentType)) {
				copiesFromInheritedRule.add(copy);
				copiesFromInheritedRuleLevel = copy.getCategoryLevel();
			}
		}

		if (!copiesFromInheritedRule.isEmpty() && !copiesFromDocumentRulesInCategoryHierarchy.isEmpty()) {
			return copiesFromInheritedRuleLevel > copiesFromDocumentRulesInCategoryHierarchyLevel ?
					copiesFromInheritedRule :
					copiesFromDocumentRulesInCategoryHierarchy;

		} else if (!copiesFromInheritedRule.isEmpty()) {
			return copiesFromInheritedRule;

		} else {
			return copiesFromDocumentRulesInCategoryHierarchy;
		}
	}

	@Override
	public List<CopyRetentionRuleInRule> getDefaultValue() {
		return null;
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
		return asList(documentRetentionRulesEnabledParam, rubricDocumentCopyRetentionRulesParam, copyTypeParam,
				inheritedDefaultDocumentPrincipalCopyRetentionRuleParam, inheritedDefaultDocumentSecondaryCopyRetentionRuleParam,
				documentTypeParam, inheritedFolderRetentionRuleParam, folderMainCopyRuleParam, documentCopyRetentionRulesParam,
				folderCategoryParam, folderCategoryLevelParam);
	}

	private class CalculatorInput {

		CopyRetentionRuleInRule folderMainCopyRule;
		boolean documentRetentionRulesEnabled;
		List<CopyRetentionRuleInRule> documentCopyRetentionRules;
		CopyRetentionRuleInRule inheritedDefaultDocumentSecondaryCopyRetentionRule;
		CopyRetentionRuleInRule inheritedDefaultDocumentPrincipalCopyRetentionRule;
		CopyType copyType;
		List<CopyRetentionRuleInRule> rubricDocumentCopies;
		String documentType;
		String inheritedFolderRetentionRule;
		String folderCategory;
		int folderCategoryLevel;

		public CalculatorInput(CalculatorParameters parameters) {
			this.inheritedFolderRetentionRule = parameters.get(inheritedFolderRetentionRuleParam);
			this.folderCategory = parameters.get(folderCategoryParam);
			this.folderCategoryLevel = toInt(parameters.get(folderCategoryLevelParam));
			this.folderMainCopyRule = toCopyInRule(parameters.get(folderMainCopyRuleParam));
			this.documentRetentionRulesEnabled = parameters.get(documentRetentionRulesEnabledParam);
			this.documentCopyRetentionRules = toCopiesInRules(parameters.get(documentCopyRetentionRulesParam));
			this.inheritedDefaultDocumentSecondaryCopyRetentionRule = toCopyInRule(
					parameters.get(inheritedDefaultDocumentSecondaryCopyRetentionRuleParam));
			this.inheritedDefaultDocumentPrincipalCopyRetentionRule = toCopyInRule(
					parameters.get(inheritedDefaultDocumentPrincipalCopyRetentionRuleParam));

			this.documentType = parameters.get(documentTypeParam);
			this.rubricDocumentCopies = parameters.get(rubricDocumentCopyRetentionRulesParam);
			this.copyType = parameters.get(copyTypeParam);

		}

		private int toInt(Double aDouble) {
			return aDouble == null ? 0 : aDouble.intValue();
		}

		private List<CopyRetentionRuleInRule> toCopiesInRules(List<CopyRetentionRule> copyRetentionRules) {
			return toCopiesInRules(copyRetentionRules, inheritedFolderRetentionRule, folderCategory, folderCategoryLevel);
		}

		private CopyRetentionRuleInRule toCopyInRule(CopyRetentionRule copyRetentionRule) {
			return toCopyInRule(copyRetentionRule, inheritedFolderRetentionRule, folderCategory, folderCategoryLevel);
		}

		private CopyRetentionRuleInRule toCopyInRule(CopyRetentionRule copyRetentionRule, String retentionRule,
				String category, int categoryLevel) {
			if (copyRetentionRule == null) {
				return null;
			} else {
				return copyRetentionRule.in(retentionRule, category, categoryLevel);
			}
		}

		private List<CopyRetentionRuleInRule> toCopiesInRules(List<CopyRetentionRule> copyRetentionRules, String retentionRule,
				String category, int categoryLevel) {

			List<CopyRetentionRuleInRule> copyRetentionRuleInRules = new ArrayList<>();

			for (CopyRetentionRule copyRetentionRule : copyRetentionRules) {
				copyRetentionRuleInRules.add(copyRetentionRule.in(retentionRule, category, categoryLevel));
			}

			return copyRetentionRuleInRules;
		}

	}
}
