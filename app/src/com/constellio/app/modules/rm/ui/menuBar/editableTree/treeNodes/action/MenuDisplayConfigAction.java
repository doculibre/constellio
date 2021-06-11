package com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.action;

import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigComponent;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigComponentBase;
import com.constellio.app.services.icons.IconService;
import com.constellio.app.ui.pages.base.SessionContext;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class MenuDisplayConfigAction extends MenuDisplayConfigComponentBase {

	public MenuDisplayConfigAction(String code, String i18nKey, SessionContext sessionContext,
								   IconService iconService) {
		this(new MenuDisplayConfigComponentBase(MenuDisplayConfigAction.class, code, sessionContext, iconService) {
			@Override
			public Map<Locale, String> getCaptions() {
				HashMap<Locale, String> captions = new HashMap<>();
				captions.put(sessionContext.getCurrentLocale(), $(i18nKey));

				return captions;
			}

			@Override
			public String getI18NKey() {
				return i18nKey;
			}
		});
	}

	public MenuDisplayConfigAction(MenuDisplayConfigComponent copy) {
		super(new MenuDisplayConfigComponentBase(copy));
	}

	@Override
	public MenuDisplayConfigComponent applyModification(MenuDisplayConfigComponent copy) {
		return new MenuDisplayConfigAction(copy);
	}

}
