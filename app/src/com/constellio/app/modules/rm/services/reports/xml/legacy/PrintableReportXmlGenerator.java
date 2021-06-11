package com.constellio.app.modules.rm.services.reports.xml.legacy;

import com.constellio.app.api.extensions.params.AddFieldsInReportXMLParams;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.services.reports.xml.legacy.parameters.XmlReportGeneratorParameters;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.CDATA;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

// Use XMLDataSourceGeneratorFactory instead
@Deprecated
public class PrintableReportXmlGenerator extends AbstractXmlGenerator {

	static final public String EMPTY_METADATA_VALUE_TAG = "This will not appear on the final report";
	private User user;

	public static final String XML_LINKED_FOLDER_PREFIX = "linkedFolders_";

	public PrintableReportXmlGenerator(AppLayerFactory appLayerFactory, String collection,
									   XmlReportGeneratorParameters xmlGeneratorParameters, Locale locale,
									   String username) {
		super(appLayerFactory, collection, locale);
		this.xmlGeneratorParameters = xmlGeneratorParameters;
		this.user = appLayerFactory.getModelLayerFactory().newUserServices().getUserInCollection(username, collection);
	}

	@Override
	public String generateXML() {
		xmlGeneratorParameters.validateInputs();
		Document xmlDocument = new Document();
		Element xmlRoot = new Element(XML_ROOT_RECORD_ELEMENTS);
		xmlDocument.setRootElement(xmlRoot);
		XmlReportGeneratorParameters parameters = getXmlGeneratorParameters();
		for (int i = 0; i < parameters.getNumberOfCopies(); i++) {
			Iterator<Record> recordIterator;
			if (parameters.getQuery() == null) {
				recordIterator = asList(parameters.isParametersUsingIds() ?
										getRecordFromIds(parameters.getSchemaCode(), parameters.getIdsOfElement()) :
										parameters.getRecordsElements()).iterator();
			} else {
				recordIterator = getFactory().getModelLayerFactory().newSearchServices().recordsIteratorKeepingOrder(parameters.getQuery(), 200);
			}

			while (recordIterator.hasNext()) {
				Record recordElement = recordIterator.next();
				Element xmlSingularElement = new Element(XML_EACH_RECORD_ELEMENTS);
				xmlSingularElement.setAttribute("schemaCode", recordElement.getSchemaCode());

				Element xmlSingularElementMetadata = new Element(XML_METADATA_TAGS);

				getFactory().getExtensions().forCollection(getCollection()).addFieldsInReportXML(new AddFieldsInReportXMLParams(
						recordElement, xmlSingularElement, xmlSingularElementMetadata));

				//TODO create an extensions and call it here.

				//Add the Extensions to the metadatas elements.
				//                getFactory().getExtensions().forCollection(getCollection()).addFieldsInLabelXML(new AddFieldsInLabelXMLParams(
				//                        recordElement, xmlSingularElement, XMLMetadatasOfSingularElement));

				//Add additional informations
				xmlSingularElementMetadata.addContent(getAdditionalInformations(recordElement));

				MetadataList listOfMetadataOfTheCurrentElement = getListOfMetadataForElement(recordElement).onlyAccessibleOnRecordBy(user, recordElement);

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

	@Override
	public XmlReportGeneratorParameters getXmlGeneratorParameters() {
		return (XmlReportGeneratorParameters) this.xmlGeneratorParameters;
	}

	public List<Element> createMetadataTagFromLinkedFoldersMetadata(Metadata metadata, Record recordElement,
																	String collection,
																	AppLayerFactory factory) {

		List<String> folderIdList = recordElement.get(metadata);

		RecordServices recordServices = factory.getModelLayerFactory().newRecordServices();
		List<Record> listOfRecordReferencedByMetadata = filterListWithUserReadAccess(
				recordServices.getRecordsById(collection, folderIdList));
		List<Element> listOfMetadataTags = new ArrayList<>();

		for (int i = 0; i < listOfRecordReferencedByMetadata.size(); i++) {
			Record currentRecord = listOfRecordReferencedByMetadata.get(i);
			MetadataList listOfMetadataOfTheCurrentElement = getListOfMetadataForElement(currentRecord)
					.onlyAccessibleOnRecordBy(user, currentRecord);

			for (Metadata currentMetadata : listOfMetadataOfTheCurrentElement) {
				List<Element> metadataTags = createMetadataTagsFromMetadata(currentMetadata, currentRecord);

				for (Element currentElement : metadataTags) {
					currentElement.setName(XML_LINKED_FOLDER_PREFIX + i + "_" + currentElement.getName());
					Attribute code = currentElement.getAttribute("code");

					if (code != null && StringUtils.isNotBlank(code.getValue())) {
						code.setValue(XML_LINKED_FOLDER_PREFIX + i + "_" + code.getValue());
					}
				}

				listOfMetadataTags.addAll(metadataTags);
			}
		}

		return listOfMetadataTags;
	}

	public List<Record> filterListWithUserReadAccess(List<Record> recordListToFilter) {
		List<Record> recordList = new ArrayList<>();

		for (Record currentRecord : recordListToFilter) {
			if (user.hasReadAccess().on(currentRecord)) {
				recordList.add(currentRecord);
			}
		}

		return recordList;
	}

	/**
	 * Method that create Element(s) for a particular metadata and record element
	 *
	 * @param metadata      metadata
	 * @param recordElement record
	 * @return list of element to add
	 */
	public List<Element> createMetadataTagsFromMetadata(Metadata metadata, Record recordElement) {

		if (recordElement.getTypeCode().equals(RMTask.SCHEMA_TYPE) && metadata.getLocalCode().equals(RMTask.LINKED_FOLDERS)) {
			return createMetadataTagFromLinkedFoldersMetadata(metadata, recordElement, getCollection(), getFactory());
		}

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

	/**
	 * Method that return all the record of each given ids.
	 *
	 * @param schemaType
	 * @param ids
	 * @return
	 */
	private Record[] getRecordFromIds(String schemaType, final List<String> ids) {
		List<Record> allRecords = new ArrayList<>();
		SearchServices searchServices = getFactory().getModelLayerFactory().newSearchServices();
		MetadataSchemasManager metadataSchemasManager = getFactory().getModelLayerFactory().getMetadataSchemasManager();
		List<List<String>> splittedIds = getChunkList(ids, 1000);
		for (List<String> idChunk : splittedIds) {
			LogicalSearchCondition condition = from(
					metadataSchemasManager.getSchemaTypes(getCollection()).getSchemaType(schemaType)).where(Schemas.IDENTIFIER)
					.isIn(idChunk);
			List<Record> recordChunk = searchServices.search(new LogicalSearchQuery(condition));
			allRecords.addAll(recordChunk);
		}
		Collections.sort(allRecords, new Comparator<Record>() {
			@Override
			public int compare(Record o1, Record o2) {
				Integer indexOfo1 = ids.indexOf(o1.getId());
				Integer indexOfo2 = ids.indexOf(o2.getId());
				return indexOfo1.compareTo(indexOfo2);
			}
		});
		return allRecords.toArray(new Record[0]);
	}

	private <T> List<List<T>> getChunkList(List<T> largeList, int chunkSize) {
		List<List<T>> chunkList = new ArrayList<>();
		for (int i = 0; i < largeList.size(); i += chunkSize) {
			chunkList.add(largeList.subList(i, i + chunkSize >= largeList.size() ? largeList.size() : i + chunkSize));
		}
		return chunkList;
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
	 * Method that format a path to make it pretty
	 * format:  folder01 > folder02 > document1
	 *
	 * @param recordElement
	 * @return
	 */
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
