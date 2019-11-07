package com.constellio.app.modules.rm.services.menu.behaviors.util;

import static com.constellio.app.ui.i18n.i18n.$;

public class RMMessageUtil {
	public static StringBuilder getRecordCountByTypeAsText(int folderNumber, int documentNumber, int containerNumber) {

		StringBuilder stringBuilder = new StringBuilder();
		boolean areContainerDeleted = containerNumber > 0;
		boolean areDocumentDeleted = documentNumber > 0;
		boolean areFolderDeleted = folderNumber > 0;


		if (areFolderDeleted) {
			stringBuilder.append(folderNumber + " " + (folderNumber == 1 ? $("CartView.folder") : $("CartView.folders")));
		}

		if (areDocumentDeleted) {
			String prefix = " ";
			if (areFolderDeleted && areContainerDeleted) {
				prefix = ", ";
			} else if (areFolderDeleted) {
				prefix = " " + $("CartView.andAll") + " ";
			}
			stringBuilder.append(prefix + documentNumber + " " + (documentNumber == 1 ? $("CartView.document") : $("CartView.documents")));
		}

		if (areContainerDeleted) {
			String prefix = " ";
			if (areFolderDeleted || areDocumentDeleted) {
				prefix = " " + $("CartView.andAll") + " ";
			}

			stringBuilder.append(prefix + containerNumber + " " + (containerNumber == 1 ? $("CartView.container") : $("CartView.containers")));
		}
		return stringBuilder;
	}
}
