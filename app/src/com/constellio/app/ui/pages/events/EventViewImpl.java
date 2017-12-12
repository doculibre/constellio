package com.constellio.app.ui.pages.events;

import java.util.List;
import java.util.Map;

import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DisplayWindowButton;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.columns.RecordVOTableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.data.event.EventTypeUtils;
import com.constellio.app.ui.framework.data.event.UnsupportedEventTypeRuntimeException;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickListener;

public class EventViewImpl extends BaseViewImpl implements EventView {

	private static Logger LOGGER = LoggerFactory.getLogger(EventViewImpl.class);
	
	public static final String EVENT_TABLE_STYLE = "selenium-event-table";
	private EventPresenter presenter;
	private Table table;
	private Map<String, String> parameters;
	public static final String OPEN_SESSION = "open_session";

	public static final String EVENT_DEFAULT_TYPE = "event_default_type";
	public static final String EVENT_DEFAULT_USERNAME = "event_default_username";

	public EventViewImpl() {
		this.presenter = new EventPresenter(this);
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout page = new VerticalLayout();
		page.setSizeFull();
		page.setSpacing(true);
		table = buildTable();
		page.addComponent(table);
		return page;
	}

	private Table buildTable() {
		final RecordVODataProvider dataProvider = presenter.getDataProvider();
		final String eventType = presenter.getEventType();
		Container container = new RecordVOLazyContainer(dataProvider, false);

		String eventTypeCaption;
    	try {
    		eventTypeCaption = EventTypeUtils.getEventTypeCaption(eventType);
		} catch (UnsupportedEventTypeRuntimeException e) {
			LOGGER.error("Error while retrieving event type caption", e);
			eventTypeCaption = eventType;
		}
		String title = eventTypeCaption + " (" + container.size() + ")";
		final Boolean isRecordEvent = EventTypeUtils.isRecordEvent(eventType);
		final RecordVOTable table = new RecordVOTable(title, container, isRecordEvent) {
			@Override
			protected Component buildMetadataComponent(MetadataValueVO metadataValue, RecordVO recordVO) {
				if (presenter.isDeltaMetadata(metadataValue)) {
					return displayButton(metadataValue);
				} else if(presenter.isTypeMetadata(metadataValue)) {
					return newEventTypeLabel(metadataValue);
				} else {
					return super.buildMetadataComponent(metadataValue, recordVO);
				}
			}

			@Override
			protected RecordVO getRecordVOForTitleColumn(Item item) {
				try {
					if (isRecordEvent) {
						RecordVO eventVO = ((RecordVOItem) item).getRecord();
						return presenter.getLinkedRecordVO(eventVO);
					} else {
						return super.getRecordVOForTitleColumn(item);
					}
				} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
					return super.getRecordVOForTitleColumn(item);
				}
			}

			@Override
			protected String getTitleForRecordVO(RecordVO titleRecordVO, String prefix, String title) {
				if (isRecordEvent) {
					title += " (" + titleRecordVO.getId() + ")";
				}
				return super.getTitleForRecordVO(titleRecordVO, prefix, title);
			}

			@Override
			public boolean isContextMenuPossible() {
				return false;
			}

			@Override
			protected TableColumnsManager newColumnsManager() {
				if (OPEN_SESSION.equalsIgnoreCase(presenter.getEventType())) {
					return new RecordVOTableColumnsManager() {
						@Override
						protected List<String> getDefaultVisibleColumnIds(Table table) {
							List<String> defaultVisibleColumnIds = super.getDefaultVisibleColumnIds(table);
							String usernameColumnId = "event_default_username";
							String titleColumnId = "event_default_title";
							if (!defaultVisibleColumnIds.contains(usernameColumnId)) {
								defaultVisibleColumnIds.add(usernameColumnId);
								defaultVisibleColumnIds.remove(titleColumnId);
							}
							return defaultVisibleColumnIds;
						}
					};
				} else {
					return new RecordVOTableColumnsManager() {
						@Override
						protected List<String> getDefaultVisibleColumnIds(Table table) {
							List<String> defaultVisibleColumnIds = super.getDefaultVisibleColumnIds(table);
							defaultVisibleColumnIds.add(EVENT_DEFAULT_TYPE);
							defaultVisibleColumnIds.add(EVENT_DEFAULT_USERNAME);
							return defaultVisibleColumnIds;
						}
					};
				}
			}
		};
		if (isRecordEvent) {
			table.addItemClickListener(new ItemClickListener() {
				@Override
				public void itemClick(ItemClickEvent event) {
					Object itemId = event.getItemId();
					RecordVOItem recordVOItem = (RecordVOItem) table.getItem(itemId);
					RecordVO recordVO = recordVOItem.getRecord();
					presenter.recordLinkClicked(recordVO);
				}
			});
		}
//		table.setPageLength(table.getItemIds().size());
		table.setWidth("100%");
		table.addStyleName(EVENT_TABLE_STYLE);
		return table;
	}

	private static Component newEventTypeLabel(MetadataValueVO metadataValue) {
		final String type = (metadataValue.getValue() != null) ? metadataValue.getValue().toString() : "";
		String eventTypeCaption = "";
		try {
			eventTypeCaption = EventTypeUtils.getEventTypeCaption(type);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Label(eventTypeCaption);
	}

	private static Component displayButton(MetadataValueVO metadataValue) {
		final String delta = (metadataValue.getValue() != null) ? metadataValue.getValue().toString() : "";
		DisplayWindowButton displayButton = new DisplayWindowButton("", delta) {
			@Override
			public boolean isVisible() {
				return super.isVisible() && StringUtils.isNotBlank(delta);
			}
		};
		return displayButton;
	}

	@Override
	public Map<String, String> getParameters() {
		return parameters;
	}

	@Override
	protected String getTitle() {
		String eventType = presenter.getEventType();
		String eventTypeCaption;
    	try {
    		eventTypeCaption = EventTypeUtils.getEventTypeCaption(eventType);
		} catch (UnsupportedEventTypeRuntimeException e) {
			LOGGER.error("Error while retrieving event type caption", e);
			eventTypeCaption = eventType;
		}
		return eventTypeCaption;
	}

	public Table getTable()
	{
		return table;
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				presenter.backButtonClick();
			}
		};
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		String paramString = event.getParameters();
		String viewNameAndParameters = NavigatorConfigurationService.EVENT_DISPLAY + "/" + paramString;
		parameters = ParamUtils.getParamsMap(viewNameAndParameters);
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

}