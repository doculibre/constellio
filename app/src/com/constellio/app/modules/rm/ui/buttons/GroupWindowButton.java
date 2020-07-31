package com.constellio.app.modules.rm.ui.buttons;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.users.UserServices;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;

public class GroupWindowButton extends WindowButton {

	private RMSchemasRecordsServices rm;
	private MenuItemActionBehaviorParams params;
	private AppLayerFactory appLayerFactory;
	private RecordServices recordServices;
	private UserServices userServices;
	private String collection;
	private List<User> records;
	final OptionGroup groupsField;

	public void addToGroup() {
		click();
	}

	public GroupWindowButton(User record, MenuItemActionBehaviorParams params) {
		this(Collections.singletonList(record), params);
	}

	public GroupWindowButton(List<User> records, MenuItemActionBehaviorParams params) {
		super($("CollectionSecurityManagement.AddToGroups"), $("CollectionSecurityManagement.SelectGroups"));

		this.params = params;
		this.appLayerFactory = params.getView().getConstellioFactories().getAppLayerFactory();
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.userServices = appLayerFactory.getModelLayerFactory().newUserServices();
		this.collection = params.getView().getSessionContext().getCurrentCollection();
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.records = records;
		this.groupsField = new OptionGroup($("CollectionSecurityManagement.SelectGroups"));
	}

	@Override
	protected Component buildWindowContent() {
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(new MarginInfo(true, true, false, true));
		layout.setSizeFull();

		HorizontalLayout collectionLayout = new HorizontalLayout();
		collectionLayout.setSpacing(true);

		groupsField.addStyleName("collections");
		groupsField.addStyleName("collections-username");
		groupsField.setId("groups");
		groupsField.setMultiSelect(true);
		List<Group> groups = userServices.getCollectionGroups(params.getView().getCollection());
		for (Group group : groups) {
			groupsField.addItem(group.getCode());
			boolean existsForAll = records.stream()
					.allMatch(record -> record.getUserGroups().stream()
							.anyMatch(gr -> gr.equals(group.getId())));
			if (existsForAll) {
				groupsField.select(group.getCode());
			}
		}

		BaseButton saveButton;

		collectionLayout.addComponent(saveButton = new BaseButton($("save")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				try {
					for (Group gr : (Collection<Group>) groupsField.getValue()) {

					}
					addToGroupsRequested(params.getView());
					getWindow().close();
				} catch (Exception e) {
					e.printStackTrace();
					params.getView().showErrorMessage(MessageUtils.toMessage(e));
				}
			}
		});
		saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		layout.addComponents(collectionLayout);
		return layout;
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

}
