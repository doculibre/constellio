package com.constellio.app.ui.pages.management.taxonomy;

import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.services.collections.CollectionsListManager;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
	Map<Language, String> newTaxonomyTitle;
	Map<Language, String> newTaxonomyAbv;
	MockedFactories mockedFactories = new MockedFactories();
	@Mock
	CollectionsListManager collectionsListManager;

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

		when(mockedFactories.getModelLayerFactory().getCollectionsListManager()).thenReturn(collectionsListManager);
		when(collectionsListManager.getCollectionLanguages(anyString())).thenReturn(languages);

		newTaxonomyTitle = new HashMap<>();
		newTaxonomyTitle.put(Language.French, "taxonomy 1");

		newTaxonomyAbv = new HashMap<>();
		newTaxonomyAbv.put(Language.French, "taxo1");

		userIds = new ArrayList<>();
		userIds.add("chuck");
		userIds.add("bob");

		groupIds = new ArrayList<>();
		groupIds.add("heroes");
		groupIds.add("legends");
		taxonomyVO = new TaxonomyVO("taxo1", newTaxonomyTitle, newTaxonomyAbv, new ArrayList<String>(),
				zeCollection, userIds, groupIds, true);

		when(taxonomy1.getTitle()).thenReturn(taxonomyVO.getTitleMap());
		when(taxonomy1.getAbbreviation()).thenReturn(taxonomyVO.getAbbreviationMap());

		presenter = spy(new AddEditTaxonomyPresenter(view));

	}

	@Test
	public void whenSaveButtonClickedThenCreateIt()
			throws Exception {

		doReturn(valueListServices).when(presenter).valueListServices();

		presenter.saveButtonClicked(taxonomyVO, true);

		verify(valueListServices).createTaxonomy(taxonomyVO.getTitleMap(), taxonomyVO.getAbbreviationMap(),
				taxonomyVO.getUserIds(), taxonomyVO.getGroupIds(), true, true);
		verify(view.navigate().to()).listTaxonomies();
	}

	@Test
	public void givenExistentTitleWhenSaveButtonClickedThenDoNotCreateIt()
			throws Exception {

		List<Taxonomy> existentTaxonomies = new ArrayList<>();
		existentTaxonomies.add(taxonomy1);
		doReturn(valueListServices).when(presenter).valueListServices();
		doReturn(existentTaxonomies).when(valueListServices).getTaxonomies();

		presenter.saveButtonClicked(taxonomyVO, false);

		verify(valueListServices, never()).createTaxonomy(taxonomyVO.getTitleMap(), taxonomyVO.getAbbreviationMap(),
				taxonomyVO.getUserIds(), taxonomyVO.getGroupIds(), true, true);
	}

	@Test
	public void whenCancelButtonClickThenReturnToList()
			throws Exception {
		presenter.cancelButtonClicked();

		verify(view.navigate().to()).listTaxonomies();
	}
}
