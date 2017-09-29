package com.constellio.app.ui.pages.management.capsule.addEdit;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.records.RecordServicesException;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class AddEditCapsuleViewImpl extends BaseViewImpl implements AddEditCapsuleView {

    private AddEditCapsulePresenter presenter;
    private RecordVO recordVO;

    @Override
    protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
        presenter = new AddEditCapsulePresenter(this);
        if (StringUtils.isNotEmpty(event.getParameters())) {
            Map<String, String> paramsMap = ParamUtils.getParamsMap(event.getParameters());
            recordVO = presenter.getRecordVO(paramsMap.get("id"));
        }
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        if (this.recordVO == null) {
            this.recordVO = presenter.newRecordVO();
        }

        return new RecordForm(this.recordVO) {
            @Override
            protected void saveButtonClick(RecordVO viewObject) throws ValidationException {
                try{
                    presenter.saveButtonClicked(recordVO);
                } catch (RecordServicesException e ) {
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
