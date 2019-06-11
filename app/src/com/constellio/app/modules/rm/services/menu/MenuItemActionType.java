package com.constellio.app.modules.rm.services.menu;

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
	DOCUMENT_DISPLAY(MenuItemActionCategory.DOCUMENT, emptyList()),
	DOCUMENT_OPEN(MenuItemActionCategory.DOCUMENT, emptyList()),
	DOCUMENT_EDIT(MenuItemActionCategory.DOCUMENT, emptyList()),
	DOCUMENT_DOWNLOAD(MenuItemActionCategory.DOCUMENT, emptyList()),
	DOCUMENT_DELETE(MenuItemActionCategory.DOCUMENT, emptyList()),
	DOCUMENT_COPY(MenuItemActionCategory.DOCUMENT, emptyList()),
	DOCUMENT_FINALIZE(MenuItemActionCategory.DOCUMENT, emptyList()),
	DOCUMENT_PUBLISH(MenuItemActionCategory.DOCUMENT, emptyList()),
	DOCUMENT_UNPUBLISH(MenuItemActionCategory.DOCUMENT, emptyList()),
	DOCUMENT_CREATE_PDF(MenuItemActionCategory.DOCUMENT, emptyList()),
	DOCUMENT_ADD_TO_SELECTION(MenuItemActionCategory.DOCUMENT, emptyList()),
	DOCUMENT_REMOVE_TO_SELECTION(MenuItemActionCategory.DOCUMENT, emptyList()),
	DOCUMENT_ADD_TO_CART(MenuItemActionCategory.DOCUMENT, emptyList()),
	DOCUMENT_UPLOAD(MenuItemActionCategory.DOCUMENT, emptyList()),
	DOCUMENT_PRINT_LABEL(MenuItemActionCategory.DOCUMENT, emptyList()),
	DOCUMENT_CHECK_OUT(MenuItemActionCategory.DOCUMENT, emptyList()),
	DOCUMENT_CHECK_IN(MenuItemActionCategory.DOCUMENT, emptyList()),
	DOCUMENT_ADD_AUTHORIZATION(MenuItemActionCategory.DOCUMENT, emptyList()),
	DOCUMENT_GENERATE_REPORT(MenuItemActionCategory.DOCUMENT, emptyList()),
	//
	FOLDER_DISPLAY(MenuItemActionCategory.FOLDER, emptyList()),
	FOLDER_ADD_DOCUMENT(MenuItemActionCategory.FOLDER, emptyList()),
	FOLDER_MOVE(MenuItemActionCategory.FOLDER, emptyList()),
	FOLDER_ADD_SUBFOLDER(MenuItemActionCategory.FOLDER, emptyList()),
	FOLDER_EDIT(MenuItemActionCategory.FOLDER, emptyList()),
	FOLDER_DELETE(MenuItemActionCategory.FOLDER, emptyList()),
	FOLDER_COPY(MenuItemActionCategory.FOLDER, emptyList()),
	FOLDER_ADD_AUTHORIZATION(MenuItemActionCategory.FOLDER, emptyList()),
	FOLDER_SHARE(MenuItemActionCategory.FOLDER, emptyList()),
	FOLDER_ADD_TO_CART(MenuItemActionCategory.FOLDER, emptyList()),
	FOLDER_BORROW(MenuItemActionCategory.FOLDER, emptyList()),
	FOLDER_RETURN(MenuItemActionCategory.FOLDER, emptyList()),
	FOLDER_RETURN_REMAINDER(MenuItemActionCategory.FOLDER, emptyList()),
	FOLDER_AVAILABLE_ALERT(MenuItemActionCategory.FOLDER, emptyList()),
	FOLDER_PRINT_LABEL(MenuItemActionCategory.FOLDER, emptyList()),
	FOLDER_GENERATE_REPORT(MenuItemActionCategory.FOLDER, emptyList()),
	FOLDER_ADD_TO_SELECTION(MenuItemActionCategory.FOLDER, emptyList()),
	FOLDER_REMOVE_FROM_SELECTION(MenuItemActionCategory.FOLDER, emptyList()),
	//
	CONTAINER_EDIT(MenuItemActionCategory.CONTAINER, emptyList()),
	CONTAINER_SLIP(MenuItemActionCategory.CONTAINER, emptyList()),
	CONTAINER_LABELS(MenuItemActionCategory.CONTAINER, emptyList()),
	CONTAINER_ADD_TO_CART(MenuItemActionCategory.CONTAINER, emptyList()),
	CONTAINER_DELETE(MenuItemActionCategory.CONTAINER, emptyList()),
	CONTAINER_EMPTY_THE_BOX(MenuItemActionCategory.CONTAINER, emptyList()),
	//
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
