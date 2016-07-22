package com.constellio.app.ui.pages.events;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DisplayWindowButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.data.event.EventTypeUtils;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.schemas.Schemas;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class EventViewImpl extends BaseViewImpl implements EventView {
	public static final String EVENT_TABLE_STYLE = "selenium-event-table";
	private EventPresenter presenter;
	private Table table;
	private Map<String, String> parameters;

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
		Container container = new RecordVOLazyContainer(dataProvider);

		String title = EventTypeUtils.getEventTypeCaption(eventType) + "(" + container.size() + ")";
		final Boolean isRecordEvent = EventTypeUtils.isRecordEvent(eventType);
		RecordVOTable table = new RecordVOTable(title, container, isRecordEvent){
			@Override
			protected Component buildMetadataComponent(MetadataValueVO metadataValue, RecordVO recordVO) {
				if (presenter.isRecordIdMetadata(metadataValue)){
					return recordLink(metadataValue, recordVO);
				}else if (presenter.isDeltaMetadata(metadataValue)){
					return displayButton(metadataValue);
				}else {
					return super.buildMetadataComponent(metadataValue, recordVO);
				}
			}
		};
		if (isRecordEvent){
			table.collapseColumn(Schemas.TITLE.getLocalCode());
		}
		table.setPageLength(table.getItemIds().size());
		table.setWidth("100%");
		table.addStyleName(EVENT_TABLE_STYLE);
		return table;
	}

	private Component recordLink(final MetadataValueVO metadataValue, final RecordVO recordVO) {
		String linkTitle = presenter.getRecordLinkTitle(metadataValue, recordVO);
		LinkButton recordLink =  new LinkButton(linkTitle
		) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.recordLinkClicked(metadataValue);
			}
		};
		return recordLink;
	}

	private static Component displayButton(MetadataValueVO metadataValue) {
		final String delta = (metadataValue.getValue() != null)? metadataValue.getValue().toString(): "";
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
	protected String getTitle(){
		return  EventTypeUtils.getEventTypeCaption(presenter.getEventType());
	};

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

}
