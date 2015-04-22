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
package com.constellio.model.entities.workflows.definitions;

import java.util.List;
import java.util.Map;

import com.constellio.model.entities.workflows.trigger.Trigger;

public class WorkflowConfiguration {

	String id;

	String collection;

	boolean enabled;

	Map<String, String> mapping;

	List<Trigger> triggers;

	String bpmnFilename;

	public WorkflowConfiguration(String id, String collection, boolean enabled, Map<String, String> mapping,
			List<Trigger> triggers, String bpmnFilename) {
		super();
		this.id = id;
		this.collection = collection;
		this.enabled = enabled;
		this.mapping = mapping;
		this.triggers = triggers;
		this.bpmnFilename = bpmnFilename;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Map<String, String> getMapping() {
		return mapping;
	}

	public void setMapping(Map<String, String> mapping) {
		this.mapping = mapping;
	}

	public List<Trigger> getTriggers() {
		return triggers;
	}

	public void setTriggers(List<Trigger> triggers) {
		this.triggers = triggers;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBpmnFilename() {
		return bpmnFilename;
	}

	public void setBpmnFilename(String bpmnFilename) {
		this.bpmnFilename = bpmnFilename;
	}

}
