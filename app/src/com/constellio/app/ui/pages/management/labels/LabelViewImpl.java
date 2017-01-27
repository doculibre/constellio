package com.constellio.app.ui.pages.management.labels;

import com.constellio.app.modules.rm.ui.components.RMMetadataDisplayFactory;
import com.constellio.app.ui.entities.LabelVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Buffered;
import com.vaadin.data.Validator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

/**
 * Created by Nicolas D'Amours & Charles Blanchette on 2017-01-25.
 */
public class LabelViewImpl extends BaseViewImpl implements AddEditLabelView {
    private AddEditLabelPresenter presenter;
    private RecordVO recordVO;

    @Override
    public void setLabels(List<LabelVO> list) {

    }

    @Override
    public void addLabels(LabelVO... items) {

    }

    @Override
    protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
        presenter = new AddEditLabelPresenter(this);
        if (StringUtils.isNotEmpty(event.getParameters())) {
            Map<String, String> paramsMap = ParamUtils.getParamsMap(event.getParameters());
            recordVO = presenter.getRecordVO(paramsMap.get("id"), RecordVO.VIEW_MODE.DISPLAY);
        }
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidth("100%");
        layout.setSpacing(true);

        layout.addComponent(new RecordDisplay(recordVO));
        return layout;
    }

    @Override
    protected Button.ClickListener getBackButtonClickListener() {
        return new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                presenter.backButtonClicked();
            }
        };
    }

    @Override
    protected String getTitle() {
        return $("LabelDisplayViewImpl.title") + " : " + recordVO.getTitle();
    }
}
