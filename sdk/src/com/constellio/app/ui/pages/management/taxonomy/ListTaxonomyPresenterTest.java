package com.constellio.app.ui.pages.management.taxonomy;

import com.constellio.app.extensions.CannotDeleteTaxonomyException;
import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.services.metadata.MetadataDeletionException;
import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.app.ui.util.MessageUtils;
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

import java.util.HashMap;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListTaxonomyPresenterTest extends ConstellioTest {

	@Mock ListTaxonomyViewImpl view;
	@Mock ValueListServices valueListServices;
	@Mock Taxonomy taxonomy1;
	@Mock TaxonomyVO taxonomyVO;
	@Mock TaxonomiesManager taxonomiesManager;

	MockedNavigation navigator;
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

	@Test
	public void whenDeleteButtonClickedAndTaxonomyHasValidationErrorsThenDisplayErrorWindow()
			throws MetadataDeletionException {
		CannotDeleteTaxonomyException exception = new CannotDeleteTaxonomyException("errorMessage");
		presenter = spy(new ListTaxonomyPresenter(view, taxonomiesManager));
		when(taxonomiesManager.getEnabledTaxonomyWithCode(zeCollection, "taxo1Code")).thenReturn(taxonomy1);
		doThrow(exception).when(presenter).validateDeletable("taxo1Code");

		presenter.deleteButtonClicked("taxo1Code");

		verify(taxonomiesManager, never()).deleteWithoutValidations(taxonomy1);
		verify(view).showMessage(MessageUtils.toMessage(exception));
	}

	@Test
	public void whenDeleteButtonClickedAndTaxonomyHasConceptsThenDisplayErrorWindow()
			throws MetadataDeletionException {
		presenter = spy(new ListTaxonomyPresenter(view, taxonomiesManager));
		when(taxonomiesManager.getEnabledTaxonomyWithCode(zeCollection, "taxo1Code")).thenReturn(taxonomy1);
		doNothing().when(presenter).validateDeletable("taxo1Code");
		doReturn(true).when(presenter).hasConcepts(taxonomy1);

		presenter.deleteButtonClicked("taxo1Code");

		verify(taxonomiesManager, never()).deleteWithoutValidations(taxonomy1);
		verify(view).showMessage($("ListTaxonomyView.cannotDeleteTaxonomy"));
	}

	@Test
	public void whenDeleteButtonClickedAndTaxonomyDoesntHaveConceptsThenDeleteTaxonomyAndReferencedMetadatas()
			throws MetadataDeletionException {
		presenter = spy(new ListTaxonomyPresenter(view, taxonomiesManager));
		when(taxonomiesManager.getEnabledTaxonomyWithCode(zeCollection, "taxo1Code")).thenReturn(taxonomy1);
		doNothing().when(presenter).deleteMetadatasInClassifiedObjects(taxonomy1);
		doNothing().when(presenter).validateDeletable("taxo1Code");
		doReturn(false).when(presenter).hasConcepts(taxonomy1);

		presenter.deleteButtonClicked("taxo1Code");

		verify(taxonomiesManager).deleteWithoutValidations(taxonomy1);
		verify(view.navigate().to()).listTaxonomies();
	}

}
