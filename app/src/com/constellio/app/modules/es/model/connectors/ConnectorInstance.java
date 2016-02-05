package com.constellio.app.modules.es.model.connectors;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.constellio.app.modules.es.model.connectors.structures.TraversalSchedule;
import com.constellio.app.modules.es.services.mapping.ConnectorField;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.structures.MapStringListStringStructure;

public class ConnectorInstance<T extends ConnectorInstance> extends RecordWrapper {

	public static final String SCHEMA_TYPE = "connectorInstance";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String CODE = "code";
	public static final String TRAVERSAL_CODE = "traversalCode";
	public static final String LAST_TRAVERSAL_ON = "lastTraversalOn";
	public static final String CONNECTOR_TYPE = "connectorType";
	public static final String ENABLED = "enabled";
	public static final String PROPERTIES_MAPPING = "propertiesMapping";
	public static final String AVAILABLE_FIELDS = "availableFields";
	public static final String TRAVERSAL_SCHEDULE = "traversalSchedule";

	public ConnectorInstance(Record record, MetadataSchemaTypes types) {
		super(record, types, "connector");
	}

	protected ConnectorInstance(Record record, MetadataSchemaTypes types, String schemaCode) {
		super(record, types, schemaCode);
	}

	public String getCode() {
		return get(CODE);
	}

	public ConnectorInstance setCode(String code) {
		this.set(CODE, code);
		return this;
	}

	@Override
	public ConnectorInstance setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public Boolean getEnabled() {
		return get(ENABLED);
	}

	public boolean isEnabled() {
		Boolean enabled = getEnabled();
		return enabled != null && enabled;
	}

	public T setEnabled(Boolean enabled) {
		set(ENABLED, enabled);
		return (T) this;
	}

	public List<ConnectorField> getAvailableFields() {
		return getList(AVAILABLE_FIELDS);
	}

	public T setAvailableFields(List<ConnectorField> availableFields) {
		this.set(AVAILABLE_FIELDS, availableFields);
		return (T) this;
	}

	public String getConnectorType() {
		return get(CONNECTOR_TYPE);
	}

	public T setConnectorType(String connectorType) {
		this.set(CONNECTOR_TYPE, connectorType);
		return (T) this;
	}

	public T setConnectorType(Record connectorType) {
		this.set(CONNECTOR_TYPE, connectorType);
		return (T) this;
	}

	public T setConnectorType(ConnectorType connectorType) {
		this.set(CONNECTOR_TYPE, connectorType);
		return (T) this;
	}

	public String getDocumentsCustomSchemaCode() {
		return getId().replace("-", "");
	}

	public MapStringListStringStructure getPropertiesMapping() {
		return get(PROPERTIES_MAPPING);
	}

	public T setPropertiesMapping(MapStringListStringStructure propertiesMapping) {
		this.set(PROPERTIES_MAPPING, propertiesMapping);
		return (T) this;
	}

	public String getTraversalCode() {
		return this.get(TRAVERSAL_CODE);
	}

	public T setTraversalCode(String traversalCode) {
		this.set(TRAVERSAL_CODE, traversalCode);
		return (T) this;
	}

	public LocalDateTime getLastTraversalOn() {
		return this.get(LAST_TRAVERSAL_ON);
	}

	public T setLastTraversalOn(LocalDateTime lastTraversalOn) {
		this.set(LAST_TRAVERSAL_ON, lastTraversalOn);
		return (T) this;
	}

	public String readToken(String code) {
		return "r" + getId() + code;
	}

	public List<TraversalSchedule> getTraversalSchedule() {
		return getList(TRAVERSAL_SCHEDULE);
	}

	public List<TraversalSchedule> getValidTraversalSchedule() {

		List<TraversalSchedule> validSchedules = new ArrayList<>();

		for (TraversalSchedule schedule : getTraversalSchedule()) {
			if (schedule.isValid()) {
				validSchedules.add(schedule);
			}
		}
		return validSchedules;
	}

	public T setTraversalSchedule(List<TraversalSchedule> traversalSchedule) {
		this.set(TRAVERSAL_SCHEDULE, traversalSchedule);
		return (T) this;
	}

	public boolean isCurrentlyRunning() {
		LocalDateTime currentTime = TimeProvider.getLocalDateTime();
		DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
		List<TraversalSchedule> traversalSchedules = getValidTraversalSchedule();
		if (traversalSchedules.isEmpty()) {
			return true;
		}
		for (TraversalSchedule schedule : traversalSchedules) {
			if (schedule.getWeekDay() == currentTime.getDayOfWeek()) {
				LocalDateTime startTime = formatter.parseLocalDateTime(schedule.getStartTime());
				LocalDateTime endTime = formatter.parseLocalDateTime(schedule.getEndTime());
				if (startTimeBeforeCurrentTime(startTime, currentTime) && endTimeAfterCurrentTime(endTime, currentTime)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean startTimeBeforeCurrentTime(LocalDateTime startTime, LocalDateTime currentTime) {
		if (startTime.getHourOfDay() == currentTime.getHourOfDay()) {
			if (startTime.getMinuteOfHour() < currentTime.getMinuteOfHour()) {
				return true;
			}
		} else if (startTime.getHourOfDay() < currentTime.getHourOfDay()) {
			return true;
		}
		return false;
	}

	private boolean endTimeAfterCurrentTime(LocalDateTime endTime, LocalDateTime currentTime) {
		if (endTime.getHourOfDay() == currentTime.getHourOfDay()) {
			if (endTime.getMinuteOfHour() > currentTime.getMinuteOfHour()) {
				return true;
			}
		} else if (endTime.getHourOfDay() > currentTime.getHourOfDay()) {
			return true;
		}
		return false;
	}
}
