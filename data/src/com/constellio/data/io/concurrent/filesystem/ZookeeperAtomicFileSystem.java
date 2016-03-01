package com.constellio.data.io.concurrent.filesystem;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.cloud.SolrZkClient;
import org.apache.zookeeper.KeeperException;

import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.io.concurrent.exception.AtomicIOException;
import com.constellio.data.io.concurrent.exception.FileNotFoundException;
import com.constellio.data.io.concurrent.exception.OptimisticLockingException;


public class ZookeeperAtomicFileSystem extends AbstractAtomicFileSystem{
	private SolrZkClient zkClient;
	private boolean retryOnConnLoss;
	
	public ZookeeperAtomicFileSystem(String zkServerAddress, Integer zkClientTimeout){
		zkClient = new SolrZkClient(zkServerAddress, zkClientTimeout);
		retryOnConnLoss = true;
	}

	@Override
	public synchronized DataWithVersion readData(String path) {
		org.apache.zookeeper.data.Stat zookeeprStat = new org.apache.zookeeper.data.Stat();
		try {
			byte[] data = zkClient.getData(path, null, zookeeprStat, retryOnConnLoss);
			return new DataWithVersion(data, zookeeprStat.getVersion());
		} catch (KeeperException.NoNodeException e){
			throw new FileNotFoundException(e);
		} catch (KeeperException | InterruptedException e) {
			throw new AtomicIOException(e);
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
			throw new AtomicIOException(e);
		}
	}

	@Override
	public synchronized void delete(String path, Object version) {
		try {
			if (!exists(path))
				return;
			
			path = makePathCompatibleWithZookeeper(path);
			if (path.equals("/") || isDirectory(path)){
				for (String subPath: list(path))
					delete(subPath, version);
					
			} 
			if (!path.equals("/")){
				if (version == null)
					zkClient.delete(path, -1, retryOnConnLoss);
				else
					zkClient.delete(path, (Integer)version, retryOnConnLoss);
			}
		} catch (KeeperException e) {
			throw new OptimisticLockingException(e);
		} catch (InterruptedException e) {
			throw new AtomicIOException(e);
		}
	}

	@Override
	public synchronized List<String> list(String path) {
		try {
			path = makePathCompatibleWithZookeeper(path);
			List<String> children = zkClient.getChildren(path, null, retryOnConnLoss);
			if (children.size() == 0 && !isDirectory(path)){
				return null;
			}
			List<String> subPathes = new ArrayList<>();
			for (String child: children){
				String augmentToPath = augmentToPath(path, child);
				subPathes.add(augmentToPath);
			}
			
			
			return subPathes;
		} catch (KeeperException | InterruptedException e) {
			if (e instanceof KeeperException.NoNodeException)
				return null;
			throw new AtomicIOException(e);
		}
		
	}

	private String augmentToPath(String path, String child) {
		
		if (path.equals("/"))
			return path + child;
		return path + "/" + child;
	}

	@Override
	public synchronized boolean exists(String path) {
		try {
			path = makePathCompatibleWithZookeeper(path);
			return zkClient.exists(path, retryOnConnLoss);
		} catch (KeeperException | InterruptedException e) {
			throw new AtomicIOException(e);
		}
	}

	
	private synchronized String makePathCompatibleWithZookeeper(String path){
		//Zookeeper path should not terminate by '/'
		if (path.length() != 1 && path.endsWith("/"))
			path = path.substring(0, path.length() - 1);
		return path;
	}

	@Override
	public synchronized boolean isDirectory(String path) {
		path = makePathCompatibleWithZookeeper(path);
		
		boolean isDir = false;
		if (exists(path)){
			DataWithVersion dirData = readData(path);
			isDir = dirData.getData() == null ;
		}
		
		return isDir;
	}

	@Override
	public synchronized boolean mkdirs(String path) {
		if (exists(path))
			return false;
		
		try {
			zkClient.makePath(path, true);
		} catch (KeeperException | InterruptedException e) {
			throw new AtomicIOException(e);
		}
		return true;
	}

	@Override
	public synchronized void close(){
		zkClient.close();
		super.close();
	}

	@Override
	public DistributedLock getLock(String path) {
		path = makePathCompatibleWithZookeeper(path);
		mkdirs(path);
		return new ZookeeperLock(zkClient.getSolrZooKeeper(), path);
	}

}
