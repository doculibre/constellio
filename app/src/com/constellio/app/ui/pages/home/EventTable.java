/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.home;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.EventSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.PresenterService;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class EventTable implements Serializable {
	public static int DEFAULT_QUANTITY = 50;

	private transient ModelLayerFactory modelLayerFactory;
	private transient SessionContext sessionContext;
	private transient User user;
	private final String schemaType;
	private final String eventType;
	private final int quantity;
	private transient MetadataSchemaTypes types;

	public EventTable(ModelLayerFactory modelLayerFactory, SessionContext sessionContext, String schemaType, String eventType,
			int quantity) {
		init(modelLayerFactory, sessionContext);
		this.schemaType = schemaType;
		this.eventType = eventType;
		this.quantity = quantity;
	}

	public EventTable(ModelLayerFactory modelLayerFactory, SessionContext sessionContext, String schemaType, String eventType) {
		this(modelLayerFactory, sessionContext, schemaType, eventType, DEFAULT_QUANTITY);
	}

	public RecordVODataProvider getDataProvider() {
		List<String> wantedMetadata = Arrays.asList(CommonMetadataBuilder.TITLE, CommonMetadataBuilder.MODIFIED_ON);
		MetadataSchemaVO schema = new EventSchemaToVOBuilder()
				.build(getSchemaType().getDefaultSchema(), VIEW_MODE.TABLE, wantedMetadata, sessionContext);
		return new RecordVODataProvider(schema, new RecordToVOBuilder(), modelLayerFactory, sessionContext) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return recentEventsQuery();
			}
		};
	}

	private void init(ModelLayerFactory modelLayerFactory, SessionContext sessionContext) {
		this.modelLayerFactory = modelLayerFactory;
		this.sessionContext = sessionContext;
		user = new PresenterService(modelLayerFactory).getCurrentUser(sessionContext);
		types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(user.getCollection());
	}

	private MetadataSchemaType getSchemaType() {
		return types.getSchemaType(schemaType);
	}

	private LogicalSearchQuery recentEventsQuery() {
		List<Event> events = fetchEvents();
		List<String> recordIds = new ArrayList<>();

		Map<String, LocalDateTime> eventsViewDateTimes = new HashMap<>();
		for (Event event : events) {
			if (!eventsViewDateTimes.containsKey(event.getRecordId())) {
				recordIds.add(event.getRecordId());
				eventsViewDateTimes.put(event.getRecordId(), event.getModifiedOn());
			}
		}

		return new LogicalSearchQuery(from(getSchemaType()).where(Schemas.IDENTIFIER).isIn(recordIds))
				.filteredByStatus(StatusFilter.ACTIVES)
				.setResultsProjection(
						new SortRecordsUsingIdsAndApplyViewDateResultsProjection(recordIds, eventsViewDateTimes));
	}

	private List<Event> fetchEvents() {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		SchemasRecordsServices schemas = new SchemasRecordsServices(sessionContext.getCurrentCollection(), modelLayerFactory);

		return schemas.wrapEvents(searchServices.search(new LogicalSearchQuery()
				.setCondition(from(schemas.eventSchema())
						.where(schemas.eventType()).isEqualTo(eventType)
						.andWhere(schemas.eventUsername()).isEqualTo(user.getUsername()))
				.setNumberOfRows(quantity * 2)
				.sortDesc(schemas.eventCreation())));
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init(ConstellioFactories.getInstance().getModelLayerFactory(), ConstellioUI.getCurrentSessionContext());
	}
}
