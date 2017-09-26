package com.constellio.app.modules.rm.services.reports;

import com.constellio.app.modules.rm.services.reports.parameters.XmlGeneratorParameters;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.SimpleDateFormatSingleton;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.jgoodies.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jsoup.Jsoup;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public abstract class XmlGenerator {
    /**
     * First element of the XMl document, usually in plural form.
     */
    public static final String XML_ROOT_RECORD_ELEMENTS = "records";


    /**
     * Xml tag of every element in the XML
     */
    public static final String XML_EACH_RECORD_ELEMENTS = "record";

    public static final String REFERENCE_PREFIX = "ref_";

    public static final String PARENT_SUFFIX = "_parent";

    public static LangUtils.StringReplacer replaceInvalidXMLCharacter = LangUtils.replacingRegex("[\\( \\)]", "").replacingRegex("[&$%]", "");
    public static LangUtils.StringReplacer replaceBracketsInValueToString = LangUtils.replacingRegex("[\\[\\]]", "");


    public static final String XML_METADATA_TAGS = "metadatas";

    public static final String XML_METADATA_SINGLE_TAGS = "metadata";

    private AppLayerFactory factory;

    private String collection;

    private RecordServices recordServices;

    private MetadataSchemasManager metadataSchemasManager;

    XmlGeneratorParameters xmlGeneratorParameters;

    public XmlGenerator(AppLayerFactory appLayerFactory, String collection) {
        this.factory = appLayerFactory;
        this.collection = collection;
        this.recordServices = this.factory.getModelLayerFactory().newRecordServices();
        this.metadataSchemasManager = this.factory.getModelLayerFactory().getMetadataSchemasManager();
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
        if (!listOfSpecificAdditionnalElement.isEmpty()) {
            elementsToAdd.addAll(listOfSpecificAdditionnalElement);
        }
        return elementsToAdd;
    }

    public static List<Element> createMetadataTagFromMetadataOfTypeEnum(Metadata metadata, Record recordElement) {
        return createMetadataTagFromMetadataOfTypeEnum(metadata, recordElement, null);
    }

    public static List<Element> createMetadataTagFromMetadataOfTypeEnum(Metadata metadata, Record recordElement, Namespace namespace) {
        List<Element> listOfMetadataTags = new ArrayList<>();
        if (recordElement.get(metadata) != null) {
            listOfMetadataTags.addAll(asList(
                    new Element(escapeForXmlTag(getLabelOfMetadata(metadata)) + "_code", namespace).setText(recordElement.<EnumWithSmallCode>get(metadata).getCode()).setAttribute("label", metadata.getFrenchLabel()).setAttribute("code", escapeForXmlTag(getLabelOfMetadata(metadata)) + "_code", namespace),
                    new Element(escapeForXmlTag(getLabelOfMetadata(metadata)) + "_title", namespace).setText(recordElement.get(metadata).toString()).setAttribute("label", metadata.getFrenchLabel()).setAttribute("code", escapeForXmlTag(getLabelOfMetadata(metadata)) + "_title", namespace)
                    )
            );
        }
        return listOfMetadataTags;
    }

    public static List<Element> createMetadataTagFromMetadataOfTypeReference(Metadata metadata, Record recordElement, String collection, AppLayerFactory factory) {
        return createMetadataTagFromMetadataOfTypeReference(metadata, recordElement, collection, factory, null);
    }

    public static List<Element> createMetadataTagFromMetadataOfTypeReference(Metadata metadata, Record recordElement, String collection, AppLayerFactory factory, Namespace namespace) {
        RecordServices recordServices = factory.getModelLayerFactory().newRecordServices();
        MetadataSchemasManager metadataSchemasManager = factory.getModelLayerFactory().getMetadataSchemasManager();
        List<String> listOfIdsReferencedByMetadata = metadata.isMultivalue() ? recordElement.<String>getList(metadata) : Collections.singletonList(recordElement.<String>get(metadata));
        List<Record> listOfRecordReferencedByMetadata = recordServices.getRecordsById(collection, listOfIdsReferencedByMetadata);
        List<Element> listOfMetadataTags = new ArrayList<>();
        for (Record recordReferenced : listOfRecordReferencedByMetadata) {
            listOfMetadataTags.addAll(asList(
                    new Element(REFERENCE_PREFIX + metadata.getCode().replace("_default_", "_") + "_code", namespace).setText(recordReferenced.<String>get(Schemas.CODE)).setAttribute("label", metadata.getFrenchLabel()).setAttribute("code", REFERENCE_PREFIX + metadata.getCode().replace("_default_", "_") + "_code"),
                    new Element(REFERENCE_PREFIX + metadata.getCode().replace("_default_", "_") + "_title", namespace).setText(recordReferenced.<String>get(Schemas.TITLE)).setAttribute("label", metadata.getFrenchLabel()).setAttribute("code", REFERENCE_PREFIX + metadata.getCode().replace("_default_", "_") + "_title")
            ));

            if (AdministrativeUnit.SCHEMA_TYPE.equals(recordReferenced.getTypeCode()) || Category.SCHEMA_TYPE.equals(recordReferenced.getTypeCode())) {
                Metadata parentMetadata = AdministrativeUnit.SCHEMA_TYPE.equals(recordReferenced.getTypeCode()) ? metadataSchemasManager.getSchemaTypeOf(recordReferenced).getDefaultSchema().get(AdministrativeUnit.PARENT) : metadataSchemasManager.getSchemaTypeOf(recordReferenced).getDefaultSchema().get(Category.PARENT);
                String parentMetadataId = recordReferenced.get(parentMetadata);
                if (parentMetadataId != null) {
                    Record parentRecord = recordServices.getDocumentById(parentMetadataId);
                    listOfMetadataTags.addAll(asList(
                            new Element(REFERENCE_PREFIX + metadata.getCode().replace("_default_", "_") + PARENT_SUFFIX + "_code", namespace).setText(parentRecord.<String>get(Schemas.CODE)),
                            new Element(REFERENCE_PREFIX + metadata.getCode().replace("_default_", "_") + PARENT_SUFFIX + "_title", namespace).setText(parentRecord.<String>get(Schemas.TITLE))
                    ));
                }
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
                DateFormat df = isDateTime ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS") : new SimpleDateFormat("yyyy-MM-dd");
                Date date = df.parse(data);
                finalData = SimpleDateFormatSingleton.getSimpleDateFormat(isDateTime ? configs.getDateTimeFormat() : configs.getDateFormat()).format(date);
            } catch (ParseException e) {
                return data;
            }
        } else if(metadata.getType().equals(MetadataValueType.TEXT)) {
            finalData = Jsoup.parse(data).text();
        }
        return replaceBracketsInValueToString.replaceOn(finalData);
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

    public abstract String generateXML();

    abstract List<Element> getSpecificDataToAddForCurrentElement(Record recordElement);

    String formatData(String data, Metadata metadata) {
        return defaultFormatData(data, metadata, getFactory(), getCollection());
    }

    public abstract XmlGeneratorParameters getXmlGeneratorParameters();
}
