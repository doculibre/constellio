package com.constellio.app.services.migrations;

import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MigrationSchemasCreationAcceptanceTest extends ConstellioTest {

	MetadataSchemasManager metadataSchemasManager;
	com.constellio.app.services.migrations.MigrationServices migrationServices;
	CollectionsManager collectionsManager;
	ConstellioPluginManager pluginManager;
	ConstellioModulesManager modulesManager;

	@Before
	public void setUp() {
		modulesManager = getAppLayerFactory().getModulesManager();
		pluginManager = getAppLayerFactory().getPluginManager();
		migrationServices = getAppLayerFactory().newMigrationServices();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		collectionsManager = getAppLayerFactory().getCollectionsManager();
	}

	@Test
	public void whenCreateCollectionThenAllSchemasCreated()
			throws Exception {
		givenCollection(zeCollection);
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(zeCollection);
		assertThatCollectionIsOK(types);
		assertThatGroupIsOK(types);
		assertThatUserIsOK(types);
		assertThatEventIsOK(types);
	}

	private void assertThatCollectionIsOK(MetadataSchemaTypes schemaTypes)
			throws Exception {
		MetadataSchema defaultCollection = schemaTypes.getSchemaType("collection").getDefaultSchema();
		assertThat(defaultCollection.getCode()).isEqualTo("collection_default");
		assertThat(defaultCollection.getMetadata("code").isUndeletable()).isTrue();
		assertThat(defaultCollection.getMetadata("name").isUndeletable()).isTrue();
	}

	private void assertThatGroupIsOK(MetadataSchemaTypes schemaTypes) {
		MetadataSchema defaultGroup = schemaTypes.getSchemaType("group").getDefaultSchema();
		assertThat(defaultGroup.getCode()).isEqualTo("group_default");
		assertThat(defaultGroup.getMetadata("code").isUndeletable()).isTrue();
		assertThat(defaultGroup.getMetadata("roles").isUndeletable()).isTrue();
		assertThat(defaultGroup.getMetadata("roles").isMultivalue()).isTrue();
	}

	private void assertThatUserIsOK(MetadataSchemaTypes schemaTypes) {
		MetadataSchema defaultUser = schemaTypes.getSchemaType("user").getDefaultSchema();
		assertThat(defaultUser.getCode()).isEqualTo("user_default");
		assertThat(defaultUser.getMetadata("username").isUndeletable()).isTrue();
		assertThat(defaultUser.getMetadata("firstname").isUndeletable()).isTrue();
		assertThat(defaultUser.getMetadata("lastname").isUndeletable()).isTrue();
		assertThat(defaultUser.getMetadata("email").isUndeletable()).isTrue();
		assertThat(defaultUser.getMetadata("userroles").isUndeletable()).isTrue();
		assertThat(defaultUser.getMetadata("userroles").isMultivalue()).isTrue();
		assertThat(defaultUser.getMetadata("allroles").isMultivalue()).isTrue();
		assertThat(defaultUser.getMetadata("title").getDataEntry().getType()).isEqualTo(DataEntryType.CALCULATED);
	}

	private void assertThatEventIsOK(MetadataSchemaTypes schemaTypes) {
		MetadataSchema defaultEvent = schemaTypes.getSchemaType("event").getDefaultSchema();
		assertThat(defaultEvent.getCode()).isEqualTo("event_default");
		assertThat(defaultEvent.getMetadata(Event.RECORD_ID).isUndeletable()).isTrue();
		//assertThat(defaultEvent.getMetadata(Event.RECORD_SCHEMA).isUndeletable()).isTrue();
		assertThat(defaultEvent.getMetadata(Event.TYPE).isUndeletable()).isTrue();
		assertThat(defaultEvent.getMetadata(Event.USERNAME).isUndeletable()).isTrue();
	}
}
