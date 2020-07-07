package com.constellio.app.api.admin.services;

import com.constellio.app.client.entities.MetadataResource;
import com.constellio.app.client.services.AdminServicesSession;
import com.constellio.app.client.services.SchemaServicesClient;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.testimpl.TestRecordValidator2;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class SchemaServicesAcceptTest extends ConstellioTest {

	Users users = new Users();
	UserServices userServices;
	AuthenticationService authService;

	AdminServicesSession bobSession;
	SchemaServicesClient driver;

	@Before
	public void setUp()
			throws Exception {
		String bobPassword = "bobPassword";
		prepareSystem(withZeCollection());

		userServices = getModelLayerFactory().newUserServices();
		authService = getModelLayerFactory().newAuthenticationService();

		users.setUp(userServices);

		userServices.givenSystemAdminPermissionsToUser(users.bob());

		String bobServiceKey = userServices.giveNewServiceToken(users.bob());
		authService.changePassword(users.bob().getUsername(), bobPassword);

		bobSession = newRestClient(bobServiceKey, users.bob().getUsername(), bobPassword);
		driver = bobSession.newSchemaServicesForCollection(zeCollection);

	}

	//This test is runned by AllAdminServicesAcceptTest
	public void whenUsingDriverThenCanConfigureSchemaTypes() {

		driver.createSchemaType("zeType");
		driver.createSchemaType("anotherType");
		driver.createSchemaType("thirdType");
		assertThat(driver.getSchemaTypes()).contains("group", "collection", "user", "zeType", "anotherType", "thirdType");
		assertThat(driver.getSchemas("zeType")).containsOnly("zeType_default");
		assertThat(driver.getSchemas("anotherType")).containsOnly("anotherType_default");
		assertThat(driver.getSchemas("thirdType")).containsOnly("thirdType_default");

		driver.createCustomSchema("zeType_custom1");
		driver.createCustomSchema("zeType_custom2");
		assertThat(driver.getSchemas("zeType")).containsOnly("zeType_default", "zeType_custom1", "zeType_custom2");
		validateSchemaHasDefaultMetadata("zeType_default");
		validateSchemaHasDefaultMetadata("zeType_custom1");
		validateSchemaHasDefaultMetadata("anotherType_default");

		validateCanAddAndRemoveSchemaValidators();

		canAddZeTypeReferenceToSelf();
		canAddAnotherTypeReferenceToSelf();
		canAddThirdTypeReferencesToZeType();
		canAddThirdTypeReferencesToAnotherType();
		canAddContentMetadataInZeTypeCustomSchema();
		canModifyCustomSchemaMetadatasInheritedFromDefaultSchema();
		canDefineTaxonomies();
	}

	private void canDefineTaxonomies() {
		assertThat(driver.getTaxonomies()).isEmpty();
		assertThat(driver.getPrincipalTaxonomy()).isEqualTo("");
		driver.createTaxonomy("aSecondaryTaxonomy", Arrays.asList("anotherType"));
		driver.createTaxonomy("thePrincipalTaxonomy", Arrays.asList("zeType", "thirdType"));
		driver.setAsPrincipalTaxonomy("thePrincipalTaxonomy");

		assertThat(driver.getTaxonomies()).containsOnly("aSecondaryTaxonomy", "thePrincipalTaxonomy");
		assertThat(driver.getPrincipalTaxonomy()).isEqualTo("thePrincipalTaxonomy");
		assertThat(driver.getTaxonomySchemaTypes("aSecondaryTaxonomy")).containsOnly("anotherType");
		assertThat(driver.getTaxonomySchemaTypes("thePrincipalTaxonomy")).containsOnly("zeType", "thirdType");
	}

	private void canAddZeTypeReferenceToSelf() {
		MetadataResource resource = new MetadataResource();
		resource.setCode("zeType_default_parent");
		resource.setAllowedReference("zeType");
		resource.setChildOfRelationship(true);
		resource.setEnabled(false);
		resource.setLabel("Ze parent");
		resource.setType(MetadataValueType.REFERENCE.name());
		driver.addUpdateMetadata(resource);

		resource = driver.getMetadata("zeType_default_parent");
		assertThat(resource.getCode()).isEqualTo("zeType_default_parent");
		assertThat(resource.getAllowedReference()).isEqualTo("zeType");
		assertThat(resource.getCalculator()).isNull();
		assertThat(resource.getDataStoreCode()).isEqualTo("parentPId_s");
		assertThat(resource.getLabel()).isEqualTo("Ze parent");
		assertThat(resource.getType()).isEqualTo(MetadataValueType.REFERENCE.name());
		assertThat(resource.getValidators()).isEmpty();
		assertThat(resource.getSearchable()).isFalse();
		assertThat(resource.getChildOfRelationship()).isTrue();
		assertThat(resource.getDefaultRequirement()).isFalse();
		assertThat(resource.getEnabled()).isFalse();
		assertThat(resource.getMultivalue()).isFalse();
		assertThat(resource.getUniqueValue()).isFalse();

		resource = new MetadataResource();
		resource.setCode("zeType_default_parent");
		resource.setEnabled(true);
		resource.setLabel("Ze ultimate parent");
		driver.addUpdateMetadata(resource);

		resource = driver.getMetadata("zeType_default_parent");
		assertThat(resource.getCode()).isEqualTo("zeType_default_parent");
		assertThat(resource.getAllowedReference()).isEqualTo("zeType");
		assertThat(resource.getCalculator()).isNull();
		assertThat(resource.getDataStoreCode()).isEqualTo("parentPId_s");
		assertThat(resource.getLabel()).isEqualTo("Ze ultimate parent");
		assertThat(resource.getType()).isEqualTo(MetadataValueType.REFERENCE.name());
		assertThat(resource.getValidators()).isEmpty();
		assertThat(resource.getSearchable()).isFalse();
		assertThat(resource.getChildOfRelationship()).isTrue();
		assertThat(resource.getDefaultRequirement()).isFalse();
		assertThat(resource.getEnabled()).isTrue();
		assertThat(resource.getMultivalue()).isFalse();
		assertThat(resource.getUniqueValue()).isFalse();

	}

	private void canAddThirdTypeReferencesToZeType() {
		MetadataResource resource = new MetadataResource();
		resource.setCode("thirdType_default_refToZetype");
		resource.setAllowedReference("zeType");
		resource.setType(MetadataValueType.REFERENCE.name());
		resource.setDefaultRequirement(true);
		driver.addUpdateMetadata(resource);

		resource = driver.getMetadata("thirdType_default_refToZetype");
		assertThat(resource.getCode()).isEqualTo("thirdType_default_refToZetype");
		assertThat(resource.getAllowedReference()).isEqualTo("zeType");
		assertThat(resource.getCalculator()).isNull();
		assertThat(resource.getDataStoreCode()).isEqualTo("refToZetypeId_s");
		assertThat(resource.getType()).isEqualTo(MetadataValueType.REFERENCE.name());
		assertThat(resource.getValidators()).isEmpty();
		assertThat(resource.getSearchable()).isFalse();
		assertThat(resource.getChildOfRelationship()).isFalse();
		assertThat(resource.getDefaultRequirement()).isTrue();
		assertThat(resource.getEnabled()).isTrue();
		assertThat(resource.getMultivalue()).isFalse();
		assertThat(resource.getUniqueValue()).isFalse();
	}

	private void canAddThirdTypeReferencesToAnotherType() {
		MetadataResource resource = new MetadataResource();
		resource.setCode("thirdType_default_refToAnotherType");
		resource.setAllowedReference("zeType");
		resource.setType(MetadataValueType.REFERENCE.name());
		resource.setMultivalue(true);
		driver.addUpdateMetadata(resource);

		resource = driver.getMetadata("thirdType_default_refToAnotherType");
		assertThat(resource.getCode()).isEqualTo("thirdType_default_refToAnotherType");
		assertThat(resource.getAllowedReference()).isEqualTo("zeType");
		assertThat(resource.getCalculator()).isNull();
		assertThat(resource.getDataStoreCode()).isEqualTo("refToAnotherTypeId_ss");
		assertThat(resource.getType()).isEqualTo(MetadataValueType.REFERENCE.name());
		assertThat(resource.getValidators()).isEmpty();
		assertThat(resource.getSearchable()).isFalse();
		assertThat(resource.getChildOfRelationship()).isFalse();
		assertThat(resource.getDefaultRequirement()).isFalse();
		assertThat(resource.getEnabled()).isTrue();
		assertThat(resource.getMultivalue()).isTrue();
		assertThat(resource.getUniqueValue()).isFalse();
	}

	private void canAddContentMetadataInZeTypeCustomSchema() {
		MetadataResource resource = new MetadataResource();
		resource.setCode("zeType_custom1_content");
		resource.setType(MetadataValueType.CONTENT.name());
		resource.setMultivalue(true);
		resource.setSearchable(true);
		driver.addUpdateMetadata(resource);

		resource = driver.getMetadata("zeType_custom1_content");
		assertThat(resource.getCode()).isEqualTo("zeType_custom1_content");
		assertThat(resource.getAllowedReference()).isNull();
		assertThat(resource.getCalculator()).isNull();
		assertThat(resource.getDataStoreCode()).isEqualTo("content_ss");
		assertThat(resource.getType()).isEqualTo(MetadataValueType.CONTENT.name());
		assertThat(resource.getValidators()).isEmpty();
		assertThat(resource.getSearchable()).isTrue();
		assertThat(resource.getChildOfRelationship()).isFalse();
		assertThat(resource.getDefaultRequirement()).isFalse();
		assertThat(resource.getEnabled()).isTrue();
		assertThat(resource.getMultivalue()).isTrue();
		assertThat(resource.getUniqueValue()).isFalse();
	}

	private void canModifyCustomSchemaMetadatasInheritedFromDefaultSchema() {
		MetadataResource resource = new MetadataResource();
		resource.setCode("zeType_custom1_title");
		resource.setLabel("Ze custom title");
		resource.setDefaultRequirement(true);
		driver.addUpdateMetadata(resource);

		resource = driver.getMetadata("zeType_default_title");
		assertThat(resource.getLabel()).isNotEqualTo("Ze custom title");
		assertThat(resource.getDefaultRequirement()).isFalse();

		resource = driver.getMetadata("zeType_custom1_title");
		assertThat(resource.getLabel()).isEqualTo("Ze custom title");
		assertThat(resource.getDefaultRequirement()).isTrue();

	}

	private void canAddAnotherTypeReferenceToSelf() {
		MetadataResource resource = new MetadataResource();
		resource.setCode("anotherType_default_parent");
		resource.setAllowedReference("zeType");
		resource.setChildOfRelationship(true);
		resource.setEnabled(true);
		resource.setType(MetadataValueType.REFERENCE.name());
		driver.addUpdateMetadata(resource);

		assertThat(driver.getMetadata("anotherType_default_parent").getEnabled()).isTrue();
		driver.disableMetadata("anotherType_default_parent");
		assertThat(driver.getMetadata("anotherType_default_parent").getEnabled()).isFalse();

		resource = new MetadataResource();
		resource.setCode("anotherType_default_parent");
		resource.setEnabled(true);
		driver.addUpdateMetadata(resource);
		assertThat(driver.getMetadata("anotherType_default_parent").getEnabled()).isTrue();
	}

	private void validateCanAddAndRemoveSchemaValidators() {
		assertThat(driver.getSchemaValidators("zeType_default")).isEmpty();
		driver.addSchemaValidator("zeType_default", TestRecordValidator2.class.getCanonicalName());
		assertThat(driver.getSchemaValidators("zeType_default")).containsOnly(TestRecordValidator2.class.getCanonicalName());
		driver.removeSchemaValidator("zeType_default", TestRecordValidator2.class.getCanonicalName());
		assertThat(driver.getSchemaValidators("zeType_default")).isEmpty();
	}

	private void validateSchemaHasDefaultMetadata(String schema) {

		String titleCode = schema + "_title";
		String modifiedOnCode = schema + "_modifiedOn";
		String createdOnCode = schema + "_createdOn";
		String modifiedByCode = schema + "_modifiedBy";
		String createdByCode = schema + "_createdBy";
		String schemaCode = schema + "_schema";
		String idCode = schema + "_id";
		String pathCode = schema + "_path";
		String parentpathCode = schema + "_parentpath";
		String deletedCode = schema + "_deleted";

		assertThat(driver.getSchemaMetadataCodes(schema)).contains(titleCode, modifiedOnCode, createdOnCode, modifiedByCode,
				createdByCode, schemaCode, idCode, pathCode, parentpathCode, deletedCode);

		MetadataResource title = driver.getMetadata(titleCode);
		assertThat(title.getCode()).isEqualTo(titleCode);
		assertThat(title.getAllowedReference()).isNull();
		assertThat(title.getCalculator()).isNull();
		assertThat(title.getDataStoreCode()).isEqualTo("title_s");
		assertThat(title.getType()).isEqualTo(MetadataValueType.STRING.name());
		assertThat(title.getValidators()).isEmpty();
		assertThat(title.getSearchable()).isTrue();
		assertThat(title.getChildOfRelationship()).isFalse();
		assertThat(title.getDefaultRequirement()).isFalse();
		assertThat(title.getEnabled()).isTrue();
		assertThat(title.getMultivalue()).isFalse();
		assertThat(title.getUniqueValue()).isFalse();

		MetadataResource modifiedOn = driver.getMetadata(modifiedOnCode);
		assertThat(modifiedOn.getCode()).isEqualTo(modifiedOnCode);
		assertThat(modifiedOn.getAllowedReference()).isNull();
		assertThat(modifiedOn.getCalculator()).isNull();
		assertThat(modifiedOn.getDataStoreCode()).isEqualTo("modifiedOn_dt");
		assertThat(modifiedOn.getType()).isEqualTo(MetadataValueType.DATE_TIME.name());
		assertThat(modifiedOn.getValidators()).isEmpty();
		assertThat(modifiedOn.getSearchable()).isFalse();
		assertThat(modifiedOn.getChildOfRelationship()).isFalse();
		assertThat(modifiedOn.getDefaultRequirement()).isFalse();
		assertThat(modifiedOn.getEnabled()).isTrue();
		assertThat(modifiedOn.getMultivalue()).isFalse();
		assertThat(modifiedOn.getUniqueValue()).isFalse();

		MetadataResource createdBy = driver.getMetadata(createdByCode);
		assertThat(createdBy.getCode()).isEqualTo(createdByCode);
		assertThat(createdBy.getAllowedReference()).isEqualTo("user");
		assertThat(createdBy.getCalculator()).isNull();
		assertThat(createdBy.getDataStoreCode()).isEqualTo("createdById_s");
		assertThat(createdBy.getType()).isEqualTo(MetadataValueType.REFERENCE.name());
		assertThat(createdBy.getValidators()).isEmpty();
		assertThat(createdBy.getSearchable()).isFalse();
		assertThat(createdBy.getChildOfRelationship()).isFalse();
		assertThat(createdBy.getDefaultRequirement()).isFalse();
		assertThat(createdBy.getEnabled()).isTrue();
		assertThat(createdBy.getMultivalue()).isFalse();
		assertThat(createdBy.getUniqueValue()).isFalse();

		MetadataResource path = driver.getMetadata(pathCode);
		assertThat(path.getCode()).isEqualTo(pathCode);
		assertThat(path.getAllowedReference()).isNull();
		assertThat(path.getCalculator()).isEqualTo("com.constellio.model.services.schemas.calculators.PathCalculator");
		assertThat(path.getDataStoreCode()).isEqualTo("path_ss");
		assertThat(path.getType()).isEqualTo(MetadataValueType.STRING.name());
		assertThat(path.getValidators()).isEmpty();
		assertThat(path.getSearchable()).isFalse();
		assertThat(path.getChildOfRelationship()).isFalse();
		assertThat(path.getDefaultRequirement()).isFalse();
		assertThat(path.getEnabled()).isTrue();
		assertThat(path.getMultivalue()).isTrue();
		assertThat(path.getUniqueValue()).isFalse();

		MetadataResource deleted = driver.getMetadata(deletedCode);
		assertThat(deleted.getCode()).isEqualTo(deletedCode);
		assertThat(deleted.getAllowedReference()).isNull();
		assertThat(deleted.getCalculator()).isNull();
		assertThat(deleted.getDataStoreCode()).isEqualTo("deleted_s");
		assertThat(deleted.getType()).isEqualTo(MetadataValueType.BOOLEAN.name());
		assertThat(deleted.getValidators()).isEmpty();
		assertThat(deleted.getSearchable()).isFalse();
		assertThat(deleted.getChildOfRelationship()).isFalse();
		assertThat(deleted.getDefaultRequirement()).isFalse();
		assertThat(deleted.getEnabled()).isTrue();
		assertThat(deleted.getMultivalue()).isFalse();
		assertThat(deleted.getUniqueValue()).isFalse();

	}

}
