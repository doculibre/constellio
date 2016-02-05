package com.constellio.app.modules.robots.migrations;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class RobotsMigrationsAcceptanceTest extends ConstellioTest {

	SchemasDisplayManager schemasDisplayManager;
	Users users = new Users();

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withRobotsModule().withAllTest(users));

		schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

	}

	@Test
	public void whenMigratingThenValidSchemas()
			throws Exception {

		SchemaDisplayConfig schemaDisplayConfig = schemasDisplayManager.getSchema(zeCollection, ActionParameters.DEFAULT_SCHEMA);
		assertThat(schemaDisplayConfig.getFormMetadataCodes()).isEmpty();
	}
}
