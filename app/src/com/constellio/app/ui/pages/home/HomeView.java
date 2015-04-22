/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.home;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.maddon.ListContainer;

import com.constellio.app.modules.rm.ui.pages.viewGroups.RecordsManagementViewGroup;
import com.constellio.app.ui.framework.buttons.BackButton;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.vaadin.data.Container;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class HomeView extends BaseViewImpl implements DropHandler, RecordsManagementViewGroup {

	HorizontalLayout mainLayout;

	public static class TestBean implements java.io.Serializable {

		String name;

		public TestBean(String name) {
			setName(name);
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}

	@Override
	protected String getTitle() {
		return $("HomeView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		Container container;
		List<TestBean> testBeans = new ArrayList<TestBean>();
		for (int i = 0; i < 1000; i++) {
			testBeans.add(new TestBean($("HomeView.testBean", i)));
		}
		container = new ListContainer<TestBean>(testBeans);

		Table table = new Table($("HomeView.records"), container);
		//        table.setWidth("100%");
		//        table.setHeight("200px");
		table.setSizeFull();
		//        table.setWidth("99%");
		//        return table;
		VerticalLayout homeLayout = new VerticalLayout();
		homeLayout.setHeight("100%");
		homeLayout.setWidth("100%");

		HorizontalLayout titleBackButtonLayout = new HorizontalLayout();
		titleBackButtonLayout.setWidth("100%");
		
		Label titleLabel = new Label("Test! Test! Test! Test! Test! Test! Test! Test! Test! Test! Test! Test! Test! Test! Test!");
		titleLabel.addStyleName("view-title");
		titleLabel.addStyleName(ValoTheme.LABEL_H1);
		
		homeLayout.addComponent(titleBackButtonLayout);
		titleBackButtonLayout.addComponents(titleLabel);
		titleBackButtonLayout.setExpandRatio(titleLabel, 1);

		for (int i = 0; i < 100; i++) {
			homeLayout.addComponent(new Label(
					i + " VVVV DDDD VVVV DDDD VVVV DDDD VVVV DDDD VVVV DDDD VVVV DDDD VVVV DDDD VVVV DDDD VVVV DDDD VVVV DDDD"));
		}
		homeLayout.addComponent(table);
		homeLayout.setExpandRatio(table, 1);

		return homeLayout;

		//		return new Label("TestTestTestTestTest TestTestTestTestTestTest TestTestTestTestTest TestTestTestTestTest TestTestTestTestTest");
		//		mainLayout = new HorizontalLayout();
		//		mainLayout.addComponent(new Label("Home page v2"));
		//		return mainLayout;
	}

	SearchServices getSearchServices() {
		return getConstellioFactories().getModelLayerFactory().newSearchServices();
	}

	MetadataSchemasManager getMetadataSchemasManager() {
		return getConstellioFactories().getModelLayerFactory().getMetadataSchemasManager();
	}

	RecordServices getRecordServices() {
		return getConstellioFactories().getModelLayerFactory().newRecordServices();
	}


	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				Notification.show("Back button clicked!");
			}
		};
	}
	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> actionMenuButtons = new ArrayList<Button>();

		Button editFolderButton = new Button($("HomeView.editFolder"));
		Button addDocumentButton = new Button($("HomeView.addDocument"));
		Button addDocumentsButton = new Button($("HomeView.addDocuments"));
		Button deleteFolderButton = new Button($("HomeView.deleteFolder"));
		Button duplicateFolderButton = new Button($("HomeView.duplicateFolder"));
		Button folderLinkButton = new Button($("HomeView.folderLink"));
		Button folderPermissionsButton = new Button($("HomeView.folderPermissions"));
		Button printFolderLabelButton = new Button($("HomeView.printFolderLabel"));

		folderPermissionsButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
			}
		});

		printFolderLabelButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
			}
		});

		actionMenuButtons.add(editFolderButton);
		actionMenuButtons.add(addDocumentButton);
		actionMenuButtons.add(addDocumentsButton);
		actionMenuButtons.add(deleteFolderButton);
		actionMenuButtons.add(duplicateFolderButton);
		actionMenuButtons.add(folderLinkButton);
		actionMenuButtons.add(folderPermissionsButton);
		actionMenuButtons.add(printFolderLabelButton);

		return actionMenuButtons;
	}

	@Override
	public void drop(DragAndDropEvent event) {
		Notification.show("Custom drag&drop!");
	}

	@Override
	public AcceptCriterion getAcceptCriterion() {
		return AcceptAll.get();
	}

}