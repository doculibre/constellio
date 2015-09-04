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

import com.constellio.data.dao.dto.records.FacetValue;

public class FacetValueVO {

	String facetId;

	String value;

	String label;

	int count;

	public FacetValueVO() {

	}

	public FacetValueVO(String facetId, FacetValue value) {
		this.facetId = facetId;
		this.value = value.getValue();
		this.count = (int) value.getQuantity();
	}

	public FacetValueVO(String facetId, FacetValue value, String label) {
		this.facetId = facetId;
		this.value = value.getValue();
		this.label = label;
		this.count = (int) value.getQuantity();
	}

	public FacetValueVO(String facetId, String value, String label, int count) {
		this.facetId = facetId;
		this.value = value;
		this.label = label;
		this.count = count;
	}

	public String getFacetId() {
		return facetId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
