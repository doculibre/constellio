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
package com.constellio.model.entities.workflows.trigger;

public class Trigger {

	TriggerType triggerType;

	String triggeredSchemaCode;

	String triggeredMetadataCode;

	ActionCompletion actionCompletion;

	public Trigger(TriggerType triggerType, String triggeredSchemaCode, String triggeredMetadataCode,
			ActionCompletion actionCompletion) {
		this.triggerType = triggerType;
		this.triggeredSchemaCode = triggeredSchemaCode;
		this.triggeredMetadataCode = triggeredMetadataCode;
		this.actionCompletion = actionCompletion;
	}

	public static Trigger manual(String schemaCode) {
		return new Trigger(TriggerType.MANUAL, schemaCode, null, ActionCompletion.SUSPEND);
	}

	public TriggerType getTriggerType() {
		return triggerType;
	}

	public String getTriggeredSchemaCode() {
		return triggeredSchemaCode;
	}

	public String getTriggeredMetadataCode() {
		return triggeredMetadataCode;
	}

	public ActionCompletion getActionCompletion() {
		return actionCompletion;
	}
}
