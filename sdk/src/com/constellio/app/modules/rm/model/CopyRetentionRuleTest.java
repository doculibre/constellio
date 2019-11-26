package com.constellio.app.modules.rm.model;

import com.constellio.app.modules.rm.model.CopyRetentionRuleFactoryRuntimeException.CopyRetentionRuleFactoryRuntimeException_CannotModifyId;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class CopyRetentionRuleTest extends ConstellioTest {

	CopyRetentionRuleFactory factory = new CopyRetentionRuleFactory();

	CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();

	@Test
	public void whenSetAttributeValueThenBecomeDirty() {
		CopyRetentionRule rule = new CopyRetentionRule();
		assertThat(rule.isDirty()).isFalse();

		rule.setId("zeId");
		assertThat(rule.isDirty()).isTrue();

		rule = new CopyRetentionRule();
		rule.setCode("zeCode:\n\tline2");
		assertThat(rule.isDirty()).isTrue();

		rule = new CopyRetentionRule();
		rule.setCopyType(CopyType.PRINCIPAL);
		assertThat(rule.isDirty()).isTrue();

		rule = new CopyRetentionRule();
		rule.setMediumTypeIds(asList("firstType", "secondType"));
		assertThat(rule.isDirty()).isTrue();

		rule = new CopyRetentionRule();
		rule.setContentTypesComment("zeContentTypesComment");
		assertThat(rule.isDirty()).isTrue();

		rule = new CopyRetentionRule();
		rule.setActiveRetentionPeriod(RetentionPeriod.OPEN_888);
		assertThat(rule.isDirty()).isTrue();

		rule = new CopyRetentionRule();
		rule.setActiveRetentionComment("zeActive_RétentionComment");
		assertThat(rule.isDirty()).isTrue();

		rule = new CopyRetentionRule();
		rule.setSemiActiveRetentionPeriod(RetentionPeriod.OPEN_999);
		assertThat(rule.isDirty()).isTrue();

		rule = new CopyRetentionRule();
		rule.setSemiActiveRetentionComment("zeSemi=;ActiveRetention-Comment");
		assertThat(rule.isDirty()).isTrue();

		rule = new CopyRetentionRule();
		rule.setInactiveDisposalType(DisposalType.DESTRUCTION);
		assertThat(rule.isDirty()).isTrue();

		rule = new CopyRetentionRule();
		rule.setInactiveDisposalComment("zeInactive:Disposable\nComment");
		assertThat(rule.isDirty()).isTrue();

		rule = new CopyRetentionRule();
		rule.setTypeId("zeDocumentTypeId");
		assertThat(rule.isDirty()).isTrue();

		rule = new CopyRetentionRule();
		rule.setActiveDateMetadata("codeActiveDate");
		assertThat(rule.isDirty()).isTrue();

		rule = new CopyRetentionRule();
		rule.setSemiActiveDateMetadata("codeSemiActiveDate");
		assertThat(rule.isDirty()).isTrue();

		rule = new CopyRetentionRule();
		rule.setEssential(false);
		assertThat(rule.isDirty()).isTrue();

		rule = new CopyRetentionRule();
		rule.setEssential(true);
		assertThat(rule.isDirty()).isTrue();

		rule = new CopyRetentionRule();
		rule.setSemiActiveYearTypeId("0001");
		assertThat(rule.isDirty()).isTrue();

		rule = new CopyRetentionRule();
		rule.setInactiveYearTypeId("0001");
		assertThat(rule.isDirty()).isTrue();
	}

	@Test
	public void whenConvertingStructureWithCommentsAndCodeThenRemainsEqual()
			throws Exception {

		CopyRetentionRule rule = new CopyRetentionRule();
		rule.setId("zeId");
		rule.setCode("zeCode:\n\tline2");
		rule.setCopyType(CopyType.PRINCIPAL);
		rule.setMediumTypeIds(asList("firstType", "secondType"));
		rule.setContentTypesComment("zeContentTypesComment");
		rule.setActiveRetentionPeriod(RetentionPeriod.OPEN_888);
		rule.setActiveRetentionComment("zeActive_RétentionComment");
		rule.setSemiActiveRetentionPeriod(RetentionPeriod.OPEN_999);
		rule.setSemiActiveRetentionComment("zeSemi=;ActiveRetention-Comment");
		rule.setInactiveDisposalType(DisposalType.DESTRUCTION);
		rule.setInactiveDisposalComment("zeInactive:Disposable\nComment");
		rule.setTypeId("zeDocumentTypeId");
		rule.setActiveDateMetadata("codeActiveDate");
		rule.setSemiActiveDateMetadata("codeSemiActiveDate");
		rule.setSemiActiveYearTypeId("00000042");
		rule.setInactiveYearTypeId("00000666");

		String stringValue = factory.toString(rule);
		CopyRetentionRule builtRule = (CopyRetentionRule) factory.build(stringValue);
		String stringValue2 = factory.toString(builtRule);

		assertThat(builtRule).isEqualTo(rule);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtRule.isDirty()).isFalse();
		assertThat(builtRule.isEssential()).isFalse();
		assertThat(builtRule.getSemiActiveYearTypeId()).isEqualTo("00000042");
		assertThat(builtRule.getInactiveYearTypeId()).isEqualTo("00000666");
	}

	@Test
	public void whenConvertingStructureWithNullStatusAndCodeThenRemainsEqual()
			throws Exception {

		CopyRetentionRule rule = new CopyRetentionRule();
		rule.setId("zeId");
		rule.setCode("zeCode:\n\tline2");
		rule.setCopyType(CopyType.PRINCIPAL);
		rule.setMediumTypeIds(asList("firstType", "secondType"));
		rule.setContentTypesComment("zeContentTypesComment");
		rule.setActiveRetentionPeriod(null);
		rule.setActiveRetentionComment("zeActive_RétentionComment");
		rule.setSemiActiveRetentionPeriod(null);
		rule.setSemiActiveRetentionComment("zeSemi=;ActiveRetention-Comment");
		rule.setInactiveDisposalType(DisposalType.DESTRUCTION);
		rule.setInactiveDisposalComment("zeInactive:Disposable\nComment");
		rule.setTypeId("zeDocumentTypeId");
		rule.setActiveDateMetadata("codeActiveDate");
		rule.setSemiActiveDateMetadata("codeSemiActiveDate");
		rule.setEssential(true);

		String stringValue = factory.toString(rule);
		CopyRetentionRule builtRule = (CopyRetentionRule) factory.build(stringValue);
		String stringValue2 = factory.toString(builtRule);

		assertThat(builtRule).isEqualTo(rule);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtRule.isDirty()).isFalse();
		assertThat(builtRule.getActiveRetentionPeriod()).isSameAs(RetentionPeriod.ZERO);
		assertThat(builtRule.getSemiActiveRetentionPeriod()).isSameAs(RetentionPeriod.ZERO);
		assertThat(builtRule.getInactiveDisposalType()).isEqualTo(DisposalType.DESTRUCTION);
		assertThat(builtRule.isEssential()).isTrue();

	}

	@Test
	public void whenConvertingStructureVersion1WithoutDisposalTypeThenSetToDestruction()
			throws Exception {
		String strValue = "~null~:S:~null~:F1:~null~:F2:~null~:R1:firstType:secondType:thirdType";
		String strValue2 = "~null~:S:~null~:F1:~null~:F2:~null~:~null~:firstType:secondType:thirdType";

		CopyRetentionRule rule1 = (CopyRetentionRule) factory.build(strValue);
		CopyRetentionRule rule2 = (CopyRetentionRule) factory.build(strValue2);

		assertThat(rule1.getInactiveDisposalType()).isEqualTo(DisposalType.DESTRUCTION);
		assertThat(rule1.getInactiveDisposalComment()).isEqualTo("R1");

		assertThat(rule2.getInactiveDisposalType()).isEqualTo(DisposalType.DESTRUCTION);
		assertThat(rule2.getInactiveDisposalComment()).isNull();

	}

	@Test
	public void whenConvertingStructureVersion2WithoutDisposalTypeThenSetToDestruction()
			throws Exception {
		String strValue = "version2:~null~:S:~null~:F1:~null~:F2:~null~:R1:zeDocumentTypeId:codeActiveDate:codeSemiActiveDate:firstType:secondType:thirdType";
		String strValue2 = "version2:~null~:S:~null~:F1:~null~:F2:~null~:~null~:zeDocumentTypeId:codeActiveDate:codeSemiActiveDate:firstType:secondType:thirdType";

		CopyRetentionRule rule1 = (CopyRetentionRule) factory.build(strValue);
		CopyRetentionRule rule2 = (CopyRetentionRule) factory.build(strValue2);

		assertThat(rule1.getInactiveDisposalType()).isEqualTo(DisposalType.DESTRUCTION);
		assertThat(rule1.getInactiveDisposalComment()).isEqualTo("R1");
		assertThat(rule1.getOpenActiveRetentionPeriod()).isNull();

		assertThat(rule2.getInactiveDisposalType()).isEqualTo(DisposalType.DESTRUCTION);
		assertThat(rule2.getInactiveDisposalComment()).isNull();
		assertThat(rule2.getOpenActiveRetentionPeriod()).isNull();
	}

	@Test
	public void whenConvertingStructureThenCorrectInfos()
			throws Exception {
		String strValue = "375:P:~null~:888::888::T:~null~:00000000003:00000000001";

		CopyRetentionRule rule1 = (CopyRetentionRule) factory.build(strValue);

		assertThat(rule1.getCode()).isEqualTo("375");
		assertThat(rule1.getCopyType()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(rule1.getMediumTypeIds()).isEqualTo(asList("00000000003", "00000000001"));
		assertThat(rule1.getActiveRetentionPeriod()).isEqualTo(RetentionPeriod.OPEN_888);
		assertThat(rule1.getSemiActiveRetentionPeriod()).isEqualTo(RetentionPeriod.OPEN_888);
		assertThat(rule1.getInactiveDisposalType()).isEqualTo(DisposalType.SORT);
		assertThat(rule1.getActiveRetentionComment()).isNull();
		assertThat(rule1.getSemiActiveRetentionComment()).isNull();
		assertThat(rule1.getInactiveDisposalComment()).isNull();
		assertThat(rule1.getTypeId()).isNull();
		assertThat(rule1.getActiveDateMetadata()).isNull();
		assertThat(rule1.getSemiActiveDateMetadata()).isNull();
		assertThat(rule1.getOpenActiveRetentionPeriod()).isNull();
		assertThat(rule1.isEssential()).isFalse();
		assertThat(rule1.getSemiActiveYearTypeId()).isNull();
		assertThat(rule1.getInactiveYearTypeId()).isNull();

	}

	@Test
	public void whenConvertingStructureVersion2ThenCorrectInfos()
			throws Exception {
		String strValue = "version2:375:P:~null~:888::888::T:~null~:zeDocumentTypeId:codeActiveDate:codeSemiActiveDate:00000000003:00000000001";

		CopyRetentionRule rule1 = (CopyRetentionRule) factory.build(strValue);

		assertThat(rule1.getCode()).isEqualTo("375");
		assertThat(rule1.getCopyType()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(rule1.getMediumTypeIds()).isEqualTo(asList("00000000003", "00000000001"));
		assertThat(rule1.getActiveRetentionPeriod()).isEqualTo(RetentionPeriod.OPEN_888);
		assertThat(rule1.getSemiActiveRetentionPeriod()).isEqualTo(RetentionPeriod.OPEN_888);
		assertThat(rule1.getInactiveDisposalType()).isEqualTo(DisposalType.SORT);
		assertThat(rule1.getActiveRetentionComment()).isNull();
		assertThat(rule1.getSemiActiveRetentionComment()).isNull();
		assertThat(rule1.getInactiveDisposalComment()).isNull();
		assertThat(rule1.getTypeId()).isEqualTo("zeDocumentTypeId");
		assertThat(rule1.getActiveDateMetadata()).isEqualTo("codeActiveDate");
		assertThat(rule1.getSemiActiveDateMetadata()).isEqualTo("codeSemiActiveDate");
		assertThat(rule1.getOpenActiveRetentionPeriod()).isNull();
		assertThat(rule1.isEssential()).isFalse();
		assertThat(rule1.getSemiActiveYearTypeId()).isNull();
		assertThat(rule1.getInactiveYearTypeId()).isNull();
	}

	@Test
	public void whenConvertingStructureWithoutCommentsAndCodeThenRemainsEqual()
			throws Exception {

		CopyRetentionRule rule = new CopyRetentionRule();
		rule.setId("zeId");
		rule.setCopyType(CopyType.SECONDARY);
		rule.setMediumTypeIds(asList("firstType", "secondType", "thirdType"));
		rule.setActiveRetentionPeriod(RetentionPeriod.fixed(1));
		rule.setSemiActiveRetentionPeriod(RetentionPeriod.fixed(2));
		rule.setInactiveDisposalType(DisposalType.SORT);

		String stringValue = factory.toString(rule);
		System.out.println(stringValue);
		CopyRetentionRule builtRule = (CopyRetentionRule) factory.build(stringValue);
		String stringValue2 = factory.toString(builtRule);

		assertThat(builtRule).isEqualTo(rule);
		assertThat(stringValue2).isEqualTo(stringValue);

	}

	@Test
	public void whenCreateCopyRetentionRuleWithEmptyPeriodsThenSetToNull()
			throws Exception {

		CopyRetentionRule rule = copyBuilder.newRetentionRule(CopyType.PRINCIPAL, asList("PA", "FI"), "888-0-");
		assertThat(rule.getActiveRetentionPeriod()).isEqualTo(RetentionPeriod.OPEN_888);
		assertThat(rule.getSemiActiveRetentionPeriod()).isSameAs(RetentionPeriod.ZERO);
		assertThat(rule.getInactiveDisposalType()).isNull();

		rule = copyBuilder.newRetentionRule(CopyType.PRINCIPAL, asList("PA", "FI"), "0-2-");
		assertThat(rule.getActiveRetentionPeriod()).isSameAs(RetentionPeriod.ZERO);
		assertThat(rule.getSemiActiveRetentionPeriod()).isEqualTo(RetentionPeriod.fixed(2));
		assertThat(rule.getInactiveDisposalType()).isNull();

		rule = copyBuilder.newRetentionRule(CopyType.PRINCIPAL, asList("PA", "FI"), "0-0-");
		assertThat(rule.getActiveRetentionPeriod()).isSameAs(RetentionPeriod.ZERO);
		assertThat(rule.getSemiActiveRetentionPeriod()).isSameAs(RetentionPeriod.ZERO);
		assertThat(rule.getInactiveDisposalType()).isNull();

		rule = copyBuilder.newRetentionRule(CopyType.PRINCIPAL, asList("PA", "FI"), "0-0-T");
		assertThat(rule.getActiveRetentionPeriod()).isSameAs(RetentionPeriod.ZERO);
		assertThat(rule.getSemiActiveRetentionPeriod()).isSameAs(RetentionPeriod.ZERO);
		assertThat(rule.getInactiveDisposalType()).isEqualTo(DisposalType.SORT);

	}

	@Test
	public void whenConvertingCopyRetentionRuleWithOpenActivePeriodThenPersisted()
			throws Exception {

		CopyRetentionRule copyRuleWithZeroOpenActivePeriod =
				copyBuilder.newRetentionRule(CopyType.PRINCIPAL, asList("PA", "FI"), "888-0-").setOpenActiveRetentionPeriod(0);

		CopyRetentionRule copyRuleWithZeroOneYearActivePeriod =
				copyBuilder.newRetentionRule(CopyType.PRINCIPAL, asList("PA", "FI"), "888-0-").setOpenActiveRetentionPeriod(1);

		CopyRetentionRule copyRuleWithNullActivePeriod =
				copyBuilder.newRetentionRule(CopyType.PRINCIPAL, asList("PA", "FI"), "888-0-").setOpenActiveRetentionPeriod(null);

		copyRuleWithZeroOpenActivePeriod = (CopyRetentionRule) factory.build(factory.toString(copyRuleWithZeroOpenActivePeriod));
		copyRuleWithZeroOneYearActivePeriod = (CopyRetentionRule) factory
				.build(factory.toString(copyRuleWithZeroOneYearActivePeriod));
		copyRuleWithNullActivePeriod = (CopyRetentionRule) factory
				.build(factory.toString(copyRuleWithNullActivePeriod));

		assertThat(copyRuleWithZeroOpenActivePeriod.getOpenActiveRetentionPeriod()).isEqualTo(0);
		assertThat(copyRuleWithZeroOneYearActivePeriod.getOpenActiveRetentionPeriod()).isEqualTo(1);
		assertThat(copyRuleWithNullActivePeriod.getOpenActiveRetentionPeriod()).isNull();
	}

	@Test
	public void whenEvaluateDecommissioningActionsThenBasedOnTypes()
			throws Exception {

		List<String> types = asList("PA", "FI");

		assertThat(copyBuilder.newRetentionRule(CopyType.PRINCIPAL, types, "888-0-D").canTransferToSemiActive()).isFalse();
		assertThat(copyBuilder.newRetentionRule(CopyType.PRINCIPAL, types, "888-0-D").canDeposit()).isFalse();
		assertThat(copyBuilder.newRetentionRule(CopyType.PRINCIPAL, types, "888-0-D").canDestroy()).isTrue();
		assertThat(copyBuilder.newRetentionRule(CopyType.PRINCIPAL, types, "888-0-D").canSort()).isFalse();

		assertThat(copyBuilder.newRetentionRule(CopyType.PRINCIPAL, types, "888-2-T").canTransferToSemiActive()).isTrue();
		assertThat(copyBuilder.newRetentionRule(CopyType.PRINCIPAL, types, "888-2-T").canDeposit()).isTrue();
		assertThat(copyBuilder.newRetentionRule(CopyType.PRINCIPAL, types, "888-2-T").canDestroy()).isTrue();
		assertThat(copyBuilder.newRetentionRule(CopyType.PRINCIPAL, types, "888-2-T").canSort()).isTrue();

		assertThat(copyBuilder.newRetentionRule(CopyType.PRINCIPAL, types, "888-2-C").canTransferToSemiActive()).isTrue();
		assertThat(copyBuilder.newRetentionRule(CopyType.PRINCIPAL, types, "888-2-C").canDeposit()).isTrue();
		assertThat(copyBuilder.newRetentionRule(CopyType.PRINCIPAL, types, "888-2-C").canDestroy()).isFalse();
		assertThat(copyBuilder.newRetentionRule(CopyType.PRINCIPAL, types, "888-2-C").canSort()).isFalse();

		assertThat(copyBuilder.newRetentionRule(CopyType.PRINCIPAL, types, "888-2-").canTransferToSemiActive()).isTrue();
		assertThat(copyBuilder.newRetentionRule(CopyType.PRINCIPAL, types, "888-2-").canDeposit()).isFalse();
		assertThat(copyBuilder.newRetentionRule(CopyType.PRINCIPAL, types, "888-2-").canDestroy()).isFalse();
		assertThat(copyBuilder.newRetentionRule(CopyType.PRINCIPAL, types, "888-2-").canSort()).isFalse();

	}

	@Test(expected = CopyRetentionRuleFactoryRuntimeException_CannotModifyId.class)
	public void whenModifyIdThenException()
			throws Exception {

		List<String> types = asList("PA", "FI");
		CopyRetentionRule copyRetentionRule = copyBuilder.newRetentionRule(CopyType.PRINCIPAL, types, "888-0-D").setId("newId");
		factory.toString(copyRetentionRule);
	}

	@Test
	public void whenMultipleNullValuesInStructure() {
		String baseValue = "v5:e56bb710-62af-11e7-86be-0050569d6495:02:::P:false:false:~null~:~null~:F0:~null~:F0:~null~:D:~null~:~null~:~null~:~null~:~null~:~null~:00022539008";
		CopyRetentionRule builtRule = (CopyRetentionRule) factory.build(baseValue);
		String correctedValue = factory.toString(builtRule);

		assertThat(builtRule.title).isEqualTo(null);
		assertThat(builtRule.description).isEqualTo(null);
		assertThat(correctedValue).isEqualTo("v5:e56bb710-62af-11e7-86be-0050569d6495:02:~null~:~null~:P:false:false:~null~:~null~:F0:~null~:F0:~null~:D:~null~:~null~:~null~:~null~:~null~:~null~:00022539008");
	}
}