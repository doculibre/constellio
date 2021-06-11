package com.constellio.app.ui.acceptation.containers;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.LegalReference;
import com.constellio.app.modules.rm.wrappers.LegalRequirement;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.SearchResultTable;
import com.constellio.app.ui.framework.containers.SearchResultVOLazyContainer;
import com.constellio.app.ui.framework.data.SearchResultVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.SimpleSearchView;
import com.constellio.app.ui.pages.search.SimpleSearchViewImpl;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.SavedSearch.SortOrder;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;
import com.constellio.sdk.tests.vaadin.FakeVaadinEngine;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.data.dao.services.cache.InsertionReason.WAS_MODIFIED;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class SearchResultVOLazyContainerAcceptanceTest extends ConstellioTest {

	private static final String USR_TABLE_METADATA = "USRtableMetadata";

	Users users = new Users();
	SessionContext fakeAdminSession;
	MetadataSchemasManager schemasManager;
	SchemasDisplayManager displayManager;
	MetadataSchemaTypes types;

	@Before
	public void setUp() {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users));
		fakeAdminSession = FakeSessionContext.adminInCollection(zeCollection);
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		displayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		types = schemasManager.getSchemaTypes(zeCollection);
	}

	@Test
	public void whenDoingSimpleSearchThenTableMetadataAvailableForResultTableColumns() {
		addCustomTableMetadataToFolderAndDocument();

		executeSearch("abeille", schemaVOs -> {
			assertThat(schemaVOs).extracting("code")
					.containsOnly(Task.DEFAULT_SCHEMA,
							Folder.DEFAULT_SCHEMA,
							Document.DEFAULT_SCHEMA,
							LegalReference.SCHEMA_TYPE,
							LegalRequirement.SCHEMA_TYPE);

			assertThat(schemaVOs.stream().map(schemaVO -> schemaVO.getMetadatas())
					.flatMap(metadataVOs -> metadataVOs.stream().map(MetadataVO::getCode))
					.collect(Collectors.toList())
			).contains(Folder.DEFAULT_SCHEMA + "_" + USR_TABLE_METADATA, Document.DEFAULT_SCHEMA + "_" + USR_TABLE_METADATA);
		});
	}

	private void executeSearch(String freeTextSearch, Consumer<List<MetadataSchemaVO>> schemaVOs) {
		try (FakeVaadinEngine fakeVaadinEngine = new FakeVaadinEngine(FakeSessionContext.gandalfInCollection(zeCollection))) {
			SimpleSearchViewImpl searchView = new SimpleSearchViewImpl() {
				@Override
				protected SearchResultTable buildResultTable(SearchResultVODataProvider dataProvider) {
					final SearchResultVOLazyContainer container = new SearchResultVOLazyContainer(dataProvider);
					List<MetadataSchemaVO> schemas = container.getSchemas();
					schemaVOs.accept(schemas);

					return super.buildResultTable(dataProvider);
				}
			};

			SavedSearch search = createFakeSavedSearchForFreeTextSearch(fakeVaadinEngine, searchView, freeTextSearch);
			fakeVaadinEngine.show(searchView, "search", "s/" + search.getId());
		}
	}

	private SavedSearch createFakeSavedSearchForFreeTextSearch(FakeVaadinEngine fakeVaadinEngine,
															   SimpleSearchViewImpl searchView, String freeTextSearch) {
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		SavedSearch search = new SavedSearch(recordServices.newRecordWithSchema(types.getSchema(SavedSearch.DEFAULT_SCHEMA), freeTextSearch), types)
				.setTitle($("SearchView.savedSearch.temporarySimple"))
				.setUser(users.adminIn(zeCollection).getId())
				.setPublic(false)
				.setSortField(null)
				.setSortOrder(SortOrder.DESCENDING)
				.setSelectedFacets(Collections.EMPTY_MAP)
				.setTemporary(true)
				.setSearchType(SimpleSearchView.SEARCH_TYPE)
				.setFreeTextSearch(freeTextSearch)
				.setPageNumber(1)
				.setPageLength(0);
		((RecordImpl) search.getWrappedRecord()).markAsSaved(search.getVersion() + 1, search.getSchema());
		getModelLayerFactory().getRecordsCaches().getCache(zeCollection).insert(search.getWrappedRecord(), WAS_MODIFIED);

		return search;
	}

	private void addCustomTableMetadataToFolderAndDocument() {
		schemasManager.modify(zeCollection, (MetadataSchemaTypesAlteration) typesBuilder -> {
			for (String schemaTypeCode : asList(Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE)) {
				MetadataSchemaBuilder defaultSchema = typesBuilder.getDefaultSchema(schemaTypeCode);
				defaultSchema.create(USR_TABLE_METADATA).setType(STRING)
						.setSearchable(true).setAvailableInSummary(true);
			}
		});

		for (String schemaCode : asList(Folder.DEFAULT_SCHEMA, Document.DEFAULT_SCHEMA)) {
			displayManager.saveSchema(displayManager.getSchema(zeCollection, schemaCode)
					.withTableMetadataCodes(asList(USR_TABLE_METADATA)));
		}
	}

}
