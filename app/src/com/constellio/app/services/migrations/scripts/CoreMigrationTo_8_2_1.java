package com.constellio.app.services.migrations.scripts;

import com.constellio.app.api.admin.services.WrapperConfUpdateUtils;
import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.core.CoreTypes;
import com.constellio.app.modules.rm.model.calculators.UserDocumentContentSizeCalculator;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.KeyListMap;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.FoldersLocatorMode;
import com.constellio.model.entities.calculators.SavedSearchRestrictedCalculator;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.LegacyGlobalMetadatas;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.records.cache.CacheConfig;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.INTEGER;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class CoreMigrationTo_8_2_1 implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.2.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
		new CoreSchemaAlterationFor_8_2_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class CoreSchemaAlterationFor_8_2_1 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_8_2_1(String collection,
											  MigrationResourcesProvider migrationResourcesProvider,
											  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			Set<MetadataBuilder> booleanMetadatas = typesBuilder.getAllMetadatasOfType(BOOLEAN);
			Set<MetadataBuilder> numberMetadatas = typesBuilder.getAllMetadatasOfType(NUMBER);
			Set<MetadataBuilder> integerMetadatas = typesBuilder.getAllMetadatasOfType(INTEGER);

			for(MetadataBuilder metadata: booleanMetadatas) {
				metadata.setSearchable(false).setSchemaAutocomplete(false);
			}

			for(MetadataBuilder metadata: numberMetadatas) {
				metadata.setSearchable(false).setSchemaAutocomplete(false);
			}

			for(MetadataBuilder metadata: integerMetadatas) {
				metadata.setSearchable(false).setSchemaAutocomplete(false);
			}
		}
	}
}
