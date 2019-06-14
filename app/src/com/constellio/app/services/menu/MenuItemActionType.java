package com.constellio.app.services.menu;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MenuItemActionType {

	// TODO move cart types to rm
	USER_EDIT,
	USER_GENERATE_TOKEN,
	//
	GROUP_ADD_SUBGROUP,
	GROUP_EDIT,
	GROUP_DELETE,
	//
	VALUELIST_EDIT,
	VALUELIST_ADD,
	VALUELIST_DISPLAY;
	//

}
