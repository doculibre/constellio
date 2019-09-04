package com.constellio.app.ui.pages.events;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DisplayWindowButton;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.app.ui.framework.components.content.InputStreamWrapper;
import com.constellio.app.ui.framework.components.content.LazyStreamRessource;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
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
import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class EventViewImpl extends BaseViewImpl implements EventView {

	private static Logger LOGGER = LoggerFactory.getLogger(EventViewImpl.class);

	public static final String EVENT_TABLE_STYLE = "selenium-event-table";
	private EventPresenter presenter;
	private Table table;
	private Map<String, String> parameters;
	public static final String OPEN_SESSION = "open_session";

	public static final String EVENT_DEFAULT_TYPE = "event_default_type";
	public static final String EVENT_DEFAULT_USERNAME = "event_default_username";

	private DownloadLink generateCSVDownloadLink;
	private LazyStreamRessource lazyStreamRessource;
	private InputStreamWrapper inputStreamWrapper;

	private RecordVOLazyContainer container;

	public EventViewImpl() {
		this.presenter = new EventPresenter(this);
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout page = new VerticalLayout();
		page.setSizeFull();
		page.setSpacing(true);


		inputStreamWrapper = presenter.createInputStreamWrapper();
		lazyStreamRessource = new LazyStreamRessource(inputStreamWrapper, this.getTitle() + ".csv");

		generateCSVDownloadLink = new DownloadLink(lazyStreamRessource, $("generateCSVRepport"));
		generateCSVDownloadLink.addStyleName(ValoTheme.BUTTON_PRIMARY);
		page.addComponent(generateCSVDownloadLink);
		table = buildTable();
		page.addComponent(table);
		return page;
	}


	public String[] getTableColumn() {
		Object[] propertyIdCollection = table.getVisibleColumns();

		ArrayList<String> containerPropertyIdArray = new ArrayList<>();

		for (Object property : propertyIdCollection) {
			if (!table.isColumnCollapsed(property)) {
				containerPropertyIdArray.add(table.getColumnHeader(property));
			}
		}

		return containerPropertyIdArray.toArray(new String[0]);
	}


	public Object[] getTableVisibleProperties() {
		Object[] propertyIdCollection = table.getVisibleColumns();
		List<Object> nonCollapsedPropertyList = new ArrayList<>();

		for (Object propertyId : propertyIdCollection) {
			if (!table.isColumnCollapsed(propertyId)) {
				nonCollapsedPropertyList.add(propertyId);
			}
		}

		return nonCollapsedPropertyList.toArray();
	}

	private Table buildTable() {
		final RecordVODataProvider dataProvider = presenter.getDataProvider();
		final String eventType = presenter.getEventType();
		container = new RecordVOLazyContainer(dataProvider);

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
			protected Component buildMetadataComponent(Object itemId, MetadataValueVO metadataValue, RecordVO recordVO) {
				if (presenter.isDeltaMetadata(metadataValue)) {
					return displayButton(metadataValue);
				} else if (presenter.isTypeMetadata(metadataValue)) {
					return newEventTypeLabel(metadataValue);
				} else if (presenter.isNegativeAuthorizationMetadata(metadataValue)) {
					return negativeAuthorizationLabel(metadataValue);
				} else {
					RecordVO linkedRecordVO = presenter.getLinkedRecordVO(recordVO);
					if (presenter.isTitleMetadata(metadataValue) && isRecordEvent && linkedRecordVO != null) {
						ReferenceDisplay referenceDisplay = new ReferenceDisplay(linkedRecordVO, true);
						referenceDisplay.setCaption(getTitleForRecordVO(linkedRecordVO, "", referenceDisplay.getCaption()));
						return referenceDisplay;
					} else {
						return super.buildMetadataComponent(itemId, metadataValue, recordVO);
					}
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
							defaultVisibleColumnIds.add(Document.DEFAULT_SCHEMA + "_" + Document.CONTENT_CHECKED_OUT_BY);
							return defaultVisibleColumnIds;
						}

						@Override
						public void manage(Table table, String tableId) {
							super.manage(table, tableId);
							setColumnCollapsed("event_default_negative", false);
						}
					};
				}
			}

			@Override
			protected String getTitleColumnStyle(RecordVO recordVO) {
				return null;
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


	private static Component negativeAuthorizationLabel(MetadataValueVO metadataValue) {
		final String negative = negativeAuthorizationString(metadataValue);
		return new Label(negative);
	}

	public static String negativeAuthorizationString(MetadataValueVO metadataValue) {
		return (metadataValue != null && metadataValue.getValue() != null) ? $("yes") : $("no");
	}

	public static String negativeAuthorizationString(Boolean metadataValue) {
		return (metadataValue != null && metadataValue) ? $("yes") : $("no");
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

	public Table getTable() {
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