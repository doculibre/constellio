package com.constellio.app.modules.rm.services.reports;

import com.constellio.app.api.extensions.params.AddFieldsInReportXMLParams;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.services.reports.parameters.XmlReportGeneratorParameters;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.jdom2.CDATA;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class XmlReportGenerator extends AbstractXmlGenerator {

	private XmlReportGeneratorParameters xmlGeneratorParameters;

	public XmlReportGenerator(AppLayerFactory appLayerFactory, String collection,
							  XmlReportGeneratorParameters xmlGeneratorParameters, Locale locale) {
		super(appLayerFactory, collection, locale);
		this.xmlGeneratorParameters = xmlGeneratorParameters;
	}

	@Override
	public String generateXML() {
		xmlGeneratorParameters.validateInputs();
		Document xmlDocument = new Document();
		Element xmlRoot = new Element(XML_ROOT_RECORD_ELEMENTS);
		xmlDocument.setRootElement(xmlRoot);
		XmlReportGeneratorParameters parameters = getXmlGeneratorParameters();
		for (int i = 0; i < parameters.getNumberOfCopies(); i++) {
			Record[] recordsElement = parameters.isParametersUsingIds() ? getRecordFromIds(parameters.getSchemaCode(), parameters.getIdsOfElement()) : parameters.getRecordsElements();
			for (Record recordElement : recordsElement) {
				Element xmlSingularElement = new Element(XML_EACH_RECORD_ELEMENTS);
				xmlSingularElement.setAttribute("schemaCode", recordElement.getSchemaCode());

				Element xmlSingularElementMetadata = new Element(XML_METADATA_TAGS);

				//Add the Extensions to the metadatas elements.
				getFactory().getExtensions().forCollection(getCollection()).addFieldsInReportXML(new AddFieldsInReportXMLParams(
						recordElement, xmlSingularElement, xmlSingularElementMetadata));

				//Add additional informations
				xmlSingularElementMetadata.addContent(getAdditionalInformations(recordElement));

				MetadataList listOfMetadataOfTheCurrentElement = getListOfMetadataForElement(recordElement);

				for (Metadata metadata : listOfMetadataOfTheCurrentElement) {
					List<Element> metadataTags = createMetadataTagsFromMetadata(metadata, recordElement);
					if (this.xmlGeneratorParameters.isForTest()) {
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

	public XmlReportGeneratorParameters getXmlGeneratorParameters() {
		return (XmlReportGeneratorParameters) this.xmlGeneratorParameters;
	}

	public List<Element> createMetadataTagsFromMetadata(Metadata metadata, Record recordElement) {
		if (metadata.getType().equals(MetadataValueType.REFERENCE)) {
			return createMetadataTagFromMetadataOfTypeReference(metadata, recordElement, getCollection(), getFactory());
		}

		if (metadata.getType().equals(MetadataValueType.ENUM)) {
			return createMetadataTagFromMetadataOfTypeEnum(metadata, recordElement, getLocale());
		}

		if (metadata.getType().equals(MetadataValueType.STRUCTURE)) {
			return createMetadataTagFromMetadataOfTypeStructure(metadata, recordElement, getCollection(), getFactory());
		}

		Element metadataXmlElement = new Element(escapeForXmlTag(getLabelOfMetadata(metadata)));
		metadataXmlElement.setAttribute("label", metadata.getFrenchLabel());
		metadataXmlElement.setAttribute("code", escapeForXmlTag(getLabelOfMetadata(metadata)));
		boolean isRichTextInputType = displayManager.getMetadata(getCollection(), metadata.getCode()).getInputType() == MetadataInputType.RICHTEXT;
		String data = getToStringOrNull(recordElement.get(metadata, getLocale()));
		if (!isRichTextInputType) {
			data = formatData(data, metadata);
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

	private Record[] getRecordFromIds(String schemaType, List<String> ids) {
		SearchServices searchServices = getFactory().getModelLayerFactory().newSearchServices();
		MetadataSchemasManager metadataSchemasManager = getFactory().getModelLayerFactory().getMetadataSchemasManager();
		LogicalSearchCondition condition = from(metadataSchemasManager.getSchemaTypes(getCollection()).getSchemaType(schemaType)).where(Schemas.IDENTIFIER).isIn(ids);
		return searchServices.search(new LogicalSearchQuery(condition)).toArray(new Record[0]);
	}

	private List<Element> fillEmptyTags(List<Element> originalElements) {
		List<Element> filledElements = new ArrayList<>();
		for (Element element : originalElements) {
			if (element.getText().isEmpty()) {
				element.setText("This will not appear on the final report");
			}
			filledElements.add(element);
		}
		return filledElements;
	}

	public String getPath(Record recordElement) {
		StringBuilder builder = new StringBuilder();
		MetadataSchema schema = factory.getModelLayerFactory().getMetadataSchemasManager().getSchemaOf(recordElement);
		String parentId = recordElement.getParentId(schema);
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
}
