package com.constellio.app.modules.rm.ui.pages.pdf;

import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusDataProvider;
import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusMessageProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;

import java.util.ArrayList;
import java.util.List;

public class PdfStatusViewPresenter extends BasePresenter<PdfStatusView> {
    private PdfStatusDataProvider<?> dataProvider;

    public PdfStatusViewPresenter(final PdfStatusView view) {
        super(view);
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }

    public PdfStatusDataProvider<?> getDataProvider() {
        if(dataProvider == null) {
            dataProvider = new PdfStatusMessageProvider();
        }

        return dataProvider;
    }
}
