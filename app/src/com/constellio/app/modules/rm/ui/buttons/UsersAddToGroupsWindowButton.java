package com.constellio.app.modules.rm.ui.buttons;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.components.user.UserSelectionAddRemoveFieldImpl;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.AdditionnalRecordField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserAddUpdateRequest;
import com.constellio.model.services.users.UserServices;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class UsersAddToGroupsWindowButton extends WindowButton {

	private SchemasRecordsServices core;
	private MenuItemActionBehaviorParams params;
	private AppLayerFactory appLayerFactory;
	private RecordServices recordServices;
	private UserServices userServices;
	private String collection;
	private List<Group> records;
	private AdditionnalRecordField userFields;

	public void addToGroup() {
		click();
	}

	public UsersAddToGroupsWindowButton(List<Group> records, MenuItemActionBehaviorParams params) {
		super($("CollectionSecurityManagement.addToGroups"), $("CollectionSecurityManagement.selectUsers"));

		this.params = params;
		this.appLayerFactory = params.getView().getConstellioFactories().getAppLayerFactory();
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.userServices = appLayerFactory.getModelLayerFactory().newUserServices();
		this.collection = params.getView().getSessionContext().getCurrentCollection();
		this.core = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
		this.records = records;
	}

	@Override
	protected Component buildWindowContent() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setMargin(new MarginInfo(true, true, false, true));
		mainLayout.setSizeFull();

		HorizontalLayout userSelectLayout = new HorizontalLayout();
		userSelectLayout.setSpacing(true);
		userSelectLayout.setSizeFull();

		userFields = buildUsersDisplayOrderField(records, params);

		BaseButton saveButton;
		BaseButton cancelButton;
		userSelectLayout.addComponent(userFields);
		I18NHorizontalLayout buttonLayout = new I18NHorizontalLayout();
		mainLayout.addComponents(userSelectLayout);

		buttonLayout.addComponent(saveButton = new BaseButton($("save")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				try {
					//getUserServices().addUsersToGroup();
					addToGroupsRequested(params.getView());
					getWindow().close();
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
		mainLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);
		mainLayout.setHeight("100%");
		mainLayout.setWidth("100%");
		mainLayout.setSpacing(true);

		return mainLayout;
	}

	private AdditionnalRecordField buildUsersDisplayOrderField(List<Group> records,
															   MenuItemActionBehaviorParams params) {

		RecordVODataProvider groupDataProvider = getUserDataProvider(params.getView().getSessionContext());

		UserSelectionAddRemoveFieldImpl usersDisplayOrderField = new UserSelectionAddRemoveFieldImpl(groupDataProvider.getIterator());
		List<String> commonUsers = matchCommonGroupsIds(records);
		usersDisplayOrderField.setValue(commonUsers);
		return usersDisplayOrderField;
	}

	@NotNull
	private List<String> matchCommonGroupsIds(List<Group> records) {
		List<String> commonUsers = new ArrayList<>();
		List<String> isToRemove = new ArrayList<>();
		boolean firstIteration = true;

		for (Group group : records) {
			List<String> users = getUserServices()
					.getAllUsersInGroup(group, false, true)
					.stream().map(x -> x.getId()).collect(Collectors.toList());
			if (firstIteration) {
				commonUsers.addAll(users);
				firstIteration = false;
			} else {
				for (String userId : commonUsers) {
					if (users.stream().noneMatch(gr -> gr.equals(userId))) {
						isToRemove.add(userId);
					}
				}
			}
		}
		commonUsers.removeAll(isToRemove);
		return commonUsers;
	}

	private UserServices getUserServices() {
		return this.userServices;
	}

	private void addToGroupsRequested(BaseView baseView) {

		List<String> userFieldsValue = (List<String>) userFields.getValue();
		List<String> userCodeList = core.getUsers(userFieldsValue).stream().map(user -> user.getUsername()).collect(Collectors.toList());
		List<String> groupCodeList = records.stream().map(group -> group.getCode()).collect(Collectors.toList());

		for (String username : userCodeList) {
			UserAddUpdateRequest userAddUpdateRequest = userServices.addUpdate(username);
			userAddUpdateRequest.addToGroupsInCollection(groupCodeList, collection);
			userServices.execute(userAddUpdateRequest);
		}
		baseView.partialRefresh();
		baseView.showMessage($("CollectionSecurityManagement.addedUsersToGroups"));
	}

	private RecordVODataProvider getUserDataProvider(SessionContext sessionContext) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
		final MetadataSchemaVO groupSchemaVO = schemaVOBuilder.build(rm.group.schema(), VIEW_MODE.TABLE, sessionContext);
		return new RecordVODataProvider(groupSchemaVO, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), sessionContext) {
			@Override
			public LogicalSearchQuery getQuery() {
				return getUsersQuery();
			}
		};
	}

	private LogicalSearchQuery getUsersQuery() {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		MetadataSchemaType userSchemaType = rm.user.schemaType();

		LogicalSearchQuery query = new LogicalSearchQuery();

		LogicalSearchCondition condition = from(userSchemaType)
				.where(Schemas.COLLECTION).isEqualTo(collection).andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull();

		query.setCondition(condition);

		return query;
	}
}
