package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;

import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class SummaryColumnViewImpl extends BaseViewImpl implements RMFolderSummaryColumnView {


    SummaryColumnPresenter presenter;

    @PropertyId("metadata")
    private ComboBox metadataComboBox;

    @PropertyId("prefix")
    private TextField prefix;

    @PropertyId("displayCondition")
    private ListOptionGroup displayCondition;

    private  IndexedContainer container;

    private Table table;

    private int batchSize = 100;

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

        List<SummaryColumnVO> summaryColumnVOList = presenter.dataInMetadataSummaryColumn();

        metadataComboBox.setTextInputAllowed(false);
        metadataComboBox.setRequired(true);
        metadataComboBox.setImmediate(true);


        for(MetadataVO metadataVO : presenter.getMetadatas()) {
            metadataComboBox.addItem(metadataVO);
        }

        removeMetadataFromPossibleSelection();

        table = new BaseTable(getClass().getName());

        container = new IndexedContainer();
        table.setContainerDataSource(container);
        container.addContainerProperty("up", Button.class, null);
        table.setItemCaption("up", "");
        table.addContainerProperty("down", Button.class, null);
        table.setItemCaption("down", "");
        table.addContainerProperty($("RMFolderSummaryColumnViewImpl.metadataHeader"), MetadataVO.class, null);
        table.addContainerProperty($("RMFolderSummaryColumnViewImpl.prefixHeader"), String.class, null);
        table.addContainerProperty($("RMFolderSummaryColumnViewImpl.displayConditionHeader"), SummaryColumnParams.DisplayCondition.class, null);
        table.addContainerProperty("delete", Button.class, null);
        table.setItemCaption("delete", "");

        setTableData(summaryColumnVOList);

        prefix = new BaseTextField($("RMFolderSummaryColumnViewImpl.prefix"));
        displayCondition = new ListOptionGroup($("RMFolderSummaryColumnViewImpl.displayCondition"));
        displayCondition.setRequired(true);
        displayCondition.addItem(SummaryColumnParams.DisplayCondition.COMPLETED);
        displayCondition.addItem(SummaryColumnParams.DisplayCondition.ALWAYS);


        BaseForm<SummaryColumnParams> baseForm = new BaseForm<SummaryColumnParams>(new SummaryColumnParams(), this, metadataComboBox, prefix, displayCondition) {
            @Override
            protected void saveButtonClick(final SummaryColumnParams viewObject) throws ValidationException {
                presenter.addMetadaForSummary(viewObject);

                setTableData(presenter.dataInMetadataSummaryColumn());
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

    private void setTableData(List<SummaryColumnVO> summaryColumnVOList) {
        table.removeAllItems();

        for(final SummaryColumnVO summaryColumnVO : summaryColumnVOList) {
            SummaryColumnParams.DisplayCondition displayCondition  = SummaryColumnParams.DisplayCondition.COMPLETED;
            if(summaryColumnVO.isAlwaysShown()) {
                displayCondition = SummaryColumnParams.DisplayCondition.ALWAYS;
            }

            Button downButton = new BaseButton($("RMFolderSummaryColumnViewImpl.downHeader")) {
                @Override
                protected void buttonClick(ClickEvent event) {
                    presenter.moveDownSummaryMetadata(summaryColumnVO.getMetadata().getCode());
                    setTableData(presenter.dataInMetadataSummaryColumn());
                }
            };

            Button upButton = new BaseButton($("RMFolderSummaryColumnViewImpl.upHeader")) {
                @Override
                protected void buttonClick(ClickEvent event) {
                    presenter.moveUpSummaryMetadata(summaryColumnVO.getMetadata().getCode());
                    setTableData(presenter.dataInMetadataSummaryColumn());
                }
            };

            table.addItem(new Object[] { upButton, downButton, summaryColumnVO.getMetadata(), summaryColumnVO.getPrefix(), displayCondition}, table.size());
        }
    }


    private void removeMetadataFromPossibleSelection() {
        List<SummaryColumnVO> summaryColumnVOList = presenter.dataInMetadataSummaryColumn();

        for(SummaryColumnVO summaryColumnVO : summaryColumnVOList) {
            metadataComboBox.removeItem(summaryColumnVO.getMetadata());
        }
    }

    private void clearFields() {
        displayCondition.setValue(null);
        prefix.setValue("");
        metadataComboBox.setValue(null);
    }
}
