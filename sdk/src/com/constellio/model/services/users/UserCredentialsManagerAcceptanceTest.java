/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
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
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.ModelLayerConfigurationAlteration;

public class UserCredentialsManagerAcceptanceTest extends ConstellioTest {

	LocalDateTime shishOClock = new LocalDateTime();

	String edouardServiceKey = "myKey";

	UserCredentialsManager manager;
	UserCredential chuckUserCredential, edouardUserCredential, bobUserCredential;

	LocalDateTime endDate = new LocalDateTime().plusMinutes(30);

	@Before
	public void setUp()
			throws Exception {

		withSpiedServices(ModelLayerConfiguration.class);
		configure(new ModelLayerConfigurationAlteration() {
			@Override
			public void alter(ModelLayerConfiguration configuration) {
				org.joda.time.Duration fortyTwoMinutes = org.joda.time.Duration.standardSeconds(42);
				doReturn(fortyTwoMinutes).when(configuration).getTokenDuration();
			}
		});

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
	public void givenUserCredentialInListWhenUpdateUserCredentialThenHeIsUpdated()
			throws Exception {

		manager.addUpdate(chuckUserCredential);
		manager.addUpdate(edouardUserCredential);

		chuckUserCredential = new UserCredential("chuck", "Chuck1", "Norris1", "chuck.norris1@gmail.com",
				Arrays.asList("group11"), Arrays.asList(zeCollection, "collection1"), UserCredentialStatus.ACTIVE, "domain");
		manager.addUpdate(chuckUserCredential);

		assertThat(manager.getActiveUserCredentials()).hasSize(3);
		assertThat(manager.getActiveUserCredentials().get(1)).isEqualToComparingFieldByField(chuckUserCredential);
		assertThat(manager.getActiveUserCredentials().get(2)).isEqualToComparingFieldByField(edouardUserCredential);
		assertThat(manager.getUserCredential("chuck")).isEqualToComparingFieldByField(chuckUserCredential);
		assertThat(manager.getUserCredential("edouard")).isEqualToComparingFieldByField(edouardUserCredential);
		assertThat(manager.getUserCredential("edouard").getTokens().get("token1")).isEqualTo(endDate);

	}

	@Test
	public void whenAddCollectionToExistingUserCredentialThenItIsUpdated()
			throws Exception {

		manager.addUpdate(chuckUserCredential);

		chuckUserCredential = new UserCredential("chuck", "Chuck", "Norris", "chuck.norris@gmail.com", Arrays.asList("group1"),
				Arrays.asList(zeCollection, "collection1"), UserCredentialStatus.ACTIVE, "domain");

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

		chuckUserCredential = chuckUserCredential.withStatus(UserCredentialStatus.SUPENDED);
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

		manager.addUpdate(chuckUserCredential.withToken("A", shishOClock).withToken("B", shishOClock.plusHours(1)));
		manager.addUpdate(bobUserCredential.withToken("C", shishOClock.plusMinutes(1)));

		givenTimeIs(shishOClock.minusSeconds(1));
		manager.removedTimedOutTokens();
		assertThat(manager.getUserCredential(chuckUserCredential.getUsername()).getTokensKeys()).containsOnly("A", "B");
		assertThat(manager.getUserCredential(bobUserCredential.getUsername()).getTokensKeys()).containsOnly("C");

		givenTimeIs(shishOClock);
		manager.removedTimedOutTokens();
		assertThat(manager.getUserCredential(chuckUserCredential.getUsername()).getTokensKeys()).containsOnly("B");
		assertThat(manager.getUserCredential(bobUserCredential.getUsername()).getTokensKeys()).containsOnly("C");

		givenTimeIs(shishOClock.plusMinutes(1));
		manager.removedTimedOutTokens();
		assertThat(manager.getUserCredential(chuckUserCredential.getUsername()).getTokensKeys()).containsOnly("B");
		assertThat(manager.getUserCredential(bobUserCredential.getUsername()).getTokensKeys()).isEmpty();

		givenTimeIs(shishOClock.plusMinutes(59));
		manager.removedTimedOutTokens();
		assertThat(manager.getUserCredential(chuckUserCredential.getUsername()).getTokensKeys()).containsOnly("B");
		assertThat(manager.getUserCredential(bobUserCredential.getUsername()).getTokensKeys()).isEmpty();

		givenTimeIs(shishOClock.plusMinutes(60));
		manager.removedTimedOutTokens();
		assertThat(manager.getUserCredential(chuckUserCredential.getUsername()).getTokensKeys()).isEmpty();
		assertThat(manager.getUserCredential(bobUserCredential.getUsername()).getTokensKeys()).isEmpty();
	}

	private void createUserCredentials() {
		chuckUserCredential = new UserCredential("chuck", "Chuck", "Norris", "chuck.norris@gmail.com", null, true,
				Arrays.asList("group1"), Arrays.asList(zeCollection), new HashMap<String, LocalDateTime>(),
				UserCredentialStatus.ACTIVE, "domain");

		bobUserCredential = new UserCredential("bob", "Bob", "Gratton", "bob.gratton@gmail.com", null, true,
				Arrays.asList("group1"), Arrays.asList(zeCollection), new HashMap<String, LocalDateTime>(),
				UserCredentialStatus.ACTIVE, "domain");

		Map<String, LocalDateTime> tokens = new HashMap<String, LocalDateTime>();
		tokens.put("token1", endDate);
		tokens.put("token2", endDate.plusMinutes(30));
		edouardUserCredential = new UserCredential("edouard", "Edouard", "Lechat", "edouard.lechat@gmail.com", edouardServiceKey,
				false, Arrays.asList("group2"), Arrays.asList(zeCollection, "collection1"), tokens, UserCredentialStatus.ACTIVE,
				"domain");
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