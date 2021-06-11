package com.constellio.app.modules.rm.wrappers.type;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ValueListItem;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.List;
import java.util.Map;

public class RequirementType extends ValueListItem {
	public static final String SCHEMA_TYPE = "ddvRequirementType";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public RequirementType(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public RequirementType setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public RequirementType setTitles(Map<Language, String> titles) {
		super.setTitles(titles);
		return this;
	}

	public RequirementType setCode(String code) {
		super.setCode(code);
		return this;
	}

	public RequirementType setDescription(String description) {
		super.setDescription(description);
		return this;
	}

	public ValueListItem setComments(List<Comment> comments) {
		super.setComments(comments);
		return this;
	}

	public ValueListItem setAbbreviation(String abbreviation) {
		super.setAbbreviation(abbreviation);
		return this;
	}
}
