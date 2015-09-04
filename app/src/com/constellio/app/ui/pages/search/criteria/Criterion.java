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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;

public class Criterion implements Serializable, ModifiableStructure {

	public enum BooleanOperator {AND, OR, AND_NOT}

	public enum SearchOperator {EQUALS, CONTAINS_TEXT, LESSER_THAN, GREATER_THAN, BETWEEN, IS_TRUE, IS_FALSE, IN_HIERARCHY}

	private String schemaType;
	String metadataCode;
	MetadataValueType metadataType;
	String enumClassName;
	private SearchOperator searchOperator;
	private Object value;
	private Object endValue;
	private boolean leftParens;
	private boolean rightParens;
	private BooleanOperator booleanOperator;
	boolean dirty;

	public Criterion() {
	}

	public Criterion(String schemaType) {
		setSchemaType(schemaType);
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	public String getSchemaType() {
		return schemaType;
	}

	public void setSchemaType(String schemaType) {
		dirty = true;
		this.schemaType = schemaType;
		metadataCode = null;
		metadataType = null;
		value = endValue = null;
		leftParens = rightParens = false;
		booleanOperator = BooleanOperator.AND;
	}

	public String getMetadataCode() {
		return metadataCode;
	}

	public void setMetadata(String metadataCode, MetadataValueType type, String enumClassName) {
		if (MetadataVO.getCodeWithoutPrefix(metadataCode).equals(CommonMetadataBuilder.PATH)) {
			searchOperator = SearchOperator.IN_HIERARCHY;
		} else if (type.isStringOrText()) {
			searchOperator = SearchOperator.CONTAINS_TEXT;
		} else if (type == MetadataValueType.BOOLEAN) {
			searchOperator = SearchOperator.IS_TRUE;
		} else {
			searchOperator = SearchOperator.EQUALS;
		}
		value = endValue = null;
		this.metadataCode = metadataCode;
		this.metadataType = type;
		this.enumClassName = enumClassName;
		dirty = true;
	}

	public SearchOperator getSearchOperator() {
		return searchOperator;
	}

	public void setSearchOperator(SearchOperator searchOperator) {
		dirty = true;
		this.searchOperator = searchOperator;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		dirty = true;
		this.value = value;
	}

	public Object getEndValue() {
		return endValue;
	}

	public void setEndValue(Object endValue) {
		dirty = true;
		this.endValue = endValue;
	}

	public boolean isLeftParens() {
		return leftParens;
	}

	public void setLeftParens(boolean leftParens) {
		dirty = true;
		this.leftParens = leftParens;
	}

	public boolean isRightParens() {
		return rightParens;
	}

	public void setRightParens(boolean rightParens) {
		dirty = true;
		this.rightParens = rightParens;
	}

	public BooleanOperator getBooleanOperator() {
		return booleanOperator;
	}

	public void setBooleanOperator(BooleanOperator booleanOperator) {
		dirty = true;
		this.booleanOperator = booleanOperator;
	}

	public void setMetadataCode(String metadataCode) {
		dirty = true;
		this.metadataCode = metadataCode;
	}

	public MetadataValueType getMetadataType() {
		return metadataType;
	}

	public void setMetadataType(MetadataValueType metadataType) {
		dirty = true;
		this.metadataType = metadataType;
	}

	public String getEnumClassName() {
		return enumClassName;
	}

	public void setEnumClassName(String enumClassName) {
		dirty = true;
		this.enumClassName = enumClassName;
	}

	public boolean isNotEmpty() {
		return metadataCode != null && (value != null || searchOperator == SearchOperator.IS_FALSE
				|| searchOperator == SearchOperator.IS_TRUE);
	}

	public String getSchemaCode() {
		String[] splittedCode = metadataCode.split("_");
		return splittedCode[0] + "_" + splittedCode[1];
	}

	//	@Override
	//	public int hashCode() {
	//		return HashCodeBuilder.reflectionHashCode(this);
	//	}
	//
	//	@Override
	//	public boolean equals(Object obj) {
	//		return EqualsBuilder.reflectionEquals(this, obj);
	//	}

}
