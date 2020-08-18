package com.constellio.app.services.sip.record;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.sip.mets.MetsDivisionInfo;
import com.constellio.app.services.sip.zip.SIPZipWriter;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordHierarchyServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.UserPhotosServices;

import java.io.IOException;
import java.util.Locale;

import static com.constellio.app.services.sip.record.DefaultRecordZipPathProvider.UKNOWN_USER;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class UnclassifiedDataSIPWriter {

	String collection;
	AppLayerFactory appLayerFactory;
	AppLayerCollectionExtensions extensions;
	SIPZipWriter writer;
	RecordSIPWriter recordSIPWriter;
	MetadataSchemaTypes types;
	Language language;
	SearchServices searchServices;
	RecordHierarchyServices recordHierarchyServices;
	TaxonomiesManager taxonomiesManager;
	ConfigManager configManager;
	IOServices ioServices;
	ProgressInfo progressInfo;
	UserPhotosServices userPhotosServices;
	ZipService zipService;

	public UnclassifiedDataSIPWriter(String collection, AppLayerFactory appLayerFactory, SIPZipWriter writer,
									 Locale locale, ProgressInfo progressInfo)
			throws IOException {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.extensions = appLayerFactory.getExtensions().forCollection(collection);
		this.writer = writer;
		this.types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		this.recordSIPWriter = new RecordSIPWriter(appLayerFactory, writer,
				new DefaultRecordZipPathProvider(appLayerFactory.getModelLayerFactory()), locale);

		this.recordSIPWriter.setIncludeRelatedMaterials(false);
		this.recordSIPWriter.setIncludeArchiveDescriptionMetadatasFromODDs(true);
		this.language = Language.withLocale(locale);
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		this.taxonomiesManager = appLayerFactory.getModelLayerFactory().getTaxonomiesManager();
		this.recordHierarchyServices = new RecordHierarchyServices(appLayerFactory.getModelLayerFactory());
		this.configManager = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getConfigManager();
		this.ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();
		this.progressInfo = progressInfo;
		this.userPhotosServices = appLayerFactory.getModelLayerFactory().newUserPhotosServices();

	}

	public KeySetMap<String, String> getExportedRecords() {
		return recordSIPWriter.getSavedRecords();
	}

	public UnclassifiedDataSIPWriter setIncludeContents(boolean includeContents) {
		this.recordSIPWriter.setIncludeContentFiles(includeContents);
		return this;
	}

	public void exportUnclassifiedData() throws IOException {

		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
		for (User user : schemas.getAllUsers()) {
			writer.addDivisionInfo(new MetsDivisionInfo("user-" + user.getId(), user.getTitle(), "user"));
		}

		writer.addDivisionInfo(new MetsDivisionInfo(UKNOWN_USER, UKNOWN_USER, "user"));

		if (!Collection.SYSTEM_COLLECTION.equals(collection)) {

			exportRecordsInSchemaTypesDivision(UserFolder.SCHEMA_TYPE);
			exportRecordsInSchemaTypesDivision(UserDocument.SCHEMA_TYPE);
			exportRecordsInSchemaTypesDivision(TemporaryRecord.SCHEMA_TYPE);
		}
	}


	public void close() {
		writer.close();
	}

	public void exportRecordsInSchemaTypesDivision(String schemaTypeCode)
			throws IOException {
		exportRecordsInSchemaTypesDivision(schemaTypeCode, true);
	}

	public void exportRecordsInSchemaTypesDivision(String schemaTypeCode, boolean createDivision)
			throws IOException {

		MetadataSchemaType schemaType = types.getSchemaType(schemaTypeCode);
		LogicalSearchQuery query = new LogicalSearchQuery(from(schemaType).returnAll());
		query.sortAsc(IDENTIFIER);

		SearchResponseIterator<Record> recordIterator = searchServices.recordsIterator(query, 1000);
		if (recordIterator.getNumFound() > 0) {
			progressInfo.setProgressMessage("Exporting '" + schemaTypeCode + "'");
			progressInfo.setEnd(recordIterator.getNumFound());

			int added = 0;
			progressInfo.setCurrentState(0);
			if (createDivision) {
				writer.addDivisionInfo(toDivisionInfo(schemaType, null));
			}
			while (recordIterator.hasNext()) {
				recordSIPWriter.add(recordIterator.next());
				progressInfo.setCurrentState(++added);
			}
		}

	}

	private MetsDivisionInfo toDivisionInfo(MetadataSchemaType schemaType, String parent) {
		return new MetsDivisionInfo(schemaType.getCode(), parent, schemaType.getLabel(language), "schemaType");
	}


}
