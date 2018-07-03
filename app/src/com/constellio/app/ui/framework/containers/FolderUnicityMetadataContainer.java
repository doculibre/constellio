package com.constellio.app.ui.framework.containers;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.FolderUnicityVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.framework.data.FolderUnicityDataProvider;
import com.constellio.app.ui.framework.data.SummaryColumnDataProvider;
import com.constellio.app.ui.pages.unicitymetadataconf.FolderUnicityMetadataView;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.server.ThemeResource;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class FolderUnicityMetadataContainer extends DataContainer<FolderUnicityDataProvider> {
    public static final String UP = "up";
    public static final String DOWN = "down";
    public static final String METADATA_VO = "metadataVo";
    public static final String DELETE = "delete";

    FolderUnicityMetadataView view;

    public FolderUnicityMetadataContainer(FolderUnicityDataProvider dataProvider, FolderUnicityMetadataView folderUnicityMetadataView) {
        super(dataProvider);
        this.view = folderUnicityMetadataView;
    }

    @Override
    protected void populateFromData(FolderUnicityDataProvider dataProvider) {
        for (FolderUnicityVO summaryColumnVO : dataProvider.getFolderUnicityVOs()) {
            addItem(summaryColumnVO);
        }
    }

    @Override
    protected Collection<?> getOwnContainerPropertyIds() {
        List<String> containerPropertyIds = new ArrayList<>();
        containerPropertyIds.add(UP);
        containerPropertyIds.add(DOWN);
        containerPropertyIds.add(METADATA_VO);
        containerPropertyIds.add(DELETE);


        return containerPropertyIds;
    }

    @Override
    protected Class<?> getOwnType(Object propertyId) {
        Class<?> type;
        if (UP.equals(propertyId)) {
            type = BaseButton.class;
        } else if (DOWN.equals(propertyId)) {
            type = BaseButton.class;
        } else if (METADATA_VO.equals(propertyId)) {
            type = MetadataVO.class;
        } else if (DELETE.equals(propertyId)) {
            type = BaseButton.class;
        } else {
            throw new IllegalArgumentException("Invalid propertyId : " + propertyId);
        }

        return type;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected Property<?> getOwnContainerProperty(Object itemId, Object propertyId) {
        final FolderUnicityVO folderUnivityItemId = (FolderUnicityVO) itemId;
        final FolderUnicityDataProvider folderUnicityDataProvider = getDataProvider();
        Object value;

        if (UP.equals(propertyId)) {

            value = new IconButton(new ThemeResource("images/icons/actions/arrow_up_blue.png"), $("UP"), true) {

                @Override
                protected void buttonClick(ClickEvent event) {
                    if(!view.getSummaryColumnPresenter().isReindextionFlag()) {
                        view.showReindexationWarningIfRequired(new ConfirmDialog.Listener() {
                            @Override
                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    moveRowUp(folderUnicityDataProvider, folderUnivityItemId);
                                }
                            }
                        });
                    } else {
                        moveRowUp(folderUnicityDataProvider, folderUnivityItemId);
                    }
                }
            };
        } else if (DOWN.equals(propertyId)) {
            value = new IconButton(new ThemeResource("images/icons/actions/arrow_down_blue.png"), $("DOWN"), true) {
                @Override
                protected void buttonClick(ClickEvent event) {
                    if(!view.getSummaryColumnPresenter().isReindextionFlag()) {
                        view.showReindexationWarningIfRequired(new ConfirmDialog.Listener() {
                            @Override
                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    moveRowDown(folderUnicityDataProvider, folderUnivityItemId);
                                }
                            }
                        });
                    } else {
                        moveRowDown(folderUnicityDataProvider, folderUnivityItemId);
                    }
                }
            };
        } else if (METADATA_VO.equals(propertyId)) {
            value = folderUnivityItemId.getMetadataVO();
        } else if (DELETE.equals(propertyId)) {
            value = new IconButton(new ThemeResource("images/icons/actions/delete.png"), $("delete"), true) {
                @Override
                protected void buttonClick(ClickEvent event) {
                    view.deleteRow(folderUnivityItemId);
                }
            };
        } else {
            throw new IllegalArgumentException("Invalid propertyId : " + propertyId);
        }
        Class<?> type = getType(propertyId);
        return new ObjectProperty(value, type);
    }

    private void moveRowUp(FolderUnicityDataProvider folderUnicityDataProvider, FolderUnicityVO summaryColumnVOItemId) {
        List<FolderUnicityVO> summaryColumnVOS = folderUnicityDataProvider.getFolderUnicityVOs();
        int index = summaryColumnVOS.indexOf(summaryColumnVOItemId);

        if(index <= 0) {
            return;
        }

        view.getSummaryColumnPresenter().moveMetadataUp(summaryColumnVOItemId.getMetadataVO().getCode());
        FolderUnicityVO summaryColumnVO = folderUnicityDataProvider.removeFolderUnicityVO(index);
        folderUnicityDataProvider.addFolderUnicityVO(index - 1, summaryColumnVO);
        folderUnicityDataProvider.fireDataRefreshEvent();
    }

    private void moveRowDown(FolderUnicityDataProvider summaryColumnDataProvider, FolderUnicityVO summaryColumnVOItemId) {
        List<FolderUnicityVO> summaryColumnVOS = summaryColumnDataProvider.getFolderUnicityVOs();
        int index = summaryColumnVOS.indexOf(summaryColumnVOItemId);

        if(index >= (summaryColumnVOS.size() - 1)) {
            return;
        }

        view.getSummaryColumnPresenter().moveMetadataDown(summaryColumnVOItemId.getMetadataVO().getCode());
        FolderUnicityVO summaryColumnVO = summaryColumnDataProvider.removeFolderUnicityVO(index);
        summaryColumnDataProvider.addFolderUnicityVO(index + 1, summaryColumnVO);
        summaryColumnDataProvider.fireDataRefreshEvent();
    }


}
