package com.constellio.app.modules.rm.ui.pages.userDocuments;

import com.constellio.app.modules.rm.ui.pages.viewGroups.PersonnalSpaceViewGroup;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.data.utils.Builder;

import java.util.List;

public interface ListUserDocumentsView extends BaseView, PersonnalSpaceViewGroup {

	void setUserContent(List<RecordVODataProvider> dataProviders);

	void setClassifyButtonFactory(Builder<ContainerButton> classifyButtonFactory);

	void refresh();
	
	void showUploadMessage(String message);
	
	void showUploadErrorMessage(String message);

	boolean isInAWindow();

	void setBreadcrumbs(List<BreadcrumbItem> breadcrumbs);

	class UserFolderBreadcrumbItem implements BreadcrumbItem {

		private String folderId;
		private boolean enabled;

		UserFolderBreadcrumbItem(String folderId, boolean enabled) {
			this.folderId = folderId;
			this.enabled = enabled;
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
			return enabled;
		}

	}
}
