package com.constellio.app.ui.pages.statistic;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.data.SolrDataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.logging.SearchEventServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.joda.time.LocalDate;

import java.util.*;

import static com.constellio.app.ui.entities.RecordVO.VIEW_MODE.TABLE;
import static com.constellio.model.entities.records.wrappers.SearchEvent.DEFAULT_SCHEMA;
import static com.constellio.model.entities.schemas.Schemas.CREATED_ON;
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
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer lines;
    private String filter;

    public StatisticsPresenter(StatisticsView view) {
        super(view, DEFAULT_SCHEMA);

        schemaVO =  new MetadataSchemaToVOBuilder().build(defaultSchema(), TABLE, view.getSessionContext());
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
        return new SolrDataProvider() {
            @Override
            public QueryResponse getQueryResponse() {
                SearchEventServices ses = new SearchEventServices(collection, modelLayerFactory);
                switch (StringUtils.trimToEmpty(statisticType)) {
                    case FAMOUS_REQUEST:
                        return ses.getFamousRequests(collection, startDate, endDate, lines, excludedRequest);
                    case FAMOUS_REQUEST_WITH_RESULT:
                        return ses.getFamousRequestsWithResults(collection, startDate, endDate, lines, excludedRequest);
                    case FAMOUS_REQUEST_WITHOUT_RESULT:
                        return ses.getFamousRequestsWithoutResults(collection, startDate, endDate, lines, excludedRequest);
                    case FAMOUS_REQUEST_WITH_CLICK:
                        return ses.getFamousRequestsWithClicks(collection, startDate, endDate, lines, excludedRequest);
                    case FAMOUS_REQUEST_WITHOUT_CLICK:
                        return ses.getFamousRequestsWithoutClicks(collection, startDate, endDate, lines, excludedRequest);
                    default:
                        throw new IllegalArgumentException("Unknown statistic type: "+statisticType);
                }
            }
        };
    }

    private LogicalSearchQuery composeQuery() {
        LogicalSearchQuery query = new LogicalSearchQuery(from(rm.searchEvent.schemaType()).returnAll());
        if(startDate != null && endDate != null) {
            query.setCondition(query.getCondition().andWhere(CREATED_ON)
                    .isValueInRange(startDate, endDate));
        } else if(startDate != null) {
            query.setCondition(query.getCondition().andWhere(CREATED_ON)
                    .isGreaterOrEqualThan(startDate));
        } else if(endDate != null) {
            query.setCondition(query.getCondition().andWhere(CREATED_ON)
                    .isLessOrEqualThan(endDate));
        }

        if (StringUtils.isNotBlank(excludedRequest)) {
            Scanner scanner = new Scanner(excludedRequest);
            while(scanner.hasNextLine()) {
                query.setCondition(query.getCondition().andWhere(rm.searchEvent.query()).isNotEqual(scanner.nextLine()));
            }
        }

        return query;
    }

    public void applyFilter(String excludedRequest, String statisticType, Date startDate, Date endDate, String lines, String filter) {
        this.excludedRequest = excludedRequest;
        this.statisticType = statisticType;

        if (startDate != null) {
            this.startDate = LocalDate.fromDateFields(startDate);
        } else {
            this.startDate = null;
        }

        if (endDate != null) {
            this.endDate = LocalDate.fromDateFields(endDate);
        } else {
            this.endDate =null;
        }

        try {
            this.lines = Integer.parseInt(lines);
        } catch (NumberFormatException e) {
            this.lines = null;
        }

        this.filter = filter;
    }
}
