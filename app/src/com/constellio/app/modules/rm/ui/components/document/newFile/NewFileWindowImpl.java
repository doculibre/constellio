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
package com.constellio.app.modules.rm.ui.components.document.newFile;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.model.entities.records.Content;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class NewFileWindowImpl extends BaseWindow implements NewFileWindow {
	
	private List<NewFileCreatedListener> newFileCreatedListeners = new ArrayList<>();
	
	private VerticalLayout mainLayout;
	
	private Label errorLabel;
	
	private ComboBox extensionField;
	
	private TextField fileNameField;
	
	private Button createFileButton;
	
	private NewFilePresenter presenter;
	
	public NewFileWindowImpl() {
		setModal(true);
		setWidth("70%");
		setHeight("220px");
		setZIndex(null);
		
		mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.setWidth("100%");
		
		String title = $("NewFileWindow.title"); 
		setCaption(title);
		
		errorLabel = new Label();
		errorLabel.addStyleName("error-label");
		errorLabel.setVisible(false);
		
		extensionField = new ComboBox();
		extensionField.setCaption($("NewFileWindow.extension"));
		
		fileNameField = new BaseTextField();
		fileNameField.setCaption($("NewFileWindow.fileName"));
		fileNameField.setRequired(true);
		
		OnEnterKeyHandler onEnterHandler = new OnEnterKeyHandler() {
			@Override
			public void onEnterKeyPressed() {
				presenter.newFileNameSubmitted();
			}
		};
		onEnterHandler.installOn(fileNameField);
		
		createFileButton = new BaseButton($("NewFileWindow.createFile")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.newFileNameSubmitted();
			}
		};
		createFileButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		
		HorizontalLayout textFieldAndButtonLayout = new HorizontalLayout(fileNameField, createFileButton);
		textFieldAndButtonLayout.setSpacing(true);
		
		setContent(mainLayout);
		mainLayout.addComponents(errorLabel, extensionField, textFieldAndButtonLayout);
		
		presenter = new NewFilePresenter(this);
	}

	@Override
	public final String getFileName() {
		return fileNameField.getValue();
	}

	@Override
	public final String getExtension() {
		return (String) extensionField.getValue();
	}

	@Override
	public void showErrorMessage(String key, Object... args) {
		errorLabel.setVisible(true);
		errorLabel.setValue($(key, args));
	}

	@Override
	public void setSupportedExtensions(List<String> extensions) {
		for (String extension : extensions) {
			String extensionCaption = $("NewFileWindow.supportedExtensions." + extension);
			extensionField.addItem(extension);
			extensionField.setItemCaption(extension, extensionCaption);
			Resource extensionIconResource = FileIconUtils.getIcon(extension);
			if (extensionIconResource != null) {
				extensionField.setItemIcon(extension, extensionIconResource);
			}
		}
	}

	@Override
	public void addNewFileCreatedListener(NewFileCreatedListener listener) {
		newFileCreatedListeners.add(listener);
	}

	@Override
	public void removeNewFileCreatedListener(NewFileCreatedListener listener) {
		newFileCreatedListeners.remove(listener);
	}

	@Override
	public void notifyNewFileCreated(Content content) {
		for (NewFileCreatedListener newFileCreatedListener : newFileCreatedListeners) {
			newFileCreatedListener.newFileCreated(content);
		}
	}

	@Override
	public void open() {
		extensionField.setValue(null);
		fileNameField.setValue(null);
		errorLabel.setVisible(false);
		extensionField.focus();
		UI.getCurrent().addWindow(this);
	}

}
