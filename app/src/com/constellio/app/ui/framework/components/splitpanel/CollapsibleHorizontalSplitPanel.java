package com.constellio.app.ui.framework.components.splitpanel;

import com.constellio.app.ui.util.ResponsiveUtils;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;

import javax.servlet.http.Cookie;

public class CollapsibleHorizontalSplitPanel extends HorizontalSplitPanel {
	
	private static final String COOKIE_PREFIX = "collapsible-horizontal-splitpanel-show-";
	
	private static final String STYLE_NAME = "collapsible-horizontal-splitpanel";

	private static final String STYLE_NAME_NOT_RESIZABLE = STYLE_NAME + "-not-resizable";
	
	private static final String STYLE_NAME_SECOND_VISIBLE = STYLE_NAME + "-second-visible";
	
	private static final String STYLE_NAME_SECOND_HIDDEN = STYLE_NAME + "-second-hidden";
	
	private String panelId;
	
	private boolean secondComponentVisible = true;
	
	private Component firstComponent;
	
	private Component secondComponent;

	private HorizontalSplitLayoutContent firstComponentContainer;

	private HorizontalSplitLayoutContent secondComponentContainer;

	private float firstComponentWidth = -1;

	private Unit firstComponentWidthUnit = Unit.PERCENTAGE;
	
	private float secondComponentWidth = 50;
	
	private Unit secondComponentWidthUnit = Unit.PERCENTAGE;

	private float firstComponentHeight = -1;

	private Unit firstComponentHeightUnit = Unit.PERCENTAGE;

	private float secondComponentHeight = -1;

	private Unit secondComponentHeightUnit = Unit.PERCENTAGE;

	private boolean resizable;
	
	public CollapsibleHorizontalSplitPanel(String panelId) {
		this.panelId = panelId;
		buildUI();
	}
	
	private void buildUI() {
		addStyleName(STYLE_NAME);
		addStyleName(STYLE_NAME_SECOND_VISIBLE);
		
		setSizeFull();

		setLocked(false);
		resizable = computeResizable();
		if (!resizable) {
			addStyleName(STYLE_NAME_NOT_RESIZABLE);
		}
		
		addSplitterClickListener(new SplitterClickListener() {
			@Override
			public void splitterClick(SplitterClickEvent event) {
				toggleSecondButtonClicked();
			}
		});
		
		boolean secondComponentVisibleFromCookie = getCookieValue();
		setSecondComponentVisible(secondComponentVisibleFromCookie);
	}

	private boolean computeResizable() {
		return ResponsiveUtils.isDesktop() && !Page.getCurrent().getWebBrowser().isIE();
	}

	public float getFirstComponentWidth() {
		return firstComponentWidth;
	}

	public Unit getFirstComponentWidthUnit() {
		return firstComponentWidthUnit;
	}

	public void setFirstComponentWidth(float width, Unit unit) {
		this.firstComponentWidth = width;
		this.firstComponentWidthUnit = unit;
		if (firstComponentContainer != null) {
			firstComponentContainer.setWidth(width, unit);
		}
	}

	public float getFirstComponentHeight() {
		return firstComponentHeight;
	}

	public Unit getFirstComponentHeightUnit() {
		return firstComponentHeightUnit;
	}

	public void setFirstComponentHeight(float height, Unit unit) {
		this.firstComponentHeight = height;
		this.firstComponentHeightUnit = unit;
		if (firstComponentContainer != null) {
			firstComponentContainer.setHeight(height, unit);
		}
	}

	public Component getFirstComponentContainer() {
		return this.firstComponentContainer;
	}

	public float getSecondComponentWidth() {
		return secondComponentWidth;
	}

	public Unit getSecondComponentWidthUnit() {
		return secondComponentWidthUnit;
	}

	public void setSecondComponentWidth(float width, Unit unit) {
		this.secondComponentWidth = width;
		this.secondComponentWidthUnit = unit;
		if (secondComponentVisible) {
			setSplitPosition(secondComponentWidth, secondComponentWidthUnit, true);
		}
		if (secondComponentContainer != null) {
			//			secondComponentContainer.setWidth(width, unit);
			secondComponentContainer.setWidth("100%");
		}
	}

	public float getSecondComponentHeight() {
		return secondComponentHeight;
	}

	public Unit getSecondComponentHeightUnit() {
		return secondComponentHeightUnit;
	}

	public void setSecondComponentHeight(float height, Unit unit) {
		this.secondComponentHeight = height;
		this.secondComponentHeightUnit = unit;
		if (secondComponentContainer != null) {
			secondComponentContainer.setHeight(height, unit);
		}
	}

	public boolean isSecondComponentVisible() {
		return secondComponentVisible;
	}
	
	public void setSecondComponentVisible(boolean secondComponentVisible) {
		this.secondComponentVisible = secondComponentVisible;
		
		if (secondComponent != null) {
			secondComponent.setVisible(secondComponentVisible);
		}
		if (secondComponentVisible) {
			setSplitPosition(secondComponentWidth, secondComponentWidthUnit, true);
			//			if (!resizable) {
			//				setMinSplitPosition(secondComponentWidth, secondComponentWidthUnit);
			//				setMaxSplitPosition(secondComponentWidth, secondComponentWidthUnit);
			//			}
			removeStyleName(STYLE_NAME_SECOND_HIDDEN);
			addStyleName(STYLE_NAME_SECOND_VISIBLE);
		} else {
			setSplitPosition(100, Unit.PERCENTAGE);
			//			if (!resizable) {
			//				setMinSplitPosition(100, Unit.PERCENTAGE);
			//				setMaxSplitPosition(100, Unit.PERCENTAGE);
			//			}
			removeStyleName(STYLE_NAME_SECOND_VISIBLE);
			addStyleName(STYLE_NAME_SECOND_HIDDEN);
		}
		
		setCookie(secondComponentVisible);
	}

	protected void toggleSecondButtonClicked() {
		setSecondComponentVisible(!secondComponentVisible);
		onSecondComponentVisibilyChanged(secondComponentVisible);
	}
	
	protected void onSecondComponentVisibilyChanged(boolean secondComponentVisible) {
	}
	
	private String getCookieName() {
		return COOKIE_PREFIX + panelId;
	}

	private boolean getCookieValue() {
		boolean cookieValue = true;
		String cookieName = getCookieName();
		Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(cookieName)) {
				cookieValue = !"false".equals(cookie.getValue());
				break;
			}
		}
		return cookieValue;
	}

	private void setCookie(Boolean cookieValue) {
		String cookieName = getCookieName();
		Cookie cookie = new Cookie(cookieName, "" + cookieValue);
		if (cookieValue != null) {
			cookie.setMaxAge(Integer.MAX_VALUE);
		} else {
			// Delete the cookie
			cookie.setMaxAge(0);
		}

		// Set the cookie path.
		cookie.setPath(VaadinService.getCurrentRequest().getContextPath());

		// Save cookie
		VaadinService.getCurrentResponse().addCookie(cookie);
	}
	
	public Component getRealFirstComponent() {
		return firstComponent;
	}

	public Component getRealSecondComponent() {
		return secondComponent;
	}

	@Override
	public void setFirstComponent(Component c) {
		this.firstComponent = c;

		float width;
		Unit widthUnit;
		float height;
		Unit heightUnit;
		if (firstComponentWidth != -1) {
			width = firstComponentWidth;
			widthUnit = firstComponentWidthUnit;
		} else {
			width = c.getWidth();
			widthUnit = c.getWidthUnits();
		}
		if (firstComponentHeight != -1) {
			height = firstComponentHeight;
			heightUnit = firstComponentHeightUnit;
		} else {
			height = c.getHeight();
			heightUnit = c.getHeightUnits();
		}
		super.setFirstComponent(firstComponentContainer = new HorizontalSplitLayoutContent(c, false, width, widthUnit, height, heightUnit));
	}

	@Override
	public void setSecondComponent(Component c) {
		this.secondComponent = c;

		float width;
		Unit widthUnit;
		float height;
		Unit heightUnit;
		if (secondComponentWidth != -1) {
			width = secondComponentWidth;
			widthUnit = secondComponentWidthUnit;
		} else {
			width = c.getWidth();
			widthUnit = c.getWidthUnits();
		}
		if (secondComponentHeight != -1) {
			height = secondComponentHeight;
			heightUnit = secondComponentHeightUnit;
		} else {
			height = c.getHeight();
			heightUnit = c.getHeightUnits();
		}
		super.setSecondComponent(secondComponentContainer = new HorizontalSplitLayoutContent(c, true, width, widthUnit, height, heightUnit));
	}
	
	private static class HorizontalSplitLayoutContent extends HorizontalLayout {

		public HorizontalSplitLayoutContent(Component component, boolean second, float width, Unit widthUnits,
											float height, Unit heightUnits) {
			addStyleName("horizontal-split-layout-content");

			if (width != -1) {
				setWidth(width, widthUnits);
			} else {
				setWidth("100%");
			}
			if (height != -1) {
				setHeight(height, heightUnits);
			} else {
				setHeight("100%");
			}
			
			Label spacer = new Label();
			spacer.setWidth("4px");
			if (second) {
				addStyleName("horizontal-split-layout-content-second");
				spacer.addStyleName(STYLE_NAME + "spacer-second");
				addComponents(spacer, component);
			} else {
				addStyleName("horizontal-split-layout-content-first");
				spacer.addStyleName(STYLE_NAME + "spacer-first");
				addComponents(component, spacer);
			}
			setExpandRatio(component, 1);
		}
		
	}

}
