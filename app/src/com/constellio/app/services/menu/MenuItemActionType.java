package com.constellio.app.services.menu;

import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Getter
@AllArgsConstructor
public enum MenuItemActionType {

	// TODO move cart types to rm
	CART_BATCH_DOCUMENT(MenuItemActionCategory.CART, emptyList()),
	CART_BATCH_FOLDER(MenuItemActionCategory.CART, emptyList()),
	CART_BATCH_CONTAINER(MenuItemActionCategory.CART, emptyList()),
	CART_LABEL_DOCUMENT(MenuItemActionCategory.CART, emptyList()),
	CART_LABEL_FOLDER(MenuItemActionCategory.CART, emptyList()),
	CART_LABEL_CONTAINER(MenuItemActionCategory.CART, emptyList()),
	CART_EMPTY(MenuItemActionCategory.CART, emptyList()),
	CART_SHARE(MenuItemActionCategory.CART, emptyList()),
	CART_DECOMMISSIONING_LIST(MenuItemActionCategory.CART, emptyList()),
	CART_GENERATE_REPORT(MenuItemActionCategory.CART, emptyList()),
	CART_SIP_ARCHIVE(MenuItemActionCategory.CART, emptyList()),
	CART_CREATE_PDF(MenuItemActionCategory.CART, emptyList()),
	//
	USER_EDIT(MenuItemActionCategory.USER, emptyList()),
	USER_GENERATE_TOKEN(MenuItemActionCategory.USER, emptyList()),
	//
	GROUP_ADD_SUBGROUP(MenuItemActionCategory.GROUP, emptyList()),
	GROUP_EDIT(MenuItemActionCategory.GROUP, emptyList()),
	GROUP_DELETE(MenuItemActionCategory.GROUP, emptyList()),
	//
	VALUELIST_EDIT(MenuItemActionCategory.VALUELIST, emptyList()),
	VALUELIST_ADD(MenuItemActionCategory.VALUELIST, emptyList()),
	VALUELIST_DISPLAY(MenuItemActionCategory.VALUELIST, emptyList()),
	//
	// selection menu
	MULTIPLE_ADD_CART(MenuItemActionCategory.MULTIPLE, asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE)),
	MULTIPLE_MOVE(MenuItemActionCategory.MULTIPLE, asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)),
	MULTIPLE_COPY(MenuItemActionCategory.MULTIPLE, asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)),
	MULTIPLE_GENERATE_REPORT(MenuItemActionCategory.MULTIPLE, asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)),
	MULTIPLE_SIP_ARCHIVE(MenuItemActionCategory.MULTIPLE, asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)),
	MULTIPLE_SEND_EMAIL(MenuItemActionCategory.MULTIPLE, singletonList(Document.SCHEMA_TYPE)),
	MULTIPLE_CREATE_PDF(MenuItemActionCategory.MULTIPLE, singletonList(Document.SCHEMA_TYPE)),
	// search menu
	MULTIPLE_BATCH(MenuItemActionCategory.MULTIPLE, asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE)),
	MULTIPLE_LABEL(MenuItemActionCategory.MULTIPLE, asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE)),
	MULTIPLE_ADD_SELECTION(MenuItemActionCategory.MULTIPLE, emptyList()),
	MULTIPLE_DOWNLOAD(MenuItemActionCategory.MULTIPLE, emptyList());

	private final MenuItemActionCategory category;
	private final List<String> schemaTypes;

	private static final Map<MenuItemActionCategory, List<MenuItemActionType>> actionTypesByCategory =
			Collections.unmodifiableMap(buildMap());

	private static Map<MenuItemActionCategory, List<MenuItemActionType>> buildMap() {
		return Arrays.stream(MenuItemActionType.values())
				.collect(Collectors.groupingBy(MenuItemActionType::getCategory));
	}

	public List<MenuItemActionType> getByCategory(MenuItemActionCategory category) {
		return actionTypesByCategory.get(category);
	}

}
