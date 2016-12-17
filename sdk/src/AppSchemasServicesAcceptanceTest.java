import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.Schemas.LINKED_SCHEMA;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsUnmodifiable;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.services.metadata.AppSchemasServices;
import com.constellio.app.services.metadata.AppSchemasServicesRuntimeException.AppSchemasServicesRuntimeException_CannotChangeCodeFromOrToDefault;
import com.constellio.app.services.metadata.AppSchemasServicesRuntimeException.AppSchemasServicesRuntimeException_CannotChangeCodeToOtherSchemaType;
import com.constellio.app.services.metadata.AppSchemasServicesRuntimeException.AppSchemasServicesRuntimeException_CannotDeleteSchema;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeCustomSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class AppSchemasServicesAcceptanceTest extends ConstellioTest {

	TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	ZeCustomSchemaMetadatas zeCustomSchema = schemas.new ZeCustomSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchemas = schemas.new AnotherSchemaMetadatas();
	ThirdSchemaMetadatas thirdSchemas = schemas.new ThirdSchemaMetadatas();

	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	AppSchemasServices appSchemasServices;
	MetadataSchemasManager schemasManager;
	SchemasDisplayManager schemasDisplayManager;

	public void setUpWithoutRecords()
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

		schemasManager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema("zeSchemaType_custom").getLabels().put(Language.French, "Ze french label");
				types.getSchema("zeSchemaType_custom").getLabels().put(Language.English, "Ze english label");
			}
		});
	}

	public void setUpWithRecords()
			throws Exception {
		setUpWithoutRecords();

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
		setUpWithRecords();
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
	public void givenAnotherSchemaHasTheCustomSchemaAsLinkedSchemaWhenModifyCodeOrDeleteSchemaThenModifyLinkedSchema()
			throws Exception {
		setUpWithoutRecords();
		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(thirdSchemas.code()).create(LINKED_SCHEMA.getLocalCode()).setType(STRING);
			}
		});
		recordServices.add(new TestRecord(thirdSchemas, "zeRecordWithLinkedSchema").set(LINKED_SCHEMA, "zeSchemaType_custom"));

		appSchemasServices.modifySchemaCode(zeCollection, "zeSchemaType_custom", "zeSchemaType_custom2");
		assertThat(recordServices.getDocumentById("zeRecordWithLinkedSchema").get(LINKED_SCHEMA))
				.isEqualTo("zeSchemaType_custom2");

		appSchemasServices.deleteSchemaCode(zeCollection, "zeSchemaType_custom2");
		assertThat(recordServices.getDocumentById("zeRecordWithLinkedSchema").get(LINKED_SCHEMA)).isNull();
	}

	@Test
	public void givenAnotherSchemaIsDirectlyReferencingASpecificSchemaThenTheReferencedSchemaIsNotDeletableAndReferenceMetadataUpdatedWhenRenamed()
			throws Exception {
		setUpWithoutRecords();

		//This is a behavior supported by the backend, but not by the UI. So, it is a very rare case!
		assertThat(appSchemasServices.isSchemaDeletable(zeCollection, "zeSchemaType_custom")).isTrue();

		schemasManager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(thirdSchemas.code()).create("zeProblematicReference")
						.defineReferencesTo(types.getSchema("zeSchemaType_custom"));
			}
		});
		assertThat(schemasManager.getSchemaTypes(zeCollection).getSchema(thirdSchemas.code()).get("zeProblematicReference")
				.getAllowedReferences().getAllowedSchemas()).containsOnly("zeSchemaType_custom");
		assertThat(appSchemasServices.isSchemaDeletable(zeCollection, "zeSchemaType_custom")).isFalse();

		appSchemasServices.modifySchemaCode(zeCollection, "zeSchemaType_custom", "zeSchemaType_custom2");
		assertThat(schemasManager.getSchemaTypes(zeCollection).getSchema(thirdSchemas.code()).get("zeProblematicReference")
				.getAllowedReferences().getAllowedSchemas()).containsOnly("zeSchemaType_custom2");

	}

	@Test
	public void whenModifyingSchemaCodeThenSchemaRenamedDisplayConfigsKeptAndRecordsHaveNewSchema()
			throws Exception {
		setUpWithRecords();
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
		assertThat(schemasManager.getSchemaTypes(zeCollection).getSchema("zeSchemaType_custom").getLabel(Language.French))
				.isEqualTo("Ze french label");
		assertThat(schemasManager.getSchemaTypes(zeCollection).getSchema("zeSchemaType_custom").getLabel(Language.English))
				.isEqualTo("Ze english label");
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
		assertThat(schemasManager.getSchemaTypes(zeCollection).getSchema("zeSchemaType_custom2").getLabel(Language.French))
				.isEqualTo("Ze french label");
		assertThat(schemasManager.getSchemaTypes(zeCollection).getSchema("zeSchemaType_custom2").getLabel(Language.English))
				.isEqualTo("Ze english label");

	}

	@Test
	public void givenSchemaWithRecordsThenNotDeletable()
			throws Exception {
		setUpWithRecords();

		assertThat(appSchemasServices.isSchemaDeletable(zeCollection, "zeSchemaType_custom")).isFalse();
		assertThat(appSchemasServices.isSchemaDeletable(zeCollection, "zeSchemaType_default")).isFalse();

		try {
			appSchemasServices.deleteSchemaCode(zeCollection, "zeSchemaType_custom");
			fail("schema should not be deletable");
		} catch (AppSchemasServicesRuntimeException_CannotDeleteSchema e) {
			//OK
		}

		try {
			appSchemasServices.deleteSchemaCode(zeCollection, "zeSchemaType_default");
			fail("schema should not be deletable");
		} catch (AppSchemasServicesRuntimeException_CannotDeleteSchema e) {
			//OK
		}

		assertThat(schemasManager.getSchemaTypes(zeCollection).getSchemaType("zeSchemaType").getAllSchemas()).extracting("code")
				.containsOnly("zeSchemaType_default", "zeSchemaType_custom");
		assertThat(schemasDisplayManager.getSchema(zeCollection, "zeSchemaType_custom").getDisplayMetadataCodes())
				.containsOnly("zeSchemaType_custom_title");

	}

	@Test
	public void givenSchemaWithoutRecordsWhenDeletedThenSchemaAndSchemaDisplayDeleted()
			throws Exception {
		setUpWithoutRecords();

		assertThat(appSchemasServices.isSchemaDeletable(zeCollection, "zeSchemaType_custom")).isTrue();
		assertThat(appSchemasServices.isSchemaDeletable(zeCollection, "zeSchemaType_default")).isFalse();
		assertThat(schemasManager.getSchemaTypes(zeCollection).getSchemaType("zeSchemaType").getAllSchemas()).extracting("code")
				.containsOnly("zeSchemaType_default", "zeSchemaType_custom");
		assertThat(schemasDisplayManager.getSchema(zeCollection, "zeSchemaType_custom").getDisplayMetadataCodes())
				.containsOnly("zeSchemaType_custom_title");

		appSchemasServices.deleteSchemaCode(zeCollection, "zeSchemaType_custom");

		assertThat(schemasManager.getSchemaTypes(zeCollection).getSchemaType("zeSchemaType").getAllSchemas()).extracting("code")
				.containsOnly("zeSchemaType_default");

		try {
			schemasDisplayManager.getSchema(zeCollection, "zeSchemaType_custom");
			fail("MetadataSchemasRuntimeException.NoSuchSchema expected");
		} catch (MetadataSchemasRuntimeException.NoSuchSchema e) {
			//OK
		}

	}
}
