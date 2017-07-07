package com.constellio.app.modules.rm.services.reports;

import com.constellio.app.modules.rm.services.reports.parameters.XmlGeneratorParameters;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataList;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Created by Marco on 2017-07-05.
 */
public abstract class XmlGenerator {
    /**
     * First element of the XMl document, usually in plural form.
     */
    public static final String XML_ROOT_RECORD_ELEMENTS= "records";

    /**
     * Xml tag of every element in the XML
     */
    public static final String XML_EACH_RECORD_ELEMENTS = "record";

    public static final String REFERENCE_PREFIX = "ref_";

    public static LangUtils.StringReplacer replaceInvalidXMLCharacter = LangUtils.replacingRegex("[\\( \\)]", "").replacingRegex("[&$%]", "");
    public static LangUtils.StringReplacer replaceBracketsInValueToString = LangUtils.replacingRegex("[\\[\\]]", "");


    public static final String XML_METADATA_TAGS = "metadatas";

    public static final String XML_METADATA_SINGLE_TAGS = "metadata";

    private AppLayerFactory factory;

    private String collection;

    private RecordServices recordServices;

    XmlGeneratorParameters xmlGeneratorParameters;

    public XmlGenerator(AppLayerFactory appLayerFactory, String collection) {
        this.factory = appLayerFactory;
        this.collection = collection;
        this.recordServices = this.factory.getModelLayerFactory().newRecordServices();
    }

    public AppLayerFactory getFactory() {
        return this.factory;
    }

    public String getCollection() {
        return this.collection;
    }

    MetadataList getListOfMetadataForElement(Record element) {
        return this.factory.getModelLayerFactory().getMetadataSchemasManager().getSchemaOf(element).getMetadatas();
    }

    List<Element> getAdditionalInformations(Record recordElement) {
        List<Element> elementsToAdd = new ArrayList<>(asList(
                new Element("collection_code").setText(getCollection()).setAttribute("label", "Code de collection").setAttribute("code", "collection_code"),
                new Element("collection_title").setText(getFactory().getCollectionsManager().getCollection(getCollection()).getName()).setAttribute("label", "Titre de collection").setAttribute("code", "collection_title")
        ));
        List<Element> listOfSpecificAdditionnalElement = getSpecificDataToAddForCurrentElement(recordElement);
        if(!listOfSpecificAdditionnalElement.isEmpty()) {
            elementsToAdd.addAll(listOfSpecificAdditionnalElement);
        }
        return elementsToAdd;
    }

    List<Element> createMetadataTagFromMetadataOfTypeEnum(Metadata metadata, Record recordElement) {
        List<Element> listOfMetadataTags = new ArrayList<>();
        if (recordElement.get(metadata) != null) {
            listOfMetadataTags.addAll(asList(
                    new Element(escapeForXmlTag(getLabelOfMetadata(metadata)) + "_code").setText(recordElement.<EnumWithSmallCode>get(metadata).getCode()).setAttribute("label", metadata.getFrenchLabel()).setAttribute("code", escapeForXmlTag(getLabelOfMetadata(metadata)) + "_code"),
                    new Element(escapeForXmlTag(getLabelOfMetadata(metadata)) + "_title").setText(recordElement.get(metadata).toString()).setAttribute("label", metadata.getFrenchLabel()).setAttribute("code", escapeForXmlTag(getLabelOfMetadata(metadata)) + "_title")
                    )
            );
        }
        return listOfMetadataTags;
    }

    List<Element> createMetadataTagFromMetadataOfTypeReference(Metadata metadata, Record recordElement) {
        List<String> listOfIdsReferencedByMetadata = metadata.isMultivalue() ? recordElement.<String>getList(metadata) : Collections.singletonList(recordElement.<String>get(metadata));
        List<Record> listOfRecordReferencedByMetadata = recordServices.getRecordsById(this.collection, listOfIdsReferencedByMetadata);
        List<Element> listOfMetadataTags = new ArrayList<>();
        for (Record recordReferenced : listOfRecordReferencedByMetadata) {
            listOfMetadataTags.addAll(asList(
                    new Element(REFERENCE_PREFIX + metadata.getCode().replace("_default_", "_") + "_code").setText(recordReferenced.<String>get(Schemas.CODE)).setAttribute("label", metadata.getFrenchLabel()).setAttribute("code", REFERENCE_PREFIX + metadata.getCode().replace("_default_", "_") + "_code"),
                    new Element(REFERENCE_PREFIX + metadata.getCode().replace("_default_", "_") + "_title").setText(recordReferenced.<String>get(Schemas.TITLE)).setAttribute("label", metadata.getFrenchLabel()).setAttribute("code", REFERENCE_PREFIX + metadata.getCode().replace("_default_", "_") + "_title")
            ));
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

    public static String getLabelOfMetadata(Metadata metadata) {
        return metadata.getCode().split("_")[2];
    }

    String getToStringOrNull(Object ob) {
        return ob == null ? null : ob.toString();
    }

    String getToStringOrNullInString(Object ob) {
        return ob == null ? "null" : ob.toString();
    }

    public abstract String generateXML() throws Exception;

    abstract List<Element> getSpecificDataToAddForCurrentElement(Record recordElement);

    abstract String formatData(String data, Metadata metadata);

    public abstract XmlGeneratorParameters getXmlGeneratorParameters();
}
