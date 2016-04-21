package com.constellio.app.ui.pages.management.schemas.display.group;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.MultilingualTextField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

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
		viewLayout.addComponents(groups);
		return viewLayout;
	}

	private Table buildTable() {
		Table table = new Table();
		table.setWidth("100%");
		table.setColumnHeader("button", "");
		table.setColumnWidth("button", 60);
		table.setColumnHeader("code", $("ListMetadataGroupSchemaTypeView.code"));
		table.addContainerProperty("code", String.class, "");
		for (String language : presenter.getCollectionLanguages()) {
			table.setColumnHeader(language, language);
			table.addContainerProperty(language, String.class, "");
		}
		//		table.setColumnHeader("caption", $("ListMetadataGroupSchemaTypeView.caption"));
		//		table.addContainerProperty("caption", String.class, "");
		table.addContainerProperty("button", Button.class, null);
		table.addStyleName(GROUP_TABLE);

		for (final String group : presenter.getMetadataGroupList()) {
			table.addItem(group);
			table.getContainerProperty(group, "code").setValue(group);

			for (String language : presenter.getCollectionLanguages()) {
				String groupLabel = presenter.getGroupLabel(group, language);
				table.getContainerProperty(group, language).setValue(groupLabel);
			}

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

	@Override
	public void invalidCodeOrLabels() {
		this.showErrorMessage($("ListMetadataGroupSchemaTypeView.invalidCodeOrLabels"));
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> buttons = new ArrayList<>();

		Button addGroupsButton = newAddGroupButton();

		buttons.add(addGroupsButton);

		return buttons;
	}

	private Button newAddGroupButton() {
		return new WindowButton($("add"),
				$("ListMetadataGroupSchemaTypeView.addGroups")) {
			@Override
			protected Component buildWindowContent() {

				final TextField groupCode = new TextField();
				groupCode.setRequired(true);
				groupCode.setCaption($("ListMetadataGroupSchemaTypeView.code"));
				groupCode.setNullRepresentation("");
				groupCode.addStyleName(GROUP_NAME);

				final MultilingualTextField multilingualTextField = new MultilingualTextField();
				multilingualTextField.setRequired(true);
				multilingualTextField.addStyleName(GROUP_NAME);

				Button addButton = new AddButton() {
					@Override
					public void buttonClick(ClickEvent event) {
						if (presenter.isValidCodeAndLabels(groupCode.getValue(), multilingualTextField.getValue())) {
							presenter.addGroupMetadata(groupCode.getValue(), multilingualTextField.getValue());
							multilingualTextField.clear();
							getWindow().close();
						} else {
							showErrorMessage($("ListMetadataGroupSchemaTypeView.invalidCodeOrLabels"));
						}
					}
				};
				addButton.addStyleName(GROUP_BUTTON);

				BaseButton cancel = new BaseButton($("cancel")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						getWindow().close();
					}
				};
				cancel.addStyleName(ValoTheme.BUTTON_PRIMARY);

				HorizontalLayout horizontalLayout = new HorizontalLayout();
				horizontalLayout.addComponent(groupCode);
				horizontalLayout.addComponent(multilingualTextField);
				horizontalLayout.setWidth("95%");
				horizontalLayout.setSpacing(true);

				HorizontalLayout buttonsLayout = new HorizontalLayout();
				buttonsLayout.addComponents(addButton, cancel);
				buttonsLayout.setSpacing(true);

				VerticalLayout wrapper = new VerticalLayout(horizontalLayout, buttonsLayout);
				wrapper.setSizeFull();

				return wrapper;
			}
		};
	}
}
