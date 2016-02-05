package com.constellio.app.ui.framework.components.fields.upload;

import static com.constellio.app.ui.i18n.i18n.$;

import org.vaadin.easyuploads.MultiFileUpload;

public abstract class BaseMultiFileUpload extends MultiFileUpload {
	
	public static final String COMPLETE_STYLE_NAME = "base-multifileupload-completed";
	
	private String dropZoneCaption = $("BaseMultiFileUpload.dropZoneCaption");

	public BaseMultiFileUpload() {
		super();
//		
//		final CssLayout progressBars = (CssLayout) getComponent(0);
//		progressBars.addComponentDetachListener(new ComponentDetachListener() {
//			@Override
//			public void componentDetachedFromContainer(ComponentDetachEvent event) {
//				Component detachedComponent = event.getDetachedComponent();
//				detachedComponent.addStyleName(COMPLETE_STYLE_NAME);
//				progressBars.addComponent(detachedComponent, 0);
//			}
//		});
	}

	@Override
	protected String getAreaText() {
		return getDropZoneCaption();
	}

	public String getDropZoneCaption() {
		return dropZoneCaption;
	}

	public void setDropZoneCaption(String dropZoneCaption) {
		this.dropZoneCaption = dropZoneCaption;
	}

}
