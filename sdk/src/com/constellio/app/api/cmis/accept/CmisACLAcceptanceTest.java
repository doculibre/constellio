package com.constellio.app.api.cmis.accept;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup.Records;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.global.AuthorizationBuilder;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.DriverTest;
import com.constellio.sdk.tests.setups.Users;

@DriverTest
public class CmisACLAcceptanceTest extends ConstellioTest {
	UserServices userServices;
	TaxonomiesManager taxonomiesManager;
	MetadataSchemasManager metadataSchemasManager;
	RecordServices recordServices;
	Users users = new Users();
	CmisAcceptanceTestSetup zeCollectionSchemas = new CmisAcceptanceTestSetup(zeCollection);
	Records zeCollectionRecords;
	TaxonomiesSearchServices taxonomiesSearchServices;

	//	Session cmisSession;
	Session session;
	String adminToken;

	@Before
	public void setUp()
			throws Exception {

		userServices = getModelLayerFactory().newUserServices();
		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();

		taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();

		users.setUp(userServices);

		defineSchemasManager().using(zeCollectionSchemas);
		taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy2(), metadataSchemasManager);
		taxonomiesManager.setPrincipalTaxonomy(zeCollectionSchemas.getTaxonomy2(), metadataSchemasManager);
		zeCollectionRecords = zeCollectionSchemas.givenRecords(recordServices);

		userServices.addUserToCollection(users.alice(), zeCollection);
		userServices.addUserToCollection(users.bob(), zeCollection);
		userServices.addUserToCollection(users.charles(), zeCollection);
		userServices.addUserToCollection(users.dakotaLIndien(), zeCollection);
		userServices.addUserToCollection(users.edouardLechat(), zeCollection);
		userServices.addUserToCollection(users.gandalfLeblanc(), zeCollection);
		userServices.addUserToCollection(users.chuckNorris(), zeCollection);
		userServices.addUserToCollection(users.sasquatch(), zeCollection);
		userServices.addUserToCollection(users.robin(), zeCollection);

		userServices.addUserToCollection(users.admin(), zeCollection);
		userServices.addUserToCollection(users.chuckNorris(), zeCollection);

		recordServices.update(users.adminIn(zeCollection).setCollectionReadAccess(true).setCollectionWriteAccess(true));
		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionReadAccess(true));

		userServices.addUpdateUserCredential(users.admin().withServiceKey("admin-key"));
		getModelLayerFactory().newAuthenticationService().changePassword(admin, "1qaz2wsx");
		adminToken = userServices.generateToken(admin);

	}

	@Test
	public void whenSetACLThenAuthorizationsCreated()
			throws Exception {
		session = givenAdminSessionOnZeCollection();

		assertThat(session.getBinding().getAclService()).isNotNull();

		givenFolderInheritingTaxonomyAuthorizations();
		assertThatRecord(zeCollectionRecords.folder2).onlyUserWithReadPermissionAre(chuckNorris, admin, edouard);
		assertThatRecord(zeCollectionRecords.folder2_1).onlyUserWithWritePermissionAre(admin, edouard);

		//assertThat(cmisFolder(zeCollectionRecords.folder2).getAcl()).isNull();
		session.getDefaultContext().setIncludeAcls(true);

		Folder cmisFolder2 = cmisFolder(zeCollectionRecords.folder2);
		assertThat(cmisFolder2.getAcl().getAces()).extracting("direct", "permissions", "principalId").containsOnly(
				Tuple.tuple(false, asList("cmis:read", "cmis:write"), "edouard")
		);

		Ace heroesReadWriteACE = session.getObjectFactory().createAce("heroes", asList("cmis:read"));
		Ace bobGrattonReadWriteACE = session.getObjectFactory().createAce(bobGratton, asList("cmis:read", "cmis:write"));

		assertThat(cmisFolder2.getAllowableActions().getAllowableActions()).contains(Action.CAN_APPLY_ACL, Action.CAN_GET_ACL);

		cmisFolder2.addAcl(asList(heroesReadWriteACE, bobGrattonReadWriteACE), AclPropagation.REPOSITORYDETERMINED);
		waitForBatchProcess();
		//		session.getBinding().getAclService().applyAcl(session.getRepositoryInfo().getId(), cmisFolder2.getId(),
		//				new AccessControlListImpl(asList(heroesReadWriteACE)), null, AclPropagation.REPOSITORYDETERMINED, null);

		//cmisFolder2.setAcl(asList(heroesReadWriteACE, bobGrattonReadWriteACE));
		giveReadAccessToHeroesGroupUsingCMIS();

		assertThatRecord(zeCollectionRecords.folder2)
				.onlyUserWithReadPermissionAre(chuckNorris, admin, gandalf, charlesFrancoisXavier, dakota, bobGratton, edouard,
						robin);
		assertThatRecord(zeCollectionRecords.folder2_1).onlyUserWithWritePermissionAre(admin, bobGratton, edouard);

		cmisFolder2 = cmisFolder(zeCollectionRecords.folder2);
		assertThat(cmisFolder2.getAcl().getAces()).extracting("direct", "permissions", "principalId").containsOnly(
				Tuple.tuple(false, asList("cmis:read", "cmis:write"), edouard),
				Tuple.tuple(true, asList("cmis:read"), "heroes"),
				Tuple.tuple(true, asList("cmis:read", "cmis:write"), bobGratton)
		);

	}

	private Folder cmisFolder(Record record) {
		return (Folder) session.getObject(record.getId());
	}

	private void giveReadAccessToHeroesGroupUsingCMIS() {

		// REPOSITORYDETERMINED("repositorydetermined"), OBJECTONLY("objectonly"), PROPAGATE("propagate")
	}

	private void givenFolderInheritingTaxonomyAuthorizations() {
		Authorization authorization = new AuthorizationBuilder(zeCollection).forUsers(users.edouardIn(zeCollection))
				.on(zeCollectionRecords.taxo2_station2_1).givingReadWriteAccess();
		getModelLayerFactory().newAuthorizationsServices().add(authorization, users.adminIn(zeCollection));
		try {
			waitForBatchProcess();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private FolderRecordValidator assertThatRecord(Record folder) {
		return new FolderRecordValidator(folder.getId());
	}

	private class FolderRecordValidator {

		private String folderId;

		public FolderRecordValidator(String folderId) {
			this.folderId = folderId;
		}

		private void onlyUserWithReadPermissionAre(String... users) {

			List<String> userWithReadAccess = new ArrayList<>();
			UserServices userServices = getModelLayerFactory().newUserServices();
			Record record = getModelLayerFactory().newRecordServices().getDocumentById(folderId);
			for (User user : userServices.getAllUsersInCollection(zeCollection)) {
				if (user.hasReadAccess().on(record)) {
					userWithReadAccess.add(user.getUsername());
				}
			}
			assertThat(userWithReadAccess).containsOnly(users);
		}

		private void onlyUserWithWritePermissionAre(String... users) {

			List<String> userWithWriteAccess = new ArrayList<>();
			UserServices userServices = getModelLayerFactory().newUserServices();
			Record record = getModelLayerFactory().newRecordServices().getDocumentById(folderId);
			for (User user : userServices.getAllUsersInCollection(zeCollection)) {
				if (user.hasWriteAccess().on(record)) {
					userWithWriteAccess.add(user.getUsername());
				}
			}
			assertThat(userWithWriteAccess).containsOnly(users);
		}

	}

	private Session givenAdminSessionOnZeCollection() {
		return newCmisSessionBuilder().authenticatedBy("admin-key", adminToken).onCollection(zeCollection).build();
	}

}
