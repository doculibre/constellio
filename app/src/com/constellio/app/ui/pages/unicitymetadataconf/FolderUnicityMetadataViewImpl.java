package com.constellio.app.ui.pages.unicitymetadataconf;

import com.constellio.app.ui.entities.FolderUnicityVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.FolderUnicityMetadataContainer;
import com.constellio.app.ui.framework.data.FolderUnicityDataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.*;

import static com.constellio.app.ui.i18n.i18n.$;

public class FolderUnicityMetadataViewImpl extends BaseViewImpl implements FolderUnicityMetadataView {

    private FolderUnicityMetadataPresenter presenter;

    @PropertyId("metadataVO")
    private ComboBox metadataComboBox;

    private FolderUnicityDataProvider summaryColumnDataProvider;

    private Table table;
    private FolderUnicityMetadataContainer folderUnicityMetadataContainer;

    public FolderUnicityMetadataViewImpl() {
        presenter = new FolderUnicityMetadataPresenter(this);
    }

    @Override
    protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
        Map<String, String> params = ParamUtils.getParamsMap(event.getParameters());
        presenter.setSchemaCode(params.get("schemaCode"));
        presenter.setParameters(params);
    }

    @Override
    public String getTitle() {
        return $("SummaryColumnViewImpl.title");
    }

    @Override
    public FolderUnicityMetadataPresenter getSummaryColumnPresenter() {
        return presenter;
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        metadataComboBox = new BaseComboBox($("SummaryColumnViewImpl.metadata"));

        List<FolderUnicityVO> summaryColumnVOList = presenter.folderUnicityVOList();

        metadataComboBox.setTextInputAllowed(false);
        metadataComboBox.setRequired(true);
        metadataComboBox.setImmediate(true);

        refreshMetadataCombox();

        table = new BaseTable(getClass().getName());

        summaryColumnDataProvider = new FolderUnicityDataProvider(summaryColumnVOList);
        folderUnicityMetadataContainer = new FolderUnicityMetadataContainer(summaryColumnDataProvider, this);

        table.setContainerDataSource(folderUnicityMetadataContainer);
        table.setColumnHeader(FolderUnicityMetadataContainer.UP, "");
        table.setColumnHeader(FolderUnicityMetadataContainer.DOWN, "");
        table.setColumnHeader(FolderUnicityMetadataContainer.METADATA_VO, $("SummaryColumnViewImpl.metadataHeader"));
        table.setColumnHeader(FolderUnicityMetadataContainer.DELETE, "");

        BaseForm<FolderUnicityMetadataParams> baseForm = new BaseForm<FolderUnicityMetadataParams>(new FolderUnicityMetadataParams(), this, metadataComboBox) {
            @Override
            protected void saveButtonClick(final FolderUnicityMetadataParams viewObject) {
                if(!presenter.isReindextionFlag()) {
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
            protected void cancelButtonClick(FolderUnicityMetadataParams viewObject) {
                // button not visible so no action here.
            }

            @Override
            protected boolean isCancelButtonVisible() {
                return false;
            }
        };

        baseForm.getSaveButton().setCaption($("add"));

        this.table.setWidth("100%");

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.addComponent(baseForm);
        verticalLayout.addComponent(table);

        return verticalLayout;
    }

    private void addConfiguration(FolderUnicityMetadataParams viewObject) {
        FolderUnicityVO folderUnicityVO = summaryColumnParamsToSummaryVO(viewObject);

        presenter.addMetadaForUnicity(viewObject);
        summaryColumnDataProvider.addFolderUnicityVO(folderUnicityVO);

        summaryColumnDataProvider.fireDataRefreshEvent();
        clearFields();
        removeMetadataFromPossibleSelection();
    }


    private void clearFields() {
        this.metadataComboBox.setValue(null);
    }

    private void refreshMetadataCombox() {
        metadataComboBox.removeAllItems();

        List<MetadataVO> metadataVOS = presenter.getMetadatas();

        Collections.sort(metadataVOS, new Comparator<MetadataVO>() {
            @Override
            public int compare(MetadataVO o1, MetadataVO o2) {
                Locale currentLocale = getSessionContext().getCurrentLocale();
                return o1.getLabel(currentLocale).compareTo(o2.getLabel(currentLocale));
            }
        });

        for(MetadataVO metadataVO : metadataVOS) {
            if(metadataVO.getType() != MetadataValueType.STRUCTURE && !metadataVO.getLocalCode().equals("summary")) {
                metadataComboBox.addItem(metadataVO);
            }
        }

        removeMetadataFromPossibleSelection();
    }


    private FolderUnicityVO summaryColumnParamsToSummaryVO(FolderUnicityMetadataParams folderUnicityMetadataParams) {
        FolderUnicityVO summaryColumnVO = new FolderUnicityVO();
        summaryColumnVO.setMetadataVO(folderUnicityMetadataParams.getMetadataVO());

        return summaryColumnVO;
    }


    private void removeMetadataFromPossibleSelection() {
        List<FolderUnicityVO> summaryColumnVOList = presenter.folderUnicityVOList();

        for(FolderUnicityVO summaryColumnVO : summaryColumnVOList) {
            this.metadataComboBox.removeItem(summaryColumnVO.getMetadataVO());
        }
    }


    public void deleteSummaryMetadata(FolderUnicityVO summaryColumnVO) {
        this.presenter.deleteMetadataForSummaryColumn(summaryColumnVO);
        this.summaryColumnDataProvider.removeFolderUnicityVO(summaryColumnVO);
        refreshMetadataCombox();
        this.summaryColumnDataProvider.fireDataRefreshEvent();
    }

    public void deleteRow(final FolderUnicityVO columnVO) {

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
