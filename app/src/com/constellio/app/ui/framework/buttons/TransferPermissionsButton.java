package com.constellio.app.ui.framework.buttons;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.pages.management.authorizations.TransferPermissionPresenter;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.List;

import static com.constellio.app.ui.framework.components.BaseForm.BUTTONS_LAYOUT;
import static com.constellio.app.ui.framework.components.BaseForm.SAVE_BUTTON;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.vaadin.ui.themes.ValoTheme.BUTTON_PRIMARY;

public class TransferPermissionsButton extends WindowButton {
	@PropertyId("users") protected ListAddRemoveRecordLookupField users;

	private Label label;
	private CheckBox removeUserAccessCheckbox;
	private Button saveButton;
	private Button cancelButton;
	private TransferPermissionPresenter presenter;
	//private CollectionGroupRolesPresenter presenter;

	private RecordVO sourceUser;

	public TransferPermissionsButton(String caption, String windowCaption, TransferPermissionPresenter presenter) {
		super(caption, windowCaption);
		this.presenter = presenter;
	}


	@Override
	protected Component buildWindowContent() {
		VerticalLayout mainLayout = new VerticalLayout();
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		final Window window = getWindow();
		//TODO: meilleur message et ajouter aux config
		label = new Label($("TransferAccessRights.ChooseDestinationUsers"));
		buildUsersSearchField();
		buildRemoveCurrentUserRightsCheckbox();

		buildSaveButton();
		buildCancelButton();
		configureButtonsLayout(buttonsLayout);

		mainLayout.addComponents(label, users, removeUserAccessCheckbox);
		mainLayout.addComponent(buttonsLayout);
		return mainLayout;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}

	private void buildUsersSearchField() {
		users = new ListAddRemoveRecordLookupField(User.SCHEMA_TYPE);
		users.setCaption($("AuthorizationsView.users"));
		users.setId("users");
	}

	private void buildRemoveCurrentUserRightsCheckbox() {
		removeUserAccessCheckbox = new CheckBox();

		//TODO: ajouter message aux config
		removeUserAccessCheckbox.setCaption("Retirer les accès à l'utilisateur");
		removeUserAccessCheckbox.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
			}
		});
	}

	private void buildSaveButton() {
		saveButton = new Button($("save"));
		saveButton.addStyleName(SAVE_BUTTON);
		saveButton.addStyleName(BUTTON_PRIMARY);
		saveButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				confirmSaveDialog();
			}
		});
	}

	private void buildCancelButton() {
		cancelButton = new Button($("cancel"));
		cancelButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				getWindow().close();
			}
		});
	}

	private void configureButtonsLayout(HorizontalLayout buttonsLayout) {
		buttonsLayout.addStyleName(BUTTONS_LAYOUT);
		buttonsLayout.setSpacing(true);
		buttonsLayout.addComponents(saveButton, cancelButton);
	}

	private String buildUserListString() {
		String selectedUsersString = "";//users.getValue();
		List<String> usersList = users.getValue();
		for (int i = 0; i < usersList.size(); i++) {
			selectedUsersString += usersList.get(i);
			if (i < usersList.size() - 1) {
				selectedUsersString += ", ";
			}
		}
		return selectedUsersString;
	}

	private void confirmSaveDialog() {

		sourceUser = presenter.getUser();
		//List<Record> userVOList = presenter.getUserVOListFromIDs(users.getValue());
		String selectedUsersString = buildUserListString();
		String confirmMessage = presenter.buildTransferRightsConfirmMessage(sourceUser.getTitle(), selectedUsersString,
				users.getValue().size() > 1, removeUserAccessCheckbox.getValue());
		ConfirmDialog.show(ConstellioUI.getCurrent(), $("TransferAccessRights.Title"), confirmMessage,
				$("Ok"), $("cancel"), new ConfirmDialog.Listener() {
					@Override
					public void onClose(ConfirmDialog dialog) {
						if (dialog.isConfirmed()) {
							presenter.copyUserPermissions(sourceUser, users.getValue());
						} else {

						}
					}
				});
	}
}