package com.constellio.app.ui.pages.search.savedSearch;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.app.ui.pages.search.criteria.Criterion.SearchOperator;
import com.constellio.app.ui.pages.search.criteria.SearchCriterionTestSetup;
import com.constellio.app.ui.pages.search.criteria.SearchCriterionTestSetup.CriterionTestRecord;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SavedSearchPresenterAcceptanceTest extends ConstellioTest {
	public static final String STRING_VALUE = "string value";
	SearchCriterionTestSetup setup = new SearchCriterionTestSetup(zeCollection);
	CriterionTestRecord shortcuts = setup.getShortcuts();

	MockedNavigation navigator;
	@Mock private SavedSearchView view;

	private SavedSearchPresenter presenter;
	private RecordServices recordServices;
	private RecordToVOBuilder voBuilder;
	private User adminUser, aliceUser, bobUser;
	private SessionContext sessionContext;

	@Before
	public void setUp()
			throws Exception {

		navigator = new MockedNavigation();

		prepareSystem(withZeCollection().withConstellioRMModule().withAllTestUsers());
		defineSchemasManager().using(setup);
		adminUser = getModelLayerFactory().newUserServices().getUserInCollection(admin, zeCollection);
		aliceUser = getModelLayerFactory().newUserServices().getUserInCollection(alice, zeCollection);
		bobUser = getModelLayerFactory().newUserServices().getUserInCollection(bob, zeCollection);
		sessionContext = FakeSessionContext.forRealUserIncollection(adminUser);
		when(view.getSessionContext()).thenReturn(sessionContext);
		when(view.getCollection()).thenReturn(zeCollection);
		voBuilder = new RecordToVOBuilder();

		recordServices = getModelLayerFactory().newRecordServices();

		presenter = new SavedSearchPresenter(view);
		when(view.navigate()).thenReturn(navigator);
	}

	@Test
	public void givenUserAndPublicSearchesThenAllDataProvidersOk()
			throws Exception {
		SavedSearch userSavedSearch1 = newSavedSearch(adminUser, false);
		SavedSearch userSavedSearch2 = newSavedSearch(adminUser, false);
		SavedSearch publicSavedSearch1 = newSavedSearch(null, true);
		SavedSearch publicSavedSearch2 = newSavedSearch(adminUser, true, singletonList(aliceUser.getId()), null);
		SavedSearch publicSavedSearch3 = newSavedSearch(adminUser, true);

		verifyThat(presenter.getUserSearchesDataProvider()).containsOnly(userSavedSearch1, userSavedSearch2);
		verifyThat(presenter.getPublicSearchesDataProvider()).containsOnly(publicSavedSearch1, publicSavedSearch2, publicSavedSearch3);
	}

	@Test
	public void whenEditingSavedSearchThenEditedProperly()
			throws Exception {

		SavedSearch userSavedSearch1 = newSavedSearch(adminUser, false);

		RecordVO recordVO = toVO(userSavedSearch1);
		recordVO.setTitle("New Title");
		recordVO.set(SavedSearch.PUBLIC, true);
		presenter.searchModificationRequested(recordVO.getId(), "New Title", true,
				aliceUser.getUserGroups(), singletonList(aliceUser.getId()));

		userSavedSearch1 = new SavedSearch(recordServices.getDocumentById(userSavedSearch1.getId()), setup.getTypes());

		assertThat(userSavedSearch1.getTitle()).isEqualTo("New Title");
		assertThat(userSavedSearch1.isPublic()).isTrue();
		assertThat(userSavedSearch1.getSharedUsers()).containsOnly(aliceUser.getId());
		assertThat(userSavedSearch1.getSharedGroups()).containsOnly(aliceUser.getUserGroups().toArray(new String[0]));
		verify(navigator.to()).listSavedSearches();
	}

	@Test
	public void whenDeletingRecordThenRecordDeleted()
			throws Exception {
		MockedNavigation navigator = new MockedNavigation();
		when(view.navigate()).thenReturn(navigator);

		SavedSearch userSavedSearch1 = newSavedSearch(adminUser, false);
		RecordVO recordVO = toVO(userSavedSearch1);

		presenter.deleteButtonClicked(recordVO);

		try {
			recordServices.getDocumentById(userSavedSearch1.getId());
		} catch (NoSuchRecordWithId ex) {
			// SUCCESS
		}
	}

	@Test
	public void whenSavingRestrictedUserPublicSearchThenNotSharedToOtherUser() throws Exception {
		newSavedSearch(aliceUser, true, singletonList(bobUser.getId()), null);
		assertThat(verifyThat(presenter.getPublicSearchesDataProvider()).recordsIds).isEmpty();
	}

	@Test
	public void whenSavingRestrictedGroupPublicSearchThenNotSharedToUserNotInGroup() throws Exception {
		newSavedSearch(aliceUser, true, null, aliceUser.getUserGroups());
		assertThat(verifyThat(presenter.getPublicSearchesDataProvider()).recordsIds).isEmpty();
	}

	@Test
	public void whenSavingUnRestrictedPublicSearchThenSharedToAll() throws Exception {
		SavedSearch savedSearch1 = newSavedSearch(aliceUser, true);
		SavedSearch savedSearch2 = newSavedSearch(null, true);

		verifyThat(presenter.getPublicSearchesDataProvider()).containsOnly(savedSearch1, savedSearch2);
	}

	public RecordVO toVO(SavedSearch userSavedSearch1) {
		return voBuilder.build(userSavedSearch1.getWrappedRecord(), VIEW_MODE.FORM, sessionContext);
	}

	private SavedSearch newSavedSearch(User user, boolean isPublic) throws RecordServicesException {
		return newSavedSearch(user, isPublic, null, null);
	}

	private SavedSearch newSavedSearch(User user, boolean isPublic, List<String> users, List<String> groups)
			throws RecordServicesException {
		SavedSearch savedSearch = new SavedSearch(recordServices.newRecordWithSchema(setup.getSchema(
				SavedSearch.DEFAULT_SCHEMA)), setup.getTypes());
		savedSearch.setAdvancedSearch(Arrays.asList(stringCriterion()));
		if (user != null) {
			savedSearch.setUser(user.getId());
		}
		if (users != null) {
			savedSearch.setSharedUsers(users);
		}
		if (groups != null) {
			savedSearch.setSharedGroups(groups);
		}
		savedSearch.setPublic(isPublic);
		recordServices.add(savedSearch, (user != null ? user : adminUser));
		return savedSearch;
	}

	private Criterion stringCriterion() {
		// criterionTestRecord_default_aString CONTAINS "string value"
		Criterion criterion = new Criterion(shortcuts.code());
		criterion.setMetadata(shortcuts.aString().getCode(), shortcuts.aString().getType(), null);
		criterion.setSearchOperator(SearchOperator.CONTAINS_TEXT);
		criterion.setValue(STRING_VALUE);
		return criterion;
	}

	//	private Criterion relativeNewerThanDateCriterion() {
	//		Criterion criterion = new Criterion(shortcuts.code());
	//		criterion.setMetadata(shortcuts.aDate().getCode(), shortcuts.aDate().getType(), null);
	//		criterion.setSearchOperator(SearchOperator.NEWER_THAN);
	//		//		LocalDateTime ldt = TimeProvider.getLocalDateTime();
	//		criterion.setMeasuringUnitTimeValue(2);
	//		criterion.setMeasuringUnitTime(MeasuringUnitTime.DAYS);
	//		return criterion;
	//	}

	private DataProvided verifyThat(RecordVODataProvider dataProvider) {
		List<String> recordsIds = new ArrayList<>();
		for (RecordVO recordVO : dataProvider.listRecordVOs(0, dataProvider.size())) {
			recordsIds.add(recordVO.getId());
		}
		return new DataProvided(recordsIds);
	}

	private class DataProvided {

		List<String> recordsIds;

		public DataProvided(List<String> recordsIds) {
			this.recordsIds = recordsIds;
		}

		public void containsOnly(RecordWrapper... records) {
			assertThat(records.length).isEqualTo(recordsIds.size());
			for (RecordWrapper record : records) {
				assertThat(recordsIds).contains(record.getId());
			}
		}

	}
}
