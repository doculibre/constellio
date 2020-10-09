package com.constellio.app.modules.restapi.folder;

import com.constellio.app.modules.restapi.BaseRestfulServiceAcceptanceTest;
import com.constellio.app.modules.restapi.RestApiConfigs;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.SchemaTypes;
import com.constellio.app.modules.restapi.folder.dto.AdministrativeUnitDto;
import com.constellio.app.modules.restapi.folder.dto.CategoryDto;
import com.constellio.app.modules.restapi.folder.dto.ContainerDto;
import com.constellio.app.modules.restapi.folder.dto.FolderDto;
import com.constellio.app.modules.restapi.folder.dto.FolderTypeDto;
import com.constellio.app.modules.restapi.folder.dto.MixinAdministrativeUnitDto;
import com.constellio.app.modules.restapi.folder.dto.MixinCategoryDto;
import com.constellio.app.modules.restapi.folder.dto.MixinContainerDto;
import com.constellio.app.modules.restapi.folder.dto.MixinFolderDto;
import com.constellio.app.modules.restapi.folder.dto.MixinFolderTypeDto;
import com.constellio.app.modules.restapi.folder.dto.MixinRetetentionRuleDto;
import com.constellio.app.modules.restapi.folder.dto.RetentionRuleDto;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.sdk.tests.CommitCounter;
import com.constellio.sdk.tests.QueryCounter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.LocalDate;
import org.junit.Before;

import javax.ws.rs.HttpMethod;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class BaseFolderRestfulServiceAcceptanceTest extends BaseRestfulServiceAcceptanceTest {

	protected String id, signature;
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
				.addMixIn(FolderTypeDto.class, MixinFolderTypeDto.class)
				.addMixIn(FolderDto.class, MixinFolderDto.class)
				.addMixIn(FolderTypeDto.class, MixinFolderTypeDto.class)
				.addMixIn(RetentionRuleDto.class, MixinRetetentionRuleDto.class)
				.addMixIn(CategoryDto.class, MixinCategoryDto.class)
				.addMixIn(AdministrativeUnitDto.class, MixinAdministrativeUnitDto.class)
				.addMixIn(ContainerDto.class, MixinContainerDto.class);

		webTarget = newWebTarget("v1/folders", mapper);

		givenConfig(RestApiConfigs.REST_API_URLS, "localhost:7070; localhost2");
		givenTimeIs(fakeDate);

		commitCounter = new CommitCounter(getDataLayerFactory());
		queryCounter = new QueryCounter(getDataLayerFactory());
	}

	protected List<String> getMediumTypesCode(List<String> mediumTypeIds) {

		if (mediumTypeIds != null) {
			List<String> mediumTypeCodes = new ArrayList<>();
			for (String mediumTypeId : mediumTypeIds) {
				Record mediumType = recordServices.getDocumentById(mediumTypeId);
				mediumTypeCodes.add(mediumType.<String>get(Schemas.CODE));
			}
			return mediumTypeCodes;
		}
		return null;
	}

	protected List<String> getMediumTypes(Record record) {
		Folder folder = rm.wrapFolder(record);

		List<String> mediumTypeIds = folder.getMediumTypes();
		if (mediumTypeIds != null) {
			List<String> mediumTypeCodes = new ArrayList<>();
			for (String mediumTypeId : mediumTypeIds) {
				Record mediumType = recordServices.getDocumentById(mediumTypeId);
				mediumTypeCodes.add(mediumType.<String>get(Schemas.CODE));
			}
			return mediumTypeCodes;
		}
		return null;
	}

	protected void switchToCustomSchema(String id) throws Exception {
		Record record = recordServices.getDocumentById(id);
		record.changeSchema(records.getSchemas().defaultFolderSchema(),
				records.getSchemas().folderSchemaFor(records.folderTypeEmploye()));
		recordServices.update(record);
	}

	protected void uploadFakeFolder() throws Exception {
		Transaction transaction = new Transaction();

		fakeFolderType = rm.newFolderType().setCode("fakeFolderTypeId").setTitle("folderTypeTitle");
		transaction.add(fakeFolderType);

		fakeFolder = rm.newFolder();
		transaction.add(fakeFolder).setAdministrativeUnitEntered(records.unitId_11b)
				.setType(fakeFolderType)
				.setTitle("aTitle")
				.setDescription("differentDescription")
				.setCategoryEntered(records.categoryId_X110).setTitle("Fake folder")
				.setRetentionRuleEntered(records.ruleId_2).setCopyStatusEntered(CopyType.PRINCIPAL)
				.setCopyStatusEntered(CopyType.SECONDARY)
				.setMediumTypes(asList(rm.FI()))
				.setKeywords(asList("initial.keyword1", "initial.keyword2"))
				.setContainer(records.containerId_bac13)
				.setOpenDate(new LocalDate().minusYears(100))
				.setCloseDateEntered(new LocalDate().plusYears(100));

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

	protected <T> void addUsrMetadata(final MetadataValueType type, T value1, T value2) throws Exception {
		addUsrMetadata(id, Folder.DEFAULT_SCHEMA, type, value1, value2);
	}

}
