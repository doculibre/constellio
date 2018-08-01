package com.constellio.app.modules.rm.model.calculators.rule;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.ReferenceListMetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;

import java.util.*;

import static com.constellio.app.modules.rm.wrappers.RetentionRule.COPY_RETENTION_RULES;
import static java.util.Arrays.asList;

public class RuleYearTypesCalculator extends ReferenceListMetadataValueCalculator {

	LocalDependency<List<CopyRetentionRule>> copyRetentionRulesParam = LocalDependency
			.toAStructure(COPY_RETENTION_RULES).whichIsMultivalue();
	LocalDependency<CopyRetentionRule> documentDefaultPrincipalParam = LocalDependency
			.toAStructure(RetentionRule.PRINCIPAL_DEFAULT_DOCUMENT_COPY_RETENTION_RULE);
	LocalDependency<CopyRetentionRule> documentDefaultSecondaryParam = LocalDependency
			.toAStructure(RetentionRule.SECONDARY_DEFAULT_DOCUMENT_COPY_RETENTION_RULE);
	LocalDependency<List<CopyRetentionRule>> documentCopyRetentionRulesParam = LocalDependency
			.toAStructure(RetentionRule.DOCUMENT_COPY_RETENTION_RULES).whichIsMultivalue();

	@Override
	public List<String> calculate(CalculatorParameters parameters) {

		Set<String> typeIds = new HashSet<String>() {
			@Override
			public boolean add(String o) {
				if (o != null) {
					return super.add(o);
				} else {
					return false;
				}
			}
		};

		for (CopyRetentionRule copyRetentionRule : parameters.get(copyRetentionRulesParam)) {
			typeIds.add(copyRetentionRule.getSemiActiveYearTypeId());
			typeIds.add(copyRetentionRule.getInactiveYearTypeId());
		}

		for (CopyRetentionRule copyRetentionRule : parameters.get(documentCopyRetentionRulesParam)) {
			typeIds.add(copyRetentionRule.getSemiActiveYearTypeId());
			typeIds.add(copyRetentionRule.getInactiveYearTypeId());
		}

		CopyRetentionRule documentDefaultPrincipal = parameters.get(documentDefaultPrincipalParam);
		if (documentDefaultPrincipal != null) {
			typeIds.add(documentDefaultPrincipal.getSemiActiveYearTypeId());
			typeIds.add(documentDefaultPrincipal.getInactiveYearTypeId());
		}

		CopyRetentionRule documentDefaultSecondary = parameters.get(documentDefaultSecondaryParam);
		if (documentDefaultSecondary != null) {
			typeIds.add(documentDefaultSecondary.getSemiActiveYearTypeId());
			typeIds.add(documentDefaultSecondary.getInactiveYearTypeId());
		}

		List<String> returnedList = new ArrayList<>(typeIds);
		Collections.sort(returnedList);

		return returnedList;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(copyRetentionRulesParam, documentCopyRetentionRulesParam, documentDefaultPrincipalParam,
				documentDefaultSecondaryParam);
	}
}
