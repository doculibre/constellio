package com.constellio.app.modules.rm.ui.buttons;

import com.constellio.app.modules.rm.ui.field.CollectionSelectOptionField;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.UserSyncMode;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServices;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class ChangeEnumStatusRecordWindowButton<T extends Enum<T>> extends WindowButton {

	private ModelLayerFactory modelLayerFactory;
	private MenuItemActionBehaviorParams params;
	private OptionGroup enumOptionsField;
	private Class<T> enumType;
	private UserCredentialStatus selectedValue;
	private List<User> users;

	public ChangeEnumStatusRecordWindowButton(String caption, String windowCaption, AppLayerFactory appLayerFactory,
											  MenuItemActionBehaviorParams params, Class<T> enumType,
											  UserCredentialStatus selectedValue, List<User> users) {
		super(caption, windowCaption);

		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.params = params;
		this.enumType = enumType;
		this.enumOptionsField = new OptionGroup();
		this.selectedValue = selectedValue;
		this.users = users;
	}

	@Override
	protected Component buildWindowContent() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setMargin(new MarginInfo(true, true, false, true));
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		HorizontalLayout enumLayout = new HorizontalLayout();
		enumLayout.setSpacing(true);

		enumOptionsField.addStyleName("collections");
		enumOptionsField.addStyleName("collections-username");
		enumOptionsField.setId("enumStatus");
		enumOptionsField.setMultiSelect(false);
		enumLayout.addComponent(enumOptionsField);
		String firstValue = null;
		for (Object enumObject : enumType.getEnumConstants()) {
			if (enumObject instanceof String) {
				if (firstValue == null) {
					firstValue = (String) enumObject;
				}
				enumOptionsField.addItem((String) enumObject);
			} else if (enumObject instanceof UserCredentialStatus) {
				String statusValue = enumObject.toString();
				if (firstValue == null) {
					firstValue = statusValue;
				}
				enumOptionsField.addItem(statusValue);
			}
		}
		if (selectedValue != null) {
			enumOptionsField.select(selectedValue.toString());
		} else if (firstValue != null) {
			enumOptionsField.select(firstValue);
		}

		mainLayout.addComponents(enumLayout);
		List<Record> userRecords = users.stream().map(u -> u.getWrappedRecord()).collect(Collectors.toList());

		HorizontalLayout collectionsLayout = new HorizontalLayout();
		collectionsLayout.addComponent(new CollectionSelectOptionField(params.getView().getConstellioFactories().getAppLayerFactory(), userRecords));
		collectionsLayout.setSpacing(true);

		mainLayout.addComponent(collectionsLayout);
		BaseButton saveButton;
		BaseButton cancelButton;
		HorizontalLayout buttonLayout = new HorizontalLayout();

		buttonLayout.addComponent(saveButton = new BaseButton($("save")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				try {
					List<User> synchronizedUsers = ListSynchronizedUsers();
					if (!synchronizedUsers.isEmpty()) {
						new DesynchronizationWarningDialog(synchronizedUsers).show(getUI(), new ConfirmDialog.Listener() {
							@Override
							public void onClose(ConfirmDialog dialog) {
								if (dialog.isConfirmed()) {
									changeStatus();
								}
							}
						});
					} else {
						changeStatus();
					}
				} catch (Exception e) {
					e.printStackTrace();
					params.getView().showErrorMessage(MessageUtils.toMessage(e));
				}
			}

		});
		saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		buttonLayout.addComponent(cancelButton = new BaseButton($("cancel")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				getWindow().close();
			}
		});


		buttonLayout.setSpacing(true);
		mainLayout.addComponent(buttonLayout);
		mainLayout.setHeight("100%");
		mainLayout.setWidth("100%");
		mainLayout.setSpacing(true);


		return mainLayout;
	}

	private List<User> ListSynchronizedUsers() {
		UserServices userServices = params.getView().getConstellioFactories().getAppLayerFactory().getModelLayerFactory().newUserServices();
		return users.stream()
				.filter(u -> userServices.getUserCredential(u.getUsername()).getSyncMode().equals(UserSyncMode.SYNCED))
				.collect(Collectors.toList());
	}

	private void changeStatus() {
		changeStatus(enumOptionsField.getValue());
		params.getView().showMessage($("CollectionSecurityManagement.changedStatus"));
		getWindow().close();
	}

	public abstract void changeStatus(Object value);

}
