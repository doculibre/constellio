package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class MergeRecordsWindow extends BaseWindow {

	public MergeRecordsWindow() {
		super($("MergeRecordsWindow.caption"));
		init();
	}

	private void init() {
		WindowConfiguration configuration = WindowConfiguration.modalDialog("50%", "20%");
		addStyleName(WINDOW_STYLE_NAME);
		setId(WINDOW_STYLE_NAME);
		setModal(configuration.isModal());
		setResizable(configuration.isResizable());
		if (configuration.getWidth() != null) {
			setWidth(configuration.getWidth());
		}
		if (configuration.getHeight() != null) {
			setHeight(configuration.getHeight());
		}

		Label label = new Label($("MergeRecordsWindow.versionConflictsMessage"));
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.addComponent(label);
		verticalLayout.setSpacing(true);
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		BaseButton mergeButton = new BaseButton($("MergeRecordsWindow.mergeModifications")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				close();
				mergeButtonClick();
			}
		};
		BaseButton cancelButton = new BaseButton($("MergeRecordsWindow.cancelModifications")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				close();
				cancelButtonClick();
			}
		};
		mergeButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		cancelButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		horizontalLayout.addComponents(mergeButton, cancelButton);
		horizontalLayout.setSpacing(true);
		verticalLayout.addComponent(horizontalLayout);
		setContent(verticalLayout);

		UI.getCurrent().addWindow(this);
		focus();
	}


	public abstract void mergeButtonClick();

	public abstract void cancelButtonClick();

}
