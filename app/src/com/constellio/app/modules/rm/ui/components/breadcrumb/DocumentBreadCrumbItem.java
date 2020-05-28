package com.constellio.app.modules.rm.ui.components.breadcrumb;

import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public class DocumentBreadCrumbItem implements BreadcrumbItem {

	private String documentId;
	private SchemaPresenterUtils schemaPresenterUtils;
	private Boolean forcedEnabled;

	public DocumentBreadCrumbItem(String documentId,
								  SchemaPresenterUtils schemaPresenterUtils) {
		this.documentId = documentId;
		this.schemaPresenterUtils = schemaPresenterUtils;
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
		boolean enabled;
		if (Boolean.TRUE.equals(forcedEnabled)) {
			Record record = schemaPresenterUtils.getRecord(documentId);
			User user = schemaPresenterUtils.getCurrentUser();
			enabled = user.hasReadAccess().on(record);
		} else {
			enabled = false;
		}
		return enabled;
	}

	public Boolean getForcedEnabled() {
		return forcedEnabled;
	}

	public DocumentBreadCrumbItem setForcedEnabled(Boolean forcedEnabled) {
		this.forcedEnabled = forcedEnabled;
		return this;
	}
}
