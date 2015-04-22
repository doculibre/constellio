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
package com.constellio.model.services.schemas.builders;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException.InvalidAttribute;

public class MetadataBuilder_EnumClassTest extends MetadataBuilderTest {

	@Test
	public void givenRecordMetadataStructureFactoryDefinedInInheritedMetadataWhenBuildingThenInherited()
			throws Exception {
		inheritedMetadataBuilder.defineAsEnum(AValidEnum.class);

		build();

		assertThat(inheritedMetadata.getType()).isSameAs(MetadataValueType.ENUM);
		assertThat(inheritedMetadata.getEnumClass()).isEqualTo(AValidEnum.class);
		assertThat(metadataWithInheritance.getEnumClass()).isEqualTo(AValidEnum.class);

	}

	@Test
	public void givenRecordMetadataStructureFactoryDefinedInInheritedMetadataWhenModifyingThenInherited()
			throws Exception {
		inheritedMetadataBuilder.defineAsEnum(AValidEnum.class);

		buildAndModify();

		assertThat(inheritedMetadataBuilder.getType()).isSameAs(MetadataValueType.ENUM);
		assertThat(inheritedMetadataBuilder.getEnumClass()).isEqualTo(AValidEnum.class);
		assertThat(metadataWithInheritanceBuilder.getEnumClass()).isEqualTo(AValidEnum.class);

	}

	@Test(expected = MetadataBuilderRuntimeException.EnumClassMustImplementEnumWithSmallCode.class)
	public void whenCreatingMetadataWithAnEnumNotImplementingEnumWithSmallCodeThenException()
			throws Exception {
		inheritedMetadataBuilder.defineAsEnum(AnInvalidEnum.class);

		build();

	}

	@Test(expected = InvalidAttribute.class)
	public void whenCreatingMetadataWithANullEnumclassThenException()
			throws Exception {
		inheritedMetadataBuilder.defineAsEnum(null);

		build();

	}

	public static enum AValidEnum implements EnumWithSmallCode {

		FIRST_VALUE("F"), SECOND_VALUE("S");

		private String code;

		AValidEnum(String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}

	}

	public static enum AnInvalidEnum {

		FIRST_VALUE, SECOND_VALUE;

	}

}
