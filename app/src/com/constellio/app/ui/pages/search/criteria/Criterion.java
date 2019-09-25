package com.constellio.app.ui.pages.search.criteria;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;

import java.io.Serializable;

public class Criterion implements Serializable, ModifiableStructure {
	public enum BooleanOperator {AND, OR, AND_NOT}

	public enum SearchOperator {
		EQUALS,
		CONTAINS_TEXT,
		LESSER_THAN,
		GREATER_THAN,
		BETWEEN,
		IS_TRUE,
		IS_FALSE,
		IN_HIERARCHY,
		IS_NULL,
		IS_NOT_NULL
	}

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
	private RelativeCriteria relativeCriteria = new RelativeCriteria();

	public Criterion() {
	}

	public  Criterion(String schemaType) {
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

	public void setSchemaSelected(String schemaCode) {
		dirty = true;
		metadataCode = null;
		metadataType = null;
		value = endValue = null;
		leftParens = rightParens = false;
		booleanOperator = BooleanOperator.AND;
	}

	public String getMetadataCode() {
		return metadataCode;
	}

	public void setMetadata(Metadata metadata) {
		Class<? extends Enum<?>> enumClass = metadata.getEnumClass();
		setMetadata(metadata.getCode(), metadata.getType(), enumClass == null ? null : enumClass.getName());
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
		Boolean stringValueIsNotEmpty = true;
		if (value instanceof String) {
			stringValueIsNotEmpty = !StringUtils.isBlank((String) value);
		}
		return metadataCode != null && value != null && stringValueIsNotEmpty
				|| searchOperator == SearchOperator.IS_FALSE
				|| searchOperator == SearchOperator.IS_TRUE
				|| searchOperator == SearchOperator.IS_NULL
				|| searchOperator == SearchOperator.IS_NOT_NULL
				;
	}

	public String getSchemaCode() {
		String[] splittedCode = metadataCode.split("_");
		return splittedCode[0] + "_" + splittedCode[1];
	}

	public RelativeCriteria getRelativeCriteria() {
		return relativeCriteria;
	}

	public void setRelativeCriteria(RelativeCriteria relativeCriteria) {
		dirty = true;
		this.relativeCriteria = relativeCriteria;
	}

	//	public MeasuringUnitTime getMeasuringUnitTime() {
	//		return measuringUnitTime;
	//	}
	//
	//	public void setMeasuringUnitTime(MeasuringUnitTime measuringUnitTime) {
	//		dirty = true;
	//		this.measuringUnitTime = measuringUnitTime;
	//	}
	//
	//	public Object getMeasuringUnitTimeValue() {
	//		return measuringUnitTimeValue;
	//	}
	//
	//	public void setMeasuringUnitTimeValue(Object measuringUnitTimeValue) {
	//		dirty = true;
	//		this.measuringUnitTimeValue = measuringUnitTimeValue;
	//	}

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
