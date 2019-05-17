package com.constellio.app.modules.restapi.folder;

import com.constellio.app.modules.restapi.BaseRestfulServiceAcceptanceTest;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.SchemaTypes;
import com.constellio.app.modules.restapi.folder.dto.FolderDto;
import com.constellio.app.modules.restapi.folder.dto.FolderTypeDto;
import com.constellio.app.modules.restapi.folder.dto.MixInFolderDto;
import com.constellio.app.modules.restapi.folder.dto.MixinFolderTypeDto;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.model.entities.records.Transaction;
import com.constellio.sdk.tests.CommitCounter;
import com.constellio.sdk.tests.QueryCounter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.LocalDate;
import org.junit.Before;

import javax.ws.rs.HttpMethod;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.records.wrappers.Collection.SYSTEM_COLLECTION;
import static com.constellio.sdk.tests.QueryCounter.ON_COLLECTION;

public class BaseFolderRestfulServiceAcceptanceTest extends BaseRestfulServiceAcceptanceTest {

	protected String id, physical, signature;
	protected String schemaType = SchemaTypes.FOLDER.name(), folderId = records.folder_A04,
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
				.addMixIn(FolderDto.class, MixInFolderDto.class)
				.addMixIn(FolderTypeDto.class, MixinFolderTypeDto.class);
		webTarget = newWebTarget("v1/folders", mapper);

		commitCounter = new CommitCounter(getDataLayerFactory());
		queryCounter = new QueryCounter(getDataLayerFactory(), ON_COLLECTION(SYSTEM_COLLECTION));
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

	protected CopyType toCopyType(String copyStatus) {
		for (CopyType copyType : CopyType.values()) {
			if (copyType.getCode().equals(copyStatus)) {
				return copyType;
			}
		}
		return null;
	}

	protected List<String> toMediumTypeIds(List<String> mediumTypeCodes) {
		List<String> ids = new ArrayList<>();
		for (String mediumTypeCode : mediumTypeCodes) {
			ids.add(rm.getMediumTypeByCode(mediumTypeCode).getId());
		}
		return ids;
	}

	protected CopyRetentionRule toMainCopyRule(String ruleId, String mainCopyRuleId) {
		RetentionRule rule = rm.getRetentionRule(ruleId);
		return rule.getCopyRetentionRuleWithId(mainCopyRuleId);
	}
}
