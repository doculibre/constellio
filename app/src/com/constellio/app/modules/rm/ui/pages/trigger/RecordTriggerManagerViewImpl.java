package com.constellio.app.modules.rm.ui.pages.trigger;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionConverter;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.menuBar.ActionMenuDisplay;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.constellio.app.ui.i18n.i18n.$;

public class RecordTriggerManagerViewImpl extends BaseViewImpl implements RecordTriggerManagerView {
	public static final String TRIGGER_BUTTONS = "triggerButtons";

	private RecordTriggerManagerPresenter presenter;
	private RecordVOLazyContainer dataSource;
	private String recordTitle;

	public RecordTriggerManagerViewImpl() {
		this.presenter = new RecordTriggerManagerPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	public void setRecordTitle(String title) {
		this.recordTitle = title;
	}

	@Override
	protected ActionMenuDisplay buildActionMenuDisplay(ActionMenuDisplay defaultActionMenuDisplay) {
		return new ActionMenuDisplay(defaultActionMenuDisplay) {
			@Override
			public Supplier<List<MenuItemAction>> getUseTheseActionsInQuickActionInsteadSupplier() {
				return () -> Stream.of(
						Arrays.asList(buildAddRuleQuickActionButton())
				).map(MenuItemActionConverter::toMenuItemAction).collect(Collectors.toList());
			}
		};
	}

	private Button buildAddRuleQuickActionButton() {
		Button addRuleButton = new BaseButton($("RecordTriggerManagerViewImpl.addRule")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addRecordTriggerClicked();
			}
		};

		return addRuleButton;
	}

	@Override
	protected boolean isOnlyQuickMenuActionVisible() {
		return true;
	}

	@Override
	protected String getActionMenuBarCaption() {
		return null;
	}

	@Override
	public String getTitle() {
		return $("RecordTriggerManagerViewImpl.title", recordTitle);
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout verticalLayout = new VerticalLayout();

		dataSource = new RecordVOLazyContainer(presenter.getDataProvider());
		RecordVOTable recordTable = new RecordVOTable(dataSource);

		recordTable.addGeneratedColumn(TRIGGER_BUTTONS, new Table.ColumnGenerator() {
			@Override
			public Component generateCell(Table source,
										  final Object itemId, Object columnId) {
				return new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						RecordVOItem recordVOItem = (RecordVOItem) recordTable.getItem(itemId);

						Map<String, String> paramsForNav = new HashMap<>();

						paramsForNav.putAll(presenter.getParams());
						paramsForNav.put("trigger", recordVOItem.getRecord().getId());

						navigate().to(RMViews.class).addEditTriggerToRecord(paramsForNav);
					}
				};
			}
		});

		recordTable.setColumnHeader(TRIGGER_BUTTONS, "");
		recordTable.setColumnWidth(TRIGGER_BUTTONS, 40);

		verticalLayout.addComponent(recordTable);
		verticalLayout.setSizeFull();
		verticalLayout.setMargin(new MarginInfo(true, false));
		recordTable.setSizeFull();
		verticalLayout.setExpandRatio(recordTable, 1);

		return verticalLayout;
	}


	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return presenter.getBuildBreadcrumbTrail();
	}

	@Override
	protected boolean isBreadcrumbsVisible() {
		return true;
	}
}
