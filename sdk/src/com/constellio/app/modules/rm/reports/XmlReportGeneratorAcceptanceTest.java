package com.constellio.app.modules.rm.reports;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.reports.AbstractXmlGenerator;
import com.constellio.app.modules.rm.services.reports.parameters.XmlReportGeneratorParameters;
import com.constellio.app.modules.rm.services.reports.printableReport.PrintableReportXmlGenerator;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.assertj.core.groups.Tuple;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.IN_PROGRESS;
import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class XmlReportGeneratorAcceptanceTest extends ConstellioTest {
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);

	SAXBuilder saxBuilder;
	IOServices ioServices;

	MetadataSchemasManager metadataSchemasManager;
	TasksSchemasRecordsServices schemas;
	RecordServices recordServices;
	User user;

	@Before
	public void setUp() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent()
		);
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		saxBuilder = new SAXBuilder();
		this.ioServices = getModelLayerFactory().getIOServicesFactory().newIOServices();
		this.metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		schemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		this.recordServices = getModelLayerFactory().newRecordServices();

		Users users = new Users();
		UserServices userServices = getModelLayerFactory().newUserServices();
		users.setUp(userServices);

		user = users.adminIn(zeCollection);
	}

	@Test
	public void testXmlGeneratorIsNotNullAndReturnsCorrectValue() {
		int numberOfCopies = 20;
		XmlReportGeneratorParameters xmlReportGeneratorParameters = new XmlReportGeneratorParameters(numberOfCopies);
		PrintableReportXmlGenerator reportGenerator = new PrintableReportXmlGenerator(getAppLayerFactory(), zeCollection,
				xmlReportGeneratorParameters, Locale.FRENCH, user);
		assertThat(reportGenerator.getXmlGeneratorParameters()).isNotNull();
		assertThat(reportGenerator.getXmlGeneratorParameters().getNumberOfCopies()).isEqualTo(numberOfCopies);
	}

	@Test
	public void testThatCoreElementOfTheGeneratorAreAccessibleAndNotNull() {
		XmlReportGeneratorParameters xmlGeneratorParameters = new XmlReportGeneratorParameters(0);
		AbstractXmlGenerator reportGenerator = new PrintableReportXmlGenerator(getAppLayerFactory(), zeCollection,
				xmlGeneratorParameters, Locale.FRENCH, user);
		assertThat(reportGenerator.getFactory()).isNotNull();
		assertThat(reportGenerator.getFactory()).isEqualTo(getAppLayerFactory());

		assertThat(reportGenerator.getCollection()).isNotNull();
		assertThat(reportGenerator.getCollection()).isEqualTo(zeCollection);
	}

	@Test
	public void testThatElementAreNotNullWhenSettingThem() {
		XmlReportGeneratorParameters xmlReportGeneratorParameters = new XmlReportGeneratorParameters(1,
				records.getFolder_A01().getWrappedRecord());
		PrintableReportXmlGenerator reportGenerator = new PrintableReportXmlGenerator(getAppLayerFactory(), zeCollection,
				xmlReportGeneratorParameters, Locale.FRENCH, user);
		assertThat(reportGenerator.getXmlGeneratorParameters().getRecordsElements()).isNotEmpty();
		assertThat(reportGenerator.getXmlGeneratorParameters().getRecordsElements()[0])
				.isEqualTo(records.getFolder_A01().getWrappedRecord());
	}

	@Test
	public void testGenerateXmlWithFolder() {
		InputStream inputStream = null;
		XmlReportGeneratorParameters xmlReportGeneratorParameters = new XmlReportGeneratorParameters(1);
		xmlReportGeneratorParameters
				.setRecordsElements(records.getFolder_A11().getWrappedRecord(), records.getFolder_A01().getWrappedRecord(),
						records.getFolder_A08().getWrappedRecord());
		PrintableReportXmlGenerator printableReportXmlGenerator = new PrintableReportXmlGenerator(getAppLayerFactory(),
				zeCollection, xmlReportGeneratorParameters, Locale.FRENCH, user);

		try {
			String xmlString = printableReportXmlGenerator.generateXML();
			assertThat(xmlString).isNotEmpty();
			inputStream = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
			Document xmlDocument = saxBuilder.build(inputStream);
			// Assert that document contains correct tags;
			assertThat(xmlDocument.getRootElement().getName()).isEqualTo(AbstractXmlGenerator.XML_ROOT_RECORD_ELEMENTS);
			assertThat(xmlDocument.getRootElement().getChildren(AbstractXmlGenerator.XML_EACH_RECORD_ELEMENTS)).hasSize(3);

			Element xmlRecordElement = xmlDocument.getRootElement().getChild(AbstractXmlGenerator.XML_EACH_RECORD_ELEMENTS);
			List<Tuple> xmlRecordValues = new ArrayList<>();
			for (Object ob : xmlRecordElement.getChild(AbstractXmlGenerator.XML_METADATA_TAGS).getChildren()) {
				Element element = (Element) ob;
				xmlRecordValues.add(tuple(element.getName(), element.getText()));
			}
			List<Tuple> listOfMetadataInFolder = new ArrayList<>();
			for (Metadata metadata : metadataSchemasManager.getSchemaOf(records.getFolder_A11().getWrappedRecord())
					.getMetadatas()) {
				List<Element> elementOfMetadata = printableReportXmlGenerator
						.createMetadataTagsFromMetadata(metadata, records.getFolder_A11().getWrappedRecord());
				for (Element element : elementOfMetadata) {
					listOfMetadataInFolder.add(tuple(element.getName(), element.getText()));
				}
			}
			assertThat(xmlRecordValues).contains(listOfMetadataInFolder.toArray(new Tuple[0]));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		} finally {
			ioServices.closeQuietly(inputStream);
		}

	}

	@Test
	public void testGenerateXmlWithDocument() {
		InputStream inputStream = null;
		XmlReportGeneratorParameters xmlReportGeneratorParameters = new XmlReportGeneratorParameters(1);
		xmlReportGeneratorParameters.setRecordsElements(records.getDocumentWithContent_A19().getWrappedRecord(),
				records.getDocumentWithContent_A79().getWrappedRecord(), records.getDocumentWithContent_B33().getWrappedRecord());
		PrintableReportXmlGenerator printableReportXmlGenerator = new PrintableReportXmlGenerator(getAppLayerFactory(),
				zeCollection, xmlReportGeneratorParameters, Locale.FRENCH, user);

		try {
			String xmlString = printableReportXmlGenerator.generateXML();
			assertThat(xmlString).isNotEmpty();
			inputStream = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
			Document xmlDocument = saxBuilder.build(inputStream);
			// Assert that document contains correct tags;
			assertThat(xmlDocument.getRootElement().getName()).isEqualTo(AbstractXmlGenerator.XML_ROOT_RECORD_ELEMENTS);
			assertThat(xmlDocument.getRootElement().getChildren(AbstractXmlGenerator.XML_EACH_RECORD_ELEMENTS)).hasSize(3);

			Element xmlRecordElement = xmlDocument.getRootElement().getChild(AbstractXmlGenerator.XML_EACH_RECORD_ELEMENTS);
			List<Tuple> xmlRecordValues = new ArrayList<>();
			for (Object ob : xmlRecordElement.getChild(AbstractXmlGenerator.XML_METADATA_TAGS).getChildren()) {
				Element element = (Element) ob;
				//exclude content because it changes every time,
				if (!element.getName().equals("content")) {
					xmlRecordValues.add(tuple(element.getName(), element.getText()));
				}
			}
			List<Tuple> listOfMetadataInFolder = new ArrayList<>();
			for (Metadata metadata : metadataSchemasManager.getSchemaOf(records.getDocumentWithContent_A19().getWrappedRecord())
					.getMetadatas()) {
				List<Element> elementOfMetadata = printableReportXmlGenerator
						.createMetadataTagsFromMetadata(metadata, records.getDocumentWithContent_A19().getWrappedRecord());
				for (Element element : elementOfMetadata) {
					//exclude content because it changes every time,
					if (!element.getName().equals("content")) {
						listOfMetadataInFolder.add(tuple(element.getName(), element.getText()));
					}
				}
			}
			assertThat(xmlRecordValues).contains(listOfMetadataInFolder.toArray(new Tuple[0]));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		} finally {
			ioServices.closeQuietly(inputStream);
		}
	}

	@Test
	public void testGenerateXmlWithTask() {
		InputStream inputStream = null;

		TaskStatus zeStatus = schemas.newTaskStatus().setCode("zeStatus").setStatusType(IN_PROGRESS).setTitle("status title");

		Task zeTask = schemas.newTask();
		zeTask.setCreatedOn(new LocalDateTime().minusDays(10));
		zeTask.setDueDate(new LocalDate().minusDays(1));
		zeTask.setEndDate(new LocalDate());
		zeTask.setStatus(zeStatus.getId());
		zeTask.setAssignee(records.getAdmin().getId());
		zeTask.setDescription("Ceci est une description de test");
		zeTask.setTitle("zeTask");
		zeTask.setType(records.taskTypeForm());
		Task zeSecondTask = schemas.newTask().setStatus(zeStatus.getId()).setDueDate(new LocalDate().plusDays(10));
		zeSecondTask.setTitle("zeSecondTask");
		zeSecondTask.setDescription("description of zeSecondTask");
		zeSecondTask.setAssignee(records.getChuckNorris().getId());
		zeSecondTask.setProgressPercentage(0D);
		Task zeThirdTask = schemas.newTask().setStatus(zeStatus.getId());
		zeThirdTask.setProgressPercentage(52D);
		zeThirdTask.setTitle("zeThirdTask");
		zeThirdTask.setAssignee(records.getDakota_managerInA_userInB().getId());
		zeThirdTask.setDescription("zeThirdTask description");
		zeThirdTask.setDueDate(new LocalDate().plusDays(30));
		XmlReportGeneratorParameters xmlReportGeneratorParameters = new XmlReportGeneratorParameters(1);
		xmlReportGeneratorParameters
				.setRecordsElements(zeTask.getWrappedRecord(), zeSecondTask.getWrappedRecord(), zeThirdTask.getWrappedRecord());
		PrintableReportXmlGenerator printableReportXmlGenerator = new PrintableReportXmlGenerator(getAppLayerFactory(),
				zeCollection, xmlReportGeneratorParameters, Locale.FRENCH, user);
		try {
			String xmlString = printableReportXmlGenerator.generateXML();
			assertThat(xmlString).isNotEmpty();
			inputStream = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
			Document xmlDocument = saxBuilder.build(inputStream);
			// Assert that document contains correct tags;
			assertThat(xmlDocument.getRootElement().getName()).isEqualTo(AbstractXmlGenerator.XML_ROOT_RECORD_ELEMENTS);
			assertThat(xmlDocument.getRootElement().getChildren(AbstractXmlGenerator.XML_EACH_RECORD_ELEMENTS)).hasSize(3);

			Element xmlRecordElement = xmlDocument.getRootElement().getChild(AbstractXmlGenerator.XML_EACH_RECORD_ELEMENTS);
			List<Tuple> xmlRecordValues = new ArrayList<>();
			for (Object ob : xmlRecordElement.getChild(AbstractXmlGenerator.XML_METADATA_TAGS).getChildren()) {
				Element element = (Element) ob;
				xmlRecordValues.add(tuple(element.getName(), element.getText()));
			}
			List<Tuple> listOfMetadataInFolder = new ArrayList<>();
			for (Metadata metadata : metadataSchemasManager.getSchemaOf(zeTask.getWrappedRecord()).getMetadatas()) {
				List<Element> elementOfMetadata = printableReportXmlGenerator
						.createMetadataTagsFromMetadata(metadata, zeTask.getWrappedRecord());
				for (Element element : elementOfMetadata) {
					listOfMetadataInFolder.add(tuple(element.getName(), element.getText()));
				}
			}
			assertThat(xmlRecordValues).contains(listOfMetadataInFolder.toArray(new Tuple[0]));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		} finally {
			ioServices.closeQuietly(inputStream);
		}
	}

	@Test
	public void checkIfMultivalueMetadataReturnsMutlivalueSeparatedByComa() {
		InputStream inputStream = null;
		XmlReportGeneratorParameters xmlReportGeneratorParameters = new XmlReportGeneratorParameters(1);
		xmlReportGeneratorParameters.setRecordsElements(records.getFolder_C30().getWrappedRecord());
		xmlReportGeneratorParameters.markAsTestXml();
		PrintableReportXmlGenerator printableReportXmlGenerator = new PrintableReportXmlGenerator(getAppLayerFactory(),
				zeCollection, xmlReportGeneratorParameters, Locale.FRENCH, user);
		try {
			String xmlString = printableReportXmlGenerator.generateXML();
			assertThat(xmlString).isNotEmpty();
			inputStream = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
			Document xmlDocument = saxBuilder.build(inputStream);

			Element xmlRecordElement = xmlDocument.getRootElement().getChild(AbstractXmlGenerator.XML_EACH_RECORD_ELEMENTS)
					.getChild("metadatas");
			assertThat(xmlRecordElement.getChild("ref_folder_mediumTypes_code").getText()).contains(",");
		} catch (Exception e) {
			e.printStackTrace();
			//fail();
		} finally {
			ioServices.closeQuietly(inputStream);
		}
	}

	@Test
	public void checkIfTestXmlGeneratorAreReturningEveryTagsFilled() {
		InputStream inputStream = null;
		XmlReportGeneratorParameters xmlReportGeneratorParameters = new XmlReportGeneratorParameters(1);
		xmlReportGeneratorParameters.setRecordsElements(records.getFolder_C30().getWrappedRecord());
		xmlReportGeneratorParameters.markAsTestXml();
		PrintableReportXmlGenerator printableReportXmlGenerator = new PrintableReportXmlGenerator(getAppLayerFactory(),
				zeCollection, xmlReportGeneratorParameters, Locale.FRENCH, user);
		try {
			String xmlString = printableReportXmlGenerator.generateXML();
			assertThat(xmlString).isNotEmpty();
			inputStream = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
			Document xmlDocument = saxBuilder.build(inputStream);

			Element xmlRecordElement = xmlDocument.getRootElement().getChild(AbstractXmlGenerator.XML_EACH_RECORD_ELEMENTS);
			for (Object ob : xmlRecordElement.getChild(AbstractXmlGenerator.XML_METADATA_TAGS).getChildren()) {
				Element element = (Element) ob;
				assertThat(element.getText()).isNotEmpty();
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		} finally {
			ioServices.closeQuietly(inputStream);
		}
	}

	@Test
	public void testForPathGeneration()
			throws Exception {
		XmlReportGeneratorParameters xmlReportGeneratorParameters = new XmlReportGeneratorParameters(1);
		xmlReportGeneratorParameters.setRecordsElements(records.getFolder_C30().getWrappedRecord());
		xmlReportGeneratorParameters.markAsTestXml();
		PrintableReportXmlGenerator printableReportXmlGenerator = new PrintableReportXmlGenerator(getAppLayerFactory(),
				zeCollection, xmlReportGeneratorParameters, Locale.FRENCH, user);

		assertThat(printableReportXmlGenerator.getPath(records.getFolder_C30().getWrappedRecord()))
				.isEqualTo("Xe category > X100 > X110 > Haricot");

		Transaction transaction = new Transaction();
		transaction.add(records.getFolder_A20().setParentFolder(records.getFolder_A12()));
		recordServices.execute(transaction);

		assertThat(printableReportXmlGenerator.getPath(records.getFolder_A20().getWrappedRecord()))
				.isEqualTo("Xe category > X100 > X110 > Castor > Chien");
	}
}
