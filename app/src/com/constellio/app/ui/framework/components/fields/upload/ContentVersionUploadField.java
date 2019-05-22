package com.constellio.app.ui.framework.components.fields.upload;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.components.content.DownloadContentVersionLink;
import com.constellio.app.ui.framework.components.converters.TempFileUploadToContentVersionVOConverter;
import com.vaadin.data.Validator.EmptyValueException;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.ui.Component;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class ContentVersionUploadField extends BaseUploadField {

	private boolean majorVersionFieldVisible = true;

	public ContentVersionUploadField() {
		this(false, true, false);
	}

	public ContentVersionUploadField(boolean multiValue) {
		this(multiValue, true, false);
	}

	public ContentVersionUploadField(boolean multiValue, boolean haveDeleteButton, boolean isViewOnly) {
		super(haveDeleteButton, isViewOnly);
		setConverter(new TempFileUploadToContentVersionVOConverter() {
			@Override
			protected boolean shouldSetHashForTemporaryFiles() {
				return true;
			}
		});
		setMultiValue(multiValue);
	}

	@Override
	protected Object getItemId(TempFileUpload tempFileUpload) {
		return getConverter().convertToModel(tempFileUpload, ContentVersionVO.class, getLocale());
	}

	@Override
	protected Component newItemCaption(Object itemId) {
		ContentVersionVO contentVersionVO = (ContentVersionVO) itemId;
		boolean majorVersionFieldVisible = isMajorVersionField(contentVersionVO);
		return new ContentVersionCaption(contentVersionVO, majorVersionFieldVisible);
	}

	protected boolean isMajorVersionField(ContentVersionVO contentVersionVO) {
		return majorVersionFieldVisible && contentVersionVO.getHash() == null;
	}

	public final boolean isMajorVersionFieldVisible() {
		return majorVersionFieldVisible;
	}

	public final void setMajorVersionFieldVisible(boolean visible) {
		this.majorVersionFieldVisible = visible;
		Map<Object, Component> itemCaptions = getItemCaptions();
		for (Object itemId : itemCaptions.keySet()) {
			ContentVersionVO contentVersionVO = (ContentVersionVO) itemId;
			Component itemCaption = itemCaptions.get(itemId);
			ContentVersionCaption contentVersionCaption = (ContentVersionCaption) itemCaption;
			contentVersionCaption.setMajorVersionFieldVisible(isMajorVersionField(contentVersionVO));
		}
	}

	@Override
	protected void deleteTempFile(Object itemId) {
		// Nothing to do, will be deleted by the linked TempUploadFile
		ContentVersionVO contentVersionVO = (ContentVersionVO) itemId;
		contentVersionVO.getInputStreamProvider().deleteTemp();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void validate()
			throws InvalidValueException {
		super.validate();
		Object convertedValue = getConvertedValue();
		if (convertedValue instanceof List) {
			List<ContentVersionVO> convertedList = (List<ContentVersionVO>) convertedValue;
			for (ContentVersionVO contentVersionVO : convertedList) {
				validate(contentVersionVO);
			}
		} else if (convertedValue != null) {
			validate((ContentVersionVO) convertedValue);
		}
	}

	private void validate(ContentVersionVO contentVersionVO) throws InvalidValueException {
		if (isMajorVersionField(contentVersionVO) && contentVersionVO.getHash() == null && contentVersionVO.isMajorVersion() == null) {
			throw new EmptyValueException($("ContentVersionUploadField.majorVersion"));
		}
	}

	public static class ContentVersionCaption extends VerticalLayout {

		private Component captionComponent;

		private OptionGroup majorVersionField;

		public ContentVersionCaption(ContentVersionVO contentVersionVO, boolean majorVersionFieldVisible) {
			setSpacing(true);

			captionComponent = newCaptionComponent(contentVersionVO);

			majorVersionField = new OptionGroup();
			majorVersionField.setVisible(majorVersionFieldVisible);
			majorVersionField.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
			majorVersionField.addItem(true);
			majorVersionField.addItem(false);
			majorVersionField.setItemCaption(true, $("yes"));
			majorVersionField.setItemCaption(false, $("no"));
			majorVersionField.setCaption($("ContentVersionUploadField.majorVersion"));
			majorVersionField.setRequired(true);
			majorVersionField.setImmediate(true);
			majorVersionField.setPropertyDataSource(new NestedMethodProperty<Boolean>(contentVersionVO, "majorVersion"));

			addComponent(captionComponent);
			addComponent(majorVersionField);
		}

		private void setMajorVersionFieldVisible(boolean visible) {
			majorVersionField.setVisible(visible);
			majorVersionField.setRequired(visible);
		}

		protected Component newCaptionComponent(ContentVersionVO contentVersionVO) {
			return new DownloadContentVersionLink(contentVersionVO);
		}

	}

}
