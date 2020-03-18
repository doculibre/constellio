package com.constellio.app.modules.rm.ui.components.breadcrumb;

import com.constellio.app.ui.pages.base.BaseView;
import lombok.Getter;

@Getter
public class FolderDocumentContainerPresenterParam {

	private String recordId;
	private String taxonomyCode;
	private String containerId;
	private String favoritesId;
	private BaseView view;

	public FolderDocumentContainerPresenterParam(String recordId, String taxonomyCode, String containerId,
												 String favoritesId, BaseView view) {
		this.recordId = recordId;
		this.taxonomyCode = taxonomyCode;
		this.containerId = containerId;
		this.favoritesId = favoritesId;
		this.view = view;
	}
}
