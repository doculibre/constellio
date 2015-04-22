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
package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class UserDocument extends RecordWrapper {

	public static final String SCHEMA_TYPE = "userDocument";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String USER = "user";

	public static final String CONTENT = "content";

	public UserDocument(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public String getUser() {
		return get(USER);
	}

	public UserDocument setUser(String user) {
		set(USER, user);
		return this;
	}

	public UserDocument setUser(Record user) {
		set(USER, user);
		return this;
	}

	public UserDocument setUser(User user) {
		set(USER, user);
		return this;
	}

	public Content getContent() {
		return get(CONTENT);
	}

	public UserDocument setContent(Content content) {
		set(CONTENT, content);
		return this;
	}
}
