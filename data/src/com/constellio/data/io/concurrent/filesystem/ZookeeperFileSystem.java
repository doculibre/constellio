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
package com.constellio.data.io.concurrent.filesystem;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.cloud.SolrZkClient;
import org.apache.zookeeper.KeeperException;

import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.io.concurrent.exception.ConcurrencyIOException;
import com.constellio.data.io.concurrent.exception.OptimisticLockingException;

public class ZookeeperFileSystem implements AtomicFileSystem{
	private SolrZkClient zkClient;
	private boolean retryOnConnLoss;
	
	public ZookeeperFileSystem(String zkServerAddress, Integer zkClientTimeout){
		zkClient = new SolrZkClient(zkServerAddress, zkClientTimeout);
		retryOnConnLoss = true;
	}

	@Override
	public synchronized DataWithVersion readData(String path) {
		org.apache.zookeeper.data.Stat zookeeprStat = new org.apache.zookeeper.data.Stat();
		try {
			byte[] data = zkClient.getData(path, null, zookeeprStat, retryOnConnLoss);
			return new DataWithVersion(data, zookeeprStat.getVersion());
		} catch (KeeperException | InterruptedException e) {
			throw new ConcurrencyIOException(e);
		}
		
	}

	@Override
	public synchronized DataWithVersion writeData(String path, DataWithVersion dataWithVersion) {
		try {
			Integer newVersion = -1;
			if (!exists(path))
				zkClient.makePath(path, retryOnConnLoss);
			
			byte[] data = dataWithVersion.getData();
			if (dataWithVersion.getVersion() == null)
				newVersion = zkClient.setData(path, data, retryOnConnLoss).getVersion();
			else
				newVersion = zkClient.setData(path, data, (Integer)dataWithVersion.getVersion(), retryOnConnLoss).getVersion();
			return new DataWithVersion(data, newVersion);
		} catch (KeeperException.BadVersionException e){
			throw new OptimisticLockingException(e);
		} catch (KeeperException | InterruptedException e) {
			throw new ConcurrencyIOException(e);
		}
	}

	@Override
	public synchronized void delete(String path, Object version) {
		try {
			path = makePathCompatibleWithZookeeper(path);
			if (version == null)
				zkClient.delete(path, -1, retryOnConnLoss);
			else
				zkClient.delete(path, (Integer)version, retryOnConnLoss);
		} catch (InterruptedException | KeeperException e) {
			throw new ConcurrencyIOException(e);
		}
	}

	@Override
	public synchronized List<String> list(String path) {
		try {
			path = makePathCompatibleWithZookeeper(path);
			List<String> children = zkClient.getChildren(path, null, retryOnConnLoss);
			List<String> subPathes = new ArrayList<>();
			for (String child: children)
				subPathes.add(path + "/" + child);
			return subPathes;
		} catch (KeeperException | InterruptedException e) {
			throw new ConcurrencyIOException(e);
		}
		
	}

	@Override
	public synchronized boolean exists(String path) {
		try {
			path = makePathCompatibleWithZookeeper(path);
			return zkClient.exists(path, retryOnConnLoss);
		} catch (KeeperException | InterruptedException e) {
			throw new ConcurrencyIOException(e);
		}
	}

	
	private synchronized String makePathCompatibleWithZookeeper(String path){
		//Zookeeper path should not terminate by '/'
		if (path.length() == 1)
			return path;
		if (path.endsWith("/"))
			return path.substring(0, path.length() - 1);
		return path;
	}

	@Override
	public boolean isDirectory(String path) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public boolean mkdirs(String path) {
		throw new UnsupportedOperationException("TODO");
	}

}
