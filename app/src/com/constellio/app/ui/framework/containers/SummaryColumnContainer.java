package com.constellio.app.ui.framework.containers;

import com.constellio.app.modules.rm.ui.pages.folder.SummaryColumnVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.data.SummaryColumnDataProvider;
import com.constellio.app.ui.pages.summarycolumn.SummaryColumnParams;
import com.constellio.app.ui.pages.summarycolumn.SummaryColumnView;
import com.constellio.app.ui.pages.summarycolumn.SummaryColumnViewImpl;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SummaryColumnContainer extends DataContainer<SummaryColumnDataProvider> {
    public static final String UP = "up";
    public static final String DOWN = "down";
    public static final String METADATA_VO = "metadataVo";
    public static final String PREFIX = "prefix";
    public static final String DISPLAY_CONDITION = "displayCondition";
    public static final String MODIFY = "modify";
    public static final String DELETE = "delete";

    SummaryColumnView summaryColumnView;


    public SummaryColumnContainer(SummaryColumnDataProvider dataProvider, SummaryColumnViewImpl summaryColumnPresenter) {
        super(dataProvider);
        this.summaryColumnView = summaryColumnPresenter;
    }

    @Override
    protected void populateFromData(SummaryColumnDataProvider dataProvider) {
        for (SummaryColumnVO summaryColumnVO : dataProvider.getSummaryColumnVOs()) {
            addItem(summaryColumnVO);
        }
    }

    @Override
    protected Collection<?> getOwnContainerPropertyIds() {
        List<String> containerPropertyIds = new ArrayList<>();
        containerPropertyIds.add(UP);
        containerPropertyIds.add(DOWN);
        containerPropertyIds.add(METADATA_VO);
        containerPropertyIds.add(PREFIX);
        containerPropertyIds.add(DISPLAY_CONDITION);
        containerPropertyIds.add(MODIFY);
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
        } else if (PREFIX.equals(propertyId)) {
            type = String.class;
        } else if (DISPLAY_CONDITION.equals(propertyId)) {
            type = SummaryColumnParams.DisplayCondition.class;
        } else if (MODIFY.equals(propertyId)) {
            type = BaseButton.class;
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
        final SummaryColumnVO summaryColumnVOItemId = (SummaryColumnVO) itemId;
        final SummaryColumnDataProvider summaryColumnDataProvider = getDataProvider();
        Object value;

        if (UP.equals(propertyId)) {
            value = new BaseButton("UP") {
                @Override
                protected void buttonClick(ClickEvent event) {
                    List<SummaryColumnVO> summaryColumnVOS = summaryColumnDataProvider.getSummaryColumnVOs();
                    int index = summaryColumnVOS.indexOf(summaryColumnVOItemId);

                    if(index <= 0) {
                        return;
                    }

                    SummaryColumnVO summaryColumnVO = summaryColumnDataProvider.removeSummaryColumnVO(index);
                    summaryColumnDataProvider.addSummaryColumnVO(index - 1, summaryColumnVO);
                    summaryColumnDataProvider.fireDataRefreshEvent();
                }
            };
        } else if (DOWN.equals(propertyId)) {
            value = new BaseButton("DOWN") {
                @Override
                protected void buttonClick(ClickEvent event) {
                    List<SummaryColumnVO> summaryColumnVOS = summaryColumnDataProvider.getSummaryColumnVOs();
                    int index = summaryColumnVOS.indexOf(summaryColumnVOItemId);

                    if(index >= (summaryColumnVOS.size() - 1)) {
                        return;
                    }

                    SummaryColumnVO summaryColumnVO = summaryColumnDataProvider.removeSummaryColumnVO(index);
                    summaryColumnDataProvider.addSummaryColumnVO(index + 1, summaryColumnVO);
                    summaryColumnDataProvider.fireDataRefreshEvent();
                }
            };
        } else if (METADATA_VO.equals(propertyId)) {
            value = summaryColumnVOItemId.getMetadataVO();
        } else if (PREFIX.equals(propertyId)) {
            value = summaryColumnVOItemId.getPrefix();
        } else if (DISPLAY_CONDITION.equals(propertyId)) {
            value = summaryColumnVOItemId.isAlwaysShown() ? SummaryColumnParams.DisplayCondition.ALWAYS : SummaryColumnParams.DisplayCondition.COMPLETED;
        } else if (MODIFY.equals(propertyId)) {
            value = new BaseButton("modify") {
                @Override
                protected void buttonClick(ClickEvent event) {
                    summaryColumnView.alterSummaryMetadata(summaryColumnVOItemId);
                }
            };
        } else if (DELETE.equals(propertyId)) {
            value = new BaseButton("delete") {
                @Override
                protected void buttonClick(ClickEvent event) {
                    summaryColumnView.deleteSummaryMetadata(summaryColumnVOItemId);
                    summaryColumnDataProvider.fireDataRefreshEvent();
                }
            };
        } else {
            throw new IllegalArgumentException("Invalid propertyId : " + propertyId);
        }
        Class<?> type = getType(propertyId);
        return new ObjectProperty(value, type);
    }


}
