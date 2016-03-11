package com.constellio.app.modules.rm.model;

import java.util.List;

import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.model.utils.EnumWithSmallCodeUtils;

public class CopyRetentionRuleBuilderWithDefinedIds extends CopyRetentionRuleBuilder {
	public CopyRetentionRuleBuilderWithDefinedIds() {
		super(new UUIDV1Generator());
	}

	@Override
	public CopyRetentionRule newRetentionRule(CopyType copyType, List<String> contentTypesCodes, String value) {
		String[] parts = (" " + value + " ").split("-");
		CopyRetentionRule copyRetentionRule = new CopyRetentionRule();
		copyRetentionRule.setId(copyType.getCode() + ":" + value + ":" + contentTypesCodes.toString());
		copyRetentionRule.setMediumTypeIds(contentTypesCodes);
		copyRetentionRule.setCopyType(copyType);

		String part0 = parts[0].trim();
		String part1 = parts[1].trim();
		String part2 = parts[2].trim();

		if (!part0.isEmpty() && !part0.equals("0")) {
			copyRetentionRule.setActiveRetentionPeriod(new RetentionPeriod(Integer.valueOf(part0)));
		}
		if (!part1.isEmpty() && !part1.equals("0")) {
			copyRetentionRule.setSemiActiveRetentionPeriod(new RetentionPeriod(Integer.valueOf(part1)));
		}
		if (!part2.isEmpty()) {
			copyRetentionRule.setInactiveDisposalType((DisposalType) EnumWithSmallCodeUtils.toEnum(DisposalType.class, part2));
		}
		return copyRetentionRule;
	}

}
