package com.constellio.app.ui.pages.collection;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.buttons.RecordVOActionButtonFactory;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.data.dao.services.Stats;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class CollectionSecurityManagementImpl extends BaseViewImpl implements CollectionSecurityManagement {

	public static final String STYLE_NAME = "display-folder";

	private VerticalLayout mainLayout;
	private I18NHorizontalLayout contentLayout;
	private TabSheet tabSheet;
	private UserSecurityManagementImpl userTab;
	private GroupSecurityManagementImpl groupTab;

	private RMModuleExtensions rmModuleExtensions;

	private RecordVODataProvider userDataProvider;
	private RecordVODataProvider groupDataProvider;
	private CollectionSecurityManagementPresenter presenter;
	private TabSheet.SelectedTabChangeListener selectedTabChangeListener;

	private Button addUserButton, addGroupButton;
	private VerticalLayout groupsTab;

	public enum TabType {USER, GROUP}

	public CollectionSecurityManagementImpl() {
		this(null);
	}

	public CollectionSecurityManagementImpl(RecordVO recordVO) {
		presenter = Stats.compilerFor(getClass().getSimpleName()).log(() -> {
			return new CollectionSecurityManagementPresenter(this);
		});
		rmModuleExtensions = getConstellioFactories().getAppLayerFactory()
				.getExtensions().forCollection(getCollection()).forModule(ConstellioRMModule.ID);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		if (event != null) {
			presenter.forParams(event.getParameters());
		}
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.addStyleName("display-folder-view");
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		tabSheet = new TabSheet();
		tabSheet.addStyleName(STYLE_NAME);

		userTab = new UserSecurityManagementImpl();
		groupTab = new GroupSecurityManagementImpl();

		tabSheet.addTab(userTab.createTabLayout(),
				$("CollectionSecurityManagement.users"));
		groupsTab = groupTab.createTabLayout();
		tabSheet.addTab(groupsTab, $("CollectionSecurityManagement.groups"));

		tabSheet.addSelectedTabChangeListener(selectedTabChangeListener = new TabSheet.SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
				Component selectedTab = tabSheet.getSelectedTab();
				if (selectedTab == userTab) {
					presenter.userTabSelected();
				} else {
					presenter.groupTabSelected();
				}
			}
		});

		if (presenter.isGroupTabSelectedFirst()) {
			tabSheet.setSelectedTab(groupsTab);
		}


		contentLayout = new I18NHorizontalLayout(tabSheet);
		contentLayout.setWidth("100%");
		contentLayout.setExpandRatio(tabSheet, 1);
		mainLayout = new VerticalLayout(contentLayout);

		return mainLayout;
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		addUserButton = newAddUserButton();
		addGroupButton = newAddGroupButton();

		List<String> excludedActionTypes = new ArrayList<String>();

		excludedActionTypes.addAll(rmModuleExtensions.getFilteredActionsForFolders());
		return new RecordVOActionButtonFactory(null, excludedActionTypes).build();
	}

	@Override
	protected List<Button> getQuickActionMenuButtons() {
		List<Button> quickActionMenuButtons = new ArrayList<>();

		if (addUserButton != null) {
			quickActionMenuButtons.add(addUserButton);
		}
		if (addGroupButton != null) {
			quickActionMenuButtons.add(addGroupButton);
		}

		return quickActionMenuButtons;
	}

	@Override
	protected String getTitle() {
		return $("AdminView.security");
	}

	@Override
	public RecordVODataProvider getUserDataProvider() {
		return userDataProvider;
	}

	@Override
	public void setUserDataProvider(RecordVODataProvider dataProvider) {
		this.userDataProvider = dataProvider;
	}

	@Override
	public RecordVODataProvider getGroupDataProvider() {
		return groupDataProvider;
	}

	@Override
	public void setGroupDataProvider(RecordVODataProvider dataProvider) {
		this.groupDataProvider = dataProvider;
	}

	@Override
	public void selectGroupTab() {
		tabSheet.removeSelectedTabChangeListener(selectedTabChangeListener);

		//viewerPanel = groupTab.createTabLayout();

		tabSheet.setSelectedTab(groupTab);
		tabSheet.addSelectedTabChangeListener(selectedTabChangeListener);
	}

	@Override
	public void selectUserTab() {
		tabSheet.removeSelectedTabChangeListener(selectedTabChangeListener);
		//viewerPanel = userTab.createTabLayout();
		tabSheet.setSelectedTab(userTab);
		tabSheet.addSelectedTabChangeListener(selectedTabChangeListener);
	}

	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {
		presenter.viewAssembled();
	}

	private Button newAddUserButton() {
		BaseButton addUserButton;

		addUserButton = new AddButton($("CollectionSecurityManagement.addUser")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addUserButtonClicked();
			}
		};
		addUserButton.setIcon(FontAwesome.USER_PLUS);
		addUserButton.setCaptionVisibleOnMobile(false);
		addUserButton.setEnabled(presenter.canAddUser());
		return addUserButton;
	}

	private Button newAddGroupButton() {
		BaseButton addGroupButton;

		addGroupButton = new AddButton($("CollectionSecurityManagement.addGroup")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addGroupButtonClicked();
			}
		};
		addGroupButton.setIcon(FontAwesome.GROUP);
		addGroupButton.setCaptionVisibleOnMobile(false);
		addGroupButton.setEnabled(presenter.canAddGroup());
		return addGroupButton;
	}

	@Override
	protected boolean isOnlyQuickMenuActionVisible() {
		return true;
	}

	@Override
	protected boolean isActionMenuBar() {
		return true;
	}

}
