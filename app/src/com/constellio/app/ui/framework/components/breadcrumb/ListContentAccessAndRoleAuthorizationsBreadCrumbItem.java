package com.constellio.app.ui.framework.components.breadcrumb;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListContentAccessAndRoleAuthorizationsBreadCrumbItem implements BreadcrumbItem {

	private final String recordId;
	private final String recordAuthorizationsSchemaType;
	private RMSchemasRecordsServices rm;

	public ListContentAccessAndRoleAuthorizationsBreadCrumbItem(String recordId,
																String recordAuthorizationsSchemaType,
																RMSchemasRecordsServices rmSchemasRecordsServices) {
		this.recordId = recordId;
		this.recordAuthorizationsSchemaType = recordAuthorizationsSchemaType;
		this.rm = rmSchemasRecordsServices;
	}

	public String getRecordId() {
		return recordId;
	}

	public String getRecordAuthorizationsSchemaType() {
		return recordAuthorizationsSchemaType;
	}

	@Override
	public String getLabel() {
		return $("ListContentAccessAuthorizationsView.viewTitle", rm.get(recordId).getTitle());
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

}
