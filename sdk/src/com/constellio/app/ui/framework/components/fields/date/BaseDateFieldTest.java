package com.constellio.app.ui.framework.components.fields.date;

import com.vaadin.data.util.converter.Converter;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class BaseDateFieldTest {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final Date DOCULIBRE_BIRTHDATE = LocalDate.parse("2005-12-14").toDate();

    @Test
    public void givenDateStringInTheConfiguredFormatThenStringParsedAndCorrectDateIsReturned() throws Exception {
        assertThat(BaseDateField.handleUnparsableDateString("2005-12-14", DATE_FORMAT)).isEqualTo(DOCULIBRE_BIRTHDATE);
    }

    @Test
    public void givenDateStringWithoutAnySeparatorAsInTheConfiguredFormatThenStringParsedAndCorrectDateIsReturned() throws Exception {
        assertThat(BaseDateField.handleUnparsableDateString("20051214", DATE_FORMAT)).isEqualTo(DOCULIBRE_BIRTHDATE);
    }

    @Test
    public void givenDateStringInTheConfiguredFormatButInReverseOrderThenStringParsedAndCorrectDateIsReturned() throws Exception {
        assertThat(BaseDateField.handleUnparsableDateString("14-12-2005", DATE_FORMAT)).isEqualTo(DOCULIBRE_BIRTHDATE);
    }

    @Test
    public void givenDateStringWithoutAnySeparatorAsInTheConfiguredFormatButInReverseOrderThenStringParsedAndCorrectDateIsReturned() throws Exception {
        assertThat(BaseDateField.handleUnparsableDateString("14122005", DATE_FORMAT)).isEqualTo(DOCULIBRE_BIRTHDATE);
    }

    @Test
    public void givenDateStringInTheConfiguredFormatButInReverseOrderAndWithDifferentSeparatorThenStringParsedAndCorrectDateIsReturned() throws Exception {
        assertThat(BaseDateField.handleUnparsableDateString("14.12/2005", DATE_FORMAT)).isEqualTo(DOCULIBRE_BIRTHDATE);
    }

    @Test
    public void givenUnexceptedDateStringThenExceptionIsThrown() throws Exception {
        try {
            BaseDateField.handleUnparsableDateString("14..12/2005", DATE_FORMAT);
            failBecauseExceptionWasNotThrown(Converter.ConversionException.class);
        } catch (final Exception e) {
            assertThat(e).isExactlyInstanceOf(Converter.ConversionException.class);
        }

        try {
            BaseDateField.handleUnparsableDateString("1412205", DATE_FORMAT);
            failBecauseExceptionWasNotThrown(Converter.ConversionException.class);
        } catch (final Exception e) {
            assertThat(e).isExactlyInstanceOf(Converter.ConversionException.class);
        }
    }
}