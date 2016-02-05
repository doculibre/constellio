package com.constellio.data.dao.services.bigVault.solr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;

import com.constellio.data.dao.dto.records.RecordsFlushing;

public class BigVaultServerTransactionCombinator {

	public static final int DEFAULT_MAX_TRANSACTION_SIZE = 10000;

	List<SolrInputDocument> mergeNewDocuments = new LinkedList<>();
	List<SolrInputDocument> mergeUpdatedDocuments = new LinkedList<>();
	Map<String, SolrInputDocument> newDocumentsById = new HashMap<>();
	Map<String, SolrInputDocument> updatedDocumentsById = new HashMap<>();
	Set<String> deletedRecords = new HashSet<>();
	List<String> deletedQueries = new ArrayList<>();

	int maximumTransactionSize;

	public BigVaultServerTransactionCombinator(int maximumTransactionSize) {
		this.maximumTransactionSize = maximumTransactionSize;
	}

	public BigVaultServerTransactionCombinator combineWith(BigVaultServerTransaction newTransaction) {
		//		for (SolrInputDocument newDocInFirstTransaction : firstTransaction.getNewDocuments()) {
		//			String id = (String) newDocInFirstTransaction.getFieldValue("id");
		//			addMissingFields(newDocInFirstTransaction);
		//			mergeNewDocuments.add(newDocInFirstTransaction);
		//			newDocumentsById.put(id, newDocInFirstTransaction);
		//		}
		//
		for (SolrInputDocument newDocInSecondTransaction : newTransaction.getNewDocuments()) {
			filterMetadatas(newDocInSecondTransaction);
			if (isAtomicUpdate(newDocInSecondTransaction)) {
				handleUpdatedDocument(newDocInSecondTransaction);
			} else {
				handleNewDocument(newDocInSecondTransaction);
			}
		}

		//		for (SolrInputDocument updatedDocInFirstTransaction : firstTransaction.getUpdatedDocuments()) {
		//			String id = (String) updatedDocInFirstTransaction.getFieldValue("id");
		//			addMissingFields(updatedDocInFirstTransaction);
		//			mergeUpdatedDocuments.add(updatedDocInFirstTransaction);
		//			updatedDocumentsById.put(id, updatedDocInFirstTransaction);
		//		}

		for (SolrInputDocument updatedDocInSecondTransaction : newTransaction.getUpdatedDocuments()) {
			filterMetadatas(updatedDocInSecondTransaction);
			handleUpdatedDocument(updatedDocInSecondTransaction);
		}

		for (String secondTransactionDeletedRecord : newTransaction.getDeletedRecords()) {
			handleDeletedDocument(secondTransactionDeletedRecord);
		}
		this.deletedQueries.addAll(newTransaction.getDeletedQueries());

		return this;
	}

	private void filterMetadatas(SolrInputDocument doc) {
		doc.remove("content_txt_fr");
	}

	public boolean canCombineWith(BigVaultServerTransaction otherTransaction) {
		int totalAddUpdate = mergeNewDocuments.size() + mergeUpdatedDocuments.size() + otherTransaction.getNewDocuments().size()
				+ otherTransaction.getUpdatedDocuments().size();
		return deletedQueries.isEmpty() && otherTransaction.getDeletedQueries().isEmpty()
				&& totalAddUpdate < maximumTransactionSize;
		//
		//		if (!otherTransaction.deletedQueries.isEmpty()) {
		//			return false;
		//		}
		//		List<String> ids = getAddUpdateDeleteRecordIds();
		//		List<String> otherTransactionIds = otherTransaction.getAddUpdateDeleteRecordIds();
		//		for (String otherTransactionId : otherTransactionIds) {
		//			if (ids.contains(otherTransactionId)) {
		//				//if (!otherTransactionId.startsWith("idx_") && ids.contains(otherTransactionId)) {
		//				return false;
		//			}
		//		}
		//
		//		return true;
	}

	public BigVaultServerTransaction combineAndClean() {
		String transactionId = UUID.randomUUID().toString();
		BigVaultServerTransaction transaction = new BigVaultServerTransaction(transactionId,
				RecordsFlushing.NOW(),
				new ArrayList<>(mergeNewDocuments), new ArrayList<>(mergeUpdatedDocuments),
				new ArrayList<String>(deletedRecords), new ArrayList<>(deletedQueries));

		this.mergeNewDocuments.clear();
		this.mergeUpdatedDocuments.clear();
		this.newDocumentsById.clear();
		this.updatedDocumentsById.clear();
		this.deletedRecords.clear();
		this.deletedQueries.clear();

		return transaction;
	}

	private void handleDeletedDocument(String secondTransactionDeletedRecord) {
		deletedRecords.add(secondTransactionDeletedRecord);

		if (newDocumentsById.containsKey(secondTransactionDeletedRecord)) {
			removeNewDocument(secondTransactionDeletedRecord);
		}

		if (updatedDocumentsById.containsKey(secondTransactionDeletedRecord)) {
			removeUpdatedDocument(secondTransactionDeletedRecord);
		}
	}

	private void handleUpdatedDocument(SolrInputDocument updatedDocInSecondTransaction) {
		String id = (String) updatedDocInSecondTransaction.getFieldValue("id");
		addMissingFields(updatedDocInSecondTransaction);
		if (newDocumentsById.containsKey(id)) {
			SolrInputDocument newDocument = newDocumentsById.get(id);
			if (isAtomicUpdate(updatedDocInSecondTransaction)) {

				for (String fieldName : updatedDocInSecondTransaction.getFieldNames()) {
					if (!fieldName.equals("id") && !fieldName.equals("_version_")) {
						SolrInputField field = updatedDocInSecondTransaction.get(fieldName);
						if (field.getValue() instanceof Map) {
							Map<String, Object> atomicSetValue = (Map<String, Object>) field.getValue();

							if (atomicSetValue.containsKey("set")) {
								Object value = atomicSetValue.get("set");
								newDocument.setField(fieldName, value);

							} else if (atomicSetValue.containsKey("inc") && fieldName.endsWith("_d")) {
								double value = toDouble(atomicSetValue.get("inc"));
								SolrInputField firstTransactionFieldValue = newDocument.get(fieldName);

								if (firstTransactionFieldValue == null) {
									newDocument.setField(fieldName, value);

								} else {
									double valueInFirstTransaction = toDouble(firstTransactionFieldValue.getValue());
									newDocument.setField(fieldName, valueInFirstTransaction + value);
								}
							} else {
								throw new UnsupportedOperationException("TODO");
							}
						} else {
							newDocument.setField(fieldName, field.getValue());
						}
					}
				}

			} else {
				removeNewDocument(id);
				mergeUpdatedDocuments.add(updatedDocInSecondTransaction);
				updatedDocumentsById.put(id, updatedDocInSecondTransaction);
			}

		} else if (updatedDocumentsById.containsKey(id)) {
			SolrInputDocument updatedDocument = updatedDocumentsById.get(id);
			if (isAtomicUpdate(updatedDocInSecondTransaction)) {
				for (String fieldName : updatedDocInSecondTransaction.getFieldNames()) {
					if (!fieldName.equals("id") && !fieldName.equals("_version_")) {
						SolrInputField field = updatedDocInSecondTransaction.get(fieldName);
						if (field.getValue() instanceof Map) {
							Map<String, Object> atomicSetValue = (Map<String, Object>) field.getValue();

							if (atomicSetValue.containsKey("set")) {
								updatedDocument.setField(fieldName, atomicSetValue);

							} else if (atomicSetValue.containsKey("inc") && fieldName.endsWith("_d")) {
								double value = toDouble(atomicSetValue.get("inc"));
								SolrInputField firstTransactionFieldValue = updatedDocument.get(fieldName);

								if (firstTransactionFieldValue == null) {
									updatedDocument.setField(fieldName, atomicSetValue);

								} else {
									Map<String, Object> firstTransactionUpdatedMap = (Map<String, Object>) firstTransactionFieldValue
											.getValue();

									if (firstTransactionUpdatedMap.containsKey("set")) {
										double firstTransactionUpdatedMapValue = toDouble(firstTransactionUpdatedMap.get("set"));
										updatedDocument
												.setField(fieldName,
														newAtomicSetMap(firstTransactionUpdatedMapValue + value));

									} else if (firstTransactionUpdatedMap.containsKey("inc")) {
										double firstTransactionUpdatedMapValue = toDouble(firstTransactionUpdatedMap.get("inc"));
										updatedDocument.setField(fieldName, newAtomicIncrementMap(
												firstTransactionUpdatedMapValue + value));
									}

								}

							} else {
								throw new UnsupportedOperationException("TODO");
							}
						} else {
							updatedDocument.setField(fieldName, field.getValue());
						}
					}
				}
			} else {
				removeUpdatedDocument(id);
				mergeUpdatedDocuments.add(updatedDocInSecondTransaction);
				updatedDocumentsById.put(id, updatedDocInSecondTransaction);
			}

		} else {
			mergeUpdatedDocuments.add(updatedDocInSecondTransaction);
			updatedDocumentsById.put(id, updatedDocInSecondTransaction);
		}
	}

	private double toDouble(Object set) {
		return Double.valueOf(set.toString());
	}

	private void handleNewDocument(SolrInputDocument newDocInSecondTransaction) {
		String id = (String) newDocInSecondTransaction.getFieldValue("id");
		deletedRecords.remove(id);
		if (newDocumentsById.containsKey(id)) {
			Iterator<SolrInputDocument> docsIterator = mergeNewDocuments.iterator();
			while (docsIterator.hasNext()) {
				if (id.equals(docsIterator.next().getFieldValue("id"))) {
					docsIterator.remove();
				}
			}

		}
		addMissingFields(newDocInSecondTransaction);
		mergeNewDocuments.add(newDocInSecondTransaction);
		newDocumentsById.put(id, newDocInSecondTransaction);
	}

	private void removeUpdatedDocument(String secondTransactionDeletedRecord) {
		updatedDocumentsById.remove(secondTransactionDeletedRecord);
		for (Iterator<SolrInputDocument> iterator = mergeUpdatedDocuments.iterator(); iterator.hasNext(); ) {
			SolrInputDocument doc = iterator.next();
			if (secondTransactionDeletedRecord.equals(doc.getFieldValue("id"))) {
				iterator.remove();
				break;
			}
		}
	}

	private void removeNewDocument(String secondTransactionDeletedRecord) {
		newDocumentsById.remove(secondTransactionDeletedRecord);
		for (Iterator<SolrInputDocument> iterator = mergeNewDocuments.iterator(); iterator.hasNext(); ) {
			SolrInputDocument doc = iterator.next();
			if (secondTransactionDeletedRecord.equals(doc.getFieldValue("id"))) {
				iterator.remove();
				break;
			}
		}
	}

	private boolean isAtomicUpdate(SolrInputDocument updatedDocInSecondTransaction) {
		for (String fieldName : updatedDocInSecondTransaction.getFieldNames()) {
			if (updatedDocInSecondTransaction.getFieldValue(fieldName) instanceof Map) {
				return true;
			}
		}
		return false;
	}

	private void addMissingFields(SolrInputDocument updatedDocInransaction) {
		//		String id = (String) updatedDocInransaction.getFieldValue("id");
		//		if (!updatedDocInransaction.getFieldNames().contains("type_s") && id.startsWith("idx_")) {
		//			updatedDocInransaction.setField("type_s", "index");
		//		}

	}

	private Map<String, Object> newAtomicIncrementMap(Object value) {
		Map<String, Object> map = new HashMap<>();
		map.put("inc", value);
		return map;
	}

	private Map<String, Object> newAtomicSetMap(Object value) {
		Map<String, Object> map = new HashMap<>();
		map.put("set", value);
		return map;
	}

	public boolean hasData() {
		return !newDocumentsById.isEmpty() || !updatedDocumentsById.isEmpty() || !deletedRecords.isEmpty() || !deletedQueries
				.isEmpty();
	}

	public static BigVaultServerTransaction combineAll(BigVaultServerTransaction... transactions) {

		BigVaultServerTransactionCombinator combinator = new BigVaultServerTransactionCombinator(DEFAULT_MAX_TRANSACTION_SIZE);
		for (BigVaultServerTransaction transaction : transactions) {
			combinator.combineWith(transaction);
		}

		return combinator.combineAndClean();
	}
}
