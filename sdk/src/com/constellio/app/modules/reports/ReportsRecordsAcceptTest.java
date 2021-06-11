package com.constellio.app.modules.reports;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.enums.TemplateVersionType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.reports.xml.legacy.LabelXmlGenerator;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.search.criteria.SearchCriterionTestSetup.TestCalculatedSeparatedStructureCalculator;
import com.constellio.app.ui.pages.search.criteria.SearchCriterionTestSetup.TestCalculatedSeparatedStructureFactory;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.setups.Users;
import org.assertj.core.groups.Tuple;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Created by Nicolas D'amours & Charles Blanchette on 2017-01-16.
 */
public class ReportsRecordsAcceptTest extends ConstellioTest {
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	SearchServices ss;
	RecordServices recordServices;
	LabelXmlGenerator reportXmlGenerator;
	ContentManager contentManager;
	private UserVO adminInZeCollection;

	@Before
	public void setUp() {
		prepareSystem(
				withZeCollection().withAllTestUsers().withRMTest(records).withConstellioRMModule()
						.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent().withDocumentsDecommissioningList()
		);
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		ss = getModelLayerFactory().newSearchServices();
		recordServices = getModelLayerFactory().newRecordServices();

		Users users = new Users();
		UserServices userServices = getModelLayerFactory().newUserServices();
		users.setUp(userServices, zeCollection);

		adminInZeCollection = FakeSessionContext.adminInCollection(zeCollection).getCurrentUser();

		reportXmlGenerator = new LabelXmlGenerator(zeCollection, getAppLayerFactory(), Locale.FRENCH, adminInZeCollection.getUsername());
		contentManager = getModelLayerFactory().getContentManager();

	}

	@Test
	public void createReportLabelAndAssignData()
			throws Exception {
		String title = "test REcords 1";
		File file = getTestResourceFile("Avery_5162_Vide.jasper");
		ContentVersionDataSummary upload = contentManager.upload(new FileInputStream(file.getAbsolutePath()), "Etiquette")
				.getContentVersionDataSummary();
		Content c = contentManager.createSystemContent("test-" + LocalDate.now() + ".jasper", upload);
		PrintableLabel r = rm.newPrintableLabel();
		r.setTitle(title);
		r.setJasperFile(c);
		r.setTemplateVersion(TemplateVersionType.CONSTELLIO_5);
		Transaction t = new Transaction();
		t.add(r);
		r.setLigne(4);
		r.setColonne(2);

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
		File file = getTestResourceFile("Avery_5162_Vide.jasper");
		ContentVersionDataSummary upload = contentManager.upload(new FileInputStream(file.getAbsolutePath()), "Etiquette")
				.getContentVersionDataSummary();
		Content c = contentManager.createSystemContent("test-" + LocalDate.now() + ".jasper", upload);
		Printable r = rm.newPrintableLabel();
		PrintableLabel printableLabel = rm.newPrintableLabel();
		printableLabel.setJasperFile(c);
		printableLabel.setTitle(title);
		printableLabel.setLigne(4);
		printableLabel.setColonne(2);
		printableLabel.setTemplateVersion(TemplateVersionType.CONSTELLIO_5);

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
		reportXmlGenerator.setElements(records.getFolder_A01().getWrappedRecord());
		String xmlWithAllFolders = reportXmlGenerator.generateXML();
		ByteArrayInputStream stream = new ByteArrayInputStream(xmlWithAllFolders.getBytes("UTF-8"));
		Document document = builder.build(stream);
		List<Element> folders = document.getRootElement().getChildren();

		List<Field> fields = asList(records.getClass().getDeclaredFields());
		int compteur = 0;
		for (Field f : fields) {
			if (f.getName().matches("folder_([ABC](\\d){1,2})")) {
				compteur++;
			}
		}
		assertThat(folders.size()).isEqualTo(1);
		//        assertThat(folders.get(0).getChild("metadatas").getChild(rm.folder.title().getLabel(i18n.getLanguage()).toLowerCase())
		//                .getValue()).isEqualTo(records.getFolder_A01().getTitle());
	}

	@Test
	public void testConvertFoldersWithIdentifierToXML()
			throws Exception {
		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, (MetadataSchemaTypesAlteration) types -> {
			types.getSchema(Folder.DEFAULT_SCHEMA)
					.create("separatedStructure")
					.defineStructureFactory(TestCalculatedSeparatedStructureFactory.class)
					.defineDataEntry().asCalculated(TestCalculatedSeparatedStructureCalculator.class);
		});

		Record folderA05 = records.getFolder_A05().getWrappedRecord();
		Record folder06 = records.getFolder_A06().getWrappedRecord();
		Record folder07 = records.getFolder_A80().getWrappedRecord();
		recordServices.recalculate(folderA05);
		recordServices.recalculate(folder06);
		recordServices.recalculate(folder07);

		SAXBuilder builder = new SAXBuilder();
		reportXmlGenerator.setElements(folderA05, folder06, folder07);
		String xmlSeul = reportXmlGenerator.generateXML();
		ByteArrayInputStream stream = new ByteArrayInputStream(xmlSeul.getBytes("UTF-8"));
		Document document = builder.build(stream);
		Element meta = ((Element) document.getRootElement().getChildren().get(0)).getChild("metadatas");
		assertThat(meta.getChild("ref_" + rm.folder.category().getCode().replace("_default", "") + "_title").getValue())
				.isEqualTo(records.getCategory_X110().getTitle());

		String xmlWithMulipleIds = reportXmlGenerator.generateXML();
		ByteArrayInputStream streamWithMultiple = new ByteArrayInputStream(xmlWithMulipleIds.getBytes("UTF-8"));
		Document docWithMultiple = builder.build(streamWithMultiple);
		List<Element> XmlMuliple = docWithMultiple.getRootElement().getChildren();

		String tst = reportXmlGenerator.generateXML();

		assertThat(XmlMuliple.size()).isEqualTo(3);
		assertThat(XmlMuliple.get(0).getChild("metadatas").getChild(rm.folder.title().getLocalCode().toLowerCase())
				.getValue()).isEqualTo(records.getFolder_A05().getTitle());
		assertThat(XmlMuliple.get(1).getChild("metadatas").getChild(rm.folder.title().getLocalCode().toLowerCase())
				.getValue()).isEqualTo(records.getFolder_A06().getTitle());
		assertThat(XmlMuliple.get(0).getChild("metadatas").getChild("separatedstructure")
				.getValue()).isEqualTo("Analysis of title '" + records.getFolder_A05().getTitle() + "'");
		assertThat(XmlMuliple.get(1).getChild("metadatas").getChild("separatedstructure")
				.getValue()).isEqualTo("Analysis of title '" + records.getFolder_A06().getTitle() + "'");
		String Null = null;
		try {
			String xmlNull = reportXmlGenerator.generateXML();
		} catch (Exception e) {
		}

	}

	@Test
	public void testConvertContainerToXML()
			throws Exception {
		SAXBuilder builder = new SAXBuilder();
		reportXmlGenerator.setElements(records.getContainerBac01().getWrappedRecord());
		String xmlWithAllContainers = reportXmlGenerator.generateXML();

		System.out.println(xmlWithAllContainers);

		ByteArrayInputStream stream = new ByteArrayInputStream(xmlWithAllContainers.getBytes("UTF-8"));
		Document document = builder.build(stream);
		List<Element> containers = document.getRootElement().getChildren();

		List<Field> fields = asList(records.getClass().getDeclaredFields());
		int compteur = 0;
		for (Field f : fields) {
			if (f.getName().matches("containerId_((bac)(\\d){1,2})")) {
				compteur++;
			}
		}
		//assertThat(containers.size()).isEqualTo(compteur);
		assertThat(containers.get(0).getChild("metadatas")
				.getChild(LabelXmlGenerator.getLabelOfMetadata(rm.containerRecord.title())).getValue())
				.isEqualTo(records.getContainerBac01().getTitle());

		String xmlWithNullParameter = reportXmlGenerator.generateXML();
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
		LabelXmlGenerator labelXmlGenerator = new LabelXmlGenerator(zeCollection, getAppLayerFactory(), Locale.FRENCH,
				adminInZeCollection.getUsername(),
				records.getContainerBac05().getWrappedRecord());

		String contenu = labelXmlGenerator.generateXML();
		ByteArrayInputStream stream = new ByteArrayInputStream(contenu.getBytes("UTF-8"));
		Document document = builder.build(stream);
		Element meta = ((Element) document.getRootElement().getChildren().get(0)).getChild("metadatas");

		LabelXmlGenerator labelXmlGeneratorContenuSeul = new LabelXmlGenerator(zeCollection, getAppLayerFactory(), Locale.FRENCH,
				adminInZeCollection.getUsername(),
				records.getContainerBac08().getWrappedRecord());
		String contenuSeul = labelXmlGeneratorContenuSeul.generateXML();
		ByteArrayInputStream stream1 = new ByteArrayInputStream(contenuSeul.getBytes("UTF-8"));
		Document doc1 = builder.build(stream1);
		Element meta1 = ((Element) doc1.getRootElement().getChildren().get(0)).getChild("metadatas");
		assertThat(meta1.getChild(LabelXmlGenerator.getLabelOfMetadata(rm.containerRecord.title())).getValue())
				.isEqualTo(records.getContainerBac08().getTitle());
		assertThat(meta1.getChild(
				LabelXmlGenerator.REFERENCE_PREFIX + rm.containerRecord.storageSpace().getCode().replace("_default_", "_")
				+ "_code")
				.getValue()).isEqualTo(records.getContainerBac08().getStorageSpace());

		LabelXmlGenerator labelXmlGeneratorConteneurWithMultipleIds = new LabelXmlGenerator(zeCollection, getAppLayerFactory(), Locale.FRENCH,
				adminInZeCollection.getUsername(),
				records.getContainerBac05().getWrappedRecord(), records.getContainerBac07().getWrappedRecord());
		String conteneurWithMultipleIds = labelXmlGeneratorConteneurWithMultipleIds.generateXML();
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
		LabelXmlGenerator labelXmlGenerator = new LabelXmlGenerator(zeCollection, getAppLayerFactory(), Locale.FRENCH,
				adminInZeCollection.getUsername(),
				records.getContainerBac05().getWrappedRecord());

		labelXmlGenerator.setStartingPosition(2);
		String contenu = labelXmlGenerator.generateXML();
		ByteArrayInputStream stream = new ByteArrayInputStream(contenu.getBytes("UTF-8"));
		Document document = builder.build(stream);
		Element meta = ((Element) document.getRootElement().getChildren().get(0)).getChild("metadatas");
		assertThat(meta.getChildren()).isNullOrEmpty();
	}

	@Test
	public void testConvertFolderToXMLWithStartingIndex()
			throws Exception {
		SAXBuilder builder = new SAXBuilder();
		LabelXmlGenerator labelXmlGenerator = new LabelXmlGenerator(zeCollection, getAppLayerFactory(), Locale.FRENCH,
				adminInZeCollection.getUsername(),
				records.getFolder_A01().getWrappedRecord());
		labelXmlGenerator.setStartingPosition(5); // index 4
		String contenu = labelXmlGenerator.generateXML();
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
	}

	@Test
	public void testCheckIfGeneratedXmlForContainerStaysTheSame()
			throws Exception {
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
				tuple("attachedancestors", ""),
				tuple("autocomplete", "10_a_02"),
				tuple("availablesize", ""),
				tuple("borrowdate", ""),
				tuple("borrowreturndate", ""),
				tuple("borrowed", ""),
				tuple("capacity", ""),
				tuple("comments", ""),
				tuple("completiondate", ""),
				tuple("decommissioningtype_code", "C"),
				tuple("decommissioningtype_title", "Versement"),
				tuple("deleted", ""),
				tuple("denytokens", ""),
				tuple("description", ""),
				tuple("detachedauthorizations", ""),
				tuple("erroronphysicaldeletion", ""),
				tuple("fillratioentered", ""),
				tuple("firstdepositreportdate", ""),
				tuple("firsttransferreportdate", ""),
				tuple("full", "Oui"),
				tuple("id", "bac05"),
				tuple("identifier", ""),
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
				tuple("planifiedreturndate", ""),
				tuple("position", ""),
				tuple("realdepositdate", "2012-05-15"),
				tuple("realreturndate", ""),
				tuple("realtransferdate", "2008-10-31"),
				tuple("removedauthorizations", ""),
				tuple("schema", "containerRecord_default"),
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

		reportXmlGenerator.setElements(records.getContainerBac05().getWrappedRecord());
		String contenu = reportXmlGenerator.generateXML();
		ByteArrayInputStream stream = new ByteArrayInputStream(contenu.getBytes());
		Document document = builder.build(stream);
		Element metadatasOfContainer = ((Element) document.getRootElement().getChildren().get(0)).getChild("metadatas");

		assertThat(getTupleOfNameAndValueForEachMetadata(metadatasOfContainer.getChildren())).contains(expectedResults);

		LabelXmlGenerator labelXmlGenerator = new LabelXmlGenerator(zeCollection, getAppLayerFactory(), Locale.FRENCH,
				adminInZeCollection.getUsername(),
				records.getContainerBac05().getWrappedRecord());
		String contenuFromV2 = labelXmlGenerator.generateXML();
		ByteArrayInputStream streamV2 = new ByteArrayInputStream(contenuFromV2.getBytes());
		Document documentV2 = builder.build(streamV2);
		Element metadatasOfContainerV2 = ((Element) documentV2.getRootElement().getChildren().get(0)).getChild("metadatas");

		assertThat(getTupleOfNameAndValueForEachMetadata(metadatasOfContainerV2.getChildren())).contains(expectedResults);
	}

	@InDevelopmentTest
	@Test
	public void testingStartingPositionInvalidIsFailing() {
		try {
			LabelXmlGenerator labelXmlGenerator = new LabelXmlGenerator(zeCollection, getAppLayerFactory(), Locale.FRENCH,
					adminInZeCollection.getUsername(),
					records.getContainerBac05().getWrappedRecord());
			labelXmlGenerator.setStartingPosition(-1);
			labelXmlGenerator.generateXML();
			fail();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	@InDevelopmentTest
	@Test
	public void testingNumberOfCopiesInvalidFailing() {
		try {
			LabelXmlGenerator labelXmlGenerator = new LabelXmlGenerator(zeCollection, getAppLayerFactory(), Locale.FRENCH,
					adminInZeCollection.getUsername(),
					records.getContainerBac05().getWrappedRecord());
			labelXmlGenerator.setNumberOfCopies(0);
			labelXmlGenerator.generateXML();
			fail();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@InDevelopmentTest
	@Test
	public void testingElementNullFailing() {
		try {
			LabelXmlGenerator labelXmlGenerator = new LabelXmlGenerator(zeCollection, getAppLayerFactory(), Locale.FRENCH,
					adminInZeCollection.getUsername());
			labelXmlGenerator.setElements(null);
			labelXmlGenerator.generateXML();
			fail();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@InDevelopmentTest
	@Test
	public void testingNullCollectionFailing() {
		try {
			LabelXmlGenerator labelXmlGenerator = new LabelXmlGenerator(null, getAppLayerFactory(), Locale.FRENCH,
					adminInZeCollection.getUsername(),
					records.getContainerBac07().getWrappedRecord());
			labelXmlGenerator.generateXML();
			fail();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@InDevelopmentTest
	@Test
	public void testingEmptyCollectionFailing() {
		try {
			LabelXmlGenerator labelXmlGenerator = new LabelXmlGenerator("", getAppLayerFactory(), Locale.FRENCH,
					adminInZeCollection.getUsername(),
					records.getContainerBac07().getWrappedRecord());
			labelXmlGenerator.generateXML();
			fail();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@InDevelopmentTest
	@Test
	public void testingNullFactoryFailing() {
		try {
			LabelXmlGenerator labelXmlGenerator = new LabelXmlGenerator(zeCollection, null, Locale.FRENCH, adminInZeCollection.getUsername(),
					records.getContainerBac07().getWrappedRecord());
			labelXmlGenerator.generateXML();
			fail();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
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
