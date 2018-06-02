package com.constellio.app.ui.pages.statistic;

import static com.constellio.app.ui.entities.RecordVO.VIEW_MODE.TABLE;
import static com.constellio.model.entities.records.wrappers.SearchEvent.DEFAULT_SCHEMA;
import static com.constellio.model.entities.schemas.Schemas.CREATED_ON;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.Date;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.data.FacetsDataProvider;
import com.constellio.app.ui.framework.data.SearchEventVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.logging.SearchEventServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class StatisticsPresenter extends SingleSchemaBasePresenter<StatisticsView> {

    public final static String FAMOUS_REQUEST = "1";
    public final static String FAMOUS_REQUEST_WITH_RESULT = "2";
    public final static String FAMOUS_REQUEST_WITHOUT_RESULT = "3";
    public final static String FAMOUS_REQUEST_WITH_CLICK = "4";
    public final static String FAMOUS_REQUEST_WITHOUT_CLICK = "5";

    private final MetadataSchemaVO schemaVO;
    private RMSchemasRecordsServices rm;

    private String excludedRequest;
    private String statisticType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String filter;
    private String capsuleId;

    public StatisticsPresenter(StatisticsView view) {
        super(view, DEFAULT_SCHEMA);

        schemaVO =  new MetadataSchemaToVOBuilder().build(defaultSchema(), TABLE, view.getSessionContext());
        rm = new RMSchemasRecordsServices(collection, appLayerFactory);
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }

    public SearchEventVODataProvider getStatisticsDataProvider() {
        return new SearchEventVODataProvider(schemaVO, composeQuery());
    }

    public FacetsDataProvider getStatisticsFacetsDataProvider() {
        return new FacetsDataProvider() {
            @Override
            public QueryResponse getQueryResponse(Integer offset, Integer limit) {
                SearchEventServices ses = new SearchEventServices(collection, modelLayerFactory);
                switch (StringUtils.trimToEmpty(statisticType)) {
                    case FAMOUS_REQUEST:
                        return ses.getFamousRequests(collection, startDate, endDate, excludedRequest, offset, limit, filter);
                    case FAMOUS_REQUEST_WITH_RESULT:
                        return ses.getFamousRequestsWithResults(collection, startDate, endDate, excludedRequest, offset, limit, filter);
                    case FAMOUS_REQUEST_WITHOUT_RESULT:
                        return ses.getFamousRequestsWithoutResults(collection, startDate, endDate, excludedRequest, offset, limit, filter);
                    case FAMOUS_REQUEST_WITH_CLICK:
                        return ses.getFamousRequestsWithClicks(collection, startDate, endDate, excludedRequest, offset, limit, filter);
                    case FAMOUS_REQUEST_WITHOUT_CLICK:
                        return ses.getFamousRequestsWithoutClicks(collection, startDate, endDate, excludedRequest, offset, limit, filter);
                    default:
                        throw new IllegalArgumentException("Unknown statistic type: "+statisticType);
                }
            }
        };
    }

    private LogicalSearchQuery composeQuery() {
        LogicalSearchQuery query = new LogicalSearchQuery(from(rm.searchEvent.schemaType()).returnAll());
        if (startDate != null && endDate != null) {
            query.setCondition(query.getCondition().andWhere(CREATED_ON)
                    .isValueInRange(startDate, endDate));
        } else if (startDate != null) {
            query.setCondition(query.getCondition().andWhere(CREATED_ON)
                    .isGreaterOrEqualThan(startDate));
        } else if (endDate != null) {
            query.setCondition(query.getCondition().andWhere(CREATED_ON)
                    .isLessOrEqualThan(endDate));
        }

        if (StringUtils.isNotBlank(excludedRequest)) {
        	try (Scanner scanner = new Scanner(excludedRequest)) {
                while(scanner.hasNextLine()) {
                    query.setCondition(query.getCondition().andWhere(rm.searchEvent.query()).isNotEqual(StringUtils.lowerCase(StringUtils.stripAccents(scanner.nextLine()))));
                }
        	}
        }

        if (StringUtils.isNotBlank(filter)) {
            query.setCondition(query.getCondition().andWhere(rm.searchEvent.params()).isContainingText(filter));
        }
        
        if (StringUtils.isNotBlank(capsuleId)) {
            query.setCondition(query.getCondition().andWhere(rm.searchEvent.capsule()).isEqualTo(capsuleId));
        }
        query.sortDesc(Schemas.CREATED_ON);

        return query;
    }

    public void applyFilter(String excludedRequest, String statisticType, Date startDate, Date endDate, String filter, String capsuleId) {
        this.excludedRequest = excludedRequest;
        this.statisticType = statisticType;

        if (startDate != null) {
            this.startDate = LocalDate.fromDateFields(startDate);
        } else {
            this.startDate = null;
        }

        if (endDate != null) {
        	// End date is inclusive, so let's add one day
        	this.endDate = new LocalDate(endDate).plus(Days.days(1));
        } else {
            this.endDate = null;
        }

        this.filter = filter;
        this.capsuleId = capsuleId;
    }

	public void backButtonClicked() {
		view.navigate().to().searchConfiguration();
	}
}
