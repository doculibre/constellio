package com.constellio.app.ui.framework.data.trees;

import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.*;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.DataStoreFieldLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchCondition;
import com.constellio.model.services.taxonomies.*;
import com.constellio.model.services.users.UserServices;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.entities.schemas.Schemas.CODE;
import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasInCollectionOf;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory.schemaTypeIsNotIn;
import static com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices.visibleInTrees;

public class SmbRecordTreeNodesDataProvider implements RecordTreeNodesDataProvider {
    String taxonomyCode;
    AppLayerFactory appLayerFactory;
    TaxonomiesManager taxonomiesManager;
    MetadataSchemasManager metadataSchemasManager;
    String collection;
    SearchServices searchServices;
    ESSchemasRecordsServices esSchemasRecordsServices;


    public SmbRecordTreeNodesDataProvider(String taxonomieCode, AppLayerFactory appLayerFactory) {
        collection = ConstellioUI.getCurrentSessionContext().getCurrentCollection();
        taxonomyCode = taxonomieCode;
        this.appLayerFactory = appLayerFactory;
        this.taxonomiesManager = appLayerFactory.getModelLayerFactory().getTaxonomiesManager();
        this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
        this.esSchemasRecordsServices = new ESSchemasRecordsServices(collection, appLayerFactory);
        this.metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
    }

    @Override
    public LinkableTaxonomySearchResponse getChildrenNodes(String recordId, int start, int maxSize, FastContinueInfos infos) {
        ConnectorSmbFolder connectorSmbFolder =  esSchemasRecordsServices.getConnectorSmbFolder(recordId);

        LogicalSearchCondition logicalSearchCondition = fromAllSchemasInCollectionOf(connectorSmbFolder)
                .where(directChildOf(connectorSmbFolder)).andWhere(visibleInTrees);

        LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(logicalSearchCondition);

        logicalSearchQuery.filteredWithUser(getCurrentUser(appLayerFactory.getModelLayerFactory()));
        logicalSearchQuery.setStartRow(start);
        logicalSearchQuery.setNumberOfRows(maxSize);


        List<Record> listResults = searchServices.search(logicalSearchQuery);
        List<TaxonomySearchRecord> taxonomySearchRecords = new ArrayList<>();

        for (Record record : listResults) {
            boolean asChildren = record.getSchemaCode().startsWith(ConnectorSmbFolder.SCHEMA_TYPE);
            taxonomySearchRecords.add(new TaxonomySearchRecord(record, true, asChildren));
        }


        return new LinkableTaxonomySearchResponse(taxonomySearchRecords.size(), infos, taxonomySearchRecords);
    }

    @Override
    public LinkableTaxonomySearchResponse getRootNodes(int start, int maxSize, FastContinueInfos infos) {

        Taxonomy taxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(collection, taxonomyCode);
        LogicalSearchCondition condition = fromConceptsOf(taxonomy).where(esSchemasRecordsServices.connectorSmbFolder.parentUrl()).isNull();


        LogicalSearchQuery query = new LogicalSearchQuery(condition);
        query.filteredByStatus(StatusFilter.ACTIVES);
        query.setStartRow(start);
        query.setNumberOfRows(maxSize);
        query.setReturnedMetadatas(returnedMetadatasForRecordsIn(collection, appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection)));
        query.sortAsc(CODE).sortAsc(TITLE);
        query.filteredWithUser(getCurrentUser(appLayerFactory.getModelLayerFactory()));

        List<Record> listResults = searchServices.search(query);
        List<TaxonomySearchRecord> taxonomySearchRecords = new ArrayList<>();

        for (Record record : listResults) {
            boolean asChildren = record.getSchemaCode().startsWith(ConnectorSmbFolder.SCHEMA_TYPE);
            taxonomySearchRecords.add(new TaxonomySearchRecord(record, true, asChildren));
        }

        return new LinkableTaxonomySearchResponse(taxonomySearchRecords.size(), infos, taxonomySearchRecords);
    }

    @Override
    public String getTaxonomyCode() {
        return taxonomyCode;
    }

    OngoingLogicalSearchCondition fromConceptsOf(Taxonomy taxonomy) {
        return from(metadataSchemasManager.getSchemaTypes(taxonomy, taxonomy.getSchemaTypes()));
    }

    public ReturnedMetadatasFilter returnedMetadatasForRecordsIn(String collection,  MetadataSchemaTypes types) {

        Set<String> metadatas = new HashSet<>();
        metadatas.add(Schemas.CODE.getDataStoreCode());
        metadatas.add(Schemas.TITLE.getDataStoreCode());
        metadatas.add(Schemas.LINKABLE.getDataStoreCode());
        metadatas.add(Schemas.VISIBLE_IN_TREES.getDataStoreCode());
        metadatas.add(Schemas.TOKENS.getDataStoreCode());
        metadatas.add(Schemas.ATTACHED_ANCESTORS.getDataStoreCode());
        metadatas.add(Schemas.ALL_REMOVED_AUTHS.getDataStoreCode());

        return ReturnedMetadatasFilter.onlyFields(metadatas);
    }

    DataStoreFieldLogicalSearchCondition directChildOf(ConnectorSmbFolder connectorSmbFolder) {
        return (DataStoreFieldLogicalSearchCondition) where(esSchemasRecordsServices.connectorSmbFolder.parentUrl()).isEqualTo(connectorSmbFolder.getUrl());
    }

    public User getCurrentUser(ModelLayerFactory modelLayerFactory) {
        UserVO userVO = ConstellioUI.getCurrentSessionContext().getCurrentUser();
        UserServices userServices = modelLayerFactory.newUserServices();
        return userServices.getUserInCollection(userVO.getUsername(), collection);
    }
}
