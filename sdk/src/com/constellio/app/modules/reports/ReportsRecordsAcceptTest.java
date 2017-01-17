package com.constellio.app.modules.reports;

import com.constellio.app.modules.reports.wrapper.ReportConfig;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.reports.ReportUtils;
import com.constellio.app.modules.rm.wrappers.RMReport;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRXmlUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by Marco on 2017-01-16.
 */
public class ReportsRecordsAcceptTest extends ConstellioTest {
    RMSchemasRecordsServices rm;
    RMTestRecords records = new RMTestRecords(zeCollection);
    SearchServices ss;
    RecordServices recordServices;
    ReportUtils ru;

    @Before
    public void setUp() {
        prepareSystem(
                withZeCollection().withAllTestUsers().withRMTest(records).withConstellioRMModule().withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent().withDocumentsDecommissioningList()
        );
        rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
        ss = getModelLayerFactory().newSearchServices();
        recordServices = getModelLayerFactory().newRecordServices();
        ru = new ReportUtils(zeCollection, getAppLayerFactory());
    }

    @Test
    public void createReportLabelAndAssignData() throws Exception {
        String title = "test REcords 1";
        String file = "C:\\Users\\Marco\\JaspersoftWorkspace\\MyReports\\tes_etiquette_1.jasper";
        ReportConfig r = rm.newReportConfig();
        r.setTitle(title);
        r.setJasperFile(file);
        Transaction t = new Transaction();
        t.add(r);
        recordServices.execute(t);

        LogicalSearchCondition condition = from(rm.reportsrecords.schemaType()).where(rm.reportsrecords.title()).isEqualTo(title);
        ReportConfig retour = rm.wrapReportConfig(ss.searchSingleResult(condition));
        assertThat(retour.getTitle()).isEqualTo(title);
        assertThat(retour.getJasperfile()).isEqualTo(file);
    }

    @Test
    public void createRecordsLabelAndAssignData() throws Exception {
        String width = "100";
        String height = "50";

        String title = "Test records 2";
        String file = "C:\\Users\\Marco\\JaspersoftWorkspace\\MyReports\\tes_etiquette_1.jasper";

        RMReport rmReport = rm.newRMReport();
        rmReport.setJasperFile(file);
        rmReport.setTitle(title);
        rmReport.setWidth(width);
        rmReport.setHeight(height);

        Transaction t = new Transaction();
        t.add(rmReport);
        recordServices.execute(t);

        LogicalSearchCondition condition = from(rm.reportsrecords.schemaType()).where(rm.reportsrecords.title()).isEqualTo(title);
        RMReport retour = rm.wrapRMReport(ss.searchSingleResult(condition));
        assertThat(retour.getJasperfile()).isEqualTo(file);
        assertThat(retour.getWidth()).isEqualTo(width);
        assertThat(retour.getHeight()).isEqualTo(height);
        assertThat(retour.getTitle()).isEqualTo(title);
    }

    @Test
    public void testConvertFoldersToXML() throws Exception {
        SAXBuilder builder = new SAXBuilder();
        String xmlWithAllFolders = ru.convertFolderToXML(rm.folder.title().getCode());
        ByteArrayInputStream stream = new ByteArrayInputStream(xmlWithAllFolders.getBytes("UTF-8"));
        Document document = builder.build(stream);
        List<Element> folders = document.getRootElement().getChildren();

        List<Field> fields = Arrays.asList(records.getClass().getDeclaredFields());
        int compteur = 0;
        for (Field f : fields) {
            if (f.getName().matches("folder_([ABC](\\d){1,2})")) compteur++;
        }
        assertThat(folders.size()).isEqualTo(compteur);
        assertThat(folders.get(0).getChild("metadatas").getChild(rm.folder.title().getCode()).getValue()).isEqualTo(records.getFolder_A01().getTitle());

        String xmlWithNullParameter = ru.convertFolderToXML(null);
        System.out.println(xmlWithNullParameter);
        ByteArrayInputStream stream2 = new ByteArrayInputStream(xmlWithNullParameter.getBytes("UTF-8"));
        Document document2 = builder.build(stream2);
        Element element = (Element) document2.getRootElement().getChildren().get(0);
        assertThat(element.getChild("metadatas").getChildren().size()).isEqualTo(rm.folder.schema().getMetadatas().size());
        assertThat(element.getChild("metadatas").getChild(rm.folder.category().getCode()).getValue()).isEqualTo(records.getFolder_A01().getCategory());
    }

    @Test
    public void testConvertFoldersWithIdentifierToXML() throws Exception {
        SAXBuilder builder = new SAXBuilder();
        String xmlSeul = ru.convertFolderWithIdentifierToXML(records.folder_A80, rm.folder.category().getCode(), rm.folder.title().getCode());
        ByteArrayInputStream stream = new ByteArrayInputStream(xmlSeul.getBytes("UTF-8"));
        Document document = builder.build(stream);
        Element meta = ((Element) document.getRootElement().getChildren().get(0)).getChild("metadatas");
        assertThat(meta.getChild(rm.folder.category().getCode()).getValue()).isEqualTo(records.getFolder_A80().getCategory());
        assertThat(meta.getChild(rm.folder.title().getCode()).getValue()).isEqualTo(records.getFolder_A80().getTitle());

        String xmlWithMulipleIds = ru.convertFolderWithIdentifierToXML(Arrays.asList(records.folder_A05, records.folder_A06), rm.folder.title().getCode(), Schemas.IDENTIFIER.getCode());
        ByteArrayInputStream streamWithMultiple = new ByteArrayInputStream(xmlWithMulipleIds.getBytes("UTF-8"));
        Document docWithMultiple = builder.build(streamWithMultiple);
        List<Element> XmlMuliple = docWithMultiple.getRootElement().getChildren();

        assertThat(XmlMuliple.size()).isEqualTo(2);
        assertThat(XmlMuliple.get(0).getChild("metadatas").getChild(rm.folder.title().getCode()).getValue()).isEqualTo(records.getFolder_A05().getTitle());
        assertThat(XmlMuliple.get(1).getChild("metadatas").getChild(rm.folder.title().getCode()).getValue()).isEqualTo(records.getFolder_A06().getTitle());
        String Null = null;
        try {
            String xmlNull = ru.convertFolderWithIdentifierToXML(Null, rm.folder.title().getCode());
            System.out.println(xmlNull);
            fail();
        } catch (Exception e) {
        }

    }

    @Test
    public void testConvertContainerToXML() throws Exception {
        SAXBuilder builder = new SAXBuilder();
        String xmlWithAllContainers = ru.convertContainerToXML(rm.containerRecord.title().getCode());
        ByteArrayInputStream stream = new ByteArrayInputStream(xmlWithAllContainers.getBytes("UTF-8"));
        Document document = builder.build(stream);
        List<Element> containers = document.getRootElement().getChildren();

        List<Field> fields = Arrays.asList(records.getClass().getDeclaredFields());
        int compteur = 0;
        for (Field f : fields) {
            if (f.getName().matches("containerId_((bac)(\\d){1,2})")) compteur++;
        }
        assertThat(containers.size()).isEqualTo(compteur);
        assertThat(containers.get(0).getChild("metadatas").getChild(rm.containerRecord.title().getCode()).getValue()).isEqualTo(records.getContainerBac01().getTitle());

        String xmlWithNullParameter = ru.convertContainerToXML(null);
        System.out.println(xmlWithNullParameter);
        ByteArrayInputStream stream2 = new ByteArrayInputStream(xmlWithNullParameter.getBytes("UTF-8"));
        Document document2 = builder.build(stream2);
        Element element = (Element) document2.getRootElement().getChildren().get(0);
        assertThat(element.getChild("metadatas").getChildren().size()).isEqualTo(rm.containerRecord.schema().getMetadatas().size());
        assertThat(element.getChild("metadatas").getChild(rm.containerRecord.title().getCode()).getValue()).isEqualTo(records.getContainerBac01().getTitle());
    }

    @Test
    public void testConvertConteneurWithIdentifierToXML() throws Exception {
        SAXBuilder builder = new SAXBuilder();

        String contenu = ru.convertContainerWithIdentifierToXML(records.containerId_bac05, null);
        ByteArrayInputStream stream = new ByteArrayInputStream(contenu.getBytes("UTF-8"));
        Document document = builder.build(stream);
        Element meta = ((Element) document.getRootElement().getChildren().get(0)).getChild("metadatas");
        assertThat(meta.getChildren().size()).isEqualTo(rm.containerRecord.schema().getMetadatas().size());


        String contenuSeul = ru.convertContainerWithIdentifierToXML(records.containerId_bac09, rm.containerRecord.title().getCode(), rm.containerRecord.storageSpace().getCode());
        ByteArrayInputStream stream1 = new ByteArrayInputStream(contenuSeul.getBytes("UTF-8"));
        Document doc1 = builder.build(stream1);
        Element meta1 = ((Element) doc1.getRootElement().getChildren().get(0)).getChild("metadatas");
        assertThat(meta1.getChild(rm.containerRecord.title().getCode()).getValue()).isEqualTo(records.getContainerBac09().getTitle());
        assertThat(meta1.getChild(rm.containerRecord.storageSpace().getCode()).getValue()).isEqualTo(records.getContainerBac09().getStorageSpace());

        String conteneurWithMultipleIds = ru.convertContainerWithIdentifierToXML(Arrays.asList(records.containerId_bac05, records.containerId_bac07), rm.containerRecord.title().getCode(), rm.containerRecord.capacity().getCode());
        ByteArrayInputStream streamWithMultipleIds = new ByteArrayInputStream(conteneurWithMultipleIds.getBytes("UTF-8"));
        Document docWithMultiple = builder.build(streamWithMultipleIds);
        List<Element> meta2 = docWithMultiple.getRootElement().getChildren();
        assertThat(meta2.size()).isEqualTo(2);
        assertThat(meta2.get(0).getChild("metadatas").getChild(rm.containerRecord.title().getCode()).getValue()).isEqualTo(records.getContainerBac05().getTitle());
        assertThat(meta2.get(1).getChild("metadatas").getChild(rm.containerRecord.title().getCode()).getValue()).isEqualTo(records.getContainerBac07().getTitle());
    }

    @Test
    @InDevelopmentTest
    public void UseCompiledJasperFileAndXmlToCreatePDF() throws Exception {
        String xml = ru.convertFolderToXML(null);
        ru.createPDFFromXmlAndJasperFile(xml, new File("C:\\Users\\Marco\\Desktop\\Blank_A4.jasper"));
    }
}
