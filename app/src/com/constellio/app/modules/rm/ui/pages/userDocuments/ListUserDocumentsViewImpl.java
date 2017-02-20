package com.constellio.app.modules.rm.ui.pages.userDocuments;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.easyuploads.MultiFileUpload;

import com.constellio.app.modules.rm.ui.components.userDocument.DeclareUserContentContainerButton;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserDocumentVO;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.components.ContentVersionDisplay;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.upload.BaseMultiFileUpload;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.data.utils.Builder;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class ListUserDocumentsViewImpl extends BaseViewImpl implements ListUserDocumentsView, DropHandler {
	
	public static final String STYLE_NAME = "user-documents";
	public static final String STYLE_LAYOUT = STYLE_NAME + "-layout";
	public static final String TABLE_STYLE_NAME = STYLE_NAME + "-table";
	public static final String SELECT_PROPERTY_ID = "select";

	List<RecordVODataProvider> dataProviders;
	
	private DragAndDropWrapper dragAndDropWrapper;
	private VerticalLayout mainLayout;
	private MultiFileUpload multiFileUpload;
	private RecordVOLazyContainer userContentContainer;
	private ButtonsContainer<RecordVOLazyContainer> buttonsContainer;
	private RecordVOTable userContentTable;
	private Builder<ContainerButton> classifyButtonFactory;
	
	private RecordIdToCaptionConverter recordIdToCaptionConverter = new RecordIdToCaptionConverter();

	private ListUserDocumentsPresenter presenter;

	public ListUserDocumentsViewImpl() {
		presenter = new ListUserDocumentsPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListUserDocumentsView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		if (classifyButtonFactory == null) {
			classifyButtonFactory = new Builder<ContainerButton>() {
				@Override
				public ContainerButton build() {
					return new DeclareUserContentContainerButton();
				}
			};
		}

		addStyleName(STYLE_NAME);
		setCaption($("UserDocumentsWindow.title"));

		mainLayout = new VerticalLayout();
		mainLayout.addStyleName(STYLE_LAYOUT);
		mainLayout.setSpacing(true);

		multiFileUpload = new BaseMultiFileUpload() {
			@Override
			protected void handleFile(File file, String fileName, String mimeType, long length) {
				presenter.handleFile(file, fileName, mimeType, length);
			}
		};
		multiFileUpload.setWidth("100%");

		userContentContainer = new RecordVOLazyContainer(dataProviders);
		buttonsContainer = new ButtonsContainer<RecordVOLazyContainer>(userContentContainer) {
			@Override
			public Collection<?> getContainerPropertyIds() {
				List<Object> containerPropertyIds = new ArrayList<>();
				Collection<?> parentContainerPropertyIds = super.getContainerPropertyIds();
				containerPropertyIds.add(SELECT_PROPERTY_ID);
				containerPropertyIds.addAll(parentContainerPropertyIds);
				return containerPropertyIds;
			}

			@Override
			protected Property<?> getOwnContainerProperty(final Object itemId, final Object propertyId) {
				Property<?> property;
				if (SELECT_PROPERTY_ID.equals(propertyId)) {
					Property<?> selectProperty = new AbstractProperty<Boolean>() {
						@Override
						public Boolean getValue() {
							RecordVO recordVO = (RecordVO) ((RecordVOItem) userContentContainer.getItem(itemId)).getRecord();
							return presenter.isSelected(recordVO);
						}

						@Override
						public void setValue(Boolean newValue)
								throws com.vaadin.data.Property.ReadOnlyException {
							RecordVO recordVO = (RecordVO) ((RecordVOItem) userContentContainer.getItem(itemId)).getRecord();
							boolean selected = Boolean.TRUE.equals(newValue);
							presenter.selectionChanged(recordVO, selected);
						}

						@Override
						public Class<? extends Boolean> getType() {
							return Boolean.class;
						}
					};
					CheckBox checkBox = new CheckBox();
					checkBox.setPropertyDataSource(selectProperty);
					property = new ObjectProperty<CheckBox>(checkBox);
				} else {
					property = super.getOwnContainerProperty(itemId, propertyId);
				}
				return property;
			}

			@Override
			protected Class<?> getOwnType(Object propertyId) {
				Class<?> ownType;
				if (SELECT_PROPERTY_ID.equals(propertyId)) {
					ownType = CheckBox.class;
				} else {
					ownType = super.getOwnType(propertyId);
				}
				return ownType;
			}
		};

		buttonsContainer.addButton(classifyButtonFactory.build());
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						RecordVO recordVO = ((RecordVOItem) buttonsContainer.getItem(itemId)).getRecord();
						presenter.deleteButtonClicked(recordVO);
					}
				};
			}
		});

		userContentTable = new RecordVOTable();
		userContentTable.setContainerDataSource(buttonsContainer);
		userContentTable.setWidth("100%");
		userContentTable.addStyleName(TABLE_STYLE_NAME);
		userContentTable.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		userContentTable.setColumnHeader(SELECT_PROPERTY_ID, $("ListUserDocumentsView.selectColumnTitle"));
		userContentTable.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");

		mainLayout.addComponents(multiFileUpload, userContentTable);

		dragAndDropWrapper = new DragAndDropWrapper(mainLayout);
		dragAndDropWrapper.setSizeFull();
		dragAndDropWrapper.setDropHandler(multiFileUpload);
		return dragAndDropWrapper;
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	protected Component newCaptionComponent(RecordVO recordVO) {
		Component captionComponent;
		if (recordVO instanceof UserDocumentVO) {
			UserDocumentVO userDocumentVO = (UserDocumentVO) recordVO;
			ContentVersionVO contentVersionVO = userDocumentVO.getContent();
			if (contentVersionVO != null) {
				String filename = contentVersionVO.getFileName();
				captionComponent = new ContentVersionDisplay(recordVO, contentVersionVO, filename);
			} else {
				captionComponent = new Label("");
			}
		} else {
			captionComponent = new Label(recordIdToCaptionConverter.convertToPresentation(recordVO.getId(), String.class, getLocale()));
		}
		return captionComponent;
	}

	@Override
	public void drop(DragAndDropEvent event) {
		setVisible(true);
		multiFileUpload.drop(event);
	}

	@Override
	public AcceptCriterion getAcceptCriterion() {
		return multiFileUpload.getAcceptCriterion();
	}

	public void setClassifyButtonFactory(Builder<ContainerButton> classifyButtonFactory) {
		this.classifyButtonFactory = classifyButtonFactory;
	}

	@Override
	public void setUserContent(List<RecordVODataProvider> dataProviders) {
		this.dataProviders = dataProviders;
	}
}
