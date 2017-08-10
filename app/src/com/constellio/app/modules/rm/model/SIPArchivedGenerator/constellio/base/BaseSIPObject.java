package com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.base;

import com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.sip.model.SIPObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseSIPObject implements SIPObject {
	
	private String id;
	
	private String type;
	
	private String title;
	
	private List<String> metadataIds = new ArrayList<String>();
	
	private Map<String, String> metadataLabels = new HashMap<String, String>();

	private Map<String, List<String>> metadataValues = new HashMap<String, List<String>>();
	
	public BaseSIPObject(String id, String type, String title, List<String> metadataIds, Map<String, String> metadataLabels, Map<String, List<String>> metadataValues) {
		this.id = id;
		this.type = type;
		this.metadataIds = metadataIds;
		this.metadataLabels = metadataLabels;
		this.metadataValues = metadataValues;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public List<String> getMetadataIds() {
		return metadataIds;
	}

	@Override
	public String getMetadataLabel(String metadataId) {
		return metadataLabels.get(metadataId);
	}

	@Override
	public String getMetadataValue(String metadataId) {
		List<String> metadataValues = getMetadataValues(metadataId);
		return metadataValues != null && !metadataValues.isEmpty() ? metadataValues.get(0) : null;
	}
	public BaseSIPObject setId(String id) {
		this.id = id;
		return this;
	}

	public BaseSIPObject setType(String type) {
		this.type = type;
		return this;
	}

	public BaseSIPObject setTitle(String title) {
		this.title = title;
		return this;
	}

	public BaseSIPObject setMetadataIds(List<String> metadataIds) {
		this.metadataIds = metadataIds;
		return this;
	}

	public BaseSIPObject setMetadataLabels(Map<String, String> metadataLabels) {
		this.metadataLabels = metadataLabels;
		return this;
	}

	public BaseSIPObject setMetadataValues(Map<String, List<String>> metadataValues) {
		this.metadataValues = metadataValues;
		return this;
	}

	@Override
	public List<String> getMetadataValues(String metadataId) {
		return metadataValues.get(metadataId);
	}

}
