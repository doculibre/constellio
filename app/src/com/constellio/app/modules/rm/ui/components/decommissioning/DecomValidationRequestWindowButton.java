package com.constellio.app.modules.rm.ui.components.decommissioning;

import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningListPresenter;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveStringLookupField;
import com.constellio.app.ui.i18n.i18n;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public class DecomValidationRequestWindowButton extends WindowButton {
	private final DecommissioningListPresenter presenter;

	private ListAddRemoveStringLookupField users;
	private TextArea comments;
	private CheckBox checkBox;

	public DecomValidationRequestWindowButton(DecommissioningListPresenter presenter) {
		super(i18n.$("DecomAskForValidationWindowButton.buttonCaption"),
				i18n.$("DecomAskForValidationWindowButton.windowCaption"), new WindowConfiguration(true, true, "50%", "550px"));
		this.presenter = presenter;
	}

	@Override
	protected Component buildWindowContent() {
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);

		this.users = new ListAddRemoveStringLookupField(presenter.getUsersWithReadPermissionOnAdministrativeUnit());
		this.users.setItemConverter(new RecordIdToCaptionConverter());
		users.setCaption($("DecomAskForValidationWindowButton.usersCaption"));
		users.setRequired(true);

		//users.setRequiredError($("DecomAskForValidationWindowButton.error.users"));
		layout.addComponent(users);
		checkBox = new CheckBox($("DecomAskForValidationWindowButton.addCommentToDecomlistCaption"));
		layout.addComponent(checkBox);
		comments = new TextArea($("DecomAskForValidationWindowButton.commentsCaption"));
		comments.setSizeFull();
		layout.addComponent(comments);

		BaseButton sendButton = new BaseButton($("DecomAskForValidationWindowButton.okButton")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (users.getValue() != null && users.getValue().size() > 0) {
					if (comments.getValue().isEmpty() && checkBox.getValue()) {
						presenter.showErrorMessage($("DecomAskForValidationWindowButton.emptyComment"));
					} else if (presenter.validationRequested(users.getValue(), comments.getValue(), checkBox.getValue())) {
						getWindow().close();
					}
				} else {
					presenter.showErrorMessage($("DecomAskForValidationWindowButton.validationWithoutUserError"));
				}
			}
		};
		sendButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.addComponent(sendButton);
		BaseButton cancelButton = new BaseButton($("DecomAskForValidationWindowButton.cancelButton")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				getWindow().close();
			}
		};
		buttonsLayout.addComponent(cancelButton);
		buttonsLayout.setSpacing(true);
		layout.addComponent(buttonsLayout);
		layout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_RIGHT);

		return layout;
	}
}
