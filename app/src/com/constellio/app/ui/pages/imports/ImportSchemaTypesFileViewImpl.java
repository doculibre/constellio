package com.constellio.app.ui.pages.imports;

import static com.constellio.app.ui.i18n.i18n.$;

public class ImportSchemaTypesFileViewImpl extends ImportFileViewImpl implements ImportFileView{
    @Override
    protected void initPresenter() {
        presenter = new ImportSchemaTypesFilePresenter(this);
    }

    @Override
    protected String getTitle() {
        return $("ImportSchemaTypesFileViewImpl.viewTitle");
    }
}
