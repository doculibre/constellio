package com.constellio.app.client.entities;

import java.util.ArrayList;
import java.util.List;

public class MetadataResource {

	String code;

	String dataStoreCode;

	String label;

	Boolean enabled;

	String type;

	String allowedReference;

	Boolean defaultRequirement;

	Boolean multivalue;

	Boolean uniqueValue;

	Boolean childOfRelationship;

	Boolean searchable;

	String calculator;

	List<String> validators = new ArrayList<>();

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDataStoreCode() {
		return dataStoreCode;
	}

	public void setDataStoreCode(String dataStoreCode) {
		this.dataStoreCode = dataStoreCode;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAllowedReference() {
		return allowedReference;
	}

	public void setAllowedReference(String allowedReference) {
		this.allowedReference = allowedReference;
	}

	public Boolean getDefaultRequirement() {
		return defaultRequirement;
	}

	public void setDefaultRequirement(Boolean defaultRequirement) {
		this.defaultRequirement = defaultRequirement;
	}

	public Boolean getMultivalue() {
		return multivalue;
	}

	public void setMultivalue(Boolean multivalue) {
		this.multivalue = multivalue;
	}

	public Boolean getUniqueValue() {
		return uniqueValue;
	}

	public void setUniqueValue(Boolean uniqueValue) {
		this.uniqueValue = uniqueValue;
	}

	public Boolean getChildOfRelationship() {
		return childOfRelationship;
	}

	public void setChildOfRelationship(Boolean childOfRelationship) {
		this.childOfRelationship = childOfRelationship;
	}

	public Boolean getSearchable() {
		return searchable;
	}

	public void setSearchable(Boolean searchable) {
		this.searchable = searchable;
	}

	public String getCalculator() {
		return calculator;
	}

	public void setCalculator(String calculator) {
		this.calculator = calculator;
	}

	public List<String> getValidators() {
		return validators;
	}

	public void setValidators(List<String> validators) {
		this.validators = validators;
	}
}
