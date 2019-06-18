package com.constellio.model.services.taxonomies;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.data.dao.services.cache.ConstellioCacheOptions;
import com.constellio.data.dao.services.cache.serialization.SerializationCheckCache;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager.TaxonomiesManagerCache;
import com.constellio.model.utils.OneXMLConfigPerCollectionManager;
import com.constellio.sdk.tests.ConstellioTest;
import org.jdom2.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class TaxonomiesManagerTest extends ConstellioTest {

	static String TAXONOMIES_CONFIG = "/taxonomies.xml";
	@Mock BatchProcessesManager batchProcessesManager;
	@Mock CollectionsListManager collectionsListManager;
	@Mock MetadataSchemasManager schemasManager;
	@Mock ConfigManager configManager;
	@Mock ConstellioEIMConfigs eimConfigs;
	@Mock ConstellioCacheManager cacheManager;
	@Mock SearchServices searchServices;
	@Mock TaxonomiesWriter writer;
	@Mock TaxonomiesReader reader;
	@Mock DocumentAlteration addDocumentAlteration;
	@Mock DocumentAlteration enableDocumentAlteration;
	@Mock DocumentAlteration disableDocumentAlteration;
	@Mock XMLConfiguration xmlConfiguration;
	@Mock Document document;
	@Mock Taxonomy taxonomy2;
	@Mock RecordsCaches caches;
	@Mock MetadataSchemaTypes metadataSchemaTypes;
	@Mock MetadataSchemaType type1;
	@Mock MetadataSchema schema;
	@Mock OneXMLConfigPerCollectionManager<TaxonomiesManagerCache> oneXMLConfigPerCollectionManager;
	InOrder inOrder;

	TaxonomiesManager taxonomiesManager;
	ArrayList<String> metadataRelations;
	Map<Language, String> labelTitle;

	ConstellioCache zeCache;

	@Before
	public void setup()
			throws Exception {

		List<String> listString = new ArrayList<>();
		listString.add(Language.French.getCode());
		zeCache = new SerializationCheckCache("zeCache", new ConstellioCacheOptions());
		when(cacheManager.getCache(anyString())).thenReturn(zeCache);
		when(collectionsListManager.getCollections()).thenReturn(Arrays.asList(zeCollection));
		taxonomiesManager = spy(
				new TaxonomiesManager(configManager, searchServices, batchProcessesManager, collectionsListManager,
						cacheManager, eimConfigs));
		doReturn(oneXMLConfigPerCollectionManager).when(taxonomiesManager).newOneXMLConfigPerCollectionManager();
		taxonomiesManager.initialize();

		TaxonomiesManagerCache cache = new TaxonomiesManagerCache(null, new ArrayList<Taxonomy>(), new ArrayList<Taxonomy>());
		when(oneXMLConfigPerCollectionManager.get(zeCollection)).thenReturn(cache);
		when(configManager.getXML(TAXONOMIES_CONFIG)).thenReturn(xmlConfiguration);
		when(xmlConfiguration.getDocument()).thenReturn(document);
		when(taxonomiesManager.newTaxonomyWriter(any(Document.class))).thenReturn(writer);
		when(collectionsListManager.getCollectionLanguages(zeCollection)).thenReturn(listString);
		when(taxonomiesManager.newTaxonomyReader(document, collectionsListManager.getCollectionLanguages(zeCollection)))
				.thenReturn(reader);
		when(taxonomiesManager.newAddTaxonomyDocumentAlteration(any(Taxonomy.class))).thenReturn(addDocumentAlteration);
		when(taxonomiesManager.newEnableTaxonomyDocumentAlteration(anyString())).thenReturn(enableDocumentAlteration);
		when(taxonomiesManager.newDisableTaxonomyDocumentAlteration(anyString())).thenReturn(disableDocumentAlteration);
		when(schemasManager.getSchemaTypes(zeCollection)).thenReturn(metadataSchemaTypes);
		when(metadataSchemaTypes.getSchemaType("type1")).thenReturn(type1);
		when(type1.getDefaultSchema()).thenReturn(schema);
		when(schema.getCollection()).thenReturn(zeCollection);
		inOrder = inOrder(configManager, taxonomiesManager, reader, writer, oneXMLConfigPerCollectionManager);

		labelTitle = new HashMap<>();
		labelTitle.put(Language.French, "1");
	}

	@Test
	public void whenAddTaxonomyThenRightMethodsAreCalled()
			throws Exception {

		Taxonomy taxonomy = Taxonomy.createPublic("1", labelTitle, zeCollection, Arrays.asList("type1"));
		addTaxonomy(taxonomy);

		inOrder.verify(taxonomiesManager).canCreateTaxonomy(taxonomy, schemasManager);
		inOrder.verify(oneXMLConfigPerCollectionManager).updateXML(zeCollection, addDocumentAlteration);
	}

	@Test
	public void whenDisableAndGetDisableTaxonomiesThenItIsReturned()
			throws Exception {

		Taxonomy taxonomy = Taxonomy.createPublic("1", labelTitle, zeCollection, Arrays.asList("type1"));
		addTaxonomy(taxonomy);

		taxonomiesManager.disable(taxonomy, schemasManager);

		inOrder.verify(taxonomiesManager).verifyRecordsWithTaxonomiesSchemaTypes(taxonomy, schemasManager);
		inOrder.verify(oneXMLConfigPerCollectionManager).updateXML(zeCollection, disableDocumentAlteration);
	}

	@Test
	public void whenEnableTaxonomieThenItIsEnabled()
			throws Exception {

		Taxonomy taxonomy = Taxonomy.createPublic("1", labelTitle, zeCollection, Arrays.asList("type1"));
		addTaxonomy(taxonomy);
		taxonomiesManager.disable(taxonomy, schemasManager);

		taxonomiesManager.enable(taxonomy, schemasManager);

		inOrder.verify(taxonomiesManager).verifyRecordsWithTaxonomiesSchemaTypes(taxonomy, schemasManager);
		inOrder.verify(oneXMLConfigPerCollectionManager).updateXML(zeCollection, enableDocumentAlteration);
	}

	private void addTaxonomy(Taxonomy taxonomy) {
		taxonomiesManager.addTaxonomy(taxonomy, schemasManager);
	}

}
