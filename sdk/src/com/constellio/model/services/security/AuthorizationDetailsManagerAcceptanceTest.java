package com.constellio.model.services.security;

import static com.constellio.model.entities.security.AuthorizationDetails.create;
import static com.constellio.model.entities.security.AuthorizationDetails.createSynced;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.services.security.AuthorizationDetailsManagerRuntimeException.AuthorizationDetailsAlreadyExists;
import com.constellio.sdk.tests.ConstellioTest;

public class AuthorizationDetailsManagerAcceptanceTest extends ConstellioTest {

	static final LocalDate NOW = new LocalDate().minusYears(1);
	static final LocalDate TWO_WEEKS_AGO = NOW.minusDays(14);
	static final LocalDate YESTERDAY = NOW.minusDays(1);
	static final LocalDate TOMORROW = NOW.plusDays(1);
	static final LocalDate IN_TO_WEEKS = NOW.plusDays(14);

	AuthorizationDetailsManager manager;
	CollectionsManager collectionsManager;

	@Before
	public void setup()
			throws Exception {
		collectionsManager = getAppLayerFactory().getCollectionsManager();
		givenCollection(zeCollection);
		manager = getModelLayerFactory().getAuthorizationDetailsManager();
		givenTimeIs(NOW);
	}

	@Test
	public void givenAuthorizationsWithSameIdInMultipleCollectionsThenAllIndependent()
			throws Exception {

		givenCollection("collection1");
		givenCollection("collection2");
		AuthorizationDetails authorizationDetailsCollection1 = create(aString(), newRoleList(zeCollection), "collection1");
		AuthorizationDetails authorizationDetailsCollection2 = create(aString(), newRoleList(zeCollection), "collection2");

		manager.add(authorizationDetailsCollection1);
		manager.add(authorizationDetailsCollection2);

		assertThat(manager.get("collection1", authorizationDetailsCollection1.getId()).getId()).isEqualTo(
				authorizationDetailsCollection1.getId());
		assertThat(manager.get("collection2", authorizationDetailsCollection2.getId()).getId()).isEqualTo(
				authorizationDetailsCollection2.getId());
		assertThat(manager.get("collection1", authorizationDetailsCollection1.getId())).isNotEqualTo(
				authorizationDetailsCollection2);
	}

	@Test
	public void whenAddAuthorizationsThenCanGetIt()
			throws Exception {
		AuthorizationDetails authorizationDetails = newAuthorizationValidFromTo(TOMORROW, TOMORROW);
		AuthorizationDetails authorizationDetails2 = create(aString(), newRoleList(zeCollection), zeCollection);
		String id = authorizationDetails.getId();
		String id2 = authorizationDetails2.getId();

		manager.add(authorizationDetails);
		manager.add(authorizationDetails2);

		assertThat(manager.get(zeCollection, id).getId()).isEqualTo(authorizationDetails.getId());
		assertThat(manager.get(zeCollection, id2).getId()).isEqualTo(authorizationDetails2.getId());
	}

	@Test(expected = AuthorizationDetailsAlreadyExists.class)
	public void whenAddAuthorizationInSameCollectionWithSameIdThenException()
			throws Exception {
		AuthorizationDetails authorizationDetails = newAuthorizationValidFromTo(TWO_WEEKS_AGO, TOMORROW);

		manager.add(authorizationDetails);
		manager.add(authorizationDetails);
	}

	@Test(expected = AuthorizationDetailsManagerRuntimeException.StartDateGreaterThanEndDate.class)
	public void whenAddAuthorizationWithStartDateGreaterThanEndDateThenException()
			throws Exception {
		AuthorizationDetails authorizationDetails = newAuthorizationValidFromTo(IN_TO_WEEKS, TOMORROW);

		manager.add(authorizationDetails);
	}

	@Test()
	public void whenAddAuthorizationWithStartDateEqualToEndDateThenCorrect()
			throws Exception {
		AuthorizationDetails authorizationDetails = newAuthorizationValidFromTo(TOMORROW, TOMORROW);

		manager.add(authorizationDetails);
	}

	@Test()
	public void whenAddAuthorizationWithStartDateEqualToCurrentDateThenCorrect()
			throws Exception {
		AuthorizationDetails authorizationDetails = newAuthorizationValidFromTo(NOW, TOMORROW);

		manager.add(authorizationDetails);
	}

	@Test()
	public void whenAddAuthorizationWithStartDateBeforeCurrentDateThenCorrect()
			throws Exception {
		AuthorizationDetails authorizationDetails = newAuthorizationValidFromTo(YESTERDAY, TOMORROW);

		manager.add(authorizationDetails);
	}

	@Test
	public void whenAddAuthorizationWithEndDateEqualToCurrentDateThenOK()
			throws Exception {
		AuthorizationDetails authorizationDetails = newAuthorizationValidFromTo(TWO_WEEKS_AGO, NOW);

		manager.add(authorizationDetails);
	}

	@Test(expected = AuthorizationDetailsManagerRuntimeException.EndDateLessThanCurrentDate.class)
	public void whenAddAuthorizationWithEndDateBeforeCurrentDateThenException()
			throws Exception {
		AuthorizationDetails authorizationDetails = newAuthorizationValidFromTo(TWO_WEEKS_AGO, YESTERDAY);

		manager.add(authorizationDetails);
	}

	@Test
	public void givenAuthorizationWithoutDatesThenEnabled()
			throws Exception {
		AuthorizationDetails authorizationDetails = create(aString(), newRoleList(zeCollection), zeCollection);

		assertThat(manager.isEnabled(authorizationDetails)).isTrue();
		assertThat(manager.isDisabled(authorizationDetails)).isFalse();
	}

	@Test
	public void givenNotYetStartedAuthorizationThenDisabled()
			throws Exception {
		AuthorizationDetails authorizationDetails = newAuthorizationValidFromTo(TOMORROW, IN_TO_WEEKS);

		assertThat(manager.isEnabled(authorizationDetails)).isFalse();
		assertThat(manager.isDisabled(authorizationDetails)).isTrue();
	}

	@Test
	public void givenFinishedAuthorizationThenDisabled()
			throws Exception {
		AuthorizationDetails authorizationDetails = newAuthorizationValidFromTo(TWO_WEEKS_AGO, YESTERDAY);

		assertThat(manager.isEnabled(authorizationDetails)).isFalse();
		assertThat(manager.isDisabled(authorizationDetails)).isTrue();
	}

	@Test
	public void givenFutureEnableAuthorizationWhenIsEnableThenFalse()
			throws Exception {
		AuthorizationDetails authorizationDetails = newAuthorizationValidFromTo(TOMORROW, IN_TO_WEEKS);

		assertThat(manager.isEnabled(authorizationDetails)).isFalse();
		assertThat(manager.isDisabled(authorizationDetails)).isTrue();
	}

	@Test
	public void givenOneAuthorizationDetailWhenRemovingThenItIsRemoved()
			throws Exception {
		AuthorizationDetails authorizationDetails = create(aString(), newRoleList(zeCollection), zeCollection);
		manager.remove(authorizationDetails);

		assertThat(manager.get(zeCollection, authorizationDetails.getId())).isNull();
	}

	@Test
	public void givenMultipleAuthorizationDetailsWhenRemovingStartingFromFirstThenAllRemoved()
			throws Exception {
		AuthorizationDetails authorizationDetails1 = create(aString(), newRoleList(zeCollection), zeCollection);
		AuthorizationDetails authorizationDetails2 = create(aString(), newRoleList(zeCollection), zeCollection);
		AuthorizationDetails authorizationDetails3 = create(aString(), newRoleList(zeCollection), zeCollection);
		manager.add(authorizationDetails1);
		manager.add(authorizationDetails2);
		manager.add(authorizationDetails3);

		manager.remove(authorizationDetails1);
		assertThat(manager.get(zeCollection, authorizationDetails1.getId())).isNull();
		assertThat(manager.get(zeCollection, authorizationDetails2.getId())).isEqualTo(authorizationDetails2);
		assertThat(manager.get(zeCollection, authorizationDetails3.getId())).isEqualTo(authorizationDetails3);

		manager.remove(authorizationDetails2);
		assertThat(manager.get(zeCollection, authorizationDetails1.getId())).isNull();
		assertThat(manager.get(zeCollection, authorizationDetails2.getId())).isNull();
		assertThat(manager.get(zeCollection, authorizationDetails3.getId())).isEqualTo(authorizationDetails3);

		manager.remove(authorizationDetails3);
		assertThat(manager.get(zeCollection, authorizationDetails1.getId())).isNull();
		assertThat(manager.get(zeCollection, authorizationDetails2.getId())).isNull();
		assertThat(manager.get(zeCollection, authorizationDetails3.getId())).isNull();
	}

	@Test
	public void givenMultipleAuthorizationDetailsWhenRemovingStartingFromLastThenAllRemoved()
			throws Exception {
		AuthorizationDetails authorizationDetails1 = create(aString(), newRoleList(zeCollection), zeCollection);
		AuthorizationDetails authorizationDetails2 = create(aString(), newRoleList(zeCollection), zeCollection);
		AuthorizationDetails authorizationDetails3 = create(aString(), newRoleList(zeCollection), zeCollection);
		manager.add(authorizationDetails1);
		manager.add(authorizationDetails2);
		manager.add(authorizationDetails3);

		assertThat(manager.get(zeCollection, authorizationDetails1.getId())).isEqualTo(authorizationDetails1);
		assertThat(manager.get(zeCollection, authorizationDetails2.getId())).isEqualTo(authorizationDetails2);
		assertThat(manager.get(zeCollection, authorizationDetails3.getId())).isEqualTo(authorizationDetails3);

		manager.remove(authorizationDetails3);
		assertThat(manager.get(zeCollection, authorizationDetails1.getId())).isEqualTo(authorizationDetails1);
		assertThat(manager.get(zeCollection, authorizationDetails2.getId())).isEqualTo(authorizationDetails2);
		assertThat(manager.get(zeCollection, authorizationDetails3.getId())).isNull();

		manager.remove(authorizationDetails2);
		assertThat(manager.get(zeCollection, authorizationDetails1.getId())).isEqualTo(authorizationDetails1);
		assertThat(manager.get(zeCollection, authorizationDetails2.getId())).isNull();
		assertThat(manager.get(zeCollection, authorizationDetails3.getId())).isNull();

		manager.remove(authorizationDetails1);
		assertThat(manager.get(zeCollection, authorizationDetails1.getId())).isNull();
		assertThat(manager.get(zeCollection, authorizationDetails2.getId())).isNull();
		assertThat(manager.get(zeCollection, authorizationDetails3.getId())).isNull();
	}

	@Test
	public void whenAddUpdatingAuthorizationsThenPersistSyncedStatus()
			throws Exception {
		AuthorizationDetails notSynchedAuth = create("zeNotSynchedAuth", newRoleList(zeCollection), zeCollection);
		AuthorizationDetails synchedAuth = createSynced("zeSynchedAuth", newRoleList(zeCollection), zeCollection);
		manager.add(notSynchedAuth);
		manager.add(synchedAuth);

		assertThat(manager.get(zeCollection, synchedAuth.getId()).isSynced()).isTrue();
		assertThat(manager.get(zeCollection, notSynchedAuth.getId()).isSynced()).isFalse();

		manager.modifyEndDate(notSynchedAuth, new LocalDate());
		manager.modifyEndDate(synchedAuth, new LocalDate());

		assertThat(manager.get(zeCollection, synchedAuth.getId()).isSynced()).isTrue();
		assertThat(manager.get(zeCollection, notSynchedAuth.getId()).isSynced()).isFalse();
	}

	@Test
	public void givenMultipleAuthorizationDetailsWhenModifyingAllThenAllModified()
			throws Exception {

		LocalDate newEndDate1 = new LocalDate();
		LocalDate newEndDate2 = new LocalDate();
		LocalDate newEndDate3 = new LocalDate();

		AuthorizationDetails authorizationDetails1 = create(aString(), newRoleList(zeCollection), zeCollection);
		AuthorizationDetails authorizationDetails2 = create(aString(), newRoleList(zeCollection), zeCollection);
		AuthorizationDetails authorizationDetails3 = create(aString(), newRoleList(zeCollection), zeCollection);
		manager.add(authorizationDetails1);
		manager.add(authorizationDetails2);
		manager.add(authorizationDetails3);

		assertThat(manager.get(zeCollection, authorizationDetails1.getId())).isEqualTo(authorizationDetails1);
		assertThat(manager.get(zeCollection, authorizationDetails2.getId())).isEqualTo(authorizationDetails2);
		assertThat(manager.get(zeCollection, authorizationDetails3.getId())).isEqualTo(authorizationDetails3);

		manager.modifyEndDate(authorizationDetails3, newEndDate3);
		assertThat(manager.get(zeCollection, authorizationDetails1.getId()).getEndDate()).isNull();
		assertThat(manager.get(zeCollection, authorizationDetails2.getId()).getEndDate()).isNull();
		assertThat(manager.get(zeCollection, authorizationDetails3.getId()).getEndDate()).isEqualTo(newEndDate3);

		manager.modifyEndDate(authorizationDetails2, newEndDate2);
		assertThat(manager.get(zeCollection, authorizationDetails1.getId()).getEndDate()).isNull();
		assertThat(manager.get(zeCollection, authorizationDetails2.getId()).getEndDate()).isEqualTo(newEndDate2);
		assertThat(manager.get(zeCollection, authorizationDetails3.getId()).getEndDate()).isEqualTo(newEndDate3);

		manager.modifyEndDate(authorizationDetails1, newEndDate1);
		assertThat(manager.get(zeCollection, authorizationDetails1.getId()).getEndDate()).isEqualTo(newEndDate1);
		assertThat(manager.get(zeCollection, authorizationDetails2.getId()).getEndDate()).isEqualTo(newEndDate2);
		assertThat(manager.get(zeCollection, authorizationDetails3.getId()).getEndDate()).isEqualTo(newEndDate3);
	}

	@Test
	public void givenAuthorizationsListWhenGetFinishedAuthorizationsThenReturnTwo()
			throws Exception {
		manager = spy(getModelLayerFactory().getAuthorizationDetailsManager());
		AuthorizationDetails authorizationThatHasFinishedTwoWeeksAgo = newAuthorizationValidFromTo(TWO_WEEKS_AGO, TWO_WEEKS_AGO);
		AuthorizationDetails authorizationThatHasFinishedYesterday = newAuthorizationValidFromTo(TWO_WEEKS_AGO, YESTERDAY);
		AuthorizationDetails authorizationThatFinishToday = newAuthorizationValidFromTo(TWO_WEEKS_AGO, NOW);
		AuthorizationDetails authorizationWithoutStartAndEndDates = create(aString(), newRoleList(zeCollection), zeCollection);
		AuthorizationDetails futureEnableAuthorizationDetails = newAuthorizationValidFromTo(TOMORROW, IN_TO_WEEKS);
		when(manager.getAuthorizationsDetails(zeCollection)).thenReturn(
				asAuthorizationDetailsMap(authorizationThatHasFinishedTwoWeeksAgo, authorizationThatHasFinishedYesterday,
						authorizationThatFinishToday, authorizationWithoutStartAndEndDates, futureEnableAuthorizationDetails));

		assertThat(manager.getListOfFinishedAuthorizationsIds(zeCollection)).containsOnly(
				authorizationThatHasFinishedTwoWeeksAgo.getId(), authorizationThatHasFinishedYesterday.getId());
	}

	@Test
	public void givenEnableAuthorizationWhenModifyEndDateThenItIsModified()
			throws Exception {
		AuthorizationDetails authorizationDetails = create(aString(), newRoleList(zeCollection), zeCollection);
		manager.add(authorizationDetails);

		LocalDate endate = new LocalDate(2020, 01, 01);
		manager.modifyEndDate(authorizationDetails, endate);

		assertThat(
				manager.get(authorizationDetails.getCollection(), authorizationDetails.getId()).getEndDate())
				.isEqualTo(endate);
	}

	private Map<String, AuthorizationDetails> asAuthorizationDetailsMap(AuthorizationDetails... details) {
		Map<String, AuthorizationDetails> map = new HashMap<>();
		for (AuthorizationDetails detail : details) {
			map.put(detail.getId(), detail);
		}
		return map;
	}

	private AuthorizationDetails newAuthorizationValidFromTo(LocalDate startDate, LocalDate endDate) {
		List<String> roles = newRoleList(zeCollection);
		return create(aString(), roles, startDate, endDate, zeCollection);
	}

	private List<String> newRoleList(String collection) {
		List<String> roles = new ArrayList<>();
		roles.add("role1");
		roles.add("role2");
		roles.add("role3");
		roles.add("role4");
		return roles;
	}
}
