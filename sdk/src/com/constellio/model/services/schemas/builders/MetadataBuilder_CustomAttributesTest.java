package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MetadataBuilder_CustomAttributesTest extends MetadataBuilderTest {

	@Test
	public void givenCustomAttributesUndefinedOnMetadataWithoutInheritanceWhenBuildingThenEmptySet()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.getCustomAttributes()).isEmpty();
	}

	@Test
	public void givenCustomAttributesUndefinedOnMetadataWithoutInheritanceWhenModifyingThenEmptySet()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getCustomAttributes()).isEmpty();
	}

	@Test
	public void givenCustomAttributesDefinedOnMetadataWithoutInheritanceWhenBuildingThenDefined()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).addCustomAttribute("flag1").addCustomAttribute("flag2");

		build();

		assertThat(metadataWithoutInheritance.getCustomAttributes()).containsOnly("flag1", "flag2");
	}

	@Test
	public void givenCustomAttributesDefinedOnMetadataWithoutInheritanceWhenModifyingThenDefined()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).addCustomAttribute("flag1").removeCustomAttribute("flag1");

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getCustomAttributes()).isEmpty();
	}

	@Test
	public void givenCustomAttributesOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).addCustomAttribute("flag1").addCustomAttribute("flag2");

		build();

		assertThat(metadataWithInheritance.getCustomAttributes()).containsOnly("flag1", "flag2");
	}

	@Test
	public void givenCustomAttributesOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).addCustomAttribute("flag1").addCustomAttribute("flag2");

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.getCustomAttributes()).containsOnly("flag1", "flag2");
	}

	@Test
	public void givenCustomAttributesModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).addCustomAttribute("flag1").addCustomAttribute("flag2");
		assertThat(metadataWithInheritanceBuilder.getCustomAttributes()).containsOnly("flag1", "flag2");

	}

	@Test (expected = MetadataBuilderRuntimeException.class)
	public void givenCustomAttributesWithCommasThenException()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).addCustomAttribute("fla,g1");
		fail();
	}

}
