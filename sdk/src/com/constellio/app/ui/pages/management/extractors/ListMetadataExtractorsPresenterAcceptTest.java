package com.constellio.app.ui.pages.management.extractors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.MetadataExtractorVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;

public class ListMetadataExtractorsPresenterAcceptTest extends ConstellioTest {

	@Mock ListMetadataExtractorsView view;
	MockedNavigation navigator;

	ListMetadataExtractorsPresenter presenter;
	SessionContext sessionContext;
	RMTestRecords rmTestRecords = new RMTestRecords(zeCollection);
	MetadataSchemaTypes types;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(rmTestRecords).withAllTestUsers()
		);

		navigator = new MockedNavigation();

		types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);

		sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(view.getSessionContext()).thenReturn(sessionContext);
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.navigate()).thenReturn(navigator);

		presenter = new ListMetadataExtractorsPresenter(view);
	}

	@Test
	public void whenGetMetadataExtractorVOsThenOk()
			throws Exception {

		List<MetadataExtractorVO> metadataExtractorVOs = presenter.getMetadataExtractorVOs(sessionContext, types);

		assertThat(metadataExtractorVOs).extracting("metadataVO.code", "properties", "metadataVO.schema.code")
				.contains(
						tuple("document_default_author", Arrays.asList("author"), "document_default"),
						tuple("document_default_company", Arrays.asList("company"), "document_default"),
						tuple("document_default_keywords", Arrays.asList("keywords"), "document_default"),
						tuple("document_default_subject", Arrays.asList("subject"), "document_default"),
						tuple("document_default_title", Arrays.asList("title"), "document_default")
				);
	}

	@Test
	public void givenMetadataExtractorVOWhenEditButtonClickedThenOk()
			throws Exception {

		MetadataExtractorVO metadataExtractorVO = presenter.getMetadataExtractorVOs(sessionContext, types).get(0);

		presenter.editButtonClicked(metadataExtractorVO);

		verify(view.navigate().to()).editMetadataExtractor(metadataExtractorVO.getMetadataVO().getCode());
	}

	@Test
	public void whenAddButtonClickedThenOk()
			throws Exception {

		presenter.addButtonClicked();

		verify(view.navigate().to()).addMetadataExtractor();
	}

	@Test
	public void whenBackButtonClickedThenOk()
			throws Exception {

		presenter.backButtonClicked();

		verify(view.navigate().to()).adminModule();
	}

	@Test
	public void givenMetadataExtractorVOWhenDeleteButtonClickedThenOk()
			throws Exception {

		List<MetadataExtractorVO> metadataExtractorVOs = presenter.getMetadataExtractorVOs(sessionContext, types);
		MetadataExtractorVO metadataExtractorVO = metadataExtractorVOs.get(0);

		assertThat(metadataExtractorVOs).extracting("metadataVO.code").contains(
				metadataExtractorVO.getMetadataVO().getCode()
		);

		presenter.deleteButtonClicked(metadataExtractorVO);

		assertThat(presenter.getMetadataExtractorVOs(sessionContext, types))
				.extracting("metadataVO.code").doesNotContain(
				metadataExtractorVO.getMetadataVO().getCode()
		);
	}

	@Test
	public void givenUserWithPageAccessWhenHasPageAccessThenReturnFalse()
			throws Exception {

		assertThat(presenter.hasPageAccess("", rmTestRecords.getAdmin())).isTrue();
	}

	@Test
	public void givenUserWithoutPageAccessWhenHasPageAccessThenReturnFalse()
			throws Exception {
		assertThat(presenter.hasPageAccess("", rmTestRecords.getBob_userInAC())).isFalse();
	}
}
