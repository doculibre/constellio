package com.constellio.app.ui.framework.data.event.category;

import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.ui.framework.data.event.EventStatistics;
import com.constellio.app.ui.pages.events.EventsCategoryDataProvider;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.joda.time.LocalDateTime;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

/**
 * Created by Marco on 2017-03-20.
 */
public class ReindexAndRestartEventDataProvider extends DefaultEventsDataProvider implements EventsCategoryDataProvider {
    public ReindexAndRestartEventDataProvider(ModelLayerFactory modelLayerFactory, String collection, String currentUserName, LocalDateTime startDate, LocalDateTime endDate) {
        super(modelLayerFactory, collection, currentUserName, startDate, endDate, null);
    }

    @Override
    protected int specificSize() {
        return size();
    }

    @Override
    protected LogicalSearchQuery createSpecificQuery(ModelLayerFactory modelLayerFactory, User currentUser, String eventType, LocalDateTime startDate, LocalDateTime endDate, String id) {
        RMEventsSearchServices rmSchemasRecordsServices = new RMEventsSearchServices(modelLayerFactory, collection);
        return rmSchemasRecordsServices.newFindEventByDateRangeQuery(currentUser, eventType, startDate, endDate);
    }

    @Override
    public String getDataTitle() {
        return $("ListEventsView.reIndexAndRestar");
    }

    @Override
    public String getDataReportTitle() {
        return null;
    }

    @Override
    public String getEventType(Integer index) {
        switch (index) {
            case 0:
                return EventType.REINDEXING;
            case 1:
                return EventType.RESTARTING;
            default:
                throw new RuntimeException("Unsupported");
        }
    }
}
