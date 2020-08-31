package com.constellio.app.modules.rm.ui.buttons;

import com.constellio.app.modules.rm.ui.components.group.GroupSelectionAddRemoveFieldImpl;
import com.constellio.app.modules.rm.ui.field.CollectionSelectOptionField;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserAddUpdateRequest;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_CannotChangeAssignmentOfSyncedUserToSyncedGroup;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.collections.CollectionUtils;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.app.ui.framework.components.BaseForm.BUTTONS_LAYOUT;
import static com.constellio.app.ui.framework.components.BaseForm.SAVE_BUTTON;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.vaadin.ui.themes.ValoTheme.BUTTON_PRIMARY;

public class AddUsersToGroupsWindowButton extends WindowButton {

	private List<User> users;
	private MenuItemActionBehaviorParams params;

	private AppLayerFactory appLayerFactory;
	private List<Record> userRecords;
	private UserServices userServices;
	private String collection;
	private SchemasRecordsServices schemaRecordsServices;

	private GroupSelectionAddRemoveFieldImpl groupsField;
	private CollectionSelectOptionField collectionsField;

	public AddUsersToGroupsWindowButton(List<User> users, MenuItemActionBehaviorParams params) {
		super($("CollectionSecurityManagement.addToGroup"), $("CollectionSecurityManagement.addToGroup"),
				WindowConfiguration.modalDialog("550px", "500px"));

		this.users = users;
		this.params = params;

		appLayerFactory = params.getView().getConstellioFactories().getAppLayerFactory();
		userRecords = users.stream().map(u -> u.getWrappedRecord()).collect(Collectors.toList());
		userServices = params.getView().getConstellioFactories().getAppLayerFactory().getModelLayerFactory().newUserServices();
		collection = params.getView().getSessionContext().getCurrentCollection();
		schemaRecordsServices = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
	}


	@Override
	protected Component buildWindowContent() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);

		buildGroupSelectionField();
		buildCollectionSelectionField();
		mainLayout.addComponents(groupsField, collectionsField);

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

	private GroupSelectionAddRemoveFieldImpl buildGroupSelectionField() {
		RecordVODataProvider groupDataProvider = getGroupDataProvider(params.getView().getSessionContext());
		groupsField = new GroupSelectionAddRemoveFieldImpl(groupDataProvider.getIterator());
		return groupsField;
	}

	private RecordVODataProvider getGroupDataProvider(SessionContext sessionContext) {
		MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
		final MetadataSchemaVO groupSchemaVO = schemaVOBuilder.build(schemaRecordsServices.group.schema(), VIEW_MODE.TABLE, sessionContext);
		return new RecordVODataProvider(groupSchemaVO, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), sessionContext) {
			@Override
			public LogicalSearchQuery getQuery() {
				return getGroupsQuery();
			}
		};
	}

	private LogicalSearchQuery getGroupsQuery() {

		MetadataSchemaType groupSchemaType = schemaRecordsServices.group.schemaType();

		LogicalSearchQuery query = new LogicalSearchQuery();

		LogicalSearchCondition condition = from(groupSchemaType)
				.where(Schemas.COLLECTION).isEqualTo(collection)
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull();

		query.setCondition(condition);

		return query;
	}

	private Component buildCollectionSelectionField() {
		collectionsField = new CollectionSelectOptionField(appLayerFactory, userRecords,
				$("CollectionSecurityManagement.addUserToGroupsInCollection", users.size())) {
			@Override
			protected List<String> getAvailableCollections() {
				Collection<String> collections = new HashSet<>(userServices.getUserInfos(users.get(0).getUsername()).getCollections());
				for (User user : users) {
					collections = CollectionUtils.intersection(collections, userServices.getUserInfos(user.getUsername()).getCollections());
				}
				return new ArrayList<>(collections);
			}

			@Override
			protected void processCommonCollection(String collection) {

			}
		};
		return collectionsField;
	}

	private Component buildButtonLayout() {
		HorizontalLayout buttonsLayout = new HorizontalLayout();

		Button saveButton = new Button($("save"));
		saveButton.addStyleName(SAVE_BUTTON);
		saveButton.addStyleName(BUTTON_PRIMARY);
		saveButton.addClickListener((ClickListener) event -> {
			try {
				executeAction(false);
			} catch (UserServicesRuntimeException_CannotChangeAssignmentOfSyncedUserToSyncedGroup e) {
				showStopSyncDialog();
			}
		});

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

	private void showStopSyncDialog() {
		ConfirmDialog.show(ConstellioUI.getCurrent(), $("CollectionSecurityManagement.desynchronizationWarningTitle"),
				$("CollectionSecurityManagement.groupUserDesynchronizationWarningMessage"), $("Ok"), $("cancel"), (ConfirmDialog.Listener) dialog -> {
					if (dialog.isConfirmed()) {
						executeAction(true);
					}
				});
	}

	private void executeAction(boolean stopSync) {
		addUsersToGroups(groupsField.getValue(), collectionsField.getSelectedValues(), stopSync);

		params.getView().navigate().to().collectionSecurity();
		params.getView().showMessage($("CollectionSecurityManagement.addedUsersToGroups"));
		getWindow().close();
	}

	private void addUsersToGroups(List<String> groups, List<String> collections, boolean stopSync) {
		if (CollectionUtils.isEmpty(groups)) {
			return;
		}

		List<String> userCodeList = users.stream().map(user -> user.getUsername()).collect(Collectors.toList());
		List<String> groupCodeList = schemaRecordsServices.getGroups(groups).stream().map(group -> group.getCode()).collect(Collectors.toList());

		for (String collection : collections) {
			for (String username : userCodeList) {
				UserAddUpdateRequest userAddUpdateRequest = userServices.addUpdate(username);
				userAddUpdateRequest.addToGroupsInCollection(groupCodeList, collection);
				if (stopSync) {
					userAddUpdateRequest.stopSyncingLDAP();
				}
				userServices.execute(userAddUpdateRequest);
			}
		}
	}
}
