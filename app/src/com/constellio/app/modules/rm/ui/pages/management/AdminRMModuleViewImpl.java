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
package com.constellio.app.modules.rm.ui.pages.management;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class AdminRMModuleViewImpl extends BaseViewImpl implements AdminRMModuleView {
	private final AdminRMModulePresenter presenter;
	private Button configButton;
	private Button ldapConfigButton;
	private Button taxonomiesButton;
	private Button valueDomainButton;
	private Button retentionCalendarButton;
	private Button metadataSchemasButton;
	private Button uniformSubdivisionsButton;
	private Button importButton;
	private Button manageUsersButton;
	private Button manageRolesButton;
	private Button manageGroupsButton;
	private Button manageUserCredentialsButton;
	private Button manageCollectionsButton;
	private Button dataExtractorButton;
	private Button connectorsButton;
	private Button searchEngineButton;
	private Button bigDataButton;
	private Button modulesButton;
	private Button updateCenterButton;
	private Button filingSpacesButton;
	private Button trashBinButton;

	public AdminRMModuleViewImpl() {
		presenter = new AdminRMModulePresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("AdminRMModuleView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		// TODO: Split in two sections
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.addStyleName("view-group");

		CssLayout buttonsLayout = new CssLayout();
		CssLayout systemSectionButtonsLayout = new CssLayout();

		configButton = new Button($("AdminRMModuleView.config"), new ThemeResource("images/icons/experience/configuration.png"));
		configButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.configButtonClicked();
			}
		});
		configButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		configButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		configButton.addStyleName("configButton");

		ldapConfigButton = new Button($("AdminRMModuleView.ldapConfig"),
				new ThemeResource("images/icons/experience/address_book3.png"));
		ldapConfigButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.ldapConfigButtonClicked();
			}
		});
		ldapConfigButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		ldapConfigButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		ldapConfigButton.addStyleName("ldapButton");

		modulesButton = new Button($("AdminRMModuleView.modules"), new ThemeResource("images/icons/experience/module.png"));
		modulesButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.modulesButtonClicked();
			}
		});
		modulesButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		modulesButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		modulesButton.addStyleName("modulesButton");
		modulesButton.setEnabled(false);

		filingSpacesButton = new Button($("AdminRMModuleView.filingSpaces"),
				new ThemeResource("images/icons/experience/filing-space.png"));
		filingSpacesButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.filingSpacesButtonClicked();
			}
		});
		filingSpacesButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		filingSpacesButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		filingSpacesButton.addStyleName("filingSpacesButton");

		taxonomiesButton = new Button($("AdminRMModuleView.taxonomies"),
				new ThemeResource("images/icons/experience/taxonomy.png"));
		taxonomiesButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.taxonomiesButtonClicked();
			}
		});
		taxonomiesButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		taxonomiesButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		taxonomiesButton.addStyleName("taxonomiesButton");

		valueDomainButton = new Button($("AdminRMModuleView.valueDomains"),
				new ThemeResource("images/icons/experience/value-domain.png"));
		valueDomainButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.valueDomainButtonClicked();
			}
		});
		valueDomainButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		valueDomainButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		valueDomainButton.addStyleName("valueDomainButton");

		//		classificationFilePlanButton = new Button($("AdminRMModuleView.classificationFilePlan"),
		//				new ThemeResource("images/icons/classification.png"));
		//		classificationFilePlanButton.addClickListener(new ClickListener() {
		//			@Override
		//			public void buttonClick(ClickEvent event) {
		//				presenter.classificationFilePlanButtonClicked();
		//			}
		//		});
		//		classificationFilePlanButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		//		classificationFilePlanButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);

		retentionCalendarButton = new Button($("AdminRMModuleView.retentionCalendar"),
				new ThemeResource("images/icons/experience/calendar.png"));
		retentionCalendarButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.retentionCalendarButtonClicked();
			}
		});
		retentionCalendarButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		retentionCalendarButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		retentionCalendarButton.addStyleName("retentionCalendarButton");

		metadataSchemasButton = new Button($("AdminRMModuleView.metadataSchemas"),
				new ThemeResource("images/icons/experience/metadata.png"));
		metadataSchemasButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.metadataSchemasButtonClicked();
			}
		});
		metadataSchemasButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		metadataSchemasButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		metadataSchemasButton.addStyleName("metadataSchemasButton");

		uniformSubdivisionsButton = new Button($("AdminRMModuleView.uniformSubdivisions"),
				new ThemeResource("images/icons/experience/uniform-subdivision.png"));
		uniformSubdivisionsButton.setVisible(presenter.isUniformSubdivisionsButtonVisible());
		uniformSubdivisionsButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.uniformSubdivisionsButtonClicked();
			}
		});
		uniformSubdivisionsButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		uniformSubdivisionsButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		uniformSubdivisionsButton.addStyleName("uniformSubdivisionsButton");

		importButton = new Button($("AdminRMModuleView.importFile"), new ThemeResource("images/icons/experience/import.png"));
		importButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.importFileButtonClicked();
			}
		});
		importButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		importButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		importButton.addStyleName("importButton");

		// TODO Change icon for trash bin
		trashBinButton = new Button($("AdminRMModuleView.trashBin"), new ThemeResource("images/icons/experience/garbage.png"));
		trashBinButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.trashBinButtonClicked();
			}
		});
		trashBinButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		trashBinButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		trashBinButton.addStyleName("trashBinButton");
		trashBinButton.setEnabled(false);

		manageUsersButton = new Button($("AdminRMModuleView.manageUsers"),
				new ThemeResource("images/icons/experience/collection-security.png"));
		manageUsersButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.manageUsersButtonClicked();
			}
		});
		manageUsersButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		manageUsersButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		manageUsersButton.addStyleName("manageUsersButton");

		manageRolesButton = new Button($("AdminRMModuleView.manageRoles"),
				new ThemeResource("images/icons/experience/crown.png"));
		manageRolesButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.manageRolesButtonClicked();
			}
		});
		manageRolesButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		manageRolesButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		manageRolesButton.addStyleName("manageRolesButton");

		manageGroupsButton = new Button($("AdminRMModuleView.manageGroups"),
				new ThemeResource("images/icons/experience/group.png"));
		manageGroupsButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.manageGroupsButtonClicked();
			}
		});
		manageGroupsButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		manageGroupsButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		manageGroupsButton.addStyleName("manageGroupsButton");

		manageUserCredentialsButton = new Button($("AdminRMModuleView.manageUserCredentials"),
				new ThemeResource("images/icons/experience/user.png"));
		manageUserCredentialsButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.manageUserCredentialsButtonClicked();
			}
		});
		manageUserCredentialsButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		manageUserCredentialsButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		manageUserCredentialsButton.addStyleName("manageUserCredentialButton");

		manageCollectionsButton = new Button($("AdminRMModuleView.manageCollections"),
				new ThemeResource("images/icons/experience/collections.png"));
		manageCollectionsButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.manageCollectionsButtonClicked();
			}
		});
		manageCollectionsButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		manageCollectionsButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		manageCollectionsButton.addStyleName("manageCollectionsButton");
		//manageCollectionsButton.setEnabled(false);

		dataExtractorButton = new Button($("AdminRMModuleView.dataExtractor"),
				new ThemeResource("images/icons/experience/metadata-extract.png"));
		dataExtractorButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.dataExtractorButtonClicked();
			}
		});
		dataExtractorButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		dataExtractorButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		dataExtractorButton.addStyleName("dataExtractorButton");
		dataExtractorButton.setEnabled(false);

		connectorsButton = new Button($("AdminRMModuleView.connectors"),
				new ThemeResource("images/icons/experience/connector.png"));
		connectorsButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.connectorsButtonClicked();
			}
		});
		connectorsButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		connectorsButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		connectorsButton.addStyleName("connectorsButton");
		connectorsButton.setEnabled(false);

		searchEngineButton = new Button($("AdminRMModuleView.searchEngine"),
				new ThemeResource("images/icons/experience/configuration-search.png"));
		searchEngineButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.searchEngineButtonClicked();
			}
		});
		searchEngineButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		searchEngineButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		searchEngineButton.addStyleName("searchEngineButton");
		searchEngineButton.setEnabled(false);

		bigDataButton = new Button($("AdminRMModuleView.bigData"), new ThemeResource("images/icons/experience/big-data.png"));
		bigDataButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.bigDataButtonClicked();
			}
		});
		bigDataButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		bigDataButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		bigDataButton.addStyleName("bigDataButton");
		bigDataButton.setEnabled(false);

		updateCenterButton = new Button($("AdminRMModuleView.updateCenter"),
				new ThemeResource("images/icons/experience/update-center.png"));
		updateCenterButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.updateCenterButtonClicked();
			}
		});
		updateCenterButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		updateCenterButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		updateCenterButton.addStyleName("updateCenterButton");

		mainLayout.addComponent(buttonsLayout);

		buttonsLayout.addComponent(taxonomiesButton);
		buttonsLayout.addComponent(uniformSubdivisionsButton);
		buttonsLayout.addComponent(retentionCalendarButton);
		buttonsLayout.addComponent(valueDomainButton);
		buttonsLayout.addComponent(metadataSchemasButton);
		buttonsLayout.addComponent(filingSpacesButton);
		buttonsLayout.addComponent(manageUsersButton);
		buttonsLayout.addComponent(manageRolesButton);
		buttonsLayout.addComponent(dataExtractorButton);
		buttonsLayout.addComponent(connectorsButton);
		buttonsLayout.addComponent(searchEngineButton);
		buttonsLayout.addComponent(trashBinButton);

		if (presenter.isSystemSectionVisible()) {
			Label systemSectionTitle = new Label($("AdminRMModuleView.systemSectionTitle"));
			systemSectionTitle.addStyleName(ValoTheme.LABEL_H1);
			mainLayout.addComponent(systemSectionTitle);
			systemSectionButtonsLayout.addComponent(configButton);
			systemSectionButtonsLayout.addComponent(ldapConfigButton);
			systemSectionButtonsLayout.addComponent(manageGroupsButton);
			systemSectionButtonsLayout.addComponent(manageUserCredentialsButton);
			systemSectionButtonsLayout.addComponent(manageCollectionsButton);
			systemSectionButtonsLayout.addComponent(modulesButton);
			systemSectionButtonsLayout.addComponent(importButton);
			systemSectionButtonsLayout.addComponent(bigDataButton);
			systemSectionButtonsLayout.addComponent(updateCenterButton);
			mainLayout.addComponent(systemSectionButtonsLayout);
		}

		presenter.onViewAssembled();

		return mainLayout;
	}

	@Override
	public void setManageTaxonomiesVisible(boolean visible) {
		taxonomiesButton.setVisible(visible);
	}

	@Override
	public void setManageUniformSubdivisionsVisible(boolean visible) {
		uniformSubdivisionsButton.setVisible(visible);
	}

	@Override
	public void setManageRetentionRuleVisible(boolean visible) {
		retentionCalendarButton.setVisible(visible);
	}

	@Override
	public void setManageValueListVisible(boolean visible) {
		valueDomainButton.setVisible(visible);
	}

	@Override
	public void setManageMetadataSchemasVisible(boolean visible) {
		metadataSchemasButton.setVisible(visible);
	}

	@Override
	public void setManageFilingSpaceVisible(boolean visible) {
		filingSpacesButton.setVisible(visible);
	}

	@Override
	public void setManageSecurityVisible(boolean visible) {
		manageUsersButton.setVisible(visible);
	}

	@Override
	public void setManageRolesVisible(boolean visible) {
		manageRolesButton.setVisible(visible);
	}

	@Override
	public void setManageConnectorsVisible(boolean visible) {
		connectorsButton.setVisible(visible);
	}

	@Override
	public void setManageSearchEngineVisible(boolean visible) {
		searchEngineButton.setVisible(visible);
	}

	@Override
	public void setManageMetadataExtractorVisible(boolean visible) {
		dataExtractorButton.setVisible(visible);
	}

	@Override
	public void setManageTrashVisible(boolean visible) {
		trashBinButton.setVisible(visible);
	}

	@Override
	public void setManageSystemConfiguration(boolean visible) {
		configButton.setVisible(visible);
	}

	@Override
	public void setManageLdapConfiguration(boolean visible) {
		ldapConfigButton.setVisible(visible);
	}

	@Override
	public void setManageSystemGroups(boolean visible) {
		manageGroupsButton.setVisible(visible);
	}

	@Override
	public void setManageSystemUsers(boolean visible) {
		manageUserCredentialsButton.setVisible(visible);
	}

	@Override
	public void setManageSystemCollections(boolean visible) {
		manageCollectionsButton.setVisible(visible);
	}

	@Override
	public void setManageSystemModules(boolean visible) {
		modulesButton.setVisible(visible);
	}

	@Override
	public void setManageSystemDataImports(boolean visible) {
		importButton.setVisible(visible);
	}

	@Override
	public void setManageSystemServers(boolean visible) {
		bigDataButton.setVisible(visible);
	}

	@Override
	public void setManageSystemUpdates(boolean visible) {
		updateCenterButton.setVisible(visible);
	}

}
