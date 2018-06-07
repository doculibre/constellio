package com.constellio.app.ui.pages.summarycolumn;

import com.constellio.app.ui.entities.SummaryColumnVO;
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
import com.constellio.model.entities.schemas.MetadataValueType;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class SummaryColumnViewImpl extends BaseViewImpl implements SummaryColumnView {

    private SummaryColumnPresenter presenter;

    @PropertyId("metadataVO")
    private ComboBox metadataComboBox;

    @PropertyId("prefix")
    private TextField prefix;

    @PropertyId("displayCondition")
    private ListOptionGroup displayCondition;

    @PropertyId("referenceMetadataDisplay")
    private ComboBox referenceMetadataDisplayComboBox;

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
    public SummaryColumnPresenter getSummaryColumnPresenter() {
        return presenter;
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        metadataComboBox = new BaseComboBox($("SummaryColumnViewImpl.metadata"));

        final List<SummaryColumnVO> summaryColumnVOList = presenter.summaryColumnVOList();

        metadataComboBox.setTextInputAllowed(false);
        metadataComboBox.setRequired(true);
        metadataComboBox.setImmediate(true);

        metadataComboBox.addValueChangeListener(new Property.ValueChangeListener() {
                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    if (modifingSummaryColumnVO != null) {
                        clearFields(false);
                        removeMetadataFromPossibleSelection();
                    }

                    MetadataVO metadataVO = (MetadataVO) metadataComboBox.getValue();

                    if(metadataVO!= null && metadataVO.getType() == MetadataValueType.REFERENCE) {
                        referenceMetadataDisplayComboBox.setRequired(true);
                        referenceMetadataDisplayComboBox.setVisible(true);
                    } else {
                        referenceMetadataDisplayComboBox.setVisible(false);
                        referenceMetadataDisplayComboBox.setRequired(false);
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
        table.setColumnHeader(SummaryColumnContainer.METADATA_VO, $("SummaryColumnViewImpl.metadataHeader"));
        table.setColumnHeader(SummaryColumnContainer.PREFIX, $("SummaryColumnViewImpl.prefixHeader"));
        table.setColumnHeader(SummaryColumnContainer.DISPLAY_CONDITION, $("SummaryColumnViewImpl.displayConditionHeader"));
        table.setColumnHeader(SummaryColumnContainer.REFERENCE_METADATA_DISPLAY, $("SummaryColumnViewImpl.referenceMetadataDisplay"));
        table.setColumnHeader(SummaryColumnContainer.MODIFY, "");
        table.setColumnHeader(SummaryColumnContainer.DELETE, "");

        prefix = new BaseTextField($("SummaryColumnViewImpl.prefix"));
        displayCondition = new ListOptionGroup($("SummaryColumnViewImpl.displayCondition"));
        displayCondition.setRequired(true);
        displayCondition.addItem(SummaryColumnParams.DisplayCondition.COMPLETED);
        displayCondition.addItem(SummaryColumnParams.DisplayCondition.ALWAYS);

        referenceMetadataDisplayComboBox = new ComboBox($("SummaryColumnViewImpl.displayConditionHeader"));
        referenceMetadataDisplayComboBox.setImmediate(true);
        referenceMetadataDisplayComboBox.setTextInputAllowed(false);
        referenceMetadataDisplayComboBox.setVisible(false);
        referenceMetadataDisplayComboBox.addItem(SummaryColumnParams.ReferenceMetadataDisplay.CODE);
        referenceMetadataDisplayComboBox.addItem(SummaryColumnParams.ReferenceMetadataDisplay.TITLE);

        BaseForm<SummaryColumnParams> baseForm = new BaseForm<SummaryColumnParams>(new SummaryColumnParams(), this, metadataComboBox, prefix, displayCondition, referenceMetadataDisplayComboBox) {
            @Override
            protected void saveButtonClick(final SummaryColumnParams viewObject) {
                if(!presenter.isReindextionFlag() && presenter.isThereAModification(viewObject)) {
                    ConfirmDialog.show(
                            UI.getCurrent(),
                            $("SummaryColumnViewImpl.save.title"),
                            $("SummaryColumnViewImpl.save.message"),
                            $("Ok"),
                            $("cancel"),
                            new ConfirmDialog.Listener() {
                                @Override
                                public void onClose(ConfirmDialog dialog) {
                                    if (dialog.isConfirmed()) {
                                        addConfiguration(viewObject);
                                    }
                                }
                            });

                } else {
                    addConfiguration(viewObject);
                }
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

    private void addConfiguration(SummaryColumnParams viewObject) {
        SummaryColumnVO summaryColumnVO = summaryColumnParamsToSummaryVO(viewObject);
        if(modifingSummaryColumnVO != null) {
            presenter.modifyMetadataForSummaryColumn(viewObject);
            List<SummaryColumnVO> summaryColumnVOList = presenter.summaryColumnVOList();
            int index = presenter.findMetadataIndex(summaryColumnVOList, viewObject.getMetadataVO().getCode());
            summaryColumnDataProvider.removeSummaryColumnVO(index);
            summaryColumnDataProvider.addSummaryColumnVO(index, summaryColumnVO);
        } else {
            presenter.addMetadaForSummary(viewObject);
            summaryColumnDataProvider.addSummaryColumnVO(summaryColumnVO);
        }

        summaryColumnDataProvider.fireDataRefreshEvent();
        clearFields();
        removeMetadataFromPossibleSelection();
    }

    private void refreshMetadataCombox() {
        metadataComboBox.removeAllItems();
        for(MetadataVO metadataVO : presenter.getMetadatas()) {
            if(metadataVO.getType() != MetadataValueType.STRUCTURE && !metadataVO.getLocalCode().equals("summary")) {
                metadataComboBox.addItem(metadataVO);
            }
        }

        removeMetadataFromPossibleSelection();
    }


    private SummaryColumnVO summaryColumnParamsToSummaryVO(SummaryColumnParams summaryColumnParams) {
        SummaryColumnVO summaryColumnVO = new SummaryColumnVO();
        summaryColumnVO.setMetadataVO(summaryColumnParams.getMetadataVO());
        summaryColumnVO.setAlwaysShown(summaryColumnParams.getDisplayCondition() == SummaryColumnParams.DisplayCondition.ALWAYS);
        summaryColumnVO.setPrefix(summaryColumnParams.getPrefix());
        SummaryColumnParams.ReferenceMetadataDisplay referenceMetadataDisplay = summaryColumnParams.getReferenceMetadataDisplay();
        if(referenceMetadataDisplay != null) {
            summaryColumnVO.setReferenceMetadataDisplay((referenceMetadataDisplay.ordinal()));
        }

        return summaryColumnVO;
    }


    private void removeMetadataFromPossibleSelection() {
        List<SummaryColumnVO> summaryColumnVOList = presenter.summaryColumnVOList();

        for(SummaryColumnVO summaryColumnVO : summaryColumnVOList) {
            this.metadataComboBox.removeItem(summaryColumnVO.getMetadataVO());
        }
    }

    private void clearFields() {
        this.clearFields(true);
    }

    private void clearFields(boolean clearMetadataTo) {
        this.displayCondition.setValue(null);
        this.prefix.setValue("");
        if (clearMetadataTo) {
            this.metadataComboBox.setValue(null);
        }
        this.referenceMetadataDisplayComboBox.setValue(null);
        this.modifingSummaryColumnVO = null;
    }

    @Override
    public void alterSummaryMetadata(SummaryColumnVO summaryColumnVO) {
        this.metadataComboBox.setValue(null);
        this.metadataComboBox.addItem(summaryColumnVO.getMetadataVO());
        this.metadataComboBox.setValue(summaryColumnVO.getMetadataVO());
        this.prefix.setValue(summaryColumnVO.getPrefix());
        if(summaryColumnVO.isAlwaysShown()) {
            this.displayCondition.setValue(SummaryColumnParams.DisplayCondition.ALWAYS);
        } else {
            this.displayCondition.setValue(SummaryColumnParams.DisplayCondition.COMPLETED);
        }
        if(summaryColumnVO.getReferenceMetadataDisplay() != null) {
            this.referenceMetadataDisplayComboBox.setValue(SummaryColumnParams.ReferenceMetadataDisplay
                    .fromInteger(summaryColumnVO.getReferenceMetadataDisplay()));
        }
        this.modifingSummaryColumnVO = summaryColumnVO;
    }

    public void deleteSummaryMetadata(SummaryColumnVO summaryColumnVO) {
        this.presenter.deleteMetadataForSummaryColumn(summaryColumnVO);
        this.summaryColumnDataProvider.removeSummaryColumnVO(summaryColumnVO);
        refreshMetadataCombox();
        this.summaryColumnDataProvider.fireDataRefreshEvent();
    }

    public void deleteRow(final SummaryColumnVO columnVO) {

        String message = $("SummaryColumnViewImpl.deleteConfirmationMesssage");
        if(!presenter.isReindextionFlag()) {
            message = $("SummaryColumnViewImpl.save.message") + " " + message;
        }



        ConfirmDialog.show(
                UI.getCurrent(),
                $("SummaryColumnViewImpl.deleteConfirmation"),
                message,
                $("Ok"),
                $("cancel"),
                new ConfirmDialog.Listener() {
                    @Override
                    public void onClose(ConfirmDialog dialog) {
                        if(dialog.isConfirmed()) {
                            deleteSummaryMetadata(columnVO);
                        }
                    }
                });
    }


    @Override
    public boolean showReindexationWarningIfRequired(ConfirmDialog.Listener confirmDialogListener) {
        if(presenter.isReindextionFlag()) {
            ConfirmDialog.show(
                    UI.getCurrent(),
                    $("SummaryColumnViewImpl.save.title"),
                    $("SummaryColumnViewImpl.save.message"),
                    $("Ok"),
                    $("cancel"),
                    confirmDialogListener);
            return true;
        } else {
            return false;
        }

    }
}
