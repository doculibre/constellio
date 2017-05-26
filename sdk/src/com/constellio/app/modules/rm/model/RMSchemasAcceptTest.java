package com.constellio.app.modules.rm.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.sdk.tests.ConstellioTest;

public class RMSchemasAcceptTest extends ConstellioTest {

	@Test
	public void whenCallLogicallyThenPhysicallyDeletableCheckOnCategoriesThenGoodBehavior() {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
		);

		SchemasDisplayManager schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		assertThat(schemasDisplayManager.getReturnedFieldsForSearch(zeCollection)).containsOnly(
				"archivisticStatus_s", "title_s", "assigneeId_s", "code_s", "content_s", "modifiedOn_dt", "dueDate_da",
				"statusId_s", "description_s", "description_t", "mimetype_s", "migrationDataVersion_d"
		);
	}
}
