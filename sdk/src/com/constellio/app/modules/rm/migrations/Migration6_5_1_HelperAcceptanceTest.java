package com.constellio.app.modules.rm.migrations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.migrations.RMMigrationTo6_5_1.Migration6_5_1_Helper;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;

public class Migration6_5_1_HelperAcceptanceTest extends ConstellioTest {
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		assertThat(getModelLayerFactory().getTaxonomiesManager().getPrincipalTaxonomy(zeCollection).getCode())
				.isEqualTo(RMTaxonomies.ADMINISTRATIVE_UNITS);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();

	}

	@Test
	public void givenInexistingMetadataWhenIsUsedThenReturnFalse()
			throws Exception {
		assertThat(Migration6_5_1_Helper.isUsed("inexistingMetadata", Folder.SCHEMA_TYPE, zeCollection, getAppLayerFactory()))
				.isFalse();
	}

	@Test
	public void givenInexistingSchemaWhenIsUsedThenReturnFalse()
			throws Exception {
		assertThat(Migration6_5_1_Helper.isUsed(Folder.CATEGORY, "inexistingSchemaType", zeCollection, getAppLayerFactory()))
				.isFalse();
	}

	@Test
	public void givenUnusedMetadataWhenIsUsedThenReturnFalse()
			throws Exception {
		String metadtaLocalCode = givenUnusedMetadata();
		assertThat(Migration6_5_1_Helper.isUsed(metadtaLocalCode, Folder.SCHEMA_TYPE, zeCollection, getAppLayerFactory()))
				.isFalse();
	}

	@Test
	public void givenUsedMetadataWhenIsUsedThenReturnTrue()
			throws Exception {
		String metadtaLocalCode = givenUsedMetadataInFolder();
		assertThat(Migration6_5_1_Helper.isUsed(metadtaLocalCode, Folder.SCHEMA_TYPE, zeCollection, getAppLayerFactory()))
				.isTrue();
	}

	@Test
	public void givenInexistingMetadataWhenDeleteIfPossibleOrDisableMetadataThenOk()
			throws Exception {
		Migration6_5_1_Helper
				.deleteIfPossibleOrDisableMetadata("inexistingMetadata", Folder.SCHEMA_TYPE, zeCollection, getAppLayerFactory());
	}

	@Test
	public void givenInexistingSchemaWhenDeleteIfPossibleOrDisableMetadataThenOK()
			throws Exception {
		Migration6_5_1_Helper
				.deleteIfPossibleOrDisableMetadata(Folder.CATEGORY, "inexistingSchemaType", zeCollection, getAppLayerFactory());
	}

	@Test
	public void givenUnusedMetadataWhenDeleteIfPossibleOrDisableMetadataThenDeleted()
			throws Exception {
		String metadtaLocalCode = givenUnusedMetadata();
		Migration6_5_1_Helper
				.deleteIfPossibleOrDisableMetadata(metadtaLocalCode, Folder.SCHEMA_TYPE, zeCollection, getAppLayerFactory());
		try {
			getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getDefaultSchema(Folder.SCHEMA_TYPE)
					.getMetadata(metadtaLocalCode);
			fail("Metadata should be deleted!");
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
			//OK
		}
	}

	@Test
	public void givenUsedMetadataWhenDeleteIfPossibleOrDisableMetadataThenDisabled()
			throws Exception {
		String metadtaLocalCode = givenUsedMetadataInFolder();
		Migration6_5_1_Helper
				.deleteIfPossibleOrDisableMetadata(metadtaLocalCode, Folder.SCHEMA_TYPE, zeCollection, getAppLayerFactory());
		Metadata metadata = getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(zeCollection).getDefaultSchema(Folder.SCHEMA_TYPE)
				.getMetadata(metadtaLocalCode);
		assertThat(metadata.isEnabled()).isFalse();
	}

	private String givenUsedMetadataInFolder()
			throws RecordServicesException {
		String metadataLocalCode = createNewStringMetadataInFolder();
		Folder folderA01 = records.getFolder_A01();
		recordServices.add((RecordWrapper) folderA01.set(metadataLocalCode, "nonNullValue"));
		return metadataLocalCode;
	}

	private String givenUnusedMetadata()
			throws RecordServicesException {
		String metadataLocalCode = createNewStringMetadataInFolder();
		Folder folderA01 = records.getFolder_A01();
		recordServices.add((RecordWrapper) folderA01.set(metadataLocalCode, null));
		return metadataLocalCode;
	}

	private String createNewStringMetadataInFolder() {
		final String newMetadataLocalCode = "zeMetaInFolder";
		getModelLayerFactory()
				.getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getDefaultSchema(Folder.SCHEMA_TYPE).createUndeletable(newMetadataLocalCode)
						.setType(MetadataValueType.STRING);
			}
		});

		return newMetadataLocalCode;
	}
}
