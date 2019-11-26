package com.constellio.data.dao.services.sql;

import com.constellio.data.dao.dto.records.TransactionSqlDTO;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

public class SqlTransactionsRecordHandlerDao extends BeanListHandler<TransactionSqlDTO> {

	private Connection connection;

	public SqlTransactionsRecordHandlerDao(Connection connection){
		super(TransactionSqlDTO.class);
		this.connection = connection;

	}

	@Override
	public List<TransactionSqlDTO> handle(ResultSet rs) throws SQLException {
		List<TransactionSqlDTO> transactions = super.handle(rs);

		return transactions;
	}

	ResultSetHandler<Object[]> h = new ResultSetHandler<Object[]>() {
		public Object[] handle(ResultSet rs) throws SQLException {
			if (!rs.next()) {
				return null;
			}

			ResultSetMetaData meta = rs.getMetaData();
			int cols = meta.getColumnCount();
			Object[] result = new Object[cols];

			for (int i = 0; i < cols; i++) {
				result[i] = rs.getObject(i + 1);
			}

			return result;
		}
	};

}
