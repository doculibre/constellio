package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;

import static com.constellio.app.ui.i18n.i18n.$;

public class RMFolderSummaryColumnViewImpl extends BaseViewImpl implements RMFolderSummaryColumnView {


    RMFolderSummaryColumnPresenter rmFolderSummaryColumnPresenter;

    @PropertyId("metadata")
    private ComboBox metadata;

    @PropertyId("textPrefix")
    private TextField textPrefix;

    @PropertyId("displayCondition")
    private ListOptionGroup displayCondition;

    public RMFolderSummaryColumnViewImpl() {
        rmFolderSummaryColumnPresenter = new RMFolderSummaryColumnPresenter(this);
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        metadata = new BaseComboBox($("RMFolderSummaryColumnViewImpl.metadata"));
        textPrefix = new BaseTextField($("RMFolderSummaryColumnViewImpl.prefix"));
        displayCondition = new ListOptionGroup($("RMFolderSummaryColumnViewImpl.displayCondition"));

        displayCondition.addItem(SummaryColomnParams.DisplayCondition.COMPLETED);
        displayCondition.addItem(SummaryColomnParams.DisplayCondition.ALWAYS);

        displayCondition.setItemCaption(SummaryColomnParams.DisplayCondition.COMPLETED, $("SummaryColomnParams.DisplayCondition.ifcompleted"));
        displayCondition.setItemCaption(SummaryColomnParams.DisplayCondition.ALWAYS, $("SummaryColomnParams.DisplayCondition.always"));

        BaseForm<SummaryColomnParams> baseForm = new BaseForm<SummaryColomnParams>(new SummaryColomnParams(), this, metadata, textPrefix, displayCondition) {
            @Override
            protected void saveButtonClick(SummaryColomnParams viewObject) throws ValidationException {

            }

            @Override
            protected void cancelButtonClick(SummaryColomnParams viewObject) {

            }
        };

        Table table = new BaseTable(getClass().getName());

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.addComponent(baseForm);
        verticalLayout.addComponent(table);

        return verticalLayout;
    }


    @Override
    protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
        if (event != null) {
            rmFolderSummaryColumnPresenter.forParams(event.getParameters());
        }
    }
}
