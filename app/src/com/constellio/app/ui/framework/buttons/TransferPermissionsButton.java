package com.constellio.app.ui.framework.buttons;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.collection.CollectionGroupRolesPresenter;
import com.constellio.app.ui.pages.collection.CollectionGroupRolesView;
import com.constellio.app.ui.pages.collection.CollectionGroupView;
import com.constellio.app.ui.pages.collection.CollectionUserPresenter;
import com.constellio.app.ui.pages.collection.ListCollectionUserPresenter;
import com.constellio.app.ui.pages.management.authorizations.ListPrincipalAccessAuthorizationsPresenter;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.data.Container;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.event.dd.DropTarget;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ColorPicker;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Set;

import static com.constellio.app.ui.framework.components.BaseForm.BUTTONS_LAYOUT;
import static com.constellio.app.ui.framework.components.BaseForm.SAVE_BUTTON;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.pages.collection.ListCollectionUserViewImpl.USER_TABLE;
import static com.vaadin.ui.themes.ValoTheme.BUTTON_PRIMARY;

//TODO: labels dans les configs
//TODO: presenter devrait être init ici au lieu de le passer en param
public class TransferPermissionsButton extends WindowButton {
	@PropertyId("users") protected ListAddRemoveRecordLookupField users;

	private Label label;
	private Table usersTable;
	private CheckBox removeUserAccessCheckbox;
	private Button saveButton;
	private Button cancelButton;
	//private CollectionGroupRolesPresenter presenter;

	public TransferPermissionsButton(String caption, String windowCaption) {
		super(caption, windowCaption);
		//this.presenter = presenter;
	}

	@Override
	protected Component buildWindowContent() {
		VerticalLayout mainLayout = new VerticalLayout();
		HorizontalLayout buttonsLayout = new HorizontalLayout();

		final Window window = getWindow();
		usersTable = new Table();
		//TODO: meilleur message et ajouter aux config
		label = new Label("\nSélectionner un ou des utilisateurs vers qui transférer les droits d'accès");
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

	private void confirmSaveDialog() {
		ConfirmDialog.show(ConstellioUI.getCurrent(), $("confirmTitle"), "confirmMessage",
				$("Ok"), $("cancel"), new ConfirmDialog.Listener() {
					@Override
					public void onClose(ConfirmDialog dialog) {

					}
				});
	}
}
