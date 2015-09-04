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

import com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator;
import com.constellio.app.ui.pages.search.criteria.Criterion.SearchOperator;

public class CriterionBuilder {

	private Criterion criterion;

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
}
