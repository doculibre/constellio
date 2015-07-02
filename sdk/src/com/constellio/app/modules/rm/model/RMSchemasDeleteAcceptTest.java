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
package com.constellio.app.modules.rm.model;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.type.VariableRetentionPeriod;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;

public class RMSchemasDeleteAcceptTest extends ConstellioTest {

	@Test
	public void whenLogicallyDeletingVariableRetentionPeriodThenOnlyPossibleIfNotUsedAndNot888And999()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
		);

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

		Transaction transaction = new Transaction();
		transaction.add(rm.newVariableRetentionPeriod().setCode("42").setTitle("42"));
		transaction.add(rm.newVariableRetentionPeriod().setCode("666").setTitle("666"));
		recordServices.execute(transaction);

		User admin = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);
		VariableRetentionPeriod period888 = rm.PERIOD_888();
		VariableRetentionPeriod period999 = rm.PERIOD_999();
		VariableRetentionPeriod period42 = rm.getVariableRetentionPeriodWithCode("42");
		VariableRetentionPeriod period666 = rm.getVariableRetentionPeriodWithCode("666");

		assertThat(recordServices.isLogicallyDeletable(period888.getWrappedRecord(), admin)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(period999.getWrappedRecord(), admin)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(period42.getWrappedRecord(), admin)).isTrue();
		assertThat(recordServices.isLogicallyDeletable(period666.getWrappedRecord(), admin)).isTrue();

		CopyRetentionRule principal42_666_T = CopyRetentionRule.newPrincipal(asList(rm.PA()))
				.setActiveRetentionPeriod(RetentionPeriod.variable(period42))
				.setSemiActiveRetentionPeriod(RetentionPeriod.variable(period666))
				.setInactiveDisposalType(DisposalType.SORT);
		CopyRetentionRule secondary2_0_D = CopyRetentionRule.newSecondary(asList(rm.PA()), "2-0-D");
		RetentionRule rule = rm.newRetentionRule().setCode("2").setTitle("Rule #2")
				.setResponsibleAdministrativeUnits(true).setApproved(true)
				.setCopyRetentionRules(asList(principal42_666_T, secondary2_0_D));
		recordServices.add(rule);

		assertThat(recordServices.isLogicallyDeletable(period888.getWrappedRecord(), admin)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(period999.getWrappedRecord(), admin)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(period42.getWrappedRecord(), admin)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(period666.getWrappedRecord(), admin)).isFalse();

		rule.getCopyRetentionRules().get(0).setSemiActiveRetentionPeriod(RetentionPeriod.fixed(2));
		recordServices.update(rule);

		assertThat(recordServices.isLogicallyDeletable(period888.getWrappedRecord(), admin)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(period999.getWrappedRecord(), admin)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(period42.getWrappedRecord(), admin)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(period666.getWrappedRecord(), admin)).isTrue();

		recordServices.logicallyDelete(rule.getWrappedRecord(), admin);

		assertThat(recordServices.isLogicallyDeletable(period888.getWrappedRecord(), admin)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(period999.getWrappedRecord(), admin)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(period42.getWrappedRecord(), admin)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(period666.getWrappedRecord(), admin)).isTrue();
	}

}
