package com.constellio.app.ui.framework.components.breadcrumb;

import java.io.Serializable;

public interface BreadcrumbItem extends Serializable {
	
	String getLabel();
	
	boolean isEnabled();

}
