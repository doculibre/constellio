package com.constellio.app.extensions.schemas;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ParsedContentProvider;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.populators.SearchFieldsPopulator;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Patrick on 2015-11-19.
 */
public class CoreSearchFieldExtensionAcceptTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	SessionContext sessionContext;
	RMSchemasRecordsServices rm;
	RecordServices recordServices;
	MetadataDisplayConfig descriptionDisplayConfig;
	Metadata descriptionMetadata;
	private final String DESCRIPTION_WITH_XML_TAGS = "<p>Supprimer les <strong>éléphants</strong> jaunes</p>";
	private final String DESCRIPTION_WITHOUT_XML_TAGS = "Supprimer les éléphants jaunes";

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records).withFoldersAndContainersOfEveryStatus()
		);

		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		descriptionDisplayConfig = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.getMetadata(zeCollection, Folder.DEFAULT_SCHEMA + "_" + Folder.DESCRIPTION);
		descriptionMetadata = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getMetadata(Folder.DEFAULT_SCHEMA + "_" + Folder.DESCRIPTION);

		recordServices = getModelLayerFactory().newRecordServices();

		sessionContext = FakeSessionContext.chuckNorrisInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
	}

	@Test
	public void givenTextIsNotWithInputTypeRichTextThanKeepsTagInSearchFieldPopulator()
			throws Exception {

		getAppLayerFactory().getMetadataSchemasDisplayManager().saveMetadata(descriptionDisplayConfig.withInputType(MetadataInputType.TEXTAREA));

		Folder folder = records.getFolder_A01().setDescription(DESCRIPTION_WITH_XML_TAGS);
		recordServices.update(folder);

		SearchFieldsPopulator searchFieldsPopulator = getSearchFieldPopulator();
		Map<String, Object> stringObjectMap = searchFieldsPopulator.populateCopyfields(descriptionMetadata, DESCRIPTION_WITH_XML_TAGS);
		assertThat(stringObjectMap).contains(MapEntry.entry("description_t_fr", DESCRIPTION_WITH_XML_TAGS));

//		List<String> foldersFound = getModelLayerFactory().newSearchServices().searchRecordIds(new LogicalSearchQuery(from(rm.folderSchemaType()).returnAll()).setFreeTextQuery("éléphants"));
//		assertThat(foldersFound).doesNotContain(records.folder_A01);
	}

	@Test
	public void givenTextIsWithInputTypeRichTextThanKeepsTagInSearchFieldPopulator()
			throws Exception {

		getAppLayerFactory().getMetadataSchemasDisplayManager().saveMetadata(descriptionDisplayConfig.withInputType(MetadataInputType.RICHTEXT));

		Folder folder = records.getFolder_A01().setDescription(DESCRIPTION_WITH_XML_TAGS);
		recordServices.update(folder);

		SearchFieldsPopulator searchFieldsPopulator = getSearchFieldPopulator();
		Map<String, Object> stringObjectMap = searchFieldsPopulator.populateCopyfields(descriptionMetadata, DESCRIPTION_WITH_XML_TAGS);
		assertThat(stringObjectMap).contains(MapEntry.entry("description_t_fr", DESCRIPTION_WITHOUT_XML_TAGS));

		List<String> foldersFound = getModelLayerFactory().newSearchServices().searchRecordIds(new LogicalSearchQuery(from(rm.folderSchemaType()).returnAll()).setFreeTextQuery("éléphants"));
		assertThat(foldersFound).contains(records.folder_A01);
	}

	private SearchFieldsPopulator getSearchFieldPopulator() {
		ContentManager contentManager = getModelLayerFactory().getContentManager();
		List<String> collectionLanguages = getModelLayerFactory().getCollectionsListManager().getCollectionLanguages(zeCollection);
		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		ConstellioEIMConfigs systemConfigs = getModelLayerFactory().getSystemConfigs();
		ParsedContentProvider parsedContentProvider = new ParsedContentProvider(contentManager,
				new HashMap<String, ParsedContent>());
		return new SearchFieldsPopulator(types, false, parsedContentProvider,
				collectionLanguages, systemConfigs, getModelLayerFactory().getExtensions());
	}
}
