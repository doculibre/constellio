package com.constellio.app.ui.framework.components.layouts;

import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

public class I18NHorizontalLayout extends HorizontalLayout {
	
	public static final String STYLE_NAME_PREFIX = "i18n-horizontal-layout-";
	
	public static final String STYLE_NAME_LEFT_TO_RIGHT = STYLE_NAME_PREFIX + "left-to-right";
	
	public static final String STYLE_NAME_RIGHT_TO_LEFT = STYLE_NAME_PREFIX + "right-to-left";
	
	public I18NHorizontalLayout() {
		init();
	}

	public I18NHorizontalLayout(Component... children) {
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
		Alignment defaultAlignment = adjustAlignment(ALIGNMENT_DEFAULT);
		setDefaultComponentAlignment(defaultAlignment);
	}
	
	private Alignment adjustAlignment(Alignment alignment) {
		Alignment result;
		if (isRightToLeft()) {
			if (Alignment.BOTTOM_LEFT.equals(alignment)) {
				result = Alignment.BOTTOM_RIGHT;
			} else if (Alignment.BOTTOM_RIGHT.equals(alignment)) {
				result = Alignment.BOTTOM_LEFT;
			} else if (Alignment.MIDDLE_LEFT.equals(alignment)) {
				result = Alignment.MIDDLE_RIGHT;
			} else if (Alignment.MIDDLE_RIGHT.equals(alignment)) {
				result = Alignment.MIDDLE_LEFT;
			} else if (Alignment.TOP_LEFT.equals(alignment)) {
				result = Alignment.TOP_RIGHT;
			} else if (Alignment.TOP_RIGHT.equals(alignment)) {
				result = Alignment.TOP_LEFT;
			} else {
				result = alignment;
			}
		} else {
			result = alignment;
		}
		return result;
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

	@Override
	public void setComponentAlignment(Component childComponent, Alignment alignment) {
		alignment = adjustAlignment(alignment);
		super.setComponentAlignment(childComponent, alignment);
	}

	@Override
	public void setDefaultComponentAlignment(Alignment defaultAlignment) {
		defaultAlignment = adjustAlignment(defaultAlignment);
		super.setDefaultComponentAlignment(defaultAlignment);
	}

}
