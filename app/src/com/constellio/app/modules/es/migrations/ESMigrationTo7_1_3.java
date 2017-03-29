package com.constellio.app.modules.es.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.*;

/**
 * Created by Charles Blanchette on 2017-03-29.
 */
public class ESMigrationTo7_1_3 extends MigrationHelper implements MigrationScript {

    @Override
    public String getVersion() {
        return "7.1.3";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        new SchemaAlterationFor7_1_3(collection, migrationResourcesProvider, appLayerFactory);
    }

    static class SchemaAlterationFor7_1_3 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationFor7_1_3(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws RecordServicesException {
            super(collection, migrationResourcesProvider, appLayerFactory);
            alterFacets(collection, appLayerFactory, migrationResourcesProvider);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

        }

        private void alterFacets(String collection, AppLayerFactory appLayerFactory, MigrationResourcesProvider migrationResourcesProvider)
                throws RecordServicesException {
            ESSchemasRecordsServices es = new ESSchemasRecordsServices(collection, appLayerFactory);
            RecordServices recordServices = es.getModelLayerFactory().newRecordServices();

            LogicalSearchCondition condition = from(es.facet.schemaType()).where(es.facet.fieldDatastoreCode()).isEqualTo(es.connectorDocument.mimetype().getDataStoreCode());

            List<Facet> facets = es.searchFacets(condition);
            for (Facet facet : facets) {
                alterAllMimeTypeLabels(facet);
                recordServices.add(facet);
            }
        }

        private void alterAllMimeTypeLabels(Facet mimeTypeFacet) {
            modifyLabelForMimeType(mimeTypeFacet, "application/vnd.ms-powerpoint.template.macroenabled.12");
            modifyLabelForMimeType(mimeTypeFacet, "application/vnd.ms-word.document.macroenabled.12");
            modifyLabelForMimeType(mimeTypeFacet, "application/vnd.ms-word.template.macroenabled.12");
            modifyLabelForMimeType(mimeTypeFacet, "application/vnd.ms-excel");
            modifyLabelForMimeType(mimeTypeFacet, "application/vnd.ms-excel.sheet.binary.macroenabled.12");
            modifyLabelForMimeType(mimeTypeFacet, "application/vnd.ms-excel.template.macroenabled.12");
            modifyLabelForMimeType(mimeTypeFacet, "application/vnd.ms-excel.sheet.macroenabled.12");
            modifyLabelForMimeType(mimeTypeFacet, "application/vnd.openxmlformats-officedocument.presentationml.presentation");
            modifyLabelForMimeType(mimeTypeFacet, "application/vnd.openxmlformats-officedocument.presentationml.slide");
            modifyLabelForMimeType(mimeTypeFacet, "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
            modifyLabelForMimeType(mimeTypeFacet, "application/vnd.openxmlformats-officedocument.presentationml.template");
            modifyLabelForMimeType(mimeTypeFacet, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            modifyLabelForMimeType(mimeTypeFacet, "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
            modifyLabelForMimeType(mimeTypeFacet, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            modifyLabelForMimeType(mimeTypeFacet, "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
            modifyLabelForMimeType(mimeTypeFacet, "application/vnd.ms-powerpoint");
            modifyLabelForMimeType(mimeTypeFacet, "application/vnd.ms-powerpoint.presentation.macroenabled.12");
            modifyLabelForMimeType(mimeTypeFacet, "application/vnd.ms-powerpoint.slideshow.macroenabled.12");
            modifyLabelForMimeType(mimeTypeFacet, "application/vnd.visio");
            modifyLabelForMimeType(mimeTypeFacet, "application/msword");
            System.out.println(mimeTypeFacet.get("fieldValuesLabel"));
        }

        private void modifyLabelForMimeType(Facet facet, String mimeType) {
            facet.withLabel(mimeType, this.migrationResourcesProvider.get("init.facet.mimetype." + mimeType));
        }
    }
}
