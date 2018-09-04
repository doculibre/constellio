package com.constellio.app.ui.pages.management.schemas.type;

import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.model.entities.records.Record;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class CannotDeleteWindow extends VerticalLayout {

	private static final String RECORD_ID = "CannotDeleteWindow.recordId";
	private static final String RECORD_TITLE = "CannotDeleteWindow.recordTitle";
	private static final String IS_IN_TRASH_QUESTION = "CannotDeleteWindow.isInTrashQuestion";

	private Label cannotDeleteLabel;
	private Label recordAccessLabel;
	private Button okButton;
	private Table recordsTable;
	private String recordAccessMessage;

	public CannotDeleteWindow(String cannotDeleteMessage) {
		this.cannotDeleteLabel = new Label(cannotDeleteMessage);
		cannotDeleteLabel.addStyleName(ValoTheme.LABEL_H2);
	}

	private void buildWindowConponents() {
		setSpacing(true);
		setWidth("90%");
		addStyleName("CannotDeleteWindow");

		okButton = new Button("Ok");
		okButton.addStyleName("OkButton");
		okButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		okButton.setEnabled(true);
		okButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				closeWindow();
			}
		});
	}

	public void buildWindowConponentsWithoutTable() {
		buildWindowConponents();
		addComponents(cannotDeleteLabel, okButton);
	}

	public void buildWindowConponentsWithTable(List<Record> records, Boolean areAllRecordsVisible) {
		buildWindowConponents();
		if (areAllRecordsVisible) {
			recordsTable = buildRecodsTable(records);
			addComponents(cannotDeleteLabel, recordsTable, okButton);
		} else {
			if (records.size() == 0) {
				recordAccessMessage = $("CannotDeleteWindow.recordAcessMessageWithNoRecords");
				recordAccessLabel = new Label(recordAccessMessage);
				addComponents(cannotDeleteLabel, recordAccessLabel, okButton);
			} else {
				recordAccessMessage = $("CannotDeleteWindow.recordAcessMessage");
				recordAccessLabel = new Label(recordAccessMessage);
				recordsTable = buildRecodsTable(records);
				addComponents(cannotDeleteLabel, recordsTable, recordAccessLabel, okButton);
			}
		}
	}

	private Table buildRecodsTable(List<Record> records) {
		Table table = new Table();
		table.setWidth("90%");
		table.addContainerProperty($(RECORD_ID), String.class, null);
		table.addContainerProperty($(RECORD_TITLE), String.class, null);
		table.addContainerProperty($(IS_IN_TRASH_QUESTION), String.class, null);

		int numberOfRecords = 0;
		for (Record record : records) {
			table.addItem(new String[]{record.getId(), record.getTitle(), record.isActive() ? $("CannotDeleteWindow.isActive") : $("CannotDeleteWindow.isInactive")}, numberOfRecords);
			numberOfRecords++;
		}

		table.setPageLength(table.size());

		return table;
	}

	public Window openWindow() {
		Window warningWindow = new BaseWindow($("CannotDeleteWindow.warning"), this);
		warningWindow.center();
		warningWindow.setModal(true);
		UI.getCurrent().addWindow(warningWindow);
		return warningWindow;
	}

	public void closeWindow() {
		Window window = (Window) getParent();
		window.close();
	}

}
