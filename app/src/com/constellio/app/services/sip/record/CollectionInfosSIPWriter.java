package com.constellio.app.services.sip.record;

import com.constellio.app.api.extensions.SIPExtension;
import com.constellio.app.api.extensions.params.ExportCollectionInfosSIPParams;
import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.importExport.settings.SettingsExportOptions;
import com.constellio.app.services.importExport.settings.SettingsExportServices;
import com.constellio.app.services.importExport.settings.model.ImportedSettings;
import com.constellio.app.services.importExport.settings.utils.SettingsXMLFileWriter;
import com.constellio.app.services.sip.mets.MetsDivisionInfo;
import com.constellio.app.services.sip.zip.SIPZipWriter;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.UserPhotosServices;
import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Locale;

import static com.constellio.app.services.schemasDisplay.SchemasDisplayManager.SCHEMAS_DISPLAY_CONFIG;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.search.SearchBoostManager.SEARCH_BOOST_CONFIG;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.security.roles.RolesManager.ROLES_CONFIG;
import static java.util.Objects.requireNonNull;

public class CollectionInfosSIPWriter {

	public static final String READ_EMAIL_TEMPLATE_RESOURCE_NAME = "CollectionInfosSIPWriter-ReadEmailTemplate";

	public static final String EXPORT_FOLDER_AS_ZIP_TEMP_FILE_RESOURCE_NAME = "CollectionInfosSIPWriter-ExportFolderAsZipTempFile";
	public static final String EXPORT_FOLDER_AS_ZIP_TEMP_FILE_INPUTSTREAM_RESOURCE_NAME = "CollectionInfosSIPWriter-ExportFolderAsZipTempFileInputStream";

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
	ConfigManager configManager;
	IOServices ioServices;
	ProgressInfo progressInfo;
	UserPhotosServices userPhotosServices;
	ZipService zipService;

	public CollectionInfosSIPWriter(String collection, AppLayerFactory appLayerFactory, SIPZipWriter writer,
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
		this.taxonomySearchServices = new ConceptNodesTaxonomySearchServices(appLayerFactory.getModelLayerFactory());
		this.configManager = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getConfigManager();
		this.ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();
		this.progressInfo = progressInfo;
		this.userPhotosServices = appLayerFactory.getModelLayerFactory().newUserPhotosServices();
		this.zipService = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newZipService();

	}

	public KeySetMap<String, String> getExportedRecords() {
		return recordSIPWriter.getSavedRecords();
	}

	public CollectionInfosSIPWriter setIncludeContents(boolean includeContents) {
		this.recordSIPWriter.setIncludeContentFiles(includeContents);
		return this;
	}

	public void exportCollectionConfigs() throws IOException {

		if (Collection.SYSTEM_COLLECTION.equals(collection)) {
			exportRecordsInSchemaTypesDivision(Collection.SCHEMA_TYPE);
			exportRecordsInSchemaTypesDivision(UserCredential.SCHEMA_TYPE);
			exportRecordsInSchemaTypesDivision(GlobalGroup.SCHEMA_TYPE);
			exportRecordsInSchemaTypesDivision(EmailToSend.SCHEMA_TYPE);
			exportSystemSettings();

		} else {

			exportRecordsInSchemaTypesDivision(Collection.SCHEMA_TYPE);
			exportRecordsInSchemaTypesDivision(Capsule.SCHEMA_TYPE);
			exportRecordsInSchemaTypesDivision(User.SCHEMA_TYPE);
			exportRecordsInSchemaTypesDivision(Group.SCHEMA_TYPE);
			exportRecordsInSchemaTypesDivision(Printable.SCHEMA_TYPE);
			exportRecordsInSchemaTypesDivision(Report.SCHEMA_TYPE);
			exportRecordsInSchemaTypesDivision(SavedSearch.SCHEMA_TYPE);
			exportRecordsInSchemaTypesDivision(Facet.SCHEMA_TYPE);

			exportValueLists();
			exportTaxonomies();
			for (SIPExtension extension : extensions.sipExtensions) {
				extension.exportCollectionInfosSIP(new ExportCollectionInfosSIPParams(this, recordSIPWriter, writer));
			}

			exportCollectionSettings();
		}
	}


	protected void exportSystemSettings() throws IOException {
		SettingsExportServices settingsExportServices = new SettingsExportServices(appLayerFactory);
		SettingsExportOptions options = new SettingsExportOptions();
		options.setExportingConfigs(true);
		try {
			ImportedSettings settings = settingsExportServices.exportSettings(collection, options);
			Document document = new SettingsXMLFileWriter().writeSettings(settings);

			writeJDomDocument(document, "/data/collectionSettings/settings.xml");

		} catch (ValidationException e) {
			throw new RuntimeException(e);
		}

		try {
			exportFolderZip("/data/photos.zip", userPhotosServices.getPhotosFolder());
			exportFolderZip("/data/userLogs.zip", userPhotosServices.getUserLogsFolder());
		} catch (ZipServiceException e) {
			throw new RuntimeException(e);
		}

		exportConfig("/data/collectionSettings/schemasDisplay.xml", SCHEMAS_DISPLAY_CONFIG);
		exportConfig("/data/collectionSettings/roles.xml", ROLES_CONFIG);
		exportConfig("/data/collectionSettings/searchBoosts.xml", SEARCH_BOOST_CONFIG);
	}

	protected void exportCollectionSettings() throws IOException {
		SettingsExportServices settingsExportServices = new SettingsExportServices(appLayerFactory);
		SettingsExportOptions options = new SettingsExportOptions();
		options.setExportingConfigs(true);
		try {
			ImportedSettings settings = settingsExportServices.exportSettings(collection, options);
			Document document = new SettingsXMLFileWriter().writeSettings(settings);

			writeJDomDocument(document, "/data/collectionSettings/settings.xml");

		} catch (ValidationException e) {
			throw new RuntimeException(e);
		}

		exportConfig("/data/collectionSettings/schemasDisplay.xml", SCHEMAS_DISPLAY_CONFIG);
		exportConfig("/data/collectionSettings/roles.xml", ROLES_CONFIG);
		exportConfig("/data/collectionSettings/searchBoosts.xml", SEARCH_BOOST_CONFIG);
		exportEmailTemplates();
	}


	private void exportConfig(String sipPath, String partialConfigPath) throws IOException {
		Document document = configManager.getXML("/" + collection + partialConfigPath).getDocument();
		writeJDomDocument(document, sipPath);

	}


	private void exportFolderZip(String sipPath, File folder) throws IOException, ZipServiceException {

		if (folder.exists() && folder.listFiles() != null) {

			File tempFile = ioServices.newTemporaryFile(EXPORT_FOLDER_AS_ZIP_TEMP_FILE_RESOURCE_NAME);
			OutputStream outputStream = null;
			InputStream inputStream = null;
			try {

				zipService.zip(tempFile, Arrays.asList(requireNonNull(folder.listFiles())));

				inputStream = ioServices.newBufferedFileInputStream(tempFile, EXPORT_FOLDER_AS_ZIP_TEMP_FILE_INPUTSTREAM_RESOURCE_NAME);
				outputStream = writer.newZipFileOutputStream(sipPath);
				ioServices.copy(inputStream, outputStream);

			} finally {
				IOUtils.closeQuietly(inputStream);
				IOUtils.closeQuietly(outputStream);
				ioServices.deleteQuietly(tempFile);
			}
		}

	}


	private void exportEmailTemplates() throws IOException {
		File configFolder = appLayerFactory.getModelLayerFactory().getDataLayerFactory()
				.getDataLayerConfiguration().getSettingsFileSystemBaseFolder();
		if (configFolder != null && configFolder.exists()) {

			File collectionConfigFolder = new File(configFolder, collection);
			File emailTemplatesFolder = new File(collectionConfigFolder, "emailTemplates");

			File[] emailTemplates = emailTemplatesFolder.listFiles();
			if (emailTemplates != null) {
				for (File emailTemplate : emailTemplates) {
					String emailTemplateSipPath = "/data/collectionSettings/emailTemplates/" + emailTemplate.getName() + ".html";
					InputStream inputStream = null;
					OutputStream outputStream = null;

					try {
						inputStream = ioServices.newBufferedFileInputStream(emailTemplate, READ_EMAIL_TEMPLATE_RESOURCE_NAME);
						outputStream = writer.newZipFileOutputStream(emailTemplateSipPath);
						ioServices.copy(inputStream, outputStream);

					} finally {
						ioServices.closeQuietly(inputStream);
						ioServices.closeQuietly(outputStream);
					}
				}
			}
		}

	}

	private void writeJDomDocument(Document document, String sipPath) throws IOException {
		XMLOutputter xmlOutput = new XMLOutputter();
		xmlOutput.setFormat(Format.getPrettyFormat());

		OutputStream outputStream = writer.newZipFileOutputStream(sipPath);
		try {
			xmlOutput.output(document, outputStream);

		} finally {
			IOUtils.closeQuietly(outputStream);
		}
	}

	public void close() {
		writer.close();
	}

	private void exportValueLists() throws IOException {
		writer.addDivisionInfo(new MetsDivisionInfo("valueLists", "Value lists", "schemaTypes"));
		for (MetadataSchemaType schemaType : types.getSchemaTypes()) {
			if (schemaType.getCode().startsWith("ddv")) {
				writer.addDivisionInfo(toDivisionInfo(schemaType, "valueLists"));
				exportRecordsInSchemaTypesDivision(schemaType.getCode(), false);
			}
		}
	}

	private void exportTaxonomies() throws IOException {

		for (Taxonomy taxonomy : taxonomiesManager.getEnabledTaxonomies(collection)) {
			if (extensions.isExportedTaxonomyInSIPCollectionInfos(taxonomy)) {
				writer.addDivisionInfo(new MetsDivisionInfo(taxonomy.getCode(), taxonomy.getTitle(language), "taxonomy"));


				for (String taxonomySchemaTypeCode : taxonomy.getSchemaTypes()) {
					exportRecordsInSchemaTypesDivision(taxonomySchemaTypeCode, false);
				}
			}
		}
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
