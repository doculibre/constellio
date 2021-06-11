package com.constellio.model.entities.records.wrappers;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataAccessRestriction;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataAccessRestrictionBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.apache.ignite.internal.util.lang.GridFunc.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class UserMetadataSecurityAcceptanceTest extends ConstellioTest {

	Users users = new Users();
	RecordServices recordServices;
	ModelLayerFactory modelLayerFactory;
	RMSchemasRecordsServices rmSchemasRecordsServices;
	RMTestRecords records = new RMTestRecords(zeCollection);
	AuthorizationsServices authorizationsServices;


	@Before
	public void setUp() {
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withAllTest(users).withFoldersAndContainersOfEveryStatus());
		recordServices = getModelLayerFactory().newRecordServices();
		rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		modelLayerFactory = getModelLayerFactory();
		authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
	}

	@Test
	public void whenUserHasAccesToRecordWithASpecificAutorizationRoleWithSameRoleRequiredByRequiredReadRoles()
			throws RecordServicesException {
		User user = users.aliceIn(zeCollection);
		ArrayList<String> roles = new ArrayList<>();

		user.setCollectionReadAccess(false);
		recordServices.update(user);

		Folder folder = rmSchemasRecordsServices.getFolder(records.folder_A16);

		// Validate that the user does not have the permission directly
		assertThat(user.getAllRoles().contains("ADM")).isEqualTo(false);

		roles.addAll(asList("ADM"));

		List<String> principals = new ArrayList<>();
		principals.addAll(asList(user.getId()));
		AuthorizationAddRequest authorization = AuthorizationAddRequest.authorizationInCollection(zeCollection).giving(roles)
				.forPrincipalsIds(principals).on(folder.getWrappedRecord())
				.startingOn(null).endingOn(null);
		authorizationsServices.add(authorization, user);

		setFolderCategoryMetadataWithAccessRestriction("ADM");

		Metadata matadata = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(zeCollection).getSchema(Folder.DEFAULT_SCHEMA).getMetadata(Folder.CATEGORY);
		assertThat(user.hasAccessToMetadata(matadata, folder.getWrappedRecord())).isTrue();
	}

	@Test
	public void whenUserHasAccessToRecordAndRequiredReadRolesIsEmptyForCategoryMetadataThenFalse()
			throws RecordServicesException {
		User user = users.aliceIn(zeCollection);

		recordServices.update(user);
		Folder folder = rmSchemasRecordsServices.getFolderSummary(records.folder_A16);

		recordServices.recalculate(user);

		Metadata matadata = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(zeCollection).getSchema(Folder.DEFAULT_SCHEMA).getMetadata(Folder.CATEGORY);
		assertThat(user.hasAccessToMetadata(matadata, folder.getWrappedRecord())).isTrue();
	}

	@Test
	public void whenUserHasNotAccessToRecordWitchContainMetadataThenFalse()
			throws RecordServicesException {
		User user = users.aliceIn(zeCollection);

		user.setCollectionReadAccess(false);
		recordServices.update(user);

		Folder folder = rmSchemasRecordsServices.getFolder(records.folder_A16);

		setFolderCategoryMetadataWithAccessRestriction("ADM");

		Metadata matadata = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(zeCollection).getSchema(Folder.DEFAULT_SCHEMA).getMetadata(Folder.CATEGORY);
		assertThat(user.hasAccessToMetadata(matadata, folder.getWrappedRecord())).isFalse();
	}

	private void setFolderCategoryMetadataWithAccessRestriction(String role) {
		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaBuilder folderRecordSchema = types.getSchema(Folder.DEFAULT_SCHEMA);
				MetadataBuilder metadataBuilder = folderRecordSchema.getMetadata(Folder.CATEGORY);

				MetadataAccessRestrictionBuilder allowedReferencesBuilder = MetadataAccessRestrictionBuilder.modify(new MetadataAccessRestriction());
				allowedReferencesBuilder.getRequiredReadRoles().add("ADM");
				metadataBuilder.setAccessRestrictionBuilder(allowedReferencesBuilder);
			}
		});
	}

	@Test
	public void whenUserAGlobalAccessToAMetadataThentrue()
			throws RecordServicesException {
		User user = users.aliceIn(zeCollection);


		user.setUserRoles("ADM");
		recordServices.update(user.getWrappedRecord());

		setFolderCategoryMetadataWithAccessRestriction("ADM");
		Metadata parent = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(zeCollection).getSchema(Folder.DEFAULT_SCHEMA).getMetadata(Folder.CATEGORY);
		assertThat(user.hasGlobalAccessToMetadata(parent)).isTrue();
	}

	@Test
	public void whenUserdoesNotHaveAGlobalAccessToAMetadataThenfalse()
			throws RecordServicesException {
		User user = users.aliceIn(zeCollection);

		user.setUserRoles("U");
		recordServices.update(user.getWrappedRecord());

		setFolderCategoryMetadataWithAccessRestriction("ADM");

		Metadata parent = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(zeCollection).getSchema(Folder.DEFAULT_SCHEMA).getMetadata(Folder.CATEGORY);
		assertThat(user.hasGlobalAccessToMetadata(parent)).isFalse();
	}
}
