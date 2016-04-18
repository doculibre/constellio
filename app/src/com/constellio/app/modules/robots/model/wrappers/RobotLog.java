package com.constellio.app.modules.robots.model.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class RobotLog extends RecordWrapper {
	public static final String SCHEMA_TYPE = "robotLog";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String ROBOT = "robot";
	public static final String COUNT = "count";

	public RobotLog(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public RobotLog setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public RobotLog setProcessRecordsCount(int process) {
		super.set(COUNT, process);
		return this;
	}

	public int getProcessRecordsCount() {
		return get(COUNT);
	}

	public String getRobot() {
		return get(ROBOT);
	}

	public RobotLog setRobot(String robotId) {
		set(ROBOT, robotId);
		return this;
	}

	public RobotLog setRobot(Robot robot) {
		set(ROBOT, robot);
		return this;
	}
}
