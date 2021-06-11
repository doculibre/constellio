package com.constellio.app.ui.framework.components;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.buttons.toggle.Toggleable;
import com.constellio.app.ui.framework.buttons.toggle.ToggleableComponent;
import com.constellio.app.ui.framework.buttons.toggle.ToggleableComponent.ToggleCallback;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.vaadin.event.UIEvents.PollListener;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.UUID;
import java.util.function.Consumer;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class ExpandableDisplay extends VerticalLayout implements Toggleable {
	public enum ExpansionControlsLocation {
		BeforeMainContent,
		AfterMainContent,
		NotVisible
	}

	public static final String CSS_STYLE_NAME = "expandable-display";
	public static final String CSS_MAIN_COMPONENT = CSS_STYLE_NAME + "-main-component";
	public static final String CSS_EXPANSION_CONTROLS = CSS_STYLE_NAME + "-expansion-controls";

	public static final int MAX_HEIGHT_BEFORE_NEEDS_TO_EXPAND_DEFAULT = 200;
	public static final ExpansionControlsLocation CONTROLS_LOCATION_DEFAULT = ExpansionControlsLocation.AfterMainContent;

	private int maxHeightBeforeNeedsToExpand;
	private Layout mainComponentLayout;
	private Component outerBoundingBox;

	private Component expansionControls;
	private ToggleableComponent expansionControlsToggleableComponent;

	private ExpansionControlsLocation expansionControlsLocation;

	public ExpandableDisplay() {
		this(MAX_HEIGHT_BEFORE_NEEDS_TO_EXPAND_DEFAULT, CONTROLS_LOCATION_DEFAULT);
	}

	public ExpandableDisplay(int maxHeightBeforeNeedsToExpand) {
		this(maxHeightBeforeNeedsToExpand, CONTROLS_LOCATION_DEFAULT);
	}

	public ExpandableDisplay(ExpansionControlsLocation expansionControlsLocation) {
		this(MAX_HEIGHT_BEFORE_NEEDS_TO_EXPAND_DEFAULT, expansionControlsLocation);
	}

	public ExpandableDisplay(int maxHeightBeforeNeedsToExpand, ExpansionControlsLocation expansionControlsLocation) {
		setMaxHeightBeforeNeedsToExpand(maxHeightBeforeNeedsToExpand);
		setExpansionControlsLocation(expansionControlsLocation);

		setStyleName(CSS_STYLE_NAME);

		expansionControlsToggleableComponent = new ToggleableComponent(this::buildShowMoreComponent, this::buildShowLessComponent);
	}

	@Override
	public void attach() {
		super.attach();

		outerBoundingBox = buildMainComponentOuterBoundingBox(buildMainComponent());
		mainComponentLayout = buildMainComponentExpandableLayout(outerBoundingBox);
		addComponent(mainComponentLayout);

		expansionControls = buildExpansionControls();

		Page.getCurrent().addBrowserWindowResizeListener(this::browserWindowResized);
		computeResponsive();
	}

	@Override
	public void detach() {
		Page.getCurrent().removeBrowserWindowResizeListener(this::browserWindowResized);
		super.detach();
	}

	public abstract Component buildMainComponent();

	private Component buildMainComponentOuterBoundingBox(Component mainComponent) {
		Panel mainComponentOuterBoundingBox = new Panel();

		mainComponentOuterBoundingBox.setContent(mainComponent);
		mainComponentOuterBoundingBox.setStyleName(ValoTheme.PANEL_BORDERLESS);//

		return mainComponentOuterBoundingBox;
	}

	private Layout buildMainComponentExpandableLayout(Component mainComponentDecorator) {
		VerticalLayout mainComponentExpandableLayout = new VerticalLayout();

		mainComponentExpandableLayout.setStyleName(CSS_MAIN_COMPONENT);

		mainComponentExpandableLayout.addComponents(mainComponentDecorator);

		return mainComponentExpandableLayout;
	}

	private Component buildExpansionControls() {

		I18NHorizontalLayout layout = new I18NHorizontalLayout();

		layout.addComponents(expansionControlsToggleableComponent);

		layout.setStyleName(CSS_EXPANSION_CONTROLS);

		return layout;
	}

	private Component buildShowMoreComponent(ToggleCallback toggleCallback) {
		Button button = new Button(getShowMoreCaption());

		button.setStyleName(ValoTheme.BUTTON_LINK);

		button.addClickListener(event -> {
			expand();
			toggleCallback.toggle();
		});

		return button;
	}

	private Component buildShowLessComponent(ToggleCallback toggleCallback) {
		Button button = new Button(getShowLessCaption());

		button.setStyleName(ValoTheme.BUTTON_LINK);

		button.addClickListener(event -> {
			collapse();
			toggleCallback.toggle();
		});

		return button;
	}

	private void collapse() {
		mainComponentLayout.setHeight(getMaxHeightBeforeNeedsToExpand(), Unit.PIXELS);
	}

	private void expand() {
		mainComponentLayout.setHeight(100, Unit.PERCENTAGE);
	}

	protected void computeResponsive() {
		if (isAttached()) {
			getComponentComputedHeightInBrowser(outerBoundingBox, mainComponentHeightInBrowser -> {

				if (mainComponentHeightInBrowser > getMaxHeightBeforeNeedsToExpand()) {
					if (!components.contains(expansionControls)) {
						addExpansionControls();

						if (!expansionControlsToggleableComponent.isToggled()) {
							collapse();
						} else {
							expand();
						}
					}
				} else {
					if (components.contains(expansionControls)) {
						removeComponent(expansionControls);
						expand();
					}
				}
			});
		}
	}

	private void addExpansionControls() {
		if (expansionControls == null) {
			return;
		}

		components.remove(expansionControls);

		if (ExpansionControlsLocation.BeforeMainContent.equals(getExpansionControlsLocation())) {
			addComponent(expansionControls, 0);
		} else if (ExpansionControlsLocation.AfterMainContent.equals(getExpansionControlsLocation())) {
			addComponent(expansionControls);
		}
	}

	private void browserWindowResized(BrowserWindowResizeEvent event) {
		computeResponsive();
	}


	private void getComponentComputedHeightInBrowser(Component component, Consumer<Integer> onHeightObtained) {
		final JavaScript jsEngine = JavaScript.getCurrent();


		String originalComponentId = component.getId();

		String componentId = originalComponentId;
		if (componentId == null) {
			componentId = "temp_id_" + UUID.randomUUID().toString().replace("-", "_");
			component.setId(componentId);
		}

		final String functionId = componentId + "_getMainContentHeightInBrowser";
		jsEngine.addFunction(functionId, arguments -> {
			jsEngine.removeFunction(functionId);

			component.setId(originalComponentId);

			onHeightObtained.accept((int) arguments.getNumber(0));
		});

		jsEngine.execute(
				"var componentDiv =  document.getElementById('" + componentId + "');" +
				"if(componentDiv){" +
				"  var componentHeight =  constellio_getHeight(componentDiv);" +
				functionId + "(componentHeight);" +
				"}"
		);
	}

	public void removePollListener(PollListener listener) {
		ConstellioUI.getCurrent().removePollListener(listener);
	}

	public void setMaxHeightBeforeNeedsToExpand(int maxHeightBeforeNeedsToExpand) {
		if (this.maxHeightBeforeNeedsToExpand != maxHeightBeforeNeedsToExpand) {
			this.maxHeightBeforeNeedsToExpand = maxHeightBeforeNeedsToExpand;
			computeResponsive();
		}
	}

	public int getMaxHeightBeforeNeedsToExpand() {
		return maxHeightBeforeNeedsToExpand;
	}

	public String getShowMoreCaption() {
		return $("ExpandableDisplay.expansionControls.showMore.caption");
	}

	public String getShowLessCaption() {
		return $("ExpandableDisplay.expansionControls.showLess.caption");
	}

	public void setExpansionControlsLocation(
			ExpansionControlsLocation expansionControlsLocation) {

		if (this.expansionControlsLocation != expansionControlsLocation) {
			this.expansionControlsLocation = expansionControlsLocation;

			addExpansionControls();
		}
	}

	public ExpansionControlsLocation getExpansionControlsLocation() {
		return expansionControlsLocation;
	}

	@Override
	public void addToggleToToggledViewListener(ToggleToToggledViewListener listener) {
		expansionControlsToggleableComponent.addToggleToToggledViewListener(listener);
	}

	@Override
	public void removeToggleToToggledViewListener(ToggleToToggledViewListener listener) {
		expansionControlsToggleableComponent.removeToggleToToggledViewListener(listener);
	}

	@Override
	public void addToggleToDefaultViewListener(ToggleToDefaultViewListener listener) {
		expansionControlsToggleableComponent.addToggleToDefaultViewListener(listener);
	}

	@Override
	public void removeToggleToDefaultViewListener(ToggleToDefaultViewListener listener) {
		expansionControlsToggleableComponent.removeToggleToDefaultViewListener(listener);
	}

	@Override
	public void addToggledListener(ToggledListener listener) {
		expansionControlsToggleableComponent.addToggledListener(listener);
	}

	@Override
	public void removeToggledListener(ToggledListener listener) {
		expansionControlsToggleableComponent.removeToggledListener(listener);
	}

	@Override
	public void toggle() {
		expansionControlsToggleableComponent.toggle();
	}

	@Override
	public boolean isToggled() {
		return expansionControlsToggleableComponent.isToggled();
	}

	@Override
	public Component getToggledView() {
		return expansionControlsToggleableComponent.getToggledView();
	}

	@Override
	public Component getDefaultView() {
		return expansionControlsToggleableComponent.getDefaultView();
	}
}
