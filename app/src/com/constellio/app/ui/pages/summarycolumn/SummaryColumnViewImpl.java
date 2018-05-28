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

        final List<SummaryColumnVO> summaryColumnVOList = presenter.summaryColumnVOList();

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


        refreshMetadataCombox();

        table = new BaseTable(getClass().getName());

        summaryColumnDataProvider = new SummaryColumnDataProvider(summaryColumnVOList);
        summaryColumnContainer = new SummaryColumnContainer(summaryColumnDataProvider, this);

        table.setContainerDataSource(summaryColumnContainer);
        table.setColumnHeader(SummaryColumnContainer.UP, "");
        table.setColumnHeader(SummaryColumnContainer.DOWN, "");
        table.setColumnHeader(SummaryColumnContainer.METADATA_VO, $("RMFolderSummaryColumnViewImpl.metadataHeader"));
        table.setColumnHeader(SummaryColumnContainer.PREFIX, $("RMFolderSummaryColumnViewImpl.prefixHeader"));
        table.setColumnHeader(SummaryColumnContainer.DISPLAY_CONDITION, $("RMFolderSummaryColumnViewImpl.displayConditionHeader"));
        table.setColumnHeader(SummaryColumnContainer.MODIFY, "");
        table.setColumnHeader(SummaryColumnContainer.DELETE, "");

        prefix = new BaseTextField($("RMFolderSummaryColumnViewImpl.prefix"));
        displayCondition = new ListOptionGroup($("RMFolderSummaryColumnViewImpl.displayCondition"));
        displayCondition.setRequired(true);
        displayCondition.addItem(SummaryColumnParams.DisplayCondition.COMPLETED);
        displayCondition.addItem(SummaryColumnParams.DisplayCondition.ALWAYS);


        BaseForm<SummaryColumnParams> baseForm = new BaseForm<SummaryColumnParams>(new SummaryColumnParams(), this, metadataComboBox, prefix, displayCondition) {
            @Override
            protected void saveButtonClick(final SummaryColumnParams viewObject) {

                SummaryColumnVO summaryColumnVO = summaryColumnParamsToSummaryVO(viewObject);
                if(modifingSummaryColumnVO != null) {
                    presenter.modifyMetadataForSummaryColumn(viewObject);
                    List<SummaryColumnVO> summaryColumnVOList = presenter.summaryColumnVOList();
                    int index = presenter.findMetadataIndex(summaryColumnVOList, viewObject.getMetadataVO().getCode());
                    summaryColumnDataProvider.removeSummaryColumnVO(index);
                    summaryColumnDataProvider.addSummaryColumnVO(index, summaryColumnVO);
                    presenter.getMetadatas();
                } else {
                    presenter.addMetadaForSummary(viewObject);
                    summaryColumnDataProvider.addSummaryColumnVO(summaryColumnVO);
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


        this.table.setWidth("100%");

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.addComponent(baseForm);
        verticalLayout.addComponent(table);

        return verticalLayout;
    }

    private void refreshMetadataCombox() {
        metadataComboBox.removeAllItems();
        for(MetadataVO metadataVO : presenter.getMetadatas()) {
            metadataComboBox.addItem(metadataVO);
        }

        removeMetadataFromPossibleSelection();
    }


    private SummaryColumnVO summaryColumnParamsToSummaryVO(SummaryColumnParams summaryColumnParams) {
        SummaryColumnVO summaryColumnVO = new SummaryColumnVO();
        summaryColumnVO.setMetadata(summaryColumnParams.getMetadataVO());
        summaryColumnVO.setAlwaysShown(summaryColumnParams.getDisplayCondition() == SummaryColumnParams.DisplayCondition.ALWAYS);
        summaryColumnVO.setPrefix(summaryColumnParams.getPrefix());

        return summaryColumnVO;
    }


    private void removeMetadataFromPossibleSelection() {
        List<SummaryColumnVO> summaryColumnVOList = presenter.summaryColumnVOList();

        for(SummaryColumnVO summaryColumnVO : summaryColumnVOList) {
            this.metadataComboBox.removeItem(summaryColumnVO.getMetadataVO());
        }
    }

    private void clearFields() {
        this.displayCondition.setValue(null);
        this.prefix.setValue("");
        this.metadataComboBox.setValue(null);
        this.modifingSummaryColumnVO = null;
    }

    @Override
    public void alterSummaryMetadata(SummaryColumnVO summaryColumnVO) {
        this.metadataComboBox.addItem(summaryColumnVO.getMetadataVO());
        this.metadataComboBox.setValue(summaryColumnVO.getMetadataVO());
        this.prefix.setValue(summaryColumnVO.getPrefix());
        if(summaryColumnVO.isAlwaysShown()) {
            this.displayCondition.setValue(SummaryColumnParams.DisplayCondition.ALWAYS);
        } else {
            this.displayCondition.setValue(SummaryColumnParams.DisplayCondition.COMPLETED);
        }
        this.modifingSummaryColumnVO = summaryColumnVO;
    }

    @Override
    public void deleteSummaryMetadata(SummaryColumnVO summaryColumnVO) {
        this.presenter.deleteMetadataForSummaryColumn(summaryColumnVO);
        this.summaryColumnDataProvider.removeSummaryColumnVO(summaryColumnVO);
        refreshMetadataCombox();
        this.summaryColumnDataProvider.fireDataRefreshEvent();
    }
}
