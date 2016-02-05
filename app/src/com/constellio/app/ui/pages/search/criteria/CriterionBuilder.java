package com.constellio.app.ui.pages.search.criteria;

import com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator;
import com.constellio.app.ui.pages.search.criteria.Criterion.SearchOperator;
import com.constellio.model.entities.schemas.Metadata;

public class CriterionBuilder {

	private Criterion criterion;

	public CriterionBuilder(String schemaType) {
		this.criterion = new Criterion(schemaType);
	}

	public CriterionBuilder(Criterion criterion) {
		this.criterion = criterion;
	}

	public String getSchemaType() {
		return criterion.getSchemaType();
	}

	public String getMetadataCode() {
		return criterion.getMetadataCode();
	}

	public boolean isRightParens() {
		return criterion.isRightParens();
	}

	public String getSchemaCode() {
		return criterion.getSchemaCode();
	}

	public Object getValue() {
		return criterion.getValue();
	}

	public boolean isLeftParens() {
		return criterion.isLeftParens();
	}

	public CriterionBuilder withRightParens() {
		return rightParens(true);
	}

	public CriterionBuilder rightParens(boolean rightParens) {
		criterion.setRightParens(rightParens);
		return this;
	}

	public CriterionBuilder where(Metadata metadata) {
		return metadata(metadata);
	}

	public CriterionBuilder metadata(Metadata metadata) {
		criterion.setMetadata(metadata);
		return this;
	}

	public CriterionBuilder searchOperator(SearchOperator searchOperator) {
		criterion.setSearchOperator(searchOperator);
		return this;
	}

	public boolean isNotEmpty() {
		return criterion.isNotEmpty();
	}

	public CriterionBuilder booleanOperator(BooleanOperator booleanOperator) {
		criterion.setBooleanOperator(booleanOperator);
		return this;
	}

	public CriterionBuilder withLeftParens() {
		return leftParens(true);
	}

	public CriterionBuilder leftParens(boolean leftParens) {
		criterion.setLeftParens(leftParens);
		return this;
	}

	public BooleanOperator getBooleanOperator() {
		return criterion.getBooleanOperator();
	}

	public CriterionBuilder isEqualTo(Object value) {
		return searchOperator(SearchOperator.EQUALS).value(value);
	}

	public CriterionBuilder isContainingText(Object value) {
		return searchOperator(SearchOperator.CONTAINS_TEXT).value(value);
	}

	public CriterionBuilder value(Object value) {
		criterion.setValue(value);
		return this;
	}

	public Object getEndValue() {
		return criterion.getEndValue();
	}

	public CriterionBuilder endValue(Object endValue) {
		criterion.setEndValue(endValue);
		return this;
	}

	public SearchOperator getSearchOperator() {
		return criterion.getSearchOperator();
	}

	public CriterionBuilder relativeSearchCriteria(RelativeCriteria relativeCriteria) {
		criterion.setRelativeCriteria(relativeCriteria);
		return this;
	}

	public RelativeCriteria getRelativeCriteria() {
		return criterion.getRelativeCriteria();
	}

	public Criterion build() {
		return criterion;
	}
}
