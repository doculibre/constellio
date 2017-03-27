package com.constellio.app.ui.framework.data.event.category;

import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.ui.framework.data.AbstractDataProvider;
import com.constellio.app.ui.framework.data.event.EventStatistics;
import com.constellio.app.ui.pages.events.EventsCategoryDataProvider;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class DocumentRequestDataEventsProvider extends AbstractDataProvider implements EventsCategoryDataProvider {

    transient List<EventStatistics> events;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String collection;

    private String currentUserName;

    private ModelLayerFactory model;

    public DocumentRequestDataEventsProvider(ModelLayerFactory modelLayerFactory, String collection,
                                             String currentUserName, LocalDateTime startDate, LocalDateTime endDate) {
        this.model = modelLayerFactory;
        this.collection = collection;
        this.currentUserName = currentUserName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public List<EventStatistics> getEvents() {
        if (events == null) {
            init(this.model);
        }
        return events;
    }

    @Override
    public EventStatistics getEventStatistics(Integer index) {
        return getEvents().get(index);
    }

    @Override
    public int size() {
        return 4;
    }

    @Override
    public String getDataTitle() {
        return $("ListEventsView.documentRequest");
    }

    @Override
    public String getDataReportTitle() {
        return $("ListEventsView.documentRequest");
    }

    @Override
    public String getEventType(Integer index) {
        switch (index) {
            case 0:
                return EventType.ACCEPT_BORROWING_DOCUMENT;
            case 1:
                return EventType.BORROWING_TIME_EXTENSIONS;
            case 2:
                return EventType.REFUSE_BORROWING_DOCUMENT;
            case 3:
                return EventType.REACTIVATING_FOLDER;
            default:
                throw new RuntimeException("Unsupported");
        }
    }

    void init(ModelLayerFactory modelLayerFactory) {
        SearchServices searchServices = modelLayerFactory.newSearchServices();
        RMEventsSearchServices rmSchemasRecordsServices = new RMEventsSearchServices(modelLayerFactory, collection);
        User currentUser = modelLayerFactory.newUserServices().getUserInCollection(this.currentUserName, this.collection);
        events = new ArrayList<>();

        EventStatistics timeExtension = new EventStatistics();
        timeExtension.setLabel($("ListEventsView.TimeExtentionsRequest"));
        LogicalSearchQuery query = rmSchemasRecordsServices.newFindEventByDateRangeQuery(currentUser, EventType.BORROWING_TIME_EXTENSIONS, this.startDate, this.endDate);
        timeExtension.setValue((float) searchServices.getResultsCount(query));
        events.add(timeExtension);

        EventStatistics acceptedRequest = new EventStatistics();
        acceptedRequest.setLabel($("ListEventsView.AcceptedBorrowingRequest"));
        query = rmSchemasRecordsServices.newFindEventByDateRangeQuery(currentUser, EventType.ACCEPT_BORROWING_DOCUMENT, this.startDate, this.endDate);
        acceptedRequest.setValue((float) searchServices.getResultsCount(query));
        events.add(acceptedRequest);

        EventStatistics refusedRequest = new EventStatistics();
        refusedRequest.setLabel($("ListEventsView.AcceptedBorrowingRequest"));
        query = rmSchemasRecordsServices.newFindEventByDateRangeQuery(currentUser, EventType.REFUSE_BORROWING_DOCUMENT, this.startDate, this.endDate);
        refusedRequest.setValue((float) searchServices.getResultsCount(query));
        events.add(refusedRequest);

        EventStatistics folderReactivation = new EventStatistics();
        folderReactivation.setLabel($("ListEventsView.reactivatingFolder"));
        query = rmSchemasRecordsServices
                .newFindEventByDateRangeQuery(currentUser, EventType.REACTIVATING_FOLDER, startDate, endDate);
        folderReactivation.setValue((float) searchServices.getResultsCount(query));
        events.add(folderReactivation);
    }
}
