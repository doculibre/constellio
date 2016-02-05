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

	private static final String COMMENT = "comment";

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
		addContainerProperty(COMMENT, String.class, null);

		setColumnHeader(FILE_NAME, $("ContentVersion.fileName"));
		setColumnHeader(VERSION, $("ContentVersion.version"));
		setColumnHeader(LENGTH, $("ContentVersion.length"));
		setColumnHeader(LAST_MODIFICATION_DATE_TIME, $("ContentVersion.lastModificationDateTime"));
		setColumnHeader(LAST_MODIFIED_BY, $("ContentVersion.lastModifiedBy"));
		setColumnHeader(COMMENT, $("ContentVersion.comment"));

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
			String comment = contentVersion.getComment();
			String version = contentVersion.getVersion();
			long length = contentVersion.getLength();
			Date lastModificationDateTime = contentVersion.getLastModificationDateTime();
			String lastModifiedBy = contentVersion.getLastModifiedBy();

			String lengthCaption = FileUtils.byteCountToDisplaySize(length);
			String lastModificationDateTimeCaption = dateTimeConverter
					.convertToPresentation(lastModificationDateTime, String.class, locale);
			String lastModifiedByCaption = recordCaptionConverter.convertToPresentation(lastModifiedBy, String.class, locale);

			Object itemId = addItem();
			Item item = getItem(itemId);
			item.getItemProperty(FILE_NAME).setValue(new DownloadContentVersionLink(contentVersion, fileName));
			item.getItemProperty(VERSION).setValue(version);
			item.getItemProperty(LENGTH).setValue(lengthCaption);
			item.getItemProperty(LAST_MODIFICATION_DATE_TIME).setValue(lastModificationDateTimeCaption);
			item.getItemProperty(LAST_MODIFIED_BY).setValue(lastModifiedByCaption);
			item.getItemProperty(COMMENT).setValue(comment);
		}
	}

}
