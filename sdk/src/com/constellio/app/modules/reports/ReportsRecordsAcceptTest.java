package com.constellio.app.modules.reports;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.services.reports.ReportXMLGeneratorV2;
import org.assertj.core.groups.Tuple;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.reports.wrapper.Printable;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.reports.ReportField;
import com.constellio.app.modules.rm.services.reports.ReportXMLGenerator;
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

/**
 * Created by Nicolas D'amours & Charles Blanchette on 2017-01-16.
 */
public class ReportsRecordsAcceptTest extends ConstellioTest {
    RMSchemasRecordsServices rm;
    RMTestRecords records = new RMTestRecords(zeCollection);
    SearchServices ss;
    RecordServices recordServices;
    ReportXMLGenerator reportXmlGenerator;
    ContentManager contentManager;

    @Before
    public void setUp() {
        prepareSystem(
                withZeCollection().withAllTestUsers().withRMTest(records).withConstellioRMModule()
                        .withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent().withDocumentsDecommissioningList()
        );
        rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
        ss = getModelLayerFactory().newSearchServices();
        recordServices = getModelLayerFactory().newRecordServices();
        reportXmlGenerator = new ReportXMLGenerator(zeCollection, getAppLayerFactory(), records.getAlice().getUsername());
        contentManager = getModelLayerFactory().getContentManager();
    }

    @Test
    public void createReportLabelAndAssignData()
            throws Exception {
        String title = "test REcords 1";
        File file = getFile("Avery_5162_Vide.jasper");
        ContentVersionDataSummary upload = contentManager.upload(new FileInputStream(file.getAbsolutePath()), "Etiquette")
                .getContentVersionDataSummary();
        Content c = contentManager.createFileSystem("test-" + LocalDate.now(), upload);
        Printable r = rm.newPrintableLabel();
        r.setTitle(title);
        r.setJasperFile(c);
        Transaction t = new Transaction();
        t.add(r);
        recordServices.execute(t);

        LogicalSearchCondition condition = from(rm.printable_label.schemaType()).where(rm.printable_label.title())
                .isEqualTo(title);
        Printable retour = rm.wrapPrintableLabel(ss.searchSingleResult(condition));
        assertThat(retour.getTitle()).isEqualTo(title);
        assertThat(retour.getJasperfile().getCurrentVersion().getHash()).isEqualTo(c.getCurrentVersion().getHash());
    }

    @Test
    public void createRecordsLabelAndAssignData()
            throws Exception {
        String title = "Test records 2";
        File file = getFile("Avery_5162_Vide.jasper");
        ContentVersionDataSummary upload = contentManager.upload(new FileInputStream(file.getAbsolutePath()), "Etiquette")
                .getContentVersionDataSummary();
        Content c = contentManager.createFileSystem("test-" + LocalDate.now(), upload);
        Printable r = rm.newPrintableLabel();
        PrintableLabel printableLabel = rm.newPrintableLabel();
        printableLabel.setJasperFile(c);
        printableLabel.setTitle(title);

        Transaction t = new Transaction();
        t.add(printableLabel);
        recordServices.execute(t);

        LogicalSearchCondition condition = from(rm.printable_label.schemaType()).where(rm.printable_label.title())
                .isEqualTo(title);
        PrintableLabel retour = rm.wrapPrintableLabel(ss.searchSingleResult(condition));
        assertThat(retour.getJasperfile().getCurrentVersion().getHash()).isEqualTo(c.getCurrentVersion().getHash());
        assertThat(retour.getTitle()).isEqualTo(title);
    }

    @Test
    public void testConvertFoldersToXML()
            throws Exception {
        SAXBuilder builder = new SAXBuilder();
        String xmlWithAllFolders = reportXmlGenerator.convertFolderToXML(
                new ReportField(rm.folder.title().getType(), rm.folder.title().getLabel(i18n.getLanguage()), Folder.SCHEMA_TYPE,
                        rm.folder.title().getCode(), getAppLayerFactory()));
        ByteArrayInputStream stream = new ByteArrayInputStream(xmlWithAllFolders.getBytes("UTF-8"));
        Document document = builder.build(stream);
        List<Element> folders = document.getRootElement().getChildren();

        List<Field> fields = asList(records.getClass().getDeclaredFields());
        int compteur = 0;
        for (Field f : fields) {
            if (f.getName().matches("folder_([ABC](\\d){1,2})"))
                compteur++;
        }
        assertThat(folders.size()).isEqualTo(compteur);
        assertThat(folders.get(0).getChild("metadatas").getChild(rm.folder.title().getLabel(i18n.getLanguage()).toLowerCase())
                .getValue()).isEqualTo(records.getFolder_A01().getTitle());

        String xmlWithNullParameter = reportXmlGenerator.convertFolderToXML(null);
        ByteArrayInputStream stream2 = new ByteArrayInputStream(xmlWithNullParameter.getBytes("UTF-8"));
        Document document2 = builder.build(stream2);
        Element element = (Element) document2.getRootElement().getChildren().get(0);
        assertThat(
                element.getChild("metadatas").getChild("ref_" + rm.folder.category().getCode().replace("_default", "") + "_code")
                        .getValue()).isEqualTo(records.getCategory_X110().getCode());
    }

    @Test
    public void testConvertFoldersWithIdentifierToXML()
            throws Exception {
        SAXBuilder builder = new SAXBuilder();
        ReportField refCategory = new ReportField(rm.folder.category().getType(),
                rm.folder.category().getLabel(i18n.getLanguage()), Folder.SCHEMA_TYPE, rm.folder.category().getCode(),
                getAppLayerFactory());
        String xmlSeul = reportXmlGenerator.convertFolderWithIdentifierToXML(records.folder_A80,
                refCategory,
                new ReportField(rm.folder.title().getType(), rm.folder.title().getLabel(i18n.getLanguage()), Folder.SCHEMA_TYPE,
                        rm.folder.title().getCode(), getAppLayerFactory()));
        ByteArrayInputStream stream = new ByteArrayInputStream(xmlSeul.getBytes("UTF-8"));
        Document document = builder.build(stream);
        Element meta = ((Element) document.getRootElement().getChildren().get(0)).getChild("metadatas");
        assertThat(meta.getChild("ref_" + rm.folder.category().getCode().replace("_default", "") + "_title").getValue())
                .isEqualTo(records.getCategory_X110().getTitle());
        assertThat(meta.getChild(ReportXMLGenerator.escapeForXmlTag(rm.folder.title().getLabel(i18n.getLanguage()).toLowerCase()))
                .getValue()).isEqualTo(records.getFolder_A80().getTitle());

        String xmlWithMulipleIds = reportXmlGenerator.convertFolderWithIdentifierToXML(
                asList(records.folder_A05, records.folder_A06),
                new ReportField(rm.folder.title().getType(), rm.folder.title().getLabel(i18n.getLanguage()), Folder.SCHEMA_TYPE,
                        rm.folder.title().getCode(), getAppLayerFactory()),
                new ReportField(Schemas.IDENTIFIER.getType(), Schemas.IDENTIFIER.getLabel(i18n.getLanguage()), Folder.SCHEMA_TYPE,
                        Schemas.IDENTIFIER.getCode(), getAppLayerFactory()));
        ByteArrayInputStream streamWithMultiple = new ByteArrayInputStream(xmlWithMulipleIds.getBytes("UTF-8"));
        Document docWithMultiple = builder.build(streamWithMultiple);
        List<Element> XmlMuliple = docWithMultiple.getRootElement().getChildren();

        String tst = reportXmlGenerator.convertFolderWithIdentifierToXML(records.folder_A05, null);

        assertThat(XmlMuliple.size()).isEqualTo(2);
        assertThat(XmlMuliple.get(0).getChild("metadatas").getChild(rm.folder.title().getLabel(i18n.getLanguage()).toLowerCase())
                .getValue()).isEqualTo(records.getFolder_A05().getTitle());
        assertThat(XmlMuliple.get(1).getChild("metadatas").getChild(rm.folder.title().getLabel(i18n.getLanguage()).toLowerCase())
                .getValue()).isEqualTo(records.getFolder_A06().getTitle());
        String Null = null;
        try {
            String xmlNull = reportXmlGenerator.convertFolderWithIdentifierToXML(Null,
                    new ReportField(rm.folder.title().getType(), rm.folder.title().getLabel(i18n.getLanguage()),
                            Folder.SCHEMA_TYPE, rm.folder.title().getCode(), getAppLayerFactory()));
            fail();
        } catch (Exception e) {
        }

    }

    @Test
    public void testConvertContainerToXML()
            throws Exception {
        SAXBuilder builder = new SAXBuilder();
        String xmlWithAllContainers = reportXmlGenerator.convertContainerWithIdentifierToXML(asList(records.getContainerBac01().getId()),
                new ReportField(rm.containerRecord.title().getType(), rm.containerRecord.title().getLabel(i18n.getLanguage()),
                        ContainerRecord.SCHEMA_TYPE, rm.containerRecord.title().getCode(), getAppLayerFactory()));

        System.out.println(xmlWithAllContainers);

        ByteArrayInputStream stream = new ByteArrayInputStream(xmlWithAllContainers.getBytes("UTF-8"));
        Document document = builder.build(stream);
        List<Element> containers = document.getRootElement().getChildren();

        List<Field> fields = asList(records.getClass().getDeclaredFields());
        int compteur = 0;
        for (Field f : fields) {
            if (f.getName().matches("containerId_((bac)(\\d){1,2})"))
                compteur++;
        }
        //assertThat(containers.size()).isEqualTo(compteur);
        assertThat(containers.get(0).getChild("metadatas")
                .getChild(ReportXMLGeneratorV2.getLabelOfMetadata(rm.containerRecord.title())).getValue())
                .isEqualTo(records.getContainerBac01().getTitle());

        String xmlWithNullParameter = reportXmlGenerator.convertContainerToXML(null);
        ByteArrayInputStream stream2 = new ByteArrayInputStream(xmlWithNullParameter.getBytes("UTF-8"));
        Document document2 = builder.build(stream2);
        Element element = (Element) document2.getRootElement().getChildren().get(0);
        assertThat(element.getChild("metadatas").getChildren().size())
                .isGreaterThanOrEqualTo(rm.containerRecord.schema().getMetadatas().size());
        assertThat(element.getChild("metadatas").getChild(rm.containerRecord.title().getLocalCode()).getValue())
                .isEqualTo(records.getContainerBac01().getTitle());
    }

    @Test
    public void testConvertConteneurWithIdentifierToXML()
            throws Exception {
        SAXBuilder builder = new SAXBuilder();
        ReportXMLGeneratorV2 reportXMLGeneratorV2 = new ReportXMLGeneratorV2(zeCollection, getAppLayerFactory(), records.getContainerBac05().getWrappedRecord());


        String contenu = reportXMLGeneratorV2.generateXML();
        ByteArrayInputStream stream = new ByteArrayInputStream(contenu.getBytes("UTF-8"));
        Document document = builder.build(stream);
        Element meta = ((Element) document.getRootElement().getChildren().get(0)).getChild("metadatas");

        ReportXMLGeneratorV2 reportXMLGeneratorV2ContenuSeul = new ReportXMLGeneratorV2(zeCollection, getAppLayerFactory(), records.getContainerBac08().getWrappedRecord());
        String contenuSeul = reportXMLGeneratorV2ContenuSeul.generateXML();
        ByteArrayInputStream stream1 = new ByteArrayInputStream(contenuSeul.getBytes("UTF-8"));
        Document doc1 = builder.build(stream1);
        Element meta1 = ((Element) doc1.getRootElement().getChildren().get(0)).getChild("metadatas");
        assertThat(meta1.getChild(ReportXMLGeneratorV2.getLabelOfMetadata(rm.containerRecord.title())).getValue())
                .isEqualTo(records.getContainerBac08().getTitle());
        assertThat(meta1.getChild(ReportXMLGeneratorV2.REFERENCE_PREFIX + rm.containerRecord.storageSpace().getCode().replace("_default_", "_") + "_code")
                .getValue()).isEqualTo(records.getContainerBac08().getStorageSpace());


        ReportXMLGeneratorV2 reportXMLGeneratorV2conteneurWithMultipleIds = new ReportXMLGeneratorV2(zeCollection, getAppLayerFactory(), records.getContainerBac05().getWrappedRecord(), records.getContainerBac07().getWrappedRecord());
        String conteneurWithMultipleIds = reportXMLGeneratorV2conteneurWithMultipleIds.generateXML();
        ByteArrayInputStream streamWithMultipleIds = new ByteArrayInputStream(conteneurWithMultipleIds.getBytes("UTF-8"));
        Document docWithMultiple = builder.build(streamWithMultipleIds);
        List<Element> meta2 = docWithMultiple.getRootElement().getChildren();
        assertThat(meta2.size()).isEqualTo(2);
        assertThat(meta2.get(0).getChild("metadatas").getChild(rm.containerRecord.title().getLocalCode()).getValue())
                .isEqualTo(records.getContainerBac05().getTitle());
        assertThat(meta2.get(1).getChild("metadatas").getChild(rm.containerRecord.title().getLocalCode()).getValue())
                .isEqualTo(records.getContainerBac07().getTitle());
    }

    @Test
    public void testConvertContainerToXMLWithStartingIndex()
            throws Exception {
        SAXBuilder builder = new SAXBuilder();
        ReportXMLGeneratorV2 reportXMLGeneratorV2 = new ReportXMLGeneratorV2(zeCollection, getAppLayerFactory(), records.getContainerBac05().getWrappedRecord());

        reportXMLGeneratorV2.setStartingPosition(1);
        String contenu = reportXMLGeneratorV2.generateXML();
        ByteArrayInputStream stream = new ByteArrayInputStream(contenu.getBytes("UTF-8"));
        Document document = builder.build(stream);
        Element meta = ((Element) document.getRootElement().getChildren().get(0)).getChild("metadatas");
        assertThat(meta.getChildren()).isNullOrEmpty();
    }

    @Test
    public void testConvertFolderToXMLWithStartingIndex()
            throws Exception {
        SAXBuilder builder = new SAXBuilder();
        ReportXMLGeneratorV2 reportXMLGeneratorV2 = new ReportXMLGeneratorV2(zeCollection, getAppLayerFactory(), records.getFolder_A01().getWrappedRecord());
        reportXMLGeneratorV2.setStartingPosition(5);
        String contenu = reportXMLGeneratorV2.generateXML();
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
    public void testCheckIfGeneratedXmlForContainerStaysTheSame() throws Exception {
        SAXBuilder builder = new SAXBuilder();

        // Removed metadata ModifedOn and CreatedOn since they change every time,
        Tuple[] expectedResults = new Tuple[]{
                tuple("collection_code", "zeCollection"),
                tuple("collection_title", "zeCollection"),
                tuple("extremeDates", ""),
                tuple("ref_containerRecord_administrativeUnits_code", "10A"),
                tuple("ref_containerRecord_administrativeUnits_title", "Unité 10-A"),
                tuple("allreferences", "boite22x22, S01-02, unitId_10a"),
                tuple("allremovedauths", ""),
                tuple("allauthorizations", ""),
                tuple("attachedancestors", ""),
                tuple("authorizations", ""),
                tuple("autocomplete", "10_a_02"),
                tuple("availablesize", ""),
                tuple("borrowdate", ""),
                tuple("borrowreturndate", ""),
                tuple("borrowed", ""),
                tuple("capacity", ""),
                tuple("comments", ""),
                tuple("completiondate", ""),
                tuple("decommissioningtype_code", "C"),
                tuple("decommissioningtype_title", "DEPOSIT"),
                tuple("deleted", ""),
                tuple("denytokens", ""),
                tuple("description", ""),
                tuple("detachedauthorizations", ""),
                tuple("erroronphysicaldeletion", ""),
                tuple("fillratioentered", ""),
                tuple("firstdepositreportdate", ""),
                tuple("firsttransferreportdate", ""),
                tuple("followers", ""),
                tuple("full", "Oui"),
                tuple("id", "bac05"),
                tuple("identifier", ""),
                tuple("inheritedauthorizations", ""),
                tuple("legacyidentifier", ""),
                tuple("linearsize", ""),
                tuple("linearsizeentered", ""),
                tuple("linearsizesum", "0.0"),
                tuple("localization", "Tablette 2-10_A_02"),
                tuple("logicallydeletedon", ""),
                tuple("manualtokens", ""),
                tuple("markedforpreviewconversion", ""),
                tuple("markedforreindexing", ""),
                tuple("migrationdataversion", "1.0"),
                tuple("parentpath", ""),
                tuple("path", "/admUnits/unitId_10/unitId_10a/bac05, /containers/S01/S01-02/bac05"),
                tuple("pathparts", "_LAST_unitId_10a, _LAST_S01-02, unitId_10, S01, S01-02, unitId_10a"),
                tuple("planifiedreturndate", ""),
                tuple("position", ""),
                tuple("principalpath", "/admUnits/unitId_10/unitId_10a/bac05"),
                tuple("realdepositdate", "2012-05-15"),
                tuple("realreturndate", ""),
                tuple("realtransferdate", "2008-10-31"),
                tuple("removedauthorizations", ""),
                tuple("schema", "containerRecord_default"),
                tuple("searchable", ""),
                tuple("sharedenytokens", ""),
                tuple("sharetokens", ""),
                tuple("ref_containerRecord_storageSpace_code", "S01-02"),
                tuple("ref_containerRecord_storageSpace_title", "Tablette 2"),
                tuple("temporaryidentifier", "10_A_02"),
                tuple("title", "10_A_02"),
                tuple("tokens", ""),
                tuple("ref_containerRecord_type_code", "B22x22"),
                tuple("ref_containerRecord_type_title", "Boite 22X22"),
                tuple("visibleintrees", "Non"),
                tuple("dispositionDate", "null"),
                tuple("decommissioningLabel", "Versement")
        };

        String contenu = reportXmlGenerator.convertContainerWithIdentifierToXML(records.containerId_bac05, null);
        ByteArrayInputStream stream = new ByteArrayInputStream(contenu.getBytes("UTF-8"));
        Document document = builder.build(stream);
        Element metadatasOfContainer = ((Element) document.getRootElement().getChildren().get(0)).getChild("metadatas");

        assertThat(getTupleOfNameAndValueForEachMetadata(metadatasOfContainer.getChildren())).contains(expectedResults);

        ReportXMLGeneratorV2 reportXMLGeneratorV2 = new ReportXMLGeneratorV2(zeCollection, getAppLayerFactory(), records.getContainerBac05().getWrappedRecord());
        String contenuFromV2 = reportXMLGeneratorV2.generateXML();
        ByteArrayInputStream streamV2 = new ByteArrayInputStream(contenuFromV2.getBytes("UTF-8"));
        Document documentV2 = builder.build(streamV2);
        Element metadatasOfContainerV2 = ((Element) documentV2.getRootElement().getChildren().get(0)).getChild("metadatas");

        assertThat(getTupleOfNameAndValueForEachMetadata(metadatasOfContainerV2.getChildren())).contains(expectedResults);
    }

    @Test
    public void testingStartingPositionInvalidIsFailing() {
        try {
            ReportXMLGeneratorV2 reportXMLGeneratorV2 = new ReportXMLGeneratorV2(zeCollection, getAppLayerFactory(), records.getContainerBac05().getWrappedRecord());
            reportXMLGeneratorV2.setStartingPosition(-1);
            reportXMLGeneratorV2.generateXML();
            fail();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    @Test
    public void testingNumberOfCopiesInvalidFailing() {
        try {
            ReportXMLGeneratorV2 reportXMLGeneratorV2 = new ReportXMLGeneratorV2(zeCollection, getAppLayerFactory(), records.getContainerBac05().getWrappedRecord());
            reportXMLGeneratorV2.setNumberOfCopies(0);
            reportXMLGeneratorV2.generateXML();
            fail();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testingElementNullFailing() {
        try {
            ReportXMLGeneratorV2 reportXMLGeneratorV2 = new ReportXMLGeneratorV2(zeCollection, getAppLayerFactory());
            reportXMLGeneratorV2.setElements(null);
            reportXMLGeneratorV2.generateXML();
            fail();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testingNullCollectionFailing() {
        try {
            ReportXMLGeneratorV2 reportXMLGeneratorV2 = new ReportXMLGeneratorV2(null, getAppLayerFactory(), records.getContainerBac07().getWrappedRecord());
            reportXMLGeneratorV2.generateXML();
            fail();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testingEmptyCollectionFailing() {
        try {
            ReportXMLGeneratorV2 reportXMLGeneratorV2 = new ReportXMLGeneratorV2("", getAppLayerFactory(), records.getContainerBac07().getWrappedRecord());
            reportXMLGeneratorV2.generateXML();
            fail();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testingNullFactoryFailing() {
        try {
            ReportXMLGeneratorV2 reportXMLGeneratorV2 = new ReportXMLGeneratorV2(zeCollection, null, records.getContainerBac07().getWrappedRecord());
            reportXMLGeneratorV2.generateXML();
            fail();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    public void UseCompiledJasperFileAndXmlToCreatePDF()
            throws Exception {
        String xml = reportXmlGenerator.convertContainerToXML(null);
        reportXmlGenerator.createPDFFromXmlAndJasperFile(xml, new File("C:\\Users\\Marco\\Desktop\\Avery_5159_Container.jasper"), "test");
    }

    public void createNewXmlWithModifiedReference()
            throws Exception {
        String xml = reportXmlGenerator.convertContainerWithIdentifierToXML(records.containerId_bac08, null);
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

    /**
     * Collect each name and each value of every metadata of the XML files.
     *
     * @param metadataElements
     * @return list of tuple.
     */
    private List<Tuple> getTupleOfNameAndValueForEachMetadata(List<Element> metadataElements) {
        List<Tuple> listOfTupleOfNameAndValues = new ArrayList<>();
        for (Element metadataElement : metadataElements) {
            listOfTupleOfNameAndValues.add(tuple(metadataElement.getName(), metadataElement.getValue()));
        }
        return listOfTupleOfNameAndValues;
    }
}
