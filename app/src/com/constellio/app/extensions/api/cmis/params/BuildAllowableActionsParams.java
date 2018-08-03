package com.constellio.app.extensions.api.cmis.params;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import org.apache.chemistry.opencmis.commons.enums.Action;

import java.util.Set;

public class BuildAllowableActionsParams {

	User user;

	Record record;

	Set<Action> actions;

	public BuildAllowableActionsParams(User user, Record record, Set<Action> actions) {
		this.user = user;
		this.record = record;
		this.actions = actions;
	}

	public boolean isOfType(String type) {
		return record.getTypeCode().equals(type);
	}

	public User getUser() {
		return user;
	}

	public Record getRecord() {
		return record;
	}

	public Set<Action> getActions() {
		return actions;
	}
}
