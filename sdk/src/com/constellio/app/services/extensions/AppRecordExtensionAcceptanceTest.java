package com.constellio.app.services.extensions;

import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AppRecordExtensionAcceptanceTest extends ConstellioTest {
	private SchemasRecordsServices schemasRecordsServices;
	private MetadataSchemasManager metadataSchemasManager;
	private RecordServices recordServices;

	@Before
	public void setUp() {
		prepareSystem(withZeCollection());

		schemasRecordsServices = new SchemasRecordsServices(zeCollection, getModelLayerFactory());

		metadataSchemasManager = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();
		metadataSchemasManager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = types.getSchema(TemporaryRecord.DEFAULT_SCHEMA).getMetadata(TemporaryRecord.TITLE);
				metadataBuilder.setMaxLength(5);
			}
		});
	}

	@Test
	public void givenRecordWithSchemaThatHave5CharactersLimitOnTitleWhenSavingTitleWith6CharactersThenThrow()
			throws Exception {
		TemporaryRecord temporaryRecord = schemasRecordsServices.newTemporaryRecord();

		temporaryRecord.setTitle("123456");

		try {
			recordServices.add(temporaryRecord.getWrappedRecord());
			Assert.fail("title is to long it should throw");
		} catch (ValidationException e) {
			assertThat(e.getErrors().getValidationErrors().get(0).getValidatorErrorCode()).isEqualTo("metadataValueDoesntRespectMaxLength");
		}
	}

	@Test
	public void givenRecordWithSchemaThatHave5CharactersLimitOnTitleWhenSavingTitleWith5CharactersThenOk()
			throws Exception {
		TemporaryRecord temporaryRecord = schemasRecordsServices.newTemporaryRecord();

		temporaryRecord.setTitle("12345");

		try {
			recordServices.add(temporaryRecord.getWrappedRecord());
		} catch (ValidationException e) {
			Assert.fail("title is the right size it should not throw");
		}
	}
}
