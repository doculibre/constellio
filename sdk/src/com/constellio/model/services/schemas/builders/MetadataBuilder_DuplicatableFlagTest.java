package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MetadataBuilder_DuplicatableFlagTest extends MetadataBuilderTest {

    @Test
    public void givenDuplicatableFlagOfMetadataWithoutInheritanceIsNullWhenBuildingThenSetToTrue() {
        metadataWithoutInheritanceBuilder.setType(STRING).setDuplicatable(null);

        build();

        assertThat(metadataWithoutInheritance.isDuplicatable()).isFalse();
    }

    @Test
    public void givenDuplicatableFlagOfMetadataWithoutInheritanceIsNullWhenModifyingThenSetToTrue() {
        metadataWithoutInheritanceBuilder.setType(STRING).setDuplicatable(null);

        buildAndModify();

        assertThat(metadataWithoutInheritanceBuilder.isDuplicatable()).isFalse();
    }

    @Test
    public void givenDuplicatableFlagOfMetadataWithoutInheritanceIsNotDefinedWhenBuildingThenSetToTrue() {
        metadataWithoutInheritanceBuilder.setType(STRING);

        build();

        assertThat(metadataWithoutInheritance.isDuplicatable()).isFalse();
    }

    @Test
    public void givenDuplicatableFlagOfMetadataWithoutInheritanceIsFalseWhenBuildingThenSetToFalse() {
        metadataWithoutInheritanceBuilder.setType(STRING).setDuplicatable(false);

        build();

        assertThat(metadataWithInheritance.isDuplicatable()).isFalse();
    }

    @Test
    public void givenDuplicatableFlagOfMetadataWithoutInheritanceIsFalseWhenModifyingThenSetToFalse() {
        metadataWithoutInheritanceBuilder.setType(STRING).setDuplicatable(false);

        buildAndModify();

        assertThat(metadataWithoutInheritanceBuilder.isDuplicatable()).isFalse();
    }

    @Test
    public void givenDuplicatableFlagOfMetadataWithInheritanceIsDifferentWhenBuildingThenSetToCustomizedValue() {
        inheritedMetadataBuilder.setType(STRING).setDuplicatable(false);
        metadataWithInheritanceBuilder.setDuplicatable(true);

        build();

        assertThat(inheritedMetadata.isDuplicatable()).isFalse();
        assertThat(metadataWithInheritance.isDuplicatable()).isTrue();
    }

    @Test
    public void givenDuplicatableFlagOfMetadataWithInheritanceIsDifferentWhenModifyingThenSetToCustomizedValue() {
        inheritedMetadataBuilder.setType(STRING).setDuplicatable(false);
        metadataWithInheritanceBuilder.setDuplicatable(true);

        buildAndModify();

        assertThat(inheritedMetadataBuilder.isDuplicatable()).isFalse();
        assertThat(metadataWithInheritanceBuilder.isDuplicatable()).isTrue();
    }

    @Test
    public void givenDuplicatableFlagOfMetadataWithInheritanceIsNullWhenBuildingThenSetToInheritedValue() {
        inheritedMetadataBuilder.setType(STRING).setDuplicatable(false);
        metadataWithInheritanceBuilder.setDuplicatable(null);

        build();

        assertThat(inheritedMetadata.isDuplicatable()).isFalse();
        assertThat(metadataWithInheritance.isDuplicatable()).isFalse();
    }

    @Test
    public void givenDuplicatableFlagOfMetadataWithInheritanceIsNullWhenModifyingThenSetToNull() {
        inheritedMetadataBuilder.setType(STRING).setDuplicatable(false);
        metadataWithInheritanceBuilder.setDuplicatable(null);

        buildAndModify();

        assertThat(inheritedMetadataBuilder.isDuplicatable()).isFalse();
        assertThat(metadataWithInheritanceBuilder.isDuplicatable()).isNull();
    }

    @Test
    public void givenDuplicatableFlagOfMetadataWithInheritanceIsSameAsInheritanceWhenModifyingThenSetToNull() {
        inheritedMetadataBuilder.setType(STRING).setDuplicatable(false);
        metadataWithInheritanceBuilder.setDuplicatable(false);

        buildAndModify();

        assertThat(inheritedMetadataBuilder.isDuplicatable()).isFalse();
        assertThat(metadataWithInheritanceBuilder.isDuplicatable()).isNull();
    }

}
