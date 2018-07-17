package com.constellio.app.ui.pages.management.taxonomy;

import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class ListTaxonomyPresenterTest extends ConstellioTest {

	@Mock ListTaxonomyViewImpl view;
	MockedNavigation navigator;
	@Mock ValueListServices valueListServices;
	@Mock Taxonomy taxonomy1;
	@Mock TaxonomyVO taxonomyVO;
	ListTaxonomyPresenter presenter;
	MockedFactories mockedFactories = new MockedFactories();

	@Before
	public void setUp()
			throws Exception {

		navigator = new MockedNavigation();

		when(view.getConstellioFactories()).thenReturn(mockedFactories.getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.dakotaInCollection(zeCollection));
		when(view.navigate()).thenReturn(navigator);

		Map<Language, String> labelTitle = new HashMap<>();
		labelTitle.put(Language.French, "taxonomy 1");

		when(taxonomy1.getTitle()).thenReturn(labelTitle);

		presenter = spy(new ListTaxonomyPresenter(view));

		doReturn(valueListServices).when(presenter).valueListServices();
	}

	@Test
	public void whenAddButtonClikedThenNavigateToAddEditTaxonomy() {
		presenter.addButtonClicked();
		verify(view.navigate().to()).addTaxonomy();
	}

	@Test
	public void whenEditButtonClikedThenNavigateToAddEditTaxonomyWithCorrectParams() {
		presenter.editButtonClicked("taxo1Code");
		verify(view.navigate().to()).editTaxonomy("taxo1Code");
	}

	@Test
	public void whenDisplayButtonClikedThenNavigateToAddEditTaxonomyWithCorrectParams()
			throws Exception {

		when(taxonomyVO.getCode()).thenReturn("taxoCode");

		presenter.displayButtonClicked(taxonomyVO);

		verify(view.navigate().to()).taxonomyManagement("taxoCode");
	}

}
