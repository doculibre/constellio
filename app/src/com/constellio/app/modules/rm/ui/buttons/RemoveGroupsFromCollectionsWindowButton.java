package com.constellio.app.modules.rm.ui.buttons;

import com.constellio.app.modules.rm.ui.field.CollectionSelectOptionField;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.security.global.GroupAddUpdateRequest;
import com.constellio.model.services.users.UserServices;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.collections.CollectionUtils;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.constellio.app.ui.framework.components.BaseForm.BUTTONS_LAYOUT;
import static com.constellio.app.ui.i18n.i18n.$;

public class RemoveGroupsFromCollectionsWindowButton extends WindowButton {

	private List<Group> groups;
	private MenuItemActionBehaviorParams params;

	private AppLayerFactory appLayerFactory;
	private List<Record> groupRecords;
	private UserServices userServices;
	private final LDAPConfigurationManager ldapConfigurationManager;

	private CollectionSelectOptionField collectionsField;
	private Button deleteButton;

	public RemoveGroupsFromCollectionsWindowButton(List<Group> groups, MenuItemActionBehaviorParams params) {
		super($("CollectionSecurityManagement.removeToCollections"), $("CollectionSecurityManagement.removeToCollections"),
				WindowConfiguration.modalDialog("550px", "300px"));

		this.groups = groups;
		this.params = params;

		appLayerFactory = params.getView().getConstellioFactories().getAppLayerFactory();
		groupRecords = groups.stream().map(g -> g.getWrappedRecord()).collect(Collectors.toList());
		userServices = params.getView().getConstellioFactories().getAppLayerFactory().getModelLayerFactory().newUserServices();
		ldapConfigurationManager = appLayerFactory.getModelLayerFactory().getLdapConfigurationManager();
	}


	@Override
	protected Component buildWindowContent() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);

		buildCollectionSelectionField();
		mainLayout.addComponents(collectionsField);

		VerticalLayout spacer = new VerticalLayout();
		mainLayout.addComponent(spacer);
		mainLayout.setExpandRatio(spacer, 1);

		Component buttonLayout = buildButtonLayout();
		mainLayout.addComponent(buttonLayout);
		mainLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_CENTER);

		mainLayout.setSizeFull();
		mainLayout.setMargin(true);

		return mainLayout;
	}

	private Component buildCollectionSelectionField() {
		collectionsField = new CollectionSelectOptionField(appLayerFactory, groupRecords,
				$("CollectionSecurityManagement.deleteGroupsFromCollection", groups.size())) {
			@Override
			protected List<String> getAvailableCollections() {
				Set<String> collections = new HashSet<>();
				for (Group group : groups) {
					collections.addAll(userServices.getGroup(group.getCode()).getCollections());
				}
				return new ArrayList<>(collections);
			}

			@Override
			protected void processCommonCollection(String collection) {

			}
		};

		collectionsField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				updateDeleteButtonAvailability();
			}
		});

		return collectionsField;
	}

	private Component buildButtonLayout() {
		HorizontalLayout buttonsLayout = new HorizontalLayout();

		deleteButton = new DeleteButton(null, $("CollectionSecurityManagement.delete"), false) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				if (canDoAction()) {
					executeAction();
				} else {
					showErrorMessage();
				}
			}

			@Override
			protected String getConfirmDialogMessage() {
				return $("ConfirmDialog.confirmDeleteWithAllRecords", $("CollectionSecurityManagement.groupLowerCase"));
			}
		};
		deleteButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		updateDeleteButtonAvailability();

		Button cancelButton = new Button($("cancel"));
		cancelButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				getWindow().close();
			}
		});

		buttonsLayout.addComponents(deleteButton, cancelButton);
		buttonsLayout.addStyleName(BUTTONS_LAYOUT);
		buttonsLayout.setSpacing(true);
		return buttonsLayout;
	}

	private void updateDeleteButtonAvailability() {
		boolean isCollectionSelected = !CollectionUtils.isEmpty(collectionsField.getSelectedValues());
		deleteButton.setEnabled(isCollectionSelected);
	}

	private boolean canDoAction() {
		List<String> syncCollections = ldapConfigurationManager
				.getLDAPUserSyncConfiguration(false).getSelectedCollectionsCodes();

		boolean hasSyncCollection = false;
		for (String collection : collectionsField.getSelectedValues()) {
			if (syncCollections.contains(collection)) {
				hasSyncCollection = true;
				break;
			}
		}

		boolean hasSyncGroup = false;
		for (Group group : groups) {
			if (!group.isLocallyCreated()) {
				hasSyncGroup = true;
				break;
			}
		}

		return !(hasSyncCollection && hasSyncGroup);
	}

	private void showErrorMessage() {
		params.getView().navigate().to().collectionSecurityShowGroupFirst();
		params.getView().showErrorMessage($("CollectionSecurityManagement.cannotRemoveSyncGroup"));
		getWindow().close();
	}

	// TODO::JOLA --> Get a response to detect if group is used or not, if used, show warning message.
	private void executeAction() {
		deleteGroupsFromCollections(collectionsField.getSelectedValues());

		params.getView().navigate().to().collectionSecurityShowGroupFirst();
		params.getView().showMessage($("CollectionSecurityManagement.groupRemovedFromCollection"));
		getWindow().close();
	}

	private void deleteGroupsFromCollections(List<String> collections) {
		for (String collection : collections) {
			for (Group currentGroup : groups) {
				GroupAddUpdateRequest userAddUpdateRequest = userServices.request(currentGroup.getCode());
				userAddUpdateRequest.removeCollection(collection);
				userServices.execute(userAddUpdateRequest);
			}
		}
	}
}
