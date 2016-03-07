package com.constellio.app.ui.pages.management.taxonomy;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;

public class AddEditTaxonomyPresenterTest extends ConstellioTest {

	@Mock AddEditTaxonomyView view;
	@Mock CoreViews navigator;
	@Mock ValueListServices valueListServices;
	@Mock Taxonomy taxonomy1, taxonomy2, taxonomy3, taxonomy4, taxonomy5;
	@Mock TaxonomiesManager taxonomiesManager;
	TaxonomyVO taxonomyVO;
	List<String> userIds;
	List<String> groupIds;
	AddEditTaxonomyPresenter presenter;
	String newTaxonomyTitle;
	MockedFactories mockedFactories = new MockedFactories();

	@Before
	public void setUp()
			throws Exception {

		when(view.getConstellioFactories()).thenReturn(mockedFactories.getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.dakotaInCollection(zeCollection));
		when(view.navigateTo()).thenReturn(navigator);

		when(mockedFactories.getModelLayerFactory().getTaxonomiesManager()).thenReturn(taxonomiesManager);

		newTaxonomyTitle = "taxonomy 1";
		userIds = new ArrayList<>();
		userIds.add("chuck");
		userIds.add("bob");

		groupIds = new ArrayList<>();
		groupIds.add("heroes");
		groupIds.add("legends");
		taxonomyVO = new TaxonomyVO("taxo1", newTaxonomyTitle, new ArrayList<String>(), zeCollection, userIds, groupIds, true);

		when(taxonomy1.getTitle()).thenReturn(newTaxonomyTitle);

		presenter = spy(new AddEditTaxonomyPresenter(view));

	}

	@Test
	public void whenSaveButtonClickedThenCreateIt()
			throws Exception {

		doReturn(valueListServices).when(presenter).valueListServices();

		presenter.saveButtonClicked(taxonomyVO);

		verify(valueListServices).createTaxonomy(taxonomyVO.getTitle(), taxonomyVO.getUserIds(), taxonomyVO.getGroupIds(), true);
		verify(view.navigateTo()).listTaxonomies();
	}

	@Test
	public void givenActionEditWhenSaveButtonClickedThenEditIt()
			throws Exception {
		doReturn(taxonomy1).when(presenter).fetchTaxonomy(taxonomyVO.getCode());
		doReturn(taxonomy2).when(taxonomy1).withTitle(taxonomyVO.getTitle());
		doReturn(taxonomy3).when(taxonomy2).withUserIds(taxonomyVO.getUserIds());
		doReturn(taxonomy4).when(taxonomy3).withGroupIds(taxonomyVO.getGroupIds());
		doReturn(taxonomy5).when(taxonomy4).withVisibleInHomeFlag(taxonomyVO.isVisibleInHomePage());
		doReturn(valueListServices).when(presenter).valueListServices();
		when(presenter.isActionEdit()).thenReturn(true);

		presenter.saveButtonClicked(taxonomyVO);

		verify(presenter).fetchTaxonomy(taxonomyVO.getCode());
		verify(taxonomiesManager).editTaxonomy(taxonomy5);
		verify(view.navigateTo()).listTaxonomies();
	}

	@Test
	public void givenExistentTitleWhenSaveButtonClickedThenDoNotCreateIt()
			throws Exception {

		List<Taxonomy> existentTaxonomies = new ArrayList<>();
		existentTaxonomies.add(taxonomy1);
		doReturn(valueListServices).when(presenter).valueListServices();
		doReturn(existentTaxonomies).when(valueListServices).getTaxonomies();

		presenter.saveButtonClicked(taxonomyVO);

		verify(valueListServices, never()).createTaxonomy(taxonomyVO.getTitle(), taxonomyVO.getUserIds(),
				taxonomyVO.getGroupIds(), true);
	}

	@Test
	public void whenCancelButtonClickThenReturnToList()
			throws Exception {
		presenter.cancelButtonClicked();

		verify(view.navigateTo()).listTaxonomies();
	}
}
