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
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class HierarchicalValueListItem extends ValueListItem {

	public static final String PARENT = "parent";

	public HierarchicalValueListItem(Record record,
			MetadataSchemaTypes types, String schemaType) {
		super(record, types, schemaType);
	}

	public HierarchicalValueListItem setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public HierarchicalValueListItem setCode(String code) {
		super.setCode(code);
		return this;
	}

	public HierarchicalValueListItem setDescription(String description) {
		super.setDescription(description);
		return this;
	}

	public HierarchicalValueListItem setComments(List<Comment> comments) {
		super.setComments(comments);
		return this;
	}

	public String getParent() {
		return get(PARENT);
	}

	public HierarchicalValueListItem setParent(HierarchicalValueListItem item) {
		super.set(PARENT, item);
		return this;
	}

	public HierarchicalValueListItem setParent(Record item) {
		super.set(PARENT, item);
		return this;
	}

	public HierarchicalValueListItem setParent(String item) {
		super.set(PARENT, item);
		return this;
	}

}
