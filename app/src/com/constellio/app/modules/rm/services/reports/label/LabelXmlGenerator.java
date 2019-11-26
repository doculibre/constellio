package com.constellio.app.modules.rm.services.reports.label;

import com.constellio.app.api.extensions.params.AddFieldsInLabelXMLParams;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.services.reports.AbstractXmlGenerator;
import com.constellio.app.modules.rm.services.reports.parameters.AbstractXmlGeneratorParameters;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.apache.commons.lang.StringUtils;
import org.jdom2.CDATA;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

/**
 * Class that creates the XML for the labels.
 */
public class LabelXmlGenerator extends AbstractXmlGenerator {

	public static final String REFERENCE_PREFIX = "ref_";
	public static final String PARENT_SUFFIX = "_parent";
	private static final Map<String, String> SCHEMA_CODE_MAP = new HashMap<String, String>() {{
		put(ContainerRecord.SCHEMA_TYPE, "containers");
		put(Folder.SCHEMA_TYPE, "folders");
		put(com.constellio.app.modules.rm.wrappers.Document.SCHEMA_TYPE, "documents");
	}};

	public static LangUtils.StringReplacer replaceInvalidXMLCharacter = LangUtils.replacingRegex("[\\( \\)]", "")
			.replacingRegex("[&$%]", "");
	public static LangUtils.StringReplacer replaceBracketsInValueToString = LangUtils.replacingRegex("[\\[\\]]", "");

	private Record[] recordElements;

	private String collection;

	private AppLayerFactory factory;

	private RecordServices recordServices;

	private MetadataSchemasManager metadataSchemasManager;

	private int startingPosition, numberOfCopies = 1;

	private String type;

	private User user;

	public LabelXmlGenerator(String collection, AppLayerFactory appLayerFactory, Locale locale, UserVO userVO) {
		super(appLayerFactory, collection, locale);
		this.collection = collection;
		this.factory = appLayerFactory;
		this.recordServices = factory.getModelLayerFactory().newRecordServices();
		this.metadataSchemasManager = factory.getModelLayerFactory().getMetadataSchemasManager();
		this.user = userVO == null ? null :
					appLayerFactory.getModelLayerFactory().newUserServices().getUserInCollection(userVO.getUsername(), collection);
	}

	public LabelXmlGenerator(String collection, AppLayerFactory appLayerFactory, Locale locale, UserVO userVO,
							 Record... recordElements) {
		this(collection, appLayerFactory, locale, userVO);
		this.setElements(recordElements);
	}

	public LabelXmlGenerator(String collection, AppLayerFactory appLayerFactory, Locale locale, int startingPosition,
							 int numberOfCopies, UserVO userVO,
							 Record... recordElements) {
		this(collection, appLayerFactory, locale, userVO, recordElements);
		this.startingPosition = startingPosition;
		this.numberOfCopies = numberOfCopies;
	}

	public Record[] getElements() {
		return this.recordElements;
	}

	public LabelXmlGenerator setElements(Record... elements) {
		this.recordElements = elements;
		this.setTypeWithElements(this.recordElements[0]);
		return this;
	}

	public int getStartingPosition() {
		return startingPosition;
	}

	public LabelXmlGenerator setStartingPosition(int startingPosition) {
		this.startingPosition = startingPosition;
		return this;
	}

	public int getNumberOfCopies() {
		return numberOfCopies;
	}

	public LabelXmlGenerator setNumberOfCopies(int numberOfCopies) {
		this.numberOfCopies = numberOfCopies;
		return this;
	}

	public String getCollection() {
		return this.collection;
	}

	public AppLayerFactory getFactory() {
		return this.factory;
	}

	@Override
	public String generateXML() {
		try {
			validateInputs();
			Document xmlDocument = new Document();
			Element xmlRoot = new Element(this.type);
			xmlDocument.setRootElement(xmlRoot);
			xmlRoot.addContent(this.skipAmountOfElementNeededForStartingPosition());
			factory.getExtensions().forCollection(collection).orderListOfElements(recordElements);
			for (Record recordElement : recordElements) {
				for (int i = 0; i < this.numberOfCopies; i++) {
					//the xml tag singular ( folder / container )
					Element xmlSingularElement = new Element(getTypeSingular());

					//the xml tag metadatas
					Element XMLMetadatasOfSingularElement = new Element("metadatas");

					//Add the Extensions to the metadatas elements.
					factory.getExtensions().forCollection(collection).addFieldsInLabelXML(new AddFieldsInLabelXMLParams(
							recordElement, xmlSingularElement, XMLMetadatasOfSingularElement));

					// List of all metadatas of current RecordElement
					MetadataList listOfMetadataOfRecordElement = getListOfMetadataForElement(recordElement);
					if (user != null) {
						listOfMetadataOfRecordElement = listOfMetadataOfRecordElement.onlyAccessibleOnRecordBy(user, recordElement);
					}

					//Add the additional informations to the metadatas.
					XMLMetadatasOfSingularElement.addContent(getAdditionalInformations(recordElement));

					//Iterate through metadatas
					for (Metadata metadata : listOfMetadataOfRecordElement) {
						//Create the metadata tags.
						List<Element> metadataTags = createMetadataTagsFromMetadata(metadata, recordElement);
						if (xmlGeneratorParameters != null && this.xmlGeneratorParameters.isForTest()) {
							metadataTags = fillEmptyTags(metadataTags);
						}
						//add them to the childrens.
						XMLMetadatasOfSingularElement.addContent(metadataTags);
					}

					xmlSingularElement.setContent(XMLMetadatasOfSingularElement);
					xmlRoot.addContent(xmlSingularElement);
				}
			}
			return new XMLOutputter(Format.getPrettyFormat()).outputString(xmlDocument);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method that will fill the empty tags to make sure JasperSoft correctly read them.
	 *
	 * @param originalElements element to check if empty
	 * @return
	 */
	private List<Element> fillEmptyTags(List<Element> originalElements) {
		List<Element> filledElements = new ArrayList<>();
		for (Element element : originalElements) {
			if (element.getText().isEmpty()) {
				element.setText($("PrintableReport.noData"));
			}
			filledElements.add(element);
		}
		return filledElements;
	}

	/**
	 * Function that checks a string and replace if needed. Used to get valid XML tag.
	 *
	 * @param input String
	 * @return the xml tag filtered
	 */
	public static String escapeForXmlTag(String input) {
		String inputWithoutaccent = AccentApostropheCleaner.removeAccents(input);
		String inputWithoutPonctuation = AccentApostropheCleaner.cleanPonctuation(inputWithoutaccent);
		return replaceInvalidXMLCharacter.replaceOn(inputWithoutPonctuation).toLowerCase();
	}

	private void validateInputs()
			throws Exception {
		if (StringUtils.isBlank(this.collection)) {
			throw new Exception("Collection must not be null or empty");
		}

		if (this.factory == null) {
			throw new Exception("Factory must not be null");
		}

		if (this.recordElements == null || this.recordElements.length == 0) {
			throw new Exception("Elements must not be null or empty");
		}

		if (this.numberOfCopies <= 0) {
			throw new Exception("Number of copy must not be equals or smaller to 0");
		}

		if (this.startingPosition < 0) {
			throw new Exception("Starting position must not be inferior to 0");
		}
	}

	private List<Element> skipAmountOfElementNeededForStartingPosition() {
		List<Element> XMLelementList = new ArrayList<>();
		for (int i = 1; i < this.startingPosition; i++) {
			XMLelementList.add(new Element(this.getTypeSingular()).setContent(new Element("metadatas")));
		}
		return XMLelementList;
	}

	private String getTypeSingular() {
		return this.type.substring(0, this.type.length() - 1);
	}

	protected MetadataList getListOfMetadataForElement(Record element) {
		return this.factory.getModelLayerFactory().getMetadataSchemasManager().getSchemaOf(element).getMetadatas();
		//return element.getSchema().getMetadatas();
	}

	private List<Element> createMetadataTagsFromMetadata(Metadata metadata, Record recordElement) {
		if (metadata.getType().equals(MetadataValueType.REFERENCE)) {
			return createMetadataTagFromMetadataOfTypeReference(metadata, recordElement);
		}

		if (metadata.getType().equals(MetadataValueType.ENUM)) {
			return createMetadataTagFromMetadataOfTypeEnum(metadata, recordElement);
		}

		Element metadataXmlElement = new Element(escapeForXmlTag(getLabelOfMetadata(metadata)));
		boolean isRichTextInputType = displayManager.getMetadata(getCollection(), metadata.getCode()).getInputType() == MetadataInputType.RICHTEXT;
		String data = getToStringOrNull(recordElement.get(metadata, getLocale()));
		if (!isRichTextInputType) {
			data = formatData(data, metadata);
		}
		if (metadata.isMultivalue()) {
			StringBuilder valueBuilder = new StringBuilder();
			List<Object> objects = recordElement.getList(metadata);
			for (Object ob : objects) {
				if (ob != null) {
					if (valueBuilder.length() > 0) {
						valueBuilder.append(", ");
					}
					valueBuilder.append(ob.toString());
				}
			}
			data = valueBuilder.toString();
		}
		if (metadata.getLocalCode().toLowerCase().contains("path")) {
			data = this.getPath(recordElement);
		}

		if (data != null && isRichTextInputType) {
			metadataXmlElement.setContent(new CDATA(data));
		} else {
			metadataXmlElement.setText(data);
		}
		return Collections.singletonList(metadataXmlElement);
	}

	List<Element> createMetadataTagFromMetadataOfTypeReference(Metadata metadata, Record recordElement) {
		List<String> listOfIdsReferencedByMetadata = metadata.isMultivalue() ?
													 recordElement.<String>getList(metadata) :
													 Collections.singletonList(recordElement.<String>get(metadata));
		List<Record> listOfRecordReferencedByMetadata = recordServices
				.getRecordsById(this.collection, listOfIdsReferencedByMetadata);
		List<Element> listOfMetadataTags = new ArrayList<>();
		StringBuilder codeBuilder = new StringBuilder();
		StringBuilder titleBuilder = new StringBuilder();
		StringBuilder codeParentBuilder = new StringBuilder();
		StringBuilder titleParentBuilder = new StringBuilder();
		for (Record recordReferenced : listOfRecordReferencedByMetadata) {
			if (titleBuilder.length() > 0) {
				titleBuilder.append(", ");
			}
			titleBuilder.append((String) recordReferenced.get(Schemas.TITLE, getLocale()));

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
					titleParentBuilder.append((String) parentRecord.get(Schemas.TITLE, getLocale()));

					if (codeParentBuilder.length() > 0) {
						codeParentBuilder.append(", ");
					}
					codeParentBuilder.append((String) parentRecord.get(Schemas.CODE));
				}
			}
		}

		String inheritedMetadataCode = metadata.getCode();
		if (metadata.inheritDefaultSchema()) {
			inheritedMetadataCode = metadata.getInheritance().getCode();
		}

		listOfMetadataTags.addAll(asList(
				new Element(REFERENCE_PREFIX + inheritedMetadataCode.replace("_default_", "_") + "_code")
						.setText(codeBuilder.toString()),
				new Element(REFERENCE_PREFIX + inheritedMetadataCode.replace("_default_", "_") + "_title")
						.setText(titleBuilder.toString())
		));

		if (codeParentBuilder.length() > 0 && titleParentBuilder.length() > 0) {
			listOfMetadataTags.addAll(asList(
					new Element(REFERENCE_PREFIX + inheritedMetadataCode.replace("_default_", "_") + PARENT_SUFFIX + "_code")
							.setText(codeParentBuilder.toString()),
					new Element(REFERENCE_PREFIX + inheritedMetadataCode.replace("_default_", "_") + PARENT_SUFFIX + "_title")
							.setText(titleParentBuilder.toString())
					)
			);
		}
		return listOfMetadataTags;
	}

	public static List<Element> createMetadataTagFromMetadataOfTypeEnum(Metadata metadata, Record recordElement) {
		List<Element> listOfMetadataTags = new ArrayList<>();
		if (recordElement.get(metadata) != null) {
			listOfMetadataTags.addAll(asList(
					new Element(escapeForXmlTag(getLabelOfMetadata(metadata)) + "_code")
							.setText(recordElement.<EnumWithSmallCode>get(metadata).getCode()),
					new Element(escapeForXmlTag(getLabelOfMetadata(metadata)) + "_title")
							.setText($(recordElement.<EnumWithSmallCode>get(metadata)))
					)
			);
		}
		return listOfMetadataTags;
	}

	@Override
	public AbstractXmlGeneratorParameters getXmlGeneratorParameters() {
		return xmlGeneratorParameters;
	}

	public void setXmlGeneratorParameters(AbstractXmlGeneratorParameters xmlGeneratorParameters) {
		this.xmlGeneratorParameters = xmlGeneratorParameters;
	}

	public static String getLabelOfMetadata(Metadata metadata) {
		return metadata.getCode().split("_")[2];
	}

	private String joinDataIntoOneString(List<String> listOfString) {
		StringBuilder builder = new StringBuilder();
		for (String string : listOfString) {
			builder.append(builder.length() == 0 ? string : ", " + string);
		}
		return builder.toString();
	}

	/**
	 * @deprecated use toString()
	 */
	private String converteInstanceToString(Object metadataValue) {
		if (metadataValue instanceof List) {
			return joinDataIntoOneString(((List<String>) metadataValue));
		} else if (metadataValue instanceof LocalDateTime) {
			return ((LocalDateTime) metadataValue).toString("yyyy-MM-dd'T'HH:mm:ss.SSS");
		} else if (metadataValue instanceof LocalDate) {
			return ((LocalDate) metadataValue).toString("yyyy-MM-dd");
		} else {
			return (String) metadataValue;
		}
	}

	protected List<Element> getAdditionalInformations(Record recordElement) {
		List<Element> elementsToAdd = new ArrayList<>(asList(
				new Element("collection_code").setText(collection),
				new Element("collection_title").setText(factory.getCollectionsManager().getCollection(collection).getName())
		));
		List<Element> listOfSpecificAdditionnalElement = getSpecificDataToAddForCurrentElement(recordElement);
		if (!listOfSpecificAdditionnalElement.isEmpty()) {
			elementsToAdd.addAll(listOfSpecificAdditionnalElement);
		}
		return elementsToAdd;
	}

	protected List<Element> getSpecificDataToAddForCurrentElement(Record recordElement) {
		if (recordElement.getSchemaCode().equals(ContainerRecord.DEFAULT_SCHEMA)) {
			DecommissioningService decommissioningService = new DecommissioningService(collection, factory);
			ContainerRecord wrappedRecord = new ContainerRecord(recordElement, getTypes());
			return asList(
					new Element("extremeDates").setText(decommissioningService.getContainerRecordExtremeDates(wrappedRecord)),
					new Element("dispositionDate")
							.setText(getToStringOrNullInString(decommissioningService.getDispositionDate(wrappedRecord))),
					new Element("decommissioningLabel")
							.setText(getToStringOrNull(decommissioningService.getDecommissionningLabel(wrappedRecord)))
			);
		}
		return Collections.emptyList();
	}

	private void setTypeWithElements(Record element) {
		String schemaType = metadataSchemasManager.getSchemaTypeOf(element).getCode();
		this.type = SCHEMA_CODE_MAP.get(schemaType);
	}

	private MetadataSchemaTypes getTypes() {
		return factory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
	}
}
