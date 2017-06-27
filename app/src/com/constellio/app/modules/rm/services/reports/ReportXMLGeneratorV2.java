package com.constellio.app.modules.rm.services.reports;

import com.constellio.app.api.extensions.params.AddFieldsInLabelXMLParams;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.SimpleDateFormatSingleton;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
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
public class ReportXMLGeneratorV2<T extends RecordWrapper> {

    private static final Map<Class, String> POSSIBLE_CLASS = new HashMap<Class, String>() {{
        put(Folder.class, "folders");
        put(ContainerRecord.class, "containers");
    }};

    public static final String REFERENCE_PREFIX = "ref_";

    public static LangUtils.StringReplacer replaceInvalidXMLCharacter = LangUtils.replacingRegex("[\\( \\)]", "").replacingRegex("[&$%]", "");
    public static LangUtils.StringReplacer replaceBracketsInValueToString = LangUtils.replacingRegex("[\\[\\]]", "");

    private T[] recordElements;

    private String collection;

    private AppLayerFactory factory;

    private RecordServices recordServices;

    private int startingPosition, numberOfCopies = 1;

    private String type;

    public ReportXMLGeneratorV2(String collection, AppLayerFactory appLayerFactory) {
        this.collection = collection;
        this.factory = appLayerFactory;
        this.recordServices = factory.getModelLayerFactory().newRecordServices();
    }

    public ReportXMLGeneratorV2(String collection, AppLayerFactory appLayerFactory, T... recordElements) {
        this(collection, appLayerFactory);
        this.recordElements = recordElements;
    }

    public ReportXMLGeneratorV2(String collection, AppLayerFactory appLayerFactory, int startingPosition, int numberOfCopies, T... recordElements) {
        this(collection, appLayerFactory, recordElements);
        this.startingPosition = startingPosition;
        this.numberOfCopies = numberOfCopies;
    }

    public T[] getElements() {
        return this.recordElements;
    }

    public ReportXMLGeneratorV2<T> setElements(T... elements) {
        this.recordElements = elements;
        return this;
    }

    public int getStartingPosition() {
        return startingPosition;
    }

    public ReportXMLGeneratorV2<T> setStartingPosition(int startingPosition) {
        this.startingPosition = startingPosition;
        return this;
    }

    public int getNumberOfCopies() {
        return numberOfCopies;
    }

    public ReportXMLGeneratorV2<T> setNumberOfCopies(int numberOfCopies) {
        this.numberOfCopies = numberOfCopies;
        return this;
    }

    public String generateXML() throws Exception {
        validateInputs();
        Document xmlDocument = new Document();
        Element xmlRoot = new Element(this.type);
        xmlDocument.setRootElement(xmlRoot);
        xmlRoot.addContent(this.skipAmountOfElementNeededForStartingPosition());
        for(int i = 0; i < this.numberOfCopies; i ++ ) {
            for (T recordElement : this.recordElements) {
                //the xml tag singular ( folder / container )
                Element xmlSingularElement = new Element(getTypeSingular());

                //the xml tag metadatas
                Element XMLMetadatasOfSingularElement = new Element("metadatas");

                // List of all metadatas of current RecordElement
                List<Metadata> listOfMetadataOfRecordElement = getListOfMetadataForElement(recordElement);

                //Add the Extensions to the metadatas elements.
                factory.getExtensions().forCollection(collection).addFieldsInLabelXML(new AddFieldsInLabelXMLParams(
                        recordElement.getWrappedRecord(), xmlSingularElement, XMLMetadatasOfSingularElement));

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
    }

    /**
     * Function that checks a string and replace if needed. Used to get valid XML tag.
     *
     * @param input String
     * @return
     */
    private static String escapeForXmlTag(String input) {
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

        if (!validateClassTypeOfElements(this.recordElements[0])) {
            throw new Exception("Invalid class instance, possible input are " + getPossibleClassName());
        }

        if (this.numberOfCopies == 0) {
            throw new Exception("Number of copy must not be equals or smaller to 0");
        }

        if (this.startingPosition < 0) {
            throw new Exception("Starting position must not be inferior to 0");
        }
    }

    private boolean validateClassTypeOfElements(T elementToValidate) {
        for (Map.Entry<Class, String> possibleClass : POSSIBLE_CLASS.entrySet()) {
            if (possibleClass.getKey().isInstance(elementToValidate)) {
                this.type = possibleClass.getValue();
                return true;
            }
        }

        return false;
    }

    private List<Element> skipAmountOfElementNeededForStartingPosition() {
        List<Element> XMLelementList = new ArrayList<>();
        for (int i = 0; i < this.startingPosition; i++) {
            XMLelementList.add(new Element(this.getTypeSingular()).setContent(new Element("metadatas")));
        }
        return XMLelementList;
    }

    private String getTypeSingular() {
        return this.type.substring(0, this.type.length() - 1);
    }

    private List<Metadata> getListOfMetadataForElement(T element) {
        return element.getSchema().getMetadatas();
    }

    private List<Element> createMetadataTagsFromMetadata(Metadata metadata, T recordElement) {
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

    private List<Element> createMetadataTagFromMetadataOfTypeReference(Metadata metadata, T recordElement) {
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

    private List<Element> createMetadataTagFromMetadataOfTypeEnum(Metadata metadata, T recordElement) {
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

    private String formatData(String data, Metadata metadata) {
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

    private String getLabelOfMetadata(Metadata metadata) {
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

    private String getPossibleClassName() {
        return Lists.transform(new ArrayList<>(POSSIBLE_CLASS.keySet()), new Function<Class, String>() {
            @Override
            public String apply(Class input) {
                return input.getName();
            }
        }).toString();
    }

    private List<Element> getAdditionalInformations(T recordElement) {
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

    private List<Element> getSpecificDataToAddForCurrentElement(T recordElement) {
        if(recordElement instanceof ContainerRecord) {
            DecommissioningService decommissioningService = new DecommissioningService(collection, factory);
            return asList(
                    new Element("extremeDates").setText(decommissioningService.getContainerRecordExtremeDates((ContainerRecord) recordElement)),
                    new Element("dispositionDate").setText(getToStringOrNullInString(decommissioningService.getDispositionDate((ContainerRecord) recordElement))),
                    new Element("decommissioningLabel").setText(getToStringOrNull(decommissioningService.getDecommissionningLabel((ContainerRecord) recordElement)))
            );
        }
        return Collections.emptyList();
    }

    private String getToStringOrNull(Object ob) {
        return ob == null ? null : ob.toString();
    }

    private String getToStringOrNullInString(Object ob) {
        return ob == null ? "null" : ob.toString();
    }
}
