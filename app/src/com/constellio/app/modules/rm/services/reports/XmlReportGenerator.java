package com.constellio.app.modules.rm.services.reports;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.constellio.app.modules.rm.services.reports.parameters.XmlReportGeneratorParameters;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class XmlReportGenerator extends XmlGenerator{

    public XmlReportGenerator(AppLayerFactory appLayerFactory, String collection, XmlReportGeneratorParameters xmlGeneratorParameters) {
        super(appLayerFactory, collection);
        this.xmlGeneratorParameters = xmlGeneratorParameters;
    }

    @Override
    public String generateXML(){
        xmlGeneratorParameters.validateInputs();
        Document xmlDocument = new Document();
        Element xmlRoot = new Element(XML_ROOT_RECORD_ELEMENTS);
        xmlDocument.setRootElement(xmlRoot);
        XmlReportGeneratorParameters parameters = getXmlGeneratorParameters();
        for(int i = 0; i < parameters.getNumberOfCopies(); i ++) {
            Record[] recordsElement = parameters.isParametersUsingIds() ? getRecordFromIds(parameters.getSchemaCode(), parameters.getIdsOfElement()) : parameters.getRecordsElements();
            for(Record recordElement : recordsElement) {
                Element xmlSingularElement = new Element(XML_EACH_RECORD_ELEMENTS);
                xmlSingularElement.setAttribute("schemaCode", recordElement.getSchemaCode());

                Element xmlSingularElementMetadata = new Element(XML_METADATA_TAGS);

                //TODO create an extensions and call it here.

                //Add the Extensions to the metadatas elements.
//                getFactory().getExtensions().forCollection(getCollection()).addFieldsInLabelXML(new AddFieldsInLabelXMLParams(
//                        recordElement, xmlSingularElement, XMLMetadatasOfSingularElement));

                //Add additional informations
                xmlSingularElementMetadata.addContent(getAdditionalInformations(recordElement));

                MetadataList listOfMetadataOfTheCurrentElement = getListOfMetadataForElement(recordElement);

                for(Metadata metadata : listOfMetadataOfTheCurrentElement) {
                    List<Element> metadataTags = createMetadataTagsFromMetadata(metadata, recordElement);
                    if(this.xmlGeneratorParameters.isForTest()) {
                        metadataTags = fillEmptyTags(metadataTags);
                    }
                    xmlSingularElementMetadata.addContent(metadataTags);
                }

                xmlSingularElement.setContent(xmlSingularElementMetadata);
                xmlRoot.addContent(xmlSingularElement);
            }
        }
        return new XMLOutputter(Format.getPrettyFormat()).outputString(xmlDocument);
    }


    @Override
    public List<Element> getSpecificDataToAddForCurrentElement(Record recordElement) {
        return new ArrayList<>();
    }

    @Override
    public XmlReportGeneratorParameters getXmlGeneratorParameters() {
        return (XmlReportGeneratorParameters) this.xmlGeneratorParameters;
    }

    public List<Element> createMetadataTagsFromMetadata(Metadata metadata, Record recordElement) {
        if (metadata.getType().equals(MetadataValueType.REFERENCE)) {
            return createMetadataTagFromMetadataOfTypeReference(metadata, recordElement, getCollection(), getFactory());
        }

        if (metadata.getType().equals(MetadataValueType.ENUM)) {
            return createMetadataTagFromMetadataOfTypeEnum(metadata, recordElement);
        }

        if(metadata.getType().equals(MetadataValueType.STRUCTURE)) {
            return createMetadataTagFromMetadataOfTypeStructure(metadata, recordElement, getCollection(), getFactory());
        }

        Element metadataXmlElement = new Element(escapeForXmlTag(getLabelOfMetadata(metadata)));
        metadataXmlElement.setAttribute("label", metadata.getFrenchLabel());
        metadataXmlElement.setAttribute("code", escapeForXmlTag(getLabelOfMetadata(metadata)));
        String data = formatData(getToStringOrNull(recordElement.get(metadata)), metadata);
        if(metadata.isMultivalue()) {
            StringBuilder valueBuilder = new StringBuilder();
            List<Object> objects = recordElement.getList(metadata);
            for(Object ob : objects) {
                if(ob != null) {
                    if(valueBuilder.length() > 0) {
                        valueBuilder.append(", ");
                    }
                    valueBuilder.append(ob.toString());
                }
            }
            data = valueBuilder.toString();
        }
        if(metadata.getLocalCode().toLowerCase().contains("path")) {
            data = this.getPath(recordElement);
        }
        metadataXmlElement.setText(data);
        return Collections.singletonList(metadataXmlElement);
    }

    private Record[] getRecordFromIds(String schemaType, final List<String> ids) {
        List<Record> allRecords = new ArrayList<>();
        SearchServices searchServices = getFactory().getModelLayerFactory().newSearchServices();
        MetadataSchemasManager metadataSchemasManager = getFactory().getModelLayerFactory().getMetadataSchemasManager();
        List<List<String>> splittedIds = getChunkList(ids, 1000);
        for (List<String> idChunk : splittedIds) {
            LogicalSearchCondition condition = from(metadataSchemasManager.getSchemaTypes(getCollection()).getSchemaType(schemaType)).where(Schemas.IDENTIFIER).isIn(idChunk);
            List<Record> recordChunk = searchServices.search(new LogicalSearchQuery(condition));
            allRecords.addAll(recordChunk);
        }
        allRecords.sort(new Comparator<Record>() {
            @Override
            public int compare(Record o1, Record o2) {
                Integer indexOfo1 = ids.indexOf(o1.getId());
                Integer indexOfo2 = ids.indexOf(o2.getId());
                return indexOfo1.compareTo(indexOfo2);
            }
        });
        return allRecords.toArray(new Record[0]);
    }
    
    private <T> List<List<T>> getChunkList(List<T> largeList , int chunkSize) {
        List<List<T>> chunkList = new ArrayList<>();
        for (int i = 0 ; i <  largeList.size() ; i += chunkSize) {
            chunkList.add(largeList.subList(i , i + chunkSize >= largeList.size() ? largeList.size() : i + chunkSize));
        }
        return chunkList;
    }

    private List<Element> fillEmptyTags(List<Element> originalElements) {
        List<Element> filledElements = new ArrayList<>();
        for (Element element : originalElements) {
            if(element.getText().isEmpty()) {
                element.setText("This will not appear on the final report");
            }
            filledElements.add(element);
        }
        return filledElements;
    }
}
