package com.constellio.app.ui.framework.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchConfigurationsManager;
import com.constellio.model.services.users.CredentialUserPermissionChecker;
import com.google.common.base.Strings;
import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.vaadin.shared.ui.label.ContentMode;

import static com.constellio.app.ui.i18n.i18n.$;

public class SearchResultDisplay extends VerticalLayout {
	public static final String RECORD_STYLE = "search-result-record";
	public static final String TITLE_STYLE = "search-result-title";
	public static final String HIGHLIGHTS_STYLE = "search-result-highlights";
	public static final String METADATA_STYLE = "search-result-metadata";
	public static final String SEPARATOR = " ... ";

	public static final String ELEVATION = "SearchResultDisplay.elevation";
	public static final String EXCLUSION = "SearchResultDisplay.exclusion";
	public static final String UNEXCLUSION = "SearchResultDisplay.unexclusion";
	public static final String UNELEVATION = "SearchResultDisplay.unelevation";

	private AppLayerFactory appLayerFactory;

	SearchConfigurationsManager searchConfigurationsManager;

	SchemasRecordsServices schemasRecordsService;

	Button exclude;
	Button raise;

	String query;

	public SearchResultDisplay(SearchResultVO searchResultVO, MetadataDisplayFactory componentFactory, AppLayerFactory appLayerFactory, String query) {
		this.appLayerFactory = appLayerFactory;
		schemasRecordsService = new SchemasRecordsServices(ConstellioUI.getCurrentSessionContext().getCurrentCollection(), getAppLayerFactory().getModelLayerFactory());
		this.query = query;
		searchConfigurationsManager = getAppLayerFactory().getModelLayerFactory().getSearchConfigurationsManager();


		init(searchResultVO, componentFactory);
	}

	protected void init(SearchResultVO searchResultVO, MetadataDisplayFactory componentFactory) {
		addComponents(newTitleComponent(searchResultVO),
				newHighlightsLabel(searchResultVO),
				newMetadataComponent(searchResultVO, componentFactory));
		addStyleName(RECORD_STYLE);
		setWidth("100%");
		setSpacing(true);
	}

	protected Component newTitleComponent(SearchResultVO searchResultVO) {
		final RecordVO record = searchResultVO.getRecordVO();


		ReferenceDisplay title = new ReferenceDisplay(searchResultVO.getRecordVO());
		title.addStyleName(TITLE_STYLE);
		title.setWidthUndefined();

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.addComponent(title);

		CredentialUserPermissionChecker userHas = getAppLayerFactory().getModelLayerFactory().newUserServices()
				.has(ConstellioUI.getCurrentSessionContext().getCurrentUser().getUsername());


		final Record recordFromRecordVO = schemasRecordsService.get(record.getId());
		boolean isElevated = searchConfigurationsManager.isElevated(query, recordFromRecordVO);

		if(!Strings.isNullOrEmpty(query) && userHas.globalPermissionInAnyCollection(CorePermissions.EXCLUDE_AND_RAISE_SEARCH_RESULT)) {

			exclude = new LinkButton($(EXCLUSION)) {
				@Override
				protected void buttonClick(ClickEvent event) {
					if(event.getButton().getCaption().equals($(EXCLUSION))) {
						event.getButton().setCaption($(UNEXCLUSION));
						raise.setCaption($(ELEVATION));
						searchConfigurationsManager.setElevated(query, recordFromRecordVO, true);
					} else {
						event.getButton().setCaption($(EXCLUSION));
						searchConfigurationsManager.removeElevated(query, recordFromRecordVO.getId());
					}
				}
			};

			String elevatedText = ($(ELEVATION));
			if(isElevated) {
				elevatedText = $(UNELEVATION);
			}

			raise = new LinkButton(elevatedText) {
				@Override
				protected void buttonClick(ClickEvent event) {
					if(event.getButton().getCaption().equals($(ELEVATION))){
						event.getButton().setCaption($(UNELEVATION));
						exclude.setCaption($(EXCLUSION));
						searchConfigurationsManager.setElevated(query, recordFromRecordVO, false);
					} else {
						event.getButton().setCaption($(ELEVATION));
						searchConfigurationsManager.removeElevated(query, recordFromRecordVO.getId());
					}
				}
			};

			horizontalLayout.addComponent(exclude, 1);
			horizontalLayout.addComponent(raise, 2);
			horizontalLayout.setComponentAlignment(exclude,Alignment.TOP_LEFT);
			horizontalLayout.setComponentAlignment(raise, Alignment.TOP_LEFT);
			horizontalLayout.setExpandRatio(exclude,1);
			horizontalLayout.setExpandRatio(raise, 1);
			horizontalLayout.setSpacing(true);
		}
		return horizontalLayout;
	}

	protected Component newMetadataComponent(SearchResultVO searchResultVO, MetadataDisplayFactory componentFactory) {
		Component metadata = buildMetadataComponent(searchResultVO.getRecordVO(), componentFactory);
		metadata.addStyleName(METADATA_STYLE);
		return metadata;
	}

	protected Label newHighlightsLabel(SearchResultVO searchResultVO) {
		String formattedHighlights = formatHighlights(searchResultVO.getHighlights());
		Label highlights = new Label(formattedHighlights, ContentMode.HTML);
		highlights.addStyleName(HIGHLIGHTS_STYLE);
		if (StringUtils.isBlank(formattedHighlights)) {
			highlights.setVisible(false);
		}
		return highlights;
	}

	private String formatHighlights(Map<String, List<String>> highlights) {
		if (highlights == null) {
			return null;
		}
		List<String> parts = new ArrayList<>(highlights.size());
		for (List<String> fieldHighlights : highlights.values()) {
			parts.add(StringUtils.join(fieldHighlights, SEPARATOR));
		}
		return StringUtils.join(parts, SEPARATOR);
	}

	private Layout buildMetadataComponent(RecordVO recordVO, MetadataDisplayFactory componentFactory) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		for (MetadataValueVO metadataValue : recordVO.getSearchMetadataValues()) {
			MetadataVO metadataVO = metadataValue.getMetadata();
			if (metadataVO.codeMatches(CommonMetadataBuilder.TITLE)) {
				continue;
			}

			Component value = componentFactory.build(recordVO, metadataValue);
			if (value == null) {
				continue;
			}

			Label caption = new Label(metadataVO.getLabel() + ":");
			caption.addStyleName("metadata-caption");

			HorizontalLayout item = new HorizontalLayout(caption, value);
			item.setHeight("100%");
			item.setSpacing(true);
			item.addStyleName("metadata-caption-layout");

			layout.addComponent(item);
		}
		return layout;
	}

	protected AppLayerFactory getAppLayerFactory() {
		return appLayerFactory;
	}
}
