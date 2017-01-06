package com.constellio.model.services.security;

import static com.constellio.model.entities.security.XMLAuthorizationDetails.create;
import static com.constellio.model.entities.security.XMLAuthorizationDetails.createSynced;
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
import com.constellio.model.entities.security.XMLAuthorizationDetails;
import com.constellio.model.services.security.AuthorizationDetailsManagerRuntimeException.AuthorizationDetailsAlreadyExists;
import com.constellio.sdk.tests.ConstellioTest;

public class XMLAuthorizationDetailsManagerAcceptanceTest extends ConstellioTest {

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
		XMLAuthorizationDetails xmlAuthorizationDetailsCollection1 = create(aString(), newRoleList(zeCollection), "collection1");
		XMLAuthorizationDetails xmlAuthorizationDetailsCollection2 = create(aString(), newRoleList(zeCollection), "collection2");

		manager.add(xmlAuthorizationDetailsCollection1);
		manager.add(xmlAuthorizationDetailsCollection2);

		assertThat(manager.get("collection1", xmlAuthorizationDetailsCollection1.getId()).getId()).isEqualTo(
				xmlAuthorizationDetailsCollection1.getId());
		assertThat(manager.get("collection2", xmlAuthorizationDetailsCollection2.getId()).getId()).isEqualTo(
				xmlAuthorizationDetailsCollection2.getId());
		assertThat(manager.get("collection1", xmlAuthorizationDetailsCollection1.getId())).isNotEqualTo(
				xmlAuthorizationDetailsCollection2);
	}

	@Test
	public void whenAddAuthorizationsThenCanGetIt()
			throws Exception {
		XMLAuthorizationDetails xmlAuthorizationDetails = newAuthorizationValidFromTo(TOMORROW, TOMORROW);
		XMLAuthorizationDetails xmlAuthorizationDetails2 = create(aString(), newRoleList(zeCollection), zeCollection);
		String id = xmlAuthorizationDetails.getId();
		String id2 = xmlAuthorizationDetails2.getId();

		manager.add(xmlAuthorizationDetails);
		manager.add(xmlAuthorizationDetails2);

		assertThat(manager.get(zeCollection, id).getId()).isEqualTo(xmlAuthorizationDetails.getId());
		assertThat(manager.get(zeCollection, id2).getId()).isEqualTo(xmlAuthorizationDetails2.getId());
	}

	@Test(expected = AuthorizationDetailsAlreadyExists.class)
	public void whenAddAuthorizationInSameCollectionWithSameIdThenException()
			throws Exception {
		XMLAuthorizationDetails xmlAuthorizationDetails = newAuthorizationValidFromTo(TWO_WEEKS_AGO, TOMORROW);

		manager.add(xmlAuthorizationDetails);
		manager.add(xmlAuthorizationDetails);
	}

	@Test(expected = AuthorizationDetailsManagerRuntimeException.StartDateGreaterThanEndDate.class)
	public void whenAddAuthorizationWithStartDateGreaterThanEndDateThenException()
			throws Exception {
		XMLAuthorizationDetails xmlAuthorizationDetails = newAuthorizationValidFromTo(IN_TO_WEEKS, TOMORROW);

		manager.add(xmlAuthorizationDetails);
	}

	@Test()
	public void whenAddAuthorizationWithStartDateEqualToEndDateThenCorrect()
			throws Exception {
		XMLAuthorizationDetails xmlAuthorizationDetails = newAuthorizationValidFromTo(TOMORROW, TOMORROW);

		manager.add(xmlAuthorizationDetails);
	}

	@Test()
	public void whenAddAuthorizationWithStartDateEqualToCurrentDateThenCorrect()
			throws Exception {
		XMLAuthorizationDetails xmlAuthorizationDetails = newAuthorizationValidFromTo(NOW, TOMORROW);

		manager.add(xmlAuthorizationDetails);
	}

	@Test()
	public void whenAddAuthorizationWithStartDateBeforeCurrentDateThenCorrect()
			throws Exception {
		XMLAuthorizationDetails xmlAuthorizationDetails = newAuthorizationValidFromTo(YESTERDAY, TOMORROW);

		manager.add(xmlAuthorizationDetails);
	}

	@Test(expected = AuthorizationDetailsManagerRuntimeException.EndDateLessThanCurrentDate.class)
	public void whenAddAuthorizationWithEndDateEqualToCurrentDateThenException()
			throws Exception {
		XMLAuthorizationDetails xmlAuthorizationDetails = newAuthorizationValidFromTo(YESTERDAY, NOW);

		manager.add(xmlAuthorizationDetails);
	}

	@Test(expected = AuthorizationDetailsManagerRuntimeException.EndDateLessThanCurrentDate.class)
	public void whenAddAuthorizationWithEndDateBeforeCurrentDateThenException()
			throws Exception {
		XMLAuthorizationDetails xmlAuthorizationDetails = newAuthorizationValidFromTo(TWO_WEEKS_AGO, YESTERDAY);

		manager.add(xmlAuthorizationDetails);
	}

	@Test
	public void givenAuthorizationWithoutDatesThenEnabled()
			throws Exception {
		XMLAuthorizationDetails xmlAuthorizationDetails = create(aString(), newRoleList(zeCollection), zeCollection);

		assertThat(manager.isEnabled(xmlAuthorizationDetails)).isTrue();
		assertThat(manager.isDisabled(xmlAuthorizationDetails)).isFalse();
	}

	@Test
	public void givenNotYetStartedAuthorizationThenDisabled()
			throws Exception {
		XMLAuthorizationDetails xmlAuthorizationDetails = newAuthorizationValidFromTo(TOMORROW, IN_TO_WEEKS);

		assertThat(manager.isEnabled(xmlAuthorizationDetails)).isFalse();
		assertThat(manager.isDisabled(xmlAuthorizationDetails)).isTrue();
	}

	@Test
	public void givenFinishedAuthorizationThenDisabled()
			throws Exception {
		XMLAuthorizationDetails xmlAuthorizationDetails = newAuthorizationValidFromTo(TWO_WEEKS_AGO, YESTERDAY);

		assertThat(manager.isEnabled(xmlAuthorizationDetails)).isFalse();
		assertThat(manager.isDisabled(xmlAuthorizationDetails)).isTrue();
	}

	@Test
	public void givenFutureEnableAuthorizationWhenIsEnableThenFalse()
			throws Exception {
		XMLAuthorizationDetails xmlAuthorizationDetails = newAuthorizationValidFromTo(TOMORROW, IN_TO_WEEKS);

		assertThat(manager.isEnabled(xmlAuthorizationDetails)).isFalse();
		assertThat(manager.isDisabled(xmlAuthorizationDetails)).isTrue();
	}

	@Test
	public void givenOneAuthorizationDetailWhenRemovingThenItIsRemoved()
			throws Exception {
		XMLAuthorizationDetails xmlAuthorizationDetails = create(aString(), newRoleList(zeCollection), zeCollection);
		manager.remove(xmlAuthorizationDetails);

		assertThat(manager.get(zeCollection, xmlAuthorizationDetails.getId())).isNull();
	}

	@Test
	public void givenMultipleAuthorizationDetailsWhenRemovingStartingFromFirstThenAllRemoved()
			throws Exception {
		XMLAuthorizationDetails xmlAuthorizationDetails1 = create(aString(), newRoleList(zeCollection), zeCollection);
		XMLAuthorizationDetails xmlAuthorizationDetails2 = create(aString(), newRoleList(zeCollection), zeCollection);
		XMLAuthorizationDetails xmlAuthorizationDetails3 = create(aString(), newRoleList(zeCollection), zeCollection);
		manager.add(xmlAuthorizationDetails1);
		manager.add(xmlAuthorizationDetails2);
		manager.add(xmlAuthorizationDetails3);

		manager.remove(xmlAuthorizationDetails1);
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails1.getId())).isNull();
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails2.getId())).isEqualTo(xmlAuthorizationDetails2);
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails3.getId())).isEqualTo(xmlAuthorizationDetails3);

		manager.remove(xmlAuthorizationDetails2);
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails1.getId())).isNull();
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails2.getId())).isNull();
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails3.getId())).isEqualTo(xmlAuthorizationDetails3);

		manager.remove(xmlAuthorizationDetails3);
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails1.getId())).isNull();
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails2.getId())).isNull();
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails3.getId())).isNull();
	}

	@Test
	public void givenMultipleAuthorizationDetailsWhenRemovingStartingFromLastThenAllRemoved()
			throws Exception {
		XMLAuthorizationDetails xmlAuthorizationDetails1 = create(aString(), newRoleList(zeCollection), zeCollection);
		XMLAuthorizationDetails xmlAuthorizationDetails2 = create(aString(), newRoleList(zeCollection), zeCollection);
		XMLAuthorizationDetails xmlAuthorizationDetails3 = create(aString(), newRoleList(zeCollection), zeCollection);
		manager.add(xmlAuthorizationDetails1);
		manager.add(xmlAuthorizationDetails2);
		manager.add(xmlAuthorizationDetails3);

		assertThat(manager.get(zeCollection, xmlAuthorizationDetails1.getId())).isEqualTo(xmlAuthorizationDetails1);
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails2.getId())).isEqualTo(xmlAuthorizationDetails2);
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails3.getId())).isEqualTo(xmlAuthorizationDetails3);

		manager.remove(xmlAuthorizationDetails3);
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails1.getId())).isEqualTo(xmlAuthorizationDetails1);
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails2.getId())).isEqualTo(xmlAuthorizationDetails2);
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails3.getId())).isNull();

		manager.remove(xmlAuthorizationDetails2);
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails1.getId())).isEqualTo(xmlAuthorizationDetails1);
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails2.getId())).isNull();
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails3.getId())).isNull();

		manager.remove(xmlAuthorizationDetails1);
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails1.getId())).isNull();
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails2.getId())).isNull();
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails3.getId())).isNull();
	}

	@Test
	public void whenAddUpdatingAuthorizationsThenPersistSyncedStatus()
			throws Exception {
		XMLAuthorizationDetails notSynchedAuth = create("zeNotSynchedAuth", newRoleList(zeCollection), zeCollection);
		XMLAuthorizationDetails synchedAuth = createSynced("zeSynchedAuth", newRoleList(zeCollection), zeCollection);
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

		XMLAuthorizationDetails xmlAuthorizationDetails1 = create(aString(), newRoleList(zeCollection), zeCollection);
		XMLAuthorizationDetails xmlAuthorizationDetails2 = create(aString(), newRoleList(zeCollection), zeCollection);
		XMLAuthorizationDetails xmlAuthorizationDetails3 = create(aString(), newRoleList(zeCollection), zeCollection);
		manager.add(xmlAuthorizationDetails1);
		manager.add(xmlAuthorizationDetails2);
		manager.add(xmlAuthorizationDetails3);

		assertThat(manager.get(zeCollection, xmlAuthorizationDetails1.getId())).isEqualTo(xmlAuthorizationDetails1);
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails2.getId())).isEqualTo(xmlAuthorizationDetails2);
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails3.getId())).isEqualTo(xmlAuthorizationDetails3);

		manager.modifyEndDate(xmlAuthorizationDetails3, newEndDate3);
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails1.getId()).getEndDate()).isNull();
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails2.getId()).getEndDate()).isNull();
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails3.getId()).getEndDate()).isEqualTo(newEndDate3);

		manager.modifyEndDate(xmlAuthorizationDetails2, newEndDate2);
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails1.getId()).getEndDate()).isNull();
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails2.getId()).getEndDate()).isEqualTo(newEndDate2);
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails3.getId()).getEndDate()).isEqualTo(newEndDate3);

		manager.modifyEndDate(xmlAuthorizationDetails1, newEndDate1);
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails1.getId()).getEndDate()).isEqualTo(newEndDate1);
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails2.getId()).getEndDate()).isEqualTo(newEndDate2);
		assertThat(manager.get(zeCollection, xmlAuthorizationDetails3.getId()).getEndDate()).isEqualTo(newEndDate3);
	}

	@Test
	public void givenAuthorizationsListWhenGetFinishedAuthorizationsThenReturnTwo()
			throws Exception {
		manager = spy(getModelLayerFactory().getAuthorizationDetailsManager());
		XMLAuthorizationDetails authorizationThatHasFinishedTwoWeeksAgo = newAuthorizationValidFromTo(TWO_WEEKS_AGO, TWO_WEEKS_AGO);
		XMLAuthorizationDetails authorizationThatHasFinishedYesterday = newAuthorizationValidFromTo(TWO_WEEKS_AGO, YESTERDAY);
		XMLAuthorizationDetails authorizationThatFinishToday = newAuthorizationValidFromTo(TWO_WEEKS_AGO, NOW);
		XMLAuthorizationDetails authorizationWithoutStartAndEndDates = create(aString(), newRoleList(zeCollection), zeCollection);
		XMLAuthorizationDetails futureEnableXMLAuthorizationDetails = newAuthorizationValidFromTo(TOMORROW, IN_TO_WEEKS);
		when(manager.getAuthorizationsDetails(zeCollection)).thenReturn(
				asAuthorizationDetailsMap(authorizationThatHasFinishedTwoWeeksAgo, authorizationThatHasFinishedYesterday,
						authorizationThatFinishToday, authorizationWithoutStartAndEndDates, futureEnableXMLAuthorizationDetails));

		assertThat(manager.getListOfFinishedAuthorizationsIds(zeCollection)).containsOnly(
				authorizationThatHasFinishedTwoWeeksAgo.getId(), authorizationThatHasFinishedYesterday.getId());
	}

	@Test
	public void givenEnableAuthorizationWhenModifyEndDateThenItIsModified()
			throws Exception {
		XMLAuthorizationDetails xmlAuthorizationDetails = create(aString(), newRoleList(zeCollection), zeCollection);
		manager.add(xmlAuthorizationDetails);

		LocalDate endate = new LocalDate(2020, 01, 01);
		manager.modifyEndDate(xmlAuthorizationDetails, endate);

		assertThat(
				manager.get(xmlAuthorizationDetails.getCollection(), xmlAuthorizationDetails.getId()).getEndDate())
				.isEqualTo(endate);
	}

	private Map<String, XMLAuthorizationDetails> asAuthorizationDetailsMap(XMLAuthorizationDetails... details) {
		Map<String, XMLAuthorizationDetails> map = new HashMap<>();
		for (XMLAuthorizationDetails detail : details) {
			map.put(detail.getId(), detail);
		}
		return map;
	}

	private XMLAuthorizationDetails newAuthorizationValidFromTo(LocalDate startDate, LocalDate endDate) {
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
