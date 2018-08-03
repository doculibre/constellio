package com.constellio.app.ui.framework.components.display;

import org.apache.commons.lang.StringUtils;
import org.vaadin.peter.contextmenu.ContextMenu;

import java.net.URI;

import static com.constellio.app.ui.i18n.i18n.$;

public class LinkReferenceDisplay extends ReferenceDisplay {
	private String path;

	public LinkReferenceDisplay(String path, String recordId) {
		super(recordId);

		this.path = path;

		addContextMenu();
	}

	@Override
	protected void addContextMenu() {
		ContextMenu contextMenu = new ContextMenu();

		ContextMenu.ContextMenuItem openDocumentItem = contextMenu.addItem($("LinkReferenceDisplay.menu.openLinkBlank"));
		openDocumentItem.addItemClickListener(new ContextMenu.ContextMenuItemClickListener() {
			@Override
			public void contextMenuItemClicked(ContextMenu.ContextMenuItemClickEvent contextMenuItemClickEvent) {
				URI location = getUI().getPage().getLocation();

				StringBuilder url = new StringBuilder();
				url.append(location.getScheme());
				url.append(":");
				url.append(location.getSchemeSpecificPart());
				url.append("#!" + StringUtils.trimToEmpty(getPath()) + "/" + getRecordId());

				getUI().getPage().open(url.toString(), "_blank");
			}
		});

		contextMenu.setAsContextMenuOf(this);
	}

	public String getPath() {
		return path;
	}
}
