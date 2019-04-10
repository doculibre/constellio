package com.constellio.app.ui.framework.components.layouts;

import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

public class I18NCssLayout extends CssLayout {

	public static final String STYLE_NAME_PREFIX = "i18n-css-layout-";

	public static final String STYLE_NAME_LEFT_TO_RIGHT = STYLE_NAME_PREFIX + "left-to-right";

	public static final String STYLE_NAME_RIGHT_TO_LEFT = STYLE_NAME_PREFIX + "right-to-left";

	public I18NCssLayout() {
		init();
	}

	public I18NCssLayout(Component... children) {
		super();
		init();
		addComponents(children);
	}

	private void init() {
		if (isRightToLeft()) {
			addStyleName(STYLE_NAME_RIGHT_TO_LEFT);
		} else {
			addStyleName(STYLE_NAME_LEFT_TO_RIGHT);
		}
	}

	private int adjustIndex(int index) {
		//		if (true) return index;
		int result;
		if (isRightToLeft()) {
			int componentCount = getComponentCount();
			if (componentCount > 0) {
				result = componentCount - index;
			} else {
				result = index;
			}
		} else {
			result = index;
		}
		return result;
	}

	@Override
	public void addComponent(Component c) {
		//		super.addComponent(c);
		int index = getComponentCount();
		addComponent(c, index);
	}

	@Override
	public void addComponentAsFirst(Component c) {
		//		super.addComponentAsFirst(c);
		if (equals(c.getParent())) {
			removeComponent(c);
		}
		addComponent(c, 0);
	}

	@Override
	public void addComponent(Component c, int index) {
		index = adjustIndex(index);
		super.addComponent(c, index);
	}

}
