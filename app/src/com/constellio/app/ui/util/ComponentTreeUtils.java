package com.constellio.app.ui.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.SingleComponentContainer;

/**
 * Adapted from https://vaadin.com/forum/#!/thread/217291/217290
 *
 * @author Vincent
 */
public class ComponentTreeUtils {

	@SuppressWarnings("unchecked")
	public static <T> T findParent(Component component, Class<T> clazz) {
		T match = null;
		Component currentParent = component;
		while (match == null && currentParent != null) {
			if (clazz.isAssignableFrom(currentParent.getClass())) {
				match = (T) currentParent;
				break;
			}
			currentParent = currentParent.getParent();
		}
		return match;
	}

	public static <T> T getFirstChild(Component component, Class<T> clazz) {
		List<T> matches = getChildren(component, clazz);
		return !matches.isEmpty() ? matches.get(0) : null;
	}

	public static <T> List<T> getChildren(Component component, Class<T> clazz) {
		final List<T> matches = new ArrayList<T>();
		traverse(component, new FindByClass<T>(clazz, matches));
		return matches;
	}
	
	public static boolean removeFromParent(Component component) {
		boolean removed;
		
		if (component != null) {
			HasComponents parent = component.getParent();
			if (parent != null) {
				removed = false;
				for (Iterator<Component> it = parent.iterator(); it.hasNext();) {
					if (it.next().equals(component)) {
						it.remove();
						removed = true;
						break;
					}
				}
			} else {
				removed = false;
			}
		} else {
			removed = false;
		}
		
		return removed;
	}

	/**
	 * Traverse all components under a given component.
	 *
	 * @param component
	 * @param invocation
	 */
	public static void traverse(Component component, MethodInvoker invocation) {
		traverse("Self", component, invocation);
	}

	private static void traverse(String theCaller, Component component, MethodInvoker invocation) {
		// Self
		invocation.invokeMethod(theCaller, component);

		// Content of Panel and children of Window
		if (component instanceof SingleComponentContainer) {
			final SingleComponentContainer singleComponentContainer = (SingleComponentContainer) component;
			if (singleComponentContainer.getContent() != null) {
				traverse(singleComponentContainer.toString(), singleComponentContainer.getContent(), invocation);
			}
		} else if (component instanceof CustomComponent) {
			final CustomComponent customComponent = (CustomComponent) component;
			// All the contained components
			final Iterator<Component> subComponents = customComponent.iterator();
			while (subComponents.hasNext()) {
				Component subComponent = subComponents.next();
				traverse(component.toString(), subComponent, invocation);
			}
		} else if (component instanceof ComponentContainer) {
			ComponentContainer componentContainer = (ComponentContainer) component;
			// All the contained components
			final Iterator<Component> subComponents = componentContainer.iterator();
			while (subComponents.hasNext()) {
				Component subComponent = subComponents.next();
				if (subComponent != null) {
					traverse(component.toString(), subComponent, invocation);
				}
			}
		}
	}

	/**
	 * Just invoke any method on a component of the Application graph, just implement the only method
	 * {@link #invokeMethod(String, Object)}.
	 *
	 * @version $Id: ComponentTraverser.java,v 1.11 2010/09/14 19:40:32 tettoni Exp $
	 */
	public static interface MethodInvoker {
		/**
		 * Invoke a method on theTarget.
		 *
		 * @param theCaller Only for logging - not functional
		 * @param theTarget The target object to invoke on.
		 */
		public void invokeMethod(String theCaller, Object theTarget);
	}

	public static class FindByClass<C> implements MethodInvoker {

		private Class<C> clazz;

		private List<C> matches;

		public FindByClass(Class<C> clazz, List<C> matches) {
			this.clazz = clazz;
			this.matches = matches;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void invokeMethod(String theCaller, Object theTarget) {
			if (clazz.isAssignableFrom(theTarget.getClass())) {
				matches.add((C) theTarget);
			}
		}

	}

}
