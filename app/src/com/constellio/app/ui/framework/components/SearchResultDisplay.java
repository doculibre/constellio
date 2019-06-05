package com.constellio.app.ui.framework.components;

import com.constellio.app.api.extensions.params.AddComponentToSearchResultParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.mouseover.NiceTitle;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.search.SearchConfigurationsManager;
import com.constellio.model.services.users.CredentialUserPermissionChecker;
import com.google.common.base.Strings;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.application.ConstellioUI.getCurrent;
import static com.constellio.app.ui.i18n.i18n.$;

public class SearchResultDisplay extends VerticalLayout {

	public static final String RECORD_STYLE = "search-result-record";
	public static final String TITLE_STYLE = "search-result-title";
	public static final String HIGHLIGHTS_STYLE = "search-result-highlights";
	public static final String METADATA_STYLE = "search-result-metadata";
	public static final String ELEVATION_BUTTON_STYLE = "search-result-elevation";
	public static final String EXCLUSION_BUTTON_STYLE = "search-result-exclusion";
	public static final String SEPARATOR = " ... ";

	public static final String ELEVATION = "SearchResultDisplay.elevation";
	public static final String EXCLUSION = "SearchResultDisplay.exclusion";
	public static final String CANCEL_EXCLUSION = "SearchResultDisplay.unexclusion";
	public static final String CANCEL_ELEVATION = "SearchResultDisplay.unelevation";

	protected AppLayerFactory appLayerFactory;
	protected SessionContext sessionContext;

	SearchConfigurationsManager searchConfigurationsManager;

	SchemasRecordsServices schemasRecordsService;

	BaseButton excludeButton;
	BaseButton elevateButton;

	String query;
	Map<String, String> extraParam;

	private Component titleComponent;

	public SearchResultDisplay(SearchResultVO searchResultVO, MetadataDisplayFactory componentFactory,
							   AppLayerFactory appLayerFactory, String query) {
		this(searchResultVO, componentFactory, appLayerFactory, query, null);
	}

	public SearchResultDisplay(SearchResultVO searchResultVO, MetadataDisplayFactory componentFactory,
							   AppLayerFactory appLayerFactory, String query, Map<String,String> extraParam) {
		this.appLayerFactory = appLayerFactory;
		this.extraParam = extraParam;
		schemasRecordsService = new SchemasRecordsServices(ConstellioUI.getCurrentSessionContext().getCurrentCollection(),
				getAppLayerFactory().getModelLayerFactory());
		this.query = query;
		searchConfigurationsManager = getAppLayerFactory().getModelLayerFactory().getSearchConfigurationsManager();

		this.sessionContext = getCurrent().getSessionContext();
		init(searchResultVO, componentFactory);
	}

	public Map<String, String> getExtraParam() {
		return extraParam;
	}

	public void setExtraParam(Map<String, String> extraParam) {
		this.extraParam = extraParam;
	}

	protected void init(SearchResultVO searchResultVO, MetadataDisplayFactory componentFactory) {
		titleComponent = newTitleComponent(searchResultVO);

		List<Component> addtionalComponent = appLayerFactory.getExtensions().forCollection(sessionContext.getCurrentCollection())
				.addComponentToSearchResult(new AddComponentToSearchResultParams(searchResultVO));

		addComponent(titleComponent);
		addComponent(newHighlightsLabel(searchResultVO));
		if (addtionalComponent != null && addtionalComponent.size() > 0) {
			addComponent(multipleComponentIntoVerticalLayout(addtionalComponent));
		}
		addComponent(newMetadataComponent(searchResultVO, componentFactory));

		addStyleName(RECORD_STYLE);
		setWidth("100%");
		setSpacing(true);
	}

	private Component multipleComponentIntoVerticalLayout(List<Component> componentList) {
		VerticalLayout verticalLayout = new VerticalLayout();
		if(componentList.size() > 1) {
			for (Component currentComponent : componentList) {
				verticalLayout.addComponent(currentComponent);
			}

			return verticalLayout;
		} else {
			return componentList.get(0);
		}
	}

	protected Component newTitleComponent(SearchResultVO searchResultVO) {
		final RecordVO record = searchResultVO.getRecordVO();

		VerticalLayout titleLayout = new VerticalLayout();
		titleLayout.setWidth("100%");
		Component titleLink = newTitleLink(searchResultVO);
		titleLink.addStyleName(TITLE_STYLE);
//		titleLink.setWidthUndefined();
		titleLayout.addComponent(titleLink);

		SessionContext currentSessionContext = ConstellioUI.getCurrentSessionContext();
		CredentialUserPermissionChecker userHas = getAppLayerFactory().getModelLayerFactory().newUserServices()
				.has(currentSessionContext.getCurrentUser().getUsername());

		if (!Strings.isNullOrEmpty(query) && Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()
			&& userHas.globalPermissionInAnyCollection(CorePermissions.EXCLUDE_AND_RAISE_SEARCH_RESULT)) {
			titleLink.setWidth("80%");
			
			boolean isElevated = searchConfigurationsManager.isElevated(currentSessionContext.getCurrentCollection(), query, record.getId());

			Resource elevateIcon = isElevated ? FontAwesome.ARROW_CIRCLE_O_DOWN : FontAwesome.ARROW_CIRCLE_O_UP;
			String elevateText = isElevated ? $(CANCEL_ELEVATION) : $(ELEVATION);
			String elevateNiceTitleText = isElevated ? $(CANCEL_ELEVATION + "NiceTitle") : $(ELEVATION + "NiceTitle");

			excludeButton = new ExcludeButton();
			elevateButton = new ElevateButton(elevateText, elevateIcon, elevateNiceTitleText);

			I18NHorizontalLayout elevationLayout = new I18NHorizontalLayout();
			elevationLayout.addStyleName("search-result-elevation-buttons");
			elevationLayout.addComponent(excludeButton);
			elevationLayout.addComponent(elevateButton);
			elevationLayout.setComponentAlignment(excludeButton, Alignment.TOP_LEFT);
			elevationLayout.setComponentAlignment(elevateButton, Alignment.TOP_LEFT);

			titleLayout.addComponent(elevationLayout, 0);
			titleLayout.setComponentAlignment(elevationLayout, Alignment.TOP_RIGHT);
			//			titleLayout.setExpandRatio(elevationLayout, 1);
			//			titleLayout.setSpacing(true);
		}
		return titleLayout;
	}

	protected void addVisitedStyleNameIfNecessary(Component titleLink, String id) {
		SessionContext sessionContext = getCurrent().getSessionContext();
		if (sessionContext.isVisited(id)) {
			titleLink.addStyleName("visited-link");
		}
	}

	protected Component newTitleLink(SearchResultVO searchResultVO) {
		return new ReferenceDisplay(searchResultVO.getRecordVO(), true, extraParam);
	}

	protected Component newMetadataComponent(SearchResultVO searchResultVO, MetadataDisplayFactory componentFactory) {
		Component metadata = buildMetadataComponent(searchResultVO.getRecordVO(), componentFactory);
		metadata.addStyleName(METADATA_STYLE);
		return metadata;
	}

	protected Label newHighlightsLabel(SearchResultVO searchResultVO) {
		String formattedHighlights = formatHighlights(searchResultVO.getHighlights(), searchResultVO.getRecordVO());
		Label highlights = new Label(formattedHighlights, ContentMode.HTML);
		highlights.addStyleName(HIGHLIGHTS_STYLE);
		if (StringUtils.isBlank(formattedHighlights)) {
			highlights.setVisible(false);
		}
		return highlights;
	}

	private String formatHighlights(Map<String, List<String>> highlights, RecordVO recordVO) {
		if (highlights == null) {
			return null;
		}

		String currentCollection = sessionContext.getCurrentCollection();
		List<String> collectionLanguages = appLayerFactory.getCollectionsManager().getCollectionLanguages(currentCollection);
		MetadataSchema schema = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(currentCollection).getSchema(recordVO.getSchema().getCode());
		SchemasDisplayManager displayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		List<String> highlightedDateStoreCodes = new ArrayList<>();
		for (Metadata metadata : schema.getMetadatas()) {
			if (displayManager.getMetadata(currentCollection, metadata.getCode()).isHighlight()) {
				highlightedDateStoreCodes.add(metadata.getDataStoreCode());
				for (String language : collectionLanguages) {
					highlightedDateStoreCodes.add(metadata.getAnalyzedField(language).getDataStoreCode());
				}
			}
		}
		List<String> parts = new ArrayList<>(highlights.size());
		for (Map.Entry<String, List<String>> entry : highlights.entrySet()) {
			if (highlightedDateStoreCodes.contains(entry.getKey())) {
				parts.add(StringUtils.join(entry.getValue(), SEPARATOR));
			}
		}
		return StringUtils.join(parts, SEPARATOR);
	}

	private Layout buildMetadataComponent(RecordVO recordVO, MetadataDisplayFactory componentFactory) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		for (MetadataValueVO metadataValue : recordVO.getSearchMetadataValues()) {
			if (recordVO.getMetadataCodes().contains(metadataValue.getMetadata().getCode())) {
				MetadataVO metadataVO = metadataValue.getMetadata();
				if (!metadataVO.codeMatches(CommonMetadataBuilder.TITLE)) {
					Component value = componentFactory.build(recordVO, metadataValue);
					if (value != null) {
						Label caption = new Label(metadataVO.getLabel() + ":");
						caption.addStyleName("metadata-caption");

						I18NHorizontalLayout item = new I18NHorizontalLayout(caption, value);
						item.setHeight("100%");
						item.setSpacing(true);
						item.addStyleName("metadata-caption-layout");

						layout.addComponent(item);
					}
				}
			}
		}
		return layout;
	}

	protected AppLayerFactory getAppLayerFactory() {
		return appLayerFactory;
	}

	public void addClickListener(ClickListener listener) {
		ReferenceDisplay referenceDisplay = ComponentTreeUtils.getFirstChild(titleComponent, ReferenceDisplay.class);
		if (referenceDisplay != null) {
			referenceDisplay.addClickListener(listener);
		}
	}

	public void addElevationClickListener(ClickListener listener) {
		if (elevateButton != null) {
			elevateButton.addClickListener(listener);
		}
	}

	public void addExclusionClickListener(ClickListener listener) {
		if (excludeButton != null) {
			excludeButton.addClickListener(listener);
		}
	}

	public Component getTitleComponent() {
		return titleComponent;
	}

	public static class ElevationButton extends BaseButton {

		public ElevationButton(String caption, Resource icon, String niceTitleText) {
			super(caption, icon, true);
			addStyleName(ValoTheme.BUTTON_LINK);
			addExtension(new NiceTitle(niceTitleText));
		}

		@Override
		protected void buttonClick(ClickEvent event) {
			// Real click listener in addElevationClickListener()
		}

	}

	public static class ElevateButton extends ElevationButton {

		public ElevateButton(String caption, Resource icon, String niceTitleText) {
			super(caption, icon, niceTitleText);
			addStyleName(ELEVATION_BUTTON_STYLE);
		}

	}

	public static class ExcludeButton extends ElevationButton {

		public ExcludeButton() {
			super($(EXCLUSION), FontAwesome.TIMES_CIRCLE_O, $(EXCLUSION + "NiceTitle"));
			addStyleName(EXCLUSION_BUTTON_STYLE);
		}

	}

}
