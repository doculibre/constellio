package com.constellio.app.modules.rm.services.reports;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleInRule;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.reports.parameters.XmlGeneratorParameters;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
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
import com.jgoodies.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Verifier;
import org.jsoup.Jsoup;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
    private RMSchemasRecordsServices rm;

    XmlGeneratorParameters xmlGeneratorParameters;

    public XmlGenerator(AppLayerFactory appLayerFactory, String collection) {
        this.factory = appLayerFactory;
        this.collection = collection;
        this.recordServices = this.factory.getModelLayerFactory().newRecordServices();
        this.metadataSchemasManager = this.factory.getModelLayerFactory().getMetadataSchemasManager();
        this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
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

    MetadataList getListOfMetadataForElement(Record element) {
        return this.factory.getModelLayerFactory().getMetadataSchemasManager().getSchemaOf(element).getMetadatas();
    }

    List<Element> getAdditionalInformations(Record recordElement) {
        MetadataSchemaType metadataSchemaType = getFactory().getModelLayerFactory().getMetadataSchemasManager().getSchemaTypeOf(recordElement);
        List<Element> elementsToAdd = new ArrayList<>(asList(
                new Element("collection_code").setText(getCollection()).setAttribute("label", "Code de collection").setAttribute("code", "collection_code"),
                new Element("collection_title").setText(getFactory().getCollectionsManager().getCollection(getCollection()).getName()).setAttribute("label", "Titre de collection").setAttribute("code", "collection_title")
        ));
        if (metadataSchemaType.getCode().equals(Folder.SCHEMA_TYPE)) {
            Metadata typeMetadata = metadataSchemaType.getDefaultSchema().getMetadata(Folder.TYPE);
            String metadataTypeId = recordElement.get(typeMetadata);
            if (metadataTypeId != null) {
                FolderType currentFolderType = new RMSchemasRecordsServices(getCollection(), getFactory()).getFolderType(metadataTypeId);
                elementsToAdd.addAll(asList(
                        new Element("ref_folder_type_title").setText(currentFolderType.getTitle()).setAttribute("label", metadataSchemaType.getFrenchLabel()).setAttribute("code", typeMetadata.getCode()),
                        new Element("ref_folder_type_code").setText(currentFolderType.getCode()).setAttribute("label", metadataSchemaType.getFrenchLabel()).setAttribute("code", typeMetadata.getCode())
                ));
            }
        }
        List<Element> listOfSpecificAdditionnalElement = getSpecificDataToAddForCurrentElement(recordElement);
        if (!listOfSpecificAdditionnalElement.isEmpty()) {
            elementsToAdd.addAll(listOfSpecificAdditionnalElement);
        }
        return elementsToAdd;
    }

    public static List<Element> createMetadataTagFromMetadataOfTypeStructure(Metadata metadata, Record recordElement, String collection, AppLayerFactory factory) {
        if(!hasMetadata(recordElement, metadata)) {
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
                CopyRetentionRule retentionRule = value instanceof CopyRetentionRuleInRule ? ((CopyRetentionRuleInRule) value).getCopyRetentionRule() : (CopyRetentionRule) value;
                listOfMetadataTags.addAll(asList(
                        new Element(escapeForXmlTag(getLabelOfMetadata(metadata))).setText(retentionRule.toString()),
                        new Element(REFERENCE_PREFIX + metadata.getLocalCode() + "_active_period").setText(retentionRule.getActiveRetentionPeriod().toString()),
                        new Element(REFERENCE_PREFIX + metadata.getLocalCode() + "_active_period_comment").setText(retentionRule.getActiveRetentionComment()),
                        new Element(REFERENCE_PREFIX + metadata.getLocalCode() + "_semi_active_period").setText(retentionRule.getSemiActiveRetentionPeriod().toString()),
                        new Element(REFERENCE_PREFIX + metadata.getLocalCode() + "_semi_active_period_comment").setText(retentionRule.getSemiActiveRetentionComment())
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
            listOfMetadataTags.add(new Element(metadata.getLocalCode()).setText(defaultFormatData(metadataValue != null ? metadataValue.toString() : null, metadata, factory, collection)));
        }
        return listOfMetadataTags;
    }


    public static List<Element> createMetadataTagFromMetadataOfTypeEnum(Metadata metadata, Record recordElement) {
        return createMetadataTagFromMetadataOfTypeEnum(metadata, recordElement, null);
    }

    public static List<Element> createMetadataTagFromMetadataOfTypeEnum(Metadata metadata, Record recordElement, Namespace namespace) {
        if(!hasMetadata(recordElement, metadata)) {
            return Collections.emptyList();
        }
        List<Element> listOfMetadataTags = new ArrayList<>();
        EnumWithSmallCode metadataValue = recordElement.get(metadata);
        String code, title;

        //TODO test;
        if(metadata.isMultivalue()) {
            StringBuilder codeBuilder = new StringBuilder();
            StringBuilder titleBuilder = new StringBuilder();
            for(EnumWithSmallCode enumWithSmallCode : recordElement.<EnumWithSmallCode>getList(metadata)) {
                if(codeBuilder.length() > 0) {
                    codeBuilder.append(", ");
                }
                codeBuilder.append(enumWithSmallCode.getCode());

                if(titleBuilder.length() > 0) {
                    titleBuilder.append(", ");
                }
                titleBuilder.append($(enumWithSmallCode));
            }
            code = codeBuilder.toString();
            title = titleBuilder.toString();
        } else if (metadataValue == null) {
            code = null;
            title = null;
        } else {
            code = metadataValue.getCode();
            title = $(metadataValue);
        }
        listOfMetadataTags.addAll(asList(
                new Element(escapeForXmlTag(getLabelOfMetadata(metadata)) + "_code", namespace).setText(code).setAttribute("label", metadata.getFrenchLabel()).setAttribute("code", escapeForXmlTag(getLabelOfMetadata(metadata)) + "_code", namespace),
                new Element(escapeForXmlTag(getLabelOfMetadata(metadata)) + "_title", namespace).setText(title).setAttribute("label", metadata.getFrenchLabel()).setAttribute("code", escapeForXmlTag(getLabelOfMetadata(metadata)) + "_title", namespace)
                )
        );
        return listOfMetadataTags;
    }

    public static List<Element> createMetadataTagFromMetadataOfTypeReference(Metadata metadata, Record recordElement, String collection, AppLayerFactory factory) {
        return createMetadataTagFromMetadataOfTypeReference(metadata, recordElement, collection, factory, null);
    }

    public static List<Element> createMetadataTagFromMetadataOfTypeReference(Metadata metadata, Record recordElement, String collection, AppLayerFactory factory, Namespace namespace) {
        if(!hasMetadata(recordElement, metadata)) {
            return Collections.emptyList();
        }
        RecordServices recordServices = factory.getModelLayerFactory().newRecordServices();
        MetadataSchemasManager metadataSchemasManager = factory.getModelLayerFactory().getMetadataSchemasManager();
        MetadataSchema recordSchemaType = metadataSchemasManager.getSchemaTypeOf(recordElement).getDefaultSchema();
        List<String> listOfIdsReferencedByMetadata = metadata.isMultivalue() ? recordElement.<String>getList(metadata) : Collections.singletonList(recordElement.<String>get(metadata));
        List<Record> listOfRecordReferencedByMetadata = recordServices.getRecordsById(collection, listOfIdsReferencedByMetadata);
        List<Element> listOfMetadataTags = new ArrayList<>();
        if (listOfRecordReferencedByMetadata.isEmpty()) {
            listOfMetadataTags = asList(
                    new Element(REFERENCE_PREFIX + recordSchemaType.getMetadata(metadata.getLocalCode()).getCode().replace("_default_", "_") + "_code", namespace).setText(null).setAttribute("label", metadata.getFrenchLabel()).setAttribute("code", REFERENCE_PREFIX + metadata.getCode().replace("_default_", "_") + "_code"),
                    new Element(REFERENCE_PREFIX + recordSchemaType.getMetadata(metadata.getLocalCode()).getCode().replace("_default_", "_") + "_title", namespace).setText(null).setAttribute("label", metadata.getFrenchLabel()).setAttribute("code", REFERENCE_PREFIX + metadata.getCode().replace("_default_", "_") + "_title"));

        } else {
            StringBuilder titleBuilder = new StringBuilder();
            StringBuilder codeBuilder = new StringBuilder();
            StringBuilder titleParentBuilder = new StringBuilder();
            StringBuilder codeParentBuilder = new StringBuilder();
            for (Record recordReferenced : listOfRecordReferencedByMetadata) {
                if(titleBuilder.length() > 0) {
                    titleBuilder.append(", ");
                }
                titleBuilder.append(recordReferenced.get(Schemas.TITLE));

                if(codeBuilder.length() > 0) {
                    codeBuilder.append(", ");
                }
                codeBuilder.append(recordReferenced.get(Schemas.CODE));


                if (AdministrativeUnit.SCHEMA_TYPE.equals(recordReferenced.getTypeCode()) || Category.SCHEMA_TYPE.equals(recordReferenced.getTypeCode())) {
                    Metadata parentMetadata = AdministrativeUnit.SCHEMA_TYPE.equals(recordReferenced.getTypeCode()) ? metadataSchemasManager.getSchemaTypeOf(recordReferenced).getDefaultSchema().get(AdministrativeUnit.PARENT) : metadataSchemasManager.getSchemaTypeOf(recordReferenced).getDefaultSchema().get(Category.PARENT);
                    String parentMetadataId = recordReferenced.get(parentMetadata);
                    if (parentMetadataId != null) {
                        Record parentRecord = recordServices.getDocumentById(parentMetadataId);
                        if(titleParentBuilder.length() > 0) {
                            titleParentBuilder.append(", ");
                        }
                        titleParentBuilder.append(parentRecord.get(Schemas.TITLE));

                        if(codeParentBuilder.length() > 0) {
                            codeParentBuilder.append(", ");
                        }
                        codeParentBuilder.append(parentRecord.get(Schemas.CODE));

                    }
                }
            }

            listOfMetadataTags.addAll(asList(
                    new Element(REFERENCE_PREFIX + recordSchemaType.getMetadata(metadata.getLocalCode()).getCode().replace("_default_", "_") + "_code", namespace).setText(codeBuilder.toString()).setAttribute("label", metadata.getFrenchLabel()).setAttribute("code", REFERENCE_PREFIX + metadata.getCode().replace("_default_", "_") + "_code"),
                    new Element(REFERENCE_PREFIX + recordSchemaType.getMetadata(metadata.getLocalCode()).getCode().replace("_default_", "_") + "_title", namespace).setText(titleBuilder.toString()).setAttribute("label", metadata.getFrenchLabel()).setAttribute("code", REFERENCE_PREFIX + metadata.getCode().replace("_default_", "_") + "_title")
            ));
            if(codeParentBuilder.length() > 0 && titleParentBuilder.length() > 0) {
                listOfMetadataTags.addAll(asList(
                        new Element(REFERENCE_PREFIX + recordSchemaType.getMetadata(metadata.getLocalCode()).getCode().replace("_default_", "_") + PARENT_SUFFIX + "_code", namespace).setText(codeParentBuilder.toString()),
                        new Element(REFERENCE_PREFIX + recordSchemaType.getMetadata(metadata.getLocalCode()).getCode().replace("_default_", "_") + PARENT_SUFFIX + "_title", namespace).setText(titleParentBuilder.toString())
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
        } else if (metadata.getType().equals(MetadataValueType.TEXT)) {
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
        String formattedData = defaultFormatData(data, metadata, getFactory(), getCollection());
        return formatToXml(formattedData);
    }

    private String formatToXml(String stringToFormat) {
        if(stringToFormat != null) {
            StringBuilder stringBuilder = new StringBuilder();
            char[] chars = stringToFormat.toCharArray();
            for(char currentChar: chars) {
                if(Verifier.isXMLCharacter(currentChar)) {
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

    public String getPath(Record recordElement){
        StringBuilder builder = new StringBuilder();
        String parentId = recordElement.getParentId();
        if(parentId == null) {
            if(recordElement.getTypeCode().equals(Folder.SCHEMA_TYPE)) {
                parentId = getRMSchemasRecordsServices().wrapFolder(recordElement).getCategory();
            } else if(recordElement.getTypeCode().equals(com.constellio.app.modules.rm.wrappers.Document.SCHEMA_TYPE)) {
                parentId = getRMSchemasRecordsServices().wrapDocument(recordElement).getFolder();
            }
        }
        if(parentId != null ) {
            builder.append(this.getParentPath(getRecordServices().getDocumentById(parentId)));
        }
        builder.append(recordElement.getTitle());
        return builder.toString();
    }

    private String getParentPath(Record recordElement) {
        StringBuilder builder = new StringBuilder();
        String parentId = null;
        if(recordElement.getTypeCode().equals(Folder.SCHEMA_TYPE)) {
            Folder folder = getRMSchemasRecordsServices().wrapFolder(recordElement);
            parentId = folder.getParentFolder();
            if(parentId == null) {
                parentId = folder.getCategory();
            }
        } else if(recordElement.getTypeCode().equals(Category.SCHEMA_TYPE)) {
            Category category = getRMSchemasRecordsServices().wrapCategory(recordElement);
            parentId = category.getParent();
        }
        if(parentId != null) {
            builder.append(this.getParentPath(getRecordServices().getDocumentById(parentId)));
        }
        builder.append(recordElement.getTitle());
        builder.append(" > ");
        return builder.toString();
    }

    public abstract XmlGeneratorParameters getXmlGeneratorParameters();

    private static boolean hasMetadata (Record record, Metadata metadata) {
        try {
            record.get(metadata);
            return true;
        } catch(MetadataSchemasRuntimeException.NoSuchMetadata e) {
            return false;
        }
    }
}
