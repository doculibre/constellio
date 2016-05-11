package com.constellio.app.ui.pages.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Locale;

import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.SearchBoostVO;
import com.constellio.app.ui.framework.builders.SearchBoostToVOBuilder;
import com.constellio.app.ui.framework.data.SearchBoostDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.MetadataListFilter;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchBoostManager;
import com.constellio.model.services.search.entities.SearchBoost;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;

/**
 * Created by Patrick on 2015-11-09.
 */
public class SearchBoostByMetadataPresenterAcceptTest extends ConstellioTest {

	public static final String METADATA = "metadata";
	@Mock SearchBoostView view;
	MockedNavigation navigator;
	UserServices userServices;
	SearchBoost searchBoostTitle, searchBoostCode;
	SearchBoostVO searchBoostVOTitle, searchBoostVOCode;
	SearchBoostByMetadataPresenter presenter;
	SessionContext sessionContext;
	MetadataSchemasManager metadataSchemasManager;
	SearchBoostManager searchBoostManager;
	SearchBoostToVOBuilder voBuilder = new SearchBoostToVOBuilder();
	SearchBoostDataProvider dataProvider;

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

		searchBoostTitle = new SearchBoost(METADATA, Schemas.TITLE.getDataStoreCode(), "Titre", 1d);
		searchBoostCode = new SearchBoost(METADATA, Schemas.CODE.getDataStoreCode(), "Code", 2d);
		searchBoostVOCode = voBuilder.build(searchBoostCode);

		presenter = new SearchBoostByMetadataPresenter(view);
	}

	@Test
	public void whenGetDataProviderThenOk()
			throws Exception {

		givenTwoSearchBoostByFields();
		dataProvider = presenter.newDataProvider();

		assertThat(dataProvider.size()).isEqualTo(2);
		assertThat(dataProvider.getSearchBoostVO(0)).isEqualToComparingFieldByField(voBuilder.build(searchBoostCode));
		assertThat(dataProvider.getSearchBoostVO(1)).isEqualToComparingFieldByField(voBuilder.build(searchBoostTitle));
	}

	@Test
	public void givenSearchBoostVOWhenAddButtonClickedThenOk()
			throws Exception {

		presenter.addButtonClicked(searchBoostVOCode, "1");
		dataProvider = presenter.newDataProvider();

		assertThat(dataProvider.size()).isEqualTo(1);
		presenter.getSearchBoostVO(0, dataProvider);
		assertThat(presenter.getSearchBoostVO(0, presenter.newDataProvider())).isEqualToComparingFieldByField(searchBoostVOCode);
	}

	@Test
	public void givenSearchBoostVOWhenEditButtonClickedThenOk()
			throws Exception {

		presenter.addButtonClicked(searchBoostVOCode, "1");

		presenter.editButtonClicked(searchBoostVOCode, "2", searchBoostVOCode);
		dataProvider = presenter.newDataProvider();

		assertThat(dataProvider.size()).isEqualTo(1);
		assertThat(presenter.getSearchBoostVO(0, dataProvider).getValue()).isEqualTo(2d);
		assertThat(presenter.getSearchBoostVO(0, dataProvider).getKey())
				.isEqualTo(searchBoostVOCode.getKey());
		assertThat(presenter.getSearchBoostVO(0, dataProvider).getLabel()).isEqualTo(searchBoostVOCode.getLabel());

	}

	@Test
	public void givenSearchBoostVOWhenDeleteButtonClickedThenOk()
			throws Exception {

		presenter.addButtonClicked(searchBoostVOCode, "1");

		presenter.deleteButtonClicked(searchBoostVOCode);
		dataProvider = presenter.newDataProvider();

		assertThat(dataProvider.size()).isEqualTo(0);
	}

	@Test
	public void whenGetMetadatasThenGetAllMetadatasFromCollection()
			throws Exception {

		MetadataListFilter filterSearchable = new MetadataListFilter() {
			@Override
			public boolean isReturned(Metadata metadata) {
				return metadata.isSearchable();
			}
		};
		assertThat(presenter.getMetadatasSearchBoostVO()).hasSize(
				metadataSchemasManager.getSchemaTypes(zeCollection).getAllMetadatas().only(filterSearchable).size());
		assertThat(presenter.getMetadatasSearchBoostVO()).extracting("key").doesNotHaveDuplicates();
	}

	@Test
	public void givenSearchBoostVOAndANotDoubleValueWhenAddButtonClickedThenDoNotAdd()
			throws Exception {

		presenter.addButtonClicked(searchBoostVOCode, "notDouble");
		dataProvider = presenter.newDataProvider();

		assertThat(dataProvider.size()).isEqualTo(0);
	}

	//
	private void givenTwoSearchBoostByFields() {
		searchBoostManager.add(zeCollection, searchBoostTitle);
		searchBoostManager.add(zeCollection, searchBoostCode);
	}
}
