package com.constellio.app.ui.pages.events;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Date;

import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.components.DateRangePanel;
import com.constellio.app.ui.framework.components.EventByIdSearchPanel;
import com.constellio.app.ui.framework.components.EventReportGenerationPanel;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.event.EventCategoryContainer;
import com.constellio.app.ui.framework.data.event.EventStatistics;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Container;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class BaseEventCategoryViewImpl extends BaseViewImpl implements BaseEventCategoryView {
	public static final String TABLE_STYLE_CODE = "seleniumTableCode";
	private static final String PROPERTY_BUTTONS = "buttons";
	BaseEventCategoryPresenter presenter;
	private Panel reportGenerationPanel;
	private Panel fetchByIdPanel;
	private Panel dateRangePanel;

	private EventViewParameters eventViewParameters;

	VerticalLayout viewLayout;
	Table statisticsTable;

	public BaseEventCategoryViewImpl() {
		this.presenter = new BaseEventCategoryPresenter(this);
	}

	@Override
	final protected Component buildMainComponent(ViewChangeEvent event) {
		viewLayout = new VerticalLayout();
		if (presenter.hasFetchById(eventViewParameters.getEventCategory())) {
			fetchByIdPanel = buildFetchByIdPanel(eventViewParameters.getEventId());
			viewLayout.addComponent(fetchByIdPanel);
			viewLayout.setComponentAlignment(fetchByIdPanel, Alignment.MIDDLE_CENTER);
		}
		if (presenter.isByRangeDate(eventViewParameters.getEventCategory())) {
			viewLayout.addComponent(new Label("&nbsp;", ContentMode.HTML));
			dateRangePanel = buildDateRangePanel(eventViewParameters.getEventStartDate(), eventViewParameters.getEventEndDate());
			dateRangePanel.setWidthUndefined();
			viewLayout.addComponent(dateRangePanel);
			viewLayout.setComponentAlignment(dateRangePanel, Alignment.MIDDLE_CENTER);
		}
		if (presenter.isWithReportPanel(eventViewParameters.getEventCategory())) {
			viewLayout.addComponent(new Label("&nbsp;", ContentMode.HTML));
			reportGenerationPanel = buildReportGenerationPanel(eventViewParameters.getEventCategory());
			reportGenerationPanel.setWidthUndefined();
			viewLayout.addComponent(reportGenerationPanel);
			viewLayout.setComponentAlignment(reportGenerationPanel, Alignment.MIDDLE_CENTER);
		}

		statisticsTable = buildStatisticsTable();
		viewLayout.addComponent(statisticsTable);
		viewLayout.setExpandRatio(statisticsTable, 1);


		return viewLayout;
	}

	private Panel buildDateRangePanel(Date startDate, Date endDate) {
		return new DateRangePanel(startDate, endDate) {
			@Override
			protected void okButtonClick() {
				refreshTable();
			}
		};
	}

	private Panel buildFetchByIdPanel(String id) {
		return new EventByIdSearchPanel(eventViewParameters.getEventCategory(), id);
	}

	private Panel buildReportGenerationPanel(EventCategory eventCategory) {
		return new EventReportGenerationPanel(eventCategory) {
			@Override
			protected String getReportTitle(EventCategory eventCategory) {
				return presenter.getReportTitle(eventCategory);
			}
		};
	}

	private Table buildStatisticsTable() {
		final EventsCategoryDataProvider dataProvider = getEventListDataProvider();
		if (dataProvider.size() == 0) {
			Table table = new Table();
			table.setVisible(false);
			return table;
		}
		Container container = new EventCategoryContainer(dataProvider);
		ButtonsContainer buttonsContainer = new ButtonsContainer(container, PROPERTY_BUTTONS);
		addButtons(dataProvider, buttonsContainer);
		container = buttonsContainer;

		Table table = new Table("", container);
		table.setColumnHeader(EventCategoryContainer.LABEL, $("title"));
		table.setColumnHeader(EventCategoryContainer.VALUE, $("value"));
		table.setPageLength(table.getItemIds().size());
		table.setWidth("100%");
		table.setSelectable(true);
		table.setImmediate(true);
		table.setColumnHeader(PROPERTY_BUTTONS, "");
		table.setColumnWidth(PROPERTY_BUTTONS, 120);

		table.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				Integer index = (Integer) event.getItemId();
				displayButtonClicked(index);
			}
		});
		table.addStyleName(TABLE_STYLE_CODE);
		return table;
	}

	private void addButtons(final EventsCategoryDataProvider provider, ButtonsContainer buttonsContainer) {
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				DisplayButton displayButton = new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						EventStatistics entity = provider.getEventStatistics((Integer) itemId);
						Float value = entity.getValue();
						if(value != 0){
							displayButtonClicked((Integer) itemId);
						}
					}
				};
				EventStatistics entity = provider.getEventStatistics((Integer) itemId);
				Float value = entity.getValue();
				boolean visible = value != 0;
				displayButton.setVisible(visible);
				return displayButton;
			}
		});
	}

	public void refreshTable() {
		Table newTable = buildStatisticsTable();
		viewLayout.replaceComponent(statisticsTable, newTable);
		statisticsTable = newTable;
	}

	@Override
	public Date getEventStartDate() {
		if (dateRangePanel == null) {
			return eventViewParameters.getEventStartDate();
		}
		if (dateRangePanel instanceof DateRangePanel) {
			return ((DateRangePanel) dateRangePanel).getEventStartDate();
		} else {
			return null;
		}
	}

	@Override
	public Date getEventEndDate() {
		if (dateRangePanel == null) {
			return eventViewParameters.getEventEndDate();
		}
		if (dateRangePanel instanceof DateRangePanel) {
			return ((DateRangePanel) dateRangePanel).getEventEndDate();
		} else {
			return null;
		}
	}

	@Override
	public String getEventId() {
		if (fetchByIdPanel == null) {
			return eventViewParameters.getEventId();
		} else {
			return ((EventByIdSearchPanel) fetchByIdPanel).getIdValue();
		}
	}

	protected void displayButtonClicked(Integer itemId) {
		presenter.displayEvent(itemId, eventViewParameters.getEventCategory());
	}

	protected EventsCategoryDataProvider getEventListDataProvider() {
		return presenter.getEventListDataProvider(eventViewParameters.getEventCategory());
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.eventAudit();
			}
		};
	}

	@Override
	protected String getTitle() {
		String title = presenter.getTitle(eventViewParameters.getEventCategory());
		return title;
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		eventViewParameters = new EventViewParameters(event.getParameters());
	}


}
