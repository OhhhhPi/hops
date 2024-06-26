/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.hops.transaction.lock;

import io.hops.metadata.common.entity.Variable;
import io.hops.metadata.hdfs.entity.INodeIdentifier;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.XAttr;
import org.apache.hadoop.hdfs.DFSConfigKeys;
import org.apache.hadoop.hdfs.protocol.DatanodeID;
import org.apache.hadoop.ipc.RetryCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LockFactory {

  private final static LockFactory instance = new LockFactory();
  
  
  public static enum BLK {
    /**
     * Replica
     */
    RE,
    /**
     * CorruptReplica
     */
    CR,
    /**
     * ExcessReplica
     */
    ER,
    /**
     * UnderReplicated
     */
    UR,
    /**
     * ReplicaUnderConstruction
     */
    UC,
    /**
     * InvalidatedBlock
     */
    IV,
    /**
     * PendingBlock
     */
    PE,
    /**
     * Cached Blocks
     */
    CA
  }

  private LockFactory() {

  }

  public static LockFactory getInstance() {
    return instance;
  }

  public Lock getBlockChecksumLock(String target, int blockIndex) {
    return new BlockChecksumLock(target, blockIndex);
  }

  public Lock getBlockLock() {
    return new BlockLock();
  }
  
  public Lock getBlockLock(long blockId, INodeIdentifier inode) {
    return new BlockLock(blockId, inode);
  }

  public Lock getReplicaLock() {
    return new BlockRelatedLock(Lock.Type.Replica);
  }

  public Lock getCorruptReplicaLock() {
    return new BlockRelatedLock(Lock.Type.CorruptReplica);
  }

  public Lock getExcessReplicaLock() {
    return new BlockRelatedLock(Lock.Type.ExcessReplica);
  }

  public Lock getReplicatUnderConstructionLock() {
    return new BlockRelatedLock(Lock.Type.ReplicaUnderConstruction);
  }

  public Lock getInvalidatedBlockLock() {
    return new BlockRelatedLock(Lock.Type.InvalidatedBlock);
  }

  public Lock getUnderReplicatedBlockLock() {
    return new BlockRelatedLock(Lock.Type.UnderReplicatedBlock);
  }

  public Lock getPendingBlockLock() {
    return new BlockRelatedLock(Lock.Type.PendingBlock);
  }

  public Lock getCachedBlockLock() {
    return new BlockRelatedLock(Lock.Type.CachedBlock);
  }
  
  public Lock getSqlBatchedBlocksLock() {
    return new SqlBatchedBlocksLock();
  }

  public Lock getSqlBatchedReplicasLock() {
    return new SqlBatchedBlocksRelatedLock(Lock.Type.Replica);
  }

  public Lock getSqlBatchedCorruptReplicasLock() {
    return new SqlBatchedBlocksRelatedLock(Lock.Type.CorruptReplica);
  }

  public Lock getSqlBatchedExcessReplicasLock() {
    return new SqlBatchedBlocksRelatedLock(Lock.Type.ExcessReplica);
  }

  public Lock getSqlBatchedReplicasUnderConstructionLock() {
    return new SqlBatchedBlocksRelatedLock(Lock.Type.ReplicaUnderConstruction);
  }

  public Lock getSqlBatchedInvalidatedBlocksLock() {
    return new SqlBatchedBlocksRelatedLock(Lock.Type.InvalidatedBlock);
  }

  public Lock getSqlBatchedUnderReplicatedBlocksLock() {
    return new SqlBatchedBlocksRelatedLock(Lock.Type.UnderReplicatedBlock);
  }

  public Lock getSqlBatchedPendingBlocksLock() {
    return new SqlBatchedBlocksRelatedLock(Lock.Type.PendingBlock);
  }

  public Lock getSqlBatchedCachedBlockLock() {
    return new SqlBatchedBlocksRelatedLock(Lock.Type.CachedBlock);
  }
  
  public Lock getIndividualBlockLock(long blockId, INodeIdentifier inode) {
    return new IndividualBlockLock(blockId, inode);
  }

  public Lock getBatchedINodesLock(List<INodeIdentifier> inodeIdentifiers) {
    return new BatchedINodeLock(inodeIdentifiers);
  }

  public Lock getMultipleINodesLock(List<INodeIdentifier> inodeIdentifiers,
                                    TransactionLockTypes.INodeLockType lockType) {
    return new MultipleINodesLock(inodeIdentifiers, lockType);
  }

  /**
   * @param lockType the lock to acquire
   * @param inodeIdentifier the id of the inode
   * @param readUpPathInodes if true, you'll get locks for the parent inodes too
   */
  public Lock getIndividualINodeLock(
      TransactionLockTypes.INodeLockType lockType,
      INodeIdentifier inodeIdentifier, boolean readUpPathInodes) {
    return new IndividualINodeLock(lockType, inodeIdentifier, readUpPathInodes);
  }

  public Lock getIndividualINodeLock(
      TransactionLockTypes.INodeLockType lockType,
      INodeIdentifier inodeIdentifier) {
    return new IndividualINodeLock(lockType, inodeIdentifier);
  }

  public Lock getINodesLocks(TransactionLockTypes.INodeLockType lockType, List<INodeIdentifier> inodeIdentifiers) {
    return new INodesLocks(lockType, inodeIdentifiers);
  }
  
  public INodeLock getINodeLock(TransactionLockTypes.INodeLockType lockType,
                                TransactionLockTypes.INodeResolveType resolveType, String... paths) {
    return new INodeLock(lockType, resolveType, paths);
  }

  public INodeLock getINodeLock(TransactionLockTypes.INodeLockType lockType,
                                TransactionLockTypes.INodeResolveType resolveType, long inodeId) {
    return new INodeLock(lockType, resolveType, inodeId);
  }

  public INodeLock getRenameINodeLock(TransactionLockTypes.INodeLockType lockType,
      TransactionLockTypes.INodeResolveType resolveType, String src, String dst) {
    return new RenameINodeLock(lockType, resolveType, src, dst);
  }

  public INodeLock getLegacyRenameINodeLock(TransactionLockTypes.INodeLockType lockType,
      TransactionLockTypes.INodeResolveType resolveType,
      String src, String dst) {
    return new RenameINodeLock(lockType, resolveType, src, dst, true);
  }

  public Lock getLeaseLockAllPaths(TransactionLockTypes.LockType lockType,
                                   String leaseHolder, int leaseCreationLockRows) {
    return new LeaseLock(lockType, TransactionLockTypes.LeaseHolderResolveType.ALL_PATHS,
            leaseHolder, null, leaseCreationLockRows);
  }

  public Lock getLeaseLockSinglePath(TransactionLockTypes.LockType lockType,
                                     String leaseHolder, String path, int leaseCreationLockRows) {
    return new LeaseLock(lockType, TransactionLockTypes.LeaseHolderResolveType.SINGLE_PATH,
            leaseHolder, path, leaseCreationLockRows);
  }

  public Lock getLeaseLockAllPaths(TransactionLockTypes.LockType lockType, int leaseCreationLockRows) {
    return new LeaseLock(lockType, leaseCreationLockRows);
  }

  public Lock getLeaseLockAllSystemPathsTesting(TransactionLockTypes.LockType lockType,
                                    int leaseCreationLockRows) {
    return new LeaseLock(lockType, TransactionLockTypes.LeaseHolderResolveType.ALL_SYSTEM_PATHS_FOR_TESTSING, null,
            null, leaseCreationLockRows);
  }

  public Lock getLeasePathLock(int expectedCount) {
    return new LeasePathLock(TransactionLockTypes.LockType.READ_COMMITTED, expectedCount);
  }

  public Lock getLeasePathLock() {
    return new LeasePathLock(TransactionLockTypes.LockType.READ_COMMITTED);
  }

  public Lock getLeasePathLock(String src) {
    return new LeasePathLock(TransactionLockTypes.LockType.READ_COMMITTED, src);
  }

  public Lock getNameNodeLeaseLock(TransactionLockTypes.LockType lockType) {
    return new NameNodeLeaseLock(lockType);
  }

  public Lock getQuotaUpdateLock(final long inodeID, final int limit) {
    return new QuotaUpdateLock(inodeID, limit);
  }

  public Lock getVariableLock(Variable.Finder[] finders,
      TransactionLockTypes.LockType[] lockTypes) {
    assert finders.length == lockTypes.length;
    VariablesLock lock = new VariablesLock();
    for (int i = 0; i < finders.length; i++) {
      lock.addVariable(finders[i], lockTypes[i]);
    }
    return lock;
  }

  public Lock getVariableLock(Variable.Finder finder,
      TransactionLockTypes.LockType lockType) {
    VariablesLock lock = new VariablesLock();
    lock.addVariable(finder, lockType);
    return lock;
  }

  public List<Lock> getBlockReportingLocks(long[] blockIds, long[] inodeIds, long[] unresolvedBlks, int storageId) {
    ArrayList<Lock> list = new ArrayList(3);
    list.add(new BatchedBlockLock(blockIds,inodeIds, unresolvedBlks));
    //list.add(new BatchedBlocksRelatedLock.BatchedInvalidatedBlocksLock(storageId));
    return list;
  }

  public Lock getEncodingStatusLock(TransactionLockTypes.LockType lockType,
      String... targets) {
    return new BaseEncodingStatusLock.EncodingStatusLock(lockType, targets);
  }

  public Lock getEncodingStatusLock(boolean includeChildren, TransactionLockTypes.LockType lockType,
      String... targets) {
    return new BaseEncodingStatusLock.EncodingStatusLock(includeChildren, lockType, targets);
  }
  public Lock getIndivdualEncodingStatusLock(
      TransactionLockTypes.LockType lockType, long inodeId) {
    return new BaseEncodingStatusLock.IndividualEncodingStatusLock(lockType,
        inodeId);
  }
  
  public Lock getBatchedEncodingStatusLock(
      TransactionLockTypes.LockType lockType, List<INodeIdentifier> inodeIds) {
    return new BaseEncodingStatusLock.BatchedEncodingStatusLock(lockType,inodeIds);
  }
  
  public Lock getSubTreeOpsLock(TransactionLockTypes.LockType lockType, 
          String pathPrefix, boolean prefix) {
    return new SubTreeOpLock(lockType, pathPrefix, prefix);
  }
  
  public Lock getIndividualHashBucketLock(int storageId, int bucketId) {
    return new IndividualHashBucketLock(storageId, bucketId);
  }
  
  public Lock getHashBucketLock(int storageId) {
    return new HashBucketLock(storageId);
  }
  
  public Lock getLastBlockHashBucketsLock(){
    return new LastBlockReplicasHashBucketLock();
  }

  public Lock getAllUsedHashBucketsLock() {
    return new HashBucketsLocksAllFileBlocks();
  }

  public Lock getRetryCacheEntryLock(byte[] clientId, int callId, long epoch){
    return new RetryCacheEntryLock(clientId, callId, epoch);
  }
  
  public Lock getRetryCacheEntryLock(List<RetryCache.CacheEntry> entries){
    return new RetryCacheEntryLock(entries);
  }
  
  public Collection<Lock> getBlockRelated(BLK... relatedBlks) {
    ArrayList<Lock> list = new ArrayList();
    for (BLK b : relatedBlks) {
      switch (b) {
        case RE:
          list.add(getReplicaLock());
          break;
        case CR:
          list.add(getCorruptReplicaLock());
          break;
        case IV:
          list.add(getInvalidatedBlockLock());
          break;
        case PE:
          list.add(getPendingBlockLock());
          break;
        case UC:
          list.add(getReplicatUnderConstructionLock());
          break;
        case UR:
          list.add(getUnderReplicatedBlockLock());
          break;
        case ER:
          list.add(getExcessReplicaLock());
          break;
        case CA:
          list.add(getCachedBlockLock());
          break;
      }
    }
    return list;
  }
  
  public Collection<Lock> getSqlBatchedBlocksRelated(BLK... relatedBlks) {
    ArrayList<Lock> list = new ArrayList();
    for (BLK b : relatedBlks) {
      switch (b) {
        case RE:
          list.add(getSqlBatchedReplicasLock());
          break;
        case CR:
          list.add(getSqlBatchedCorruptReplicasLock());
          break;
        case IV:
          list.add(getSqlBatchedInvalidatedBlocksLock());
          break;
        case PE:
          list.add(getSqlBatchedPendingBlocksLock());
          break;
        case UC:
          list.add(getSqlBatchedReplicasUnderConstructionLock());
          break;
        case UR:
          list.add(getUnderReplicatedBlockLock());
          break;
        case ER:
          list.add(getSqlBatchedExcessReplicasLock());
          break;
        case CA:
          list.add(getSqlBatchedCachedBlockLock());
      }
    }
    return list;
  }

  public Lock getLastTwoBlocksLock(String src){
    return new LastTwoBlocksLock(src);
  }

  public Lock getLastTwoBlocksLock(long fileId){
    return new LastTwoBlocksLock(fileId);
  }
  
  public Lock getAcesLock(){
    return new AcesLock();
  }
  
  public Lock getEZLock(){
    return new EZLock();
  }
  
  public void setConfiguration(Configuration conf) {
    BaseINodeLock.enableSetPartitionKey(
        conf.getBoolean(DFSConfigKeys.DFS_SET_PARTITION_KEY_ENABLED,
            DFSConfigKeys.DFS_SET_PARTITION_KEY_ENABLED_DEFAULT));
    BaseINodeLock.enableSetRandomPartitionKey(conf.getBoolean(DFSConfigKeys
        .DFS_SET_RANDOM_PARTITION_KEY_ENABLED, DFSConfigKeys
        .DFS_SET_RANDOM_PARTITION_KEY_ENABLED_DEFAULT));
    XAttrLock.setBacthLockSize(conf.getInt(DFSConfigKeys.DFS_DIR_XATTR_LOCK_BATCH_SIZE_KEY,
            DFSConfigKeys.DFS_DIR_XATTR_LOCK_BATCH_SIZE_DEFAULT));
  }  
  public Lock getCacheDirectiveLock(long id) {
    return new CacheDirectiveLock(id);
  }
  
  public Lock getCacheDirectiveLock(String poolName) {
    return new CacheDirectiveLock(poolName);
  }
  
  public Lock getCacheDirectiveLock(long id, final String path, final String pool, final int maxNumResults){
    return new CacheDirectiveLock(id, path, pool, maxNumResults);
  }
  
  public Lock getCachePoolLock(String poolName) {
    return new CachePoolLock(poolName);
  }
  
  public Lock getCachePoolsLock(List<String> poolNames) {
    return new CachePoolLock(poolNames);
  }
  
  public Lock getCachePoolLock(TransactionLockTypes.LockType lockType) {
    return new CachePoolLock(lockType);
  }
    
  public Lock getCachedBlockReportingLocks(List<Long> blockIds, DatanodeID datanodeId) {
    return new CachedBlockLock(blockIds, datanodeId);
  }
  
  public Lock getDatanodeCachedBlockLocks(DatanodeID datanodeId) {
    return new CachedBlockLock(datanodeId);
  }
  
  public Lock getAllCachedBlockLocks(){
    return new AllCachedBlockLock();
  }
  
  public Lock getXAttrLock(){ return new XAttrLock(); }
  
  public Lock getXAttrLock(XAttr attr){ return new XAttrLock(attr); }
  
  public Lock getXAttrLock(XAttr attr, String path){ return new XAttrLock(attr, path); }
  
  public Lock getXAttrLock(List<XAttr> attrs){ return new XAttrLock(attrs); }
  
}
