package com.constellio.app.modules.restapi.folder;

import com.constellio.app.modules.restapi.BaseRestfulServiceAcceptanceTest;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.SchemaTypes;
import com.constellio.app.modules.restapi.folder.dto.FolderDto;
import com.constellio.app.modules.restapi.folder.dto.MixInFolderDto;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.model.entities.records.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.LocalDate;
import org.junit.Before;

import javax.ws.rs.HttpMethod;

public class BaseFolderRestfulServiceAcceptanceTest extends BaseRestfulServiceAcceptanceTest {

	protected String id, physical, signature;
	protected String schemaType = SchemaTypes.FOLDER.name(), folderId = records.folder_A20,
			method = HttpMethod.GET, expiration = "2147483647";
	protected String date = DateUtils.formatIsoNoMillis(fakeDate);
	protected Folder fakeFolder;
	protected FolderType fakeFolderType;

	@Override
	protected SchemaTypes getSchemaType() {
		return SchemaTypes.FOLDER;
	}

	@Before
	public void setUp() throws Exception {
		setUpTest();

		uploadFakeFolder();
		createAuthorizations(fakeFolder.getWrappedRecord());
		id = fakeFolder.getId();

		ObjectMapper mapper = new ObjectMapper()
				.addMixIn(FolderDto.class, MixInFolderDto.class);
		webTarget = newWebTarget("v1/folders", mapper);
	}

	protected void uploadFakeFolder() throws Exception {
		Transaction transaction = new Transaction();

		fakeFolderType = rm.newFolderTypeWithId("fakeFolderTypeId").setCode("folderTypeCode").setTitle("folderTypeTitle");
		transaction.add(fakeFolderType);

		fakeFolder = rm.newFolder();
		transaction.add(fakeFolder).setAdministrativeUnitEntered(records.unitId_11b)
				.setCategoryEntered(records.categoryId_X110).setTitle("Fake folder")
				.setRetentionRuleEntered(records.ruleId_2).setCopyStatusEntered(CopyType.PRINCIPAL)
				.setOpenDate(new LocalDate(2019, 4, 4));

		recordServices.execute(transaction);
	}
}
