package com.constellio.app.ui.framework.components.breadcrumb;

import static com.constellio.app.ui.i18n.i18n.$;
import java.util.List;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.MenuViewGroup;

public class TitleBreadcrumbTrail extends BaseBreadcrumbTrail {
	
	public TitleBreadcrumbTrail(final BaseView view, final String viewTitle) {
		String collectionCode = ConstellioUI.getCurrentSessionContext().getCurrentCollection();
		if (StringUtils.isNotBlank(collectionCode)) {
			addItem(new CollectionBreadcrumbItem(collectionCode)); 
		}
		
		String viewGroupLabel = null;
		List<Class<?>> implementedInterfaces = ClassUtils.getAllInterfaces(view.getClass());
		for (Class<?> implementedInterface : implementedInterfaces) {
			if (!MenuViewGroup.class.equals(implementedInterface) && MenuViewGroup.class.isAssignableFrom(implementedInterface)) {
				String className = implementedInterface.getSimpleName();
				String key = "ViewGroup." + className;
				viewGroupLabel = $(key);
				if (key.equals(viewGroupLabel)) {
					viewGroupLabel = null;
				} else {
					break;
				}
			}
		}
		
		if (StringUtils.isNotBlank(viewGroupLabel)) {
			addItem(new DefaultBreadcrumbItem(viewGroupLabel, false)); 
		}
		if (StringUtils.isNotBlank(viewTitle) && (StringUtils.isBlank(viewGroupLabel) || !viewGroupLabel.equals(viewTitle))) {
			addItem(new DefaultBreadcrumbItem(viewTitle, false)); 
		}
	}

	@Override
	protected void itemClick(BreadcrumbItem item) {
	}

}
