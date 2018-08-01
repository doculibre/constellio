package com.constellio.app.modules.rm.imports;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.schemas.bulkImport.*;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.builder.ImportDataBuilder;
import com.constellio.app.services.schemas.bulkImport.data.xml.XMLImportDataProvider;
import com.constellio.data.utils.Provider;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Test;

import static com.constellio.app.services.schemas.bulkImport.BulkImportParams.ImportErrorsBehavior.CONTINUE_FOR_RECORD_OF_SAME_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

public class RMImportsAcceptanceTest extends ConstellioTest {

	Users users = new Users();
	RecordsImportServices importServices;
	RMSchemasRecordsServices rm;

	@Test
	public void whenImportingDocumentTypesAndRetentionRulesAtTheSameMomentThenOK()
			throws Exception {

		prepareSystem(
				withZeCollection().withAllTest(users).withConstellioRMModule()
		);
		importServices = new RecordsImportServices(getModelLayerFactory());
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		ImportDataProvider importDataProvider = XMLImportDataProvider.forZipFile(
				getModelLayerFactory(), getTestResourceFile("data.zip"));

		BulkImportProgressionListener progressionListener = new LoggerBulkImportProgressionListener();

		User admin = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);

		BulkImportParams params = new BulkImportParams();
		params.setBatchSize(1);
		importServices.bulkImport(importDataProvider, progressionListener, admin, params);

		RetentionRule rule1 = rm.getRetentionRuleWithCode("111200");
		assertThat(rule1.getDocumentTypesDetails()).isNotEmpty();
		assertThat(rule1.getDocumentTypes()).isNotEmpty();

	}

	@Test
	public void whenImportingFolderWithAutoSetToStatusThenMagicallyAutosetted()
			throws Exception {

		RMTestRecords records = new RMTestRecords(zeCollection);

		prepareSystem(
				withZeCollection().withAllTest(users).withConstellioRMModule().withConstellioRMModule().withRMTest(records)
		);
		importServices = new RecordsImportServices(getModelLayerFactory());
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		Provider<String, ImportDataBuilder> provider1 = new Provider<String, ImportDataBuilder>() {
			@Override
			public ImportDataBuilder get(String id) {
				return folder().setId(id).addField("title", "Record " + id)
							   .addField(Folder.ADMINISTRATIVE_UNIT_ENTERED, "code:10A")
							   .addField(Folder.CATEGORY_ENTERED, "code:X13")
							   .addField(Folder.RETENTION_RULE_ENTERED, "code:1")
							   .addField(Folder.OPENING_DATE, date(2015, 1, 4))
							   .addField(Folder.ENTERED_CLOSING_DATE, date(2016, 1, 4));
			}
		};

		Provider<String, ImportDataBuilder> provider2 = new Provider<String, ImportDataBuilder>() {
			@Override
			public ImportDataBuilder get(String id) {
				return folder().setId(id).addField("title", "Record " + id)
							   .addField(Folder.ADMINISTRATIVE_UNIT_ENTERED, "code:10A")
							   .addField(Folder.CATEGORY_ENTERED, "code:X13")
							   .addField(Folder.RETENTION_RULE_ENTERED, "code:2")
							   .addField(Folder.COPY_STATUS_ENTERED, "P")
							   .addField(Folder.OPENING_DATE, date(2014, 1, 4))
							   .addField(Folder.ENTERED_CLOSING_DATE, date(2015, 1, 4));
			}
		};

		DummyImportDataProvider importDataProvider = new DummyImportDataProvider();
		importDataProvider.add(Folder.SCHEMA_TYPE, provider1.get("active1"));
		importDataProvider.add(Folder.SCHEMA_TYPE, provider2.get("active2").addOption("autoSetStatusTo", "a"));
		importDataProvider.add(Folder.SCHEMA_TYPE, provider1.get("semiActive1").addOption("autoSetStatusTo", "s"));
		importDataProvider.add(Folder.SCHEMA_TYPE, provider2.get("semiActive2").addOption("autoSetStatusTo", "s"));
		importDataProvider.add(Folder.SCHEMA_TYPE, provider1.get("inactive1").addOption("autoSetStatusTo", "v"));
		importDataProvider.add(Folder.SCHEMA_TYPE, provider2.get("inactive2").addOption("autoSetStatusTo", "v"));
		importDataProvider.add(Folder.SCHEMA_TYPE, provider2.get("inactive3").addOption("autoSetStatusTo", "d"));

		bulkImport(importDataProvider, users.adminIn(zeCollection));

		Folder folder = rm.getFolderWithLegacyId("active1");
		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
		assertThat(folder.getExpectedTransferDate()).isEqualTo(date(2017, 12, 31));
		assertThat(folder.getExpectedDepositDate()).isEqualTo(date(2022, 12, 31));
		assertThat(folder.getExpectedDestructionDate()).isNull();

		folder = rm.getFolderWithLegacyId("active2");
		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
		assertThat(folder.getExpectedTransferDate()).isEqualTo(date(2020, 12, 31));
		assertThat(folder.getExpectedDepositDate()).isEqualTo(date(2022, 12, 31));
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(date(2022, 12, 31));

		folder = rm.getFolderWithLegacyId("semiActive1");
		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folder.getActualTransferDate()).isEqualTo(date(2017, 12, 31));
		assertThat(folder.getExpectedDepositDate()).isEqualTo(date(2022, 12, 31));
		assertThat(folder.getExpectedDestructionDate()).isNull();

		folder = rm.getFolderWithLegacyId("semiActive2");
		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folder.getActualTransferDate()).isEqualTo(date(2020, 12, 31));
		assertThat(folder.getExpectedDepositDate()).isEqualTo(date(2022, 12, 31));
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(date(2022, 12, 31));

		folder = rm.getFolderWithLegacyId("inactive1");
		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.INACTIVE_DEPOSITED);
		assertThat(folder.getActualTransferDate()).isEqualTo(date(2017, 12, 31));
		assertThat(folder.getActualDepositDate()).isEqualTo(date(2022, 12, 31));
		assertThat(folder.getExpectedDestructionDate()).isNull();

		folder = rm.getFolderWithLegacyId("inactive2");
		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.INACTIVE_DEPOSITED);
		assertThat(folder.getActualTransferDate()).isEqualTo(date(2020, 12, 31));
		assertThat(folder.getActualDepositDate()).isEqualTo(date(2022, 12, 31));
		assertThat(folder.getExpectedDestructionDate()).isNull();

		folder = rm.getFolderWithLegacyId("inactive3");
		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.INACTIVE_DESTROYED);
		assertThat(folder.getActualTransferDate()).isEqualTo(date(2020, 12, 31));
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isEqualTo(date(2022, 12, 31));

	}

	private ImportDataBuilder folder() {
		return new ImportDataBuilder().setSchema("default");
	}

	private void bulkImport(ImportDataProvider importDataProvider, final User user)
			throws ValidationException {
		final BulkImportProgressionListener bulkImportProgressionListener = new LoggerBulkImportProgressionListener();
		bulkImport(importDataProvider, bulkImportProgressionListener, user, new BulkImportParams());
	}

	private void bulkImport(ImportDataProvider importDataProvider,
							final BulkImportProgressionListener bulkImportProgressionListener,
							final User user, BulkImportParams params)
			throws ValidationException {

		params.setImportErrorsBehavior(CONTINUE_FOR_RECORD_OF_SAME_TYPE);
		new RecordsImportServices(getModelLayerFactory())
				.bulkImport(importDataProvider, bulkImportProgressionListener, user, params);

	}

}
