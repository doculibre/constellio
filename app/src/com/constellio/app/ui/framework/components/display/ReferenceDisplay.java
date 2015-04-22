/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.framework.components.display;

import java.util.List;

import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedListener.ComponentListener;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnComponentEvent;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenu;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenuHandler;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.converters.RecordVOToCaptionConverter;
import com.constellio.app.ui.framework.navigation.RecordNavigationHandler;
import com.constellio.app.ui.util.FileIconUtils;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;

public class ReferenceDisplay extends Button {
	
	public static final String STYLE_NAME = "reference-display";

	private RecordVO recordVO;

	private String recordId;
	
	private RecordContextMenu contextMenu;

	public ReferenceDisplay(RecordVO recordVO) {
		this.recordVO = recordVO;
		String caption = new RecordVOToCaptionConverter().convertToPresentation(recordVO, String.class, getLocale());
		Resource icon = FileIconUtils.getIcon(recordVO);
		if (icon != null) {
			setIcon(icon);
		}
		setCaption(caption);
		init();
	}

	public ReferenceDisplay(String recordId) {
		this.recordId = recordId;
		String caption = new RecordIdToCaptionConverter().convertToPresentation(recordId, String.class, getLocale());
		Resource icon = FileIconUtils.getIconForRecordId(recordId);
		if (icon != null) {
			setIcon(icon);
		}
		setCaption(caption);
		init();
	}

	private void init() {
		setSizeFull();
		addStyleName(STYLE_NAME);
		addStyleName(ValoTheme.BUTTON_LINK);
		setEnabled(false);
		addClickListener();
//		addContextMenu();
	}
	
	protected void addClickListener() {
		ClickListener clickListener = null;
		List<RecordNavigationHandler> recordNavigationHandlers = ConstellioUI.getCurrent().getRecordNavigationHandlers();
		for (final RecordNavigationHandler recordNavigationHandler : recordNavigationHandlers) {
			if (recordId != null && recordNavigationHandler.isViewForRecordId(recordId)) {
				clickListener = new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						recordNavigationHandler.navigateToView(recordId);
					}
				};
				break;
			} else if (recordVO != null && recordNavigationHandler.isView(recordVO)) {
				clickListener = new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						recordNavigationHandler.navigateToView(recordVO);
					}
				};
				break;
			}
		}
		if (clickListener != null) {
			addClickListener(clickListener);
		}
	}
	
	protected void addContextMenu() {
		List<RecordContextMenuHandler> recordContextMenuHandlers = ConstellioUI.getCurrent().getRecordContextMenuHandlers();
		for (final RecordContextMenuHandler recordContextMenuHandler : recordContextMenuHandlers) {
			if (recordId != null && recordContextMenuHandler.isContextMenuForRecordId(recordId)) {
				contextMenu = recordContextMenuHandler.getForRecordId(recordId);
				break;
			} else if (recordVO != null && recordContextMenuHandler.isContextMenu(recordVO)) {
				contextMenu = recordContextMenuHandler.get(recordVO);
				break;
			}
		}
		if (contextMenu != null) {
			contextMenu.setAsContextMenuOf(this);
			contextMenu.addContextMenuComponentListener(new ComponentListener() {
				@Override
				public void onContextMenuOpenFromComponent(ContextMenuOpenedOnComponentEvent event) {
					if (recordId != null) {
						contextMenu.openFor(recordId);
					} else if (recordVO != null) {
						contextMenu.openFor(recordVO);
					}
				}
			});
		}
	}

	@Override
	public void addClickListener(ClickListener listener) {
		setEnabled(true);
		super.addClickListener(listener);
	}

}
