package com.constellio.app.services.sip.record;

import com.constellio.app.api.extensions.SIPExtension;
import com.constellio.app.api.extensions.params.ExportCollectionInfosSIPParams;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.sip.mets.MetsDivisionInfo;
import com.constellio.app.services.sip.zip.SIPZipWriter;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class CollectionInfosSIPWriter {

	String collection;
	AppLayerFactory appLayerFactory;
	AppLayerCollectionExtensions extensions;
	SIPZipWriter writer;
	RecordSIPWriter recordSIPWriter;
	MetadataSchemaTypes types;
	Language language;
	SearchServices searchServices;
	ConceptNodesTaxonomySearchServices taxonomySearchServices;
	TaxonomiesManager taxonomiesManager;

	public CollectionInfosSIPWriter(String collection, AppLayerFactory appLayerFactory, SIPZipWriter writer,
									Locale locale)
			throws IOException {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.extensions = appLayerFactory.getExtensions().forCollection(collection);
		this.writer = writer;
		this.types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		this.recordSIPWriter = new RecordSIPWriter(appLayerFactory, writer, new CollectionInfosRecordPathProvider(), locale);
		this.language = Language.withLocale(locale);
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		this.taxonomiesManager = appLayerFactory.getModelLayerFactory().getTaxonomiesManager();
		this.taxonomySearchServices = new ConceptNodesTaxonomySearchServices(appLayerFactory.getModelLayerFactory());
	}

	public void exportCollectionConfigs() throws IOException {

		exportValueLists();
		for (SIPExtension extension : extensions.sipExtensions) {
			extension.exportCollectionInfosSIP(new ExportCollectionInfosSIPParams(this, recordSIPWriter, writer));
		}
	}

	private void exportValueLists() throws IOException {
		for (MetadataSchemaType schemaType : types.getSchemaTypes()) {
			if (schemaType.getCode().startsWith("ddv")) {
				exportRecordsInSchemaTypesDivision(schemaType, null);
			}
		}
	}

	private void exportTaxonomies() throws IOException {


		for (Taxonomy taxonomy : taxonomiesManager.getEnabledTaxonomies(collection)) {
			writer.addDivisionInfo(new MetsDivisionInfo(taxonomy.getCode(), null, taxonomy.getTitle().get(language), "taxonomy"));
			if (extensions.isExportedTaxonomyInSIPCollectionInfos(taxonomy)) {
				TaxonomiesSearchOptions options = new TaxonomiesSearchOptions();
				LogicalSearchQuery query = taxonomySearchServices
						.getRootConceptsQuery(collection, taxonomy.getCode(), options);

				Iterator<Record> recordIterator = searchServices.recordsIterator(query);
				while (recordIterator.hasNext()) {
					Record record = recordIterator.next();
					
				}
			}
		}
	}

	public void exportRecordsInSchemaTypesDivision(MetadataSchemaType schemaType, String parentDivision)
			throws IOException {

		LogicalSearchQuery query = new LogicalSearchQuery(from(schemaType).returnAll());
		query.sortAsc(IDENTIFIER);

		writer.addDivisionInfo(toDivisionInfo(schemaType, parentDivision));
		Iterator<Record> recordIterator = searchServices.recordsIterator(query);
		while (recordIterator.hasNext()) {
			recordSIPWriter.add(recordIterator.next());
		}

	}

	private MetsDivisionInfo toDivisionInfo(MetadataSchemaType schemaType, String parent) {
		return new MetsDivisionInfo(schemaType.getCode(), parent, schemaType.getLabel(language), "schemaType");
	}

	private static class CollectionInfosRecordPathProvider implements RecordPathProvider {

		@Override
		public String getPath(Record record) {
			return record.getTypeCode();
		}
	}
}
