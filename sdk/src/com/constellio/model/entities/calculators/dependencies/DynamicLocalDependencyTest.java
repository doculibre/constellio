package com.constellio.model.entities.calculators.dependencies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.schemas.Metadata;

public class DynamicLocalDependencyTest {
	DynamicLocalDependency dynamicLocalDependency = new DynamicLocalDependency() {
		@Override
		public boolean isDependentOf(Metadata metadata) {
			return true;
		}
	};
	@Mock
	private DynamicDependencyValues values = mock(DynamicDependencyValues.class);

	@Test
	public void givenStringMetadataWhenGetDateThanOk()
			throws Exception {
		String zMeta = "zMeta";
		when(values.getValue(zMeta)).thenReturn("2000-2010");
		LocalDate date = dynamicLocalDependency.getDate(zMeta, values, "1/12");
		assertThat(date).isEqualTo(new LocalDate(2010, 1, 12));
	}

	@Test
	public void givenNumberMetadataWhenGetDateThanOk()
			throws Exception {
		String zMeta = "zMeta";
		Number number = 1985.5;
		when(values.getValue(zMeta)).thenReturn(number);
		LocalDate date = dynamicLocalDependency.getDate(zMeta, values, "02/28");
		assertThat(date).isEqualTo(new LocalDate(1985, 2, 28));
	}

	@Test
	public void givenDateMetadataWhenGetDateThanOk()
			throws Exception {
		String zMeta = "zMeta";
		LocalDate date = new LocalDate(1980, 10,11);
		when(values.getValue(zMeta)).thenReturn(date);
		assertThat(dynamicLocalDependency.getDate(zMeta, values, "02/28")).isEqualTo(date);
	}

	@Test
	public void givenDatetimeMetadataWhenGetDateThanOk()
			throws Exception {
		String zMeta = "zMeta";
		LocalDateTime date = new LocalDateTime();
		when(values.getValue(zMeta)).thenReturn(date);
		assertThat(dynamicLocalDependency.getDate(zMeta, values, "02/28")).isEqualTo(date.toLocalDate());
	}

	@Test
	public void givenListMetadataWhenGetDateThanReturnFirstValue()
			throws Exception {
		String zMeta = "zMeta";
		List<Object> listOfObjects = new ArrayList();
		listOfObjects.add("2000-2010");
		listOfObjects.add( new LocalDate(1980, 10,11));
		when(values.getValue(zMeta)).thenReturn(listOfObjects);
		assertThat(dynamicLocalDependency.getDate(zMeta, values, "1/12")).isEqualTo(new LocalDate(2010, 1, 12));
	}
}
