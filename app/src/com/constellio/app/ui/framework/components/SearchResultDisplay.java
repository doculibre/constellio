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
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.application.ConstellioUI.getCurrent;
import static com.constellio.app.ui.i18n.i18n.$;

public class SearchResultDisplay extends CssLayout {

	public static final String RECORD_STYLE = "search-result-record";
	public static final String TITLE_STYLE = "search-result-title";
	public static final String HIGHLIGHTS_STYLE = "search-result-highlights";
	public static final String METADATA_CAPTION_STYLE = "search-result-metadata-caption";
	public static final String METADATA_VALUE_STYLE = "search-result-metadata-value";
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
	boolean noLinks;

	private Component titleLink;

	private Boolean lastModeDesktop;

	public SearchResultDisplay(SearchResultVO searchResultVO, MetadataDisplayFactory componentFactory,
							   AppLayerFactory appLayerFactory, String query, boolean noLinks) {
		this(searchResultVO, componentFactory, appLayerFactory, query, null, noLinks);
	}

	public SearchResultDisplay(SearchResultVO searchResultVO, MetadataDisplayFactory componentFactory,
							   AppLayerFactory appLayerFactory, String query, Map<String, String> extraParam,
							   boolean noLinks) {
		this.appLayerFactory = appLayerFactory;
		this.extraParam = extraParam;
		schemasRecordsService = new SchemasRecordsServices(ConstellioUI.getCurrentSessionContext().getCurrentCollection(),
				getAppLayerFactory().getModelLayerFactory());
		this.query = query;
		this.noLinks = noLinks;
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
		addStyleName(RECORD_STYLE);
		setWidth("100%");

		addTitleComponents(searchResultVO);

		addComponent(newHighlightsLabel(searchResultVO));
		List<Component> additionalComponents = appLayerFactory.getExtensions().forCollection(sessionContext.getCurrentCollection())
				.addComponentToSearchResult(new AddComponentToSearchResultParams(searchResultVO));
		for (Component additionalComponent : additionalComponents) {
			addComponent(additionalComponent);
		}
		buildMetadataComponent(searchResultVO.getRecordVO(), componentFactory);
	}

	private void addTitleComponents(SearchResultVO searchResultVO) {
		final RecordVO record = searchResultVO.getRecordVO();

		titleLink = newTitleLink(searchResultVO);
		titleLink.addStyleName(TITLE_STYLE);

		SessionContext currentSessionContext = ConstellioUI.getCurrentSessionContext();
		CredentialUserPermissionChecker userHas = getAppLayerFactory().getModelLayerFactory().newUserServices()
				.has(currentSessionContext.getCurrentUser().getUsername());

		if (!Strings.isNullOrEmpty(query) && Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()
			&& userHas.globalPermissionInAnyCollection(CorePermissions.EXCLUDE_AND_RAISE_SEARCH_RESULT)) {
			titleLink.setWidth("90%");
			
			addStyleName("search-result-with-elevation-buttons");
			
			boolean isElevated = searchConfigurationsManager.isElevated(currentSessionContext.getCurrentCollection(), query, record.getId());

			Resource elevateIcon = isElevated ? FontAwesome.ARROW_CIRCLE_O_DOWN : FontAwesome.ARROW_CIRCLE_O_UP;
			String elevateText = isElevated ? $(CANCEL_ELEVATION) : $(ELEVATION);
			String elevateNiceTitleText = isElevated ? $(CANCEL_ELEVATION + "NiceTitle") : $(ELEVATION + "NiceTitle");

			excludeButton = new ExcludeButton();
			elevateButton = new ElevateButton(elevateText, elevateIcon, elevateNiceTitleText);

			addComponent(elevateButton);
			addComponent(excludeButton);
		} else {
			titleLink.setWidth("100%");
		}
		addComponent(titleLink);
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

	private void buildMetadataComponent(RecordVO recordVO, MetadataDisplayFactory componentFactory) {
		if (noLinks) {
			StringBuilder sb = new StringBuilder();
			for (MetadataValueVO metadataValue : recordVO.getSearchMetadataValues()) {
				if (recordVO.getMetadataCodes().contains(metadataValue.getMetadata().getCode())) {
					MetadataVO metadataVO = metadataValue.getMetadata();
					if (!metadataVO.codeMatches(CommonMetadataBuilder.TITLE)) {
						String stringDisplayValue = componentFactory.buildString(recordVO, metadataValue);
						buildSearchResultInfo(sb, metadataVO.getLabel(), stringDisplayValue);
					}
				}
			}
			this.addComponent(addSearchResultMetadatas(sb));
		} else {
			for (MetadataValueVO metadataValue : recordVO.getSearchMetadataValues()) {
				if (recordVO.getMetadataCodes().contains(metadataValue.getMetadata().getCode())) {
					MetadataVO metadataVO = metadataValue.getMetadata();
					if (!metadataVO.codeMatches(CommonMetadataBuilder.TITLE)) {
						Component value = componentFactory.build(recordVO, metadataValue);
						if (value != null) {
							value.addStyleName("metadata-value");
							Label caption = new Label(metadataVO.getLabel() + ":");
							caption.addStyleName("metadata-caption");
							addComponents(caption, value);
						}
					}
				}
			}
		}
	}

	public static Label addSearchResultMetadatas(StringBuilder sb) {
		if (sb.length() > 0) {
			sb.insert(0, "<div class=\"search-result-metadatas\">");
			sb.append("</div>");
			return new Label(sb.toString(), ContentMode.HTML);
		} else {
			return null;
		}
	}

	public static void buildSearchResultInfo(StringBuilder sb, String label, String stringDisplayValue) {
		if (stringDisplayValue != null) {
			sb.append("<div class=\"search-result-metadata\">");
			sb.append("<div class=\"metadata-caption\">");
			sb.append(label);
			sb.append(":</div><div class=\"metadata-value\">");
			sb.append(stringDisplayValue);
			sb.append("</div>");
			sb.append("</div>");
		}
	}

	protected AppLayerFactory getAppLayerFactory() {
		return appLayerFactory;
	}

	@Override
	public void addLayoutClickListener(final LayoutClickListener layoutListener) {
		super.addLayoutClickListener(layoutListener);

		Button nestedButton = ComponentTreeUtils.getFirstChild(titleLink, Button.class);
		if (nestedButton != null) {
			nestedButton.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					MouseEventDetails mouseEventDetails = new MouseEventDetails();
					mouseEventDetails.setButton(MouseButton.LEFT);
					mouseEventDetails.setClientX(event.getClientX());
					mouseEventDetails.setClientY(event.getClientY());
					mouseEventDetails.setRelativeX(event.getRelativeX());
					mouseEventDetails.setRelativeY(event.getRelativeY());
					LayoutClickEvent layoutClickEvent = new LayoutClickEvent(event.getComponent(), mouseEventDetails, SearchResultDisplay.this, event.getComponent());
					layoutListener.layoutClick(layoutClickEvent);
				}
			});
		}
	}

	public void addClickListener(final ClickListener listener) {
		Button nestedButton = ComponentTreeUtils.getFirstChild(titleLink, Button.class);
		if (nestedButton != null) {
			nestedButton.addClickListener(listener);
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

	public Component getTitleLink() {
		return titleLink;
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
