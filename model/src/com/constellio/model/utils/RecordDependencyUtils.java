package com.constellio.model.utils;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.model.entities.records.Record;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecordDependencyUtils {

	public static List<RecordId> findFirstRecordsForIdSortedIteration(Stream<Record> recordStream,
																	  Function<Record, List<Record>> dependenciesFunction) {

		List<DependencyItem> items = new ArrayList<>();
		Map<RecordId, DependencyItem> map = new HashMap<>();

		BiConsumer<LinkedList<RecordId>, Record> incrementCounter = new BiConsumer<LinkedList<RecordId>, Record>() {
			@Override
			public void accept(LinkedList<RecordId> path, Record dependency) {

				DependencyItem item = map.get(dependency.getRecordId());
				if (item == null) {
					item = new DependencyItem(dependency.getRecordId(), 0);
					map.put(dependency.getRecordId(), item);
					items.add(item);
				}
				item.counter += path.size();

				if (!path.contains(dependency.getRecordId())) {
					path.add(dependency.getRecordId());
					for (Record dependencyDependency : dependenciesFunction.apply(dependency)) {
						accept(path, dependencyDependency);
					}
					path.removeLast();
				}
			}
		};

		recordStream.forEach(record -> {
			LinkedList<RecordId> path = new LinkedList<>();
			path.add(record.getRecordId());
			for (Record dependency : dependenciesFunction.apply(record)) {
				if (!record.getRecordId().isInteger() || (dependency.getRecordId().isInteger() && dependency.getRecordId().intValue() + 1000 >= record.getRecordId().intValue())) {
					incrementCounter.accept(path, dependency);
				}
			}
		});

		//Free some memory before sorting/creating response list
		map.clear();
		items.sort(Comparator.comparing(DependencyItem::getCounter).reversed());

		return items.stream().map(DependencyItem::getId).collect(Collectors.toList());
	}

	@AllArgsConstructor
	private static class DependencyItem {

		RecordId id;

		int counter;

		public RecordId getId() {
			return id;
		}

		public int getCounter() {
			return counter;
		}
	}
}
