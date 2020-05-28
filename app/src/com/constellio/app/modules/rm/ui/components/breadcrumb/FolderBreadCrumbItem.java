package com.constellio.app.modules.rm.ui.components.breadcrumb;

import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public class FolderBreadCrumbItem implements BreadcrumbItem {

	private String folderId;
	private SchemaPresenterUtils schemaPresenterUtils;
	private String recordId;
	boolean forceEnableBaseItem;

	public FolderBreadCrumbItem(String folderId, SchemaPresenterUtils schemaPresenterUtils, String recordId,
								boolean forceEnableBaseItem) {
		this.folderId = folderId;
		this.schemaPresenterUtils = schemaPresenterUtils;
		this.recordId = recordId;
		this.forceEnableBaseItem = forceEnableBaseItem;
	}

	public FolderBreadCrumbItem(String folderId, SchemaPresenterUtils schemaPresenterUtils, String recordId) {
		this.folderId = folderId;
		this.schemaPresenterUtils = schemaPresenterUtils;
		this.recordId = recordId;
		this.forceEnableBaseItem = false;
	}


	public final String getFolderId() {
		return folderId;
	}

	@Override
	public String getLabel() {
		return SchemaCaptionUtils.getCaptionForRecordId(folderId);
	}

	@Override
	public boolean isEnabled() {
		boolean enabled;
		Record record = schemaPresenterUtils.getRecord(folderId);
		User user = schemaPresenterUtils.getCurrentUser();
		boolean readAccess = user.hasReadAccess().on(record);

		if (folderId.equals(recordId)) {
			enabled = Boolean.TRUE.equals(forceEnableBaseItem) && readAccess;
		} else {
			enabled = readAccess;
		}

		return enabled;
	}



	public Boolean getForcedEnabled() {
		return forceEnableBaseItem;
	}

	public FolderBreadCrumbItem setForcedEnabled(Boolean forcedEnabled) {
		this.forceEnableBaseItem = forcedEnabled;
		return this;
	}
}
