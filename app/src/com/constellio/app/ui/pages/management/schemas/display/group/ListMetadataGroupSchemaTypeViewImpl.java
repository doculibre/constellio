package com.constellio.app.ui.pages.management.schemas.display.group;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Map;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class ListMetadataGroupSchemaTypeViewImpl extends BaseViewImpl implements ListMetadataGroupSchemaTypeView, ClickListener {

	ListMetadataGroupSchemaTypePresenter presenter;
	private VerticalLayout viewLayout;
	private Table groups;
	public static final String GROUP_TABLE = "groups";
	public static final String GROUP_NAME = "label";
	public static final String GROUP_BUTTON = "button";
	public static final String GROUP_DELETE_BUTTON = "delete_button";

	public ListMetadataGroupSchemaTypeViewImpl() {
		this.presenter = new ListMetadataGroupSchemaTypePresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListMetadataGroupSchemaTypeView.viewTitle");
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return this;
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		Map<String, String> params = ParamUtils.getParamsMap(event.getParameters());
		presenter.setSchemaTypeCode(params.get("schemaTypeCode"));
		viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		groups = buildTable();

		HorizontalLayout nameLayout = new HorizontalLayout();
		final TextField groupName = new TextField();
		groupName.setRequired(true);
		groupName.setNullRepresentation("");
		groupName.addStyleName(GROUP_NAME);

		Button addButton = new AddButton() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addGroupMetadata(groupName.getValue());
				groupName.setValue("");
			}
		};
		addButton.addStyleName(GROUP_BUTTON);

		nameLayout.addComponent(groupName);
		nameLayout.addComponent(addButton);
		nameLayout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);

		viewLayout.addComponents(nameLayout, groups);
		return viewLayout;
	}

	private Table buildTable() {
		Table table = new Table();
		table.setWidth("100%");
		table.setColumnHeader("button", "");
		table.setColumnWidth("button", 60);
		table.setColumnHeader("caption", $("ListMetadataGroupSchemaTypeView.caption"));
		table.addContainerProperty("caption", String.class, "");
		table.addContainerProperty("button", Button.class, null);
		table.addStyleName(GROUP_TABLE);

		for (final String group : presenter.getMetadataGroupList()) {
			table.addItem(group);
			table.getContainerProperty(group, "caption").setValue(group);
			Button deleteButton = new DeleteButton() {
				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					presenter.deleteGroupMetadata(group);
				}
			};
			deleteButton.addStyleName(GROUP_DELETE_BUTTON);

			table.getContainerProperty(group, "button").setValue(deleteButton);
		}

		return table;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		presenter.backButtonClicked();
	}

	@Override
	public void refreshTable() {
		Table table = buildTable();
		viewLayout.replaceComponent(groups, table);
		groups = table;
	}

	@Override
	public void displayAddError() {
		this.showErrorMessage($("ListMetadataGroupSchemaTypeView.addError"));
	}

	@Override
	public void displayDeleteError() {
		this.showErrorMessage($("ListMetadataGroupSchemaTypeView.deleteError"));
	}
}
