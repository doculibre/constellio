package com.constellio.app.modules.rm.model.calculators.rule.util;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class RetentionRuleCalculatorUtil {
	public static void calculateNewFolderMetadatas(Folder folder, RMSchemasRecordsServices rm) {
		AtomicInteger longest = new AtomicInteger(0);
		BiConsumer<RetentionRule, CopyRetentionRule> availableCopyConsumer = (rule, copy) -> {
			int value = copy == null ? 0 : copy.getActiveRetentionPeriod().getValue() + copy.getSemiActiveRetentionPeriod().getValue();
			if (value > longest.get()) {
				longest.set(value);
				folder.setRetentionRuleEntered(rule);
				// longest is used
				folder.setCopyStatusEntered(rule.isResponsibleAdministrativeUnits() ? CopyType.PRINCIPAL : null);
			}
		};

		Category category = rm.getCategory(folder.getCategoryEntered());
		for (RetentionRule rule : rm.getRetentionRules(category.getRententionRules())) {
			if (rule.isResponsibleAdministrativeUnits() || rule.getAdministrativeUnits().contains(folder.getAdministrativeUnitEntered())) {
				rule.getPrincipalCopies().forEach((copy) -> availableCopyConsumer.accept(rule, copy));
			} else {
				availableCopyConsumer.accept(rule, rule.getSecondaryCopy());
			}
		}
	}
}
