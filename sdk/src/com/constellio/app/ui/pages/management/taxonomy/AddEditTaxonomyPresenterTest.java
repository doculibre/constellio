package com.constellio.app.ui.pages.management.taxonomy;

import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class AddEditTaxonomyPresenterTest extends ConstellioTest {

	@Mock AddEditTaxonomyView view;
	MockedNavigation navigator;
	@Mock ValueListServices valueListServices;
	@Mock Taxonomy taxonomy1, taxonomy2, taxonomy3, taxonomy4, taxonomy5;
	@Mock TaxonomiesManager taxonomiesManager;
	TaxonomyVO taxonomyVO;
	List<String> userIds;
	List<String> groupIds;
	AddEditTaxonomyPresenter presenter;
	String newTaxonomyTitle;
	MockedFactories mockedFactories = new MockedFactories();
	@Mock CollectionsManager collectionsManager;

	@Before
	public void setUp()
			throws Exception {

		navigator = new MockedNavigation();

		when(view.getConstellioFactories()).thenReturn(mockedFactories.getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.dakotaInCollection(zeCollection));
		when(view.navigate()).thenReturn(navigator);

		when(mockedFactories.getModelLayerFactory().getTaxonomiesManager()).thenReturn(taxonomiesManager);

		List<String> languages = new ArrayList<>();
		languages.add("en");
		languages.add("fr");

		when(mockedFactories.getAppLayerFactory().getCollectionsManager()).thenReturn(collectionsManager);
		when(mockedFactories.getAppLayerFactory().getCollectionsManager().getCollectionLanguages(anyString())).thenReturn(languages);


		newTaxonomyTitle = "taxonomy 1";
		userIds = new ArrayList<>();
		userIds.add("chuck");
		userIds.add("bob");

		groupIds = new ArrayList<>();
		groupIds.add("heroes");
		groupIds.add("legends");
		taxonomyVO = new TaxonomyVO("taxo1", newTaxonomyTitle, new ArrayList<String>(), zeCollection, userIds, groupIds, true);

		Map<Language, String> mapLangueTitle = new HashMap<>();
		mapLangueTitle.put(Language.French, taxonomyVO.getTitle());

		when(taxonomy1.getTitle()).thenReturn(mapLangueTitle);

		presenter = spy(new AddEditTaxonomyPresenter(view));

	}

	@Test
	public void whenSaveButtonClickedThenCreateIt()
			throws Exception {

		doReturn(valueListServices).when(presenter).valueListServices();

		presenter.saveButtonClicked(taxonomyVO);

		Map<Language, String> mapLangueTitle = new HashMap<>();
		mapLangueTitle.put(Language.French, taxonomyVO.getTitle());

		verify(valueListServices).createTaxonomy(mapLangueTitle, taxonomyVO.getUserIds(), taxonomyVO.getGroupIds(), true, true);
		verify(view.navigate().to()).listTaxonomies();
	}

	@Test
	public void givenActionEditWhenSaveButtonClickedThenEditIt()
			throws Exception {

		Map<Language, String> mapLangueTitle = new HashMap<>();
		mapLangueTitle.put(Language.French, taxonomyVO.getTitle());

		doReturn(taxonomy1).when(presenter).fetchTaxonomy(taxonomyVO.getCode());
		doReturn(taxonomy2).when(taxonomy1).withTitle(mapLangueTitle);
		doReturn(taxonomy3).when(taxonomy2).withUserIds(taxonomyVO.getUserIds());
		doReturn(taxonomy4).when(taxonomy3).withGroupIds(taxonomyVO.getGroupIds());
		doReturn(taxonomy5).when(taxonomy4).withVisibleInHomeFlag(taxonomyVO.isVisibleInHomePage());
		doReturn(valueListServices).when(presenter).valueListServices();
		when(presenter.isActionEdit()).thenReturn(true);

		presenter.saveButtonClicked(taxonomyVO);

		verify(presenter).fetchTaxonomy(taxonomyVO.getCode());
		verify(taxonomiesManager).editTaxonomy(taxonomy5);
		verify(view.navigate().to()).listTaxonomies();
	}

	@Test
	public void givenExistentTitleWhenSaveButtonClickedThenDoNotCreateIt()
			throws Exception {

		List<Taxonomy> existentTaxonomies = new ArrayList<>();
		existentTaxonomies.add(taxonomy1);
		doReturn(valueListServices).when(presenter).valueListServices();
		doReturn(existentTaxonomies).when(valueListServices).getTaxonomies();

		presenter.saveButtonClicked(taxonomyVO);

		Map<Language, String> mapLangueTitle = new HashMap<>();
		mapLangueTitle.put(Language.French, taxonomyVO.getTitle());

		verify(valueListServices, never()).createTaxonomy(mapLangueTitle, taxonomyVO.getUserIds(),
				taxonomyVO.getGroupIds(), true, true);
	}

	@Test
	public void whenCancelButtonClickThenReturnToList()
			throws Exception {
		presenter.cancelButtonClicked();

		verify(view.navigate().to()).listTaxonomies();
	}
}
