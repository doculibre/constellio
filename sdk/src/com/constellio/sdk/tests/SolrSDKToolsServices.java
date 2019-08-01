package com.constellio.sdk.tests;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.LazyResultsIterator.LazyRecordDTOResultsIterator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.LangUtils.ListComparisonResults;
import com.constellio.data.utils.LangUtils.MapComparisonResults;
import com.constellio.data.utils.LangUtils.ModifiedEntry;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.ModifiableSolrParams;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class SolrSDKToolsServices {

	RecordDao recordDao;

	public SolrSDKToolsServices(RecordDao recordDao) {
		this.recordDao = recordDao;
	}

	public void flushAndDeleteContentMarkers()
			throws Exception {
		recordDao.flush();
		recordDao.removeOldLocks();

		ModifiableSolrParams deleteMarkers = new ModifiableSolrParams();
		deleteMarkers.set("q", "type_s:marker");
		deleteMarkers.set("fq", "-schema_s:*");

		recordDao.execute(new TransactionDTO(RecordsFlushing.NOW).withDeletedByQueries(deleteMarkers));
	}

	public VaultSnapshot snapshot() {
		Map<String, Map<String, Object>> dataSnapshot = new HashMap<>();
		Set<String> ids = new HashSet<>();

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "*:*");
		params.add("fq", "-type_s:marker");
		params.add("fq", "-id:lock__*");

		for (Iterator<RecordDTO> iterator = new LazyRecordDTOResultsIterator(recordDao, params, 100, true, "*SDK* Snapshot"); iterator
				.hasNext(); ) {
			RecordDTO record = iterator.next();
			if (!record.getId().endsWith("ZZ")) {
				ids.add(record.getId());
				dataSnapshot.put(record.getId(), toValue(record));
			}
		}

		return new VaultSnapshot(ids, dataSnapshot);
	}

	//	public List<RecordDTO> getRecords(int start, int end) {
	//
	//		ModifiableSolrParams params = new ModifiableSolrParams();
	//		params.set("q", "*:*");
	//		params.set("start", "" + start);
	//		params.set("sort", "id asc");
	//		params.set("rows", "" + end);
	//		params.add("fq", "-type_s:marker");
	//		params.add("fq", "-id:lock__*");
	//
	//		List<RecordDTO> recordDTOs = recordDao.searchQuery(params);
	//
	//		return recordDTOs;
	//	}

	public long getRecordsCount() {

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "*:*");
		params.set("rows", "1");
		params.add("fq", "-type_s:marker");
		params.add("fq", "-id:lock__*");

		return recordDao.nativeQuery(params).getResults().getNumFound();
	}

	public Map<String, Object> toValue(RecordDTO record) {
		Map<String, Object> values = new HashMap<>();

		for (Entry<String, Object> entry : record.getFields().entrySet()) {
			String fieldKey = entry.getKey();
			if (!fieldKey.equals("_version_") && !fieldKey.equals("markedForParsing_s")) {
				Object value = record.getFields().get(fieldKey);

				if (value != null && (!(value instanceof List) || !((List) value).isEmpty())) {
					values.put(fieldKey, value);
				}
			}
		}

		for (Entry<String, Object> entry : record.getCopyFields().entrySet()) {
			String fieldKey = entry.getKey();
			Object value = record.getCopyFields().get(fieldKey);

			if (value != null && (!(value instanceof List) || !((List) value).isEmpty())) {
				values.put(fieldKey, value);
			}
		}

		return values;
	}

	public void writeSnapshotToFile(VaultSnapshot vaultSnapshot, File file)
			throws IOException {
		BufferedWriter bufferedWriter = null;

		try {
			bufferedWriter = new BufferedWriter(new FileWriter(file));

			List<String> ids = new ArrayList<>(vaultSnapshot.ids);
			Collections.sort(ids);

			for (String id : ids) {
				String fieldsString = toString(vaultSnapshot.datas.get(id));

				bufferedWriter.write(fieldsString);
				bufferedWriter.newLine();
			}
			bufferedWriter.flush();
		} finally {
			IOUtils.closeQuietly(bufferedWriter);
		}

	}

	private String toString(Map<String, Object> data) {
		List<String> fields = new ArrayList<>(data.keySet());
		Collections.sort(fields);

		StringBuilder stringBuilder = new StringBuilder();
		for (String field : fields) {
			stringBuilder.append(field);
			stringBuilder.append("=");
			stringBuilder.append(data.get(field));
			stringBuilder.append("\t");
		}

		return stringBuilder.toString();
	}

	public static class VaultSnapshot {

		Set<String> ids = new HashSet<>();

		Map<String, Map<String, Object>> datas = new HashMap<>();

		private VaultSnapshot(Set<String> ids, Map<String, Map<String, Object>> datas) {
			this.ids = ids;
			this.datas = datas;
		}
	}

	public void ensureSameSnapshots(String message, VaultSnapshot snapshotBeforeReindexation,
									VaultSnapshot snapshotAfterReindexation) {

		ListComparisonResults<String> idsComparison = LangUtils
				.compare(snapshotBeforeReindexation.ids, snapshotAfterReindexation.ids);

		StringBuilder idsComparisonMessage = new StringBuilder();
		if (!idsComparison.getNewItems().isEmpty()) {
			idsComparisonMessage
					.append("\n\t* Created documents : " + StringUtils.join(idsComparison.getNewItems(), ","));
		}

		if (!idsComparison.getRemovedItems().isEmpty()) {
			idsComparisonMessage
					.append("\n\t* Removed documents : " + StringUtils.join(idsComparison.getRemovedItems(), ","));
		}

		if (idsComparisonMessage.length() > 0) {
			fail(message + idsComparisonMessage + "\n\n");
		}

		boolean isSameIds = snapshotBeforeReindexation.ids.size() == snapshotAfterReindexation.ids.size();

		if (isSameIds) {
			int matches = 0;
			for (String id : snapshotBeforeReindexation.ids) {
				if (snapshotAfterReindexation.ids.contains(id)) {
					matches++;
				}
			}
			isSameIds = snapshotBeforeReindexation.ids.size() == matches;
		}

		String[] idsAfterReindexation = snapshotAfterReindexation.ids.toArray(new String[0]);
		if (!isSameIds) {
			assertThat(snapshotBeforeReindexation.ids)
					.containsOnly(idsAfterReindexation)
					.containsOnlyOnce(idsAfterReindexation);
		}

		for (String id : idsAfterReindexation) {
			Map<String, Object> dataBefore = snapshotBeforeReindexation.datas.get(id);
			Map<String, Object> dataAfter = snapshotAfterReindexation.datas.get(id);

			dataBefore.remove("fieldValuesLabel_s");
			dataAfter.remove("fieldValuesLabel_s");

			dataBefore.remove("estimatedSize_i");
			dataAfter.remove("estimatedSize_i");

			MapComparisonResults<String, Object> results = LangUtils.compare(dataBefore, dataAfter);

			StringBuilder sb = new StringBuilder();

			for (String removedKey : results.getRemovedEntries()) {
				sb.append(
						"\n\t* The field '" + removedKey + "' with value '" + dataBefore.get(removedKey) + "' was removed");
			}

			for (String addedKey : results.getNewEntries()) {
				sb.append("\n\t* The field '" + addedKey + "' with value '" + dataAfter.get(addedKey) + "' was added");
			}

			for (ModifiedEntry<String, Object> entry : results.getModifiedEntries()) {
				if (!entry.getKey().equals("jasperfile_s")) {
					sb.append("\n\t* The field '" + entry.getKey() + "' with value '" + entry.getValueBefore()
							  + "' was modified to '" + entry.getValueAfter() + "'");
				}
			}

			if (sb.length() > 0) {
				fail(message + "\nModifications in document '" + id + " : " + sb + "\n\n");
			}
		}
	}
}
