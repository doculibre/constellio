package com.constellio.app.ui.pages.statistic;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.data.SolrDataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.LocalDateTime;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class StatisticsPresenter extends SingleSchemaBasePresenter<StatisticsView> {

    public final static String FAMOUS_REQUEST = "1";
    public final static String FAMOUS_REQUEST_WITH_RESULT = "2";
    public final static String FAMOUS_REQUEST_WITHOUT_RESULT = "3";
    public final static String FAMOUS_REQUEST_WITH_CLICK = "4";
    public final static String FAMOUS_REQUEST_WITHOUT_CLICK = "5";

    private RecordToVOBuilder recordToVOBuilder = new RecordToVOBuilder();
    private final MetadataSchemaVO schemaVO;
    private RMSchemasRecordsServices rm;

    private String excludedRequest;
    private String statisticType;
    private Date startDate;
    private Date endDate;
    private String filter;

    public StatisticsPresenter(StatisticsView view) {
        super(view, SearchEvent.DEFAULT_SCHEMA);

        schemaVO =  new MetadataSchemaToVOBuilder().build(defaultSchema(), RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
        rm = new RMSchemasRecordsServices(collection, appLayerFactory);
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }

    public RecordVODataProvider getStatisticsDataProvider() {
        return new RecordVODataProvider(schemaVO, recordToVOBuilder, modelLayerFactory, view.getSessionContext()) {
            @Override
            protected LogicalSearchQuery getQuery() {
                return composeQuery();
            }

            @Override
            protected boolean isSearchCache() {
                return true;
            }
        };
    }

    public SolrDataProvider getStatisticsFacetsDataProvider() {
        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("q", "*:*"); //changer la recherche ici
        params.set("rows", "0");
        params.add("fq", "collection_s:" + collection);
        params.add("json.facet", "{'query_s': {'type':'terms', 'field':'query_s', 'facet': {'clickCount_d': 'sum(clickCount_d)'}}}");

        return new SolrDataProvider() {
            @Override
            public QueryResponse getQueryResponse() {
                return modelLayerFactory.getDataLayerFactory().newEventsDao().nativeQuery(params);
            }
        };
    }

    private LogicalSearchQuery composeQuery() {
        LogicalSearchQuery query = new LogicalSearchQuery(from(rm.searchEvent.schemaType()).returnAll());
        if(startDate != null && endDate != null) {
            query.setCondition(query.getCondition().andWhere(Schemas.CREATED_ON)
                    .isValueInRange(LocalDateTime.fromDateFields(startDate), LocalDateTime.fromDateFields(endDate)));
        } else if(startDate != null) {
            query.setCondition(query.getCondition().andWhere(Schemas.CREATED_ON)
                    .isGreaterOrEqualThan(LocalDateTime.fromDateFields(startDate)));
        } else if(endDate != null) {
            query.setCondition(query.getCondition().andWhere(Schemas.CREATED_ON)
                    .isLessOrEqualThan(LocalDateTime.fromDateFields(endDate)));
        }

        if (StringUtils.isNotBlank(excludedRequest)) {
            Scanner scanner = new Scanner(excludedRequest);
            while(scanner.hasNextLine()) {
                query.setCondition(query.getCondition().andWhere(rm.searchEvent.query()).isNotEqual(scanner.nextLine()));
            }
        }

        return query;
    }

    public boolean applyFilter(String excludedRequest, String statisticType, Date startDate, Date endDate, String filter) {
        boolean changed = !Objects.equals(this.excludedRequest, excludedRequest);
        this.excludedRequest = excludedRequest;

        changed = changed || !Objects.equals(this.statisticType, statisticType);
        this.statisticType = statisticType;

        changed = changed || !Objects.equals(this.startDate, startDate);
        this.startDate = startDate;

        changed = changed || !Objects.equals(this.endDate, endDate);
        this.endDate = endDate;

        changed = changed || !Objects.equals(this.filter, filter);
        this.filter = filter;

        return changed;
    }
}
