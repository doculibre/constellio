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

public class RequestTasksDataEventsProvider extends AbstractDataProvider implements EventsCategoryDataProvider {

    transient List<EventStatistics> events;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String collection;

    private String currentUserName;

    private ModelLayerFactory model;

    public RequestTasksDataEventsProvider(ModelLayerFactory modelLayerFactory, String collection,
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
        return 8;
    }

    @Override
    public String getDataTitle() {
        return $("ListEventsView.requestTask");
    }

    @Override
    public String getDataReportTitle() {
        return $("ListEventsView.requestTask");
    }

    @Override
    public String getEventType(Integer index) {
        switch (index) {
            case 0:
                return EventType.BORROW_REQUEST_FOLDER;
            case 1:
                return EventType.RETURN_REQUEST_FOLDER;
            case 2:
                return EventType.REACTIVATION_REQUEST_FOLDER;
            case 3:
                return EventType.BORROW_EXTENSION_REQUEST_FOLDER;
            case 4:
                return EventType.BORROW_REQUEST_CONTAINER;
            case 5:
                return EventType.RETURN_REQUEST_CONTAINER;
            case 6:
                return EventType.REACTIVATION_REQUEST_CONTAINER;
            case 7:
                return EventType.BORROW_EXTENSION_REQUEST_CONTAINER;
            default:
                throw new RuntimeException("Unsupported");
        }
    }

    void init(ModelLayerFactory modelLayerFactory) {
        SearchServices searchServices = modelLayerFactory.newSearchServices();
        RMEventsSearchServices rmSchemasRecordsServices = new RMEventsSearchServices(modelLayerFactory, collection);
        User currentUser = modelLayerFactory.newUserServices().getUserInCollection(this.currentUserName, this.collection);
        events = new ArrayList<>();

        EventStatistics acceptedRequest = new EventStatistics();
        acceptedRequest.setLabel($("ListEventsView.borrowRequestFolder"));
        LogicalSearchQuery query = rmSchemasRecordsServices.newFindEventByDateRangeQuery(currentUser, EventType.BORROW_REQUEST_FOLDER, this.startDate, this.endDate);
        acceptedRequest.setValue((float) searchServices.getResultsCount(query));
        events.add(acceptedRequest);

        EventStatistics refusedRequest = new EventStatistics();
        refusedRequest.setLabel($("ListEventsView.returnRequestFolder"));
        query = rmSchemasRecordsServices.newFindEventByDateRangeQuery(currentUser, EventType.RETURN_REQUEST_FOLDER, this.startDate, this.endDate);
        refusedRequest.setValue((float) searchServices.getResultsCount(query));
        events.add(refusedRequest);

        EventStatistics timeExtension = new EventStatistics();
        timeExtension.setLabel($("ListEventsView.reactivationRequestFolder"));
        query = rmSchemasRecordsServices.newFindEventByDateRangeQuery(currentUser, EventType.REACTIVATION_REQUEST_FOLDER, this.startDate, this.endDate);
        timeExtension.setValue((float) searchServices.getResultsCount(query));
        events.add(timeExtension);

        EventStatistics folderReactivation = new EventStatistics();
        folderReactivation.setLabel($("ListEventsView.borrowExtensionRequestFolder"));
        query = rmSchemasRecordsServices
                .newFindEventByDateRangeQuery(currentUser, EventType.BORROW_EXTENSION_REQUEST_FOLDER, startDate, endDate);
        folderReactivation.setValue((float) searchServices.getResultsCount(query));
        events.add(folderReactivation);

        EventStatistics borrowRequestFolder = new EventStatistics();
        borrowRequestFolder.setLabel($("ListEventsView.borrowRequestContainer"));
        query = rmSchemasRecordsServices.newFindEventByDateRangeQuery(currentUser, EventType.BORROW_REQUEST_CONTAINER, this.startDate, this.endDate);
        borrowRequestFolder.setValue((float) searchServices.getResultsCount(query));
        events.add(borrowRequestFolder);

        EventStatistics returnRequestFolder = new EventStatistics();
        returnRequestFolder.setLabel($("ListEventsView.returnRequestContainer"));
        query = rmSchemasRecordsServices.newFindEventByDateRangeQuery(currentUser, EventType.RETURN_REQUEST_CONTAINER, this.startDate, this.endDate);
        returnRequestFolder.setValue((float) searchServices.getResultsCount(query));
        events.add(returnRequestFolder);

        EventStatistics reactivationRequestFolder = new EventStatistics();
        reactivationRequestFolder.setLabel($("ListEventsView.reactivationRequestContainer"));
        query = rmSchemasRecordsServices.newFindEventByDateRangeQuery(currentUser, EventType.REACTIVATION_REQUEST_CONTAINER, this.startDate, this.endDate);
        reactivationRequestFolder.setValue((float) searchServices.getResultsCount(query));
        events.add(reactivationRequestFolder);

        EventStatistics borrowExtensionRequestFolder = new EventStatistics();
        borrowExtensionRequestFolder.setLabel($("ListEventsView.borrowExtensionRequestContainer"));
        query = rmSchemasRecordsServices
                .newFindEventByDateRangeQuery(currentUser, EventType.BORROW_EXTENSION_REQUEST_CONTAINER, startDate, endDate);
        borrowExtensionRequestFolder.setValue((float) searchServices.getResultsCount(query));
        events.add(borrowExtensionRequestFolder);
    }
}
