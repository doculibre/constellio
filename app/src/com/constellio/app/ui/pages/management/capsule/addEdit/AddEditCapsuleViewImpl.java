package com.constellio.app.ui.pages.management.capsule.addEdit;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.fields.upload.ContentVersionUploadField;
import com.constellio.app.ui.framework.components.resource.ConstellioResourceHandler;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.searchConfig.SearchConfigurationViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.services.records.RecordServicesException;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditCapsuleViewImpl extends BaseViewImpl implements AddEditCapsuleView {

	private AddEditCapsulePresenter presenter;
	private RecordVO recordVO;

	@Override
	protected String getTitle() {
		return $("AddEditCapsuleView.viewTitle");
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
		presenter = new AddEditCapsulePresenter(this);
		if (StringUtils.isNotEmpty(event.getParameters())) {
			Map<String, String> paramsMap = ParamUtils.getParamsMap(event.getParameters());
			recordVO = presenter.getRecordVO(paramsMap.get("id"));
		}
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return SearchConfigurationViewImpl.getSearchConfigurationBreadCrumbTrail(this, getTitle());
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		if (this.recordVO == null) {
			this.recordVO = presenter.newRecordVO();
		}

		return new RecordForm(this.recordVO, new CapsuleRecordFieldFactory()) {
			@Override
			protected void saveButtonClick(RecordVO viewObject) {
				try {
					presenter.saveButtonClicked(recordVO);
				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			protected void cancelButtonClick(RecordVO viewObject) {
				presenter.cancelButtonClicked();
			}
		};
	}

	private class CapsuleRecordFieldFactory extends RecordFieldFactory {

		@Override
		public Field<?> build(RecordVO recordVO, MetadataVO metadataVO, Locale locale) {
			Field<?> field;
			if (MetadataVO.getCodeWithoutPrefix(metadataVO.getCode()).equals(Capsule.IMAGES)) {
				field = new ContentVersionUploadField(true, true, false) {
					@Override
					protected Component newItemCaption(Object itemId) {
						ContentVersionVO contentVersionVO = (ContentVersionVO) itemId;
						boolean majorVersionFieldVisible = false;
						return new ContentVersionUploadField.ContentVersionCaption(contentVersionVO, majorVersionFieldVisible) {
							@Override
							protected Component newCaptionComponent(ContentVersionVO contentVersionVO) {
								String hash = presenter.getHash(contentVersionVO);
								String filename = contentVersionVO.getFileName();
								Resource resource = ConstellioResourceHandler.createResource(hash, filename);
								return new DownloadLink(resource, filename);
							}
						};
					}
				};
				((ContentVersionUploadField) field).setMajorVersionFieldVisible(false);
			} else {
				field = super.build(recordVO, metadataVO, locale);
			}
			return field;
		}

	}
}
