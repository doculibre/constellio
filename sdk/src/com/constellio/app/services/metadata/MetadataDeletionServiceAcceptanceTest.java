package com.constellio.app.services.metadata;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_CalculatedMetadataSource;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_CopiedMetadataReference;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_CopiedMetadataSource;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_ExtractedMetadataSource;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_FacetMetadata;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_InheritedMetadata;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_PopulatedMetadata;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_SystemMetadata;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.NoSuchMetadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataPopulateConfigsBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.constellio.app.modules.rm.model.enums.CopyType.PRINCIPAL;
import static com.constellio.app.services.metadata.DeletionProhibitionReason.CALCULATED_METADATA_SOURCE;
import static com.constellio.app.services.metadata.DeletionProhibitionReason.COPIED_METADATA_REFERENCE;
import static com.constellio.app.services.metadata.DeletionProhibitionReason.COPIED_METADATA_SOURCE;
import static com.constellio.app.services.metadata.DeletionProhibitionReason.EXTRACTED_METADATA_SOURCE;
import static com.constellio.app.services.metadata.DeletionProhibitionReason.FACET_METADATA;
import static com.constellio.app.services.metadata.DeletionProhibitionReason.INHERITED_METADATA;
import static com.constellio.app.services.metadata.DeletionProhibitionReason.POPULATED_METADATA;
import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class MetadataDeletionServiceAcceptanceTest extends ConstellioTest {
	private RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	MetadataDeletionService metadataDeletionService;
	Metadata userMetadataInCustomSchema, userMetadataInCustomSchema2, mappedMetadata, systemMetadata, directlyPopulatedMetadata,
			populatedMetadataInInheritance,
			inheritedMetadata, copiedMetadataSource, copiedMetadataDestination,
			copiedMetadataReference, calculatedMetadataLocalDependencySource, calculatedMetadataReferenceDependencySource,
			calculatedMetadataDestination, extractedMetadataSource, extractedMetadataDestination, facetMetadata;

	private MetadataSchemasManager schemaManager;
	private Metadata userEncryptedMetadataWithDefaultValue;
	private RecordServices recordServices;
	private MetadataSchemaBuilder folderSchema, customFolderSchema, documentSchema, documentCustomSchema;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(
						records).withFoldersAndContainersOfEveryStatus()
		);
		schemaManager = getModelLayerFactory().getMetadataSchemasManager();
		metadataDeletionService = new MetadataDeletionService(getAppLayerFactory(), zeCollection);
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		initTestData();
	}

	private void initTestData()
			throws OptimisticLocking, RecordServicesException {
		MetadataSchemaTypesBuilder schemaTypesBuilder = schemaManager
				.modify(zeCollection);
		MetadataSchemaTypeBuilder folderSchemaType = schemaTypesBuilder.getSchemaType(Folder.SCHEMA_TYPE);
		folderSchema = schemaTypesBuilder.getSchema(Folder.DEFAULT_SCHEMA);
		MetadataSchemaBuilder administrativeUnitSchema = schemaTypesBuilder.getSchema(AdministrativeUnit.DEFAULT_SCHEMA);
		customFolderSchema = folderSchemaType.createCustomSchema("customFolder");
		MetadataSchemaBuilder customFolderSchema2 = folderSchemaType.createCustomSchema("customFolder2");

		MetadataBuilder mappedMetadataBuilderWithDefaultValue = folderSchema.create("MAPMetadata").setType(STRING)
				.setDefaultValue("default");
		MetadataBuilder userEncryptedMetadataBuilderWithDefaultValue = folderSchema.create("USREncryptMetadata").setType(STRING)
				.setDefaultValue("encrypted").setEncrypted(true);
		MetadataBuilder userMetadataBuilder = customFolderSchema.createUndeletable("USRMetadata").setType(BOOLEAN);
		customFolderSchema2.createUndeletable("USRMetadata").setType(BOOLEAN);
		MetadataBuilder systemMetadataBuilder = folderSchema.createUndeletable("systemMetadata").setType(STRING);
		MetadataBuilder populatedMetadataBuilder = folderSchema.createUndeletable("USRPopulatedMetadata").setType(STRING);
		folderSchema.createUndeletable("populatedMetadataInInheritance").setType(STRING);
		MetadataBuilder inheritedMetadataBuilder = folderSchema.createUndeletable("USRInheritedMetadata").setType(DATE);
		MetadataBuilder copiedMetadataSourceBuilder = folderSchema.createUndeletable("USRCopySourceMetadata").setType(DATE);
		MetadataBuilder copiedMetadataReferenceBuilder = folderSchema.createUndeletable("USRCopiedReferenceMetadata")
				.defineChildOfRelationshipToType(folderSchemaType);
		MetadataBuilder copiedMetadataDestinationBuilder = folderSchema.createUndeletable("USRCopyDestinationMetadata")
				.setType(DATE)
				.defineDataEntry().asCopied(copiedMetadataReferenceBuilder, copiedMetadataSourceBuilder);

		MetadataBuilder localDependencyInCalculatorBuilder = folderSchema
				.createUndeletable("USRCalculatedLocalDependencyMetadata")
				.setType(BOOLEAN);
		MetadataBuilder referenceDependencyInCalculatorBuilder = administrativeUnitSchema
				.createUndeletable("USRCalculatedReferenceDependencyMetadata")
				.setType(BOOLEAN);
		MetadataBuilder calculatedMetadataDestinationBuilder = folderSchema.createUndeletable("USRCalculatedDestinationMetadata")
				.setType(BOOLEAN).defineDataEntry().asCalculated(TestCalculator.class);
		MetadataBuilder extractedMetadataSourceBuilder = folderSchema.createUndeletable("USRExtractedSourceMetadata")
				.defineAsEnum(CopyType.class);

		MetadataPopulateConfigsBuilder metadataPopulateConfigsBuilder = MetadataPopulateConfigsBuilder.create();
		metadataPopulateConfigsBuilder.setProperties(asList("USRExtractedSourceMetadata"));
		MetadataBuilder extractedMetadataDestinationBuilder = folderSchema.createUndeletable("USRExtractedDestinationMetadata")
				.defineAsEnum(CopyType.class);
		extractedMetadataDestinationBuilder.definePopulateConfigsBuilder(metadataPopulateConfigsBuilder);

		MetadataBuilder facetMetadataBuilder = folderSchema.createUndeletable("USRFacetMetadata").setType(STRING)
				.setDefaultValue("facet default value");

		schemaManager.saveUpdateSchemaTypes(schemaTypesBuilder);
		recordServices.add((RecordWrapper) records.getFolder_A02()
				.set(populatedMetadataBuilder.getLocalCode(), "directly populated metadata value")
				.set(userEncryptedMetadataBuilderWithDefaultValue.getLocalCode(), "encrypted"));
		recordServices.add(rm.newFacetField().setFieldDataStoreCode(facetMetadataBuilder.getCode()).setTitle("Ze Facet"));

		MetadataSchemaTypes types = schemaManager.getSchemaTypes(zeCollection);
		populatedMetadataInInheritance = types
				.getMetadata(folderSchema.getCode() + "_" + "populatedMetadataInInheritance");

		Folder customFolder = new Folder(rm.create(types.getSchema(customFolderSchema.getCode())), types);
		recordServices.add((RecordWrapper) customFolder.setTitle("customFolder").setOpenDate(TimeProvider.getLocalDate())
				.setAdministrativeUnitEntered(records.getUnit10())
				.setCategoryEntered(records.getCategory_X()).setRetentionRuleEntered(records.getRule1())
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL)
				.set("populatedMetadataInInheritance", "populated metadata value"));
		directlyPopulatedMetadata = types.getMetadata(populatedMetadataBuilder.getCode());

		mappedMetadata = types.getMetadata(mappedMetadataBuilderWithDefaultValue.getCode());
		userEncryptedMetadataWithDefaultValue = types.getMetadata(userEncryptedMetadataBuilderWithDefaultValue.getCode());
		userMetadataInCustomSchema = types.getMetadata(userMetadataBuilder.getCode());
		userMetadataInCustomSchema2 = types.getMetadata(customFolderSchema2.getCode() + "_" + userMetadataBuilder.getLocalCode());
		systemMetadata = types.getMetadata(systemMetadataBuilder.getCode());

		inheritedMetadata = types.getMetadata(customFolderSchema.getCode() + "_" + inheritedMetadataBuilder.getLocalCode());
		copiedMetadataSource = types.getMetadata(copiedMetadataSourceBuilder.getCode());
		copiedMetadataDestination = types.getMetadata(copiedMetadataDestinationBuilder.getCode());
		copiedMetadataReference = types.getMetadata(copiedMetadataReferenceBuilder.getCode());
		calculatedMetadataLocalDependencySource = types.getMetadata(localDependencyInCalculatorBuilder.getCode());
		calculatedMetadataReferenceDependencySource = types.getMetadata(referenceDependencyInCalculatorBuilder.getCode());
		calculatedMetadataDestination = types.getMetadata(calculatedMetadataDestinationBuilder.getCode());
		extractedMetadataSource = types.getMetadata(extractedMetadataSourceBuilder.getCode());
		extractedMetadataDestination = types.getMetadata(extractedMetadataDestinationBuilder.getCode());
		facetMetadata = types.getMetadata(facetMetadataBuilder.getCode());
		createMetadataWithSameNamesInDocumentSchema();

	}

	private void createMetadataWithSameNamesInDocumentSchema()
			throws OptimisticLocking {
		MetadataSchemaTypesBuilder schemaTypesBuilder = schemaManager
				.modify(zeCollection);
		MetadataSchemaTypeBuilder documetSchemaType = schemaTypesBuilder.getSchemaType(Document.SCHEMA_TYPE);
		documentSchema = documetSchemaType.getDefaultSchema();
		documentCustomSchema = documetSchemaType.createCustomSchema("customFolder2");

		documentSchema.create("MAPMetadata").setType(STRING)
				.setDefaultValue("default");
		documentSchema.create("USREncryptMetadata").setType(STRING)
				.setDefaultValue("encrypted").setEncrypted(true);
		documentSchema.createUndeletable("USRMetadata").setType(BOOLEAN);
		documentSchema.createUndeletable("systemMetadata").setType(STRING);
		documentSchema.createUndeletable("USRPopulatedMetadata").setType(STRING);
		documentSchema.createUndeletable("populatedMetadataInInheritance").setType(STRING);
		documentSchema.createUndeletable("USRInheritedMetadata").setType(DATE);
		documentSchema.createUndeletable("USRCopySourceMetadata").setType(DATE);
		documentSchema.createUndeletable("USRCopiedReferenceMetadata").setType(STRING);
		documentSchema.createUndeletable("USRCopyDestinationMetadata").setType(DATE);
		documentSchema.createUndeletable("USRCalculatedLocalDependencyMetadata").setType(BOOLEAN);
		documentSchema.createUndeletable("USRCalculatedDestinationMetadata").setType(BOOLEAN);
		documentSchema.createUndeletable("USRExtractedSourceMetadata").defineAsEnum(CopyType.class);
		documentSchema.createUndeletable("USRExtractedDestinationMetadata").defineAsEnum(CopyType.class);
		documentSchema.createUndeletable("USRFacetMetadata").setType(STRING).setDefaultValue("facet default value");

		schemaManager.saveUpdateSchemaTypes(schemaTypesBuilder);
	}

	@Test
	public void whenIsMetadataDeletableThenReturnExpectedValue() {
		assertThat(metadataDeletionService.isMetadataDeletable(userMetadataInCustomSchema.getCode())).isTrue();
		assertThat(metadataDeletionService.isMetadataDeletable(mappedMetadata.getCode())).isTrue();
		assertThat(metadataDeletionService.isMetadataDeletable(systemMetadata.getCode())).isFalse();

		assertThat(metadataDeletionService.isMetadataDeletable(userMetadataInCustomSchema.getLocalCode())).isTrue();
		assertThat(metadataDeletionService.isMetadataDeletable(mappedMetadata.getLocalCode())).isTrue();
		assertThat(metadataDeletionService.isMetadataDeletable(systemMetadata.getLocalCode())).isFalse();
	}

	@Test
	public void whenCanDeleteMetadataThenBehavesAsExpected() {
		assertThat(metadataDeletionService.canDeleteMetadata(userMetadataInCustomSchema.getCode())).isNull();
		assertThat(metadataDeletionService.canDeleteMetadata(mappedMetadata.getCode())).isNull();
		assertThat(metadataDeletionService.canDeleteMetadata(copiedMetadataDestination.getCode())).isNull();
		assertThat(metadataDeletionService.canDeleteMetadata(calculatedMetadataDestination.getCode())).isNull();
		assertThat(metadataDeletionService.canDeleteMetadata(extractedMetadataDestination.getCode())).isNull();
		assertThat(metadataDeletionService.canDeleteMetadata(userEncryptedMetadataWithDefaultValue.getCode())).isNotNull();
		assertThat(metadataDeletionService.canDeleteMetadata(inheritedMetadata.getCode())).isEqualTo(INHERITED_METADATA);
		assertThat(metadataDeletionService.canDeleteMetadata(directlyPopulatedMetadata.getCode())).isEqualTo(POPULATED_METADATA);
		assertThat(metadataDeletionService.canDeleteMetadata(populatedMetadataInInheritance
				.getCode())).isEqualTo(POPULATED_METADATA);
		assertThat(metadataDeletionService.canDeleteMetadata(facetMetadata.getCode()))
				.isEqualTo(FACET_METADATA);

		System.out.println(metadataDeletionService.canDeleteMetadata(calculatedMetadataLocalDependencySource.getCode()));
		assertThat(metadataDeletionService.canDeleteMetadata(calculatedMetadataLocalDependencySource.getCode()))
				.isEqualTo(CALCULATED_METADATA_SOURCE);
		assertThat(metadataDeletionService.canDeleteMetadata(extractedMetadataSource.getCode()))
				.isEqualTo(EXTRACTED_METADATA_SOURCE);
		assertThat(metadataDeletionService.canDeleteMetadata(calculatedMetadataReferenceDependencySource.getCode()))
				.isEqualTo(CALCULATED_METADATA_SOURCE);
		assertThat(metadataDeletionService.canDeleteMetadata(copiedMetadataReference.getCode()))
				.isEqualTo(COPIED_METADATA_REFERENCE);
		assertThat(metadataDeletionService.canDeleteMetadata(copiedMetadataSource.getCode()))
				.isEqualTo(COPIED_METADATA_SOURCE);
	}

	@Test
	public void whenDeleteMetadataThenBehavesAsExpected()
			throws MetadataDeletionException {
		metadataDeletionService.deleteMetadata(folderSchema.getCode() + "_" + mappedMetadata.getLocalCode());
		assertMetadataDeletedCorrectly(mappedMetadata);
		metadataDeletionService.deleteMetadata(userMetadataInCustomSchema.getCode());
		assertMetadataDeletedCorrectly(userMetadataInCustomSchema);
		assertThat(schemaManager.getSchemaTypes(zeCollection).getMetadata(userMetadataInCustomSchema2.getCode())).isNotNull();

		try {
			metadataDeletionService.deleteMetadata(userEncryptedMetadataWithDefaultValue.getCode());
			fail("encrypted metadata does not discriminate the default value. If there is one it counted a data.");
		} catch (MetadataDeletionException_PopulatedMetadata e) {
			assertMetadataNotDeletedAndCanBeDeletedFromDocumentSchema(userEncryptedMetadataWithDefaultValue);
		}

		try {
			metadataDeletionService.deleteMetadata(systemMetadata.getCode());
			fail("non user metadata deletion is forbidden");
		} catch (MetadataDeletionException_SystemMetadata e) {
			//OK
			assertMetadataNotDeletedAndCanBeDeletedFromDocumentSchema(systemMetadata);
		}

		try {
			metadataDeletionService.deleteMetadata(directlyPopulatedMetadata.getCode());
			fail("populated metadata deletion is forbidden");
		} catch (MetadataDeletionException_PopulatedMetadata e) {
			//OK
			assertMetadataNotDeletedAndCanBeDeletedFromDocumentSchema(directlyPopulatedMetadata);
		}

		try {
			metadataDeletionService.deleteMetadata(extractedMetadataSource.getCode());
			fail("extracted metadata source deletion is forbidden");
		} catch (MetadataDeletionException_ExtractedMetadataSource e) {
			//OK
			assertMetadataNotDeletedAndCanBeDeletedFromDocumentSchema(extractedMetadataSource);
		}

		try {
			metadataDeletionService.deleteMetadata(facetMetadata.getCode());
			fail("facet metadata deletion is forbidden");
		} catch (MetadataDeletionException_FacetMetadata e) {
			//OK
			assertMetadataNotDeletedAndCanBeDeletedFromDocumentSchema(facetMetadata);
		}

		try {
			metadataDeletionService.deleteMetadata(inheritedMetadata.getCode());
			fail("inherited metadata deletion is forbidden");
		} catch (MetadataDeletionException_InheritedMetadata e) {
			//OK
			assertMetadataNotDeletedAndCanBeDeletedFromDocumentSchema(inheritedMetadata);
		}

		try {
			metadataDeletionService.deleteMetadata(calculatedMetadataLocalDependencySource.getCode());
			fail("calculated metadata source deletion is forbidden");
		} catch (MetadataDeletionException_CalculatedMetadataSource e) {
			//OK
			assertMetadataNotDeletedAndCanBeDeletedFromDocumentSchema(calculatedMetadataLocalDependencySource);
		}

		try {
			metadataDeletionService.deleteMetadata(calculatedMetadataReferenceDependencySource.getCode());
			fail("calculated metadata source deletion is forbidden");
		} catch (MetadataDeletionException_CalculatedMetadataSource e) {
			//OK
			assertMetadataNotDeletedAndCanBeDeletedFromDocumentSchema(calculatedMetadataReferenceDependencySource);
		}

		try {
			metadataDeletionService.deleteMetadata(copiedMetadataReference.getCode());
			fail("copied metadata reference deletion is forbidden");
		} catch (MetadataDeletionException_CopiedMetadataReference e) {
			//OK
			assertMetadataNotDeletedAndCanBeDeletedFromDocumentSchema(copiedMetadataReference);
		}

		try {
			metadataDeletionService.deleteMetadata(copiedMetadataSource.getCode());
			fail("copied metadata source deletion is forbidden");
		} catch (MetadataDeletionException_CopiedMetadataSource e) {
			//OK
			assertMetadataNotDeletedAndCanBeDeletedFromDocumentSchema(copiedMetadataSource);
		}

		metadataDeletionService.deleteMetadata(copiedMetadataDestination.getCode());
		assertMetadataDeletedCorrectly(copiedMetadataDestination);
		metadataDeletionService.deleteMetadata(calculatedMetadataDestination.getCode());
		assertMetadataDeletedCorrectly(calculatedMetadataDestination);
		metadataDeletionService.deleteMetadata(extractedMetadataDestination.getCode());
		assertMetadataDeletedCorrectly(extractedMetadataDestination);
	}

	private void assertMetadataNotDeletedAndCanBeDeletedFromDocumentSchema(Metadata metadata)
			throws MetadataDeletionException {
		MetadataSchemaTypes types = schemaManager.getSchemaTypes(zeCollection);
		Metadata metadataFound = types.getMetadata(metadata.getCode());
		assertThat(metadataFound).isNotNull();
		//FIXME enlever commentaire
		/*try {
			String metadataCode = documentCustomSchema
					.getCode() + "_" + metadata.getLocalCode();
			types.getMetadata(metadataCode);
			fail("Metadata should be deleted " + metadataCode);
		} catch (NoSuchMetadata e) {
			//OK
		}*/
	}

	private void assertMetadataDeletedCorrectly(Metadata metadata) {
		MetadataSchemaTypes types = schemaManager.getSchemaTypes(zeCollection);
		assertDeletedFromDefaultFolderSchema(types, metadata.getLocalCode());
		assertDeletedFromCustomFolderSchema(types, metadata.getLocalCode());
		assertNotDeletedFromDefaultDocumentSchema(types, metadata.getLocalCode());
		assertNotDeletedFromCustomDocumentSchema(types, metadata.getLocalCode());
	}

	private void assertNotDeletedFromCustomDocumentSchema(MetadataSchemaTypes types, String localCode) {
		String metadataCode = documentCustomSchema.getCode() + "_" + localCode;
		Metadata metadata = types.getMetadata(metadataCode);
		assertThat(metadata).isNotNull();
	}

	private void assertNotDeletedFromDefaultDocumentSchema(MetadataSchemaTypes types, String localCode) {
		String metadataCode = documentSchema.getCode() + "_" + localCode;
		Metadata metadata = types.getMetadata(metadataCode);
		assertThat(metadata).isNotNull();
	}

	private void assertDeletedFromCustomFolderSchema(MetadataSchemaTypes types, String localCode) {
		try {
			String metadataCode = customFolderSchema
										  .getCode() + "_" + localCode;
			types.getMetadata(metadataCode);
			fail("Metadata should be deleted " + metadataCode);
		} catch (NoSuchMetadata e) {
			//OK
		}
	}

	private void assertDeletedFromDefaultFolderSchema(MetadataSchemaTypes types, String localCode) {
		try {
			String metadataCode = folderSchema.getCode() + "_" + localCode;
			types.getMetadata(metadataCode);
			fail("Metadata should be deleted " + metadataCode);
		} catch (NoSuchMetadata e) {
			//OK
		}
	}

	public static class TestCalculator extends AbstractMetadataValueCalculator<Boolean> {

		ReferenceDependency<Boolean> referenceDependency = ReferenceDependency
				.toABoolean(Folder.ADMINISTRATIVE_UNIT, "USRCalculatedReferenceDependencyMetadata");
		LocalDependency<Boolean> localDependency = LocalDependency.toABoolean("USRCalculatedLocalDependencyMetadata");

		@Override
		public Boolean calculate(CalculatorParameters parameters) {
			Boolean code = parameters.get(referenceDependency);
			if (code == null) {
				return null;
			}
			return !code;
		}

		@Override
		public Boolean getDefaultValue() {
			return null;
		}

		@Override
		public MetadataValueType getReturnType() {
			return BOOLEAN;
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return asList(referenceDependency, localDependency);
		}
	}
}
