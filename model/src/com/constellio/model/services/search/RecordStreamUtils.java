package com.constellio.model.services.search;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class RecordStreamUtils {

	public static Collector<Record, List<String>, List<String>> recordIds() {
		return new LimitingRetrievedSolrFieldsCollector<Record, List<String>, List<String>>() {

			@Override
			public void filterFields(LogicalSearchQuery query) {
				query.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchema());
			}

			@Override
			public Supplier<List<String>> supplier() {
				return ArrayList::new;
			}

			@Override
			public BiConsumer<List<String>, Record> accumulator() {
				return (strings, record) -> strings.add(record.getId());
			}

			@Override
			public BinaryOperator<List<String>> combiner() {
				return (aList, anotherList) -> {
					List<String> combined = new ArrayList<>();
					combined.addAll(aList);
					combined.addAll(anotherList);
					return combined;
				};
			}

			@Override
			public Function<List<String>, List<String>> finisher() {
				return (r -> r);
			}

			@Override
			public Set<Characteristics> characteristics() {
				return Collections.emptySet();
			}
		};
	}

	public static <T> Collector<Record, List<T>, List<T>> recordMetadataValues(Metadata metadata) {
		return new LimitingRetrievedSolrFieldsCollector<Record, List<T>, List<T>>() {

			@Override
			public void filterFields(LogicalSearchQuery query) {
				query.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(metadata));
			}

			@Override
			public Supplier<List<T>> supplier() {
				return ArrayList::new;
			}

			@Override
			public BiConsumer<List<T>, Record> accumulator() {
				return (strings, record) -> strings.addAll(record.getValues(metadata));
			}

			@Override
			public BinaryOperator<List<T>> combiner() {
				return (aList, anotherList) -> {
					List<T> combined = new ArrayList<>();
					combined.addAll(aList);
					combined.addAll(anotherList);
					return combined;
				};
			}

			@Override
			public Function<List<T>, List<T>> finisher() {
				return (r -> r);
			}

			@Override
			public Set<Characteristics> characteristics() {
				return Collections.emptySet();
			}
		};
	}

}
