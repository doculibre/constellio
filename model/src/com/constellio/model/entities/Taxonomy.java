package com.constellio.model.entities;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Taxonomy implements CollectionObject {

	private final String code;

	private final String title;

	private final List<String> schemaTypes;

	private final boolean visibleInHomePage;

	private final List<String> userIds;

	private final List<String> groupIds;

	private final String collection;

	public Taxonomy(String code, String title, String collection, String taxonomySchemaType) {
		this(code, title, collection, false, new ArrayList<String>(), new ArrayList<String>(), asList(taxonomySchemaType));
	}

	public Taxonomy(String code, String title, String collection, boolean visibleInHomePage,
			List<String> userIds, List<String> groupIds, String taxonomySchemaType) {
		this(code, title, collection, visibleInHomePage, userIds, groupIds, asList(taxonomySchemaType));
	}

	public Taxonomy(String code, String title, String collection, boolean visibleInHomePage,
			List<String> userIds, List<String> groupIds, List<String> taxonomySchemaTypes) {
		this.code = code;
		this.title = title;
		this.collection = collection;
		this.visibleInHomePage = visibleInHomePage;
		this.userIds = Collections.unmodifiableList(userIds);
		this.groupIds = Collections.unmodifiableList(groupIds);
		this.schemaTypes = Collections.unmodifiableList(taxonomySchemaTypes);

	}

	public String getCollection() {
		return collection;
	}

	public String getCode() {
		return code;
	}

	public String getTitle() {
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

	public Taxonomy withTitle(String title) {
		return new Taxonomy(code, title, collection, visibleInHomePage, userIds, groupIds, schemaTypes);
	}

	public Taxonomy withVisibleInHomeFlag(boolean visibleInHomePage) {
		return new Taxonomy(code, title, collection, visibleInHomePage, userIds, groupIds, schemaTypes);
	}

	public Taxonomy withUserIds(List<String> userIds) {
		return new Taxonomy(code, title, collection, visibleInHomePage, userIds, groupIds, schemaTypes);
	}

	public Taxonomy withGroupIds(List<String> groupIds) {
		return new Taxonomy(code, title, collection, visibleInHomePage, userIds, groupIds, schemaTypes);
	}

	public static Taxonomy createHiddenInHomePage(String code, String title, String collection,
			String taxonomySchemaType) {
		return new Taxonomy(code, title, collection, false, new ArrayList<String>(), new ArrayList<String>(),
				asList(taxonomySchemaType));
	}

	public static Taxonomy createHiddenInHomePage(String code, String title, String collection,
			List<String> taxonomySchemaTypes) {
		return new Taxonomy(code, title, collection, false, new ArrayList<String>(), new ArrayList<String>(),
				taxonomySchemaTypes);
	}

	public static Taxonomy createPublic(String code, String title, String collection, List<String> taxonomySchemaTypes) {
		return new Taxonomy(code, title, collection, true, new ArrayList<String>(), new ArrayList<String>(), taxonomySchemaTypes);
	}

	public static Taxonomy createPublic(String code, String title, String collection, String taxonomySchemaType) {
		return new Taxonomy(code, title, collection, true, new ArrayList<String>(), new ArrayList<String>(),
				asList(taxonomySchemaType));
	}

	public static Taxonomy createHomeTaxonomyForGroups(String code, String title, String collection, String taxonomySchemaType,
			List<String> groupIds) {
		return new Taxonomy(code, title, collection, true, new ArrayList<String>(), groupIds, asList(taxonomySchemaType));
	}

	public static Taxonomy createPublic(String code, String title, String collection, List<String> userIds, List<String> groupIds,
			List<String> taxonomySchemaTypes, boolean isVisibleInHomePage) {
		return new Taxonomy(code, title, collection, isVisibleInHomePage, userIds, groupIds, taxonomySchemaTypes);
	}

	public boolean hasSameCode(Taxonomy taxonomy) {
		return taxonomy != null && code.equals(taxonomy.getCode());
	}
}
