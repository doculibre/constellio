package com.constellio.app.modules.rm.ui.buttons;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.components.group.GroupSelectionAddRemoveFieldImpl;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.AdditionnalRecordField;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserServices;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class GroupWindowButton extends WindowButton {

	private RMSchemasRecordsServices rm;
	private MenuItemActionBehaviorParams params;
	private AppLayerFactory appLayerFactory;
	private RecordServices recordServices;
	private UserServices userServices;
	private String collection;
	private List<User> records;
	private AdditionnalRecordField groupsField;

	public void addToGroup() {
		click();
	}

	public GroupWindowButton(User record, MenuItemActionBehaviorParams params) {
		this(Collections.singletonList(record), params);
	}

	public GroupWindowButton(List<User> records, MenuItemActionBehaviorParams params) {
		super($("CollectionSecurityManagement.addToGroups"), $("CollectionSecurityManagement.selectGroups"));

		this.params = params;
		this.appLayerFactory = params.getView().getConstellioFactories().getAppLayerFactory();
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.userServices = appLayerFactory.getModelLayerFactory().newUserServices();
		this.collection = params.getView().getSessionContext().getCurrentCollection();
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
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

		groupsField = buildFavoritesDisplayOrderField(records, params);

		BaseButton saveButton;
		BaseButton cancelButton;
		userSelectLayout.addComponent(groupsField);
		HorizontalLayout buttonLayout = new HorizontalLayout();
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
		mainLayout.setHeight("100%");
		mainLayout.setWidth("100%");
		mainLayout.setSpacing(true);

		return mainLayout;
	}

	private AdditionnalRecordField buildFavoritesDisplayOrderField(List<User> records,
																   MenuItemActionBehaviorParams params) {

		RecordVODataProvider groupDataProvider = getGroupDataProvider(params.getView().getSessionContext());

		GroupSelectionAddRemoveFieldImpl groupsDisplayOrderField = new GroupSelectionAddRemoveFieldImpl(groupDataProvider.getIterator());
		List<String> commonGroups = matchCommonGroupsIds(records);
		groupsDisplayOrderField.setValue(commonGroups);
		return groupsDisplayOrderField;
	}

	@NotNull
	private List<String> matchCommonGroupsIds(List<User> records) {
		List<String> commonGroups = new ArrayList<>();
		List<String> isToRemove = new ArrayList<>();
		boolean firstIteration = true;
		for (User user : records) {
			if (firstIteration) {
				commonGroups.addAll(user.getUserGroups());
				firstIteration = false;
			} else {
				for (String groupId : commonGroups) {
					if (user.getUserGroups().stream().noneMatch(gr -> gr.equals(groupId))) {
						isToRemove.add(groupId);
					}
				}
			}
		}
		commonGroups.removeAll(isToRemove);
		return commonGroups;
	}

	private UserServices getUserServices() {
		return this.userServices;
	}

	private void addToGroupsRequested(BaseView baseView) {
		Transaction transaction = new Transaction(RecordUpdateOptions.validationExceptionSafeOptions());
		List<Record> updateRecords = records.stream().map(group -> group.getWrappedRecord()).collect(Collectors.toList());
		transaction.update(updateRecords);
		try {
			recordServices.execute(transaction);
			baseView.showMessage($("CollectionSecurityManagement.addedUsersToGroups"));
		} catch (Exception e) {
			throw new ImpossibleRuntimeException(e);
		}
	}

	private RecordVODataProvider getGroupDataProvider(SessionContext sessionContext) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
		final MetadataSchemaVO groupSchemaVO = schemaVOBuilder.build(rm.group.schema(), VIEW_MODE.TABLE, sessionContext);
		return new RecordVODataProvider(groupSchemaVO, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), sessionContext) {
			@Override
			public LogicalSearchQuery getQuery() {
				return getGroupsQuery();
			}
		};
	}

	private LogicalSearchQuery getGroupsQuery() {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		MetadataSchemaType groupSchemaType = rm.group.schemaType();

		LogicalSearchQuery query = new LogicalSearchQuery();

		LogicalSearchCondition condition = from(groupSchemaType)
				.where(Schemas.COLLECTION).isEqualTo(collection)
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull();

		query.setCondition(condition);

		return query;
	}
}
