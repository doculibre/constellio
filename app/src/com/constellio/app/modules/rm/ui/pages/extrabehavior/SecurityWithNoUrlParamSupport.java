package com.constellio.app.modules.rm.ui.pages.extrabehavior;

import com.constellio.model.entities.records.wrappers.User;

public interface SecurityWithNoUrlParamSupport {
	boolean hasPageAccess(User user);
}
