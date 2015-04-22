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

public class FilingSpace extends RecordWrapper {

	public static final String SCHEMA_TYPE = "filingSpace";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String CODE = "code";

	public static final String DESCRIPTION = "description";

	public static final String USERS = "users";

	public static final String ADMINISTRATORS = "administrators";

	public static final String COMMENTS = "comments";

	public FilingSpace(Record record,
			MetadataSchemaTypes types) {
		super(record, types, DEFAULT_SCHEMA);
	}

	public FilingSpace setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public String getCode() {
		return get(CODE);
	}

	public FilingSpace setCode(String code) {
		set(CODE, code);
		return this;
	}

	public String getDescription() {
		return get(DESCRIPTION);
	}

	public FilingSpace setDescription(String description) {
		set(DESCRIPTION, description);
		return this;
	}

	public List<String> getUsers() {
		return getList(USERS);
	}

	public FilingSpace setUsers(List<?> users) {
		set(USERS, users);
		return this;
	}

	public List<String> getAdministrators() {
		return getList(ADMINISTRATORS);
	}

	public FilingSpace setAdministrators(List<?> administrators) {
		set(ADMINISTRATORS, administrators);
		return this;
	}

	public List<Comment> getComments() {
		return getList(COMMENTS);
	}

	public FilingSpace setComments(List<Comment> comments) {
		set(COMMENTS, comments);
		return this;
	}
}
