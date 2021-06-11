package com.constellio.app.modules.rm.ui.pages.externallink;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.schemas.Schemas;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.List;
import java.util.Map.Entry;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListExternalLinksViewImpl extends BaseViewImpl implements ListExternalLinksView {

	private final ListExternalLinksPresenter presenter;

	private TabSheet tabSheet;

	public static boolean isOffice365SelectPopupShowing = false;

	private WindowButton externalLinksAddButton;

	public ListExternalLinksViewImpl() {
		presenter = new ListExternalLinksPresenter(this);
	}

	public void addSource(ExternalLinkSource source) {
		presenter.addSource(source);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	protected String getTitle() {
		return $("ListExternalLinksView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout mainLayout = new VerticalLayout();

		if (presenter.hasSource()) {
			Component addButton = buildAddButton();
			mainLayout.addComponents(addButton);
			mainLayout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);

			tabSheet = new TabSheet();
			mainLayout.addComponent(tabSheet);

			refreshTables();
			if (!presenter.hasExternalLinks() && !isOffice365SelectPopupShowing) {
				isOffice365SelectPopupShowing = true;
				externalLinksAddButton.click();
			}
		} else {
			Label errorLabel = new Label($("ListExternalLinksView.noSource"));
			errorLabel.addStyleName("error-label");
			mainLayout.addComponent(errorLabel);
		}

		return mainLayout;
	}

	private Component buildAddButton() {
		final ExternalLinkSource singleSource = presenter.hasSingleSource() ? presenter.getSources().get(0) : null;
		String windowTitle = singleSource != null
							 ? singleSource.getCaption()
							 : $("ListExternalLinksView.selectSource");
		WindowConfiguration config = singleSource != null
									 ? new WindowConfiguration(true, true, "50%", "50%")
									 : new WindowConfiguration(true, true, "30%", "25%");
		externalLinksAddButton = new WindowButton($("ListExternalLinksView.add"), windowTitle, config) {
			@Override
			protected Component buildWindowContent() {
				if (singleSource != null) {
					return buildSourceWindow(singleSource);
				}

				return buildSourceSelectionWindow(getWindow());
			}
		};

		if (singleSource != null) {
			externalLinksAddButton.addCloseListener(new CloseListener() {
				@Override
				public void windowClose(CloseEvent e) {
					refreshTables();
					isOffice365SelectPopupShowing = false;
				}
			});
		}

		externalLinksAddButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		externalLinksAddButton.addStyleName("add-button");

		return externalLinksAddButton;
	}

	private Component buildSourceSelectionWindow(Window window) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);

		ComboBox sourceBox = new BaseComboBox();
		sourceBox.setCaption(" ");
		sourceBox.setNullSelectionAllowed(false);
		for (ExternalLinkSource source : presenter.getSources()) {
			sourceBox.addItem(source);
			sourceBox.setItemCaption(source, source.getCaption());
		}
		layout.addComponent(sourceBox);
		layout.setComponentAlignment(sourceBox, Alignment.MIDDLE_CENTER);

		Button confirmButton = new BaseButton($("ListExternalLinksView.confirm")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (sourceBox.getValue() != null) {
					window.close();
					buildSourceButton((ExternalLinkSource) sourceBox.getValue());
				}
			}
		};

		layout.addComponent(confirmButton);
		layout.setComponentAlignment(confirmButton, Alignment.MIDDLE_CENTER);

		return layout;
	}

	private void buildSourceButton(ExternalLinkSource source) {
		WindowButton sourceButton = new WindowButton("", source.getCaption()) {
			@Override
			protected Component buildWindowContent() {
				return buildSourceWindow(source);
			}
		};
		sourceButton.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(CloseEvent e) {
				refreshTables();
			}
		});
		sourceButton.click();
	}

	private Component buildSourceWindow(ExternalLinkSource source) {
		return new ExternalLinkSourceViewImpl(source.getSource(), presenter.getFolderId());
	}

	private Component buildTable(RecordVODataProvider dataProvider) {
		ButtonsContainer recordsContainer = new ButtonsContainer<>(new RecordVOLazyContainer(dataProvider), "buttons");
		// TODO: Uncomment this code after March 31st demo and implement consult functionnality.
		/*recordsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						RecordVO entity = dataProvider.getRecordVO(index);
						presenter.displayButtonClicked(entity);
					}
				};
			}
		});*/
		recordsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				DeleteButton deleteButton = new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						Integer index = (Integer) itemId;
						RecordVO entity = dataProvider.getRecordVO(index);
						presenter.deleteButtonClicked(entity);
					}
				};
				return deleteButton;
			}
		});

		RecordVOTable table = new RecordVOTable($(dataProvider.getSchema().getLabel(), dataProvider.getSchema().getCode()), recordsContainer);
		table.setWidth("100%");
		table.setColumnHeader("buttons", "");
		table.setColumnWidth(dataProvider.getSchema().getCode() + "_id", 120);
		table.setColumnWidth("buttons", 40);
		table.setColumnCollapsible("buttons", false);
		table.setColumnExpandRatio(dataProvider.getSchema().getCode() + "_" + Schemas.TITLE_CODE, 1.0f);
		table.setPageLength(Math.min(15, dataProvider.size()));
		table.sort();

		return table;
	}

	public void refreshTables() {
		tabSheet.removeAllComponents();

		for (ExternalLinkSource source : presenter.getSources()) {
			for (Entry<String, List<String>> tab : source.getTabs()) {
				if (presenter.hasResults(tab.getValue())) {
					tabSheet.addTab(buildTable(presenter.getDataProvider(tab.getValue())), tab.getKey());
				}
			}
		}
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return presenter.getBreadCrumbTrail();
	}

	@Override
	protected boolean isBreadcrumbsVisible() {
		return true;
	}

}
