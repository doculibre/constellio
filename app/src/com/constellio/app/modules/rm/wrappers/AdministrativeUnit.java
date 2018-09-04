package com.constellio.app.modules.rm.wrappers;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.List;
import java.util.Locale;

public class AdministrativeUnit extends RecordWrapper {

	public static final String SCHEMA_TYPE = "administrativeUnit";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String CODE = "code";
	public static final String DESCRIPTION = "description";
	public static final String PARENT = "parent";
	public static final String FILING_SPACES = "filingSpaces";
	public static final String ADRESS = "adress";
	public static final String FILING_SPACES_USERS = "filingSpacesUsers";
	public static final String FILING_SPACES_ADMINISTRATORS = "filingSpacesAdmins";
	public static final String COMMENTS = "comments";
	public static final String DECOMMISSIONING_MONTH = "decommissioningMonth";
	public static final String ANCESTORS = "unitAncestors";

	@Deprecated
	public AdministrativeUnit(Record record,
							  MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	@Deprecated
	protected AdministrativeUnit(Record record,
								 MetadataSchemaTypes types, String schemaCode) {
		super(record, types, schemaCode);
	}

	public AdministrativeUnit(Record record,
							  MetadataSchemaTypes types, Locale locale) {
		super(record, types, SCHEMA_TYPE, locale);
	}

	protected AdministrativeUnit(Record record,
								 MetadataSchemaTypes types, String schemaCode, Locale locale) {
		super(record, types, schemaCode, locale);
	}

	public AdministrativeUnit setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public AdministrativeUnit setTitle(Locale locale, String title) {
		super.setTitle(locale, title);
		return this;
	}

	public String getCode() {
		return get(CODE);
	}

	public AdministrativeUnit setCode(String code) {
		set(CODE, code);
		return this;
	}

	public String getAdress() {
		return get(ADRESS);
	}

	public AdministrativeUnit setAdress(String adress) {
		set(ADRESS, adress);
		return this;
	}

	public String getDescription() {
		return get(DESCRIPTION, locale);
	}

	public AdministrativeUnit setDescription(String description) {
		set(DESCRIPTION, description);
		return this;
	}

	public AdministrativeUnit setDescription(Locale locale, String description) {
		set(DESCRIPTION, locale, description);
		return this;
	}

	public String getParent() {
		return get(PARENT);
	}

	public AdministrativeUnit setParent(AdministrativeUnit parent) {
		set(PARENT, parent);
		return this;
	}

	public AdministrativeUnit setParent(Record parent) {
		set(PARENT, parent);
		return this;
	}

	public AdministrativeUnit setParent(String parent) {
		set(PARENT, parent);
		return this;
	}

	public List<String> getFilingSpaces() {
		return getList(FILING_SPACES);
	}

	public AdministrativeUnit setFilingSpaces(List<?> filingSpaces) {
		set(FILING_SPACES, filingSpaces);
		return this;
	}

	public List<Comment> getComments() {
		return getList(COMMENTS);
	}

	public AdministrativeUnit setComments(List<Comment> comments) {
		set(COMMENTS, comments);
		return this;
	}

	public int getDecommissioningMonth() {
		return get(DECOMMISSIONING_MONTH);
	}

	public AdministrativeUnit setDecommissioningMonth(int decommissioningMonth) {
		set(DECOMMISSIONING_MONTH, decommissioningMonth);
		return this;
	}

	public List<String> getFilingSpacesUsers() {
		return getList(FILING_SPACES_USERS);
	}

	public List<String> getFilingSpacesAdministrators() {
		return getList(FILING_SPACES_ADMINISTRATORS);
	}
}
