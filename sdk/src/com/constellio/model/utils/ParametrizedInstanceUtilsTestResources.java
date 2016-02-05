package com.constellio.model.utils;

import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.constellio.model.entities.schemas.Metadata;

public class ParametrizedInstanceUtilsTestResources {

	public static class CorrectParametrizedClass implements Parametrized {
		private List<String> metadatas;
		private Map<String, List<Integer>> values;
		private Boolean aBoolean;
		private LocalDateTime aDateTime;
		private LocalDate aDate;
		private Double aDouble;

		public CorrectParametrizedClass(List<String> metadatas,
				Map<String, List<Integer>> values, Boolean aBoolean, LocalDateTime aDateTime, LocalDate aDate, Double aDouble) {
			this.metadatas = metadatas;
			this.values = values;
			this.aBoolean = aBoolean;
			this.aDateTime = aDateTime;
			this.aDate = aDate;
			this.aDouble = aDouble;
		}

		@Override
		public Object[] getInstanceParameters() {
			return new Object[] { metadatas, values, aBoolean, aDateTime, aDate, aDouble };
		}
	}

	public static class NotParametrizedClass {

		public NotParametrizedClass() {
		}
	}

	public static class UnsupportedArgumentClass implements Parametrized {

		private Metadata metadata;

		public UnsupportedArgumentClass(Metadata metadata) {
			this.metadata = metadata;
		}

		@Override
		public Object[] getInstanceParameters() {
			return new Object[] { metadata };
		}
	}

	public static class UnsupportedChildArgumentClass implements Parametrized {

		private List<Metadata> metadatas;

		public UnsupportedChildArgumentClass(List<Metadata> metadatas) {
			this.metadatas = metadatas;
		}

		@Override
		public Object[] getInstanceParameters() {
			return new Object[] { metadatas };
		}
	}

	public static class PrivateConstructorClass implements Parametrized {

		private PrivateConstructorClass(List<String> metadatas) {
		}

		@Override
		public Object[] getInstanceParameters() {
			return new Object[] { };
		}
	}
}
