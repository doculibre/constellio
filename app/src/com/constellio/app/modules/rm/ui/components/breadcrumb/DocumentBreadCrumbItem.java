package com.constellio.app.modules.rm.ui.components.breadcrumb;

import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.util.SchemaCaptionUtils;

public class DocumentBreadCrumbItem implements BreadcrumbItem {

	private String documentId;

	public DocumentBreadCrumbItem(String documentId) {
		this.documentId = documentId;
	}

	public final String getDocumentId() {
		return documentId;
	}

	@Override
	public String getLabel() {
		return SchemaCaptionUtils.getCaptionForRecordId(documentId);
	}

	@Override
	public boolean isEnabled() {
		// Always last item
		return false;
	}

}
