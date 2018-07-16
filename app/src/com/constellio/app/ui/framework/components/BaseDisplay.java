package com.constellio.app.ui.framework.components;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class BaseDisplay extends CustomComponent {
	
	public static final String STYLE_NAME = "base-display";
	public static final String STYLE_CAPTION = "display-caption";
	public static final String STYLE_VALUE = "display-value";
	
	private boolean useTabSheet;

	private VerticalLayout mainLayout;
	
	private TabSheet tabSheet;
	
	private Map<String, VerticalLayout> tabs = new HashMap<>();

	public BaseDisplay(List<CaptionAndComponent> captionsAndDisplayComponents) {
		addStyleName(STYLE_NAME);
		
		setSizeFull();
		
		tabSheet = new TabSheet();

		mainLayout = newMainLayout();
		
		if (isUseTabsheet()) {
			boolean atLeastOneTabCaption = false;
			for (CaptionAndComponent captionAndComponent : captionsAndDisplayComponents) {
				String tabCaption = captionAndComponent.tabCaption;
				if (StringUtils.isNotBlank(tabCaption)) {
					atLeastOneTabCaption = true;
					break;
				}
			}
			useTabSheet = atLeastOneTabCaption;
		} else {
			useTabSheet = false;
		}
		
		if (useTabSheet) {
			setCompositionRoot(tabSheet);
		} else {
			setCompositionRoot(mainLayout);
		}
		
		setCaptionsAndComponents(captionsAndDisplayComponents);
	}
	
	protected void setCaptionsAndComponents(List<CaptionAndComponent> captionsAndDisplayComponents) {
		if (mainLayout.iterator().hasNext()) {
			mainLayout.removeAllComponents();
		}
		for (CaptionAndComponent captionAndComponent : captionsAndDisplayComponents) {
			Label captionLabel = captionAndComponent.captionLabel;
			Component displayComponent = captionAndComponent.displayComponent;
			String tabCaption = captionAndComponent.tabCaption;
			captionLabel.addStyleName(STYLE_CAPTION);
			displayComponent.addStyleName(STYLE_VALUE);
			addToDefaultLayoutOrTabSheet(captionLabel, displayComponent, tabCaption);
		}
	}
	
	private VerticalLayout newMainLayout() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeUndefined();
		mainLayout.setSpacing(true);
		mainLayout.addStyleName(STYLE_NAME + "-main-layout");
		return mainLayout;
	}

	private void addToDefaultLayoutOrTabSheet(Label captionLabel, Component displayComponent, String tabCaption) {
		VerticalLayout layout;
		if (useTabSheet) {
			if (StringUtils.isBlank(tabCaption)) {
				tabCaption = $("BaseDisplay.defaultTab");
			}
			Resource tabIcon = getTabIcon(tabCaption);
			layout = tabs.get(tabCaption);
			if (layout == null) {
				layout = new VerticalLayout();
				layout.setWidth("100%");
				tabs.put(tabCaption, layout);
				layout.setSpacing(true);
				addTab(tabSheet, layout, tabCaption, tabIcon);
			}
		} else {
			layout = mainLayout;
		}
		addCaptionAndDisplayComponent(captionLabel, displayComponent, layout);
	}
	
	protected void addTab(TabSheet tabSheet, Component tabComponent, String caption, Resource icon) {
		tabSheet.addTab(tabComponent, caption, icon);
	}

	protected Resource getTabIcon(String tabCaption) {
		return null;
	}
	
	protected void addCaptionAndDisplayComponent(Label captionLabel, Component displayComponent, VerticalLayout layout) {
		if (displayComponent.isVisible()) {
			I18NHorizontalLayout captionAndComponentLayout = new I18NHorizontalLayout();
			if (isCaptionAndDisplayComponentWidthUndefined()) {
				captionAndComponentLayout.setWidthUndefined();
			} else {
				captionAndComponentLayout.setSizeFull();
			}
			
			layout.addComponent(captionAndComponentLayout);
			captionAndComponentLayout.addComponent(captionLabel);
			captionAndComponentLayout.addComponent(displayComponent);
		}
	}
	
	protected boolean isCaptionAndDisplayComponentWidthUndefined() {
		return false;
	}
	
	protected boolean isUseTabsheet() {
		return false;
	}
	
	public static class CaptionAndComponent implements Serializable {
		
		public Label captionLabel;
		
		public Component displayComponent;
		
		public String tabCaption;

		public CaptionAndComponent(Label captionLabel, Component displayComponent) {
			this(captionLabel, displayComponent, null);
		}

		public CaptionAndComponent(Label captionLabel, Component displayComponent, String tabCaption) {
			super();
			this.captionLabel = captionLabel;
			this.displayComponent = displayComponent;
			this.tabCaption = tabCaption;
		}
		
	}

}
