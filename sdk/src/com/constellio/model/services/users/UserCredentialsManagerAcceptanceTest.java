package com.constellio.model.services.users;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.XmlUserCredential;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.encrypt.EncryptionKeyFactory;
import com.constellio.model.services.factories.ModelLayerFactoryUtils;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.ModelLayerConfigurationAlteration;

public class UserCredentialsManagerAcceptanceTest extends ConstellioTest {

	LocalDateTime shishOClock = new LocalDateTime();

	String edouardServiceKey = "myKey";

	UserCredentialsManager manager;
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
			public void alter(ModelLayerConfiguration configuration) {
				org.joda.time.Duration fortyTwoMinutes = org.joda.time.Duration.standardSeconds(42);
				doReturn(fortyTwoMinutes).when(configuration).getTokenDuration();
			}
		});

		collectionsListManager = getModelLayerFactory().getCollectionsListManager();

		msExchDelegateListBL = new ArrayList<>();
		msExchDelegateListBL.add("msExchDelegateListBL1");
		msExchDelegateListBL.add("msExchDelegateListBL2");

		Key key = EncryptionKeyFactory.newApplicationKey("zePassword", "zeUltimateSalt");
		ModelLayerFactoryUtils.setApplicationEncryptionKey(getModelLayerFactory(), key);

		createUserCredentials();

		manager = getModelLayerFactory().getUserCredentialsManager();

	}

	@Test
	public void whenAddUsersCredentialsThenTheyAreAddedInListOnce()
			throws Exception {

		manager.addUpdate(chuckUserCredential);
		manager.addUpdate(edouardUserCredential);
		manager.addUpdate(chuckUserCredential);

		assertThat(manager.getActiveUserCredentials()).hasSize(3);
		assertThat(manager.getActiveUserCredentials().get(1)).isEqualToComparingFieldByField(chuckUserCredential);
		assertThat(manager.getActiveUserCredentials().get(2)).isEqualToComparingFieldByField(edouardUserCredential);
		assertThat(manager.getUserCredential("chuck")).isEqualToComparingFieldByField(chuckUserCredential);
		assertThat(manager.getUserCredential("Chuck")).isEqualToComparingFieldByField(chuckUserCredential);
		assertThat(manager.getUserCredential("Édouard")).isEqualToComparingFieldByField(edouardUserCredential);
	}

	@Test
	public void givenHasInvalidCollectionWhenReadThenHAsOnlyValidCollections() {

		manager.addUpdate(edouardUserCredential);

		assertThat(manager.getUserCredential("Édouard").getCollections()).containsOnly(zeCollection, "collection1");
	}

	@Test
	public void givenUserCredentialInListWhenUpdateUserCredentialThenHeIsUpdated()
			throws Exception {

		manager.addUpdate(chuckUserCredential);
		manager.addUpdate(edouardUserCredential);

		chuckUserCredential = new XmlUserCredential("chuck", "Chuck1", "Norris1", "chuck.norris1@gmail.com",
				asList("group11"), asList(zeCollection, "collection1"), UserCredentialStatus.ACTIVE, "domain",
				msExchDelegateListBL, null);
		manager.addUpdate(chuckUserCredential);

		assertThat(manager.getActiveUserCredentials()).hasSize(3);
		assertThat(manager.getActiveUserCredentials().get(1)).isEqualToComparingFieldByField(chuckUserCredential);
		assertThat(manager.getActiveUserCredentials().get(2)).isEqualToComparingFieldByField(edouardUserCredential);
		assertThat(manager.getUserCredential("chuck")).isEqualToComparingFieldByField(chuckUserCredential);
		assertThat(manager.getUserCredential("edouard")).isEqualToComparingFieldByField(edouardUserCredential);
		assertThat(manager.getUserCredential("edouard").getAccessTokens().get("token1")).isEqualTo(endDate);

	}

	@Test
	public void whenAddCollectionToExistingUserCredentialThenItIsUpdated()
			throws Exception {

		manager.addUpdate(chuckUserCredential);

		chuckUserCredential = new XmlUserCredential("chuck", "Chuck", "Norris", "chuck.norris@gmail.com", asList("group1"),
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

		assertThat(manager.getActiveUserCredentials()).hasSize(3);

		assertThat(manager.getActiveUserCredentials().get(0).getUsername()).isEqualTo("admin");
		assertThat(manager.getActiveUserCredentials().get(0).getCollections()).hasSize(0);
		assertThat(manager.getActiveUserCredentials().get(1).getUsername()).isEqualTo(chuckUserCredential.getUsername());
		assertThat(manager.getActiveUserCredentials().get(1).getCollections()).isEmpty();
		assertThat(manager.getActiveUserCredentials().get(2).getUsername()).isEqualTo(edouardUserCredential.getUsername());
		assertThat(manager.getActiveUserCredentials().get(2).getCollections()).hasSize(2);
	}

	@Test
	public void whenChangeStatusUserThenOk()
			throws Exception {
		manager.addUpdate(chuckUserCredential);
		manager.addUpdate(edouardUserCredential);

		chuckUserCredential = chuckUserCredential.withStatus(UserCredentialStatus.DELETED);
		manager.addUpdate(chuckUserCredential);

		assertThat(manager.getActiveUserCredentials()).hasSize(2);
		assertThat(manager.getDeletedUserCredentials()).hasSize(1);
		assertThat(manager.getDeletedUserCredentials().get(0).getUsername()).isEqualTo(chuckUserCredential.getUsername());

		chuckUserCredential = chuckUserCredential.withStatus(UserCredentialStatus.SUSPENDED);
		manager.addUpdate(chuckUserCredential);

		assertThat(manager.getActiveUserCredentials()).hasSize(2);
		assertThat(manager.getDeletedUserCredentials()).isEmpty();
		assertThat(manager.getSuspendedUserCredentials().get(0).getUsername()).isEqualTo(chuckUserCredential.getUsername());

		chuckUserCredential = chuckUserCredential.withStatus(UserCredentialStatus.PENDING);
		manager.addUpdate(chuckUserCredential);

		assertThat(manager.getActiveUserCredentials()).hasSize(2);
		assertThat(manager.getSuspendedUserCredentials()).isEmpty();
		assertThat(manager.getPendingApprovalUserCredentials().get(0).getUsername()).isEqualTo(chuckUserCredential.getUsername());

		chuckUserCredential = chuckUserCredential.withStatus(UserCredentialStatus.ACTIVE);
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
		assertThat(manager.getUserCredential("Edouard").getGlobalGroups()).hasSize(1);
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

		givenTimeIs(shishOClock.minusYears(1));
		Map<String, LocalDateTime> tokens = new HashMap<String, LocalDateTime>();

		manager.addUpdate(chuckUserCredential);
		manager.addUpdate(bobUserCredential);

		manager.addUpdate(chuckUserCredential.withAccessToken("A", shishOClock).withAccessToken("B", shishOClock.plusHours(1)));
		manager.addUpdate(bobUserCredential.withAccessToken("C", shishOClock.plusMinutes(1)));

		givenTimeIs(shishOClock.minusSeconds(1));
		manager.removedTimedOutTokens();
		assertThat(manager.getUserCredential(chuckUserCredential.getUsername()).getTokenKeys()).containsOnly("A", "B");
		assertThat(manager.getUserCredential(bobUserCredential.getUsername()).getTokenKeys()).containsOnly("C");

		givenTimeIs(shishOClock);
		manager.removedTimedOutTokens();
		assertThat(manager.getUserCredential(chuckUserCredential.getUsername()).getTokenKeys()).containsOnly("B");
		assertThat(manager.getUserCredential(bobUserCredential.getUsername()).getTokenKeys()).containsOnly("C");

		givenTimeIs(shishOClock.plusMinutes(1));
		manager.removedTimedOutTokens();
		assertThat(manager.getUserCredential(chuckUserCredential.getUsername()).getTokenKeys()).containsOnly("B");
		assertThat(manager.getUserCredential(bobUserCredential.getUsername()).getTokenKeys()).isEmpty();

		givenTimeIs(shishOClock.plusMinutes(59));
		manager.removedTimedOutTokens();
		assertThat(manager.getUserCredential(chuckUserCredential.getUsername()).getTokenKeys()).containsOnly("B");
		assertThat(manager.getUserCredential(bobUserCredential.getUsername()).getTokenKeys()).isEmpty();

		givenTimeIs(shishOClock.plusMinutes(60));
		manager.removedTimedOutTokens();
		assertThat(manager.getUserCredential(chuckUserCredential.getUsername()).getTokenKeys()).isEmpty();
		assertThat(manager.getUserCredential(bobUserCredential.getUsername()).getTokenKeys()).isEmpty();
	}

	private void createUserCredentials() {
		chuckUserCredential = new XmlUserCredential("chuck", "Chuck", "Norris", "chuck.norris@gmail.com", null, true,
				asList("group1"), asList(zeCollection), new HashMap<String, LocalDateTime>(),
				UserCredentialStatus.ACTIVE, "domain", msExchDelegateListBL, null);

		bobUserCredential = new XmlUserCredential("bob", "Bob", "Gratton", "bob.gratton@gmail.com", null, true,
				asList("group1"), asList(zeCollection), new HashMap<String, LocalDateTime>(),
				UserCredentialStatus.ACTIVE, "domain", msExchDelegateListBL, null);

		Map<String, LocalDateTime> tokens = new HashMap<String, LocalDateTime>();
		tokens.put("token1", endDate);
		tokens.put("token2", endDate.plusMinutes(30));
		edouardUserCredential = new XmlUserCredential("edouard", "Edouard", "Lechat", "edouard.lechat@gmail.com", edouardServiceKey,
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
