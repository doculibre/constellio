package com.constellio.app.modules.robots.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;

public class DryRunRobotAction {

	String recordId;

	String recordUrl;

	String recordTitle;

	String robotId;

	String robotCode;

	String robotTitle;

	String actionTitle;

	String robotHierarchy;

	Map<Metadata, Object> actionParameters;

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public String getRecordUrl() {
		return recordUrl;
	}

	public void setRecordUrl(String recordUrl) {
		this.recordUrl = recordUrl;
	}

	public String getRecordTitle() {
		return recordTitle;
	}

	public void setRecordTitle(String recordTitle) {
		this.recordTitle = recordTitle;
	}

	public String getRobotId() {
		return robotId;
	}

	public void setRobotId(String robotId) {
		this.robotId = robotId;
	}

	public String getRobotCode() {
		return robotCode;
	}

	public void setRobotCode(String robotCode) {
		this.robotCode = robotCode;
	}

	public String getRobotTitle() {
		return robotTitle;
	}

	public void setRobotTitle(String robotTitle) {
		this.robotTitle = robotTitle;
	}

	public String getActionTitle() {
		return actionTitle;
	}

	public void setActionTitle(String actionTitle) {
		this.actionTitle = actionTitle;
	}

	public String getRobotHierarchy() {
		return robotHierarchy;
	}

	public void setRobotHierarchy(String robotHierarchy) {
		this.robotHierarchy = robotHierarchy;
	}

	public Map<Metadata, Object> getActionParameters() {
		return actionParameters;
	}

	public void setActionParameters(Map<Metadata, Object> actionParameters) {
		this.actionParameters = actionParameters;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static DryRunRobotAction dryRunRobotAction(Record record, Robot robot,
			RobotSchemaRecordServices robotSchemaRecordServices) {

		MetadataSchemaTypes types = robotSchemaRecordServices.getTypes();

		String title = record.get(Schemas.TITLE);
		String url = record.get(Schemas.URL);
		String actionTitle = robot.getAction();

		Map<Metadata, Object> actionParameters = new HashMap<>();
		if (robot.getActionParameters() != null) {
			Record actionParametersRecord = robotSchemaRecordServices.get(robot.getActionParameters());
			MetadataSchema schema = types.getSchema(actionParametersRecord.getSchemaCode());
			for (Metadata metadata : schema.getMetadatas()) {
				if (!metadata.inheritDefaultSchema()) {
					Object value = actionParametersRecord.get(metadata);
					actionParameters.put(metadata, value);
				}
			}
		}

		String robotCodeHierarchy = robotSchemaRecordServices.getRobotCodesPath(robot, " > ");

		DryRunRobotAction action = new DryRunRobotAction();
		action.setRecordId(record.getId());
		action.setRecordTitle(title);
		action.setRecordUrl(url);
		action.setRobotId(robot.getId());
		action.setRobotCode(robot.getCode());
		action.setRobotTitle(robot.getTitle());
		action.setRobotHierarchy(robotCodeHierarchy);
		action.setActionTitle(actionTitle);
		action.setActionParameters(actionParameters);
		return action;
	}
}
