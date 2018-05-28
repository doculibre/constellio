package com.constellio.app.ui.pages.summarycolumn;

import com.constellio.app.modules.rm.ui.pages.folder.SummaryColumnVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.SummaryColumnContainer;
import com.constellio.app.ui.framework.data.SummaryColumnDataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;

import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class SummaryColumnViewImpl extends BaseViewImpl implements SummaryColumnView {

    SummaryColumnPresenter presenter;

    @PropertyId("metadataVO")
    private ComboBox metadataComboBox;

    @PropertyId("prefix")
    private TextField prefix;

    @PropertyId("displayCondition")
    private ListOptionGroup displayCondition;

    private SummaryColumnDataProvider summaryColumnDataProvider;

    private Table table;
    private SummaryColumnContainer summaryColumnContainer;
    private SummaryColumnVO modifingSummaryColumnVO;

    public SummaryColumnViewImpl() {
        presenter = new SummaryColumnPresenter(this);
    }

    @Override
    protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {

        Map<String, String> params = ParamUtils.getParamsMap(event.getParameters());
        presenter.setSchemaCode(params.get("schemaCode"));
        presenter.setParameters(params);
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        metadataComboBox = new BaseComboBox($("RMFolderSummaryColumnViewImpl.metadata"));

        final List<SummaryColumnVO> summaryColumnVOList = presenter.dataInMetadataSummaryColumn();

        metadataComboBox.setTextInputAllowed(false);
        metadataComboBox.setRequired(true);
        metadataComboBox.setImmediate(true);

        metadataComboBox.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (modifingSummaryColumnVO != null) {
                    clearFields();
                    removeMetadataFromPossibleSelection();
                }
            }
        });


        for(MetadataVO metadataVO : presenter.getMetadatas()) {
            metadataComboBox.addItem(metadataVO);
        }

        removeMetadataFromPossibleSelection();

        table = new BaseTable(getClass().getName());

        summaryColumnDataProvider = new SummaryColumnDataProvider(summaryColumnVOList);
        summaryColumnContainer = new SummaryColumnContainer(summaryColumnDataProvider, this);

        //

        table.setContainerDataSource(summaryColumnContainer);
        table.addContainerProperty("up", Button.class, null);
//        table.setItemCaption("up", "");
//        table.addContainerProperty("down", Button.class, null);
//        table.setItemCaption("down", "");
//        table.addContainerProperty($("RMFolderSummaryColumnViewImpl.metadataHeader"), MetadataVO.class, null);
//        table.addContainerProperty($("RMFolderSummaryColumnViewImpl.prefixHeader"), String.class, null);
//        table.addContainerProperty($("RMFolderSummaryColumnViewImpl.displayConditionHeader"), SummaryColumnParams.DisplayCondition.class, null);
//        table.addContainerProperty("delete", Button.class, null);
//        table.setItemCaption("delete", "");

        //setTableData(summaryColumnVOList);

        prefix = new BaseTextField($("RMFolderSummaryColumnViewImpl.prefix"));
        displayCondition = new ListOptionGroup($("RMFolderSummaryColumnViewImpl.displayCondition"));
        displayCondition.setRequired(true);
        displayCondition.addItem(SummaryColumnParams.DisplayCondition.COMPLETED);
        displayCondition.addItem(SummaryColumnParams.DisplayCondition.ALWAYS);


        BaseForm<SummaryColumnParams> baseForm = new BaseForm<SummaryColumnParams>(new SummaryColumnParams(), this, metadataComboBox, prefix, displayCondition) {
            @Override
            protected void saveButtonClick(final SummaryColumnParams viewObject) throws ValidationException {

                if(modifingSummaryColumnVO != null) {
                    presenter.modifyMetadataForSummaryColumn(viewObject);
                } else {
                    presenter.addMetadaForSummary(viewObject);
                    summaryColumnDataProvider.addSummaryColumnVO(summaryColumnParamsToSummaryVO(viewObject));
                }

                summaryColumnDataProvider.fireDataRefreshEvent();
                clearFields();
                removeMetadataFromPossibleSelection();
            }

            @Override
            protected void cancelButtonClick(SummaryColumnParams viewObject) {
                clearFields();
            }
        };


        table.setWidth("100%");

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.addComponent(baseForm);
        verticalLayout.addComponent(table);

        return verticalLayout;
    }


    private SummaryColumnVO summaryColumnParamsToSummaryVO(SummaryColumnParams summaryColumnParams) {
        SummaryColumnVO summaryColumnVO = new SummaryColumnVO();
        summaryColumnVO.setMetadata(summaryColumnParams.getMetadataVO());
        summaryColumnVO.setAlwaysShown(summaryColumnParams.getDisplayCondition() == SummaryColumnParams.DisplayCondition.ALWAYS);
        summaryColumnVO.setPrefix(summaryColumnParams.getPrefix());

        return summaryColumnVO;
    }


    private void removeMetadataFromPossibleSelection() {
        List<SummaryColumnVO> summaryColumnVOList = presenter.dataInMetadataSummaryColumn();

        for(SummaryColumnVO summaryColumnVO : summaryColumnVOList) {
            metadataComboBox.removeItem(summaryColumnVO.getMetadataVO());
        }
    }

    private void clearFields() {
        displayCondition.setValue(null);
        prefix.setValue("");
        metadataComboBox.setValue(null);
        this.modifingSummaryColumnVO = null;
    }

    @Override
    public void alterSummaryMetadata(SummaryColumnVO summaryColumnVO) {
        this.modifingSummaryColumnVO = summaryColumnVO;
        metadataComboBox.addItem(this.modifingSummaryColumnVO.getMetadataVO());
        metadataComboBox.setValue(summaryColumnVO.getMetadataVO());
        prefix.setValue(summaryColumnVO.getPrefix());
        if(summaryColumnVO.isAlwaysShown()) {
            displayCondition.setValue(SummaryColumnParams.DisplayCondition.ALWAYS);
        } else {
            displayCondition.setValue(SummaryColumnParams.DisplayCondition.COMPLETED);
        }
    }

    @Override
    public void deleteSummaryMetadata(SummaryColumnVO summaryColumnVO) {
        presenter.deleteMetadataForSummaryColumn(summaryColumnVO);
        summaryColumnDataProvider.removeSummaryColumnVO(summaryColumnVO);
        summaryColumnDataProvider.fireDataRefreshEvent();
    }
}
