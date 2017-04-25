package com.constellio.app.modules.rm.configScripts;

import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by constellios on 2017-04-25.
 */
public class EnableOrDisableContainerMultiValueMetadataScriptAcceptanceTest2 extends ConstellioTest
{
    MetadataSchemasManager metadataSchemasManager = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager();

    @Test
    public void whenValidateAndContainerIsPresentThenErrorIsPresent() {
        prepareSystem(
                withZeCollection().withConstellioRMModule().withFoldersAndContainersOfEveryStatus()
        );

        EnableOrDisableContainerMultiValueMetadataScript enableOrDisableContainerMultiValueMetadataScript = new EnableOrDisableContainerMultiValueMetadataScript();

        ValidationErrors validationError = new ValidationErrors();

        enableOrDisableContainerMultiValueMetadataScript.validate(true, validationError);

        assertThat(validationError.getValidationErrors().size()).isEqualTo(1);
    }

    @Test
    public void whenOnValueChangedTrueThenSetMultivalueTrue() {
       prepareSystem(
                withZeCollection().withConstellioRMModule().withAllTestUsers()
        );

        EnableOrDisableContainerMultiValueMetadataScript enableOrDisableContainerMultiValueMetadataScript = new EnableOrDisableContainerMultiValueMetadataScript();

        enableOrDisableContainerMultiValueMetadataScript.onValueChanged(false, true, getModelLayerFactory());

        assertThat(metadataSchemasManager.getSchemaTypes(zeCollection).getMetadata(ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.STORAGE_SPACE).isMultivalue()).isTrue();
    }

    @Test
    public void whenOnValueChangedFalseThenSetMultivalueFalse() {
        prepareSystem(
                withZeCollection().withConstellioRMModule()
        );

        EnableOrDisableContainerMultiValueMetadataScript enableOrDisableContainerMultiValueMetadataScript = new EnableOrDisableContainerMultiValueMetadataScript();

        enableOrDisableContainerMultiValueMetadataScript.onValueChanged(true, false, getModelLayerFactory());

        assertThat(metadataSchemasManager.getSchemaTypes(zeCollection).getMetadata(ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.STORAGE_SPACE).isMultivalue()).isFalse();
    }
}