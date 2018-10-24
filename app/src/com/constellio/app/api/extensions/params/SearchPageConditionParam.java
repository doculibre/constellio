package com.constellio.app.api.extensions.params;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.ui.Component;

public class SearchPageConditionParam {

	private Component mainComponent;
	private LogicalSearchCondition condition;
	private User user;

	public SearchPageConditionParam(Component mainComponent, LogicalSearchCondition condition, User user) {
		this.mainComponent = mainComponent;
		this.condition = condition;
		this.user = user;
	}

	public Component getMainComponent() {
		return mainComponent;
	}

	public LogicalSearchCondition getCondition() {
		return condition;
	}

	public User getUser() {
		return user;
	}
}
