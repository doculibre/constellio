package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.structure.FacetType;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

/**
 * Created by Constellio on 2016-12-22.
 */

public class CoreMigrationTo_6_6 implements MigrationScript {
    private final static Logger LOGGER = LoggerFactory.getLogger(CoreMigrationTo_6_6.class);

    @Override
    public String getVersion() {
        return "6.6";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
            throws Exception {
        SchemasRecordsServices schemas = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
        SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
        List<Facet> facetList = schemas.wrapFacets(searchServices.search(new LogicalSearchQuery().setCondition(from(schemas.facetSchemaType())
                .whereAllConditions(
                        where(schemas.facetQuerySchema().getMetadata(Facet.FACET_TYPE)).isEqualTo(FacetType.QUERY),
                        where(schemas.facetQuerySchema().getMetadata(Facet.TITLE)).isEqualTo("Création/Modification date")
                )
        )));

        Transaction transaction = new Transaction();
        for(Facet facet: facetList) {
            transaction.update(facet.setActive(false).getWrappedRecord());
        }
        appLayerFactory.getModelLayerFactory().newRecordServices().execute(transaction);
    }
}
