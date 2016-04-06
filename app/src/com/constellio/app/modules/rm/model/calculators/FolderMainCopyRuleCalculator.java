package com.constellio.app.modules.rm.model.calculators;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.calculators.folder.FolderMainCopyRuleCalculator2;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;

public class FolderMainCopyRuleCalculator extends FolderMainCopyRuleCalculator2 implements
																				MetadataValueCalculator<CopyRetentionRule> {

	LocalDependency<List<CopyRetentionRule>> copyRulesParam = LocalDependency.toAStructure(Folder.APPLICABLE_COPY_RULES)
			.whichIsRequired();

	LocalDependency<List<LocalDate>> expectedDestructionDatesParam = LocalDependency
			.toADate(Folder.COPY_RULES_EXPECTED_DESTRUCTION_DATES).whichIsMultivalue().whichIsRequired();

	LocalDependency<List<LocalDate>> expectedDepositDatesParam = LocalDependency
			.toADate(Folder.COPY_RULES_EXPECTED_DEPOSIT_DATES).whichIsMultivalue().whichIsRequired();

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(copyRulesParam, expectedDestructionDatesParam, expectedDepositDatesParam);
	}
}
