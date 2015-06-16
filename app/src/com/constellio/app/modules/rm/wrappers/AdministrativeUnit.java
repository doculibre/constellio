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

	public AdministrativeUnit(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	protected AdministrativeUnit(Record record,
			MetadataSchemaTypes types, String schemaCode) {
		super(record, types, schemaCode);
	}

	public AdministrativeUnit setTitle(String title) {
		super.setTitle(title);
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
		return get(DESCRIPTION);
	}

	public AdministrativeUnit setDescription(String description) {
		set(DESCRIPTION, description);
		return this;
	}

	public String getParent() {
		return get(PARENT);
	}

	public AdministrativeUnit setParent(AdministrativeUnit parent) {
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
