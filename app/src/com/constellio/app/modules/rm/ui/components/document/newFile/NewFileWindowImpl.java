package com.constellio.app.modules.rm.ui.components.document.newFile;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.model.entities.records.Content;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class NewFileWindowImpl extends BaseWindow implements NewFileWindow {

	private boolean opened;

	private List<NewFileCreatedListener> newFileCreatedListeners = new ArrayList<>();

	private NewFileComponent newFileComponent;

	private Button createFileButton;

	private NewFileWindowPresenter presenter;

	public NewFileWindowImpl() {
		this(false);
	}

	public NewFileWindowImpl(boolean isViewOnly) {
		setModal(true);
		setWidth("70%");
		setHeight("360px");

		newFileComponent = new NewFileComponent();

		OnEnterKeyHandler onEnterHandler = new OnEnterKeyHandler() {
			@Override
			public void onEnterKeyPressed() {
				presenter.newFileNameSubmitted();
			}
		};
		onEnterHandler.installOn(newFileComponent.getFileNameField());

		createFileButton = new BaseButton($("NewFileWindow.createFile")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.newFileNameSubmitted();
			}
		};
		createFileButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setWidth("98%");
		buttonLayout.addComponent(createFileButton);
		buttonLayout.setSpacing(true);
		buttonLayout.setComponentAlignment(createFileButton, Alignment.MIDDLE_CENTER);

		setContent(newFileComponent);
		newFileComponent.getMainLayout().addComponents(buttonLayout);

		presenter = new NewFileWindowPresenter(this);
	}

	public void setDocumentTypeId(String documentTypeId) {
		newFileComponent.setDocumentTypeId(documentTypeId);
	}

	@Override
	public String getDocumentTypeId() {
		return newFileComponent.getDocumentTypeId();
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
	public void notifyNewFileCreated(Content content, String documentTypeId) {
		for (NewFileCreatedListener newFileCreatedListener : newFileCreatedListeners) {
			newFileCreatedListener.newFileCreated(content, documentTypeId);
		}
	}

	@Override
	public void open() {
		opened = true;
		newFileComponent.clearValues();

		// Bugfix for windows opened twice because of ClassBasedViewProvider
		for (Window window : new ArrayList<>(UI.getCurrent().getWindows())) {
			if (window instanceof NewFileWindowImpl) {
				window.close();
			}
		}
		UI.getCurrent().addWindow(this);
	}

	public final String getFileName() {
		return newFileComponent.getFileName();
	}

	public final String getExtension() {
		return newFileComponent.getExtension();
	}

	public Content getTemplate() {
		return newFileComponent.getTemplate();
	}

	public void showErrorMessage(String key, Object... args) {
		newFileComponent.showErrorMessage(key, args);
	}

	@Override
	public boolean isOpened() {
		return opened;
	}

	@Override
	public void close() {
		opened = false;
		super.close();
	}
}
