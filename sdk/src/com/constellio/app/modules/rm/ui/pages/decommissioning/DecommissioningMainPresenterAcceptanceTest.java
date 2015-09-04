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
package com.constellio.app.modules.rm.ui.pages.decommissioning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.decommissioning.DecomissioningListQueryFactory;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;

public class DecommissioningMainPresenterAcceptanceTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	@Mock
	DecommissioningMainView view;
	@Mock
	ConstellioFactories factories;

	DecommissioningMainPresenter presenter;
	private RecordServices recordServices;
	private SearchServices searchServices;
	private SessionContext sessionContext;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		sessionContext = FakeSessionContext.chuckNorrisInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);

		when(factories.getModelLayerFactory()).thenReturn(getModelLayerFactory());
		when(factories.getAppLayerFactory()).thenReturn(getAppLayerFactory());
		when(view.getSessionContext()).thenReturn(sessionContext);
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getConstellioFactories()).thenReturn(factories);

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		presenter = new DecommissioningMainPresenter(view);
	}

	@Test
	public void givenUserHasNoPermissionWhenSearchListsSentForValidationThenReturnEmptyList()
			throws RecordServicesException {
		LogicalSearchQuery query = queryFactory().getListsPendingValidationQuery(records.getAlice());
		Record toValidate = searchServices.searchSingleResult(query.getCondition());
		assertThat(toValidate).isNull();
	}

	@Test
	public void givenListWithValidationWhenGetListsSentForValidationThenReturnValidList()
			throws RecordServicesException {
		Record sentForValidation = searchServices.searchSingleResult(queryFactory().getListsPendingValidationQuery(
				records.getAdmin()).getCondition());
		assertThat(sentForValidation.getId()).isEqualTo(records.getList25().getId());
	}

	@Test
	public void givenNoValidationWhenSearchListsToValidateByChuckThenReturnEmptyList()
			throws RecordServicesException {
		Record toValidate = searchServices
				.searchSingleResult(queryFactory().getListsToValidateQuery(records.getChuckNorris()).getCondition());
		assertThat(toValidate).isNull();
	}

	@Test
	public void givenListWithValidationSentToBobAndChuckAndValidatedByChuckWhenSearchListsToValidateByChuckThenReturnEmptyList()
			throws RecordServicesException {
		addUserValidationToList(addListWithValidationRequestToBobAndChuck(), records.getChuckNorris());

		Record toValidate = searchServices
				.searchSingleResult(queryFactory().getListsToValidateQuery(records.getChuckNorris()).getCondition());
		assertThat(toValidate).isNull();
	}

	@Test
	public void givenListWithValidationSentToBobAndChuckAndValidatedByBobWhenSearchListsToValidateByChuckThenReturnEmptyList()
			throws RecordServicesException {
		DecommissioningList newList = addListWithValidationRequestToBobAndChuck();
		addUserValidationToList(newList, records.getBob_userInAC());

		Record toValidate = searchServices
				.searchSingleResult(queryFactory().getListsToValidateQuery(records.getChuckNorris()).getCondition());
		assertThat(toValidate).isNotNull();
		assertThat(toValidate.getId()).isEqualTo(newList.getId());
	}

	void addUserValidationToList(DecommissioningList list, User user)
			throws RecordServicesException {
		list.getValidationFor(user.getId()).setValidationDate(TimeProvider.getLocalDate());
		recordServices.add(list);
	}

	private DecomissioningListQueryFactory queryFactory() {
		return new DecomissioningListQueryFactory(zeCollection, getModelLayerFactory());
	}

	private DecommissioningList addListWithValidationRequestToBobAndChuck()
			throws RecordServicesException {
		String bob = records.getBob_userInAC().getId();
		String chuck = records.getChuckNorris().getId();
		DecommissioningList list = records.getList01()
				.addValidationRequest(bob, TimeProvider.getLocalDate())
				.addValidationRequest(chuck, TimeProvider.getLocalDate());
		recordServices.add(list);
		return list;
	}
}
