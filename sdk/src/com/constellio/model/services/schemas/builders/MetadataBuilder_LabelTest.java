package com.constellio.model.services.schemas.builders;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataValueType;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MetadataBuilder_LabelTest extends MetadataBuilderTest {

	@Test(expected = MetadataBuilderRuntimeException.InvalidAttribute.class)
	public void givenLabelOfMetadataWithoutInheritanceISNullLabelWhenBuildingThenException() {
		metadataWithoutInheritanceBuilder.setType(MetadataValueType.STRING).addLabel(Language.French, null);

		build();
	}

	@Test
	public void givenLabelOfMetadataWithInheritanceIsNullWhenBuildingThenSetToInheritedValue() {
		inheritedMetadataBuilder.setType(MetadataValueType.STRING).addLabel(Language.French, "default label");
		metadataWithInheritanceBuilder.addLabel(Language.French, null);

		build();

		assertThat(metadataWithInheritance.getLabel(Language.French)).isEqualTo("default label");
	}

	@Test
	public void givenLabelOfMetadataWithInheritanceIsNullWhenModifyingThenSetToNull() {
		inheritedMetadataBuilder.setType(MetadataValueType.STRING).addLabel(Language.French, "default label");
		metadataWithInheritanceBuilder.addLabel(Language.French, null);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.getLabel(Language.French)).isNull();
	}

	@Test
	public void givenLabelOfMetadataWithInheritanceIsSameAsItInheritanceWhenModifyingThenSetToNull() {
		inheritedMetadataBuilder.setType(MetadataValueType.STRING).addLabel(Language.French, "default label");
		metadataWithInheritanceBuilder.addLabel(Language.French, "default label");

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.getLabel(Language.French)).isNull();
	}

	@Test
	public void givenLabelOfMetadataWithInheritanceIsDifferentWhenBuildingThenSetToCustomizedValue() {
		inheritedMetadataBuilder.setType(MetadataValueType.STRING).addLabel(Language.French, "default label");
		metadataWithInheritanceBuilder.addLabel(Language.French, "custom label");

		build();

		assertThat(metadataWithInheritance.getLabel(Language.French)).isEqualTo("custom label");
	}

	@Test
	public void givenLabelOfMetadataWithInheritanceIsDifferentWhenModifyingThenSetToCustomizedValue() {
		inheritedMetadataBuilder.setType(MetadataValueType.STRING).addLabel(Language.French, "default label");
		metadataWithInheritanceBuilder.addLabel(Language.French, "custom label");

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.getLabel(Language.French)).isEqualTo("custom label");
	}

	@Test
	public void givenLabelOfMetadataWithInheritanceIsNotDefinedWhenBuildingThenSetToCodeValue() {
		inheritedMetadataBuilder.setType(MetadataValueType.STRING);

		build();

		assertThat(inheritedMetadataBuilder.getLabel(Language.French)).isEqualTo(CODE_DEFAULT_METADATA);
	}

	@Test(expected = MetadataBuilderRuntimeException.InvalidAttribute.class)
	public void givenCodeFinishingWithIdThenException()
			throws Exception {
		MetadataBuilder.createMetadataWithoutInheritance(CODE_DEFAULT_METADATA + "pid",
				schemaBuilder).buildWithoutInheritance(typesFactory, schemaTypeBuilder, (short) 42, modelLayerFactory);

	}

}
