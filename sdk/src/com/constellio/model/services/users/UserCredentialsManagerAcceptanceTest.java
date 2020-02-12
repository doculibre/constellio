package com.constellio.model.services.users;

import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.conf.PropertiesModelLayerConfiguration.InMemoryModelLayerConfiguration;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.encrypt.EncryptionKeyFactory;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.factories.ModelLayerFactoryUtils;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.ModelLayerConfigurationAlteration;
import com.constellio.sdk.tests.QueryCounter;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.entities.records.wrappers.Collection.SYSTEM_COLLECTION;
import static com.constellio.sdk.tests.QueryCounter.ON_COLLECTION;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class UserCredentialsManagerAcceptanceTest extends ConstellioTest {

	LocalDateTime shishOClock = new LocalDateTime();

	String edouardServiceKey = "myKey";

	SolrUserCredentialsManager manager;
	UserCredential chuckUserCredential, edouardUserCredential, bobUserCredential;
	CollectionsListManager collectionsListManager;
	LocalDateTime endDate = new LocalDateTime().plusMinutes(30);
	List<String> msExchDelegateListBL = new ArrayList<>();

	@Before
	public void setUp()
			throws Exception {

		givenCollection("collection1");
		givenCollection("zeCollection");
		givenDisabledAfterTestValidations();
		withSpiedServices(ModelLayerConfiguration.class, CollectionsListManager.class);
		configure(new ModelLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryModelLayerConfiguration configuration) {
				configuration.setTokenDuration(org.joda.time.Duration.standardSeconds(42));
			}
		});

		collectionsListManager = getModelLayerFactory().getCollectionsListManager();

		msExchDelegateListBL = new ArrayList<>();
		msExchDelegateListBL.add("msExchDelegateListBL1");
		msExchDelegateListBL.add("msExchDelegateListBL2");

		Key key = EncryptionKeyFactory.newApplicationKey("zePassword", "zeUltimateSalt");
		ModelLayerFactoryUtils.setApplicationEncryptionKey(getModelLayerFactory(), key);

		manager = getModelLayerFactory().getUserCredentialsManager();

		createUserCredentials();
	}

	@Test
	public void whenAddingAndSearchingCredentialsThenUsernamesAreNormalized() {
		manager.addUpdate(edouardUserCredential);
		assertThat(manager.getUserCredential("Edouard")).isNotNull();
		assertThat(manager.getUserCredential("EDOUARD")).isNotNull();
		assertThat(manager.getUserCredential("Édouard")).isNotNull();
	}

	@Test
	public void whenAddUsersCredentialsThenTheyAreAddedInListOnce()
			throws Exception {
		manager.addUpdate(chuckUserCredential);
		manager.addUpdate(edouardUserCredential);
		manager.addUpdate(chuckUserCredential);

		UserCredential admin = manager.getUserCredential("admin");

		assertThat(manager.getActiveUserCredentials()).extracting("firstName", "lastName").containsOnly(
				tuple(admin.getFirstName(), admin.getLastName()),
				tuple(chuckUserCredential.getFirstName(), chuckUserCredential.getLastName()),
				tuple(edouardUserCredential.getFirstName(), edouardUserCredential.getLastName()));
	}

	@Test
	public void givenHasInvalidCollectionWhenReadThenHAsOnlyValidCollections() {
		manager.addUpdate(edouardUserCredential);
		assertThat(manager.getUserCredential("edouard").getCollections()).containsOnly(zeCollection, "collection1");
	}

	@Test
	public void givenUserCredentialInListWhenUpdateUserCredentialThenHeIsUpdated()
			throws Exception {

		manager.addUpdate(chuckUserCredential);
		manager.addUpdate(edouardUserCredential);

		chuckUserCredential = manager.create("chuck", "Chuck1", "Norris1", "chuck.norris1@gmail.com",
				asList("group11"), asList(zeCollection, "collection1"), UserCredentialStatus.ACTIVE, "domain",
				msExchDelegateListBL, null);
		manager.addUpdate(chuckUserCredential);


		assertThat(manager.getActiveUserCredentials()).extracting("username")
				.containsOnly(chuckUserCredential.getUsername(), edouardUserCredential.getUsername(), "admin");

		assertThat(manager.getUserCredential("chuck")).isEqualToComparingFieldByField(chuckUserCredential);
		assertThat(manager.getUserCredential("edouard")).isEqualToComparingFieldByField(edouardUserCredential);
		assertThat(manager.getUserCredential("edouard").getAccessTokens().get("token1")).isEqualTo(endDate);


	}

	@Test
	public void whenAddCollectionToExistingUserCredentialThenItIsUpdated()
			throws Exception {

		manager.addUpdate(chuckUserCredential);

		chuckUserCredential = manager.create("chuck", "Chuck", "Norris", "chuck.norris@gmail.com", asList("group1"),
				asList(zeCollection, "collection1"), UserCredentialStatus.ACTIVE, "domain", msExchDelegateListBL, null);

		manager.addUpdate(chuckUserCredential);
		assertThat(manager.getActiveUserCredentials()).hasSize(2);
		assertThat(manager.getUserCredential("chuck")).isEqualToComparingFieldByField(chuckUserCredential);
	}

	@Test
	public void givenUserCredentialInGlobalGroupWhenGetUserCredentialInGlobalGroupThenItIsReturned()
			throws Exception {
		manager.addUpdate(chuckUserCredential);
		manager.addUpdate(edouardUserCredential);

		assertThat(manager.getUserCredentialsInGlobalGroup("group1")).hasSize(1);
		assertThat(manager.getUserCredentialsInGlobalGroup("group1").get(0)).isEqualToComparingFieldByField(chuckUserCredential);
		assertThat(manager.getUserCredentialsInGlobalGroup("group2")).hasSize(1);
		assertThat(manager.getUserCredentialsInGlobalGroup("group2").get(0))
				.isEqualToComparingFieldByField(edouardUserCredential);
		assertThat(manager.getUserCredentialsInGlobalGroup("group3")).isEmpty();
	}

	@Test
	public void givenMultipleUserAndGroupsAndMultipleWhenInitializedThenCacheLoadedAndUsingIt()
			throws Exception {

		Users users = new Users().setUp(getModelLayerFactory().newUserServices());

		ModelLayerFactory otherInstanceModelLayer = getModelLayerFactory("other");
		SolrUserCredentialsManager otherInstanceUserCredentialsManager = otherInstanceModelLayer.getUserCredentialsManager();
		SolrGlobalGroupsManager otherInstanceGlobalGroupsManager = otherInstanceModelLayer.getGlobalGroupsManager();

		QueryCounter queryCounter = new QueryCounter(otherInstanceModelLayer.getDataLayerFactory(),
				ON_COLLECTION(SYSTEM_COLLECTION));

		assertThat(otherInstanceUserCredentialsManager.getUserCredential("alice").getLastName()).isEqualTo("Wonderland");
		assertThat(otherInstanceUserCredentialsManager.getUserCredential("bob").getLastName()).isEqualTo("Gratton");
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);

		otherInstanceGlobalGroupsManager.getActiveGlobalGroupWithCode("heroes");
		otherInstanceGlobalGroupsManager.getActiveGlobalGroupWithCode("rumors");
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);

		SchemasRecordsServices schemas = new SchemasRecordsServices(zeCollection, otherInstanceModelLayer);
		assertThat(schemas.isGroupActive(schemas.getGroupWithCode("legends"))).isTrue();
		assertThat(schemas.isGroupActive(schemas.getGroupWithCode("sidekicks"))).isTrue();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
	}

	@Test
	public void givenUserCredentialWhenRemoveCollectionThenRemoveItFromAllUsers()
			throws Exception {

		manager.addUpdate(chuckUserCredential);
		manager.addUpdate(edouardUserCredential);

		manager.removeCollection(zeCollection);

		Set<String> collections = getAllCollectionsInUserCredentialFile();
		assertThat(collections).doesNotContain(zeCollection);
	}

	@Test
	public void givenUserCredentialWhenRemoveTokenThenItIsRemoved()
			throws Exception {

		manager.addUpdate(chuckUserCredential);
		manager.addUpdate(edouardUserCredential);

		manager.removeToken("token1");

		assertThat(manager.getServiceKeyByToken("token1")).isNull();
		assertThat(manager.getServiceKeyByToken("token2")).isNotNull();
	}

	@Test
	public void whenRemoveUserCredentialThenItIsRemoved()
			throws Exception {
		manager.addUpdate(chuckUserCredential);
		manager.addUpdate(edouardUserCredential);

		manager.removeUserCredentialFromCollection(chuckUserCredential, zeCollection);

		assertThat(manager.getActiveUserCredentials()).extracting("username")
				.containsOnly(chuckUserCredential.getUsername(), edouardUserCredential.getUsername(), "admin");

	}

	@Test
	public void whenChangeStatusUserThenOk()
			throws Exception {
		manager.addUpdate(chuckUserCredential);
		manager.addUpdate(edouardUserCredential);

		chuckUserCredential = chuckUserCredential.setStatus(UserCredentialStatus.DELETED);
		manager.addUpdate(chuckUserCredential);

		assertThat(manager.getActiveUserCredentials()).hasSize(2);
		assertThat(manager.getDeletedUserCredentials()).hasSize(1);
		assertThat(manager.getDeletedUserCredentials().get(0).getUsername()).isEqualTo(chuckUserCredential.getUsername());

		chuckUserCredential = chuckUserCredential.setStatus(UserCredentialStatus.SUSPENDED);
		manager.addUpdate(chuckUserCredential);

		assertThat(manager.getActiveUserCredentials()).hasSize(2);
		assertThat(manager.getDeletedUserCredentials()).isEmpty();
		assertThat(manager.getSuspendedUserCredentials().get(0).getUsername()).isEqualTo(chuckUserCredential.getUsername());

		chuckUserCredential = chuckUserCredential.setStatus(UserCredentialStatus.PENDING);
		manager.addUpdate(chuckUserCredential);

		assertThat(manager.getActiveUserCredentials()).hasSize(2);
		assertThat(manager.getSuspendedUserCredentials()).isEmpty();
		assertThat(manager.getPendingApprovalUserCredentials().get(0).getUsername()).isEqualTo(chuckUserCredential.getUsername());

		chuckUserCredential = chuckUserCredential.setStatus(UserCredentialStatus.ACTIVE);
		manager.addUpdate(chuckUserCredential);

		assertThat(manager.getActiveUserCredentials()).hasSize(3);
		assertThat(manager.getPendingApprovalUserCredentials()).isEmpty();
	}

	@Test
	public void whenRemoveGroupThenItIsRemoved()
			throws Exception {
		manager.addUpdate(chuckUserCredential);
		manager.addUpdate(edouardUserCredential);

		manager.removeGroup("group1");

		assertThat(manager.getUserCredential("chuck").getGlobalGroups()).isEmpty();
		assertThat(manager.getUserCredential("edouard").getGlobalGroups()).hasSize(1);
		assertThat(manager.getUserCredential("edouard").getGlobalGroups().get(0)).isEqualTo("group2");
	}

	@Test
	public void givenTwoTokensWhenGetServiceKeyByTokenThenReturnIt()
			throws Exception {

		manager.addUpdate(edouardUserCredential);

		String serviceKey = manager.getServiceKeyByToken("token1");
		String serviceKey2 = manager.getServiceKeyByToken("token2");
		String serviceKey3 = manager.getServiceKeyByToken("token3");

		assertThat(serviceKey).isEqualTo(serviceKey2).isEqualTo(edouardServiceKey);
		assertThat(serviceKey3).isNull();
	}

	@Test
	public void givenUserHasTokensThenAreAutomaticallyDeletedAfterTheGivenTime()
			throws Exception {
		String tokenA = UUIDV1Generator.newRandomId();
		String tokenB = UUIDV1Generator.newRandomId();
		String tokenC = UUIDV1Generator.newRandomId();

		givenTimeIs(shishOClock.minusYears(1));

		manager.addUpdate(chuckUserCredential);
		manager.addUpdate(bobUserCredential);

		manager.addUpdate(chuckUserCredential
				.addAccessToken(tokenA, shishOClock).addAccessToken(tokenB, shishOClock.plusHours(1)));
		manager.addUpdate(bobUserCredential.addAccessToken(tokenC, shishOClock.plusMinutes(1)));

		givenTimeIs(shishOClock.minusSeconds(1));
		manager.removeTimedOutTokens();
		assertThat(manager.getUserCredential(chuckUserCredential.getUsername()).getTokenKeys()).containsOnly(tokenA, tokenB);
		assertThat(manager.getUserCredential(bobUserCredential.getUsername()).getTokenKeys()).containsOnly(tokenC);

		givenTimeIs(shishOClock);
		manager.removeTimedOutTokens();
		assertThat(manager.getUserCredential(chuckUserCredential.getUsername()).getTokenKeys()).containsOnly(tokenB);
		assertThat(manager.getUserCredential(bobUserCredential.getUsername()).getTokenKeys()).containsOnly(tokenC);

		givenTimeIs(shishOClock.plusMinutes(1));
		manager.removeTimedOutTokens();
		assertThat(manager.getUserCredential(chuckUserCredential.getUsername()).getTokenKeys()).containsOnly(tokenB);
		assertThat(manager.getUserCredential(bobUserCredential.getUsername()).getTokenKeys()).isEmpty();

		givenTimeIs(shishOClock.plusMinutes(59));
		manager.removeTimedOutTokens();
		assertThat(manager.getUserCredential(chuckUserCredential.getUsername()).getTokenKeys()).containsOnly(tokenB);
		assertThat(manager.getUserCredential(bobUserCredential.getUsername()).getTokenKeys()).isEmpty();

		givenTimeIs(shishOClock.plusMinutes(60));
		manager.removeTimedOutTokens();
		assertThat(manager.getUserCredential(chuckUserCredential.getUsername()).getTokenKeys()).isEmpty();
		assertThat(manager.getUserCredential(bobUserCredential.getUsername()).getTokenKeys()).isEmpty();
	}

	private void createUserCredentials() {
		chuckUserCredential = manager.create("chuck", "Chuck", "Norris", "chuck.norris@gmail.com", null, true,
				asList("group1"), asList(zeCollection), new HashMap<String, LocalDateTime>(),
				UserCredentialStatus.ACTIVE, "domain", msExchDelegateListBL, null);

		bobUserCredential = manager.create("bob", "Bob", "Gratton", "bob.gratton@gmail.com", null, true,
				asList("group1"), asList(zeCollection), new HashMap<String, LocalDateTime>(),
				UserCredentialStatus.ACTIVE, "domain", msExchDelegateListBL, null);

		Map<String, LocalDateTime> tokens = new HashMap<String, LocalDateTime>();
		tokens.put("token1", endDate);
		tokens.put("token2", endDate.plusMinutes(30));
		edouardUserCredential = manager.create("edouard", "Edouard", "Lechat", "edouard.lechat@gmail.com",
				edouardServiceKey,
				false, asList("group2"), asList(zeCollection, "collection1"), tokens, UserCredentialStatus.ACTIVE,
				"domain", msExchDelegateListBL, null);
	}

	private Set<String> getAllCollectionsInUserCredentialFile() {
		Set<String> collections = new HashSet<>();
		List<UserCredential> userCredentials = manager.getActiveUserCredentials();
		for (UserCredential userCredential : userCredentials) {
			collections.addAll(userCredential.getCollections());
		}
		return collections;
	}
}
