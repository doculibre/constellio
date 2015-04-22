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
package com.constellio.app.ui.framework.components.table;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.components.content.DownloadContentVersionLink;
import com.constellio.app.ui.framework.components.converters.BaseStringToDateTimeConverter;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;

public class ContentVersionVOTable extends Table {
	
	private static final String FILE_NAME = "fileName";
	
	private static final String VERSION = "version";
	
	private static final String LENGTH = "length";
	
	private static final String LAST_MODIFICATION_DATE_TIME = "lastModificationDateTime";
	
	private static final String LAST_MODIFIED_BY = "lastModifiedBy";
	
	private List<ContentVersionVO> contentVersionVOs;
	
	private RecordIdToCaptionConverter recordCaptionConverter = new RecordIdToCaptionConverter();
	
	private BaseStringToDateTimeConverter dateTimeConverter = new BaseStringToDateTimeConverter();
	
	private ButtonsContainer buttonsContainer;
	
	public ContentVersionVOTable() {
		this(new ArrayList<ContentVersionVO>());
	}
	
	public ContentVersionVOTable(List<ContentVersionVO> contentVersions) {
		addContainerProperty(FILE_NAME, DownloadContentVersionLink.class, null);
		addContainerProperty(VERSION, String.class, null);
		addContainerProperty(LENGTH, String.class, null);
		addContainerProperty(LAST_MODIFICATION_DATE_TIME, String.class, null);
		addContainerProperty(LAST_MODIFIED_BY, String.class, null);
		
		setColumnHeader(FILE_NAME, $("ContentVersion.fileName"));
		setColumnHeader(VERSION, $("ContentVersion.version"));
		setColumnHeader(LENGTH, $("ContentVersion.length"));
		setColumnHeader(LAST_MODIFICATION_DATE_TIME, $("ContentVersion.lastModificationDateTime"));
		setColumnHeader(LAST_MODIFIED_BY, $("ContentVersion.lastModifiedBy"));

		setColumnExpandRatio(FILE_NAME, 1);
		setContentVersions(contentVersions);
		
		if (isDeleteColumn()) {
			buttonsContainer = new ButtonsContainer(getContainerDataSource());
			buttonsContainer.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(Object itemId) {
					int indexOfItemId = buttonsContainer.indexOfId(itemId);
					final ContentVersionVO contentVersionVO = contentVersionVOs.get(indexOfItemId);
					DeleteButton deleteButton = new DeleteButton() {
						@Override
						protected void confirmButtonClick(ConfirmDialog dialog) {
							deleteButtonClick(contentVersionVO);
						}
					};
					boolean visible = isDeletePossible(contentVersionVO);
					deleteButton.setVisible(visible);
					return deleteButton;
				}
			});
			setContainerDataSource(buttonsContainer);
			setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
			setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 47);
		}
	} 
	
	protected boolean isDeleteColumn() {
		return false;
	}
	
	protected boolean isDeletePossible(ContentVersionVO contentVersionVO) {
		return false;
	}
	
	protected void deleteButtonClick(ContentVersionVO contentVersionVO) {
		throw new UnsupportedOperationException("Override");
	}
	
	public List<ContentVersionVO> getContentVersions() {
		return contentVersionVOs;
	}
	
	@SuppressWarnings("unchecked")
	public void setContentVersions(List<ContentVersionVO> contentVersions) {
		this.contentVersionVOs = contentVersions;
		removeAllItems();
		Locale locale = getLocale();
		for (ContentVersionVO contentVersion : contentVersions) {
			String fileName = contentVersion.getFileName();
			String version = contentVersion.getVersion();
			long length = contentVersion.getLength();
			Date lastModificationDateTime = contentVersion.getLastModificationDateTime();
			String lastModifiedBy = contentVersion.getLastModifiedBy();

			String lengthCaption = FileUtils.byteCountToDisplaySize(length);
			String lastModificationDateTimeCaption = dateTimeConverter.convertToPresentation(lastModificationDateTime, String.class, locale);
			String lastModifiedByCaption = recordCaptionConverter.convertToPresentation(lastModifiedBy, String.class, locale);
			
			Object itemId = addItem();
			Item item = getItem(itemId);
			item.getItemProperty(FILE_NAME).setValue(new DownloadContentVersionLink(contentVersion, fileName));
			item.getItemProperty(VERSION).setValue(version);
			item.getItemProperty(LENGTH).setValue(lengthCaption);
			item.getItemProperty(LAST_MODIFICATION_DATE_TIME).setValue(lastModificationDateTimeCaption);
			item.getItemProperty(LAST_MODIFIED_BY).setValue(lastModifiedByCaption);
		}
	}
	
}
