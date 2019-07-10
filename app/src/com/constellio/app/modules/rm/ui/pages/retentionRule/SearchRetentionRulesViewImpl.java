package com.constellio.app.modules.rm.ui.pages.retentionRule;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.SearchButton;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.dialogs.ConfirmDialog;

import static com.constellio.app.ui.i18n.i18n.$;

public class SearchRetentionRulesViewImpl extends BaseViewImpl implements SearchRetentionRulesView {
	public static final String STYLE_NAME = "search-retention-rules";

	private HorizontalLayout searchLayout;
	private final SearchRetentionRulesPresenter presenter;
	private RecordVODataProvider dataProvider;

	public SearchRetentionRulesViewImpl() {
		presenter = new SearchRetentionRulesPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
		presenter.viewAssembled();
	}

	@Override
	public void setDataProvider(RecordVODataProvider dataProvider) {
		this.dataProvider = dataProvider;
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
	protected String getTitle() {
		return $("ListRetentionRulesView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {

		buildSearch();

		RecordVOTable table = new RecordVOTable($("ListRetentionRulesView.tableTitle", dataProvider.size()), buildContainer());
		table.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		table.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 120);
		table.setSizeFull();
		table.setPageLength(table.size());
		setDefaultOrderBy(presenter.getDefaultOrderField(), dataProvider, table);
		table.sort();

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.addComponents(searchLayout, table);
		mainLayout.setExpandRatio(table, 1);
		mainLayout.setSpacing(true);
		mainLayout.setSizeFull();

		return mainLayout;
	}

	private void setDefaultOrderBy(String localCode, RecordVODataProvider dataProvider, Table table) {
		Object[] properties = {dataProvider.getSchema().getMetadata(localCode)};
		boolean[] ordering = {true};
		table.sort(properties, ordering);
	}

	private Container buildContainer() {
		final ButtonsContainer<RecordVOLazyContainer> rules = new ButtonsContainer<>(new RecordVOLazyContainer(dataProvider));
		rules.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						RecordVO recordVO = rules.getNestedContainer().getRecordVO((int) itemId);
						presenter.displayButtonClicked(recordVO);
					}
				};
			}
		});

		if (presenter.userHaveManageRetentionRulePermission()) {
			rules.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
					return new EditButton() {
						@Override
						protected void buttonClick(ClickEvent event) {
							RecordVO recordVO = rules.getNestedContainer().getRecordVO((int) itemId);
							presenter.editButtonClicked(recordVO);
						}
					};
				}
			});


			rules.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
					DeleteButton deleteButton = new DeleteButton() {
						@Override
						protected void confirmButtonClick(ConfirmDialog dialog) {
							RecordVO recordVO = rules.getNestedContainer().getRecordVO((int) itemId);
							presenter.deleteButtonClicked(recordVO);
						}
					};
					return deleteButton;
				}
			});
		}

		return rules;
	}

	private void buildSearch() {
		searchLayout = new HorizontalLayout();
		searchLayout.setSpacing(true);
		final TextField searchField = new BaseTextField();
		searchField.focus();
		searchField.setValue(presenter.getQueryExpression());
		searchField.setNullRepresentation("");
		Button searchButton = new SearchButton();
		searchButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.search(searchField.getValue());
			}
		});
		searchLayout.addComponents(searchField, searchButton);

		OnEnterKeyHandler onEnterHandler = new OnEnterKeyHandler() {
			@Override
			public void onEnterKeyPressed() {
				presenter.search(searchField.getValue());
			}
		};
		onEnterHandler.installOn(searchField);
	}

}
