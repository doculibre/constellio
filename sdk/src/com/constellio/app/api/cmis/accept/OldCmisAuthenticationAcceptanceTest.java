package com.constellio.app.api.cmis.accept;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup.Records;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.data.utils.hashing.HashingServiceException;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.chemistry.opencmis.client.api.Session;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.constellio.data.conf.HashingEncoding.BASE64;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class OldCmisAuthenticationAcceptanceTest extends ConstellioTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(OldCmisAuthenticationAcceptanceTest.class);

	String anotherCollection = "anotherCollection";
	CmisAcceptanceTestSetup anotherCollectionSchemas = new CmisAcceptanceTestSetup(anotherCollection);
	String thirdCollection = "thirdCollection";
	UserServices userServices;
	TaxonomiesManager taxonomiesManager;
	MetadataSchemasManager metadataSchemasManager;
	RecordServices recordServices;
	Users users = new Users();
	CmisAcceptanceTestSetup zeCollectionSchemas = new CmisAcceptanceTestSetup(zeCollection);
	Records zeCollectionRecords;
	Records anotherCollectionRecords;
	TaxonomiesSearchServices taxonomiesSearchServices;

	Session cmisSessionChuck;
	Session cmisSessionBob;

	AuthenticationService authenticationService;
	ConfigManager configManager;
	HashingService hashingService;

	@Before
	public void setUp()
			throws Exception {

		authenticationService = getModelLayerFactory().newAuthenticationService();
		configManager = getDataLayerFactory().getConfigManager();
		hashingService = getIOLayerFactory().newHashingService(BASE64);

		userServices = getModelLayerFactory().newUserServices();
		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();

		taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();

		users.setUp(userServices, zeCollection);

		defineSchemasManager().using(zeCollectionSchemas);
		defineSchemasManager().using(anotherCollectionSchemas);
		CmisAcceptanceTestSetup.allSchemaTypesSupported(getAppLayerFactory());
		taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy2(), metadataSchemasManager);
		taxonomiesManager.setPrincipalTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		taxonomiesManager.addTaxonomy(anotherCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		taxonomiesManager.setPrincipalTaxonomy(anotherCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		zeCollectionRecords = zeCollectionSchemas.givenRecords(recordServices);
		anotherCollectionRecords = anotherCollectionSchemas.givenRecords(recordServices);

		givenChuckAndBobPasswordsProperties();
		userServices.execute(users.bob().getUsername(), (req) -> req.addToCollection(zeCollection));
		userServices.execute(users.chuckNorris().getUsername(), (req) -> req.addToCollection(zeCollection));
		userServices.execute(users.chuckNorris().getUsername(), (req) -> req.addToCollection(zeCollection));
		CmisAcceptanceTestSetup.giveUseCMISPermissionToUsers(getModelLayerFactory());
	}
	//
	//	@Test
	//	public void givenMultipleCollectionsWithMultipleTaxonomies()
	//			throws Exception {
	//
	//		assertThat(authenticationService.authenticate(chuckNorris, "1qaz2wsx")).isTrue();
	//		assertThat(authenticationService.authenticate(chuckNorris, "soleil")).isFalse();
	//
	//		runSubTest(new thenChuckAndBobCanConnectInZeCollection());
	//		givenWrongPasswordThenChuckCannotConnectInZeCollection();
	//		thenChuckCanConnectInAnotherCollection();
	//		thenBobCannotConnectInAnotherCollection();
	//		thenChuckCannotConnectInInexistentCollection();
	//		whenCreateNewCollectionThenUsersCanConnectInIt();
	//	}
	//
	//	@Test
	//	public void givenWriteDeletePermissionsToChuckInCollectionAndReadPermissionToBobWhenChuckLoginAsBobThenCanConnect()
	//			throws Exception {
	//
	//		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionWriteAccess(true).getWrappedRecord());
	//		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionDeleteAccess(true).getWrappedRecord());
	//		Session cmisSessionChuckAsBob = newCmisSessionBuilder().authenticatedBy(chuckNorris, "1qaz2wsx")
	//				.logedAs(bobGratton).onCollection(zeCollection).build();
	//
	//		assertThat(cmisSessionChuckAsBob.getRootFolder().getProperty("cmis:path").getValue()).isEqualTo("/");
	//	}
	//
	//	@Test
	//	public void givenWriteDeletePermissionsToChuckInCollectionAndReadPermissionToBoboWhenBobLoginAsChuckThenCannotConnect()
	//			throws Exception {
	//
	//		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionWriteAccess(true).getWrappedRecord());
	//		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionDeleteAccess(true).getWrappedRecord());
	//		try {
	//			newCmisSessionBuilder().authenticatedBy(bobGratton, "1qaz2wsx").logedAs(chuckNorris).onCollection(zeCollection)
	//					.build();
	//			fail();
	//		} catch (Exception e) {
	//			assertThat(true);
	//		}
	//	}
	//
	//	@Test
	//	public void givenWritePermissionsToChuckInCollectionAndReadPermissionToBobWhenChuckLoginAsBobThenCannotConnect()
	//			throws Exception {
	//
	//		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionWriteAccess(true).getWrappedRecord());
	//		try {
	//			newCmisSessionBuilder().authenticatedBy(chuckNorris, "1qaz2wsx").logedAs(bobGratton).onCollection(zeCollection)
	//					.build();
	//			fail();
	//		} catch (Exception e) {
	//			assertThat(true);
	//		}
	//	}
	//
	//	@Test
	//	public void givenDeletePermissionsToChuckInCollectionAndReadPermissionToBobWhenChuckLoginAsBobThenCannotConnect()
	//			throws Exception {
	//
	//		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionDeleteAccess(true).getWrappedRecord());
	//		try {
	//			newCmisSessionBuilder().authenticatedBy(chuckNorris, "1qaz2wsx").logedAs(bobGratton).onCollection(zeCollection)
	//					.build();
	//			fail();
	//		} catch (Exception e) {
	//			assertThat(true);
	//		}
	//	}
	//
	//	@Test
	//	public void givenWriteDeletePermissionsToChuckInAnotherCollectionAndNoPermissionToBobWhenChuckLoginAsBobThenCannotConnect()
	//			throws Exception {
	//
	//		recordServices.update(users.chuckNorrisIn(anotherCollection).setCollectionWriteAccess(true).getWrappedRecord());
	//		recordServices.update(users.chuckNorrisIn(anotherCollection).setCollectionDeleteAccess(true).getWrappedRecord());
	//		try {
	//			newCmisSessionBuilder().authenticatedBy(chuckNorris, "1qaz2wsx").logedAs(bobGratton).onCollection(
	//					anotherCollection)
	//					.build();
	//			fail();
	//		} catch (Exception e) {
	//			assertThat(true);
	//		}
	//	}
	//
	//	@Test
	//	public void givenNewCollectionAndAddChuckInCollectionWhenAuthenticateThenChuckCanConnectInIt()
	//			throws Exception {
	//		whenCreateNewCollectionThenUsersCanConnectInIt();
	//	}

	private void givenWrongPasswordThenChuckCannotConnectInZeCollection() {
		try {
			Session cmisSessionChuck = newCmisSessionBuilder().authenticatedBy(chuckNorris, "wrongPassword")
					.onCollection(anotherCollection).build();
			cmisSessionChuck.getRootFolder().getProperty("cmis:path").getValue();
			fail();
		} catch (Exception e) {
			assertThat(true);
		}
	}

	private void thenChuckCanConnectInAnotherCollection() {
		cmisSessionChuck = newCmisSessionBuilder().authenticatedBy(chuckNorris, "1qaz2wsx").onCollection(anotherCollection)
				.build();
		assertThat(cmisSessionChuck.getRootFolder().getProperty("cmis:path").<String>getValue()).isEqualTo("/");
	}

	private void thenBobCannotConnectInAnotherCollection() {
		try {
			cmisSessionBob = newCmisSessionBuilder().authenticatedBy(bobGratton, "xsw21qaz").onCollection(anotherCollection)
					.build();
			cmisSessionBob.getRootFolder().getProperty("cmis:path").getValue();
			fail();
		} catch (Exception e) {
			assertThat(true);
		}
	}

	private void thenChuckCannotConnectInInexistentCollection() {
		try {
			cmisSessionChuck = newCmisSessionBuilder().authenticatedBy(chuckNorris, "1qaz2wsx").onCollection(
					"inexistentCollection")
					.build();
			cmisSessionChuck.getRootFolder().getProperty("cmis:path").getValue();
			fail();
		} catch (Exception e) {
			assertThat(true);
		}
	}

	private void whenCreateNewCollectionThenUsersCanConnectInIt() {
		givenCollection(thirdCollection);

		userServices.execute(users.chuckNorris().getUsername(), (req) -> req.addToCollection(thirdCollection));

		Session cmisSessionChuckThirdCollection = newCmisSessionBuilder().authenticatedBy(chuckNorris, "1qaz2wsx")
				.onCollection(thirdCollection).build();
		assertThat(cmisSessionChuckThirdCollection.getRootFolder().getProperty("cmis:path").<String>getValue()).isEqualTo("/");
	}

	// --- ---
	private void givenChuckAndBobPasswordsProperties()
			throws HashingServiceException {
		authenticationService.changePassword(chuckNorris, "1qaz2wsx");
		authenticationService.changePassword(bobGratton, "xsw21qaz");
	}

	public class thenChuckAndBobCanConnectInZeCollection extends SubTest {

		@Override
		public void run() {
			cmisSessionChuck = newCmisSessionBuilder().authenticatedBy(chuckNorris, "1qaz2wsx").onCollection(zeCollection)
					.build();
			assertThat(cmisSessionChuck.getRootFolder().getProperty("cmis:path").<String>getValue()).isEqualTo("/");
			cmisSessionBob = newCmisSessionBuilder().authenticatedBy(bobGratton, "xsw21qaz").onCollection(zeCollection)
					.build();
			assertThat(cmisSessionBob.getRootFolder().getProperty("cmis:path").<String>getValue()).isEqualTo("/");
		}
	}

}
