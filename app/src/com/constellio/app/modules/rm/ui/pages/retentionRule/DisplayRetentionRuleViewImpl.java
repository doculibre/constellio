package com.constellio.app.modules.rm.ui.pages.retentionRule;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.ui.components.retentionRule.RetentionRuleDisplay;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.BaseDisplay;
import com.constellio.app.ui.framework.components.BaseDisplay.CaptionAndComponent;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Container;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class DisplayRetentionRuleViewImpl extends BaseViewImpl implements DisplayRetentionRuleView {

	public static final String STYLE_NAME = "display-folder";

	private RetentionRuleVO retentionRuleVO;

	private VerticalLayout mainLayout;
	private VerticalLayout folderTabLayout;
	private VerticalLayout metadataTabLayout;

	private RetentionRuleDisplay recordDisplay;

	private Button editButton, deleteButton;

	private DisplayRetentionRulePresenter presenter;

	private TabSheet tabSheet;
	private Map<String, Component> tabComponents;

	public DisplayRetentionRuleViewImpl() {
		presenter = new DisplayRetentionRulePresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	public void setRetentionRule(RetentionRuleVO retentionRuleVO) {
		this.retentionRuleVO = retentionRuleVO;
	}

	@Override
	protected String getTitle() {
		return $("DisplayRetentionRuleView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);
		mainLayout.setMargin(new MarginInfo(true, true, false, true));

		tabSheet = new TabSheet();
		tabSheet.setStyleName(STYLE_NAME);

		folderTabLayout = new VerticalLayout(buildFoldersTabLayout());
		folderTabLayout.setSizeFull();

		metadataTabLayout = new VerticalLayout(buildMetadataTabLayout());
		metadataTabLayout.setSizeFull();

		tabSheet.addTab(metadataTabLayout, $("DisplayRetentionRuleView.tabs.metadata")).setStyleName("metadata");
		tabSheet.addTab(folderTabLayout, $("DisplayRetentionRuleView.tabs.folders")).setStyleName("folders");

		mainLayout.addComponent(tabSheet);

		return mainLayout;
	}

	private Component buildMetadataTabLayout() {
		VerticalLayout metadataTabContentLayout = new VerticalLayout();
		metadataTabContentLayout.setSpacing(true);
		metadataTabContentLayout.setSizeFull();

		recordDisplay = new RetentionRuleDisplay(presenter, retentionRuleVO, getSessionContext().getCurrentLocale());
		recordDisplay.setWidth("100%");

		Component component = buildAdditionalComponent();

		metadataTabContentLayout.addComponent(recordDisplay);
		metadataTabContentLayout.addComponent(component);

		return metadataTabContentLayout;
	}

	private Component buildFoldersTabLayout() {
		VerticalLayout folderTabContentLayout = new VerticalLayout();
		folderTabContentLayout.setSizeFull();

		RecordVODataProvider dataProvider = presenter.getDataProvider();
		Container recordsContainer = new RecordVOLazyContainer(dataProvider);
		String schemaTypeCode = retentionRuleVO.getSchema().getTypeCode();

		RecordVOTable table = new RecordVOTable($(dataProvider.getSchema().getLabel(), dataProvider.getSchema().getCode()), recordsContainer);
		table.setWidth("100%");
		table.setId("retentionRuleFoldersTable");
		table.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				RecordVOItem item = (RecordVOItem) event.getItem();
				RecordVO recordVO = item.getRecord();
				presenter.tabElementClicked(recordVO);
			}
		});
		folderTabContentLayout.addComponent(table);
		return folderTabContentLayout;
	}

	private Component buildAdditionalComponent() {
		Label foldersNumberCaptionLabel = new Label($("DisplayRetentionRuleView.foldersNumber"));
		foldersNumberCaptionLabel.setId("foldersNumber");
		foldersNumberCaptionLabel.addStyleName("foldersNumber");
		Label foldersNumberDisplayComponent = new Label(presenter.getFoldersNumber());
		foldersNumberDisplayComponent.addStyleName("display-value-foldersNumber");

		List<CaptionAndComponent> captionsAndComponents = new ArrayList<>();
		captionsAndComponents.add(new CaptionAndComponent(foldersNumberCaptionLabel, foldersNumberDisplayComponent));
		return new BaseDisplay(captionsAndComponents);
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> actionMenuButtons = super.buildActionMenuButtons(event);

		if (presenter.isManageRetentionRulesOnSomething()) {
			editButton = new EditButton(false) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.editButtonClicked();
				}
			};

			deleteButton = new DeleteButton(false) {
				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					presenter.deleteButtonClicked();
				}
			};
			deleteButton.setEnabled(presenter.validateDeletable(retentionRuleVO).isEmpty());

			actionMenuButtons.add(editButton);
			actionMenuButtons.add(deleteButton);
		}

		return actionMenuButtons;
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return new TitleBreadcrumbTrail(this, getTitle()) {
			@Override
			public List<? extends IntermediateBreadCrumbTailItem> getIntermediateItems() {
				return Arrays.asList(new IntermediateBreadCrumbTailItem() {
					@Override
					public String getTitle() {
						return $("ListRetentionRulesView.viewTitle");
					}

					@Override
					public void activate(Navigation navigate) {
						navigate.to(RMViews.class).listRetentionRules();
					}

					@Override
					public boolean isEnabled() {
						return true;
					}
				});
			}
		};
	}

}
