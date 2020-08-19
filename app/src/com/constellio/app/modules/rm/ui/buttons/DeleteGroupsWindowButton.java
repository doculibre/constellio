package com.constellio.app.modules.rm.ui.buttons;

import com.constellio.app.modules.rm.ui.field.GroupCollectionSelectOptionField;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.security.global.GroupAddUpdateRequest;
import com.constellio.model.services.users.UserServices;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;

public class DeleteGroupsWindowButton extends WindowButton {
	private AppLayerFactory appLayerFactory;
	private List<Record> groupRecords;
	private List<Group> groups;
	private MenuItemActionBehaviorParams params;
	private UserServices userServices;
	private GroupCollectionSelectOptionField collectionsField;

	public DeleteGroupsWindowButton(List<Group> groups, MenuItemActionBehaviorParams params) {
		super($("CollectionSecurityManagement.deleteGroups"), $("CollectionSecurityManagement.deleteGroups"),
				WindowConfiguration.modalDialog("50%", "300px"));
		this.appLayerFactory = params.getView().getConstellioFactories().getAppLayerFactory();
		this.groups = groups;
		this.groupRecords = groups.stream().map(g -> g.getWrappedRecord()).collect(Collectors.toList());
		this.params = params;
		this.userServices = params.getView().getConstellioFactories().getAppLayerFactory().getModelLayerFactory().newUserServices();
	}


	@Override
	protected Component buildWindowContent() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setHeight("90%");
		mainLayout.setSpacing(true);

		this.collectionsField = new GroupCollectionSelectOptionField(appLayerFactory, groupRecords, $("CollectionSecurityManagement.deleteGroupsFromCollection", groups.size()));
		mainLayout.addComponent(collectionsField);
		Button deleteUserButton = new DeleteButton(null, $("CollectionSecurityManagement.delete"), false) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				deleteGroupsAction();
				getWindow().close();
			}

			@Override
			protected String getConfirmDialogMessage() {
				return $("ConfirmDialog.confirmDeleteWithAllRecords", $("CollectionSecurityManagement.groupLowerCase"));
			}

			private void deleteGroupsAction() {
				deleteGroupFromCollection(groups);
				params.getView().navigate().to().collectionSecurityShowGroupFirst();
				params.getView().showMessage($("CollectionSecurityManagement.groupRemovedFromCollection"));
			}
		};
		deleteUserButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		buttonsLayout.addComponent(deleteUserButton);
		Button cancelButton = new BaseButton($("cancel")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				getWindow().close();
			}
		};
		buttonsLayout.addComponent(cancelButton);
		mainLayout.addComponent(buttonsLayout);
		mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
		return mainLayout;
	}

	public void deleteGroupFromCollection(List<Group> userRecords) {
		List<String> collections = collectionsField.getSelectedValues();
		for (String collection : collections) {
			for (Group currentGroup : userRecords) {
				GroupAddUpdateRequest userAddUpdateRequest = userServices.request(currentGroup.getCode());
				userAddUpdateRequest.removeCollection(collection);
				userServices.execute(userAddUpdateRequest);
			}
		}
	}
}
