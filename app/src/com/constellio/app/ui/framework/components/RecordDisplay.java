package com.constellio.app.ui.framework.components;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.fields.comment.RecordCommentsDisplayImpl;
import com.constellio.app.ui.framework.components.fields.comment.RecordCommentsEditorImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.SchemaVOUtils;
import com.constellio.model.entities.Language;
import com.constellio.model.services.schemas.SchemaUtils;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("serial")
public class RecordDisplay extends BaseDisplay {

	public static final String STYLE_NAME = "record-display";
	private RecordVO recordVO;
	private MetadataDisplayFactory metadataDisplayFactory;

	public RecordDisplay(RecordVO recordVO) {
		this(recordVO, false);
	}

	public RecordDisplay(RecordVO recordVO, boolean useTabSheet) {
		this(recordVO, new MetadataDisplayFactory(), STYLE_NAME, useTabSheet);
	}

	public RecordDisplay(RecordVO recordVO, MetadataDisplayFactory metadataDisplayFactory) {
		this(recordVO, metadataDisplayFactory, false);
	}

	public RecordDisplay(RecordVO recordVO, MetadataDisplayFactory metadataDisplayFactory, boolean useTabSheet) {
		this(recordVO, metadataDisplayFactory, STYLE_NAME, useTabSheet);
	}

	public RecordDisplay(RecordVO recordVO, MetadataDisplayFactory metadataDisplayFactory, String styleName) {
		this(recordVO, metadataDisplayFactory, styleName, false);
	}

	public RecordDisplay(RecordVO recordVO, MetadataDisplayFactory metadataDisplayFactory, String styleName,
						 boolean useTabSheet) {
		super(toCaptionsAndComponents(recordVO, metadataDisplayFactory), useTabSheet);
		this.recordVO = recordVO;
		this.metadataDisplayFactory = metadataDisplayFactory;
		addStyleName(styleName);
	}

	private static List<CaptionAndComponent> toCaptionsAndComponents(RecordVO recordVO,
																	 MetadataDisplayFactory metadataDisplayFactory) {
		List<CaptionAndComponent> captionsAndComponents = new ArrayList<CaptionAndComponent>();

		Locale locale = ConstellioUI.getCurrentSessionContext().getCurrentLocale();
		for (MetadataValueVO metadataValue : recordVO.getDisplayMetadataValues()) {
			if (SchemaVOUtils.isMetadataNotPresentInList(metadataValue.getMetadata(), recordVO.getExcludedMetadataCodeList())) {
				Component displayComponent = metadataDisplayFactory.build(recordVO, metadataValue);
				if (displayComponent != null) {
					MetadataVO metadataVO = metadataValue.getMetadata();
					String tabCaption = getTabCaption(metadataVO);
					String tabCode = getTabCode(metadataVO);

					MetadataVO metadata = metadataValue.getMetadata();
					String caption = metadata.getLabel(locale);
					Label captionLabel = new Label(caption);

					String captionId = STYLE_CAPTION + "-" + metadata.getCode();
					captionLabel.setId(captionId);
					captionLabel.addStyleName(captionId);
					captionLabel.setVisible(displayComponent.isVisible());

					String valueId = STYLE_VALUE + "-" + metadata.getCode();
					displayComponent.setId(valueId);
					displayComponent.addStyleName(valueId);

					captionsAndComponents.add(new CaptionAndComponent(captionLabel, displayComponent, tabCaption, tabCode));
				}
			}
		}
		return captionsAndComponents;
	}

	private static String getTabCaption(MetadataVO metadataVO) {
		return metadataVO.getMetadataGroup();
	}

	private static String getTabCode(MetadataVO metadataVO) {
		SchemasDisplayManager schemasDisplayManager = ConstellioFactories.getInstance().getAppLayerFactory().getMetadataSchemasDisplayManager();
		MetadataDisplayConfig metadataDisplayConfig = schemasDisplayManager.getMetadata(metadataVO.getCollection(), metadataVO.getCode());
		return metadataDisplayConfig.getMetadataGroupCode();
	}

	public final RecordVO getRecordVO() {
		return recordVO;
	}

	public final void setRecordVO(RecordVO recordVO) {
		this.recordVO = recordVO;
	}

	private Component commentsComponent;

	@Override
	protected void addCaptionAndDisplayComponent(Label captionLabel, Component displayComponent,
												 VerticalLayout layout) {
		if ((displayComponent instanceof RecordCommentsEditorImpl) || (displayComponent instanceof RecordCommentsDisplayImpl)) {
			VerticalLayout verticalLayout = new VerticalLayout(displayComponent);
			verticalLayout.addStyleName("record-comments-layout");
			verticalLayout.setWidth("100%");
			verticalLayout.setSpacing(true);
			verticalLayout.addStyleName("record-comments-editor");
			layout.addComponent(commentsComponent = verticalLayout);
		} else {
			boolean addCommentsComponent;
			if (commentsComponent != null && layout.equals(commentsComponent.getParent())) {
				addCommentsComponent = true;
				layout.removeComponent(commentsComponent);
			} else {
				addCommentsComponent = false;
			}
			super.addCaptionAndDisplayComponent(captionLabel, displayComponent, layout);
			if (addCommentsComponent) {
				layout.addComponent(commentsComponent);
			}
		}
	}

	public void refresh() {
		setCaptionsAndComponents(toCaptionsAndComponents(this.recordVO, metadataDisplayFactory));
		reorderTabs();
	}

	@Override
	protected void addTab(TabSheet tabSheet, Component tabComponent, String caption, Resource icon) {
		super.addTab(tabSheet, tabComponent, caption, icon);
	}

	private void reorderTabs() {
		List<String> orderedTabCaptions = getOrderedTabCaptions(recordVO);
		List<String> usedTabCaptions = new ArrayList<>();
		for (String orderedTabCaption : orderedTabCaptions) {
			boolean usedTab = false;
			usedTabsLoop:
			for (int i = 0; i < tabSheet.getComponentCount(); i++) {
				Tab tab = tabSheet.getTab(i);
				String tabCaption = tab.getCaption();
				if (tabCaption.equals(orderedTabCaption)) {
					usedTab = true;
					break usedTabsLoop;
				}
			}
			if (usedTab && !usedTabCaptions.contains(orderedTabCaption)) {
				usedTabCaptions.add(orderedTabCaption);
			}
		}
		Map<Component, Integer> newTabOrders = new LinkedHashMap<>();
		for (int i = 0; i < tabSheet.getComponentCount(); i++) {
			Tab tab = tabSheet.getTab(i);
			String tabCaption = tab.getCaption();
			int tabOrder = usedTabCaptions.indexOf(tabCaption);
			if (tabOrder >= 0) {
				newTabOrders.put(tab.getComponent(), tabOrder);
			}
		}
		for (Component tabComponent : newTabOrders.keySet()) {
			Integer newPosition = newTabOrders.get(tabComponent);
			Tab tab = tabSheet.getTab(tabComponent);
			tabSheet.setTabPosition(tab, newPosition);
		}
		if (!newTabOrders.isEmpty()) {
			tabSheet.setSelectedTab(0);
		}
	}

	@Override
	public void attach() {
		reorderTabs();
		super.attach();
	}

	private List<String> getOrderedTabCaptions(RecordVO recordVO) {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		Locale currentLocale = sessionContext.getCurrentLocale();

		MetadataSchemaVO schemaVO = recordVO.getSchema();
		String collection = schemaVO.getCollection();
		String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaVO.getCode());
		List<String> orderedTabCaptions = new ArrayList<>();
		SchemasDisplayManager displayManager = ConstellioFactories.getInstance().getAppLayerFactory().getMetadataSchemasDisplayManager();
		SchemaTypeDisplayConfig typeConfig = displayManager.getType(collection, schemaTypeCode);
		Map<String, Map<Language, String>> groups = typeConfig.getMetadataGroup();
		for (String group : groups.keySet()) {
			Map<Language, String> groupLabels = groups.get(group);
			for (Language language : groupLabels.keySet()) {
				if (language.getLocale().equals(currentLocale)) {
					String tabCaption = groupLabels.get(language);
					if (!orderedTabCaptions.contains(tabCaption)) {
						orderedTabCaptions.add(tabCaption);
					}
				}
			}
		}
		return orderedTabCaptions;
	}

}
