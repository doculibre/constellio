package com.constellio.app.ui.entities;

import com.constellio.model.entities.Language;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.application.ConstellioUI.getCurrentSessionContext;

@SuppressWarnings("serial")
public class TaxonomyVO implements Serializable {

	private String code;

	private java.util.Map<Language, String> title;

	private java.util.Map<Language, String> abbreviation;

	private List<String> schemaTypes;

	private List<String> userIds = new ArrayList<>();

	private List<String> groupIds = new ArrayList<>();

	private String collection;

	private boolean visibleInHomePage;

	private List<String> classifiedObjects;

	public TaxonomyVO() {
	}

	public TaxonomyVO(String code, Map<Language, String> title, Map<Language, String> abbreviation,
					  List<String> schemaTypes, String collection, List<String> userIds,
					  List<String> groupIds, boolean visibleInHomePage) {
		this.code = code;
		this.title = title;
		this.abbreviation = abbreviation;
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

	public Map<Language, String> getTitleMap() {
		return title;
	}

	public String getTitle(Language language) {
		return title.get(language);
	}


	public String getTitle() {
		if (title == null) {
			return null;
		}
		return title.get(Language.withCode(getCurrentSessionContext().getCurrentLocale().getLanguage()));
	}

	public void setTitle(Map<Language, String> title) {
		this.title = title;
	}

	public Map<Language, String> getAbbreviationMap() {
		return abbreviation;
	}

	public String getAbbreviation(Language language) {
		return abbreviation.get(language);
	}

	public String getAbbreviation() {
		return abbreviation != null ?
			   abbreviation.get(Language.withCode(getCurrentSessionContext().getCurrentLocale().getLanguage())) :
			   null;
	}

	public void setAbbreviation(Map<Language, String> abbreviation) {
		this.abbreviation = abbreviation;
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
