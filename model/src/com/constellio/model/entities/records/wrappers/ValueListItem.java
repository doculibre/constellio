package com.constellio.model.entities.records.wrappers;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.List;
import java.util.Map;

public class ValueListItem extends RecordWrapper {

	public static final String CODE = "code";

	public static final String DESCRIPTION = "description";

	public static final String ABBREVIATION = "abbreviation";

	public static final String COMMENTS = "comments";

	public ValueListItem(Record record,
						 MetadataSchemaTypes types, String schemaType) {
		super(record, types, schemaType);
	}

	public ValueListItem setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public ValueListItem setTitles(Map<Language, String> titles) {
		super.setTitles(titles);
		return this;
	}

	public String getCode() {
		return get(CODE);
	}

	public ValueListItem setCode(String code) {
		set(CODE, code);
		return this;
	}

	public String getDescription() {
		return get(DESCRIPTION);
	}

	public ValueListItem setDescription(String description) {
		set(DESCRIPTION, description);
		return this;
	}

	public List<Comment> getComments() {
		return getList(COMMENTS);
	}

	public ValueListItem setComments(List<Comment> comments) {
		set(COMMENTS, comments);
		return this;
	}

	public String getAbbreviation() {
		return get(ABBREVIATION);
	}

	public ValueListItem setAbbreviation(String abbreviation) {
		set(ABBREVIATION, abbreviation);
		return this;
	}

}
