package com.constellio.app.modules.rm.ui.buttons;

import com.constellio.app.modules.rm.ui.components.user.UserSelectionAddRemoveFieldImpl;
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
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
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
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.constellio.app.ui.framework.components.BaseForm.BUTTONS_LAYOUT;
import static com.constellio.app.ui.framework.components.BaseForm.SAVE_BUTTON;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.records.wrappers.Collection.SYSTEM_COLLECTION;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.vaadin.ui.themes.ValoTheme.BUTTON_PRIMARY;

public class RemoveUsersFromGroupsWindowButton extends WindowButton {

	private List<Group> groups;
	private MenuItemActionBehaviorParams params;

	private AppLayerFactory appLayerFactory;
	private List<Record> groupRecords;
	private UserServices userServices;
	private MetadataSchemasManager schemasManager;
	private SchemasRecordsServices systemRecordsServices;

	private UserSelectionAddRemoveFieldImpl usersField;
	private CollectionSelectOptionField collectionsField;

	public RemoveUsersFromGroupsWindowButton(List<Group> groups, MenuItemActionBehaviorParams params) {
		super($("CollectionSecurityManagement.removeUserToGroups"), $("CollectionSecurityManagement.removeUserToGroups"),
				WindowConfiguration.modalDialog("550px", "500px"));

		this.groups = groups;
		this.params = params;

		appLayerFactory = params.getView().getConstellioFactories().getAppLayerFactory();
		groupRecords = groups.stream().map(g -> g.getWrappedRecord()).collect(Collectors.toList());
		userServices = params.getView().getConstellioFactories().getAppLayerFactory().getModelLayerFactory().newUserServices();
		schemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		systemRecordsServices = new SchemasRecordsServices(SYSTEM_COLLECTION, appLayerFactory.getModelLayerFactory());
	}


	@Override
	protected Component buildWindowContent() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);

		buildUserSelectionField();
		buildCollectionSelectionField();
		mainLayout.addComponents(usersField, collectionsField);

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

	private UserSelectionAddRemoveFieldImpl buildUserSelectionField() {
		RecordVODataProvider groupDataProvider = getUserDataProvider(params.getView().getSessionContext());
		usersField = new UserSelectionAddRemoveFieldImpl(appLayerFactory, groupDataProvider.getIterator());
		return usersField;
	}

	private RecordVODataProvider getUserDataProvider(SessionContext sessionContext) {
		MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
		MetadataSchema userCredentialSchema = schemasManager.getSchemaTypes(SYSTEM_COLLECTION)
				.getSchemaType(UserCredential.SCHEMA_TYPE).getDefaultSchema();
		final MetadataSchemaVO groupSchemaVO = schemaVOBuilder.build(userCredentialSchema, VIEW_MODE.TABLE, sessionContext);
		return new RecordVODataProvider(groupSchemaVO, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), sessionContext) {
			@Override
			public LogicalSearchQuery getQuery() {
				return getUsersQuery();
			}
		};
	}

	private LogicalSearchQuery getUsersQuery() {
		MetadataSchemaType userCredentialSchemaType = schemasManager.getSchemaTypes(SYSTEM_COLLECTION)
				.getSchemaType(UserCredential.SCHEMA_TYPE);

		LogicalSearchQuery query = new LogicalSearchQuery();
		LogicalSearchCondition condition = from(userCredentialSchemaType)
				.where(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull();
		query.setCondition(condition);
		return query;
	}

	private Component buildCollectionSelectionField() {
		collectionsField = new CollectionSelectOptionField(appLayerFactory, groupRecords,
				$("CollectionSecurityManagement.removeUserFromGroupsInCollection", groups.size())) {
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
		removeUsersFromGroups(usersField.getValue(), collectionsField.getSelectedValues(), stopSync);

		params.getView().navigate().to().collectionSecurityShowGroupFirst();
		params.getView().showMessage($("CollectionSecurityManagement.removedUsersToGroups"));
		getWindow().close();
	}

	private void removeUsersFromGroups(List<String> users, List<String> collections, boolean stopSync) {
		List<UserCredential> credentials = users.stream().map(id -> systemRecordsServices.getUserCredential(id))
				.collect(Collectors.toList());
		List<String> userCodeList = credentials.stream().map(user -> user.getUsername()).collect(Collectors.toList());
		List<String> groupCodeList = groups.stream().map(group -> group.getCode()).collect(Collectors.toList());

		for (String collection : collections) {
			for (String username : userCodeList) {
				UserAddUpdateRequest userAddUpdateRequest = userServices.addUpdate(username);
				for (String currentGroupCode : groupCodeList) {
					userAddUpdateRequest.removeFromGroupOfCollection(currentGroupCode, collection);
				}
				if (stopSync) {
					userAddUpdateRequest.stopSyncingLDAP();
				}
				userServices.execute(userAddUpdateRequest);
			}
		}
	}
}
