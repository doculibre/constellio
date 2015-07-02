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
package com.constellio.app.modules.rm.ui.components.document.fields;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.ui.components.document.newFile.NewFileWindow;
import com.constellio.app.modules.rm.ui.components.document.newFile.NewFileWindowImpl;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.components.fields.upload.ContentVersionUploadField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class DocumentContentFieldImpl extends ContentVersionUploadField implements DocumentContentField {
	
	private Button newFileButton;
	
	private NewFileWindowImpl newFileWindow;
	
	private List<NewFileClickListener> newFileClickListeners = new ArrayList<>();
	
	public DocumentContentFieldImpl() {
		super();
		
		newFileButton = new Button($("DocumentContentField.newFile"));
		newFileButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				for (NewFileClickListener newFileClickListener : newFileClickListeners) {
					newFileClickListener.newFileClicked();
				}
			}
		});
		
		getMainLayout().addComponent(newFileButton, 0);
		getMainLayout().setComponentAlignment(newFileButton, Alignment.TOP_RIGHT);
		
		newFileWindow = new NewFileWindowImpl();
	}

	@Override
	public NewFileWindow getNewFileWindow() {
		return newFileWindow;
	}

	@Override
	public ContentVersionVO getFieldValue() {
		return (ContentVersionVO) getConvertedValue();
	}

	@Override
	public void setFieldValue(Object value) {
		setInternalValue(value);
	}

	@Override
	protected boolean isDeleteTempFilesOnDetach() {
		return false;
	}

	@Override
	public void setNewFileButtonVisible(boolean visible) {
		newFileButton.setVisible(visible);
	}

	@Override
	public void addNewFileClickListener(NewFileClickListener listener) {
		newFileClickListeners.add(listener);
	}

	@Override
	public void removeNewFileClickListener(NewFileClickListener listener) {
		newFileClickListeners.remove(listener);
	}

}
