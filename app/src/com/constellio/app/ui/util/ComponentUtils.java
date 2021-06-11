package com.constellio.app.ui.util;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;

import java.util.Iterator;

/**
 * Component Utils class
 *
 * @author Elie
 */
public class ComponentUtils {

	public static Component setCaptionAsHtmlRecursive(Component component, boolean captionAsHtml) {
		if (component instanceof AbstractComponent) {
			((AbstractComponent) component).setCaptionAsHtml(captionAsHtml);
		}
		if (component instanceof HasComponents) {
			HasComponents hasComponents = (HasComponents) component;
			Iterator<Component> iterator = hasComponents.iterator();
			while (iterator.hasNext()) {
				setCaptionAsHtmlRecursive(iterator.next(), captionAsHtml);
			}
		}
		return component;
	}

}
