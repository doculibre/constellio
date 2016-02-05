package com.constellio.model.entities.records.wrappers;

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
