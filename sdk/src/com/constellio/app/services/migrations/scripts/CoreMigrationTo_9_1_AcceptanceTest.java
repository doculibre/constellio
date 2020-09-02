package com.constellio.app.services.migrations.scripts;

import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class CoreMigrationTo_9_1_AcceptanceTest extends ConstellioTest {
	@Test
	public void whenMigratingTo9_1ThenEventSchemaTypeHasVeryFewCalculatedMetadatas() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "given_system_in_8.3.zip");

		getCurrentTestSession().getFactoriesTestFeatures()
				.givenSystemInState(state).withPasswordsResetAndDisableLDAPSync()
				.withFakeEncryptionServices();

		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		assertThat(types.getDefaultSchema(Event.SCHEMA_TYPE).getAutomaticMetadatas()).extracting("localCode").isEmpty();
		assertThat(types.getDefaultSchema(SavedSearch.SCHEMA_TYPE).getAutomaticMetadatas()).extracting("localCode").isEmpty();
		assertThat(types.getDefaultSchema(SearchEvent.SCHEMA_TYPE).getAutomaticMetadatas()).extracting("localCode").isEmpty();

	}

	@Test
	public void whenCreatingNewSystemTo9_1ThenEventSchemaTypeHasVeryFewCalculatedMetadatas() {
		prepareSystem(withZeCollection().withConstellioRMModule());


		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		assertThat(types.getDefaultSchema(Event.SCHEMA_TYPE).getAutomaticMetadatas()).extracting("localCode").isEmpty();
		assertThat(types.getDefaultSchema(SavedSearch.SCHEMA_TYPE).getAutomaticMetadatas()).extracting("localCode").isEmpty();
		assertThat(types.getDefaultSchema(SearchEvent.SCHEMA_TYPE).getAutomaticMetadatas()).extracting("localCode").isEmpty();


	}
}
