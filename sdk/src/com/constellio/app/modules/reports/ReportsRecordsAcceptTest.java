package com.constellio.app.modules.reports;

import com.constellio.app.modules.reports.wrapper.Printable;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.reports.ReportField;
import com.constellio.app.modules.rm.services.reports.ReportUtils;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.AbstractConstellioTest;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Nicolas D'amours & Charles Blanchette on 2017-01-16.
 */
public class ReportsRecordsAcceptTest extends ConstellioTest {
    RMSchemasRecordsServices rm;
    RMTestRecords records = new RMTestRecords(zeCollection);
    SearchServices ss;
    RecordServices recordServices;
    ReportUtils ru;
    ContentManager contentManager;

    @Before
    public void setUp() {
        prepareSystem(
                withZeCollection().withAllTestUsers().withRMTest(records).withConstellioRMModule().withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent().withDocumentsDecommissioningList()
        );
        rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
        ss = getModelLayerFactory().newSearchServices();
        recordServices = getModelLayerFactory().newRecordServices();
        ru = new ReportUtils(zeCollection, getAppLayerFactory(), records.getAlice().getUsername());
        contentManager = getModelLayerFactory().getContentManager();
    }

    @Test
    public void createReportLabelAndAssignData() throws Exception {
        String title = "test REcords 1";
        File file = getFile("Avery_5162_Vide.jasper");
        ContentVersionDataSummary upload = contentManager.upload(new FileInputStream(file.getAbsolutePath()), "Etiquette");
        Content c = contentManager.createFileSystem("test-" + LocalDate.now(), upload);
        Printable r = rm.newPrintableLabel();
        r.setTitle(title);
        r.setJasperFile(c);
        Transaction t = new Transaction();
        t.add(r);
        recordServices.execute(t);

        LogicalSearchCondition condition = from(rm.printable_label.schemaType()).where(rm.printable_label.title()).isEqualTo(title);
        Printable retour = rm.wrapPrintableLabel(ss.searchSingleResult(condition));
        assertThat(retour.getTitle()).isEqualTo(title);
        assertThat(retour.getJasperfile().getCurrentVersion().getHash()).isEqualTo(c.getCurrentVersion().getHash());
    }

    @Test
    public void createRecordsLabelAndAssignData() throws Exception {
        String title = "Test records 2";
        File file = getFile("Avery_5162_Vide.jasper");
        ContentVersionDataSummary upload = contentManager.upload(new FileInputStream(file.getAbsolutePath()), "Etiquette");
        Content c = contentManager.createFileSystem("test-" + LocalDate.now(), upload);
        Printable r = rm.newPrintableLabel();
        PrintableLabel printableLabel = rm.newPrintableLabel();
        printableLabel.setJasperFile(c);
        printableLabel.setTitle(title);

        Transaction t = new Transaction();
        t.add(printableLabel);
        recordServices.execute(t);

        LogicalSearchCondition condition = from(rm.printable_label.schemaType()).where(rm.printable_label.title()).isEqualTo(title);
        PrintableLabel retour = rm.wrapPrintableLabel(ss.searchSingleResult(condition));
        assertThat(retour.getJasperfile().getCurrentVersion().getHash()).isEqualTo(c.getCurrentVersion().getHash());
        assertThat(retour.getTitle()).isEqualTo(title);
    }

    @Test
    public void testConvertFoldersToXML() throws Exception {
        SAXBuilder builder = new SAXBuilder();
        String xmlWithAllFolders = ru.convertFolderToXML(new ReportField(rm.folder.title().getType(), rm.folder.title().getLabel(i18n.getLanguage()), Folder.SCHEMA_TYPE, rm.folder.title().getCode(), getAppLayerFactory()));
        ByteArrayInputStream stream = new ByteArrayInputStream(xmlWithAllFolders.getBytes("UTF-8"));
        Document document = builder.build(stream);
        List<Element> folders = document.getRootElement().getChildren();

        List<Field> fields = Arrays.asList(records.getClass().getDeclaredFields());
        int compteur = 0;
        for (Field f : fields) {
            if (f.getName().matches("folder_([ABC](\\d){1,2})")) compteur++;
        }
        assertThat(folders.size()).isEqualTo(compteur);
        assertThat(folders.get(0).getChild("metadatas").getChild(rm.folder.title().getLabel(i18n.getLanguage()).toLowerCase()).getValue()).isEqualTo(records.getFolder_A01().getTitle());

        String xmlWithNullParameter = ru.convertFolderToXML(null);
        ByteArrayInputStream stream2 = new ByteArrayInputStream(xmlWithNullParameter.getBytes("UTF-8"));
        Document document2 = builder.build(stream2);
        Element element = (Element) document2.getRootElement().getChildren().get(0);
        assertThat(element.getChild("metadatas").getChild("ref_" + rm.folder.category().getCode().replace("_default", "") + "_code").getValue()).isEqualTo(records.getCategory_X110().getCode());
    }

    @Test
    public void testConvertFoldersWithIdentifierToXML() throws Exception {
        SAXBuilder builder = new SAXBuilder();
        ReportField refCategory = new ReportField(rm.folder.category().getType(), rm.folder.category().getLabel(i18n.getLanguage()), Folder.SCHEMA_TYPE, rm.folder.category().getCode(), getAppLayerFactory());
        String xmlSeul = ru.convertFolderWithIdentifierToXML(records.folder_A80,
                refCategory,
                new ReportField(rm.folder.title().getType(), rm.folder.title().getLabel(i18n.getLanguage()), Folder.SCHEMA_TYPE, rm.folder.title().getCode(), getAppLayerFactory()));
        ByteArrayInputStream stream = new ByteArrayInputStream(xmlSeul.getBytes("UTF-8"));
        Document document = builder.build(stream);
        Element meta = ((Element) document.getRootElement().getChildren().get(0)).getChild("metadatas");
        assertThat(meta.getChild("ref_" + rm.folder.category().getCode().replace("_default", "") + "_title").getValue()).isEqualTo(records.getCategory_X110().getTitle());
        assertThat(meta.getChild(ReportUtils.escapeForXmlTag(rm.folder.title().getLabel(i18n.getLanguage()).toLowerCase())).getValue()).isEqualTo(records.getFolder_A80().getTitle());

        String xmlWithMulipleIds = ru.convertFolderWithIdentifierToXML(
                Arrays.asList(records.folder_A05, records.folder_A06),
                new ReportField(rm.folder.title().getType(), rm.folder.title().getLabel(i18n.getLanguage()), Folder.SCHEMA_TYPE, rm.folder.title().getCode(), getAppLayerFactory()),
                new ReportField(Schemas.IDENTIFIER.getType(), Schemas.IDENTIFIER.getLabel(i18n.getLanguage()), Folder.SCHEMA_TYPE, Schemas.IDENTIFIER.getCode(), getAppLayerFactory()));
        ByteArrayInputStream streamWithMultiple = new ByteArrayInputStream(xmlWithMulipleIds.getBytes("UTF-8"));
        Document docWithMultiple = builder.build(streamWithMultiple);
        List<Element> XmlMuliple = docWithMultiple.getRootElement().getChildren();

        String tst = ru.convertFolderWithIdentifierToXML(records.folder_A05, null);

        assertThat(XmlMuliple.size()).isEqualTo(2);
        assertThat(XmlMuliple.get(0).getChild("metadatas").getChild(rm.folder.title().getLabel(i18n.getLanguage()).toLowerCase()).getValue()).isEqualTo(records.getFolder_A05().getTitle());
        assertThat(XmlMuliple.get(1).getChild("metadatas").getChild(rm.folder.title().getLabel(i18n.getLanguage()).toLowerCase()).getValue()).isEqualTo(records.getFolder_A06().getTitle());
        String Null = null;
        try {
            String xmlNull = ru.convertFolderWithIdentifierToXML(Null, new ReportField(rm.folder.title().getType(), rm.folder.title().getLabel(i18n.getLanguage()), Folder.SCHEMA_TYPE, rm.folder.title().getCode(), getAppLayerFactory()));
            fail();
        } catch (Exception e) {
        }

    }

    @Test
    public void testConvertContainerToXML() throws Exception {
        SAXBuilder builder = new SAXBuilder();
        String xmlWithAllContainers = ru.convertContainerToXML(new ReportField(rm.containerRecord.title().getType(), rm.containerRecord.title().getLabel(i18n.getLanguage()), ContainerRecord.SCHEMA_TYPE, rm.containerRecord.title().getCode(), getAppLayerFactory()));
        ByteArrayInputStream stream = new ByteArrayInputStream(xmlWithAllContainers.getBytes("UTF-8"));
        Document document = builder.build(stream);
        List<Element> containers = document.getRootElement().getChildren();

        List<Field> fields = Arrays.asList(records.getClass().getDeclaredFields());
        int compteur = 0;
        for (Field f : fields) {
            if (f.getName().matches("containerId_((bac)(\\d){1,2})")) compteur++;
        }
        assertThat(containers.size()).isEqualTo(compteur);
        assertThat(containers.get(0).getChild("metadatas").getChild(rm.containerRecord.title().getLabel(i18n.getLanguage()).toLowerCase()).getValue()).isEqualTo(records.getContainerBac01().getTitle());

        String xmlWithNullParameter = ru.convertContainerToXML(null);
        ByteArrayInputStream stream2 = new ByteArrayInputStream(xmlWithNullParameter.getBytes("UTF-8"));
        Document document2 = builder.build(stream2);
        Element element = (Element) document2.getRootElement().getChildren().get(0);
        assertThat(element.getChild("metadatas").getChildren().size()).isGreaterThanOrEqualTo(rm.containerRecord.schema().getMetadatas().size());
        assertThat(element.getChild("metadatas").getChild(rm.containerRecord.title().getLocalCode()).getValue()).isEqualTo(records.getContainerBac01().getTitle());
    }

    @Test
    public void testConvertConteneurWithIdentifierToXML() throws Exception {
        SAXBuilder builder = new SAXBuilder();

        String contenu = ru.convertContainerWithIdentifierToXML(records.containerId_bac05, null);
        ByteArrayInputStream stream = new ByteArrayInputStream(contenu.getBytes("UTF-8"));
        Document document = builder.build(stream);
        Element meta = ((Element) document.getRootElement().getChildren().get(0)).getChild("metadatas");


        String contenuSeul = ru.convertContainerWithIdentifierToXML(
                records.containerId_bac08,
                new ReportField(rm.containerRecord.title().getType(), rm.containerRecord.title().getLabel(i18n.getLanguage()), ContainerRecord.SCHEMA_TYPE, rm.containerRecord.title().getCode(), getAppLayerFactory()),
                new ReportField(rm.containerRecord.capacity().getType(), rm.containerRecord.storageSpace().getLabel(i18n.getLanguage()), ContainerRecord.SCHEMA_TYPE, rm.containerRecord.storageSpace().getCode(), getAppLayerFactory()));
        ByteArrayInputStream stream1 = new ByteArrayInputStream(contenuSeul.getBytes("UTF-8"));
        Document doc1 = builder.build(stream1);
        Element meta1 = ((Element) doc1.getRootElement().getChildren().get(0)).getChild("metadatas");
        assertThat(meta1.getChild(rm.containerRecord.title().getLabel(i18n.getLanguage()).toLowerCase()).getValue()).isEqualTo(records.getContainerBac08().getTitle());
        assertThat(meta1.getChild(ReportUtils.escapeForXmlTag(rm.containerRecord.storageSpace().getLabel(i18n.getLanguage()))).getValue()).isEqualTo(records.getContainerBac08().getStorageSpace());

        String conteneurWithMultipleIds = ru.convertContainerWithIdentifierToXML(Arrays.asList(records.containerId_bac05, records.containerId_bac07),
                new ReportField(rm.containerRecord.title().getType(), rm.containerRecord.title().getLabel(i18n.getLanguage()), ContainerRecord.SCHEMA_TYPE, rm.containerRecord.title().getCode(), getAppLayerFactory()),
                new ReportField(rm.containerRecord.capacity().getType(), rm.containerRecord.capacity().getLabel(i18n.getLanguage()), ContainerRecord.SCHEMA_TYPE, rm.containerRecord.capacity().getCode(), getAppLayerFactory()));
        ByteArrayInputStream streamWithMultipleIds = new ByteArrayInputStream(conteneurWithMultipleIds.getBytes("UTF-8"));
        Document docWithMultiple = builder.build(streamWithMultipleIds);
        List<Element> meta2 = docWithMultiple.getRootElement().getChildren();
        assertThat(meta2.size()).isEqualTo(2);
        assertThat(meta2.get(0).getChild("metadatas").getChild(rm.containerRecord.title().getLocalCode()).getValue()).isEqualTo(records.getContainerBac05().getTitle());
        assertThat(meta2.get(1).getChild("metadatas").getChild(rm.containerRecord.title().getLocalCode()).getValue()).isEqualTo(records.getContainerBac07().getTitle());
    }

    @Test
    public void testConvertContainerToXMLWithStartingIndex() throws Exception {
        SAXBuilder builder = new SAXBuilder();

        ru.setStartingPosition(1);
        String contenu = ru.convertContainerWithIdentifierToXML(records.containerId_bac05, null);
        ByteArrayInputStream stream = new ByteArrayInputStream(contenu.getBytes("UTF-8"));
        Document document = builder.build(stream);
        Element meta = ((Element) document.getRootElement().getChildren().get(0)).getChild("metadatas");
        assertThat(meta.getChildren()).isNullOrEmpty();
    }

    @Test
    public void testConvertFolderToXMLWithStartingIndex() throws Exception {
        SAXBuilder builder = new SAXBuilder();

        ru.setStartingPosition(5);
        String contenu = ru.convertFolderWithIdentifierToXML(records.containerId_bac05, null);
        ByteArrayInputStream stream = new ByteArrayInputStream(contenu.getBytes("UTF-8"));
        Document document = builder.build(stream);
        Element meta = ((Element) document.getRootElement().getChildren().get(0)).getChild("metadatas");
        assertThat(meta.getChildren()).isNullOrEmpty();
        meta = ((Element) document.getRootElement().getChildren().get(1)).getChild("metadatas");
        assertThat(meta.getChildren()).isNullOrEmpty();
        meta = ((Element) document.getRootElement().getChildren().get(2)).getChild("metadatas");
        assertThat(meta.getChildren()).isNullOrEmpty();
        meta = ((Element) document.getRootElement().getChildren().get(3)).getChild("metadatas");
        assertThat(meta.getChildren()).isNullOrEmpty();
        meta = ((Element) document.getRootElement().getChildren().get(4)).getChild("metadatas");
        assertThat(meta.getChildren()).isNullOrEmpty();
    }

    @Test
    @InDevelopmentTest
    public void UseCompiledJasperFileAndXmlToCreatePDF() throws Exception {
        String xml = ru.convertContainerToXML(null);
        ru.createPDFFromXmlAndJasperFile(xml, new File("C:\\Users\\Marco\\Desktop\\Avery_5159_Container.jasper"), "test");
    }

    @Test
    @InDevelopmentTest
    public void createNewXmlWithModifiedReference() throws Exception {
        String xml = ru.convertContainerWithIdentifierToXML(records.containerId_bac08, null);
        System.out.println(records.getCategory_X110().getCode());
        System.out.println(records.getCategory_X110().getTitle());
        System.out.println(xml);
    }

    private static File getFile(String name) {
        File resourcesDir = AbstractConstellioTest.getResourcesDir();
        String pathInResourcesDir =
                ReportsRecordsAcceptTest.class.getName().replace(".", File.separator) + File.separator + name;
        return new File(resourcesDir, pathInResourcesDir);
    }
}
