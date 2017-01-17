package com.constellio.app.services.migrations.scripts;

import static com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails.END_DATE;
import static com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails.ROLES;
import static com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails.START_DATE;
import static com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails.SYNCED;
import static com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails.TARGET;
import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.Schemas.AUTHORIZATIONS;
import static com.constellio.model.entities.schemas.entries.DataEntryType.CALCULATED;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.TOKENS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQuery.query;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.constellio.app.modules.reports.wrapper.ReportConfig;
import com.constellio.model.entities.schemas.MetadataValueType;
import org.apache.sis.metadata.MetadataStandard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.FacetType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntry;
import com.constellio.model.entities.security.XMLAuthorizationDetails;
import com.constellio.model.entities.security.global.AuthorizationDetails;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.calculators.TokensCalculator2;
import com.constellio.model.services.schemas.calculators.TokensCalculator3;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.AuthorizationDetailsManager;

public class CoreMigrationTo_7_1 implements MigrationScript {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreMigrationTo_7_1.class);

    @Override
    public String getVersion() {
        return "7.1";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
            throws Exception {

        new CoreSchemaAlterationFor7_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
    }

    private class CoreSchemaAlterationFor7_1 extends MetadataSchemasAlterationHelper {
        public CoreSchemaAlterationFor7_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
                                          AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            MetadataSchemaBuilder builder = typesBuilder.createNewSchemaType(ReportConfig.SCHEMA_TYPE).getDefaultSchema();
            builder.create(ReportConfig.JASPERFILE).setType(MetadataValueType.STRING).setUndeletable(true).setEssential(true).defineDataEntry().asManual();
        }
    }
}
