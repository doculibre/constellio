package com.constellio.model.entities.records.wrappers;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.List;
import java.util.Map;

public class Source extends ValueListItem {

	public static final String SCHEMA_TYPE = "ddvSource";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public Source(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public Source setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public Source setTitles(Map<Language, String> titles) {
		super.setTitles(titles);
		return this;
	}

	public Source setCode(String code) {
		set(CODE, code);
		return this;
	}

	public Source setDescription(String description) {
		set(DESCRIPTION, description);
		return this;
	}

	public Source setComments(List<Comment> comments) {
		set(COMMENTS, comments);
		return this;
	}

	public Source setAbbreviation(String abbreviation) {
		set(ABBREVIATION, abbreviation);
		return this;
	}
}
