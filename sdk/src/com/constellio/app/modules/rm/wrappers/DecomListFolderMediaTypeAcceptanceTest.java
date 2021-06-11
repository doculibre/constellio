package com.constellio.app.modules.rm.wrappers;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.FolderMediaType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailStatus;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DecomListFolderMediaTypeAcceptanceTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();
	RMSchemasRecordsServices rm;
	RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records).withFoldersAndContainersOfEveryStatus().withRMTest(records).withDocumentsHavingContent()
		);
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
	}

	@Test
	public void givenFoldersInDocomissioningListThenMediaTypeIsCopiedInDecomissioningList()
			throws RecordServicesException {
		DecommissioningList decommissioningList1 = rm.newDecommissioningList();

		decommissioningList1.addFolderDetailsFor(FolderDetailStatus.INCLUDED, rm.getFolder(records.folder_A10));
		decommissioningList1.setTitle("title");
		recordServices.add(decommissioningList1);

		decommissioningList1 = rm.getDecommissioningList(decommissioningList1.getId());

		assertThat(decommissioningList1.getFoldersMediaTypes()).contains(FolderMediaType.HYBRID);

		decommissioningList1.addFolderDetailsFor(FolderDetailStatus.INCLUDED, rm.getFolder(records.folder_A11));
		recordServices.update(decommissioningList1);

		decommissioningList1 = rm.getDecommissioningList(decommissioningList1.getId());
		assertThat(decommissioningList1.getFoldersMediaTypes()).contains(FolderMediaType.HYBRID, FolderMediaType.HYBRID);

		Folder newFolder = rm.newFolder();
		newFolder.setTitle("folder");
		newFolder.setParentFolder(records.folder_A01);
		newFolder.setOpenDate(LocalDate.now());
		recordServices.add(newFolder);

		newFolder = rm.getFolder(newFolder.getId());
		decommissioningList1.addFolderDetailsFor(FolderDetailStatus.INCLUDED, newFolder);
		recordServices.update(decommissioningList1);

		decommissioningList1 = rm.getDecommissioningList(decommissioningList1.getId());

		assertThat(decommissioningList1.getFoldersMediaTypes()).contains(FolderMediaType.HYBRID, FolderMediaType.HYBRID, FolderMediaType.UNKNOWN);

		newFolder.setMediumTypes(rm.PA());
		recordServices.update(newFolder);

		decommissioningList1 = rm.getDecommissioningList(decommissioningList1.getId());
		assertThat(decommissioningList1.getFoldersMediaTypes()).contains(FolderMediaType.HYBRID, FolderMediaType.HYBRID, FolderMediaType.ANALOG);
	}
}
