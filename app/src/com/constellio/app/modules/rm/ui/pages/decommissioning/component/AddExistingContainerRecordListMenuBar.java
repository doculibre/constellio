package com.constellio.app.modules.rm.ui.pages.decommissioning.component;

import com.constellio.app.ui.framework.components.menuBar.RecordListMenuBar;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.vaadin.server.FontAwesome;

import java.util.Collections;
import java.util.List;

public class AddExistingContainerRecordListMenuBar extends RecordListMenuBar {

	private MenuBarItemAdder menuBarItemAdder;

	public AddExistingContainerRecordListMenuBar(String rootItemCaption, MenuBarItemAdder menuBarItemAdder) {
		super(null, rootItemCaption, Collections.emptyList());
		this.menuBarItemAdder = menuBarItemAdder;
	}

	@Override
	public void buildMenuItems() {
		if (recordProvider == null) {
			throw new ImpossibleRuntimeException("Mauvaise utilisation de AddExistingContainerRecordListMenuBar. " +
												 "Il faut setter un recordProvider avant de builder les menus");
		}

		removeItems();

		MenuItem rootItem = addItem(rootItemCaption, FontAwesome.ELLIPSIS_V, null);

		menuBarItemAdder.addMenuBarItems(rootItem, recordProvider.getRecords());
	}

	public interface MenuBarItemAdder {
		void addMenuBarItems(MenuItem rootItem, List<Record> records);
	}
}
