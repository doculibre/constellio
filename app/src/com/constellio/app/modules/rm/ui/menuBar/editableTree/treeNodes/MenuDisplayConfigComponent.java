package com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes;

import com.constellio.app.services.icons.IconService;
import com.constellio.app.ui.pages.base.SessionContext;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface MenuDisplayConfigComponent {
	MenuDisplayConfigComponent getParent();

	String getCode();

	String getI18NKey();

	Map<Locale, String> getCaptions();

	String getIconName();

	List<MenuDisplayConfigComponent> getChildren();

	List<MenuDisplayConfigComponent> getEnabledChildren();

	List<MenuDisplayConfigComponent> getDisabledChildren();

	boolean isEnabled();

	boolean isAlwaysEnabled();

	Class<? extends MenuDisplayConfigComponent> getMainClass();

	SessionContext getSessionContext();

	IconService getIconService();

	MenuDisplayConfigComponent applyModification(MenuDisplayConfigComponent copy);
}
