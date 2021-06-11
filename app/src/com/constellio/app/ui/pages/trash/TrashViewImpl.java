package com.constellio.app.ui.pages.trash;

import com.constellio.app.modules.restapi.core.util.ListUtils;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionConverter;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.menuBar.ActionMenuDisplay;
import com.constellio.app.ui.framework.containers.SchemaTypeVOLazyContainer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.constellio.app.ui.i18n.i18n.$;

public class TrashViewImpl extends BaseViewImpl implements TrashView {

	private final TrashPresenter presenter;
	private ComboBox typeSelectionDropDown;
	private Label recordsToDeleteMessage;
	private Button restoreSelectionButton, deleteSelectionButton;
	private Component logicallyDeletedRecordsTable;
	private VerticalLayout vLayout;

	public TrashViewImpl() {
		presenter = new TrashPresenter(this);
		buildActionMenuButtons();
	}

	@Override
	protected String getTitle() {
		return $("TrashView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		vLayout = new VerticalLayout();
		this.typeSelectionDropDown = buildTypeSelectionComponent();
		vLayout.addComponent(this.typeSelectionDropDown);
		this.recordsToDeleteMessage = buildRecordsToDeleteMessage();
		vLayout.addComponent(this.recordsToDeleteMessage);

		logicallyDeletedRecordsTable = buildTrashTable();
		vLayout.addComponent(logicallyDeletedRecordsTable);
		return vLayout;
	}

	private Label buildRecordsToDeleteMessage() {
		Label message = new Label(
				"<p style=\"color:red\">" + $("TrashView.recordsToDeleteMessage", presenter.getLogicallyDeletedRecordsCount())
				+ "</p>",
				ContentMode.HTML);
		return message;
	}

	private ComboBox buildTypeSelectionComponent() {
		ComboBox typeSelection = new BaseComboBox($("TrashView.typeSelection"));
		Container typeContainer = new SchemaTypeVOLazyContainer(presenter.getSchemaTypes());
		typeSelection.setContainerDataSource(typeContainer);
		typeSelection.setItemCaptionPropertyId("label");
		typeSelection.setNullSelectionAllowed(false);
		typeSelection.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				presenter.clearSelectedRecords();
				disableActionButtons();
				rebuildTrashTable();
			}

		});
		return typeSelection;
	}

	private void rebuildTrashTable() {
		Component newTable = buildTrashTable();
		vLayout.replaceComponent(this.logicallyDeletedRecordsTable, newTable);
		this.logicallyDeletedRecordsTable = newTable;

	}

	private void disableActionButtons() {
		this.restoreSelectionButton.setEnabled(false);
		this.deleteSelectionButton.setEnabled(false);
		refreshActionMenu();
	}

	@Override
	protected List<MenuItemAction> buildMenuItemActions(ViewChangeEvent event) {
		return ListUtils.flatMapFilteringNull(
				super.buildMenuItemActions(event),
				Stream.of(restoreSelectionButton, deleteSelectionButton).map(MenuItemActionConverter::toMenuItemAction).collect(Collectors.toList())
		);
	}

	@Override
	protected ActionMenuDisplay buildActionMenuDisplay(ActionMenuDisplay defaultActionMenuDisplay) {
		return new ActionMenuDisplay(defaultActionMenuDisplay) {
			@Override
			public boolean isQuickActionsAreVisible() {
				return false;
			}
		};
	}

	protected void buildActionMenuButtons() {
		buildRestoreSelectionButton();
		buildDeleteSelectionButton();
	}

	private Button buildDeleteSelectionButton() {
		deleteSelectionButton = new ConfirmDialogButton($("TrashView.deleteSelection")) {
			@Override
			protected String getConfirmDialogMessage() {
				return $("TrashView.deleteConfirmation");
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				Map<String, String> notDeletedIdsAndTitle = presenter.deleteSelection();
				replaceDeletedRecordsTypeAndCountComponents();
				rebuildTrashTable();
				enableOrDisableActionButtons();
				if (!notDeletedIdsAndTitle.isEmpty()) {
					StringBuffer errorMessage = new StringBuffer($("TrashView.deleteNotPossibleForRecords") + ":<br>");
					for (Map.Entry<String, String> deleteItem : notDeletedIdsAndTitle.entrySet()) {
						errorMessage.append(deleteItem.getKey() + " - " + deleteItem.getValue() + "<br>");
					}
					showErrorMessage(errorMessage.toString());
				}
			}
		};
		deleteSelectionButton.setEnabled(presenter.atLeastOneRecordSelected());
		return deleteSelectionButton;
	}


	private Button buildRestoreSelectionButton() {
		restoreSelectionButton = new ConfirmDialogButton($("TrashView.restoreSelection")) {
			@Override
			protected String getConfirmDialogMessage() {
				return $("TrashView.restoreConfirmation");
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				Map<String, String> notRestoredIdsAndTitle = presenter.restoreSelection();
				replaceDeletedRecordsTypeAndCountComponents();
				rebuildTrashTable();
				enableOrDisableActionButtons();
				if (!notRestoredIdsAndTitle.isEmpty()) {
					StringBuffer errorMessage = new StringBuffer($("TrashView.restoreNotPossibleForRecords") + ":<br>");
					for (Map.Entry<String, String> deleteItem : notRestoredIdsAndTitle.entrySet()) {
						errorMessage.append(deleteItem.getKey() + " - " + deleteItem.getValue() + "<br>");
					}
					showErrorMessage(errorMessage.toString());
				}
			}
		};
		restoreSelectionButton.setEnabled(presenter.atLeastOneRecordSelected());
		return restoreSelectionButton;
	}

	private void rebuildRecordsToDeleteMessage() {
		Label newMessage = buildRecordsToDeleteMessage();
		vLayout.replaceComponent(this.recordsToDeleteMessage, newMessage);
		this.recordsToDeleteMessage = newMessage;
	}

	@Override
	public String getSelectedType() {
		Item selection = typeSelectionDropDown.getItem(typeSelectionDropDown.getValue());
		if (selection != null) {
			return (String) selection.getItemProperty("code").getValue();
		} else {
			return null;
		}

	}

	private Component buildTrashTable() {
		if (StringUtils.isBlank(getSelectedType())) {
			Table emptyTable = new Table();
			emptyTable.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
			emptyTable.setVisible(false);
			return emptyTable;
		}

		Component trashRecords = new TrashRecordsTable(presenter.getTrashRecords(), presenter);

		//trashRecords.header
		return trashRecords;
	}

	@Override
	public void updateSelectDeselectAllToggle(boolean allItemsSelected) {
		if (logicallyDeletedRecordsTable instanceof TrashRecordsTable) {
			TrashRecordsTable trashRecordsTable = (TrashRecordsTable) logicallyDeletedRecordsTable;
			trashRecordsTable.getToggleButton().setSelectAllMode(!allItemsSelected);
		}
	}

	@Override
	public void enableOrDisableActionButtons() {
		boolean atLeastOneRecordSelected = presenter.atLeastOneRecordSelected();
		this.restoreSelectionButton.setEnabled(atLeastOneRecordSelected);
		this.deleteSelectionButton.setEnabled(atLeastOneRecordSelected);
		refreshActionMenu();
	}

	private void replaceDeletedRecordsTypeAndCountComponents() {
		ComboBox newType = buildTypeSelectionComponent();
		vLayout.replaceComponent(this.typeSelectionDropDown, newType);
		this.typeSelectionDropDown = newType;

		Label newMessage = buildRecordsToDeleteMessage();
		vLayout.replaceComponent(this.recordsToDeleteMessage, newMessage);
		this.recordsToDeleteMessage = newMessage;
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return (ClickListener) event -> presenter.backButtonClicked();
	}
}
