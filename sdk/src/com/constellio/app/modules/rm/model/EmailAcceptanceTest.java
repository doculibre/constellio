package com.constellio.app.modules.rm.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;

public class EmailAcceptanceTest extends ConstellioTest {

	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		assertThat(getModelLayerFactory().getTaxonomiesManager().getPrincipalTaxonomy(zeCollection).getCode())
				.isEqualTo(RMTaxonomies.ADMINISTRATIVE_UNITS);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();

	}

	@Test
	public void whenCreatingAnEmailWithoutDescriptionThenOK()
			throws Exception {

		Email email = rm.newEmail();
		email.setTitle("My email").setDescription("test").setFolder(records.folder_A03);
		email.setEmailTo(Arrays.asList("dest1", "dest2"));
		recordServices.add(email);
		email.setDescription(null).setTitle("Z");
		recordServices.update(email);
		assertThat(email.getEmailTo()).containsOnly("dest1", "dest2");
	}

	@Test
	public void givenEmailThenInheritFolderMetadatas()
			throws Exception {

		Email email = rm.newEmail();
		email.setTitle("My email").setDescription("test").setFolder(records.folder_A03);
		email.setEmailTo(Arrays.asList("dest1", "dest2"));
		recordServices.add(email);

		assertThat(email.getFolderAdministrativeUnit()).isEqualTo(records.unitId_10a);
		assertThat(email.getFolderCategory()).isEqualTo(records.categoryId_X110);

		Folder folder = records.getFolder_A03()
				.setCategoryEntered(records.categoryId_X13)
				.setAdministrativeUnitEntered(records.unitId_11b);

		recordServices.execute(new Transaction(folder.getWrappedRecord()));
		waitForBatchProcess();
		recordServices.refresh(email);
		assertThat(email.getFolderAdministrativeUnit()).isEqualTo(records.unitId_11b);
		assertThat(email.getFolderCategory()).isEqualTo(records.categoryId_X13);

	}
}
