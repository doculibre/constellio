package com.constellio.app.ui.framework.components;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.util.ResponsiveUtils;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("serial")
public class BaseDisplay extends CustomComponent implements BrowserWindowResizeListener {

	public static final String STYLE_NAME = "base-display";
	public static final String STYLE_CAPTION = "display-caption";
	public static final String STYLE_VALUE = "display-value";

	private boolean useTabSheet;

	private VerticalLayout mainLayout;

	protected TabSheet tabSheet;

	private Map<String, Panel> tabs = new LinkedHashMap<>();

	private List<CaptionAndComponent> captionsAndDisplayComponents;

	private Boolean lastModeDesktop;

	public BaseDisplay(List<CaptionAndComponent> captionsAndDisplayComponents) {
		this(captionsAndDisplayComponents, false);
	}

	public BaseDisplay(List<CaptionAndComponent> captionsAndDisplayComponents, boolean useTabSheet) {
		this.useTabSheet = useTabSheet;
		addStyleName(STYLE_NAME);
		setSizeFull();
		setResponsive(true);

		tabSheet = new TabSheet();

		mainLayout = newMainLayout();

		if (isUseTabsheet()) {
			int tabCaptionCount = 0;

			Set<String> tabCaptions = new HashSet<>();
			for (CaptionAndComponent captionAndComponent : captionsAndDisplayComponents) {
				String tabCaption = captionAndComponent.tabCaption;
				if (StringUtils.isNotBlank(tabCaption) && !tabCaptions.contains(tabCaption)) {
					tabCaptions.add(tabCaption);
					tabCaptionCount++;
				}
			}
			this.useTabSheet = tabCaptionCount > 1;
		} else {
			this.useTabSheet = false;
		}

		if (this.useTabSheet) {
			setCompositionRoot(tabSheet);
		} else {
			setCompositionRoot(mainLayout);
		}

		setCaptionsAndComponents(captionsAndDisplayComponents);
	}

	@Override
	public void attach() {
		super.attach();
		Page.getCurrent().addBrowserWindowResizeListener(this);
		computeResponsive();
	}

	@Override
	public void detach() {
		Page.getCurrent().removeBrowserWindowResizeListener(this);
		super.detach();
	}

	@Override
	public void browserWindowResized(BrowserWindowResizeEvent event) {
		computeResponsive();
	}

	private void computeResponsive() {
		if (lastModeDesktop == null) {
			lastModeDesktop = ResponsiveUtils.isDesktop();
		}
		if ((lastModeDesktop && !ResponsiveUtils.isDesktop()) || (!lastModeDesktop && ResponsiveUtils.isDesktop())) {
			refresh();
		}
		lastModeDesktop = ResponsiveUtils.isDesktop();
	}

	protected void setCaptionsAndComponents(List<CaptionAndComponent> captionsAndDisplayComponents) {
		this.captionsAndDisplayComponents = captionsAndDisplayComponents;
		refresh();
	}

	private void refresh() {
		if (mainLayout.iterator().hasNext()) {
			mainLayout.removeAllComponents();
		}
		if (tabSheet != null) {
			tabs.clear();
			tabSheet.removeAllComponents();
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
		// mainLayout.setSizeUndefined();
		mainLayout.setSizeFull();
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
			Panel panel = tabs.get(tabCaption);
			if (panel == null) {
				layout = new VerticalLayout();
				layout.addStyleName("base-display-tab-layout");
				layout.setWidth("100%");

				panel = new Panel(layout);
				panel.addStyleName(ValoTheme.PANEL_BORDERLESS);
				panel.addStyleName(ValoTheme.PANEL_SCROLL_INDICATOR);
				panel.addStyleName("base-display-tab-panel");
				panel.setWidth("100%");
				panel.setHeight((Page.getCurrent().getBrowserWindowHeight() -250) + "px");
				tabs.put(tabCaption, panel);
				addTab(tabSheet, panel, tabCaption, tabIcon);

				AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();

				List<String> tabCaptionToIgnore = appLayerFactory.getExtensions().
						forCollection(ConstellioUI.getCurrentSessionContext()
								.getCurrentCollection()).getTabSheetCaptionToHideInDisplayAndForm();

				if (tabCaptionToIgnore.contains(tabCaption)) {
					Tab tab = tabSheet.getTab(panel);
					tab.setVisible(false);
					tab.setEnabled(false);
				}
			} else {
				layout = (VerticalLayout) panel.getContent();
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
			if (ResponsiveUtils.isDesktop()) {
				captionLabel.setWidth("300px");
				I18NHorizontalLayout captionAndComponentLayout = new I18NHorizontalLayout();
				captionAndComponentLayout.addStyleName("display-caption-and-component");

				layout.addComponent(captionAndComponentLayout);
				captionAndComponentLayout.addComponents(captionLabel, displayComponent);
			} else {
				layout.addComponents(captionLabel, displayComponent);
			}
		}
	}

	protected boolean isCaptionAndDisplayComponentWidthUndefined() {
		return false;
	}
	
	protected boolean isUseTabsheet() {
		return useTabSheet;
	}

	public void addSelectedTabChangeListener(SelectedTabChangeListener listener) {
		tabSheet.addSelectedTabChangeListener(listener);
	}

	public void removeSelectedTabChangeListener(SelectedTabChangeListener listener) {
		tabSheet.removeSelectedTabChangeListener(listener);
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
