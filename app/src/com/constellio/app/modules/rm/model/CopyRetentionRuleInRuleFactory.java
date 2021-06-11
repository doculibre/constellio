package com.constellio.app.modules.rm.model;

import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.schemas.CombinedStructureFactory;
import com.constellio.model.entities.schemas.ModifiableStructure;

public class CopyRetentionRuleInRuleFactory implements CombinedStructureFactory {

	private static final String NULL = "~null~";

	private static CopyRetentionRuleFactory copyRetentionRuleFactory = new CopyRetentionRuleFactory();

	@Override
	public ModifiableStructure build(String string) {
		int indexOfFirstColon = string.indexOf(":");
		int version = Integer.valueOf(string.substring(0, indexOfFirstColon));

		if (version == 1) {
			return readStructureInVersion1(string, indexOfFirstColon);

		} else {
			throw new ImpossibleRuntimeException("Unsupported version");
		}
	}

	private ModifiableStructure readStructureInVersion1(String string, int indexOfFirstColon) {
		int indexOfSecondColon = string.indexOf(":", indexOfFirstColon + 1);
		int indexOfThirdColon = string.indexOf(":", indexOfSecondColon + 1);
		int indexOfFourthColon = string.indexOf(":", indexOfThirdColon + 1);

		String ruleId = string.substring(indexOfFirstColon + 1, indexOfSecondColon);
		if (NULL.equals(ruleId)) {
			ruleId = null;
		}

		String categoryId = string.substring(indexOfSecondColon + 1, indexOfThirdColon);
		if (NULL.equals(categoryId)) {
			categoryId = null;
		}

		int level = Integer.valueOf(string.substring(indexOfThirdColon + 1, indexOfFourthColon));

		CopyRetentionRule copyRetentionRule = (CopyRetentionRule) copyRetentionRuleFactory
				.build(string.substring(indexOfFourthColon + 1));

		CopyRetentionRuleInRule copyRetentionRuleInRule = new CopyRetentionRuleInRule(ruleId, categoryId, level,
				copyRetentionRule);
		copyRetentionRuleInRule.dirty = false;
		return copyRetentionRuleInRule;
	}

	@Override
	public String toString(ModifiableStructure structure) {
		CopyRetentionRuleInRule copyRetentionRuleInRule = (CopyRetentionRuleInRule) structure;
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("1:");
		stringBuilder.append(copyRetentionRuleInRule.ruleId == null ? NULL : copyRetentionRuleInRule.ruleId);
		stringBuilder.append(":");
		stringBuilder.append(copyRetentionRuleInRule.categoryId == null ? NULL : copyRetentionRuleInRule.categoryId);
		stringBuilder.append(":");
		stringBuilder.append(copyRetentionRuleInRule.categoryLevel);
		stringBuilder.append(":");
		stringBuilder.append(copyRetentionRuleFactory.toString(copyRetentionRuleInRule.copyRetentionRule));

		return stringBuilder.toString();
	}
}
