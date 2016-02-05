package com.constellio.app.modules.rm.wrappers.structures;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.sdk.tests.ConstellioTest;

public class RetentionRuleDocumentTypeFactoryTest extends ConstellioTest {

	RetentionRuleDocumentTypeFactory factory = new RetentionRuleDocumentTypeFactory();

	@Test
	public void whenSetAttributeValueThenBecomeDirty() {
		RetentionRuleDocumentType type = newRetentionRuleDocumentType();
		assertThat(type.isDirty()).isFalse();

		type.setDocumentTypeId("01");
		type.setDisposalType(DisposalType.DESTRUCTION);
		assertThat(type.isDirty()).isFalse();

		type = newRetentionRuleDocumentType();
		type.setDocumentTypeId("02");
		assertThat(type.isDirty()).isTrue();

		type = newRetentionRuleDocumentType();
		type.setDisposalType(DisposalType.DEPOSIT);
		assertThat(type.isDirty()).isTrue();
	}

	@Test
	public void whenConvertingStructureWithoutDecommissioningTypeThenOK()
			throws Exception {

		RetentionRuleDocumentType type = new RetentionRuleDocumentType();
		type.setDocumentTypeId("01");

		String stringValue = factory.toString(type);
		RetentionRuleDocumentType builtType = (RetentionRuleDocumentType) factory.build(stringValue);
		String stringValue2 = factory.toString(builtType);

		assertThat(builtType).isEqualTo(type);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtType.isDirty()).isFalse();
	}

	@Test
	public void whenConvertingStructureWithDecommissioningTypeThenOK()
			throws Exception {

		RetentionRuleDocumentType type = new RetentionRuleDocumentType();
		type.setDocumentTypeId("01");
		type.setDisposalType(DisposalType.DESTRUCTION);

		String stringValue = factory.toString(type);
		RetentionRuleDocumentType builtType = (RetentionRuleDocumentType) factory.build(stringValue);
		String stringValue2 = factory.toString(builtType);

		assertThat(builtType).isEqualTo(type);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtType.isDirty()).isFalse();
	}

	private RetentionRuleDocumentType newRetentionRuleDocumentType() {
		RetentionRuleDocumentType type = new RetentionRuleDocumentType();
		type.documentTypeId = "01";
		type.disposalType = DisposalType.DESTRUCTION;
		return type;
	}
}
