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
package com.constellio.app.ui.pages.search.criteria;

import java.io.Serializable;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;

public class Criterion implements Serializable {
	public enum BooleanOperator {AND, OR, AND_NOT}

	public enum SearchOperator {EQUALS, CONTAINS_TEXT, LESSER_THAN, GREATER_THAN, BETWEEN, IS_TRUE, IS_FALSE, IN_HIERARCHY}

	private String schemaType;
	private MetadataVO metadata;
	private SearchOperator searchOperator;
	private Object value;
	private Object endValue;
	private boolean leftParens;
	private boolean rightParens;
	private BooleanOperator booleanOperator;

	public Criterion(String schemaType) {
		setSchemaType(schemaType);
	}

	public String getSchemaType() {
		return schemaType;
	}

	public void setSchemaType(String schemaType) {
		this.schemaType = schemaType;
		metadata = null;
		value = endValue = null;
		leftParens = rightParens = false;
		booleanOperator = BooleanOperator.AND;
	}

	public MetadataVO getMetadata() {
		return metadata;
	}

	public void setMetadata(MetadataVO metadata) {
		if (MetadataVO.getCodeWithoutPrefix(metadata.getCode()).equals(CommonMetadataBuilder.PATH)) {
			searchOperator = SearchOperator.IN_HIERARCHY;
		} else if (metadata.getType().isStringOrText()) {
			searchOperator = SearchOperator.CONTAINS_TEXT;
		} else if (metadata.getType() == MetadataValueType.BOOLEAN) {
			searchOperator = SearchOperator.IS_TRUE;
		} else {
			searchOperator = SearchOperator.EQUALS;
		}
		value = endValue = null;
		this.metadata = metadata;
	}

	public SearchOperator getSearchOperator() {
		return searchOperator;
	}

	public void setSearchOperator(SearchOperator searchOperator) {
		this.searchOperator = searchOperator;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getEndValue() {
		return endValue;
	}

	public void setEndValue(Object endValue) {
		this.endValue = endValue;
	}

	public boolean isLeftParens() {
		return leftParens;
	}

	public void setLeftParens(boolean leftParens) {
		this.leftParens = leftParens;
	}

	public boolean isRightParens() {
		return rightParens;
	}

	public void setRightParens(boolean rightParens) {
		this.rightParens = rightParens;
	}

	public BooleanOperator getBooleanOperator() {
		return booleanOperator;
	}

	public void setBooleanOperator(BooleanOperator booleanOperator) {
		this.booleanOperator = booleanOperator;
	}

	public boolean isNotEmpty() {
		return metadata != null && (value != null || searchOperator == SearchOperator.IS_FALSE
				|| searchOperator == SearchOperator.IS_TRUE);
	}

	public String getSchemaCode() {
		return metadata.getSchema().getCode();
	}

	public String getMetadataCode() {
		return metadata.getCode();
	}
}
