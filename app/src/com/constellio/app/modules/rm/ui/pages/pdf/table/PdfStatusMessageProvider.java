package com.constellio.app.modules.rm.ui.pages.pdf.table;

import com.constellio.app.ui.framework.data.AbstractDataProvider;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PdfStatusMessageProvider extends AbstractDataProvider implements PdfStatusDataProvider<String> {
    private List<String> messages;

    @Override
    public List<String> listPdfStatus() {
        return ObjectUtils.defaultIfNull(messages, new ArrayList<String>());
    }

    @Override
    public Collection<?> getOwnContainerPropertyIds() {
        return Arrays.asList("id");
    }

    @Override
    public Class<?> getOwnType(Object propertyId) {
        return String.class;
    }

    @Override
    public Object getOwnValue(Object itemId, Object propertyId) {
        return (String)itemId;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;

        fireDataRefreshEvent();
    }
}
