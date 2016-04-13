package com.constellio.app.ui.pages.management.valueDomains;

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
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaTypeToVOBuilder;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;

public class ListValueDomainPresenterTest extends ConstellioTest {

	@Mock ListValueDomainViewImpl view;
	@Mock CoreViews navigator;
	@Mock ValueListServices valueListServices;
	@Mock MetadataSchemaType valueDomainType1;
	@Mock MetadataSchemaTypeToVOBuilder metadataSchemaTypeToVOBuilder;
	@Mock MetadataSchemaTypeVO metadataSchemaTypeVO;
	ListValueDomainPresenter presenter;
	String newValueDomainTitle;
	MockedFactories mockedFactories = new MockedFactories();

	@Before
	public void setUp()
			throws Exception {

		when(view.getConstellioFactories()).thenReturn(mockedFactories.getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.dakotaInCollection(zeCollection));
		when(view.navigateTo()).thenReturn(navigator);

		newValueDomainTitle = "new value domain";
		when(valueDomainType1.getLabel(Language.French)).thenReturn(newValueDomainTitle);

		presenter = spy(new ListValueDomainPresenter(view));

	}

	@Test
	public void whenValueDomainCreationRequestedThenCreateIt()
			throws Exception {

		doReturn(valueListServices).when(presenter).valueListServices();

		presenter.valueDomainCreationRequested(newValueDomainTitle);

		verify(valueListServices).createValueDomain(newValueDomainTitle);
		verify(view).refreshTable();
	}

	@Test
	public void givenExistentTitleWhenValueDomainCreationRequestedThenDoNotCreateIt()
			throws Exception {

		List<MetadataSchemaType> existentMetadataSchemaTypes = new ArrayList<>();
		existentMetadataSchemaTypes.add(valueDomainType1);
		doReturn(valueListServices).when(presenter).valueListServices();
		doReturn(existentMetadataSchemaTypes).when(valueListServices).getValueDomainTypes();
		when(presenter.newMetadataSchemaTypeToVOBuilder()).thenReturn(metadataSchemaTypeToVOBuilder);
		when(metadataSchemaTypeToVOBuilder.build(valueDomainType1)).thenReturn(metadataSchemaTypeVO);

		presenter.valueDomainCreationRequested(newValueDomainTitle);

		verify(valueListServices, never()).createTaxonomy(newValueDomainTitle);
		verify(view, never()).refreshTable();
	}

	@Test
	public void givenEmptyTitleWhenTaxonomyCreationRequestedThenDoNotCreateIt()
			throws Exception {

		presenter.valueDomainCreationRequested(" ");

		verify(valueListServices, never()).createTaxonomy(newValueDomainTitle);
		verify(view, never()).refreshTable();
	}

	@Test
	public void givenExistentTitleWithSpacesWhenValueDomainCreationRequestedThenDoNotCreateIt()
			throws Exception {

		List<MetadataSchemaType> existentMetadataSchemaTypes = new ArrayList<>();
		existentMetadataSchemaTypes.add(valueDomainType1);
		doReturn(valueListServices).when(presenter).valueListServices();
		doReturn(existentMetadataSchemaTypes).when(valueListServices).getValueDomainTypes();
		when(presenter.newMetadataSchemaTypeToVOBuilder()).thenReturn(metadataSchemaTypeToVOBuilder);
		when(metadataSchemaTypeToVOBuilder.build(valueDomainType1)).thenReturn(metadataSchemaTypeVO);

		presenter.valueDomainCreationRequested(" " + newValueDomainTitle + " ");

		verify(valueListServices, never()).createTaxonomy(newValueDomainTitle);
		verify(view, never()).refreshTable();
	}
}
