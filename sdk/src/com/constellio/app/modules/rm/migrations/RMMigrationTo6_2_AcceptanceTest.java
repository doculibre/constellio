package com.constellio.app.modules.rm.migrations;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Test;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;

public class RMMigrationTo6_2_AcceptanceTest extends ConstellioTest {

	@Test
	public void whenMigrateFromOlderVersionThenAllCopyRetentionRuleReceivedAnId()
			throws Exception {

		givenSystemAtVersion6_1WithDocumentRules();

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		RetentionRule folderAndDocumentRule = rm.getRetentionRule("00000000309");
		RetentionRule documentRule = rm.getRetentionRule("00000000312");
		RetentionRule folderRule = rm.getRetentionRule("ruleId_4");

		assertThat(folderAndDocumentRule.getSecondaryCopy().getId()).isNotNull();
		assertThat(folderAndDocumentRule.getPrincipalCopies().get(0).getId()).isNotNull();
		assertThat(folderAndDocumentRule.getDocumentCopyRetentionRules().get(0).getId()).isNotNull();
		assertThat(folderAndDocumentRule.getDocumentCopyRetentionRules().get(1).getId()).isNotNull();

		assertThat(folderRule.getSecondaryCopy().getId()).isNotNull();
		assertThat(folderRule.getPrincipalCopies().get(0).getId()).isNotNull();

		assertThat(documentRule.getPrincipalDefaultDocumentCopyRetentionRule().getId()).isNotNull();
		assertThat(documentRule.getSecondaryDefaultDocumentCopyRetentionRule().getId()).isNotNull();
		assertThat(documentRule.getDocumentCopyRetentionRules().get(0).getId()).isNotNull();
		assertThat(documentRule.getDocumentCopyRetentionRules().get(1).getId()).isNotNull();
	}

	private void givenSystemAtVersion6_1WithDocumentRules() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "given_system_in_6.1_with_tasks,rm_modules__with_document_rules.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

}
