package com.constellio.app.ui.pages.base;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.pages.search.criteria.Criterion;

public interface ConstellioHeader extends Serializable {
	String getSearchExpression();

	void setSearchExpression(String expression);

	void addEmptyCriterion();

	List<Criterion> getAdvancedSearchCriteria();

	void setAdvancedSearchCriteria(List<Criterion> criteria);

	String getAdvancedSearchSchemaType();

	void setAdvancedSearchSchemaType(String schemaTypeCode);

	void selectAdvancedSearchSchemaType(String schemaTypeCode);

	ConstellioHeader hideAdvancedSearchPopup();

	ConstellioNavigator navigateTo();

	String getCollection();

	ConstellioFactories getConstellioFactories();
}
