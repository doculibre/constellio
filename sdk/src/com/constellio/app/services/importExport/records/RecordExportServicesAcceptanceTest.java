package com.constellio.app.services.importExport.records;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.*;
import static com.constellio.sdk.tests.TestUtils.asList;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.wrappers.*;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;

import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.taskdefs.Copy;
import org.assertj.core.groups.Tuple;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.services.schemas.bulkImport.BulkImportParams;
import com.constellio.app.services.schemas.bulkImport.LoggerBulkImportProgressionListener;
import com.constellio.app.services.schemas.bulkImport.RecordsImportServices;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.xml.XMLImportDataProvider;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class RecordExportServicesAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordExportOptions options = new RecordExportOptions();
	Users users = new Users();

	@Test(expected = RecordExportServicesRuntimeException.ExportServicesRuntimeException_NoRecords.class)
	public void givenEmptyCollectionWhenExportRecordsThenExceptionThrown()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioRMModule().withAllTest(users),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));

		exportThenImportInAnotherCollection(options);

	}

	@Test
	public void whenExportingDecommissionList()
	{
		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioRMModule().withAllTest(users).withRMTest(records).withDocumentsDecommissioningList(),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));

		exportThenImportInAnotherCollection(
				options.setExportedSchemaTypes(asList(AdministrativeUnit.SCHEMA_TYPE, DecommissioningList.SCHEMA_TYPE)));

		//records.getList01(.get)
	}

	@Test
	public void whenExportingComment() throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioRMModule().withAllTest(users).withRMTest(records),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));
		final String MESSAGE = "Message";
		final User user = records.getAdmin();

		Comment comment = new Comment();
		comment.setUser(records.getAdmin());
		comment.setMessage(MESSAGE);


		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		Transaction transaction = new Transaction();

		Category category = records.getCategory_X().setComments(asList(comment));
		transaction.update(category.getWrappedRecord());

		recordServices.execute(transaction);

		exportThenImportInAnotherCollection(
				options.setExportedSchemaTypes(asList(AdministrativeUnit.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE,Category.SCHEMA_TYPE)));


		RMSchemasRecordsServices rmAnotherCollection = new RMSchemasRecordsServices("anotherCollection", getAppLayerFactory());

		Category categoryFromAnOtherCollection = rmAnotherCollection.getCategoryWithCode("X");

		assertThat(categoryFromAnOtherCollection.getComments().size()).isEqualTo(1);

		Comment commentFromAnOtherCollection = categoryFromAnOtherCollection.getComments().get(0);
		assertThat(commentFromAnOtherCollection.getMessage()).isEqualTo(MESSAGE);
		assertThat(commentFromAnOtherCollection.getUsername()).isEqualTo(user.getUsername());
		assertThat(commentFromAnOtherCollection.getUserId()).isEqualTo(user.getId());
	}

	@Test
	public void whenExportingSpecificExportValueLists() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioRMModule().withAllTest(users).withRMTest(records),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));

		exportThenImportInAnotherCollection(
				options.setExportValueLists(true));

		RMSchemasRecordsServices rmAnotherCollection = new RMSchemasRecordsServices("anotherCollection", getAppLayerFactory());

		assertThatRecords(rmAnotherCollection.searchDocumentTypes(ALL)).extractingMetadatas("legacyIdentifier", "code", "title")
				.contains(
						tuple("documentTypeId_1", "1", "Livre de recettes"), tuple("documentTypeId_2", "2", "Typologie"),
						tuple("documentTypeId_3", "3", "Petit guide"), tuple("documentTypeId_4", "4", "Histoire"),
						tuple("documentTypeId_5", "5", "Calendrier des réunions"), tuple("documentTypeId_6", "6",
								"Dossier de réunion : avis de convocation, ordre du jour, procès-verbal, extraits de procès-verbaux, résolutions, documents déposés, correspondance"),
						tuple("documentTypeId_7", "7", "Notes de réunion"), tuple("documentTypeId_8", "8",
								"Dossiers des administrateurs : affirmations solennelles, serments de discrétion"),
						tuple("documentTypeId_9", "9", "Contrat"), tuple("documentTypeId_10", "10", "Procès-verbal"));

	}

	@Test
	public void whenExportingSpecificSchemaTypesThenExported()
			throws Exception {
		givenDisabledAfterTestValidations();
		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioRMModule().withAllTest(users).withRMTest(records),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		RetentionRule retentionRule = rm.newRetentionRule();

		Transaction transaction = new Transaction();

		final String TITLE = "Title1";
		final String CODE = "CODE1";
		final String DESCRIPTION = "DESCRIPTION1";
		final String CONTENT_TYPES_COMMENT = "CONTENT_TYPES_COMMENT1";
		final String ACTIVE_RETENTION_COMMENT = "ACTIVE_RETENTION_COMMENT";
		final RetentionPeriod ACTIVE_RETENTION_PERIOD = RetentionPeriod.OPEN_888;
		final String SEMI_ACTIVE_RETENTION_COMMENT = "SEMI_ACTIVE_RETENTION_COMMENT";
		final RetentionPeriod SEMI_ACTIVE_RETENTION_PERIOD = RetentionPeriod.OPEN_888;
		final String INACTIVE_DISPOSAL_COMMENT = "DISPOSAL_COMMENT";
		final DisposalType INACTIVE_DISPOSAL_TYPE = DisposalType.DESTRUCTION;
		final Integer OPEN_ACTIVE_RETENTION_PERIOD = new Integer(100);
		final boolean REQUIRED_COPYRULE_FIELD = true;
		final String SET_ID = "ID1";
		final List<String> MEDIUM_TYPE = records.PA_MD;

		ArrayList<CopyRetentionRule> arrayList = new ArrayList<CopyRetentionRule>();

		CopyRetentionRule copyRetentionRule1 = new CopyRetentionRule().setCopyType(CopyType.PRINCIPAL).setCode(CODE)
				.setTitle(TITLE)
				.setDescription(DESCRIPTION).setContentTypesComment(CONTENT_TYPES_COMMENT)
				.setActiveRetentionComment(ACTIVE_RETENTION_COMMENT)
				.setActiveRetentionPeriod(ACTIVE_RETENTION_PERIOD).setSemiActiveRetentionComment(SEMI_ACTIVE_RETENTION_COMMENT)
				.setSemiActiveRetentionPeriod(SEMI_ACTIVE_RETENTION_PERIOD)
				.setInactiveDisposalComment(INACTIVE_DISPOSAL_COMMENT).setInactiveDisposalType(INACTIVE_DISPOSAL_TYPE)
				.setOpenActiveRetentionPeriod(OPEN_ACTIVE_RETENTION_PERIOD)
				.setEssential(REQUIRED_COPYRULE_FIELD).setId(SET_ID).setMediumTypeIds(MEDIUM_TYPE).setIgnoreActivePeriod(false);

		CopyRetentionRule copyRetentionRule2 = new CopyRetentionRule().setCopyType(CopyType.SECONDARY).setCode(CODE)
				.setTitle(TITLE)
				.setDescription(DESCRIPTION).setContentTypesComment(CONTENT_TYPES_COMMENT)
				.setActiveRetentionComment(ACTIVE_RETENTION_COMMENT)
				.setActiveRetentionPeriod(ACTIVE_RETENTION_PERIOD).setSemiActiveRetentionComment(SEMI_ACTIVE_RETENTION_COMMENT)
				.setSemiActiveRetentionPeriod(SEMI_ACTIVE_RETENTION_PERIOD)
				.setInactiveDisposalComment(INACTIVE_DISPOSAL_COMMENT).setInactiveDisposalType(INACTIVE_DISPOSAL_TYPE)
				.setOpenActiveRetentionPeriod(OPEN_ACTIVE_RETENTION_PERIOD)
				.setEssential(REQUIRED_COPYRULE_FIELD).setId(SET_ID).setMediumTypeIds(MEDIUM_TYPE).setIgnoreActivePeriod(true);

		CopyRetentionRule copyRetentionRule3 = new CopyRetentionRule().setCopyType(CopyType.PRINCIPAL).setCode(CODE)
				.setTitle(TITLE)
				.setDescription(DESCRIPTION).setContentTypesComment(CONTENT_TYPES_COMMENT)
				.setActiveRetentionComment(ACTIVE_RETENTION_COMMENT)
				.setActiveRetentionPeriod(ACTIVE_RETENTION_PERIOD).setSemiActiveRetentionComment(SEMI_ACTIVE_RETENTION_COMMENT)
				.setSemiActiveRetentionPeriod(SEMI_ACTIVE_RETENTION_PERIOD)
				.setInactiveDisposalComment(INACTIVE_DISPOSAL_COMMENT).setInactiveDisposalType(INACTIVE_DISPOSAL_TYPE)
				.setOpenActiveRetentionPeriod(OPEN_ACTIVE_RETENTION_PERIOD)
				.setEssential(REQUIRED_COPYRULE_FIELD).setId(SET_ID).setMediumTypeIds(MEDIUM_TYPE)
				.setTypeId(records.folderTypeEmploye()).setIgnoreActivePeriod(false);

		arrayList.add(copyRetentionRule1);
		arrayList.add(copyRetentionRule2);
		arrayList.add(copyRetentionRule3);

		retentionRule.setTitle(TITLE);
		retentionRule.setCode(CODE);
		retentionRule.setResponsibleAdministrativeUnits(true);

		retentionRule.setCopyRetentionRules(arrayList);

		RecordServices recordService = getModelLayerFactory().newRecordServices();

		transaction.add(retentionRule);

		recordService.execute(transaction);

		// GetCopyRetentionRule.
		// Save avec une transaction.

		// Category.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE
		exportThenImportInAnotherCollection(
				options.setExportedSchemaTypes(
						asList(AdministrativeUnit.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE)));

		List<CopyRetentionRule> retentionRuleList = rm.getRetentionRuleWithCode(CODE).getCopyRetentionRules();

		retentionRule.getCopyRetentionRules();

		CopyRetentionRule currentCopyRetentionRule = retentionRuleList.get(0);

		// Test primary rententionRule.

		assertThat(currentCopyRetentionRule.getCopyType()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(currentCopyRetentionRule.getCode()).isEqualTo(CODE);
		assertThat(currentCopyRetentionRule.getTitle()).isEqualTo(TITLE);
		assertThat(currentCopyRetentionRule.getDescription()).isEqualTo(DESCRIPTION);
		assertThat(currentCopyRetentionRule.getContentTypesComment()).isEqualTo(CONTENT_TYPES_COMMENT);
		assertThat(currentCopyRetentionRule.getActiveRetentionPeriod()).isEqualTo(ACTIVE_RETENTION_PERIOD);
		assertThat(currentCopyRetentionRule.getSemiActiveRetentionComment()).isEqualTo(SEMI_ACTIVE_RETENTION_COMMENT);
		assertThat(currentCopyRetentionRule.getSemiActiveRetentionPeriod()).isEqualTo(SEMI_ACTIVE_RETENTION_PERIOD);
		assertThat(currentCopyRetentionRule.getInactiveDisposalComment()).isEqualTo(INACTIVE_DISPOSAL_COMMENT);
		assertThat(currentCopyRetentionRule.getInactiveDisposalType()).isEqualTo(INACTIVE_DISPOSAL_TYPE);
		assertThat(currentCopyRetentionRule.getActiveRetentionPeriod()).isEqualTo(ACTIVE_RETENTION_PERIOD);
		assertThat(currentCopyRetentionRule.isEssential()).isEqualTo(REQUIRED_COPYRULE_FIELD);
		assertThat(currentCopyRetentionRule.getId()).isEqualTo(SET_ID);
		assertThat(currentCopyRetentionRule.getMediumTypeIds()).isEqualTo(MEDIUM_TYPE);
		assertThat(currentCopyRetentionRule.getTypeId()).isNull();
		assertThat(currentCopyRetentionRule.isIgnoreActivePeriod()).isFalse();

		// Test secondary rententionRule.
		currentCopyRetentionRule = retentionRuleList.get(1);

		assertThat(currentCopyRetentionRule.getCopyType()).isEqualTo(CopyType.SECONDARY);
		assertThat(currentCopyRetentionRule.getCode()).isEqualTo(CODE);
		assertThat(currentCopyRetentionRule.getTitle()).isEqualTo(TITLE);
		assertThat(currentCopyRetentionRule.getDescription()).isEqualTo(DESCRIPTION);
		assertThat(currentCopyRetentionRule.getContentTypesComment()).isEqualTo(CONTENT_TYPES_COMMENT);
		assertThat(currentCopyRetentionRule.getActiveRetentionPeriod()).isEqualTo(ACTIVE_RETENTION_PERIOD);
		assertThat(currentCopyRetentionRule.getSemiActiveRetentionComment()).isEqualTo(SEMI_ACTIVE_RETENTION_COMMENT);
		assertThat(currentCopyRetentionRule.getSemiActiveRetentionPeriod()).isEqualTo(SEMI_ACTIVE_RETENTION_PERIOD);
		assertThat(currentCopyRetentionRule.getInactiveDisposalComment()).isEqualTo(INACTIVE_DISPOSAL_COMMENT);
		assertThat(currentCopyRetentionRule.getInactiveDisposalType()).isEqualTo(INACTIVE_DISPOSAL_TYPE);
		assertThat(currentCopyRetentionRule.getActiveRetentionPeriod()).isEqualTo(ACTIVE_RETENTION_PERIOD);
		assertThat(currentCopyRetentionRule.isEssential()).isEqualTo(REQUIRED_COPYRULE_FIELD);
		assertThat(currentCopyRetentionRule.getId()).isEqualTo(SET_ID);
		assertThat(currentCopyRetentionRule.getMediumTypeIds()).isEqualTo(MEDIUM_TYPE);
		assertThat(currentCopyRetentionRule.getTitle()).isEqualTo(TITLE);
		assertThat(currentCopyRetentionRule.getCode()).isEqualTo(CODE);
		assertThat(currentCopyRetentionRule.getTypeId()).isNull();
		assertThat(currentCopyRetentionRule.isIgnoreActivePeriod()).isTrue();

		currentCopyRetentionRule = retentionRuleList.get(2);

		assertThat(rm.getFolderType(currentCopyRetentionRule.getTypeId()).getCode())
				.isEqualTo(records.folderTypeEmploye().getCode());

		transaction.add(retentionRule);

		RMSchemasRecordsServices rmAnotherCollection = new RMSchemasRecordsServices("anotherCollection", getAppLayerFactory());

		assertThatRecords(rmAnotherCollection.searchAdministrativeUnits(ALL)).extractingMetadatas("code", "title", "parent.code")
				.containsOnly(
						tuple("10A", "Unité 10-A", "10"), tuple("11B", "Unité 11-B", "11"), tuple("11", "Unité 11", "10"),
						tuple("12", "Unité 12", "10"), tuple("20", "Unité 20", null), tuple("30", "Unité 30", null),
						tuple("10", "Unité 10", null), tuple("30C", "Unité 30-C", "30"), tuple("12B", "Unité 12-B", "12"),
						tuple("12C", "Unité 12-C", "12"), tuple("20D", "Unité 20-D", "20"), tuple("20E", "Unité 20-E", "20")
				);

	}


	private void exportThenImportInAnotherCollection(RecordExportOptions options) {
		File zipFile = new RecordExportServices(getAppLayerFactory()).exportRecords(zeCollection, SDK_STREAM, options);
		ImportDataProvider importDataProvider = null;
		try {
			importDataProvider = XMLImportDataProvider.forZipFile(getModelLayerFactory(), zipFile);

			UserServices userServices = getModelLayerFactory().newUserServices();
			User user = userServices.getUserInCollection("admin", "anotherCollection");
			BulkImportParams importParams = BulkImportParams.STRICT();
			LoggerBulkImportProgressionListener listener = new LoggerBulkImportProgressionListener();
			try {
				new RecordsImportServices(getModelLayerFactory()).bulkImport(importDataProvider, listener, user, importParams);
			} catch (ValidationException e) {

				fail(StringUtils.join(i18n.asListOfMessages(e.getValidationErrors()), "\n"));

			}
		} finally {
			getIOLayerFactory().newIOServices().deleteQuietly(zipFile);
		}
	}
}
