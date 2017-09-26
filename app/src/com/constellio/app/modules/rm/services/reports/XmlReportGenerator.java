package com.constellio.app.modules.rm.services.reports;

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
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

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

        Element metadataXmlElement = new Element(escapeForXmlTag(getLabelOfMetadata(metadata)));
        metadataXmlElement.setAttribute("label", metadata.getFrenchLabel());
        metadataXmlElement.setAttribute("code", escapeForXmlTag(getLabelOfMetadata(metadata)));
        metadataXmlElement.setText(formatData(getToStringOrNull(recordElement.get(metadata)), metadata));
        return Collections.singletonList(metadataXmlElement);
    }

    private Record[] getRecordFromIds(String schemaType, List<String> ids) {
        SearchServices searchServices = getFactory().getModelLayerFactory().newSearchServices();
        MetadataSchemasManager metadataSchemasManager = getFactory().getModelLayerFactory().getMetadataSchemasManager();
        LogicalSearchCondition condition = from(metadataSchemasManager.getSchemaTypes(getCollection()).getSchemaType(schemaType)).where(Schemas.IDENTIFIER).isIn(ids);
        return searchServices.search(new LogicalSearchQuery(condition)).toArray(new Record[0]);
    }
}
