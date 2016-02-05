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
