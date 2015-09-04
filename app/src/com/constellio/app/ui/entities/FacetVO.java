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
package com.constellio.app.ui.entities;

import java.util.List;

import com.constellio.model.entities.records.wrappers.structure.FacetType;

public class FacetVO {

	String datastoreCode;

	String label;

	String id;

	FacetType type;

	List<FacetValueVO> values;

	public FacetVO(String id) {
		this.id = id;
	}

	public FacetVO(String id, String datastoreCode, String label, FacetType type,
			List<FacetValueVO> values) {
		this.id = id;
		this.datastoreCode = datastoreCode;
		this.label = label;
		this.type = type;
		this.values = values;
	}

	public String getId() {
		return id;
	}

	public String getDatastoreCode() {
		return datastoreCode;
	}

	public void setDatastoreCode(String datastoreCode) {
		this.datastoreCode = datastoreCode;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public FacetType getType() {
		return type;
	}

	public void setType(FacetType type) {
		this.type = type;
	}

	public List<FacetValueVO> getValues() {
		return values;
	}

	public void setValues(List<FacetValueVO> values) {
		this.values = values;
	}
}
