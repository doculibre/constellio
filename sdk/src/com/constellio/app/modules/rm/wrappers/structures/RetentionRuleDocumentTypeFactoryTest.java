/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
