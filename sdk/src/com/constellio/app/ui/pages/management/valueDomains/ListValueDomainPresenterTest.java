package com.constellio.app.ui.pages.management.valueDomains;

import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaTypeToVOBuilder;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.validators.metadatas.IllegalCharactersValidator;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListValueDomainPresenterTest extends ConstellioTest {

	@Mock ListValueDomainViewImpl view;
	@Mock CoreViews navigator;
	@Mock ValueListServices valueListServices;
	@Mock MetadataSchemaType metadataSchemaType;
	@Mock MetadataSchemaTypeToVOBuilder metadataSchemaTypeToVOBuilder;
	@Mock MetadataSchemaTypeVO metadataSchemaTypeVO;
	@Mock MetadataSchemasManager metadataSchemasManager;
	@Mock MetadataSchemaTypes metadataSchemaTypes;
	@Mock UniqueIdGenerator uniqueIdGenerator;

	@Mock CollectionsListManager collectionsListManager;
	@Mock MetadataSchemaTypesBuilder metadataSchemaTypesBuilder;
	@Mock MetadataSchemaTypeBuilder metadataSchemaTypeBuilder;
	@Mock MetadataSchemaBuilder metadataSchemaBuilder;
	@Mock MetadataBuilder metadataBuilder;

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

		when(uniqueIdGenerator.next()).thenReturn("1").thenReturn("2");

		List<String> languages = new ArrayList<>();
		languages.add("en");
		languages.add("fr");

		when(mockedFactories.getModelLayerFactory().getCollectionsListManager()).thenReturn(collectionsListManager);
		when(collectionsListManager.getCollectionLanguages(anyString())).thenReturn(languages);


		newValueFrenchDomainTitle = "domaine de valeur";
		newValueEnglishDomainTitle = "new value domain";

		mapLangueTitle = new HashMap<>();

		mapLangueTitle.put(Language.French, newValueFrenchDomainTitle);
		mapLangueTitle.put(Language.English, newValueEnglishDomainTitle);

		when(metadataSchemaType.getLabel()).thenReturn(mapLangueTitle);

		presenter = spy(new ListValueDomainPresenter(view));

	}

	@Test
	public void whenValueDomainCreationRequestedThenCreateIt()
			throws Exception {

		doReturn(valueListServices).when(presenter).valueListServices();


		presenter.valueDomainCreationRequested(mapLangueTitle, true);

		verify(valueListServices).createValueDomain(mapLangueTitle, true);
		verify(view).refreshTable();
	}

	@Test
	public void givenExistentTitleWhenValueDomainCreationRequestedThenDoNotCreateIt()
			throws Exception {

		List<MetadataSchemaType> existentMetadataSchemaTypes = new ArrayList<>();
		existentMetadataSchemaTypes.add(metadataSchemaType);
		doReturn(valueListServices).when(presenter).valueListServices();
		doReturn(existentMetadataSchemaTypes).when(valueListServices).getValueDomainTypes();
		when(presenter.newMetadataSchemaTypeToVOBuilder()).thenReturn(metadataSchemaTypeToVOBuilder);
		when(metadataSchemaTypeToVOBuilder.build(metadataSchemaType)).thenReturn(metadataSchemaTypeVO);

		presenter.valueDomainCreationRequested(mapLangueTitle, true);

		verify(valueListServices, never()).createTaxonomy(mapLangueTitle, true);
		verify(view, never()).refreshTable();
	}

	@Test
	public void givenEmptyTitleWhenTaxonomyCreationRequestedThenDoNotCreateIt()
			throws Exception {

		when(mockedFactories.getModelLayerFactory().getMetadataSchemasManager()).thenReturn(metadataSchemasManager);
		when(metadataSchemasManager.getSchemaTypes(anyString())).thenReturn(metadataSchemaTypes);
		when(metadataSchemasManager.saveUpdateSchemaTypes((MetadataSchemaTypesBuilder) anyObject())).thenReturn(metadataSchemaTypes);
		when(mockedFactories.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(anyString()).getSchemaTypes()).thenReturn(new ArrayList<MetadataSchemaType>());
		when(mockedFactories.getModelLayerFactory().getDataLayerFactory().getUniqueIdGenerator()).thenReturn(uniqueIdGenerator);

		when(metadataSchemaTypesBuilder.createNewSchemaTypeWithSecurity(anyString())).thenReturn(metadataSchemaTypeBuilder);
		when(metadataSchemaTypeBuilder.getDefaultSchema()).thenReturn(metadataSchemaBuilder);

		when(metadataSchemaBuilder.getMetadata(anyString())).thenReturn(metadataBuilder);
		when(metadataSchemaTypeBuilder.getDefaultSchema().setLabels(anyMap())).thenReturn(metadataSchemaBuilder);
		when(metadataSchemaBuilder.getMetadata(anyString())).thenReturn(metadataBuilder);
		when(metadataBuilder.setUniqueValue(anyBoolean())).thenReturn(metadataBuilder);
		when(metadataBuilder.setDefaultRequirement(anyBoolean())).thenReturn(metadataBuilder);
		when(metadataBuilder.setMultiLingual(anyBoolean())).thenReturn(metadataBuilder);
		when(metadataSchemaBuilder.create(anyString())).thenReturn(metadataBuilder).thenReturn(metadataBuilder);
		when(metadataBuilder.setType((MetadataValueType) anyObject())).thenReturn(metadataBuilder);
		when(metadataBuilder.setSearchable(anyBoolean())).thenReturn(metadataBuilder);
		when(metadataBuilder.setUndeletable(anyBoolean())).thenReturn(metadataBuilder);
		when(metadataBuilder.setSchemaAutocomplete(anyBoolean())).thenReturn(metadataBuilder);
		when(metadataBuilder.setMultivalue(anyBoolean())).thenReturn(metadataBuilder);
		when(metadataBuilder.addValidator(IllegalCharactersValidator.class)).thenReturn(metadataBuilder);

		List languageList = new ArrayList();
		languageList.add(Language.French);
		languageList.add(Language.English);

		when(metadataSchemasManager.modify(anyString())).thenReturn(metadataSchemaTypesBuilder);
		when(metadataSchemaTypesBuilder.getLanguages()).thenReturn(languageList);

		presenter.valueDomainCreationRequested(new HashMap<Language, String>(), false);

		verify(valueListServices, never()).createTaxonomy(mapLangueTitle, true);
		verify(view, Mockito.times(1)).refreshTable();
	}

	@Test
	public void givenExistentTitleWithSpacesWhenValueDomainCreationRequestedThenDoNotCreateIt()
			throws Exception {

		List<MetadataSchemaType> existentMetadataSchemaTypes = new ArrayList<>();
		existentMetadataSchemaTypes.add(metadataSchemaType);
		doReturn(valueListServices).when(presenter).valueListServices();
		doReturn(existentMetadataSchemaTypes).when(valueListServices).getValueDomainTypes();
		when(presenter.newMetadataSchemaTypeToVOBuilder()).thenReturn(metadataSchemaTypeToVOBuilder);
		when(metadataSchemaTypeToVOBuilder.build(metadataSchemaType)).thenReturn(metadataSchemaTypeVO);

		Map<Language, String> labelTitle1 = new HashMap<>();
		labelTitle1.put(Language.French, "taxo");

		presenter.valueDomainCreationRequested(mapLangueTitle, true);

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
