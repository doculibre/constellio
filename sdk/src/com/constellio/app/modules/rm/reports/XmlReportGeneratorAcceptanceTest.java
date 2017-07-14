package com.constellio.app.modules.rm.reports;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.reports.XmlGenerator;
import com.constellio.app.modules.rm.services.reports.XmlReportGenerator;
import com.constellio.app.modules.rm.services.reports.parameters.XmlGeneratorParameters;
import com.constellio.app.modules.rm.services.reports.parameters.XmlReportGeneratorParameters;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.TaskStatusType;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import jxl.demo.XML;
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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.IN_PROGRESS;
import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Created by Marco on 2017-07-05.
 */
public class XmlReportGeneratorAcceptanceTest extends ConstellioTest {
    RMSchemasRecordsServices rm;
    RMTestRecords records = new RMTestRecords(zeCollection);

    SAXBuilder saxBuilder;
    IOServices ioServices;

    MetadataSchemasManager metadataSchemasManager;
    TasksSchemasRecordsServices schemas;
    RecordServices recordServices;

    @Before
    public void setUp() {
        prepareSystem(
                withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent()
        );
        rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
        saxBuilder = new SAXBuilder();
        this.ioServices = getModelLayerFactory().getIOServicesFactory().newIOServices();
        this.metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
        schemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
        this.recordServices = getModelLayerFactory().newRecordServices();
    }

    @Test
    public void testXmlGeneratorIsNotNullAndReturnsCorrectValue() {
        int numberOfCopies = 20;
        XmlReportGeneratorParameters xmlReportGeneratorParameters = new XmlReportGeneratorParameters(numberOfCopies);
        XmlReportGenerator reportGenerator = new XmlReportGenerator(getAppLayerFactory(), zeCollection, xmlReportGeneratorParameters);
        assertThat(reportGenerator.getXmlGeneratorParameters()).isNotNull();
        assertThat(reportGenerator.getXmlGeneratorParameters().getNumberOfCopies()).isEqualTo(numberOfCopies);
    }

    @Test
    public void testThatCoreElementOfTheGeneratorAreAccessibleAndNotNull() {
        XmlReportGeneratorParameters xmlGeneratorParameters = new XmlReportGeneratorParameters(0);
        XmlGenerator reportGenerator = new XmlReportGenerator(getAppLayerFactory(), zeCollection, xmlGeneratorParameters);
        assertThat(reportGenerator.getFactory()).isNotNull();
        assertThat(reportGenerator.getFactory()).isEqualTo(getAppLayerFactory());

        assertThat(reportGenerator.getCollection()).isNotNull();
        assertThat(reportGenerator.getCollection()).isEqualTo(zeCollection);
    }

    @Test
    public void testThatElementAreNotNullWhenSettingThem() {
        XmlReportGeneratorParameters xmlReportGeneratorParameters = new XmlReportGeneratorParameters(1, records.getFolder_A01().getWrappedRecord());
        XmlGenerator reportGenerator = new XmlReportGenerator(getAppLayerFactory(), zeCollection, xmlReportGeneratorParameters);
        assertThat(reportGenerator.getXmlGeneratorParameters().getRecordsElements()).isNotEmpty();
        assertThat(reportGenerator.getXmlGeneratorParameters().getRecordsElements()[0]).isEqualTo(records.getFolder_A01().getWrappedRecord());
    }

    @Test
    public void testGenerateXmlWithFolder() {
        InputStream inputStream = null;
        try{
            XmlReportGeneratorParameters xmlReportGeneratorParameters = new XmlReportGeneratorParameters(1);
            xmlReportGeneratorParameters.setRecordsElements(records.getFolder_A11().getWrappedRecord(), records.getFolder_A01().getWrappedRecord(), records.getFolder_A08().getWrappedRecord());
            XmlReportGenerator xmlReportGenerator = new XmlReportGenerator(getAppLayerFactory(), zeCollection, xmlReportGeneratorParameters);
            String xmlString = xmlReportGenerator.generateXML();
            inputStream = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
            Document xmlDocument = saxBuilder.build(inputStream);
            // Assert that document contains correct tags;
            assertThat(xmlDocument.getRootElement().getName()).isEqualTo(XmlGenerator.XML_ROOT_RECORD_ELEMENTS);
            assertThat(xmlDocument.getRootElement().getChildren(XmlGenerator.XML_EACH_RECORD_ELEMENTS)).hasSize(1);

            Element xmlRecordElement = xmlDocument.getRootElement().getChild(XmlGenerator.XML_EACH_RECORD_ELEMENTS);
            List<Tuple> xmlRecordValues = new ArrayList<>();
            for(Object ob : xmlRecordElement.getChild(XmlGenerator.XML_METADATA_TAGS).getChildren()) {
                Element element = (Element) ob;
                xmlRecordValues.add(tuple(element.getName(), element.getText()));
            }
            List<Tuple> listOfMetadataInFolder = new ArrayList<>();
            for(Metadata metadata : metadataSchemasManager.getSchemaOf(records.getFolder_A11().getWrappedRecord()).getMetadatas()) {
                List<Element> elementOfMetadata = xmlReportGenerator.createMetadataTagsFromMetadata(metadata, records.getFolder_A11().getWrappedRecord());
                for(Element element : elementOfMetadata) {
                    listOfMetadataInFolder.add(tuple(element.getName(), element.getText()));
                }
            }
            assertThat(xmlRecordValues).contains(listOfMetadataInFolder.toArray(new Tuple[0]));
        }catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            ioServices.closeQuietly(inputStream);
        }
    }

    @Test
    public void testGenerateXmlWithDocument() {
        InputStream inputStream = null;
        try{
            XmlReportGeneratorParameters xmlReportGeneratorParameters = new XmlReportGeneratorParameters(1);
            xmlReportGeneratorParameters.setRecordsElements(records.getDocumentWithContent_A19().getWrappedRecord(), records.getDocumentWithContent_A79().getWrappedRecord(), records.getDocumentWithContent_B33().getWrappedRecord());
            XmlReportGenerator xmlReportGenerator = new XmlReportGenerator(getAppLayerFactory(), zeCollection, xmlReportGeneratorParameters);
            String xmlString = xmlReportGenerator.generateXML();
            inputStream = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
            Document xmlDocument = saxBuilder.build(inputStream);
            // Assert that document contains correct tags;
            assertThat(xmlDocument.getRootElement().getName()).isEqualTo(XmlGenerator.XML_ROOT_RECORD_ELEMENTS);
            assertThat(xmlDocument.getRootElement().getChildren(XmlGenerator.XML_EACH_RECORD_ELEMENTS)).hasSize(1);

            Element xmlRecordElement = xmlDocument.getRootElement().getChild(XmlGenerator.XML_EACH_RECORD_ELEMENTS);
            List<Tuple> xmlRecordValues = new ArrayList<>();
            for(Object ob : xmlRecordElement.getChild(XmlGenerator.XML_METADATA_TAGS).getChildren()) {
                Element element = (Element) ob;
                xmlRecordValues.add(tuple(element.getName(), element.getText()));
            }
            List<Tuple> listOfMetadataInFolder = new ArrayList<>();
            for(Metadata metadata : metadataSchemasManager.getSchemaOf(records.getDocumentWithContent_A19().getWrappedRecord()).getMetadatas()) {
                List<Element> elementOfMetadata = xmlReportGenerator.createMetadataTagsFromMetadata(metadata, records.getDocumentWithContent_A19().getWrappedRecord());
                for(Element element : elementOfMetadata) {
                    listOfMetadataInFolder.add(tuple(element.getName(), element.getText()));
                }
            }
            assertThat(xmlRecordValues).contains(listOfMetadataInFolder.toArray(new Tuple[0]));
        }catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            ioServices.closeQuietly(inputStream);
        }
    }

    @Test
    public void testGenerateXmlWithTask() {
        InputStream inputStream = null;
        try{
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
            xmlReportGeneratorParameters.setRecordsElements(zeTask.getWrappedRecord(), zeSecondTask.getWrappedRecord(), zeThirdTask.getWrappedRecord());
            XmlReportGenerator xmlReportGenerator = new XmlReportGenerator(getAppLayerFactory(), zeCollection, xmlReportGeneratorParameters);
            String xmlString = xmlReportGenerator.generateXML();
            inputStream = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
            Document xmlDocument = saxBuilder.build(inputStream);
            // Assert that document contains correct tags;
            assertThat(xmlDocument.getRootElement().getName()).isEqualTo(XmlGenerator.XML_ROOT_RECORD_ELEMENTS);
            assertThat(xmlDocument.getRootElement().getChildren(XmlGenerator.XML_EACH_RECORD_ELEMENTS)).hasSize(1);

            Element xmlRecordElement = xmlDocument.getRootElement().getChild(XmlGenerator.XML_EACH_RECORD_ELEMENTS);
            List<Tuple> xmlRecordValues = new ArrayList<>();
            for(Object ob : xmlRecordElement.getChild(XmlGenerator.XML_METADATA_TAGS).getChildren()) {
                Element element = (Element) ob;
                xmlRecordValues.add(tuple(element.getName(), element.getText()));
            }
            List<Tuple> listOfMetadataInFolder = new ArrayList<>();
            for(Metadata metadata : metadataSchemasManager.getSchemaOf(zeTask.getWrappedRecord()).getMetadatas()) {
                List<Element> elementOfMetadata = xmlReportGenerator.createMetadataTagsFromMetadata(metadata, zeTask.getWrappedRecord());
                for(Element element : elementOfMetadata) {
                    listOfMetadataInFolder.add(tuple(element.getName(), element.getText()));
                }
            }
            assertThat(xmlRecordValues).contains(listOfMetadataInFolder.toArray(new Tuple[0]));
        }catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            ioServices.closeQuietly(inputStream);
        }
    }
}
