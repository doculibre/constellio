package com.constellio.model.entities;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class Taxonomy implements CollectionObject, Serializable {

	private final String code;

	private final Map<Language, String> title;

	private final List<String> schemaTypes;

	private final boolean visibleInHomePage;

	private final List<String> userIds;

	private final List<String> groupIds;

	private final String collection;

	private final boolean showParentsInSearchResults;

	public Taxonomy(String code, Map<Language, String> title, String collection, String taxonomySchemaType) {
		this(code, title, collection, false, new ArrayList<String>(), new ArrayList<String>(), asList(taxonomySchemaType), false);
	}

	public Taxonomy(String code, Map<Language, String> title, String collection, boolean visibleInHomePage,
					List<String> userIds, List<String> groupIds, String taxonomySchemaType) {
		this(code, title, collection, visibleInHomePage, userIds, groupIds, asList(taxonomySchemaType), false);
	}

	public Taxonomy(String code, Map<Language, String> title, String collection, boolean visibleInHomePage,
					List<String> userIds, List<String> groupIds, List<String> taxonomySchemaTypes,
					boolean showParentsInSearchResults) {
		this.code = code;
		if (title != null) {
			this.title = new HashMap(title);
		} else {
			this.title = new HashMap<>();
		}
		this.collection = collection;
		this.visibleInHomePage = visibleInHomePage;
		this.userIds = Collections.unmodifiableList(userIds);
		this.groupIds = Collections.unmodifiableList(groupIds);
		this.schemaTypes = Collections.unmodifiableList(taxonomySchemaTypes);
		this.showParentsInSearchResults = showParentsInSearchResults;

	}

	public String getCollection() {
		return collection;
	}

	public String getCode() {
		return code;
	}

	public String getFrenchTitle() {
		return title.get(Language.French);
	}

	public String getEnglishTitle() {
		return title.get(Language.French);
	}

	public List<Language> getTitleLanguage() {
		List<Language> languageList = new ArrayList<>();
		languageList.addAll(title.keySet());
		return languageList;
	}

	public String getTitle(Language language) {
		return title.get(language);
	}

	public Map<Language, String> getTitle() {
		return title;
	}

	public List<String> getSchemaTypes() {
		return schemaTypes;
	}

	public boolean isVisibleInHomePage() {
		return visibleInHomePage;
	}

	public List<String> getUserIds() {
		return userIds;
	}

	public List<String> getGroupIds() {
		return groupIds;
	}

	public boolean isShowParentsInSearchResults() {
		return showParentsInSearchResults;
	}

	@Override
	public String toString() {
		return code;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public Taxonomy withTitle(Map<Language, String> title) {
		return new Taxonomy(code, title, collection, visibleInHomePage, userIds, groupIds, schemaTypes,
				showParentsInSearchResults);
	}

	public Taxonomy withVisibleInHomeFlag(boolean visibleInHomePage) {
		return new Taxonomy(code, title, collection, visibleInHomePage, userIds, groupIds, schemaTypes,
				showParentsInSearchResults);
	}

	public Taxonomy withUserIds(List<String> userIds) {
		return new Taxonomy(code, title, collection, visibleInHomePage, userIds, groupIds, schemaTypes,
				showParentsInSearchResults);
	}

	public Taxonomy withGroupIds(List<String> groupIds) {
		return new Taxonomy(code, title, collection, visibleInHomePage, userIds, groupIds, schemaTypes,
				showParentsInSearchResults);
	}

	public Taxonomy withShownParentsInSearchResults(boolean shownParentsInSearchResults) {
		return new Taxonomy(code, title, collection, visibleInHomePage, userIds, groupIds, schemaTypes,
				shownParentsInSearchResults);
	}

	public static Taxonomy createHiddenInHomePage(String code, Map<Language, String> title, String collection,
												  String taxonomySchemaType) {
		return new Taxonomy(code, title, collection, false, new ArrayList<String>(), new ArrayList<String>(),
				asList(taxonomySchemaType), false);
	}

	public static Taxonomy createHiddenInHomePage(String code, Map<Language, String> title, String collection,
												  List<String> taxonomySchemaTypes) {
		return new Taxonomy(code, title, collection, false, new ArrayList<String>(), new ArrayList<String>(),
				taxonomySchemaTypes, false);
	}

	public static Taxonomy createPublic(String code, Map<Language, String> title, String collection,
										List<String> taxonomySchemaTypes) {
		return new Taxonomy(code, title, collection, true, new ArrayList<String>(), new ArrayList<String>(), taxonomySchemaTypes,
				false);
	}

	public static Taxonomy createPublic(String code, Map<Language, String> title, String collection,
										String taxonomySchemaType) {
		return new Taxonomy(code, title, collection, true, new ArrayList<String>(), new ArrayList<String>(),
				asList(taxonomySchemaType), false);
	}

	public static Taxonomy createHomeTaxonomyForGroups(String code, Map<Language, String> title, String collection,
													   String taxonomySchemaType,
													   List<String> groupIds) {
		return new Taxonomy(code, title, collection, true, new ArrayList<String>(), groupIds, asList(taxonomySchemaType), false);
	}

	public static Taxonomy createPublic(String code, Map<Language, String> title, String collection,
										List<String> userIds, List<String> groupIds,
										List<String> taxonomySchemaTypes, boolean isVisibleInHomePage) {
		return new Taxonomy(code, title, collection, isVisibleInHomePage, userIds, groupIds, taxonomySchemaTypes, false);
	}

	public boolean hasSameCode(Taxonomy taxonomy) {
		return taxonomy != null && code.equals(taxonomy.getCode());
	}
}
