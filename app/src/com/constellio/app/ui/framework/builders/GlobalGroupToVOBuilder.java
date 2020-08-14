package com.constellio.app.ui.framework.builders;

import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.SystemWideGroup;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class GlobalGroupToVOBuilder implements Serializable {

	public GlobalGroupVO build(SystemWideGroup globalGroup) {
		String code = globalGroup.getCode();
		String name = globalGroup.getName();
		Set<String> collections = new HashSet<>();
		collections.addAll(globalGroup.getCollections());
		String parent = globalGroup.getParent();
		GlobalGroupStatus status = globalGroup.getStatus();
		boolean isLogicallyDeleted = globalGroup.getLogicallyDeletedStatus();
		boolean locallyCreated = globalGroup.isLocallyCreated();


		return new GlobalGroupVO(code, name, collections, parent, isLogicallyDeleted ? GlobalGroupStatus.INACTIVE : GlobalGroupStatus.ACTIVE, locallyCreated);
	}
}
