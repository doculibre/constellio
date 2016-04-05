package com.constellio.app.modules.rm.model;

import java.util.List;

import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.data.dao.services.idGenerator.ZeroPaddedSequentialUniqueIdGenerator;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.utils.EnumWithSmallCodeUtils;

public class CopyRetentionRuleBuilder {

	UniqueIdGenerator uniqueIdGenerator;

	public CopyRetentionRuleBuilder(UniqueIdGenerator uniqueIdGenerator) {
		this.uniqueIdGenerator = uniqueIdGenerator;
	}

	public CopyRetentionRule newPrincipal(List<String> contentTypesCodes, String value) {
		return newRetentionRule(CopyType.PRINCIPAL, contentTypesCodes, value);
	}

	public CopyRetentionRule newSecondary(List<String> contentTypesCodes, String value) {
		return newRetentionRule(CopyType.SECONDARY, contentTypesCodes, value);
	}

	public CopyRetentionRule newPrincipal(List<String> contentTypesCodes) {
		CopyRetentionRule copyRetentionRule = new CopyRetentionRule();
		copyRetentionRule.setId(uniqueIdGenerator.next());
		copyRetentionRule.setMediumTypeIds(contentTypesCodes);
		copyRetentionRule.setCopyType(CopyType.PRINCIPAL);
		return copyRetentionRule;
	}

	public CopyRetentionRule newSecondary(List<String> contentTypesCodes) {
		CopyRetentionRule copyRetentionRule = new CopyRetentionRule();
		copyRetentionRule.setId(uniqueIdGenerator.next());
		copyRetentionRule.setMediumTypeIds(contentTypesCodes);
		copyRetentionRule.setCopyType(CopyType.SECONDARY);
		return copyRetentionRule;
	}

	public CopyRetentionRule newRetentionRule(CopyType copyType, List<String> contentTypesCodes, String value) {
		String[] parts = (" " + value + " ").split("-");
		CopyRetentionRule copyRetentionRule = new CopyRetentionRule();
		copyRetentionRule.setId(uniqueIdGenerator.next());
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

	public CopyRetentionRule newCopyRetentionRule() {
		CopyRetentionRule copyRetentionRule = new CopyRetentionRule();
		copyRetentionRule.setId(uniqueIdGenerator.next());
		return copyRetentionRule;
	}

	public CopyRetentionRule newCopyRetentionRuleWithId(String id) {
		CopyRetentionRule copyRetentionRule = new CopyRetentionRule();
		copyRetentionRule.setId(id);
		return copyRetentionRule;
	}

	public static CopyRetentionRuleBuilder sequential(ConstellioFactories constellioFactories) {
		return sequential(constellioFactories.getDataLayerFactory());
	}

	public static CopyRetentionRuleBuilder sequential(RMSchemasRecordsServices rm) {
		return sequential(rm.getModelLayerFactory().getDataLayerFactory());
	}

	public static CopyRetentionRuleBuilder sequential(AppLayerFactory appLayerFactory) {
		return sequential(appLayerFactory.getModelLayerFactory().getDataLayerFactory());
	}

	public static CopyRetentionRuleBuilder sequential(ModelLayerFactory modelLayerFactory) {
		return sequential(modelLayerFactory.getDataLayerFactory());
	}

	public static CopyRetentionRuleBuilder sequential(DataLayerFactory dataLayerFactory) {
		final UniqueIdGenerator layerUniqueIdGenerator = dataLayerFactory.getUniqueIdGenerator();
		UniqueIdGenerator uniqueIdGenerator = layerUniqueIdGenerator;
		if (layerUniqueIdGenerator instanceof ZeroPaddedSequentialUniqueIdGenerator) {
			uniqueIdGenerator = new UniqueIdGenerator() {

				@Override
				public String next() {
					return ((ZeroPaddedSequentialUniqueIdGenerator) layerUniqueIdGenerator).nextWithoutZeros();
				}
			};
		}

		return new CopyRetentionRuleBuilder(uniqueIdGenerator);
	}

	public static CopyRetentionRuleBuilder UUID() {
		return new CopyRetentionRuleBuilder(new UUIDV1Generator());
	}

	public void addIdsTo(List<CopyRetentionRule> copyRetentionRules) {
		for (CopyRetentionRule copyRetentionRule : copyRetentionRules) {
			addIdsTo(copyRetentionRule);
		}
	}

	public void addIdsTo(CopyRetentionRule copyRetentionRule) {
		if (copyRetentionRule != null) {
			copyRetentionRule.setId(uniqueIdGenerator.next());
		}
	}
}
