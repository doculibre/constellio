package com.constellio.app.services.sip.record;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.sip.RMSelectedFoldersAndDocumentsSIPBuilder;
import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.services.sip.bagInfo.DefaultSIPZipBagInfoFactory;
import com.constellio.app.services.sip.bagInfo.SIPZipBagInfoFactory;
import com.constellio.app.services.sip.zip.FileSIPZipWriter;
import com.constellio.app.services.sip.zip.SIPZipWriter;
import com.constellio.data.dao.services.idGenerator.InMemorySequentialGenerator;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.SecurityModel;
import com.constellio.model.entities.security.SecurityModelAuthorization;
import com.constellio.model.entities.security.global.SystemWideGroup;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestUtils;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CollectionInfosSIPWriterAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();
	RMSchemasRecordsServices rm;
	IOServices ioServices;
	RMSelectedFoldersAndDocumentsSIPBuilder constellioSIP;

	@Before
	public void setUp() throws Exception {

		records.copyBuilder = new CopyRetentionRuleBuilder(new InMemorySequentialGenerator());

		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records));
		this.rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		givenTimeIs(new LocalDateTime(2018, 1, 2, 3, 4, 5));

		ioServices = getModelLayerFactory().getIOServicesFactory().newIOServices();
		constellioSIP = new RMSelectedFoldersAndDocumentsSIPBuilder(zeCollection, getAppLayerFactory());
	}

	@Test
	public void whenExportingSystemCollectionInfosThenExportAllToolsAndSettings()
			throws Exception {

		File sipFile = new File(newTempFolder(), "collectionInfos.zip");
		SIPZipBagInfoFactory bagInfoFactory = new DefaultSIPZipBagInfoFactory(getAppLayerFactory(), Locale.FRENCH);
		SIPZipWriter sipZipWriter = new FileSIPZipWriter(getAppLayerFactory(), sipFile, "collectionInfos", bagInfoFactory);
		CollectionInfosSIPWriter writer = new CollectionInfosSIPWriter(Collection.SYSTEM_COLLECTION, getAppLayerFactory(), sipZipWriter, Locale.FRENCH, new ProgressInfo());
		writer.exportCollectionConfigs();
		writer.close();

		SchemasRecordsServices system = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, getModelLayerFactory());
		List<String> expectedFiles = new ArrayList<>();
		for (SystemWideUserInfos userCredential : system.getAllUserCredentials()) {
			expectedFiles.add("data/userCredential/userCredential-" + userCredential.getId() + ".xml");
		}

		for (SystemWideGroup globalGroup : system.getAllGlobalGroups()) {
			expectedFiles.add("data/globalGroup/globalGroup-" + globalGroup.getId() + ".xml");
		}


		expectedFiles.add("manifest-sha256.txt");
		expectedFiles.add("tagmanifest-sha256.txt");


		TestUtils.assertFilesInZip(sipFile).contains(expectedFiles.toArray(new String[0]));

	}

	@Test
	public void whenExportingCollectionInfosThenExportAllToolsAndSettings()
			throws Exception {

		File sipFile = new File(newTempFolder(), "collectionInfos.zip");
		SIPZipBagInfoFactory bagInfoFactory = new DefaultSIPZipBagInfoFactory(getAppLayerFactory(), Locale.FRENCH);
		SIPZipWriter sipZipWriter = new FileSIPZipWriter(getAppLayerFactory(), sipFile, "collectionInfos", bagInfoFactory);
		CollectionInfosSIPWriter writer = new CollectionInfosSIPWriter(zeCollection, getAppLayerFactory(), sipZipWriter, Locale.FRENCH, new ProgressInfo());
		writer.exportCollectionConfigs();
		writer.close();

		assertContainsAllTools(sipFile);

	}

	protected void assertContainsAllTools(File sipFile) {
		List<String> expectedFiles = new ArrayList<>();
		expectedFiles.add("bag-info.txt");
		expectedFiles.add("bagit.txt");
		expectedFiles.add("collectionInfos.xml");
		expectedFiles.add("data/admUnits/administrativeUnit-unitId_10.xml");
		expectedFiles.add("data/admUnits/administrativeUnit-unitId_10/administrativeUnit-unitId_10a.xml");
		expectedFiles.add("data/admUnits/administrativeUnit-unitId_10/administrativeUnit-unitId_11.xml");
		expectedFiles.add("data/admUnits/administrativeUnit-unitId_10/administrativeUnit-unitId_11/administrativeUnit-unitId_11b.xml");

		SecurityModel securityModel = getModelLayerFactory().newRecordServices().getSecurityModel(zeCollection);

		for (SecurityModelAuthorization auth : securityModel.getAuthorizationsOnTarget("unitId_11")) {
			expectedFiles.add("data/admUnits/administrativeUnit-unitId_10/administrativeUnit-unitId_11/authorizationDetails-" + auth.getDetails().getId() + ".xml");
		}

		expectedFiles.add("data/admUnits/administrativeUnit-unitId_10/administrativeUnit-unitId_12.xml");
		expectedFiles.add("data/admUnits/administrativeUnit-unitId_10/administrativeUnit-unitId_12/administrativeUnit-unitId_12b.xml");
		expectedFiles.add("data/admUnits/administrativeUnit-unitId_10/administrativeUnit-unitId_12/administrativeUnit-unitId_12c.xml");
		expectedFiles.add("data/admUnits/administrativeUnit-unitId_20.xml");
		expectedFiles.add("data/admUnits/administrativeUnit-unitId_20/administrativeUnit-unitId_20d.xml");
		expectedFiles.add("data/admUnits/administrativeUnit-unitId_20/administrativeUnit-unitId_20e.xml");
		expectedFiles.add("data/collectionSettings/emailTemplates/alertAvailable.html");
		expectedFiles.add("data/collectionSettings/emailTemplates/alertBorrowedAccepted.html");
		expectedFiles.add("data/collectionSettings/emailTemplates/alertBorrowedDenied.html");
		expectedFiles.add("data/collectionSettings/emailTemplates/alertBorrowingExtendedAccepted.html");

		expectedFiles.add("data/collectionSettings/roles.xml");
		expectedFiles.add("data/collectionSettings/schemasDisplay.xml");
		expectedFiles.add("data/collectionSettings/searchBoosts.xml");
		expectedFiles.add("data/collectionSettings/settings.xml");

		for (Facet facet : rm.getAllFacets()) {
			expectedFiles.add("data/facet/facet-" + facet.getId() + ".xml");
		}
		for (Group group : rm.getAllGroups()) {
			expectedFiles.add("data/group/group-" + group.getId() + ".xml");
		}
		expectedFiles.add("data/plan/category-categoryId_X.xml");
		expectedFiles.add("data/plan/category-categoryId_X/category-categoryId_X100.xml");
		expectedFiles.add("data/plan/category-categoryId_X/category-categoryId_X100/category-categoryId_X110.xml");
		expectedFiles.add("data/plan/category-categoryId_X/category-categoryId_X100/category-categoryId_X120.xml");
		expectedFiles.add("data/plan/category-categoryId_X/category-categoryId_X13.xml");

		for (Printable printable : rm.getAllPrintableReports()) {
			expectedFiles.add("data/printable/printable-" + printable.getId() + "-jasperfile-1.0.jasper");
			expectedFiles.add("data/printable/printable-" + printable.getId() + ".xml");
		}

		expectedFiles.add("data/retentionRule/retentionRule-ruleId_1.xml");
		expectedFiles.add("data/retentionRule/retentionRule-ruleId_2.xml");
		expectedFiles.add("data/retentionRule/retentionRule-ruleId_5.xml");
		expectedFiles.add("data/uniformSubdivision/uniformSubdivision-subdivId_1.xml");
		expectedFiles.add("data/uniformSubdivision/uniformSubdivision-subdivId_3.xml");

		for (User user : rm.getAllUsers()) {
			expectedFiles.add("data/user/user-" + user.getId() + ".xml");
		}
		expectedFiles.add("data/valueLists/ddvDocumentType/ddvDocumentType-documentTypeId_1.xml");
		expectedFiles.add("data/valueLists/ddvDocumentType/ddvDocumentType-documentTypeId_10.xml");
		expectedFiles.add("data/valueLists/ddvDocumentType/ddvDocumentType-documentTypeId_2.xml");

		expectedFiles.add("manifest-sha256.txt");
		expectedFiles.add("tagmanifest-sha256.txt");


		TestUtils.assertFilesInZip(sipFile).contains(expectedFiles.toArray(new String[0]));
	}

}
