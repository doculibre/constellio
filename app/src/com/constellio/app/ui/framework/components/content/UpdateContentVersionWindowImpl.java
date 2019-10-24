package com.constellio.app.ui.framework.components.content;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.BaseForm.FieldAndPropertyId;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.fields.upload.ContentVersionUploadField;
import com.constellio.app.ui.util.ResponsiveUtils;
import com.constellio.model.frameworks.validation.ValidationException;
import com.jgoodies.common.base.Strings;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class UpdateContentVersionWindowImpl extends BaseWindow implements UpdateContentVersionWindow, DropHandler {

	private boolean checkingIn;

	private String nullValue = new String();

	private ContentVersionVO newVersionVO;

	private Object majorVersion;

	private Property<ContentVersionVO> contentVersionProperty = new NestedMethodProperty<ContentVersionVO>(this, "contentVersion");

	private Property<Object> majorVersionProperty = new NestedMethodProperty<Object>(this, "majorVersion");

	private VerticalLayout mainLayout;

	private BaseForm<RecordVO> uploadForm;

	private Label errorLabel;

	private ContentVersionUploadField uploadField;

	private OptionGroup majorVersionField;

	private UpdateContentVersionPresenter presenter;

	public boolean isCancel() {
		return isCancel;
	}

	private boolean isCancel = false;

	public UpdateContentVersionWindowImpl(Map<RecordVO, MetadataVO> records) {
		this(records, false);
	}

	public UpdateContentVersionWindowImpl(Map<RecordVO, MetadataVO> records, boolean isEditView) {
		setModal(true);
		if (ResponsiveUtils.isPhone()) {
			setWidth("90%");
		} else {
			setWidth("750px");
		}
		
		setZIndex(null);

		mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.setWidth("100%");

		String title = $("UpdateContentVersionWindow.newVersionTitle");
		setCaption(title);

		errorLabel = new Label();
		errorLabel.addStyleName("error-label");
		errorLabel.setVisible(false);

		uploadField = new ContentVersionUploadField(false, false, isEditView) {
			@Override
			protected boolean isMajorVersionField(ContentVersionVO contentVersionVO) {
				return false;
			}
		};
		uploadField.setCaption($("UpdateContentVersionWindow.uploadField"));
		uploadField.setImmediate(true);
		uploadField.addValidator(new Validator() {
			@Override
			public void validate(Object value)
					throws InvalidValueException {
				if (getContentVersion() == null && getMajorVersion() instanceof Boolean) {
					throw new InvalidValueException($("UpdateContentVersionWindow.validate.noVersionIfContentVersionUploaded"));
				}
			}
		});

		majorVersionField = new OptionGroup();
		majorVersionField.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		majorVersionField.setCaption($("UpdateContentVersionWindow.version"));
		majorVersionField.setRequired(true);
		majorVersionField.setImmediate(true);

		majorVersionField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				Object value = majorVersionField.getValue();

				if (value instanceof String) {
					uploadField.setRequired(false);
				} else {
					uploadField.setRequired(true);
				}
			}
		});

		List<FieldAndPropertyId> fieldsAndPropertyIds = new ArrayList<FieldAndPropertyId>();
		fieldsAndPropertyIds.add(new FieldAndPropertyId(uploadField, "contentVersion"));
		fieldsAndPropertyIds.add(new FieldAndPropertyId(majorVersionField, "majorVersion"));

		if (records.keySet().iterator().hasNext()) {
			RecordVO recordVO = records.keySet().iterator().next();
			uploadForm = new BaseForm<RecordVO>(recordVO, fieldsAndPropertyIds) {
				@Override
				protected Item newItem(RecordVO viewObject) {
					return new Item() {
						@SuppressWarnings("rawtypes")
						@Override
						public Property getItemProperty(Object id) {
							Property property;
							if ("contentVersion".equals(id)) {
								property = contentVersionProperty;
							} else if ("majorVersion".equals(id)) {
								property = majorVersionProperty;
							} else {
								property = null;
							}
							return property;
						}

						@Override
						public Collection<?> getItemPropertyIds() {
							return Arrays.asList("contentVersion", "majorVersion");
						}

						@SuppressWarnings("rawtypes")
						@Override
						public boolean addItemProperty(Object id, Property property)
								throws UnsupportedOperationException {
							throw new UnsupportedOperationException("Read-only item");
						}

						@Override
						public boolean removeItemProperty(Object id)
								throws UnsupportedOperationException {
							throw new UnsupportedOperationException("Read-only item");
						}
					};
				}

				@Override
				protected void saveButtonClick(RecordVO viewObject)
						throws ValidationException {
					Boolean bMajorVersion;
					if (nullValue.equals(majorVersion)) {
						bMajorVersion = null;
					} else {
						bMajorVersion = (Boolean) majorVersion;
					}
					newVersionVO = (ContentVersionVO) uploadField.getValue();
					presenter.contentVersionSaved(newVersionVO, bMajorVersion);
					close();
				}

				@Override
				protected void cancelButtonClick(RecordVO viewObject) {
					isCancel = true;
					close();
				}
			};
		}

		uploadForm.setSizeFull();

		if(Strings.isNotBlank(getDocumentTitle())) {
			mainLayout.addComponent(new Label(getDocumentTitle()));
		}

		mainLayout.addComponents(errorLabel, uploadForm);

		DragAndDropWrapper dragAndDropWrapper = new DragAndDropWrapper(mainLayout);
		dragAndDropWrapper.setSizeFull();
		dragAndDropWrapper.addStyleName("no-scroll");
		setContent(dragAndDropWrapper);
		dragAndDropWrapper.setDropHandler(uploadField);

		presenter = new UpdateContentVersionPresenter(this, records);
	}

	public String getDocumentTitle() {
		return null;
	}

	@Override
	public void attach() {
		super.attach();
		errorLabel.setVisible(false);
		presenter.windowAttached(checkingIn);
	}

	public final ContentVersionVO getContentVersion() {
		return newVersionVO;
	}

	public final void setContentVersion(ContentVersionVO newVersionVO) {
		this.newVersionVO = newVersionVO;
	}

	public final Object getMajorVersion() {
		return majorVersion;
	}

	public final void setMajorVersion(Object majorVersion) {
		this.majorVersion = majorVersion;
	}

	@Override
	public void drop(DragAndDropEvent event) {
		uploadField.drop(event);
	}

	@Override
	public AcceptCriterion getAcceptCriterion() {
		return uploadField.getAcceptCriterion();
	}

	@Override
	public void showErrorMessage(String key, Object... args) {
		errorLabel.setVisible(true);
		errorLabel.setValue($(key, args));
	}

	@Override
	public boolean isFormVisible() {
		return uploadField.isVisible();
	}

	@Override
	public void setFormVisible(boolean visible) {
		uploadField.setVisible(visible);
		majorVersionField.setVisible(visible);
	}

	@Override
	public void setUploadFieldVisible(boolean visible) {
		uploadField.setVisible(visible);
	}

	private void initMajorVersionFieldOptions() {
		majorVersionField.removeAllItems();
		majorVersionField.addItem(false);
		majorVersionField.addItem(true);
	}

	@Override
	public void addMajorMinorSameOptions() {
		initMajorVersionFieldOptions();
		majorVersionField.addItem(nullValue);
		majorVersionField.setItemCaption(true, $("UpdateContentVersionWindow.options.newMajorVersion"));
		majorVersionField.setItemCaption(false, $("UpdateContentVersionWindow.options.newMinorVersion"));
		majorVersionField.setItemCaption(nullValue, $("UpdateContentVersionWindow.options.sameVersion"));
	}

	@Override
	public void addMajorMinorOptions() {
		initMajorVersionFieldOptions();
		majorVersionField.setItemCaption(true, $("UpdateContentVersionWindow.options.majorVersion"));
		majorVersionField.setItemCaption(false, $("UpdateContentVersionWindow.options.minorVersion"));
	}

	public void open(boolean checkingIn) {
		this.checkingIn = checkingIn;
		String updatedTitle;
		if (checkingIn) {
			setHeight("200px");
			updatedTitle = $("UpdateContentVersionWindow.checkInTitle");
		} else {
			setHeight("300px");
			updatedTitle = $("UpdateContentVersionWindow.newVersionTitle");
		}
		setCaption(updatedTitle);
		UI.getCurrent().addWindow(this);
	}

	@Override
	public void close() {
		if (newVersionVO != null && newVersionVO.getInputStreamProvider() != null) {
			newVersionVO.getInputStreamProvider().deleteTemp();
		}
		super.close();
	}
}
