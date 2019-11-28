package com.constellio.data.dao.services.sql;

import com.constellio.data.dao.dto.records.TransactionSqlDTO;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.SQLException;
import java.util.List;

public interface SqlRecordDao<Object> {

	void insert(String query) throws SQLException;

	void insert(Object dto) throws SQLException;

	Object get(int id)
			throws SQLException;
	Object get(String transactionId) throws SQLException;

	ResultSetHandler<Object> realGet(int id)
			throws SQLException;

	List<Object> getAll() throws SQLException ;

	void delete(int id)throws SQLException;

	void deleteAll()throws SQLException;

	void update(Object dto) throws SQLException;

	void increaseVersion() throws SQLException;

	int getCurrentVersion() throws SQLException;

	void flush();
}
