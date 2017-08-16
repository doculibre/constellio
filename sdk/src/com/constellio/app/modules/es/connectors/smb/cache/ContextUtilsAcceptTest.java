package com.constellio.app.modules.es.connectors.smb.cache;

import com.constellio.app.modules.es.connectors.smb.service.SmbModificationIndicator;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ContextUtilsAcceptTest {

    @Test
    public void whenValidDocumentEqualsThenTrue() {
        SmbModificationIndicator firstIndicator = new SmbModificationIndicator("zzz", 455.0D, 67L);
        SmbModificationIndicator secondIndicator = new SmbModificationIndicator("zzz", 455.0D, 67L);
        assertThat(ContextUtils.equals(firstIndicator, secondIndicator, false)).isTrue();

        firstIndicator = new SmbModificationIndicator("zzz", 455.0D, 67L);
        firstIndicator.setParentId("something");
        secondIndicator = new SmbModificationIndicator("zzz", 455.0D, 67L);
        assertThat(ContextUtils.equals(firstIndicator, secondIndicator, false)).isTrue();
    }

    @Test
    public void whenInvalidDocumentEqualsThenTrue() {
        SmbModificationIndicator firstIndicator = new SmbModificationIndicator("zzz", 455.0D, 67L);
        SmbModificationIndicator secondIndicator = new SmbModificationIndicator("different", 455.0D, 67L);
        assertThat(ContextUtils.equals(firstIndicator, secondIndicator, false)).isFalse();

        firstIndicator = new SmbModificationIndicator("zzz", 455.0D, 67L);
        secondIndicator = new SmbModificationIndicator("zzz", 456.0D, 67L);
        assertThat(ContextUtils.equals(firstIndicator, secondIndicator, false)).isFalse();

        firstIndicator = new SmbModificationIndicator("zzz", 455.0D, 99L);
        secondIndicator = new SmbModificationIndicator("different", 455.0D, 67L);
        assertThat(ContextUtils.equals(firstIndicator, secondIndicator, false)).isFalse();
    }

    @Test
    public void whenValidFolderEqualsThenTrue() {
        SmbModificationIndicator firstIndicator = new SmbModificationIndicator("", 0.0D, 67L);
        SmbModificationIndicator secondIndicator = new SmbModificationIndicator("zzz", 22.0D, 67L);
        assertThat(ContextUtils.equals(firstIndicator, secondIndicator, true)).isTrue();
    }

    @Test
    public void whenInvalidFolderEqualsThenTrue() {
        SmbModificationIndicator firstIndicator = new SmbModificationIndicator("", 0.0D, 67L);
        SmbModificationIndicator secondIndicator = new SmbModificationIndicator("zzz", 22.0D, 68L);
        assertThat(ContextUtils.equals(firstIndicator, secondIndicator, true)).isFalse();
    }
}
