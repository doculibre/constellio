package com.constellio.app.ui.pages.management.bagInfo.AddEditBagInfo;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditBagInfoViewImpl extends BaseViewImpl implements AddEditBagInfoView {
	private AddEditBagInfoPresenter presenter;
	private RecordVO recordVO;
	private boolean isEdit = false;

	@Override
	protected String getTitle() {
		return $(isEdit ? "AddEditBagInfoViewImpl.edit.title" : "AddEditBagInfoViewImpl.add.title");
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
		presenter = new AddEditBagInfoPresenter(this);
		if (StringUtils.isNotEmpty(event.getParameters())) {
			Map<String, String> paramsMap = ParamUtils.getParamsMap(event.getParameters());
			try {
				recordVO = presenter.getRecordVO(paramsMap.get("id"));
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				this.showErrorMessage(String.format("%s : %s", $("AddEditBagInfoView.couldNotFindId"), paramsMap.get("id")));
				this.navigateTo().previousView();
			}
		}
	}

	@Override
	protected Button.ClickListener getBackButtonClickListener() {
		return new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				navigateTo().previousView();
			}
		};
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		if (this.recordVO == null) {
			this.recordVO = presenter.newRecordVO();
		}

		return new RecordForm(this.recordVO, AddEditBagInfoViewImpl.this.getConstellioFactories()) {
			@Override
			protected void saveButtonClick(RecordVO viewObject) throws ValidationException {
				try {
					presenter.saveButtonClicked(recordVO);
				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			protected void cancelButtonClick(RecordVO viewObject) {
				navigateTo().previousView();
			}
		};
	}
}
