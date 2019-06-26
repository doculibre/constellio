package com.constellio.app.services.schemas.bulkImport.data.excel;

import com.constellio.app.services.schemas.bulkImport.data.ImportData;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataIterator;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataIteratorRuntimeException;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataIteratorTest;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class Excel2007FileImportDataIteratorAcceptanceTest extends ImportDataIteratorTest {

    ImportDataIterator importDataIterator;
    Excel2007ImportDataProvider excelImportDataProvider;

    @Before
    public void setUp() {
        excelImportDataProvider = new Excel2007ImportDataProvider(getTestResourceFile("data.xlsx"));
        excelImportDataProvider.initialize();
    }

    @Test
    public void whenIteratingWithImportAsLegacyIdIsTrue()
            throws Exception {

        importDataIterator = excelImportDataProvider.newDataIterator("option-importAsLegacyId-true");
        assertThat(excelImportDataProvider.size("option-importAsLegacyId-true")).isEqualTo(5);
        assertThat(importDataIterator.getOptions().isImportAsLegacyId()).isTrue();

        importDataIterator = excelImportDataProvider.newDataIterator("datas");
        assertThat(importDataIterator.getOptions().isImportAsLegacyId()).isTrue();
    }

    @Test
    public void whenIteratingWithImportAsLegacyIdIsFalse()
            throws Exception {

        importDataIterator = excelImportDataProvider.newDataIterator("option-importAsLegacyId-false");

        assertThat(excelImportDataProvider.size("option-importAsLegacyId-false")).isEqualTo(5);
        assertThat(importDataIterator.getOptions().isImportAsLegacyId()).isFalse();
    }

    @Test
    public void whenIteratingThen()
            throws Exception {

        LocalDateTime localDateTime = new LocalDateTime(2010, 04, 29, 9, 31, 46, 235);
        LocalDate localDate = new LocalDate(2010, 05, 16);
        LocalDate anOtherLocalDate = new LocalDate(2010, 05, 15);

        importDataIterator = excelImportDataProvider.newDataIterator("datas");

        assertThat(excelImportDataProvider.size("datas")).isEqualTo(5);

        assertThat(importDataIterator.next()).has(id("1")).has(index(2)).has(schema("default"))
                .has(noField("id")).has(noField("schema"))
                .has(field("title", "Ze title"))
                .has(field("createdOn", localDateTime))
                .has(field("referenceToAnotherSchema", "42"))
                .has(field("zeEmptyField", null))
                .has(field("modifyOn", emptyList()));

        assertThat(importDataIterator.next()).has(id("42")).has(index(3)).has(schema("default"))
                .has(noField("id")).has(noField("schema"))
                .has(field("title", "Another title"))
                .has(field("referenceToAThirdSchema", "666"))
                .has(field("zeEmptyField", null))
                .has(field("modifyOn", emptyList()));

        assertThat(importDataIterator.next()).has(id("666")).has(index(4)).has(schema("customSchema"))
                .has(noField("id")).has(noField("schema"))
                .has(field("createdOn", localDateTime))
                .has(field("modifyOn", asList(anOtherLocalDate, localDate)))
                .has(field("keywords", asList("keyword1", "keyword2")))
                .has(field("zeNullField", null))
                .has(field("title", "A third title"));

        assertThat(importDataIterator.next()).has(id("32")).has(field("referenceToAThirdSchema", "10-9"));
        assertThat(importDataIterator.next()).has(id("8-10"));
    }

    @Test
    public void whenIteratingWithBlankLineThen()
            throws Exception {

        LocalDateTime localDateTime = new LocalDateTime(2010, 04, 29, 9, 31, 46, 235);
        LocalDate localDate = new LocalDate(2010, 05, 16);
        LocalDate anOtherLocalDate = new LocalDate(2010, 05, 15);

        importDataIterator = excelImportDataProvider.newDataIterator("content");

        assertThat(excelImportDataProvider.size("content")).isEqualTo(3);

        assertThat(importDataIterator.next()).has(id("1")).has(index(2)).has(schema("default"))
                .has(noField("id")).has(noField("schema"))
                .has(field("title", "Ze title"))
                .has(field("createdOn", localDateTime))
                .has(field("referenceToAnotherSchema", "42"))
                .has(field("zeEmptyField", null));

        assertThat(importDataIterator.next()).has(id("666")).has(index(4)).has(schema("customSchema"))
                .has(noField("id")).has(noField("schema"))
                .has(field("createdOn", localDateTime))
                .has(field("modifyOn", asList(anOtherLocalDate, localDate)))
                .has(field("keywords", asList("keyword1", "keyword2")))
                .has(field("zeNullField", null))
                .has(field("title", "A third title"));

        assertThat(importDataIterator.next()).has(id("35")).has(index(7)).has(schema("default"))
                .has(noField("id")).has(noField("schema"))
                .has(field("createdOn", null))
                .has(field("modifyOn", emptyList()))
                .has(field("title", "There is also a title here"))
                .has(field("referenceToAThirdSchema", "42"));
    }

    @Test
    public void whenIteratingWithDateAndDateTimeCells()
            throws Exception {

        LocalDate localDate = new LocalDate(2010, 05, 16);
        LocalDate localDateFirst = new LocalDate(2010, 12, 28);
        LocalDate localDateSecond = new LocalDate(2015, 02, 25);
        LocalDate localDateThird = new LocalDate(2010, 04, 29);
        LocalDate anOtherLocalDate = new LocalDate(2010, 05, 15);
        LocalDateTime localDateTime = new LocalDateTime(2010, 04, 29, 9, 31, 46, 235);
        LocalDateTime localDateTimeSecond = new LocalDateTime(2015, 04, 12, 10, 31, 46, 265);

        importDataIterator = excelImportDataProvider.newDataIterator("dates");

        assertThat(excelImportDataProvider.size("dates")).isEqualTo(4);

        assertThat(importDataIterator.next()).has(id("1")).has(index(2)).has(schema("default"))
                .has(noField("id")).has(noField("schema"))
                .has(field("title", "Ze title"))
                .has(field("createdOn", localDateThird))
                .has(field("createdOnDateTime", localDateTimeSecond))
                .has(field("referenceToAnotherSchema", "42"))
                .has(field("zeEmptyField", null));

        assertThat(importDataIterator.next()).has(id("666")).has(index(4)).has(schema("customSchema"))
                .has(noField("id")).has(noField("schema"))
                .has(field("createdOn", localDateFirst))
                .has(field("createdOnDateTime", localDateTime))
                .has(field("modifyOn", asList(anOtherLocalDate, localDate)))
                .has(field("keywords", asList("keyword1", "keyword2")))
                .has(field("zeNullField", null))
                .has(field("title", "A third title"));

        assertThat(importDataIterator.next()).has(id("25")).has(index(6)).has(schema("anotherCustomSchema"))
                .has(noField("id")).has(noField("schema"))
                .has(field("createdOn", localDateSecond))
                .has(field("zeNullField", null))
                .has(field("title", "title"));

        ImportData lastData = importDataIterator.next();
        assertThat(lastData).has(id("35")).has(index(7)).has(schema("default"))
                .has(noField("id")).has(noField("schema"))
                .has(field("createdOn", null))
                .has(field("zeNullField", null))
                .has(field("modifyOn", emptyList()))
                .has(field("title", "There is also a title here"))
                .has(field("referenceToAThirdSchema", "42"));
    }

    @Test(expected = ImportDataIteratorRuntimeException.ImportDataIteratorRuntimeException_InvalidDate.class)
    public void givenWrongDateInFourthRecordThenExceptionExpected()
            throws Exception {

        importDataIterator = excelImportDataProvider.newDataIterator("datas-exception");

        importDataIterator.next();
        importDataIterator.next();
        importDataIterator.next();

        //this one throw the exception :
        importDataIterator.next();

    }
/*
    //----------------------------------

	private Condition<? super ImportData> field(final String entryKey, final Object entryValue) {
		return new Condition<ImportData>() {
			@Override
			public boolean matches(ImportData value) {
				assertThat(value.getFields()).containsEntry(entryKey, entryValue);
				return true;
			}
		};
	}

	private Condition<? super ImportData> noField(final String entryKey) {
		return new Condition<ImportData>() {
			@Override
			public boolean matches(ImportData value) {
				assertThat(value.getFields()).doesNotContainKey(entryKey);
				return true;
			}
		};
	}

	private Condition<? super ImportData> schema(final String schema) {
		return new Condition<ImportData>() {
			@Override
			public boolean matches(ImportData value) {
				assertThat(value.getSchema()).isEqualTo(schema);
				return true;
			}
		};
	}

	private Condition<? super ImportData> id(final String expectedId) {
		return new Condition<ImportData>() {
			@Override
			public boolean matches(ImportData value) {
				assertThat(value.getLegacyId()).isEqualTo(expectedId);
				return true;
			}
		};
	}

	private Condition<? super ImportData> index(final int expectedIndex) {
		return new Condition<ImportData>() {
			@Override
			public boolean matches(ImportData value) {
				assertThat(value.getIndex()).isEqualTo(expectedIndex);
				return true;
			}
		};
	}*/
}
