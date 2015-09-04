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
package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class ApprovalTask extends WorkflowTask {

	public static final String SCHEMA_LOCAL_CODE = "approval";
	public static final String SCHEMA_CODE = SCHEMA_TYPE + "_" + SCHEMA_LOCAL_CODE;

	public static final String DECISION = "decision";
	public static final String DECISION_APPROVED = "approved";
	public static final String DECISION_REFUSED = "refused";

	public ApprovalTask(Record record, MetadataSchemaTypes types) {
		super(record, types, "task_approval");
	}

	public void approve() {
		set(DECISION, DECISION_APPROVED);
	}

	public void refuse() {
		set(DECISION, DECISION_REFUSED);
	}

	public String getDecision() {
		return get(DECISION);
	}
}
