package com.constellio.app.modules.rm.services.reports;

import com.constellio.app.api.extensions.params.AddFieldsInLabelXMLParams;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.services.reports.parameters.XmlGeneratorParameters;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.SimpleDateFormatSingleton;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.*;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;


/**
 * Class that creates the XML for the labels.
 */
public class ReportXMLGeneratorV2 extends  XmlGenerator {

    public static final String REFERENCE_PREFIX = "ref_";
    private static final Map<String, String> SCHEMA_CODE_MAP= new HashMap<String, String>() {{
        put(ContainerRecord.SCHEMA_TYPE, "containers");
        put(Folder.SCHEMA_TYPE, "folders");
    }};

    public static LangUtils.StringReplacer replaceInvalidXMLCharacter = LangUtils.replacingRegex("[\\( \\)]", "").replacingRegex("[&$%]", "");
    public static LangUtils.StringReplacer replaceBracketsInValueToString = LangUtils.replacingRegex("[\\[\\]]", "");

    private Record[] recordElements;

    private String collection;

    private AppLayerFactory factory;

    private RecordServices recordServices;

    private MetadataSchemasManager metadataSchemasManager;

    private int startingPosition, numberOfCopies = 1;

    private String type;

    public ReportXMLGeneratorV2(String collection, AppLayerFactory appLayerFactory) {
        super(appLayerFactory, collection);
        this.collection = collection;
        this.factory = appLayerFactory;
        this.recordServices = factory.getModelLayerFactory().newRecordServices();
        this.metadataSchemasManager = factory.getModelLayerFactory().getMetadataSchemasManager();
    }

    public ReportXMLGeneratorV2(String collection, AppLayerFactory appLayerFactory, Record... recordElements) {
        this(collection, appLayerFactory);
        this.setElements(recordElements);
    }

    public ReportXMLGeneratorV2(String collection, AppLayerFactory appLayerFactory, int startingPosition, int numberOfCopies, Record... recordElements) {
        this(collection, appLayerFactory, recordElements);
        this.startingPosition = startingPosition;
        this.numberOfCopies = numberOfCopies;
    }

    public Record[] getElements() {
        return this.recordElements;
    }

    public ReportXMLGeneratorV2 setElements(Record... elements) {
        this.recordElements = elements;
        this.setTypeWithElements(this.recordElements[0]);
        return this;
    }

    public int getStartingPosition() {
        return startingPosition;
    }

    public ReportXMLGeneratorV2 setStartingPosition(int startingPosition) {
        this.startingPosition = startingPosition;
        return this;
    }

    public int getNumberOfCopies() {
        return numberOfCopies;
    }

    public ReportXMLGeneratorV2 setNumberOfCopies(int numberOfCopies) {
        this.numberOfCopies = numberOfCopies;
        return this;
    }

    public String getCollection() { return this.collection; }

    public AppLayerFactory getFactory() { return this.factory; }

    @Override
    public String generateXML(){
        try{
            validateInputs();
            Document xmlDocument = new Document();
            Element xmlRoot = new Element(this.type);
            xmlDocument.setRootElement(xmlRoot);
            xmlRoot.addContent(this.skipAmountOfElementNeededForStartingPosition());
            for(int i = 0; i < this.numberOfCopies; i ++ ) {
                for (Record recordElement : this.recordElements) {
                    //the xml tag singular ( folder / container )
                    Element xmlSingularElement = new Element(getTypeSingular());

                    //the xml tag metadatas
                    Element XMLMetadatasOfSingularElement = new Element("metadatas");

                    //Add the Extensions to the metadatas elements.
                    factory.getExtensions().forCollection(collection).addFieldsInLabelXML(new AddFieldsInLabelXMLParams(
                            recordElement, xmlSingularElement, XMLMetadatasOfSingularElement));

                    // List of all metadatas of current RecordElement
                    List<Metadata> listOfMetadataOfRecordElement = getListOfMetadataForElement(recordElement);

                    //Add the additional informations to the metadatas.
                    XMLMetadatasOfSingularElement.addContent(getAdditionalInformations(recordElement));

                    //Iterate through metadatas
                    for (Metadata metadata : listOfMetadataOfRecordElement) {
                        //Create the metadata tags.
                        List<Element> metadataTags = createMetadataTagsFromMetadata(metadata, recordElement);
                        //add them to the childrens.
                        XMLMetadatasOfSingularElement.addContent(metadataTags);
                    }

                    xmlSingularElement.setContent(XMLMetadatasOfSingularElement);
                    xmlRoot.addContent(xmlSingularElement);
                }
            }
            return new XMLOutputter(Format.getPrettyFormat()).outputString(xmlDocument);
        }catch (Exception e) {
            //error in validation
        }
       return "";
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

    private void validateInputs() throws Exception {
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

    MetadataList getListOfMetadataForElement(Record element) {
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
        metadataXmlElement.setText(formatData(getToStringOrNull(recordElement.get(metadata)), metadata));
        return Collections.singletonList(metadataXmlElement);
    }

    List<Element> createMetadataTagFromMetadataOfTypeReference(Metadata metadata, Record recordElement) {
        List<String> listOfIdsReferencedByMetadata = metadata.isMultivalue() ? recordElement.<String>getList(metadata) : Collections.singletonList(recordElement.<String>get(metadata));
        List<Record> listOfRecordReferencedByMetadata = recordServices.getRecordsById(this.collection, listOfIdsReferencedByMetadata);
        List<Element> listOfMetadataTags = new ArrayList<>();
        for (Record recordReferenced : listOfRecordReferencedByMetadata) {
            listOfMetadataTags.addAll(asList(
                    new Element(REFERENCE_PREFIX + metadata.getCode().replace("_default_", "_") + "_code").setText(recordReferenced.<String>get(Schemas.CODE)),
                    new Element(REFERENCE_PREFIX + metadata.getCode().replace("_default_", "_") + "_title").setText(recordReferenced.<String>get(Schemas.TITLE))
            ));
        }
        return listOfMetadataTags;
    }

    List<Element> createMetadataTagFromMetadataOfTypeEnum(Metadata metadata, Record recordElement) {
        List<Element> listOfMetadataTags = new ArrayList<>();
        if (recordElement.get(metadata) != null) {
            listOfMetadataTags.addAll(asList(
                    new Element(escapeForXmlTag(getLabelOfMetadata(metadata)) + "_code").setText(recordElement.<EnumWithSmallCode>get(metadata).getCode()),
                    new Element(escapeForXmlTag(getLabelOfMetadata(metadata)) + "_title").setText(recordElement.get(metadata).toString())
                    )
            );
        }
        return listOfMetadataTags;
    }

    String formatData(String data, Metadata metadata) {
        if (Strings.isNullOrEmpty(data)) {
            return data;
        }
        String finalData = data;
        ConstellioEIMConfigs configs = new ConstellioEIMConfigs(factory.getModelLayerFactory().getSystemConfigurationsManager());
        if (metadata.getType().equals(MetadataValueType.BOOLEAN)) {
            finalData = $(data);
        } else if (metadata.getType().equals(MetadataValueType.DATE) || metadata.getType().equals(MetadataValueType.DATE_TIME)) {
            try {
                boolean isDateTime = metadata.getType().equals(MetadataValueType.DATE_TIME);
                DateFormat df = isDateTime ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS") : new SimpleDateFormat("yyyy-MM-dd");
                Date date = df.parse(data);
                finalData = SimpleDateFormatSingleton.getSimpleDateFormat(isDateTime ? configs.getDateTimeFormat() : configs.getDateFormat()).format(date);
            } catch (ParseException e) {
                return data;
            }
        }
        return replaceBracketsInValueToString.replaceOn(finalData);
    }

    @Override
    public XmlGeneratorParameters getXmlGeneratorParameters() {
        return null;
    }

    public static String getLabelOfMetadata(Metadata metadata) {
        return metadata.getCode().split("_")[2];
    }

    private String joinDataIntoOneString(List<String> listOfString) {
        StringBuilder builder = new StringBuilder();
        for(String string : listOfString) {
            builder.append(builder.length() == 0 ? string : ", " + string);
        }
        return builder.toString();
    }

    /**
     * @deprecated
     * use toString()
     */
    private String converteInstanceToString(Object metadataValue) {
        if(metadataValue instanceof List) {
            return joinDataIntoOneString(((List<String>) metadataValue));
        } else if(metadataValue instanceof LocalDateTime) {
            return ((LocalDateTime) metadataValue).toString("yyyy-MM-dd'T'HH:mm:ss.SSS");
        }else if(metadataValue instanceof LocalDate) {
            return ((LocalDate) metadataValue).toString("yyyy-MM-dd");
        } else {
            return (String) metadataValue;
        }
    }

    List<Element> getAdditionalInformations(Record recordElement) {
        List<Element> elementsToAdd = new ArrayList<>(asList(
                new Element("collection_code").setText(collection),
                new Element("collection_title").setText(factory.getCollectionsManager().getCollection(collection).getName())
        ));
        List<Element> listOfSpecificAdditionnalElement = getSpecificDataToAddForCurrentElement(recordElement);
        if(!listOfSpecificAdditionnalElement.isEmpty()) {
            elementsToAdd.addAll(listOfSpecificAdditionnalElement);
        }
        return elementsToAdd;
    }

    List<Element> getSpecificDataToAddForCurrentElement(Record recordElement) {
        if(recordElement.getSchemaCode().equals(ContainerRecord.DEFAULT_SCHEMA)) {
            DecommissioningService decommissioningService = new DecommissioningService(collection, factory);
            ContainerRecord wrappedRecord = new ContainerRecord(recordElement, getTypes());
            return asList(
                    new Element("extremeDates").setText(decommissioningService.getContainerRecordExtremeDates(wrappedRecord)),
                    new Element("dispositionDate").setText(getToStringOrNullInString(decommissioningService.getDispositionDate(wrappedRecord))),
                    new Element("decommissioningLabel").setText(getToStringOrNull(decommissioningService.getDecommissionningLabel(wrappedRecord)))
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
