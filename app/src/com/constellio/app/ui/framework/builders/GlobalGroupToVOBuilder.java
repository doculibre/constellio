package com.constellio.app.ui.framework.builders;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;

@SuppressWarnings("serial")
public class GlobalGroupToVOBuilder implements Serializable {

	public GlobalGroupVO build(GlobalGroup globalGroup) {
		String code = globalGroup.getCode();
		String name = globalGroup.getName();
		Set<String> collections = new HashSet<>();
		collections.addAll(globalGroup.getUsersAutomaticallyAddedToCollections());
		String parent = globalGroup.getParent();
		GlobalGroupStatus status = globalGroup.getStatus();

		return new GlobalGroupVO(code, name, collections, parent, status);
	}
}
