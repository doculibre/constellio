package com.constellio.app.ui.pages.management.valueDomains;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;

import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.MetadataSchemasManager;
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
	@Mock MetadataSchemasManager metadataSchemasManager;
	@Mock MetadataSchemaTypes metadataSchemaTypes;
	@Mock UniqueIdGenerator uniqueIdGenerator;
	ListValueDomainPresenter presenter;
	String newValueFrenchDomainTitle;
	String newValueEnglishDomainTitle;
	MockedFactories mockedFactories = new MockedFactories();
	Map<Language, String> mapLangueTitle;

	@Before
	public void setUp()
			throws Exception {

		when(view.getConstellioFactories()).thenReturn(mockedFactories.getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.dakotaInCollection(zeCollection));
		when(view.navigateTo()).thenReturn(navigator);

		newValueFrenchDomainTitle = "domaine de valeur";
		newValueEnglishDomainTitle = "new value domain";

		mapLangueTitle = new HashMap<>();

		mapLangueTitle.put(Language.French, newValueFrenchDomainTitle);
		mapLangueTitle.put(Language.English, newValueEnglishDomainTitle);

		when(valueDomainType1.getLabel()).thenReturn(mapLangueTitle);

		presenter = spy(new ListValueDomainPresenter(view));

	}

	@Test
	public void whenValueDomainCreationRequestedThenCreateIt()
			throws Exception {

		doReturn(valueListServices).when(presenter).valueListServices();


		presenter.valueDomainCreationRequested(mapLangueTitle);

		verify(valueListServices).createValueDomain(mapLangueTitle, true);
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

		presenter.valueDomainCreationRequested(mapLangueTitle);

		verify(valueListServices, never()).createTaxonomy(mapLangueTitle, true);
		verify(view, never()).refreshTable();
	}

	@Test
	public void givenEmptyTitleWhenTaxonomyCreationRequestedThenDoNotCreateIt()
			throws Exception {

		presenter.valueDomainCreationRequested(new HashMap<Language, String>());

		verify(valueListServices, never()).createTaxonomy(mapLangueTitle, true);
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

		Map<Language, String> labelTitle1 = new HashMap<>();
		labelTitle1.put(Language.French, "taxo");

		presenter.valueDomainCreationRequested(mapLangueTitle);

		verify(valueListServices, never()).createTaxonomy(mapLangueTitle, true);
		verify(view, never()).refreshTable();
	}

	@Test
	public void whenDeterminingIfASchemaTypeIsPossiblyDeletableThenOnlyIfHasUSROrNumberCode() {
		assertThat(presenter.isValueListPossiblyDeletable("USRddvFolderType")).isTrue();
		assertThat(presenter.isValueListPossiblyDeletable("USRDDVFolderType")).isTrue();
		assertThat(presenter.isValueListPossiblyDeletable("ddvusrFolderType")).isTrue();
		assertThat(presenter.isValueListPossiblyDeletable("ddvUSRFolderType")).isTrue();
		assertThat(presenter.isValueListPossiblyDeletable("ddvFolderType")).isFalse();
		assertThat(presenter.isValueListPossiblyDeletable("ddvFolderZEUSRoy")).isFalse();
		assertThat(presenter.isValueListPossiblyDeletable("folder")).isFalse();
		assertThat(presenter.isValueListPossiblyDeletable("ddv12345")).isTrue();
		assertThat(presenter.isValueListPossiblyDeletable("ddv12345type")).isTrue();
		assertThat(presenter.isValueListPossiblyDeletable("ddvFolder12")).isFalse();
	}
}
