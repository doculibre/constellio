package com.constellio.app.services.schemasDisplay;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationRuntimeException;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerListener;
import com.constellio.model.utils.OneXMLConfigPerCollectionManager;
import com.constellio.model.utils.OneXMLConfigPerCollectionManagerListener;
import com.constellio.model.utils.XMLConfigReader;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

public class SchemasDisplayManager
		implements OneXMLConfigPerCollectionManagerListener<SchemasDisplayManagerCache>, StatefulService {

	public static final String REQUIRED_METADATA_IN_FORM_LIST = "requiredMetadataInFormList";

	public static final String SCHEMAS_DISPLAY_CONFIG = "/schemasDisplay.xml";

	private ConfigManager configManager;

	private CollectionsListManager collectionsListManager;

	private ConstellioCacheManager cacheManager;

	private OneXMLConfigPerCollectionManager<SchemasDisplayManagerCache> oneXMLConfigPerCollectionManager;

	private MetadataSchemasManager metadataSchemasManager;

	public SchemasDisplayManager(ConfigManager configManager, CollectionsListManager collectionsListManager,
								 MetadataSchemasManager metadataSchemasManager, ConstellioCacheManager cacheManager) {
		this.configManager = configManager;
		this.collectionsListManager = collectionsListManager;
		this.metadataSchemasManager = metadataSchemasManager;
		this.cacheManager = cacheManager;
	}

	public Set<String> getReturnedFieldsForSearch(String collection) {
		return getCacheForCollection(collection).getReturnedFieldsForSearch(metadataSchemasManager);
	}

	public Set<String> getReturnedFieldsForTable(String collection) {
		return getCacheForCollection(collection).getReturnedFieldsForTable(metadataSchemasManager);
	}

	public void execute(final SchemaDisplayManagerTransaction transaction) {
		validate(transaction);
		final String collection = transaction.getCollection();
		if (collection != null) {
			oneXMLConfigPerCollectionManager.updateXML(collection, new DocumentAlteration() {
				@Override
				public void alter(Document document) {
					SchemasDisplayWriter writer = newSchemasDisplayWriter(document);
					if (transaction.getModifiedCollectionTypes() != null) {
						writer.saveTypes(transaction.getModifiedCollectionTypes());
					}

					for (SchemaTypeDisplayConfig typeDisplayConfig : transaction.getModifiedTypes()) {
						writer.saveType(typeDisplayConfig);
					}

					for (SchemaDisplayConfig schemaDisplayConfig : transaction.getModifiedSchemas()) {
						if (!schemaDisplayConfig.getSchemaCode().endsWith("_default")) {
							writer.saveSchema(schemaDisplayConfig);

						} else {
							SchemaDisplayConfig defaultSchemaDisplayConfig = getCacheForCollection(collection)
									.getSchemaDefaultDisplay(schemaDisplayConfig.getSchemaCode(), metadataSchemasManager);

							if (defaultSchemaDisplayConfig.equals(schemaDisplayConfig)) {
								writer.resetSchema(schemaDisplayConfig.getSchemaCode());
							} else {
								writer.saveSchema(schemaDisplayConfig);
							}
						}
					}

					for (MetadataDisplayConfig metadataDisplayConfig : transaction.getModifiedMetadatas()) {
						if (!metadataDisplayConfig.getMetadataCode().contains("_default_")) {
							writer.saveMetadata(metadataDisplayConfig);

						} else {
							MetadataDisplayConfig defaultMetadataDisplayConfig = getCacheForCollection(collection)
									.getMetadata(metadataDisplayConfig.getMetadataCode(), metadataSchemasManager);

							if (defaultMetadataDisplayConfig.equals(metadataDisplayConfig)) {
								writer.resetSchema(metadataDisplayConfig.getMetadataCode());
							} else {
								writer.saveMetadata(metadataDisplayConfig);
							}
						}

						writer.saveMetadata(metadataDisplayConfig);
					}
				}
			});
		}

	}

	private void validate(SchemaDisplayManagerTransaction transaction) {

		ValidationErrors errors = new ValidationErrors();

		for (SchemaDisplayConfig config : transaction.getModifiedSchemas()) {
			validate(errors, config);
		}

		if (!errors.getValidationErrors().isEmpty()) {
			throw new ValidationRuntimeException(errors);
		}
	}

	private void validate(ValidationErrors errors, SchemaDisplayConfig config) {
		MetadataSchema schema = metadataSchemasManager.getSchemaTypes(config.getCollection()).getSchema(config.getSchemaCode());
		for (Metadata metadata : SchemaDisplayUtils.getRequiredMetadatasInSchemaForm(schema)) {
			if (!config.getFormMetadataCodes().contains(metadata.getCode())
				&& (!metadata.isEssential() || metadata.getDefaultValue() == null)) {
				Map<String, Object> params = new HashMap<>();
				params.put("code", metadata.getCode());
				params.put("label", metadata.getLabelsByLanguageCodes());
				errors.add(SchemasDisplayManager.class, REQUIRED_METADATA_IN_FORM_LIST, params);
			}
		}
	}

	public void saveTypes(final SchemaTypesDisplayConfig config) {
		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
		transaction.setModifiedCollectionTypes(config);
		execute(transaction);
	}

	public void saveType(final SchemaTypeDisplayConfig config) {
		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
		transaction.add(config);
		execute(transaction);
	}

	public void saveSchema(final SchemaDisplayConfig config) {
		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
		transaction.add(config);
		execute(transaction);
	}

	public void saveMetadata(final MetadataDisplayConfig config) {
		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
		transaction.add(config);
		execute(transaction);
	}

	public SchemasDisplayManagerCache getCacheForCollection(String collection) {
		return oneXMLConfigPerCollectionManager.get(collection);
	}

	public SchemaTypesDisplayConfig getTypes(String collection) {
		return getCacheForCollection(collection).getTypes();
	}

	public SchemaTypeDisplayConfig getType(String collection, String typeCode) {
		if (typeCode.split("_").length != 1) {
			throw new RuntimeException("Invalid code : " + typeCode);
		}
		return getCacheForCollection(collection).getType(typeCode);
	}

	public SchemaDisplayConfig getSchema(String collection, String schemaCode) {
		if (schemaCode.split("_").length != 2) {
			throw new RuntimeException("Invalid code : " + schemaCode);
		}
		return getCacheForCollection(collection).getSchema(schemaCode, metadataSchemasManager);
	}

	public MetadataDisplayConfig getMetadata(String collection, String schemaCode, String metadataLocalCode) {
		return getMetadata(collection, schemaCode + "_" + metadataLocalCode);
	}

	public SchemaDisplayConfig getDisplayConfig(MetadataSchema schema) {
		return getSchema(schema.getCollection(), schema.getCode());
	}

	public MetadataDisplayConfig getDisplayConfig(Metadata metadata) {
		return getMetadata(metadata.getCollection(), metadata.getCode());
	}

	public MetadataDisplayConfig getMetadata(String collection, String metadataCode) {
		//		if (metadataCode.split("_").length != 3) {
		//			throw new RuntimeException("Invalid code : " + metadataCode);
		//		}
		return getCacheForCollection(collection).getMetadata(metadataCode, metadataSchemasManager);
	}

	public List<SchemaTypeDisplayConfig> getSimpleSearchSchemaTypeConfigs(String collection) {
		List<SchemaTypeDisplayConfig> simpleSearchSchemaTypes = new ArrayList<>();

		for (MetadataSchemaType type : metadataSchemasManager.getSchemaTypes(collection).getSchemaTypes()) {
			SchemaTypeDisplayConfig typeDisplayConfig = getType(collection, type.getCode());
			if (typeDisplayConfig.isSimpleSearch()) {
				simpleSearchSchemaTypes.add(typeDisplayConfig);
			}
		}

		return simpleSearchSchemaTypes;
	}

	public List<SchemaTypeDisplayConfig> getAdvancedSearchSchemaTypeConfigs(String collection) {
		List<SchemaTypeDisplayConfig> simpleSearchSchemaTypes = new ArrayList<>();

		for (MetadataSchemaType type : metadataSchemasManager.getSchemaTypes(collection).getSchemaTypes()) {
			SchemaTypeDisplayConfig typeDisplayConfig = getType(collection, type.getCode());
			if (typeDisplayConfig.isAdvancedSearch()) {
				simpleSearchSchemaTypes.add(typeDisplayConfig);
			}
		}

		return simpleSearchSchemaTypes;
	}

	public List<MetadataDisplayConfig> getAdvancedSearchMetadatas(String collection, String type) {
		List<MetadataDisplayConfig> metadataDisplayConfigs = new ArrayList<>();

		for (Metadata metadata : metadataSchemasManager.getSchemaTypes(collection).getSchemaType(type).getAllMetadatas()) {
			MetadataDisplayConfig metadataDisplayConfig = getMetadata(collection, metadata.getCode());
			if (metadataDisplayConfig.isVisibleInAdvancedSearch()) {
				metadataDisplayConfigs.add(metadataDisplayConfig);
			}
		}

		return metadataDisplayConfigs;
	}

	@Override
	public void initialize() {
		ConstellioCache cache = cacheManager.getCache(SchemasDisplayManager.class.getName());
		this.oneXMLConfigPerCollectionManager = new OneXMLConfigPerCollectionManager<>(configManager, collectionsListManager,
				SCHEMAS_DISPLAY_CONFIG, xmlConfigReader(), this, new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				SchemasDisplayWriter writer = newSchemasDisplayWriter(document);
				writer.writeEmptyDocument();
			}
		}, cache);
		metadataSchemasManager.registerListener(new MetadataSchemasManagerListener() {
			@Override
			public void onCollectionSchemasModified(String collection) {
				oneXMLConfigPerCollectionManager.reload(collection, InsertionReason.WAS_MODIFIED);
			}
		});
	}

	private XMLConfigReader<SchemasDisplayManagerCache> xmlConfigReader() {
		return new XMLConfigReader<SchemasDisplayManagerCache>() {
			@Override
			public SchemasDisplayManagerCache read(String collection, Document document) {

				Element rootElement = document.getRootElement();
				String formatVersion =
						rootElement == null ? null : rootElement.getAttributeValue(SchemasDisplayWriter.FORMAT_ATTRIBUTE);

				MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
				List<Language> languages = Language
						.withCodes(collectionsListManager.getCollectionLanguages(types.getCollection()));
				if (formatVersion == null) {
					SchemasDisplayReader1 reader = new SchemasDisplayReader1(document, types, languages);
					return reader.readSchemaTypesDisplay(collection);
				} else if (SchemasDisplayReader2.FORMAT_VERSION.equals(formatVersion)) {
					SchemasDisplayReader2 reader = new SchemasDisplayReader2(document, types, languages);
					return reader.readSchemaTypesDisplay(collection);
				} else {
					throw new ImpossibleRuntimeException("Invalid format version '" + formatVersion + "'");
				}
			}
		};
	}

	@Override
	public void close() {
		//Nothing to do
	}

	@Override
	public void onValueModified(String collection, SchemasDisplayManagerCache newValue) {

	}

	private SchemasDisplayWriter newSchemasDisplayWriter(Document document) {
		return new SchemasDisplayWriter(document);
	}

	public void enableAllMetadatasInAdvancedSearch(String collection, String schemaType) {

		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();

		SchemaTypeDisplayConfig schemaTypeDisplayConfig = getType(collection, schemaType);
		if (!schemaTypeDisplayConfig.isAdvancedSearch()) {
			transaction.getModifiedTypes().add(schemaTypeDisplayConfig.withAdvancedSearchStatus(true));
		}

		transaction.getModifiedTypes().add(getType(collection, schemaType).withAdvancedSearchStatus(true));
		List<MetadataValueType> restrictedTypes = asList(MetadataValueType.CONTENT, MetadataValueType.STRUCTURE);
		for (Metadata metadata : metadataSchemasManager.getSchemaTypes(collection).getSchemaType(schemaType).getAllMetadatas()) {
			if ("id".equals(metadata.getLocalCode()) || "path".equals(metadata.getLocalCode()) || (
					!metadata.getCode().toLowerCase().contains("entered")
					&& !restrictedTypes
							.contains(metadata.getType()) && !metadata
							.isSystemReserved())) {
				transaction.getModifiedMetadatas().add(
						getMetadata(collection, metadata.getCode()).withVisibleInAdvancedSearchStatus(true));
			}
		}

		execute(transaction);
	}

	public void enableMetadatasInAdvancedSearch(String collection, String schemaType, String... metadatas) {

		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();

		SchemaTypeDisplayConfig schemaTypeDisplayConfig = getType(collection, schemaType);
		if (!schemaTypeDisplayConfig.isAdvancedSearch()) {
			transaction.getModifiedTypes().add(schemaTypeDisplayConfig.withAdvancedSearchStatus(true));
		}

		List<String> metadatasToEnable = asList(metadatas);
		transaction.getModifiedTypes().add(getType(collection, schemaType).withAdvancedSearchStatus(true));
		for (Metadata metadata : metadataSchemasManager.getSchemaTypes(collection).getSchemaType(schemaType).getAllMetadatas()) {
			if (metadatasToEnable.contains(metadata.getLocalCode()) || metadatasToEnable.contains(metadata.getCode())) {
				transaction.getModifiedMetadatas().add(
						getMetadata(collection, metadata.getCode()).withVisibleInAdvancedSearchStatus(true));
			}
		}

		execute(transaction);
	}

	public SchemaTypesDisplayTransactionBuilder newTransactionBuilderFor(String collection) {
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
		return new SchemaTypesDisplayTransactionBuilder(types, this);
	}

	public void resetSchema(String collection, final String code) {
		oneXMLConfigPerCollectionManager.updateXML(collection, new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				SchemasDisplayWriter writer = newSchemasDisplayWriter(document);
				writer.resetSchema(code);
			}
		});
	}

	public void resetMetadata(String collection, final String code) {
		oneXMLConfigPerCollectionManager.updateXML(collection, new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				SchemasDisplayWriter writer = newSchemasDisplayWriter(document);
				writer.resetMetadata(code);
			}
		});
	}

	public List<MetadataSchemaType> getAllowedSchemaTypesForSimpleSearch(String collection) {
		List<MetadataSchemaType> result = new ArrayList<>();
		for (MetadataSchemaType type : metadataSchemasManager.getSchemaTypes(collection).getSchemaTypes()) {
			SchemaTypeDisplayConfig config = getType(collection, type.getCode());
			if (config.isSimpleSearch()) {
				result.add(type);
			}
		}
		return result;
	}

	public List<String> getDefinedMetadatasIn(String collection) {
		List<String> definedMetadatas = new ArrayList<>();
		XMLConfiguration initialConfig = configManager.getXML(oneXMLConfigPerCollectionManager.getConfigPath(collection));
		Document document = initialConfig.getDocument();
		Element element = document.getRootElement();
		Element metadataDisplayConfigs = element.getChild("MetadataDisplayConfigs");
		if (metadataDisplayConfigs != null) {
			for (Element metadataElement : metadataDisplayConfigs.getChildren()) {
				definedMetadatas.add(metadataElement.getName());
			}
		}

		return definedMetadatas;
	}

	public List<String> rewriteInOrderAndGetCodes(final String collection) {

		XMLConfiguration initialConfig = configManager.getXML(oneXMLConfigPerCollectionManager.getConfigPath(collection));

		Document document = initialConfig.getDocument();
		Element rootElement = document.detachRootElement();
		sort(rootElement);
		Document newDocument = new Document(rootElement);

		try {
			oneXMLConfigPerCollectionManager.update(collection, initialConfig.getHash(), newDocument);
		} catch (OptimisticLockingConfiguration optimisticLockingConfiguration) {
			throw new RuntimeException(optimisticLockingConfiguration);
		}

		return getCodesOfElements(newDocument);

	}

	public List<String> getCodesOfElements(Document newDocument) {
		List<String> codes = new ArrayList<>();
		getCodesOfElements(newDocument.getRootElement(), codes);
		return codes;
	}

	private void getCodesOfElements(Element element, List<String> codes) {
		String value = element.getAttributeValue("code");

		if (value == null) {
			value = element.getAttributeValue("SchemaCode");
		}

		if (value == null) {
			value = element.getName();
		}

		codes.add(value);

		if (!asList("SchemaTypesDisplayConfig", "DisplayMetadataCodes", "FormMetadataCodes", "SearchResultsMetadataCodes",
				"TableMetadataCodes")
				.contains(element.getName())) {
			for (Element child : element.getChildren()) {
				getCodesOfElements(child, codes);
			}
		}

	}

	private void sort(Element element) {

		if (!asList("SchemaTypesDisplayConfig", "DisplayMetadataCodes", "FormMetadataCodes", "SearchResultsMetadataCodes",
				"TableMetadataCodes").contains(element.getName())) {

			List<Element> children = new ArrayList<Element>(element.getChildren());
			element.removeContent();

			Comparator<Element> comparator = new Comparator<Element>() {
				public int compare(Element o1, Element o2) {
					String n1 = o1.getAttributeValue("code");
					String n2 = o2.getAttributeValue("code");

					if (n1 == null) {
						n1 = o1.getAttributeValue("SchemaCode");
					}

					if (n2 == null) {
						n2 = o2.getAttributeValue("SchemaCode");
					}

					if (n1 == null) {
						n1 = o1.getName();
					}

					if (n2 == null) {
						n2 = o2.getName();
					}

					return n1.compareTo(n2);
				}
			};
			Collections.sort(children, comparator);
			for (Element child : children) {
				sort(child);
			}
			element.addContent(children);
		}
	}

}
