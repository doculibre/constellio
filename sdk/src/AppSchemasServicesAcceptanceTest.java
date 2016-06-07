import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsUnmodifiable;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.services.metadata.AppSchemasServices;
import com.constellio.app.services.metadata.AppSchemasServicesRuntimeException.AppSchemasServicesRuntimeException_CannotChangeCodeFromOrToDefault;
import com.constellio.app.services.metadata.AppSchemasServicesRuntimeException.AppSchemasServicesRuntimeException_CannotChangeCodeToOtherSchemaType;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeCustomSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class AppSchemasServicesAcceptanceTest extends ConstellioTest {

	TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	ZeCustomSchemaMetadatas zeCustomSchema = schemas.new ZeCustomSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchemas = schemas.new AnotherSchemaMetadatas();

	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	AppSchemasServices appSchemasServices;
	MetadataSchemasManager schemasManager;
	SchemasDisplayManager schemasDisplayManager;

	@Before
	public void setUp()
			throws Exception {
		defineSchemasManager().using(schemas.andCustomSchema().withAStringMetadataInCustomSchema(whichIsUnmodifiable));
		appSchemasServices = getAppLayerFactory().newSchemasServices();
		recordServices = getModelLayerFactory().newRecordServices();
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		schemasDisplayManager.saveSchema(schemasDisplayManager.getSchema(zeCollection, "zeSchemaType_custom")
				.withDisplayMetadataCodes(asList("zeSchemaType_custom_title")));

		schemasDisplayManager.saveMetadata(schemasDisplayManager.getMetadata(zeCollection, "zeSchemaType_custom_customString")
				.withVisibleInAdvancedSearchStatus(true));

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeCustomSchema, "r1")
				.set(Schemas.TITLE, "Record 1").set(zeCustomSchema.customStringMetadata(), "custom value 1"));
		transaction.add(new TestRecord(zeCustomSchema, "r2")
				.set(Schemas.TITLE, "Record 2").set(zeCustomSchema.customStringMetadata(), "custom value 2"));

		transaction.add(new TestRecord(zeSchema, "r3").set(Schemas.TITLE, "Record 3"));
		getModelLayerFactory().newRecordServices().execute(transaction);
	}

	@Test
	public void whenModifyingSchemaCodeThenExceptionIfFromOrToDefault()
			throws Exception {

		try {
			appSchemasServices.modifySchemaCode(zeCollection, zeSchema.code(), zeCustomSchema.code());
			fail("AppSchemasServicesRuntimeException_CannotChangeCodeFromOrToDefault expected");
		} catch (AppSchemasServicesRuntimeException_CannotChangeCodeFromOrToDefault e) {
			//OK
		}

		try {
			appSchemasServices.modifySchemaCode(zeCollection, zeCustomSchema.code(), zeSchema.code());
			fail("AppSchemasServicesRuntimeException_CannotChangeCodeFromOrToDefault expected");
		} catch (AppSchemasServicesRuntimeException_CannotChangeCodeFromOrToDefault e) {
			//OK
		}

		try {
			appSchemasServices.modifySchemaCode(zeCollection, zeCustomSchema.code(), "anotherSchemaType_custom");
			fail("AppSchemasServicesRuntimeException_CannotChangeCodeFromOrToDefault expected");
		} catch (AppSchemasServicesRuntimeException_CannotChangeCodeToOtherSchemaType e) {
			//OK
		}
	}

	@Test
	public void whenModifyingSchemaCodeThenSchemaRenamedDisplayConfigsKeptAndRecordsHaveNewSchema()
			throws Exception {

		//Validate initial state
		assertThat(recordServices.getDocumentById("r1").getSchemaCode()).isEqualTo("zeSchemaType_custom");
		assertThat(recordServices.getDocumentById("r1").get(zeCustomSchema.customStringMetadata())).isEqualTo("custom value 1");
		assertThat(recordServices.getDocumentById("r2").getSchemaCode()).isEqualTo("zeSchemaType_custom");
		assertThat(recordServices.getDocumentById("r2").get(zeCustomSchema.customStringMetadata())).isEqualTo("custom value 2");
		assertThat(recordServices.getDocumentById("r3").getSchemaCode()).isEqualTo("zeSchemaType_default");
		assertThat(schemasManager.getSchemaTypes(zeCollection).getSchemaType("zeSchemaType").getAllSchemas()).extracting("code")
				.containsOnly("zeSchemaType_default", "zeSchemaType_custom");
		assertThat(schemasManager.getSchemaTypes(zeCollection).getMetadata("zeSchemaType_custom_customString").isUnmodifiable())
				.isTrue();
		assertThat(schemasDisplayManager.getSchema(zeCollection, "zeSchemaType_custom").getDisplayMetadataCodes())
				.containsOnly("zeSchemaType_custom_title");
		assertThat(schemasDisplayManager.getMetadata(zeCollection, "zeSchemaType_custom_customString")
				.isVisibleInAdvancedSearch()).isTrue();

		appSchemasServices.modifySchemaCode(zeCollection, "zeSchemaType_custom", "zeSchemaType_custom2");

		//Validate state after code modification
		assertThat(recordServices.getDocumentById("r1").getSchemaCode()).isEqualTo("zeSchemaType_custom2");
		assertThat(recordServices.getDocumentById("r1").get(zeCustomSchema.customStringMetadata())).isEqualTo("custom value 1");
		assertThat(recordServices.getDocumentById("r2").getSchemaCode()).isEqualTo("zeSchemaType_custom2");
		assertThat(recordServices.getDocumentById("r2").get(zeCustomSchema.customStringMetadata())).isEqualTo("custom value 2");
		assertThat(recordServices.getDocumentById("r3").getSchemaCode()).isEqualTo("zeSchemaType_default");
		assertThat(schemasManager.getSchemaTypes(zeCollection).getSchemaType("zeSchemaType").getAllSchemas()).extracting("code")
				.containsOnly("zeSchemaType_default", "zeSchemaType_custom2");
		assertThat(schemasManager.getSchemaTypes(zeCollection).getMetadata("zeSchemaType_custom2_customString").isUnmodifiable())
				.isTrue();
		assertThat(schemasDisplayManager.getSchema(zeCollection, "zeSchemaType_custom2").getDisplayMetadataCodes())
				.containsOnly("zeSchemaType_custom2_title");
		assertThat(schemasDisplayManager.getMetadata(zeCollection, "zeSchemaType_custom2_customString")
				.isVisibleInAdvancedSearch()).isTrue();

	}
}
