package com.constellio.app.modules.rm.ui.buttons;

import com.constellio.app.modules.rm.ui.field.CollectionSelectOptionField;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.framework.buttons.WindowButton;
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
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.app.ui.framework.components.BaseForm.BUTTONS_LAYOUT;
import static com.constellio.app.ui.framework.components.BaseForm.SAVE_BUTTON;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.vaadin.ui.themes.ValoTheme.BUTTON_PRIMARY;

public class AddGroupsToCollectionsWindowButton extends WindowButton {

	private List<Group> groups;
	private MenuItemActionBehaviorParams params;

	private AppLayerFactory appLayerFactory;
	private List<Record> groupRecords;
	private UserServices userServices;

	private CollectionSelectOptionField collectionsField;
	private Button saveButton;

	public AddGroupsToCollectionsWindowButton(List<Group> groups, MenuItemActionBehaviorParams params) {
		super($("CollectionSecurityManagement.addToCollections"), $("CollectionSecurityManagement.addToCollections"),
				WindowConfiguration.modalDialog("550px", "300px"));

		this.groups = groups;
		this.params = params;

		appLayerFactory = params.getView().getConstellioFactories().getAppLayerFactory();
		groupRecords = groups.stream().map(g -> g.getWrappedRecord()).collect(Collectors.toList());
		userServices = params.getView().getConstellioFactories().getAppLayerFactory().getModelLayerFactory().newUserServices();
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
				$("CollectionSecurityManagement.addGroupsToCollection", groups.size())) {
			@Override
			protected boolean isCommonCollection(String collection) {
				for (Group group : groups) {
					if (!userServices.getGroup(group.getCode()).getCollections().contains(collection) || userServices.getGroupInCollection(group.getCode(), collection).isLogicallyDeletedStatus()) {
						return false;
					}
				}
				return true;
			}

			@Override
			protected void processCommonCollection(String collection) {
				super.processCommonCollection(collection);
				this.setItemEnabled(collection, false);
			}
		};

		collectionsField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				updateSaveButtonAvailability();
			}
		});

		return collectionsField;
	}

	private Component buildButtonLayout() {
		HorizontalLayout buttonsLayout = new HorizontalLayout();

		saveButton = new Button($("save"));
		saveButton.addStyleName(SAVE_BUTTON);
		saveButton.addStyleName(BUTTON_PRIMARY);
		saveButton.addClickListener((ClickListener) event -> {
			executeAction();
		});

		updateSaveButtonAvailability();

		Button cancelButton = new Button($("cancel"));
		cancelButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				getWindow().close();
			}
		});

		buttonsLayout.addComponents(saveButton, cancelButton);
		buttonsLayout.addStyleName(BUTTONS_LAYOUT);
		buttonsLayout.setSpacing(true);
		return buttonsLayout;
	}

	private void updateSaveButtonAvailability() {
		boolean isCollectionSelected = !CollectionUtils.isEmpty(collectionsField.getSelectedValues());
		saveButton.setEnabled(isCollectionSelected);
	}

	private void executeAction() {
		addGroupsToCollections(collectionsField.getSelectedValues());

		params.getView().navigate().to().collectionSecurityShowGroupFirst();
		params.getView().showMessage($("CollectionSecurityManagement.addedGroupToCollections"));
		getWindow().close();
	}

	private void addGroupsToCollections(List<String> collections) {
		for (String collection : collections) {
			for (Group currentGroup : groups) {
				GroupAddUpdateRequest userAddUpdateRequest = userServices.request(currentGroup.getCode());
				userAddUpdateRequest.addCollections(collection);
				userServices.execute(userAddUpdateRequest);
			}
		}
	}
}
