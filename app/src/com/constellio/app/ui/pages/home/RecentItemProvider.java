package com.constellio.app.ui.pages.home;

import com.constellio.app.entities.navigation.PageItem.RecentItemTable.RecentItem;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.PresenterService;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.joda.time.LocalDateTime;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class RecentItemProvider implements Serializable {
	public static int DEFAULT_QUANTITY = 50;

	private transient ModelLayerFactory modelLayerFactory;
	private transient SessionContext sessionContext;
	private transient SearchServices searchServices;
	private transient MetadataSchemaTypes types;
	private transient User user;
	private final String schemaType;
	private final int quantity;

	public RecentItemProvider(ModelLayerFactory modelLayerFactory, SessionContext sessionContext, String schemaType,
							  int quantity) {
		init(modelLayerFactory, sessionContext);
		this.schemaType = schemaType;
		this.quantity = quantity;
	}

	public RecentItemProvider(ModelLayerFactory modelLayerFactory, SessionContext sessionContext, String schemaType) {
		this(modelLayerFactory, sessionContext, schemaType, DEFAULT_QUANTITY);
	}

	public List<RecentItem> getItems() {
		RecordToVOBuilder builder = new RecordToVOBuilder();
		ArrayList<RecentItem> items = new ArrayList<>();
		for (Record record : getRecentEvents()) {
			RecordVO vo = builder.build(record, VIEW_MODE.TABLE, sessionContext);
			String caption = SchemaCaptionUtils.getCaptionForRecord(record, sessionContext.getCurrentLocale(), true);
			items.add(new RecentItem(vo, caption));
		}
		return items;
	}

	private List<Record> getRecentEvents() {
		List<Event> events = fetchEvents();
		List<String> recordIds = new ArrayList<>();

		Map<String, LocalDateTime> eventsViewDateTimes = new HashMap<>();
		for (Event event : events) {
			if (!eventsViewDateTimes.containsKey(event.getRecordId())) {
				recordIds.add(event.getRecordId());
				eventsViewDateTimes.put(event.getRecordId(), event.getCreatedOn());
			}
		}

		LogicalSearchQuery query = new LogicalSearchQuery(from(getSchemaType()).where(Schemas.IDENTIFIER).isIn(recordIds))
				.filteredByStatus(StatusFilter.ACTIVES)
				.filteredWithUserRead(user)
				.setResultsProjection(new SortRecordsUsingIdsAndApplyViewDateResultsProjection(recordIds, eventsViewDateTimes));

		return searchServices.search(query);
	}

	private MetadataSchemaType getSchemaType() {
		return types.getSchemaType(schemaType);
	}

	private List<Event> fetchEvents() {
		SchemasRecordsServices schemas = new SchemasRecordsServices(sessionContext.getCurrentCollection(), modelLayerFactory);
		return schemas.wrapEvents(searchServices.search(new LogicalSearchQuery()
				.setCondition(from(schemas.eventSchema())
						.where(schemas.eventType()).isEndingWithText(schemaType.toLowerCase())
						.andWhere(schemas.eventUsername()).isEqualTo(user.getUsername()))
				.setNumberOfRows(quantity * 2)
				.sortDesc(schemas.eventCreation())));
	}

	private void init(ModelLayerFactory modelLayerFactory, SessionContext sessionContext) {
		this.modelLayerFactory = modelLayerFactory;
		this.sessionContext = sessionContext;
		user = new PresenterService(modelLayerFactory).getCurrentUser(sessionContext);
		types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(user.getCollection());
		searchServices = modelLayerFactory.newSearchServices();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init(ConstellioFactories.getInstance().getModelLayerFactory(), ConstellioUI.getCurrentSessionContext());
	}
}
