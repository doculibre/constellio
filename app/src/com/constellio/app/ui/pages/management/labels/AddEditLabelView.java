package com.constellio.app.ui.pages.management.labels;

import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.LabelVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;
import com.constellio.app.ui.pages.viewGroups.PrintableViewGroup;

import java.util.List;

public interface AddEditLabelView extends BaseView, AdminViewGroup{

    public void setLabels(List<LabelVO> list);

    public void addLabels(LabelVO... items);

}
