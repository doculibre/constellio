/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.data.dao.services.bigVault.solr;

import static com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransactionCombinator.combineAll;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.assertj.core.api.ObjectAssert;
import org.junit.Test;

import com.constellio.data.dao.dto.records.RecordsFlushing;

public class BigVaultServerTransactionTest {

	BigVaultServerTransaction firstTransaction = new BigVaultServerTransaction(RecordsFlushing.NOW);
	BigVaultServerTransaction secondTransaction = new BigVaultServerTransaction(RecordsFlushing.NOW);
	BigVaultServerTransaction expectedTransaction = new BigVaultServerTransaction(RecordsFlushing.NOW);

	//TODO String in decimal fields;
	//TODO Test with multiple combine
	//TODO Check performance bottlenecks

	@Test
	public void givenADeletedDocumentIsRecreatedInAnotherTransactionWhenMergingThenRemovedFromMarkForDeletion()
			throws Exception {

		//
		//-- First transaction
		firstTransaction.setDeletedRecords(asList("42"));

		//
		//-- Second transaction
		SolrInputDocument createdDocument = newSolrInputDocument("42");
		createdDocument.setField("field1_s", "value1");
		createdDocument.setField("type_s", "record");
		createdDocument.setField("field2_ss", asList("value2", "value3"));
		secondTransaction.setNewDocuments(asList(createdDocument));

		//
		//-- Expected merged transaction
		expectedTransaction.setNewDocuments(asList(createdDocument));

		;
		validateThat(combineAll(firstTransaction, secondTransaction)).isEqualTo(expectedTransaction);
	}

	@Test
	public void givenACreatedDocumentIsDeletedInAnotherTransactionWhenMergingThenRemovedFromAddedAndMarkForDeletionTransaction()
			throws Exception {

		//
		//-- First transaction
		SolrInputDocument createdDocument = newSolrInputDocument("42");
		createdDocument.setField("field1_s", "value1");
		createdDocument.setField("type_s", "record");
		createdDocument.setField("field2_ss", asList("value2", "value3"));

		firstTransaction.setNewDocuments(asList(createdDocument));

		//
		//-- Second transaction
		secondTransaction.setDeletedRecords(asList("42"));

		//
		//-- Expected merged transaction
		expectedTransaction.setDeletedRecords(asList("42"));

		;
		validateThat(combineAll(firstTransaction, secondTransaction)).isEqualTo(expectedTransaction);
	}

	@Test
	public void givenACreatedDocumentIsModifiedAtomicallyInAnotherTransactionWhenMergingThenMergeDocTransaction()
			throws Exception {

		//
		//-- First transaction
		SolrInputDocument createdDocument1 = newSolrInputDocument("1");
		createdDocument1.setField("type_s", "record");
		createdDocument1.setField("field1_s", "value1");
		createdDocument1.setField("field2_ss", asList("value2", "value3"));
		createdDocument1.setField("field3_s", "value4");
		createdDocument1.setField("field4_ss", asList("value5", "value6"));

		SolrInputDocument createdDocument2 = newSolrInputDocument("2");
		createdDocument2.setField("type_s", "record");
		createdDocument2.setField("field1_s", "value7");
		createdDocument2.setField("field2_ss", asList("value8", "value9"));
		createdDocument2.setField("field3_s", "value10");
		createdDocument2.setField("field4_ss", asList("value11", "value12"));

		firstTransaction.setNewDocuments(asList(createdDocument1, createdDocument2));

		//
		//-- Second transaction
		SolrInputDocument modifiedDocument = newSolrInputDocument("1");
		modifiedDocument.setField("type_s", "record");
		modifiedDocument.setField("field1_s", atomicSet("value13"));
		modifiedDocument.setField("field4_ss", atomicSet(asList("value14", "value15")));
		secondTransaction.setUpdatedDocuments(asList(modifiedDocument));

		//
		//-- Expected merged transaction
		SolrInputDocument expectedMergedDocument1 = newSolrInputDocument("1");
		expectedMergedDocument1.setField("type_s", "record");
		expectedMergedDocument1.setField("field1_s", "value13");
		expectedMergedDocument1.setField("field2_ss", asList("value2", "value3"));
		expectedMergedDocument1.setField("field3_s", "value4");
		expectedMergedDocument1.setField("field4_ss", asList("value14", "value15"));
		expectedTransaction.setNewDocuments(asList(expectedMergedDocument1, createdDocument2));

		//Assert
		validateThat(combineAll(firstTransaction, secondTransaction)).isEqualTo(expectedTransaction);
	}

	@Test
	public void givenACreatedDocumentIsModifiedFullyInAnotherTransactionWhenMergingThenMergeDocTransaction()
			throws Exception {

		//
		//-- First transaction
		SolrInputDocument createdDocument1 = newSolrInputDocument("1");
		createdDocument1.setField("type_s", "record");
		createdDocument1.setField("field1_s", "value1");
		createdDocument1.setField("field2_ss", asList("value2", "value3"));
		createdDocument1.setField("field3_s", "value4");
		createdDocument1.setField("field4_ss", asList("value5", "value6"));

		SolrInputDocument createdDocument2 = newSolrInputDocument("2");
		createdDocument2.setField("type_s", "record");
		createdDocument2.setField("field1_s", "value7");
		createdDocument2.setField("field2_ss", asList("value8", "value9"));
		createdDocument2.setField("field3_s", "value10");
		createdDocument2.setField("field4_ss", asList("value11", "value12"));

		firstTransaction.setNewDocuments(asList(createdDocument1, createdDocument2));

		//
		//-- Second transaction
		SolrInputDocument modifiedDocument = newSolrInputDocument("1");
		modifiedDocument.setField("type_s", "record");
		modifiedDocument.setField("field1_s", "value13");
		modifiedDocument.setField("field4_ss", asList("value14", "value15"));
		secondTransaction.setUpdatedDocuments(asList(modifiedDocument));

		//
		//-- Expected merged transaction
		SolrInputDocument expectedMergedDocument1 = newSolrInputDocument("1");
		expectedMergedDocument1.setField("type_s", "record");
		expectedMergedDocument1.setField("field1_s", "value13");
		expectedMergedDocument1.setField("field4_ss", asList("value14", "value15"));
		expectedTransaction.setNewDocuments(asList(createdDocument2)).setUpdatedDocuments(asList(expectedMergedDocument1));

		//Assert
		validateThat(combineAll(firstTransaction, secondTransaction)).isEqualTo(expectedTransaction);
	}

	@Test
	public void givenAModifiedDocumentIsModifiedFullyInAnotherTransactionWhenMergingThenMergeDocTransaction()
			throws Exception {

		//
		//-- First transaction
		SolrInputDocument updatedDocument1 = newSolrInputDocument("1");
		updatedDocument1.setField("type_s", "record");
		updatedDocument1.setField("field1_s", atomicSet("value1"));
		updatedDocument1.setField("field2_ss", atomicSet(asList("value2", "value3")));
		updatedDocument1.setField("field3_s", atomicSet("value4"));
		updatedDocument1.setField("field4_ss", atomicSet(asList("value5", "value6")));

		firstTransaction.setUpdatedDocuments(asList(updatedDocument1));

		//
		//-- Second transaction
		SolrInputDocument modifiedDocument = newSolrInputDocument("1");
		modifiedDocument.setField("type_s", "record");
		modifiedDocument.setField("field1_s", "value13");
		modifiedDocument.setField("field4_ss", asList("value14", "value15"));
		secondTransaction.setUpdatedDocuments(asList(modifiedDocument));

		//
		//-- Expected merged transaction
		SolrInputDocument expectedMergedDocument1 = newSolrInputDocument("1");
		expectedMergedDocument1.setField("type_s", "record");
		expectedMergedDocument1.setField("field1_s", "value13");
		expectedMergedDocument1.setField("field4_ss", asList("value14", "value15"));
		expectedTransaction.setUpdatedDocuments(asList(expectedMergedDocument1));

		//Assert
		validateThat(combineAll(firstTransaction, secondTransaction)).isEqualTo(expectedTransaction);
	}

	@Test
	public void givenACreatedDocumentFieldIsIncrementdInAnotherTransactionWhenMergingThenMergeDocTransaction()
			throws Exception {

		//
		//-- First transaction
		SolrInputDocument createdDocument1 = newSolrInputDocument("1");
		createdDocument1.setField("type_s", "record");
		createdDocument1.setField("field1_s", "value1");
		createdDocument1.setField("field2_ss", asList("value2", "value3"));
		createdDocument1.setField("field3_s", "value4");
		createdDocument1.setField("field4_ss", asList("value5", "value6"));
		createdDocument1.setField("field5_d", 42.5);

		firstTransaction.setNewDocuments(asList(createdDocument1));

		//
		//-- Second transaction
		SolrInputDocument modifiedDocument = newSolrInputDocument("1");
		modifiedDocument.setField("type_s", "record");
		modifiedDocument.setField("field5_d", atomicIncrement(2.4));
		modifiedDocument.setField("field6_d", atomicIncrement(3.5));
		secondTransaction.setUpdatedDocuments(asList(modifiedDocument));

		//
		//-- Expected merged transaction
		SolrInputDocument expectedMergedDocument1 = newSolrInputDocument("1");
		expectedMergedDocument1.setField("type_s", "record");
		expectedMergedDocument1.setField("field1_s", "value1");
		expectedMergedDocument1.setField("field2_ss", asList("value2", "value3"));
		expectedMergedDocument1.setField("field3_s", "value4");
		expectedMergedDocument1.setField("field4_ss", asList("value5", "value6"));
		expectedMergedDocument1.setField("field5_d", 44.9);
		expectedMergedDocument1.setField("field6_d", 3.5);
		expectedTransaction.setNewDocuments(asList(expectedMergedDocument1));

		//Assert
		validateThat(combineAll(firstTransaction, secondTransaction)).isEqualTo(expectedTransaction);
	}

	@Test
	public void givenAModifiedDocumentFieldIsIncrementdInAnotherTransactionWhenMergingThenMergeDocTransaction()
			throws Exception {

		//
		//-- First transaction
		SolrInputDocument modifiedDocument1 = newSolrInputDocument("1");
		modifiedDocument1.setField("type_s", "record");
		modifiedDocument1.setField("field1_s", atomicSet("value1"));
		modifiedDocument1.setField("field2_ss", atomicSet(asList("value2", "value3")));
		modifiedDocument1.setField("field3_s", atomicSet("value4"));
		modifiedDocument1.setField("field4_ss", atomicSet(asList("value5", "value6")));
		modifiedDocument1.setField("field5_d", atomicSet(42.5));
		firstTransaction.setUpdatedDocuments(asList(modifiedDocument1));

		//
		//-- Second transaction
		SolrInputDocument modifiedDocument = newSolrInputDocument("1");
		modifiedDocument.setField("type_s", "record");
		modifiedDocument.setField("field5_d", atomicIncrement(2.4));
		secondTransaction.setUpdatedDocuments(asList(modifiedDocument));

		//
		//-- Expected merged transaction
		SolrInputDocument expectedMergedDocument1 = newSolrInputDocument("1");
		expectedMergedDocument1.setField("type_s", "record");
		expectedMergedDocument1.setField("field1_s", atomicSet("value1"));
		expectedMergedDocument1.setField("field2_ss", atomicSet(asList("value2", "value3")));
		expectedMergedDocument1.setField("field3_s", atomicSet("value4"));
		expectedMergedDocument1.setField("field4_ss", atomicSet(asList("value5", "value6")));
		expectedMergedDocument1.setField("field5_d", atomicSet(44.9));
		expectedTransaction.setUpdatedDocuments(asList(expectedMergedDocument1));

		//Assert
		validateThat(combineAll(firstTransaction, secondTransaction)).isEqualTo(expectedTransaction);
	}

	@Test
	public void givenAnIncrementdDocumentFieldIsIncrementdAnotherTimeInAnotherTransactionWhenMergingThenIncrementTheSum()
			throws Exception {

		//
		//-- First transaction
		SolrInputDocument modifiedDocument1 = newSolrInputDocument("1");
		modifiedDocument1.setField("type_s", "record");
		modifiedDocument1.setField("field1_s", atomicSet("value1"));
		modifiedDocument1.setField("field2_ss", atomicSet(asList("value2", "value3")));
		modifiedDocument1.setField("field3_s", atomicSet("value4"));
		modifiedDocument1.setField("field4_ss", atomicSet(asList("value5", "value6")));
		modifiedDocument1.setField("field5_d", atomicIncrement(-42));
		firstTransaction.setUpdatedDocuments(asList(modifiedDocument1));

		//
		//-- Second transaction
		SolrInputDocument modifiedDocument = newSolrInputDocument("1");
		modifiedDocument.setField("type_s", "record");
		modifiedDocument.setField("field5_d", atomicIncrement(46));
		secondTransaction.setUpdatedDocuments(asList(modifiedDocument));

		//
		//-- Expected merged transaction
		SolrInputDocument expectedMergedDocument1 = newSolrInputDocument("1");
		expectedMergedDocument1.setField("type_s", "record");
		expectedMergedDocument1.setField("field1_s", atomicSet("value1"));
		expectedMergedDocument1.setField("field2_ss", atomicSet(asList("value2", "value3")));
		expectedMergedDocument1.setField("field3_s", atomicSet("value4"));
		expectedMergedDocument1.setField("field4_ss", atomicSet(asList("value5", "value6")));
		expectedMergedDocument1.setField("field5_d", atomicIncrement(4.0));
		expectedTransaction.setUpdatedDocuments(asList(expectedMergedDocument1));

		//Assert
		validateThat(combineAll(firstTransaction, secondTransaction)).isEqualTo(expectedTransaction);
	}

	@Test
	public void givenAFieldSetInAFirstTransactionAndAnotherOneIncrementInTheSecondThenMergedCorrectly()
			throws Exception {

		//
		//-- First transaction
		SolrInputDocument modifiedDocument1 = newSolrInputDocument("1");
		modifiedDocument1.setField("type_s", "record");
		modifiedDocument1.setField("field1_s", atomicSet("value1"));
		modifiedDocument1.setField("field2_ss", atomicSet(asList("value2", "value3")));
		firstTransaction.setUpdatedDocuments(asList(modifiedDocument1));

		//
		//-- Second transaction
		SolrInputDocument modifiedDocumentInSecondTransaction = newSolrInputDocument("1");
		modifiedDocumentInSecondTransaction.setField("type_s", "record");
		modifiedDocumentInSecondTransaction.setField("field3_d", atomicIncrement(4.7));
		secondTransaction.setUpdatedDocuments(asList(modifiedDocumentInSecondTransaction));

		//
		//-- Expected merged transaction
		SolrInputDocument expectedMergedDocument1 = newSolrInputDocument("1");
		expectedMergedDocument1.setField("type_s", "record");
		expectedMergedDocument1.setField("field1_s", atomicSet("value1"));
		expectedMergedDocument1.setField("field2_ss", atomicSet(asList("value2", "value3")));
		expectedMergedDocument1.setField("field3_d", atomicIncrement(4.7));
		expectedTransaction.setUpdatedDocuments(asList(expectedMergedDocument1));

		//Assert
		validateThat(combineAll(firstTransaction, secondTransaction)).isEqualTo(expectedTransaction);
	}

	@Test
	public void givenANewDocumentIsAddedAnotherTimeInASecondTransactionThenReplaced()
			throws Exception {

		//
		//-- First transaction
		SolrInputDocument newDocument1 = newSolrInputDocument("1");
		newDocument1.setField("type_s", "record");
		newDocument1.setField("field1_s", "value1");
		newDocument1.setField("field2_ss", asList("value2", "value3"));
		newDocument1.setField("field3_s", "value4");
		newDocument1.setField("field4_ss", asList("value5", "value6"));
		firstTransaction.setNewDocuments(asList(newDocument1));

		//
		//-- Second transaction
		SolrInputDocument newDocument1InTheSecondTransaction = newSolrInputDocument("1");
		newDocument1InTheSecondTransaction.setField("type_s", "record");
		newDocument1InTheSecondTransaction.setField("field1_s", "value7");
		newDocument1InTheSecondTransaction.setField("field2_ss", asList("value8", "value9"));
		newDocument1InTheSecondTransaction.setField("field3_s", "value10");
		newDocument1InTheSecondTransaction.setField("field4_ss", asList("value11", "value12"));
		secondTransaction.setNewDocuments(asList(newDocument1InTheSecondTransaction));

		//
		//-- Expected merged transaction
		SolrInputDocument expectedMergedDocument1 = newSolrInputDocument("1");
		expectedMergedDocument1.setField("type_s", "record");
		expectedMergedDocument1.setField("field1_s", "value7");
		expectedMergedDocument1.setField("field2_ss", asList("value8", "value9"));
		expectedMergedDocument1.setField("field3_s", "value10");
		expectedMergedDocument1.setField("field4_ss", asList("value11", "value12"));
		expectedTransaction.setNewDocuments(asList(expectedMergedDocument1));

		//Assert
		validateThat(combineAll(firstTransaction, secondTransaction)).isEqualTo(expectedTransaction);
	}

	@Test
	public void givenTwoTransactionCreatingDifferentDocumentsThenMergedCorrectly()
			throws Exception {

		//
		//-- First transaction
		SolrInputDocument createdDocument1 = newSolrInputDocument("1");
		createdDocument1.setField("type_s", "record");
		createdDocument1.setField("field1_s", "value1");
		createdDocument1.setField("field2_ss", asList("value2", "value3"));

		SolrInputDocument createdDocument2 = newSolrInputDocument("2");
		createdDocument2.setField("type_s", "record");
		createdDocument2.setField("field1_s", "value4");
		createdDocument2.setField("field2_ss", asList("value5", "value6"));

		firstTransaction.setNewDocuments(asList(createdDocument1, createdDocument2));

		//
		//-- Second transaction
		SolrInputDocument createdDocument3 = newSolrInputDocument("3");
		createdDocument3.setField("type_s", "record");
		createdDocument3.setField("field1_s", "value7");
		createdDocument3.setField("field2_ss", asList("value8", "value9"));

		SolrInputDocument createdDocument4 = newSolrInputDocument("4");
		createdDocument4.setField("type_s", "record");
		createdDocument4.setField("field1_s", "value10");
		createdDocument4.setField("field2_ss", asList("value11", "value12"));

		secondTransaction.setNewDocuments(asList(createdDocument3, createdDocument4));

		//
		//-- Expected merged transaction
		expectedTransaction.setNewDocuments(asList(createdDocument1, createdDocument2, createdDocument3, createdDocument4));

		//Assert
		validateThat(combineAll(firstTransaction, secondTransaction)).isEqualTo(expectedTransaction);
	}

	@Test
	public void givenTwoTransactionModifyingDocumentsThenMergedCorrectly()
			throws Exception {

		//
		//-- First transaction
		SolrInputDocument modifiedDocument1 = newSolrInputDocument("1");
		modifiedDocument1.setField("type_s", "record");
		modifiedDocument1.setField("field1_s", atomicSet("value1"));
		modifiedDocument1.setField("field2_ss", atomicSet(asList("value2", "value3")));

		SolrInputDocument modifiedDocument2 = newSolrInputDocument("2");
		modifiedDocument2.setField("type_s", "record");
		modifiedDocument2.setField("field1_s", atomicSet("value4"));
		modifiedDocument2.setField("field2_ss", atomicSet(asList("value5", "value6")));
		modifiedDocument2.setField("field3_s", atomicSet("value7"));
		modifiedDocument2.setField("field4_ss", atomicSet(asList("value8", "value9")));

		firstTransaction.setUpdatedDocuments(asList(modifiedDocument1, modifiedDocument2));

		//
		//-- Second transaction
		SolrInputDocument modifiedDocument2InSecondTransaction = newSolrInputDocument("2");
		modifiedDocument2.setField("type_s", "record");
		modifiedDocument2InSecondTransaction.setField("field2_ss", atomicSet(asList("value16", "value17")));
		modifiedDocument2InSecondTransaction.setField("field3_s", atomicSet("value18"));

		SolrInputDocument modifiedDocument3 = newSolrInputDocument("3");
		modifiedDocument3.setField("type_s", "record");
		modifiedDocument3.setField("field1_s", atomicSet("value10"));
		modifiedDocument3.setField("field2_ss", atomicSet(asList("value11", "value12")));

		SolrInputDocument modifiedDocument4 = newSolrInputDocument("4");
		modifiedDocument4.setField("type_s", "record");
		modifiedDocument4.setField("field1_s", atomicSet("value13"));
		modifiedDocument4.setField("field2_ss", atomicSet(asList("value14", "value15")));

		secondTransaction.setUpdatedDocuments(asList(modifiedDocument3, modifiedDocument2InSecondTransaction, modifiedDocument4));

		//
		//-- Expected merged transaction
		SolrInputDocument expectedMergedDocument2 = newSolrInputDocument("2");
		expectedMergedDocument2.setField("type_s", "record");
		expectedMergedDocument2.setField("field1_s", atomicSet("value4"));
		expectedMergedDocument2.setField("field2_ss", atomicSet(asList("value16", "value17")));
		expectedMergedDocument2.setField("field3_s", atomicSet("value18"));
		expectedMergedDocument2.setField("field4_ss", atomicSet(asList("value8", "value9")));
		expectedTransaction.setUpdatedDocuments(
				asList(modifiedDocument1, expectedMergedDocument2, modifiedDocument3, modifiedDocument4));

		//Assert
		validateThat(combineAll(firstTransaction, secondTransaction)).isEqualTo(expectedTransaction);
	}

	@Test
	public void givenATransactionModifyingARecordAndAnotherOneDeletingItThenRemovedFromTransactionAndStillMarkedForDeletion()
			throws Exception {

		//
		//-- First transaction
		SolrInputDocument modifiedDocument1 = newSolrInputDocument("1");
		modifiedDocument1.setField("type_s", "record");
		modifiedDocument1.setField("field1_s", atomicSet("value1"));
		modifiedDocument1.setField("field2_ss", atomicSet(asList("value2", "value3")));

		SolrInputDocument modifiedDocument2 = newSolrInputDocument("2");
		modifiedDocument2.setField("type_s", "record");
		modifiedDocument2.setField("field1_s", atomicSet("value4"));
		modifiedDocument2.setField("field2_ss", atomicSet(asList("value5", "value6")));

		firstTransaction.setUpdatedDocuments(asList(modifiedDocument1, modifiedDocument2));

		//
		//-- Second transaction
		secondTransaction.setDeletedRecords(asList("2"));

		//
		//-- Expected merged transaction
		expectedTransaction.setUpdatedDocuments(asList(modifiedDocument1)).setDeletedRecords(asList("2"));

		//Assert
		validateThat(combineAll(firstTransaction, secondTransaction)).isEqualTo(expectedTransaction);
	}

	@Test
	public void givenTwoTransactionsWithAddUpdatedDocumentsThenMergedCorrectly()
			throws Exception {

		//
		//-- First transaction
		SolrInputDocument firstTransactionAddedDoc9 = newSolrInputDocument("9");
		firstTransactionAddedDoc9.setField("type_s", "record");
		firstTransactionAddedDoc9.setField("field1_s", "value1");
		firstTransactionAddedDoc9.setField("field2_ss", asList("value2", "value3"));

		SolrInputDocument firstTransactionAddedDoc1 = newSolrInputDocument("1");
		firstTransactionAddedDoc1.setField("type_s", "record");
		firstTransactionAddedDoc1.setField("field1_s", "value4");
		firstTransactionAddedDoc1.setField("field2_ss", asList("value5", "value6"));
		firstTransactionAddedDoc1.setField("field3_s", "valueA");
		firstTransactionAddedDoc1.setField("field4_ss", asList("valueB", "valueC"));

		SolrInputDocument firstTransactionAddedDoc2 = newSolrInputDocument("2");
		firstTransactionAddedDoc2.setField("type_s", "record");
		firstTransactionAddedDoc2.setField("field1_s", "value7");
		firstTransactionAddedDoc2.setField("field2_ss", asList("value8", "value9"));

		SolrInputDocument firstTransactionModifiedDoc3 = newSolrInputDocument("3");
		firstTransactionModifiedDoc3.setField("type_s", "record");
		firstTransactionModifiedDoc3.setField("field1_s", atomicSet("value10"));
		firstTransactionModifiedDoc3.setField("field2_ss", atomicSet(asList("value11", "value12")));
		firstTransactionModifiedDoc3.setField("field3_s", atomicSet("valueD"));
		firstTransactionModifiedDoc3.setField("field4_ss", atomicSet(asList("valueE", "valueF")));

		SolrInputDocument firstTransactionModifiedDoc18 = newSolrInputDocument("18");
		firstTransactionModifiedDoc18.setField("type_s", "record");
		firstTransactionModifiedDoc18.setField("field1_s", atomicSet("value10"));
		firstTransactionModifiedDoc18.setField("field2_ss", atomicSet(asList("value11", "value12")));

		BigVaultServerTransaction firstTransaction = new BigVaultServerTransaction(RecordsFlushing.NOW)
				.setNewDocuments(asList(firstTransactionAddedDoc1, firstTransactionAddedDoc2, firstTransactionAddedDoc9))
				.setUpdatedDocuments(asList(firstTransactionModifiedDoc3, firstTransactionModifiedDoc18));

		//
		//-- Second transaction
		SolrInputDocument secondTransactionAddedDoc4 = newSolrInputDocument("4");
		secondTransactionAddedDoc4.setField("type_s", "record");
		secondTransactionAddedDoc4.setField("field1_s", "value13");
		secondTransactionAddedDoc4.setField("field2_ss", asList("value14", "value15"));

		SolrInputDocument secondTransactionAddedDoc5 = newSolrInputDocument("5");
		secondTransactionAddedDoc5.setField("type_s", "record");
		secondTransactionAddedDoc5.setField("field1_s", "value16");
		secondTransactionAddedDoc5.setField("field2_ss", asList("value17", "value18"));

		SolrInputDocument secondTransactionModifiedDoc1 = newSolrInputDocument("1");
		secondTransactionModifiedDoc1.setField("type_s", "record");
		secondTransactionModifiedDoc1.setField("field1_s", atomicSet("value19"));
		secondTransactionModifiedDoc1.setField("field2_ss", atomicSet(asList("value20", "value21")));

		SolrInputDocument secondTransactionModifiedDoc3 = newSolrInputDocument("3");
		secondTransactionModifiedDoc3.setField("type_s", "record");
		secondTransactionModifiedDoc3.setField("field1_s", atomicSet("value22"));
		secondTransactionModifiedDoc3.setField("field2_ss", atomicSet(asList("value23", "value24")));

		SolrInputDocument secondTransactionModifiedDoc6 = newSolrInputDocument("6");
		secondTransactionModifiedDoc6.setField("type_s", "record");
		secondTransactionModifiedDoc6.setField("field1_s", atomicSet("value25"));
		secondTransactionModifiedDoc6.setField("field2_ss", atomicSet(asList("value26", "value27")));

		BigVaultServerTransaction secondTransaction = new BigVaultServerTransaction(RecordsFlushing.NOW)
				.setNewDocuments(asList(secondTransactionAddedDoc4, secondTransactionAddedDoc5))
				.setUpdatedDocuments(asList(
						secondTransactionModifiedDoc1, secondTransactionModifiedDoc3, secondTransactionModifiedDoc6))
				.setDeletedRecords(asList("9", "18", "27"));

		//
		//-- Expected merged transaction
		SolrInputDocument mergedNewDoc1 = newSolrInputDocument("1");
		mergedNewDoc1.setField("type_s", "record");
		mergedNewDoc1.setField("field1_s", "value19");
		mergedNewDoc1.setField("field2_ss", asList("value20", "value21"));
		mergedNewDoc1.setField("field3_s", "valueA");
		mergedNewDoc1.setField("field4_ss", asList("valueB", "valueC"));

		SolrInputDocument mergedUpdatedDoc3 = newSolrInputDocument("3");
		mergedUpdatedDoc3.setField("type_s", "record");
		mergedUpdatedDoc3.setField("field1_s", atomicSet("value22"));
		mergedUpdatedDoc3.setField("field2_ss", atomicSet(asList("value23", "value24")));
		mergedUpdatedDoc3.setField("field3_s", atomicSet("valueD"));
		mergedUpdatedDoc3.setField("field4_ss", atomicSet(asList("valueE", "valueF")));

		expectedTransaction
				.setNewDocuments(
						asList(mergedNewDoc1, firstTransactionAddedDoc2, secondTransactionAddedDoc4, secondTransactionAddedDoc5))
				.setUpdatedDocuments(asList(mergedUpdatedDoc3, secondTransactionModifiedDoc6))
				.setDeletedRecords(asList("9", "18", "27"));

		//Assert
		validateThat(combineAll(firstTransaction, secondTransaction)).isEqualTo(expectedTransaction);
	}

	private SolrInputDocument newSolrInputDocument(String id) {
		SolrInputDocument doc = new SolrInputDocument();
		doc.setField("id", id);
		return doc;
	}

	private ObjectAssert<BigVaultServerTransaction> validateThat(BigVaultServerTransaction transaction) {
		return assertThat(transaction).usingComparator(new Comparator<BigVaultServerTransaction>() {
			@Override
			public int compare(BigVaultServerTransaction o1, BigVaultServerTransaction o2) {
				boolean sameNewDocuments = isSameList(o1.getNewDocuments(), o2.getNewDocuments(), "newDocuments");
				boolean sameUpdatedDocuments = isSameList(o1.getUpdatedDocuments(), o2.getUpdatedDocuments(), "updatedDocuments");
				boolean sameDeletedDocuments = isSameStringList(o1.getDeletedRecords(), o2.getDeletedRecords());
				boolean sameDeleteQueries = isSameStringList(o1.getDeletedQueries(), o2.getDeletedQueries());

				return (sameNewDocuments && sameUpdatedDocuments && sameDeletedDocuments && sameDeleteQueries) ? 0 : 1;
			}

			private boolean isSameStringList(java.util.Collection<String> list1, java.util.Collection<String> list2) {
				Set<String> set1 = new HashSet<>(list1);
				Set<String> set2 = new HashSet<>(list2);
				return set1.equals(set2) && set1.size() == list1.size() && set2.size() == list2.size();
			}

			private boolean isSameList(List<SolrInputDocument> l1, List<SolrInputDocument> l2, final String listName) {
				List<String> l1Ids = new ArrayList<String>();
				List<String> l2Ids = new ArrayList<String>();

				for (SolrInputDocument doc : l1) {
					l1Ids.add((String) doc.getFieldValue("id"));
				}

				for (SolrInputDocument doc : l2) {
					l2Ids.add((String) doc.getFieldValue("id"));
				}

				if (isSameStringList(l1Ids, l2Ids)) {
					for (String id : l1Ids) {
						SolrInputDocument doc1 = getDocWithId(l1, id);
						SolrInputDocument doc2 = getDocWithId(l2, id);

						java.util.Collection<String> fieldNames1 = doc1.getFieldNames();
						java.util.Collection<String> fieldNames2 = doc2.getFieldNames();

						if (isSameStringList(fieldNames1, fieldNames2)) {
							for (String fieldName : fieldNames1) {
								java.util.Collection<Object> values1 = doc1.getFieldValues(fieldName);
								java.util.Collection<Object> values2 = doc2.getFieldValues(fieldName);
								if (!values1.equals(values2)) {
									System.out.println("Different values for field " + fieldName + " in document " + id + " : "
											+ values1 + " is not equal to " + values2);
									return false;
								}
							}

						} else {
							System.out.println("Different field name for document " + id + " : "
									+ fieldNames1 + " is not equal to " + fieldNames2);
							return false;
						}

					}
				} else {
					System.out.println("Not same ids for " + listName + " : " + l1Ids + " is not equal to " + l2Ids);
					return false;
				}

				return true;
			}

			private SolrInputDocument getDocWithId(List<SolrInputDocument> list, String id) {
				for (SolrInputDocument doc : list) {
					if (doc.getFieldValue("id").equals(id)) {
						return doc;
					}
				}
				return null;
			}
		});
	}

	private org.assertj.core.api.ListAssert<SolrInputDocument> validateThat(List<SolrInputDocument> docs) {
		return assertThat(docs).usingElementComparator(new Comparator<SolrInputDocument>() {
			@Override
			public int compare(SolrInputDocument o1, SolrInputDocument o2) {
				Map<String, Object> o1Map = toMapOfValues(o1);
				Map<String, Object> o2Map = toMapOfValues(o2);
				return o1Map.equals(o2Map) ? 0 : 1;
			}

			private Map<String, Object> toMapOfValues(SolrInputDocument doc) {
				Map<String, Object> values = new HashMap<>();
				for (String field : doc.getFieldNames()) {
					values.put(field, doc.getFieldValues(field));
				}
				return values;
			}
		});
	}

	private Map<String, Object> atomicSet(Object value) {
		Map<String, Object> maps = new HashMap<>();
		maps.put("set", value);
		return maps;

	}

	private Map<String, Object> atomicIncrement(Object value) {
		Map<String, Object> maps = new HashMap<>();
		maps.put("inc", value);
		return maps;

	}
}
