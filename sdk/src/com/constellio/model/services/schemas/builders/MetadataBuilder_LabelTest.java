package com.constellio.model.services.schemas.builders;

//TODO Thiago
public class MetadataBuilder_LabelTest extends MetadataBuilderTest {

	//	@Test(expected = MetadataBuilderRuntimeException.InvalidAttribute.class)
	//	public void givenLabelOfMetadataWithoutInheritanceISNullLabelWhenBuildingThenException() {
	//		metadataWithoutInheritanceBuilder.setType(STRING).setLabel(Language.English, null);
	//
	//		build();
	//	}
	//
	//	@Test
	//	public void givenLabelOfMetadataWithInheritanceIsNullWhenBuildingThenSetToInheritedValue() {
	//		inheritedMetadataBuilder.setType(STRING).setLabel(Language.English, "default label");
	//		metadataWithInheritanceBuilder.setLabel(Language.English, null);
	//
	//		build();
	//
	//		assertThat(metadataWithInheritance.getLabel()).isEqualTo("default label");
	//	}
	//
	//	@Test
	//	public void givenLabelOfMetadataWithInheritanceIsNullWhenModifyingThenSetToNull() {
	//		inheritedMetadataBuilder.setType(STRING).setLabel(Language.English,"default label");
	//		metadataWithInheritanceBuilder.setLabel(Language.English,null);
	//
	//		buildAndModify();
	//
	//		assertThat(metadataWithInheritanceBuilder.getLabel(Language.English)).isNull();
	//	}
	//
	//	@Test
	//	public void givenLabelOfMetadataWithInheritanceIsSameAsItInheritanceWhenModifyingThenSetToNull() {
	//		inheritedMetadataBuilder.setType(STRING).setLabel(Language.English,"default label");
	//		metadataWithInheritanceBuilder.setLabel(Language.English,"default label");
	//
	//		buildAndModify();
	//
	//		assertThat(metadataWithInheritanceBuilder.getLabel(Language.English)).isNull();
	//	}
	//
	//	@Test
	//	public void givenLabelOfMetadataWithInheritanceIsDifferentWhenBuildingThenSetToCustomizedValue() {
	//		inheritedMetadataBuilder.setType(STRING).setLabel(Language.English,"default label");
	//		metadataWithInheritanceBuilder.setLabel(Language.English,"custom label");
	//
	//		build();
	//
	//		assertThat(metadataWithInheritance.getLabel(Language.English)).isEqualTo("custom label");
	//	}
	//
	//	@Test
	//	public void givenLabelOfMetadataWithInheritanceIsDifferentWhenModifyingThenSetToCustomizedValue() {
	//		inheritedMetadataBuilder.setType(STRING).setLabel(Language.English,"default label");
	//		metadataWithInheritanceBuilder.setLabel(Language.English,"custom label");
	//
	//		buildAndModify();
	//
	//		assertThat(metadataWithInheritanceBuilder.getLabel(Language.English)).isEqualTo("custom label");
	//	}
	//
	//	@Test
	//	public void givenLabelOfMetadataWithInheritanceIsNotDefinedWhenBuildingThenSetToCodeValue() {
	//		inheritedMetadataBuilder.setType(STRING);
	//
	//		build();
	//
	//		assertThat(inheritedMetadataBuilder.getLabel(Language.English)).isEqualTo(CODE_DEFAULT_METADATA);
	//	}
	//
	//	@Test(expected = MetadataBuilderRuntimeException.InvalidAttribute.class)
	//	public void givenCodeFinishingWithIdThenException()
	//			throws Exception {
	//		MetadataBuilder.createMetadataWithoutInheritance(CODE_DEFAULT_METADATA + "pid",
	//				schemaBuilder).buildWithoutInheritance(typesFactory, modelLayerFactory);
	//
	//	}

}
