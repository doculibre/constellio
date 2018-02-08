package com.constellio.app.modules.rm.ui.pages.pdf.table;

import com.constellio.app.modules.rm.pdfgenerator.FileStatus;
import com.constellio.app.ui.framework.data.AbstractDataProvider;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PdfStatusMessageProvider extends AbstractDataProvider implements PdfStatusDataProvider<FileStatus> {
    private List<FileStatus> messages;

    @Override
    public List<FileStatus> listPdfStatus() {
        return ObjectUtils.defaultIfNull(messages, new ArrayList<FileStatus>());
    }

    @Override
    public Collection<?> getOwnContainerPropertyIds() {
        return Arrays.asList("id");
    }

    @Override
    public Class<?> getOwnType(Object propertyId) {
        return FileStatus.class;
    }

    @Override
    public Object getOwnValue(Object itemId, Object propertyId) {
        return ((FileStatus)itemId).toString();
    }

    public void setMessages(List<FileStatus> messages) {
        this.messages = messages;

        fireDataRefreshEvent();
    }
}
