/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
