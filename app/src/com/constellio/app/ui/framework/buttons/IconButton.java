package com.constellio.app.ui.framework.buttons;

import com.constellio.app.ui.framework.components.mouseover.NiceTitle;
import com.vaadin.server.Resource;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("serial")
public abstract class IconButton extends BaseButton {

	public IconButton(Resource iconResource, String caption) {
		this(iconResource, caption, iconResource != null);
	}

	public IconButton(Resource iconResource, String caption, boolean iconOnly) {
		this(iconResource, caption, iconOnly, true);
	}

	public IconButton(Resource iconResource, String caption, boolean iconOnly, boolean borderless) {
		super(caption);
		setIcon(iconResource);
		if (borderless) {
			addStyleName(ValoTheme.BUTTON_BORDERLESS);
		}
		setCaption(caption);

		if (iconOnly) {
			addStyleName(ValoTheme.BUTTON_ICON_ONLY);
			if (StringUtils.isNotBlank(caption)) {
				setIconAlternateText(caption);
				addExtension(new NiceTitle(caption));
			}
		}
	}

	protected boolean isBorderless() {
		return true;
	}

}
