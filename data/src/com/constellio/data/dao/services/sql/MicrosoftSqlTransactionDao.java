package com.constellio.data.dao.services.sql;

import com.constellio.data.dao.dto.records.TransactionSqlDTO;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;
import java.util.List;

public class MicrosoftSqlTransactionDao implements SqlRecordDao<TransactionSqlDTO>{

	private final SqlConnector connector;
	public static final String TABLE_NAME = "transactions";
	public static final String SCHEMA_NAME = "constellio";
	private final QueryRunner queryRunner;
	private ScalarHandler<Integer> defaultHandler = new ScalarHandler<>();

	public MicrosoftSqlTransactionDao(SqlConnector connector){
		queryRunner = new QueryRunner();
		this.connector = connector;
	}

	public MicrosoftSqlTransactionDao(SqlConnector connector, QueryRunner queryRunner){
		this.queryRunner = queryRunner;
		this.connector = connector;
	}

	@Override
	public void insert(String query) throws SQLException {

		int numberOfRowsUpdated = queryRunner.insert(connector.getConnection(), query, defaultHandler);
	}

	@Override
	public void insert(TransactionSqlDTO dto) throws SQLException {

		String insertQuery = "INSERT INTO transactions "
							 +"(transactionUUID,timestamp, logVersion, transactionSummary, content) "
							 + "VALUES (?, CURRENT_TIMESTAMP, ?, ?, ?)";

		queryRunner.insert(connector.getConnection(),
				insertQuery, defaultHandler, dto.getTransactionUUID(),
				dto.getLogVersion(), dto.getTransactionSummary(), dto.getContent());

		//dto.setId(newId);
	}

	@Override
	public TransactionSqlDTO get(int id) throws SQLException {

		ResultSetHandler<TransactionSqlDTO> handler = new BeanHandler<>(TransactionSqlDTO.class);

		TransactionSqlDTO dto = queryRunner.query(connector.getConnection(),
				"SELECT * FROM transactions WHERE id=?", handler, id);

		return dto;
	}

	@Override
	public TransactionSqlDTO get(String transactionId) throws SQLException {

		ResultSetHandler<TransactionSqlDTO> handler = new BeanHandler<>(TransactionSqlDTO.class);

		TransactionSqlDTO dto = queryRunner.query(connector.getConnection(),
				"SELECT TOP 1 * FROM transactions WHERE transactionUUID=?", handler, transactionId);

		return dto;
	}

	@Override
	public ResultSetHandler<TransactionSqlDTO> realGet(int id) throws SQLException {
		return null;
	}

	@Override
	public List<TransactionSqlDTO> getAll()  throws SQLException  {
		ResultSetHandler<List<TransactionSqlDTO>> handler = new BeanListHandler<>(TransactionSqlDTO.class);

		List<TransactionSqlDTO> dto = queryRunner.query(connector.getConnection(),
				"SELECT * FROM transactions", handler);

		return dto;
	}

	@Override
	public void delete(int id) throws SQLException {
		queryRunner.execute(connector.getConnection(),
				"DELETE FROM transactions WHERE id=?",id);
	}

	@Override
	public void deleteAll() throws SQLException {

		queryRunner.execute(connector.getConnection(),
				"DELETE FROM transactions WHERE id > 0");
	}

	@Override
	public void update(TransactionSqlDTO dto) throws SQLException {

		String updateQuery = "UPDATE transactions tr "
							 +"SET tr.transactionUUID=?, tr.timestamp=?, tr.logVersion=?, tr.transactionSummary=?, tr.content=?) "
							 + "WHERE id=?";
		 queryRunner.update(connector.getConnection(), updateQuery,
				dto.getTransactionUUID(),dto.getTimestamp(),dto.getLogVersion(),dto.getTransactionSummary(),dto.getContent(), dto.getId());
	}

	@Override
	public void increaseVersion() throws SQLException {

		queryRunner.update(connector.getConnection(),
				"UPDATE versions SET version = version + 1 WHERE name = 'transactionLog' ");
	}

	@Override
	public int getCurrentVersion() throws SQLException {

		ScalarHandler<Integer> scalarHandler = new ScalarHandler<>();

		Integer version = queryRunner.query(connector.getConnection(),
				"SELECT version FROM versions WHERE name = 'transactionLog' ",scalarHandler);

		if(version == null)
		{
			String insertQuery = "INSERT INTO versions "
								 +"(name, version) "
								 + "VALUES ('transactionLog', 1)";

			queryRunner.insert(connector.getConnection(),
					insertQuery, defaultHandler);
			return 1;
		}

		return version;
	}


	@Override
	public void flush() {

	}


}
