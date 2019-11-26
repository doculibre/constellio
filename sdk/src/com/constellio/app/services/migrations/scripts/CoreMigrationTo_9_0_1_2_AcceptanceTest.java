package com.constellio.app.services.migrations.scripts;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.ValueListItem;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

public class CoreMigrationTo_9_0_1_2_AcceptanceTest extends ConstellioTest {

	@Test
	public void whenMigratingTo9_0_1_2_ThenAbbreviationMetadataIsAddedToTaxonomies() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "given_system_in_9.0.zip");

		getCurrentTestSession().getFactoriesTestFeatures()
				.givenSystemInState(state).withPasswordsReset()
				.withFakeEncryptionServices();

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);

		TaxonomiesManager taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();

		getAppLayerFactory().getCollectionsManager().getCollectionCodes().forEach(collection -> {
			if (collection.equals("_system_")) {
				return;
			}

			MetadataSchemaTypesBuilder typesBuilder = getModelLayerFactory().getMetadataSchemasManager().modify(collection);

			List<Taxonomy> taxonomies = new ArrayList<>(taxonomiesManager.getEnabledTaxonomies(collection));
			taxonomies.addAll(taxonomiesManager.getDisabledTaxonomies(collection));

			if (taxonomies.isEmpty()) {
				fail();
			}

			taxonomies.forEach(taxonomy -> {
				taxonomy.getSchemaTypes().forEach(schemaTypeCode -> {
					MetadataSchemaBuilder schema = typesBuilder.getDefaultSchema(schemaTypeCode);

					if (!schema.hasMetadata(ValueListItem.ABBREVIATION)) {
						fail();
					}

					for (Language language : typesBuilder.getLanguages()) {
						if (schema.getLabel(language) == null) {
							fail();
						}
					}
				});
			});
		});
	}
}
