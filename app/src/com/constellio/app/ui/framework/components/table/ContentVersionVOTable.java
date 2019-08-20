package com.constellio.app.ui.framework.components.table;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.components.content.DownloadContentVersionLink;
import com.constellio.app.ui.framework.components.converters.BaseStringToDateTimeConverter;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.data.conf.ContentDaoType;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.contents.FileSystemContentDao;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import org.apache.commons.io.FileUtils;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

public class ContentVersionVOTable extends BaseTable {

	private static final String CHECK_BOX = "checkBox";

	private static final String FILE_NAME = "fileName";

	private static final String VERSION = "version";

	private static final String LENGTH = "length";

	private static final String COMMENT = "comment";

	private static final String LAST_MODIFICATION_DATE_TIME = "lastModificationDateTime";

	private static final String LAST_MODIFIED_BY = "lastModifiedBy";

	private static final String SYSTEM_FILE_NAME = "systemFileName";

	private List<ContentVersionVO> contentVersionVOs;

	private RecordIdToCaptionConverter recordCaptionConverter = new RecordIdToCaptionConverter();

	private BaseStringToDateTimeConverter dateTimeConverter = new BaseStringToDateTimeConverter();

	private ButtonsContainer buttonsContainer;

	private AppLayerFactory appLayerFactory;

	private boolean isShowingSystemFileName = false;

	private HashSet<ContentVersionVO> selectedContentVersions;

	public ContentVersionVOTable(String tableId, AppLayerFactory appLayerFactory, boolean isShowingSystemFileName) {
		this(tableId, new ArrayList<ContentVersionVO>(), appLayerFactory, isShowingSystemFileName);
	}

	public ContentVersionVOTable(String tableId, List<ContentVersionVO> contentVersions,
								 AppLayerFactory appLayerFactory, boolean isShowingSystemFileName) {
		super(tableId);
		this.appLayerFactory = appLayerFactory;
		this.isShowingSystemFileName = isShowingSystemFileName;
		this.selectedContentVersions = new HashSet<>();

		if (isSelectionColumn()) {
			addContainerProperty(CHECK_BOX, CheckBox.class, null);
		}
		addContainerProperty(FILE_NAME, DownloadContentVersionLink.class, null);
		addContainerProperty(VERSION, String.class, null);
		addContainerProperty(LENGTH, String.class, null);
		addContainerProperty(LAST_MODIFICATION_DATE_TIME, String.class, null);
		addContainerProperty(LAST_MODIFIED_BY, String.class, null);
		addContainerProperty(COMMENT, String.class, null);
		if (this.isShowingSystemFileName) {
			addContainerProperty(SYSTEM_FILE_NAME, String.class, null);
		}


		setColumnHeader(CHECK_BOX, "");
		setColumnHeader(FILE_NAME, $("ContentVersion.fileName"));
		setColumnHeader(VERSION, $("ContentVersion.version"));
		setColumnHeader(LENGTH, $("ContentVersion.length"));
		setColumnHeader(LAST_MODIFICATION_DATE_TIME, $("ContentVersion.lastModificationDateTime"));
		setColumnHeader(LAST_MODIFIED_BY, $("ContentVersion.lastModifiedBy"));
		setColumnHeader(COMMENT, $("ContentVersion.comment"));
		if (this.isShowingSystemFileName) {
			setColumnHeader(SYSTEM_FILE_NAME, $("ContentVersion.systemFileName"));
		}

		setColumnExpandRatio(FILE_NAME, 1);
		setColumnCollapsingAllowed(true);
		setContentVersions(contentVersions);

		if (isDeleteColumn()) {
			buttonsContainer = new ButtonsContainer(getContainerDataSource());
			buttonsContainer.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(Object itemId, ButtonsContainer<?> container) {
					int indexOfItemId = buttonsContainer.indexOfId(itemId);
					final ContentVersionVO contentVersionVO = contentVersionVOs.get(indexOfItemId);
					DeleteButton deleteButton = new DeleteButton() {
						@Override
						protected void confirmButtonClick(ConfirmDialog dialog) {
							selectedContentVersions.remove(contentVersionVO);
							selectionUpdated();
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

	protected boolean isSelectionColumn() {
		return false;
	}

	protected boolean isSelectionPossible(ContentVersionVO contentVersionVO) {
		return isDeletePossible(contentVersionVO);
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
		for (final ContentVersionVO contentVersion : contentVersions) {
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
			String systemFileName = "";
			DataLayerFactory dataLayerFactory = appLayerFactory.getModelLayerFactory().getDataLayerFactory();
			DataLayerConfiguration conf = dataLayerFactory.getDataLayerConfiguration();
			if (conf.getContentDaoType() == ContentDaoType.FILESYSTEM) {
				systemFileName = ((FileSystemContentDao) dataLayerFactory.getContentsDao()).getFileOf(contentVersion.getHash()).getName();
			} else {
				systemFileName = "Unsupported";
			}

			final Object itemId = addItem();
			Item item = getItem(itemId);
			final CheckBox checkBox = new CheckBox() {
				@Override
				public boolean isVisible() {
					return isSelectionPossible(contentVersion);
				}
			};
			checkBox.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					if (Boolean.TRUE.equals(event.getProperty().getValue())) {
						selectedContentVersions.add(contentVersion);
					} else {
						selectedContentVersions.remove(contentVersion);
					}
					selectionUpdated();
				}
			});
			checkBox.setValue(selectedContentVersions.contains(contentVersion));

			if (isSelectionColumn()) {
				item.getItemProperty(CHECK_BOX).setValue(checkBox);
			}

			item.getItemProperty(FILE_NAME).setValue(new DownloadContentVersionLink(contentVersion, fileName));
			item.getItemProperty(VERSION).setValue(version);
			item.getItemProperty(LENGTH).setValue(lengthCaption);
			item.getItemProperty(LAST_MODIFICATION_DATE_TIME).setValue(lastModificationDateTimeCaption);
			item.getItemProperty(LAST_MODIFIED_BY).setValue(lastModifiedByCaption);
			item.getItemProperty(COMMENT).setValue(comment);
			if (isShowingSystemFileName) {
				item.getItemProperty(SYSTEM_FILE_NAME).setValue(systemFileName);
			}
		}
		setColumnCollapsed(COMMENT, true);
		if (isShowingSystemFileName) {
			setColumnCollapsed(SYSTEM_FILE_NAME, true);
		}
	}

	public void removeAllSelection() {
		selectedContentVersions.clear();
		selectionUpdated();
	}

	public HashSet<ContentVersionVO> getSelectedContentVersions() {
		return selectedContentVersions;
	}

	protected void selectionUpdated() {

	}
}
