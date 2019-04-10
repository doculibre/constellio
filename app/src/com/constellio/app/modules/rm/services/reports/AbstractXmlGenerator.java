package com.constellio.app.modules.rm.services.reports;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleInRule;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.reports.parameters.AbstractXmlGeneratorParameters;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.SimpleDateFormatSingleton;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Verifier;
import org.jsoup.Jsoup;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public abstract class AbstractXmlGenerator {
	/**
	 * First element of the XMl document, usually in plural form.
	 */
	public static final String XML_ROOT_RECORD_ELEMENTS = "records";

	/**
	 * Xml tag of every element in the XML
	 */
	public static final String XML_EACH_RECORD_ELEMENTS = "record";

	/**
	 * Prefix for reference
	 */
	public static final String REFERENCE_PREFIX = "ref_";

	/**
	 * Suffix for reference of parents
	 */
	public static final String PARENT_SUFFIX = "_parent";

	/**
	 * XML Tag for multiple Metadata
	 */
	public static final String XML_METADATA_TAGS = "metadatas";

	/**
	 * XMl tag for single metadata
	 */
	public static final String XML_METADATA_SINGLE_TAGS = "metadata";

	public static LangUtils.StringReplacer replaceInvalidXMLCharacter = LangUtils.replacingRegex("[\\( \\)]", "")
			.replacingRegex("[&$%]", "");
	public static LangUtils.StringReplacer replaceBracketsInValueToString = LangUtils.replacingRegex("[\\[\\]]", "");

	/**
	 * Constellio's AppLayerFactory
	 */
	private AppLayerFactory factory;

	/**
	 * Constellio's Collection
	 */
	private String collection;

	private RecordServices recordServices;


	private Locale locale;

	private MetadataSchemasManager metadataSchemasManager;
	protected SchemasDisplayManager displayManager;
	private RMSchemasRecordsServices rm;

	protected AbstractXmlGeneratorParameters xmlGeneratorParameters;

	public AbstractXmlGenerator(AppLayerFactory appLayerFactory, String collection, Locale locale) {
		this.factory = appLayerFactory;
		this.collection = collection;
		this.recordServices = this.factory.getModelLayerFactory().newRecordServices();
		this.metadataSchemasManager = this.factory.getModelLayerFactory().getMetadataSchemasManager();
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
	}

	public RecordServices getRecordServices() {
		return recordServices;
	}

	public RMSchemasRecordsServices getRMSchemasRecordsServices() {
		return rm;
	}

	public AppLayerFactory getFactory() {
		return this.factory;
	}

	public String getCollection() {
		return this.collection;
	}

	protected MetadataList getListOfMetadataForElement(Record element) {
		return this.factory.getModelLayerFactory().getMetadataSchemasManager().getSchemaOf(element).getMetadatas();
	}

	/**
	 * Add additionnal needed information like collection code and title
	 * there should be an extension here to add information from plugins.
	 *
	 * @param recordElement record
	 * @return list of elements
	 */
	protected List<Element> getAdditionalInformations(Record recordElement) {
		MetadataSchemaType metadataSchemaType = getFactory().getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypeOf(recordElement);
		List<Element> elementsToAdd = new ArrayList<>(asList(
				new Element("collection_code").setText(getCollection()).setAttribute("label", "Code de collection")
						.setAttribute("code", "collection_code"),
				new Element("collection_title")
						.setText(getFactory().getCollectionsManager().getCollection(getCollection()).getName())
						.setAttribute("label", "Titre de collection").setAttribute("code", "collection_title")
		));
		if (metadataSchemaType.getCode().equals(Folder.SCHEMA_TYPE)) {
			Metadata typeMetadata = metadataSchemaType.getDefaultSchema().getMetadata(Folder.TYPE);
			String metadataTypeId = recordElement.get(typeMetadata);
			if (metadataTypeId != null) {
				FolderType currentFolderType = new RMSchemasRecordsServices(getCollection(), getFactory())
						.getFolderType(metadataTypeId);
				elementsToAdd.addAll(asList(
						new Element("ref_folder_type_title").setText(currentFolderType.getTitle())
								.setAttribute("label", metadataSchemaType.getFrenchLabel())
								.setAttribute("code", typeMetadata.getCode()),
						new Element("ref_folder_type_code").setText(currentFolderType.getCode())
								.setAttribute("label", metadataSchemaType.getFrenchLabel())
								.setAttribute("code", typeMetadata.getCode())
				));
			}
		}
		List<Element> listOfSpecificAdditionnalElement = getSpecificDataToAddForCurrentElement(recordElement);
		if (!listOfSpecificAdditionnalElement.isEmpty()) {
			elementsToAdd.addAll(listOfSpecificAdditionnalElement);
		}
		return elementsToAdd;
	}

	/**
	 * Return the elements created from the value of metadata of type structure.
	 *
	 * @param metadata
	 * @param recordElement
	 * @param collection
	 * @param factory
	 * @return
	 */
	public static List<Element> createMetadataTagFromMetadataOfTypeStructure(Metadata metadata, Record recordElement,
																			 String collection,
																			 AppLayerFactory factory) {
		if (!hasMetadata(recordElement, metadata)) {
			return Collections.emptyList();
		}
		List<Element> listOfMetadataTags = new ArrayList<>();
		if (metadata.getLocalCode().toLowerCase().contains("copyrule")) {
			List<ModifiableStructure> metadataValue;
			if (metadata.isMultivalue()) {
				metadataValue = recordElement.getList(metadata);
			} else {
				metadataValue = Collections.singletonList(recordElement.<ModifiableStructure>get(metadata));
			}

			for (ModifiableStructure value : metadataValue) {
				CopyRetentionRule retentionRule = value instanceof CopyRetentionRuleInRule ?
												  ((CopyRetentionRuleInRule) value).getCopyRetentionRule() :
												  (CopyRetentionRule) value;
				listOfMetadataTags.addAll(asList(
						new Element(escapeForXmlTag(getLabelOfMetadata(metadata))).setText(retentionRule.toString()),
						new Element(REFERENCE_PREFIX + metadata.getLocalCode() + "_active_period")
								.setText(retentionRule.getActiveRetentionPeriod().toString()),
						new Element(REFERENCE_PREFIX + metadata.getLocalCode() + "_active_period_comment")
								.setText(retentionRule.getActiveRetentionComment()),
						new Element(REFERENCE_PREFIX + metadata.getLocalCode() + "_semi_active_period")
								.setText(retentionRule.getSemiActiveRetentionPeriod().toString()),
						new Element(REFERENCE_PREFIX + metadata.getLocalCode() + "_semi_active_period_comment")
								.setText(retentionRule.getSemiActiveRetentionComment())
				));
			}
		} else if (metadata.getLocalCode().equals("comments")) {
			List<Comment> comments = recordElement.getList(metadata);
			StringBuilder commentsText = new StringBuilder();
			for (Comment comment : comments) {
				commentsText.append(comment.getMessage()).append("\n");
			}
			listOfMetadataTags.add(new Element(metadata.getLocalCode()).setText(commentsText.toString()));
		} else {
			Object metadataValue = recordElement.get(metadata);
			listOfMetadataTags.add(new Element(metadata.getLocalCode()).setText(
					defaultFormatData(metadataValue != null ? metadataValue.toString() : null, metadata, factory, collection)));
		}
		return listOfMetadataTags;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public static List<Element> createMetadataTagFromMetadataOfTypeEnum(Metadata metadata, Record recordElement,
																		Locale locale) {
		return createMetadataTagFromMetadataOfTypeEnum(metadata, recordElement, null, locale);
	}

	/**
	 * Method that returns the value of metadata enum,
	 * use the i18n to get correct label for current user language.
	 *
	 * @param metadata
	 * @param recordElement
	 * @param namespace
	 * @return
	 */
	public static List<Element> createMetadataTagFromMetadataOfTypeEnum(Metadata metadata, Record recordElement,
																		Namespace namespace, Locale locale) {
		if (!hasMetadata(recordElement, metadata)) {
			return Collections.emptyList();
		}
		List<Element> listOfMetadataTags = new ArrayList<>();

		Object valueAsObject = recordElement.get(metadata, locale);

		String code, title;

		//TODO test;
		if (metadata.isMultivalue()) {
			StringBuilder codeBuilder = new StringBuilder();
			StringBuilder titleBuilder = new StringBuilder();
			for (EnumWithSmallCode enumWithSmallCode : recordElement.<EnumWithSmallCode>getList(metadata)) {
				if (codeBuilder.length() > 0) {
					codeBuilder.append(", ");
				}
				codeBuilder.append(enumWithSmallCode.getCode());

				if (titleBuilder.length() > 0) {
					titleBuilder.append(", ");
				}
				titleBuilder.append($(enumWithSmallCode));
			}
			code = codeBuilder.toString();
			title = titleBuilder.toString();
		} else if (valueAsObject == null) {
			code = null;
			title = null;
		} else {
			EnumWithSmallCode metadataValue = (EnumWithSmallCode) valueAsObject;
			code = metadataValue.getCode();
			title = $(metadataValue);
		}
		listOfMetadataTags.addAll(asList(
				new Element(escapeForXmlTag(getLabelOfMetadata(metadata)) + "_code", namespace).setText(code)
						.setAttribute("label", metadata.getFrenchLabel())
						.setAttribute("code", escapeForXmlTag(getLabelOfMetadata(metadata)) + "_code", namespace),
				new Element(escapeForXmlTag(getLabelOfMetadata(metadata)) + "_title", namespace).setText(title)
						.setAttribute("label", metadata.getFrenchLabel())
						.setAttribute("code", escapeForXmlTag(getLabelOfMetadata(metadata)) + "_title", namespace)
				)
		);
		return listOfMetadataTags;
	}

	/**
	 * Method that returns the value of metadata enum,
	 * use the i18n to get correct label for current user language.
	 *
	 * @param metadata
	 * @param recordElement
	 * @param namespace
	 * @return
	 */
	public static List<Element> createMetadataTagFromMetadataOfTypeContent(Metadata metadata, Record recordElement,
																		   Namespace namespace, Locale locale) {
		if (!hasMetadata(recordElement, metadata)) {
			return Collections.emptyList();
		}
		List<Element> listOfMetadataTags = new ArrayList<>();

		for (Content content : recordElement.<Content>getValues(metadata)) {
			listOfMetadataTags.add(new Element(escapeForXmlTag(getLabelOfMetadata(metadata)), namespace).setText(new ContentFactory().toString(content)));
		}
		return listOfMetadataTags;
	}

	public static List<Element> createMetadataTagFromMetadataOfTypeReference(Metadata metadata, Record recordElement,
																			 String collection,
																			 AppLayerFactory factory) {
		return createMetadataTagFromMetadataOfTypeReference(metadata, recordElement, collection, factory, null);
	}

	/**
	 * Method that will return the code and title of the referenced record element,
	 * If referenced record has parent, will return the code and title of the parent.
	 *
	 * @param metadata      metadata
	 * @param recordElement record
	 * @param collection    collection
	 * @param factory       factory
	 * @param namespace     use if need to add a namespace.
	 * @return list of element to add
	 */
	public static List<Element> createMetadataTagFromMetadataOfTypeReference(Metadata metadata, Record recordElement,
																			 String collection, AppLayerFactory factory,
																			 Namespace namespace) {
		if (!hasMetadata(recordElement, metadata)) {
			return Collections.emptyList();
		}
		RecordServices recordServices = factory.getModelLayerFactory().newRecordServices();
		MetadataSchemasManager metadataSchemasManager = factory.getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchema recordSchema = metadataSchemasManager.getSchemaOf(recordElement);
		List<String> listOfIdsReferencedByMetadata = metadata.isMultivalue() ?
													 recordElement.<String>getList(metadata) :
													 Collections.singletonList(recordElement.<String>get(metadata));
		List<Record> listOfRecordReferencedByMetadata = recordServices.getRecordsById(collection, listOfIdsReferencedByMetadata);
		List<Element> listOfMetadataTags = new ArrayList<>();
		Metadata metadataInSchema = recordSchema.getMetadata(metadata.getLocalCode());
		String inheritedMetadataCode = metadataInSchema.getCode();
		if (metadataInSchema.inheritDefaultSchema()) {
			inheritedMetadataCode = metadataInSchema.getInheritance().getCode();
		}
		if (listOfRecordReferencedByMetadata.isEmpty()) {
			String elementNamePrefix = REFERENCE_PREFIX + inheritedMetadataCode.replace("_default_", "_");
			listOfMetadataTags = asList(
					new Element(elementNamePrefix + "_code", namespace).setText(null)
							.setAttribute("label", metadata.getFrenchLabel()).setAttribute("code", elementNamePrefix + "_code"),
					new Element(elementNamePrefix + "_title", namespace).setText(null)
							.setAttribute("label", metadata.getFrenchLabel()).setAttribute("code", elementNamePrefix + "_title"));
		} else {
			StringBuilder titleBuilder = new StringBuilder();
			StringBuilder codeBuilder = new StringBuilder();
			StringBuilder titleParentBuilder = new StringBuilder();
			StringBuilder codeParentBuilder = new StringBuilder();
			for (Record recordReferenced : listOfRecordReferencedByMetadata) {
				if (titleBuilder.length() > 0) {
					titleBuilder.append(", ");
				}
				titleBuilder.append((String) recordReferenced.get(Schemas.TITLE));

				if (codeBuilder.length() > 0) {
					codeBuilder.append(", ");
				}
				codeBuilder.append((String) recordReferenced.get(Schemas.CODE));

				if (AdministrativeUnit.SCHEMA_TYPE.equals(recordReferenced.getTypeCode()) || Category.SCHEMA_TYPE
						.equals(recordReferenced.getTypeCode())) {
					Metadata parentMetadata = AdministrativeUnit.SCHEMA_TYPE.equals(recordReferenced.getTypeCode()) ?
											  metadataSchemasManager.getSchemaTypeOf(recordReferenced).getDefaultSchema()
													  .get(AdministrativeUnit.PARENT) :
											  metadataSchemasManager.getSchemaTypeOf(recordReferenced).getDefaultSchema().get(Category.PARENT);
					String parentMetadataId = recordReferenced.get(parentMetadata);
					if (parentMetadataId != null) {
						Record parentRecord = recordServices.getDocumentById(parentMetadataId);
						if (titleParentBuilder.length() > 0) {
							titleParentBuilder.append(", ");
						}
						titleParentBuilder.append((String) parentRecord.get(Schemas.TITLE));

						if (codeParentBuilder.length() > 0) {
							codeParentBuilder.append(", ");
						}
						codeParentBuilder.append((String) parentRecord.get(Schemas.CODE));

					}
				}
			}

			String elementNamePrefix = REFERENCE_PREFIX + inheritedMetadataCode.replace("_default_", "_");
			listOfMetadataTags.addAll(asList(
					new Element(elementNamePrefix + "_code", namespace).setText(codeBuilder.toString())
							.setAttribute("label", elementNamePrefix + "_code"),
					new Element(elementNamePrefix + "_title", namespace).setText(titleBuilder.toString())
							.setAttribute("label", elementNamePrefix + "_title")
			));
			if (codeParentBuilder.length() > 0 && titleParentBuilder.length() > 0) {
				listOfMetadataTags.addAll(asList(
						new Element(elementNamePrefix + PARENT_SUFFIX + "_code", namespace).setText(codeParentBuilder.toString()),
						new Element(elementNamePrefix + PARENT_SUFFIX + "_title", namespace)
								.setText(titleParentBuilder.toString())
				));
			}
		}
		return listOfMetadataTags;
	}

	/**
	 * Function that checks a string and replace if needed. Used to get valid XML tag.
	 *
	 * @param input String
	 * @return
	 */
	public static String escapeForXmlTag(String input) {
		String inputWithoutaccent = AccentApostropheCleaner.removeAccents(input);
		String inputWithoutPonctuation = AccentApostropheCleaner.cleanPonctuation(inputWithoutaccent);
		return replaceInvalidXMLCharacter.replaceOn(inputWithoutPonctuation).toLowerCase();
	}

	/**
	 * Format data to make sure it's conform.
	 * ex: Will format date for the defined pattern in constellio config.
	 * Will also returns correct i18n value for boolean.
	 * And will remove html tags from text.
	 *
	 * @param data
	 * @param metadata
	 * @param factory
	 * @param collection
	 * @return
	 */
	public static String defaultFormatData(String data, Metadata metadata, AppLayerFactory factory, String collection) {
		if (StringUtils.isEmpty(data)) {
			return data;
		}
		String finalData = data;
		ConstellioEIMConfigs configs = new ConstellioEIMConfigs(factory.getModelLayerFactory().getSystemConfigurationsManager());
		if (metadata.getType().equals(MetadataValueType.BOOLEAN)) {
			finalData = $(data);
		} else if (metadata.getType().equals(MetadataValueType.DATE) || metadata.getType().equals(MetadataValueType.DATE_TIME)) {
			try {
				boolean isDateTime = metadata.getType().equals(MetadataValueType.DATE_TIME);
				DateFormat df = isDateTime ?
								new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS") :
								new SimpleDateFormat("yyyy-MM-dd");
				Date date = df.parse(data);
				finalData = SimpleDateFormatSingleton
						.getSimpleDateFormat(isDateTime ? configs.getDateTimeFormat() : configs.getDateFormat()).format(date);
			} catch (ParseException e) {
				return data;
			}

		} else if (metadata.getType().equals(MetadataValueType.TEXT)) {
			finalData = Jsoup.parse(data).text();
		}
		return replaceBracketsInValueToString.replaceOn(finalData);
	}

	public static String getLabelOfMetadata(Metadata metadata) {
		return metadata.getCode().split("_")[2];
	}

	protected String getToStringOrNull(Object ob) {
		return ob == null ? null : ob.toString();
	}

	protected String getToStringOrNullInString(Object ob) {
		return ob == null ? "null" : ob.toString();
	}

	/**
	 * Method that returns a XML String.
	 * Should be override by children.
	 *
	 * @return
	 */
	public abstract String generateXML();

	/**
	 * Method that returns a list of element to add from the record.
	 * Usually used for data that isn't from a metadata.
	 *
	 * @param recordElement record
	 * @return list of element to add.
	 */
	protected abstract List<Element> getSpecificDataToAddForCurrentElement(Record recordElement);

	protected String formatData(String data, Metadata metadata) {
		String formattedData = defaultFormatData(data, metadata, getFactory(), getCollection());
		return formatToXml(formattedData);
	}

	private String formatToXml(String stringToFormat) {
		if (stringToFormat != null) {
			StringBuilder stringBuilder = new StringBuilder();
			char[] chars = stringToFormat.toCharArray();
			for (char currentChar : chars) {
				if (Verifier.isXMLCharacter(currentChar)) {
					stringBuilder.append(currentChar);
				} else {
					stringBuilder.append(" ");
				}
			}
			return stringBuilder.toString();
		} else {
			return null;
		}
	}

	/**
	 * Method that format a path to make it better
	 * ex:  folder1 > folder2 > folder3 > document
	 *
	 * @param recordElement
	 * @return
	 */
	public String getPath(Record recordElement) {
		StringBuilder builder = new StringBuilder();
		String parentId = recordElement.getParentId();
		if (parentId == null) {
			if (recordElement.getTypeCode().equals(Folder.SCHEMA_TYPE)) {
				parentId = getRMSchemasRecordsServices().wrapFolder(recordElement).getCategory();
			} else if (recordElement.getTypeCode().equals(com.constellio.app.modules.rm.wrappers.Document.SCHEMA_TYPE)) {
				parentId = getRMSchemasRecordsServices().wrapDocument(recordElement).getFolder();
			}
		}
		if (parentId != null) {
			builder.append(this.getParentPath(getRecordServices().getDocumentById(parentId)));
		}
		builder.append(recordElement.getTitle());
		return builder.toString();
	}

	private String getParentPath(Record recordElement) {
		StringBuilder builder = new StringBuilder();
		String parentId = null;
		if (recordElement.getTypeCode().equals(Folder.SCHEMA_TYPE)) {
			Folder folder = getRMSchemasRecordsServices().wrapFolder(recordElement);
			parentId = folder.getParentFolder();
			if (parentId == null) {
				parentId = folder.getCategory();
			}
		} else if (recordElement.getTypeCode().equals(Category.SCHEMA_TYPE)) {
			Category category = getRMSchemasRecordsServices().wrapCategory(recordElement);
			parentId = category.getParent();
		}
		if (parentId != null) {
			builder.append(this.getParentPath(getRecordServices().getDocumentById(parentId)));
		}
		builder.append(recordElement.getTitle());
		builder.append(" > ");
		return builder.toString();
	}

	/**
	 * Method that returns the xml generator parameters
	 *
	 * @return AbstractXmlGeneratorParameters parameters
	 */
	public abstract AbstractXmlGeneratorParameters getXmlGeneratorParameters();

	/**
	 * Method that returns whether or not a record has a particular metadata
	 *
	 * @param record   record
	 * @param metadata metadata
	 * @return boolean
	 */
	private static boolean hasMetadata(Record record, Metadata metadata) {
		try {
			record.get(metadata);
			return true;
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
			return false;
		}
	}
}
