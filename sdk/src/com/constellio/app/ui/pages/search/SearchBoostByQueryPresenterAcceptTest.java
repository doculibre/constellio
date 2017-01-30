package com.constellio.app.ui.pages.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.ui.entities.SearchBoostVO;
import com.constellio.app.ui.framework.builders.SearchBoostToVOBuilder;
import com.constellio.app.ui.framework.data.SearchBoostDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchBoostManager;
import com.constellio.model.services.search.entities.SearchBoost;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;

/**
 * Created by Patrick on 2015-11-09.
 */
public class SearchBoostByQueryPresenterAcceptTest extends ConstellioTest {

	public static final String QUERY = "query";
	@Mock SearchBoostView view;
	MockedNavigation navigator;
	UserServices userServices;
	SearchBoost searchBoostQuery1, searchBoostQuery2;
	SearchBoostVO searchBoostQuery1VO, searchBoostQuery2VO;
	SearchBoostByQueryPresenter presenter;
	SessionContext sessionContext;
	MetadataSchemasManager metadataSchemasManager;
	SearchBoostManager searchBoostManager;
	SearchBoostToVOBuilder voBuilder = new SearchBoostToVOBuilder();
	SearchBoostDataProvider dataProvider;

	int defaultQtyOfBoosts = 0;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withAllTestUsers(),
				withCollection("otherCollection")
		);

		sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		userServices = getModelLayerFactory().newUserServices();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		searchBoostManager = getModelLayerFactory().getSearchBoostManager();
		when(view.getSessionContext()).thenReturn(sessionContext);
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.navigate()).thenReturn(navigator);

		//		for (String collection : getModelLayerFactory().getCollectionsListManager().getCollectionsExcludingSystem()) {
		//			for (SearchBoost searchBoost : searchBoostManager.getAllSearchBoostsByMetadataType(collection)) {
		//				searchBoostManager.delete(zeCollection, searchBoost);
		//			}
		//		}

		searchBoostQuery1 = new SearchBoost(QUERY, "*", "1 Query", 1d);
		searchBoostQuery2 = new SearchBoost(QUERY, "code:a*", "2 Query", 2d);
		searchBoostQuery1VO = voBuilder.build(searchBoostQuery1);
		searchBoostQuery2VO = voBuilder.build(searchBoostQuery2);

		presenter = new SearchBoostByQueryPresenter(view);
	}

	@Test
	public void whenGetDataProviderThenOk()
			throws Exception {

		givenTwoSearchBoostByQuery();
		dataProvider = presenter.newDataProvider();

		assertThat(dataProvider.size()).isEqualTo(defaultQtyOfBoosts + 2);
		assertThat(dataProvider.getSearchBoostVO(0)).isEqualToComparingFieldByField(voBuilder.build(searchBoostQuery1));
		assertThat(dataProvider.getSearchBoostVO(1)).isEqualToComparingFieldByField(voBuilder.build(searchBoostQuery2));
	}

	@Test
	public void givenSearchBoostVOWhenAddButtonClickedThenOk()
			throws Exception {

		presenter.addButtonClicked(searchBoostQuery1VO, "1");
		dataProvider = presenter.newDataProvider();

		assertThat(dataProvider.size()).isEqualTo(defaultQtyOfBoosts + 1);
		presenter.getSearchBoostVO(0, dataProvider);
		assertThat(presenter.getSearchBoostVO(0, presenter.newDataProvider()))
				.isEqualToComparingFieldByField(searchBoostQuery1VO);
	}

	@Test
	public void givenSearchBoostVOWhenEditButtonClickedThenOk()
			throws Exception {

		presenter.addButtonClicked(searchBoostQuery1VO, "1");

		presenter.editButtonClicked(searchBoostQuery2VO, "2", searchBoostQuery1VO);
		dataProvider = presenter.newDataProvider();

		assertThat(dataProvider.size()).isEqualTo(defaultQtyOfBoosts + 1);
		assertThat(presenter.getSearchBoostVO(0, dataProvider).getValue()).isEqualTo(2d);
		assertThat(presenter.getSearchBoostVO(0, dataProvider).getKey())
				.isEqualTo(searchBoostQuery2VO.getKey());
		assertThat(presenter.getSearchBoostVO(0, dataProvider).getLabel()).isEqualTo(searchBoostQuery2VO.getLabel());

	}

	@Test
	public void givenSearchBoostVOWhenDeleteButtonClickedThenOk()
			throws Exception {

		presenter.addButtonClicked(searchBoostQuery1VO, "1");

		presenter.deleteButtonClicked(searchBoostQuery1VO);
		dataProvider = presenter.newDataProvider();

		assertThat(dataProvider.size()).isEqualTo(defaultQtyOfBoosts + 0);
	}

	@Test
	public void givenSearchBoostVOAndANotDoubleValueWhenAddButtonClickedThenDoNotAdd()
			throws Exception {

		presenter.addButtonClicked(searchBoostQuery1VO, "notDouble");
		dataProvider = presenter.newDataProvider();

		assertThat(dataProvider.size()).isEqualTo(defaultQtyOfBoosts + 0);
	}

	//
	private void givenTwoSearchBoostByQuery() {
		searchBoostManager.add(zeCollection, searchBoostQuery1);
		searchBoostManager.add(zeCollection, searchBoostQuery2);
	}
}
