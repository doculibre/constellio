package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MetadataBuilder_DuplicableFlagTest extends MetadataBuilderTest {

    @Test
    public void givenDuplicableFlagOfMetadataWithoutInheritanceIsNullWhenBuildingThenSetToTrue() {
        metadataWithoutInheritanceBuilder.setType(STRING).setDuplicable(null);

        build();

        assertThat(metadataWithoutInheritance.isDuplicable()).isFalse();
    }

    @Test
    public void givenDuplicableFlagOfMetadataWithoutInheritanceIsNullWhenModifyingThenSetToTrue() {
        metadataWithoutInheritanceBuilder.setType(STRING).setDuplicable(null);

        buildAndModify();

        assertThat(metadataWithoutInheritanceBuilder.isDuplicable()).isFalse();
    }

    @Test
    public void givenDuplicableFlagOfMetadataWithoutInheritanceIsNotDefinedWhenBuildingThenSetToTrue() {
        metadataWithoutInheritanceBuilder.setType(STRING);

        build();

        assertThat(metadataWithoutInheritance.isDuplicable()).isFalse();
    }

    @Test
    public void givenDuplicableFlagOfMetadataWithoutInheritanceIsFalseWhenBuildingThenSetToFalse() {
        metadataWithoutInheritanceBuilder.setType(STRING).setDuplicable(false);

        build();

        assertThat(metadataWithInheritance.isDuplicable()).isFalse();
    }

    @Test
    public void givenDuplicableFlagOfMetadataWithoutInheritanceIsFalseWhenModifyingThenSetToFalse() {
        metadataWithoutInheritanceBuilder.setType(STRING).setDuplicable(false);

        buildAndModify();

        assertThat(metadataWithoutInheritanceBuilder.isDuplicable()).isFalse();
    }

    @Test
    public void givenDuplicableFlagOfMetadataWithInheritanceIsDifferentWhenBuildingThenSetToCustomizedValue() {
        inheritedMetadataBuilder.setType(STRING).setDuplicable(false);
        metadataWithInheritanceBuilder.setDuplicable(true);

        build();

        assertThat(inheritedMetadata.isDuplicable()).isFalse();
        assertThat(metadataWithInheritance.isDuplicable()).isTrue();
    }

    @Test
    public void givenDuplicableFlagOfMetadataWithInheritanceIsDifferentWhenModifyingThenSetToCustomizedValue() {
        inheritedMetadataBuilder.setType(STRING).setDuplicable(false);
        metadataWithInheritanceBuilder.setDuplicable(true);

        buildAndModify();

        assertThat(inheritedMetadataBuilder.isDuplicable()).isFalse();
        assertThat(metadataWithInheritanceBuilder.isDuplicable()).isTrue();
    }

    @Test
    public void givenDuplicableFlagOfMetadataWithInheritanceIsNullWhenBuildingThenSetToInheritedValue() {
        inheritedMetadataBuilder.setType(STRING).setDuplicable(false);
        metadataWithInheritanceBuilder.setDuplicable(null);

        build();

        assertThat(inheritedMetadata.isDuplicable()).isFalse();
        assertThat(metadataWithInheritance.isDuplicable()).isFalse();
    }

    @Test
    public void givenDuplicableFlagOfMetadataWithInheritanceIsNullWhenModifyingThenSetToNull() {
        inheritedMetadataBuilder.setType(STRING).setDuplicable(false);
        metadataWithInheritanceBuilder.setDuplicable(null);

        buildAndModify();

        assertThat(inheritedMetadataBuilder.isDuplicable()).isFalse();
        assertThat(metadataWithInheritanceBuilder.isDuplicable()).isNull();
    }

    @Test
    public void givenDuplicableFlagOfMetadataWithInheritanceIsSameAsInheritanceWhenModifyingThenSetToNull() {
        inheritedMetadataBuilder.setType(STRING).setDuplicable(false);
        metadataWithInheritanceBuilder.setDuplicable(false);

        buildAndModify();

        assertThat(inheritedMetadataBuilder.isDuplicable()).isFalse();
        assertThat(metadataWithInheritanceBuilder.isDuplicable()).isNull();
    }

}
