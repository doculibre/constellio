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
package com.constellio.app.modules.rm.wrappers;

import java.util.List;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;

public class Category extends RecordWrapper {

	public static final String SCHEMA_TYPE = "category";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String CODE = "code";
	public static final String DESCRIPTION = "description";

	public static final String PARENT = "parent";

	public static final String KEYWORDS = "keywords";

	public static final String COMMENTS = "comments";
	public static final String RETENTION_RULES = "retentionRules";

	public Category(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public Category setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public String getParentCategory() {
		return get(PARENT);
	}

	public Category setParent(Category category) {
		set(PARENT, category);
		return this;
	}

	public Category setParent(Record category) {
		set(PARENT, category);
		return this;
	}

	public Category setParent(String category) {
		set(PARENT, category);
		return this;
	}

	public String getCode() {
		return get(CODE);
	}

	public Category setCode(String code) {
		set(CODE, code);
		return this;
	}

	public String getDescription() {
		return get(DESCRIPTION);
	}

	public Category setDescription(String description) {
		set(DESCRIPTION, description);
		return this;
	}

	public List<String> getKeywords() {
		return getList(KEYWORDS);
	}

	public Category setKeywords(String keywords) {
		set(KEYWORDS, keywords);
		return this;
	}

	public List<Comment> getComments() {
		return getList(COMMENTS);
	}

	public Category setComments(List<Comment> comments) {
		set(COMMENTS, comments);
		return this;
	}

	public Category setRetentionRules(List<?> retentionRules) {
		set(RETENTION_RULES, retentionRules);
		return this;
	}

	public List<String> getRententionRules() {
		return getList(RETENTION_RULES);
	}

	public boolean isLinkable() {
		return getBooleanWithDefaultValue(Schemas.LINKABLE.getLocalCode(), true);
	}

}
