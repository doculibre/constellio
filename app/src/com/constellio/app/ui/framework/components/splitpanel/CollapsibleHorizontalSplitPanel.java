package com.constellio.app.ui.framework.components.splitpanel;

import com.vaadin.server.VaadinService;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;

import javax.servlet.http.Cookie;

public class CollapsibleHorizontalSplitPanel extends HorizontalSplitPanel {
	
	private static final String COOKIE_PREFIX = "collapsible-horizontal-splitpanel-show-";
	
	private static final String STYLE_NAME = "collapsible-horizontal-splitpanel";
	
	private static final String STYLE_NAME_SECOND_VISIBLE = STYLE_NAME + "-second-visible";
	
	private static final String STYLE_NAME_SECOND_HIDDEN = STYLE_NAME + "-second-hidden";
	
	private String panelId;
	
	private boolean secondComponentVisible = true;
	
	private Component firstComponent;
	
	private Component secondComponent;
	
	private float secondComponentWidth = 50;
	
	private Unit secondComponentWidthUnit = Unit.PERCENTAGE;
	
	public CollapsibleHorizontalSplitPanel(String panelId) {
		this.panelId = panelId;
		buildUI();
	}
	
	private void buildUI() {
		addStyleName(STYLE_NAME);
		addStyleName(STYLE_NAME_SECOND_VISIBLE);
		
		setSizeFull();
		setLocked(false);
		
		addSplitterClickListener(new SplitterClickListener() {
			@Override
			public void splitterClick(SplitterClickEvent event) {
				toggleSecondButtonClicked();
			}
		});
		
		boolean secondComponentVisibleFromCookie = getCookieValue();
		setSecondComponentVisible(secondComponentVisibleFromCookie);
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
			removeStyleName(STYLE_NAME_SECOND_HIDDEN);
			addStyleName(STYLE_NAME_SECOND_VISIBLE);
		} else {
			setSplitPosition(100, Unit.PERCENTAGE);
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
		super.setFirstComponent(new HorizontalSplitLayoutContent(c, false));
	}

	@Override
	public void setSecondComponent(Component c) {
		this.secondComponent = c;
		super.setSecondComponent(new HorizontalSplitLayoutContent(c, true));
	}
	
	private static class HorizontalSplitLayoutContent extends HorizontalLayout {
		
		public HorizontalSplitLayoutContent(Component component, boolean second) {
			setSizeFull();
			
			Label spacer = new Label();
			spacer.setWidth("4px");
			if (second) {
				spacer.addStyleName(STYLE_NAME + "spacer-second");
				addComponents(spacer, component);
			} else {
				spacer.addStyleName(STYLE_NAME + "spacer-first");
				addComponents(component, spacer);
			}
			setExpandRatio(component, 1);
		}
		
	}

}
