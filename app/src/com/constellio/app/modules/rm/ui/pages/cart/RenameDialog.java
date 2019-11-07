package com.constellio.app.modules.rm.ui.pages.cart;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class RenameDialog extends WindowButton {
	private String originalValue;

	public RenameDialog(Resource icon, String caption, String windowCaption, boolean iconOnly) {
		super(icon, caption, windowCaption, iconOnly, WindowConfiguration.modalDialog("768px", "130px"));
	}

	public String getOriginalValue() {
		return originalValue;
	}

	public void setOriginalValue(String originalValue) {
		this.originalValue = originalValue;
	}

	@Override
	protected Component buildWindowContent() {
		final TextField title = new BaseTextField();
		title.setValue(originalValue);
		title.setWidth("100%");

		final Button save = new BaseButton($("DisplayDocumentView.renameContentConfirm")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				save(title.getValue());
			}
		};
		save.addStyleName(ValoTheme.BUTTON_PRIMARY);
		save.addStyleName(BaseForm.SAVE_BUTTON);

		Button cancel = new BaseButton($("DisplayDocumentView.renameContentCancel")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				getWindow().close();
			}
		};

		HorizontalLayout form = new HorizontalLayout(title, save, cancel);
		form.setExpandRatio(title, 1);
		form.setSpacing(true);
		form.setWidth("95%");

		VerticalLayout layout = new VerticalLayout(form);
		layout.setSizeFull();

		return layout;
	}

	public abstract void save(String newValue);
}
