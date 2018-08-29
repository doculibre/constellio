package com.constellio.app.ui.entities;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.model.entities.Language;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class TaxonomyVO implements Serializable {

	private String code;

	private java.util.Map<Language, String> title;

	private List<String> schemaTypes;

	private List<String> userIds = new ArrayList<>();

	private List<String> groupIds = new ArrayList<>();

	private String collection;

	private boolean visibleInHomePage;

	private List<String> classifiedObjects;

	public TaxonomyVO() {
	}

	public TaxonomyVO(String code, java.util.Map<Language, String> title, List<String> schemaTypes, String collection,
					  List<String> userIds,
					  List<String> groupIds, boolean visibleInHomePage) {
		this.code = code;
		this.title = title;
		this.schemaTypes = schemaTypes;
		this.collection = collection;
		this.userIds = userIds;
		this.groupIds = groupIds;
		this.visibleInHomePage = visibleInHomePage;
		this.classifiedObjects = new ArrayList<>();
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public java.util.Map<Language, String> getTitleMap() {
		return title;
	}

	public String getTitle(Language language) {
		return title.get(language);
	}


	public String getTitle() {
		if (title == null) {
			return null;
		}
		return title.get(Language.withCode(ConstellioUI.getCurrentSessionContext().getCurrentLocale().getLanguage()));
	}

	public void setTitle(java.util.Map<Language, String> title) {
		this.title = title;
	}

	public List<String> getSchemaTypes() {
		return schemaTypes;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public List<String> getUserIds() {
		return userIds;
	}

	public void setUserIds(List<String> userIds) {
		this.userIds = userIds;
	}

	public List<String> getGroupIds() {
		return groupIds;
	}

	public void setGroupIds(List<String> groupIds) {
		this.groupIds = groupIds;
	}

	public boolean isVisibleInHomePage() {
		return visibleInHomePage;
	}

	public void setVisibleInHomePage(boolean visibleInHomePage) {
		this.visibleInHomePage = visibleInHomePage;
	}

	public List<String> getClassifiedObjects() {
		return classifiedObjects;
	}

	public void setClassifiedObjects(List<String> classifiedObjects) {
		this.classifiedObjects = classifiedObjects;
	}

}
