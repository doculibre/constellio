package com.constellio.app.services.importExport.settings.model;

import com.constellio.model.entities.Language;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImportedTaxonomy {

	private String code;
	private Map<Language,String> title;
	private List<String> classifiedTypes = new ArrayList<>();
	private Boolean visibleOnHomePage;
	private List<String> userIds = new ArrayList<>();
	private List<String> groupIds = new ArrayList<>();

	public ImportedTaxonomy setCode(String code) {
		this.code = code;
		return this;
	}

	public ImportedTaxonomy setTitle(Map<Language, String> title) {
		this.title = title;
		return this;
	}

	public String getTitle(Language language) {
		return title.get(language);
	}

	public Map<Language, String> getTitle() {
		return title;
	}

	public List<Language> getTitleLanguage() {
		List<Language> languageList = new ArrayList<>();
		languageList.addAll(title.keySet());
		return languageList;
	}

	public ImportedTaxonomy setClassifiedTypes(List classifiedTypes) {
		this.classifiedTypes = classifiedTypes;
		return this;
	}

	public ImportedTaxonomy setVisibleOnHomePage(Boolean visibleOnHomePage) {
		this.visibleOnHomePage = visibleOnHomePage;
		return this;
	}

	public Boolean getVisibleOnHomePage() {
		return visibleOnHomePage;
	}

	public ImportedTaxonomy setUserIds(List<String> users) {
		this.userIds = users;
		return this;
	}

	public List<String> getUserIds() {
		return userIds;
	}

	public String getCode() {
		return code;
	}

	public ImportedTaxonomy setGroupIds(List<String> groups) {
		this.groupIds = groups;
		return this;
	}

	public List<String> getGroupIds() {
		return groupIds;
	}

	public List<String> getClassifiedTypes() {
		return classifiedTypes;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);

	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
