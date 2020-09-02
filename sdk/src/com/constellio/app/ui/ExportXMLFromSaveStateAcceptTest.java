package com.constellio.app.ui;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.MainTestDefaultStart;
import com.constellio.sdk.tests.annotations.PreserveState;
import com.constellio.sdk.tests.annotations.UiTest;
import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

@UiTest
@MainTest
//@PreserveState(state = "/Users/francisbaril/Workspaces/saveStates/07-20-VSL.zip")
@PreserveState(state = "/Users/francisbaril/Workspaces/saveStates/instance-asma.zip", enabled = true)
public class ExportXMLFromSaveStateAcceptTest extends ConstellioTest {

	@Test
	@MainTestDefaultStart
	public void startApplicationWithSaveState()
			throws Exception {
		givenTransactionLogIsEnabled();

		File exportFolder = prepareExportFolder();
		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(saveState()).withPasswordsResetAndDisableLDAPSync();

		for (String collection : getModelLayerFactory().getCollectionsListManager().getCollections()) {
			exportCollectionData(exportFolder, collection);
		}

	}

	private void exportCollectionData(File exportFolder, String collection)
			throws Exception {
		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		File exportCollectionFolder = new File(exportFolder, collection);
		exportCollectionFolder.mkdirs();
		for (MetadataSchemaType type : types.getSchemaTypes()) {
			File exportFile = new File(exportCollectionFolder, type.getCode() + ".xml");
			exportTypeData(type, exportFile);
		}
	}

	private void exportTypeData(MetadataSchemaType type, File exportFile)
			throws Exception {

		Element root = new Element("records");
		Document xmlDocument = new Document(root);

		buildXMLDocument(type, root);

		XMLOutputter xmlOutput = new XMLOutputter();
		xmlOutput.setFormat(Format.getPrettyFormat());
		Writer writer = new BufferedWriter(new FileWriter(exportFile));
		xmlOutput.output(xmlDocument, writer);

	}

	private void buildXMLDocument(MetadataSchemaType type, Element root) {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		Iterator<Record> recordsIterator = searchServices.recordsIterator(new LogicalSearchQuery(from(type).returnAll()));
		while (recordsIterator.hasNext()) {
			Record record = recordsIterator.next();
			MetadataSchema schema = type.getSchema(record.getSchemaCode());
			Element recordElement = new Element("record");
			buildRecordElement(schema, record, recordElement);
			root.addContent(recordElement);
		}
	}

	private void buildRecordElement(MetadataSchema schema, Record record, Element recordElement) {
		recordElement.setAttribute("id", record.getId());

		for (Metadata metadata : schema.getMetadatas().onlyManuals()) {
			if (isImported(metadata)) {
				Element metadataElement = new Element(metadata.getLocalCode());
				boolean hasValue = false;
				if (metadata.isMultivalue()) {
					metadataElement.setAttribute("multivalue", "true");
					List<Object> values = record.getList(metadata);
					for (Object value : values) {
						Element itemElement = new Element("item");
						buildRecordMetadataElement(metadata, value, itemElement);
						metadataElement.addContent(itemElement);
						hasValue = true;
					}
				} else {
					Object value = record.get(metadata);
					if (value != null) {
						buildRecordMetadataElement(metadata, value, metadataElement);
						hasValue = true;
					}
				}

				if (hasValue) {
					recordElement.addContent(metadataElement);
				}
			}
		}
	}

	private boolean isImported(Metadata metadata) {
		return !metadata.isSystemReserved() && !metadata.getCode().equals("category_default_retentionRules");
		//return !metadata.isSameLocalCodeThanAny(CREATED_ON, MODIFIED_ON, CREATED_BY, MODIFIED_BY, IDENTIFIER);
	}

	private void buildRecordMetadataElement(Metadata metadata, Object value, Element itemElement) {
		itemElement.setText(value.toString());
	}

	private File saveState() {
		return new File(getClass().getAnnotation(PreserveState.class).state());
	}

	private File prepareExportFolder() {
		File saveState = saveState();
		File exportFolder = new File(saveState.getParentFile(), saveState.getName().replace(".zip", ""));
		if (exportFolder.exists()) {
			try {
				FileUtils.deleteDirectory(exportFolder);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		exportFolder.mkdirs();

		return exportFolder;
	}
}
