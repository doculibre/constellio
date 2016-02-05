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
