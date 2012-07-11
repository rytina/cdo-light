/**
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Simon McDuff - initial API and implementation
 *    Simon McDuff - bug 233273
 *    Eike Stepper - maintenance
 *    Andre Dietisheim - bug 256649
 */
package org.eclipse.emf.cdo.internal.server.mem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.branch.CDOBranchHandler;
import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.common.commit.CDOCommitInfo;
import org.eclipse.emf.cdo.common.commit.CDOCommitInfoHandler;
import org.eclipse.emf.cdo.common.id.CDOIDUtil;
import org.eclipse.emf.cdo.common.lob.CDOLobHandler;
import org.eclipse.emf.cdo.common.lock.IDurableLockingManager.LockArea.Handler;
import org.eclipse.emf.cdo.common.model.CDOModelConstants;
import org.eclipse.emf.cdo.common.protocol.CDODataInput;
import org.eclipse.emf.cdo.common.protocol.CDODataOutput;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.CDORevisionHandler;
import org.eclipse.emf.cdo.server.ISession;
import org.eclipse.emf.cdo.server.IStoreAccessor;
import org.eclipse.emf.cdo.server.IStoreAccessor.DurableLocking;
import org.eclipse.emf.cdo.server.IStoreAccessor.QueryXRefsContext;
import org.eclipse.emf.cdo.server.ITransaction;
import org.eclipse.emf.cdo.server.IView;
import org.eclipse.emf.cdo.server.StoreThreadLocal;
import org.eclipse.emf.cdo.server.mem.IMEMStore;
import org.eclipse.emf.cdo.spi.common.branch.InternalCDOBranch;
import org.eclipse.emf.cdo.spi.common.branch.InternalCDOBranchManager;
import org.eclipse.emf.cdo.spi.common.branch.InternalCDOBranchManager.BranchLoader;
import org.eclipse.emf.cdo.spi.common.commit.CDOChangeSetSegment;
import org.eclipse.emf.cdo.spi.common.commit.InternalCDOCommitInfoManager;
import org.eclipse.emf.cdo.spi.common.revision.DetachedCDORevision;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevision;
import org.eclipse.emf.cdo.spi.common.revision.SyntheticCDORevision;
import org.eclipse.emf.cdo.spi.server.DurableLockArea;
import org.eclipse.emf.cdo.spi.server.InternalLockManager;
import org.eclipse.emf.cdo.spi.server.LongIDStore;
import org.eclipse.emf.cdo.spi.server.StoreAccessorPool;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.net4j.util.HexUtil;
import org.eclipse.net4j.util.ObjectUtil;
import org.eclipse.net4j.util.ReflectUtil.ExcludeFromDump;
import org.eclipse.net4j.util.collection.Pair;
import org.eclipse.net4j.util.concurrent.IRWLockManager.LockType;
import org.eclipse.net4j.util.io.IOUtil;
import org.eclipse.net4j.util.om.monitor.OMMonitor;

/**
 * @author Simon McDuff
 */
public class MEMStore extends LongIDStore implements IMEMStore, BranchLoader, DurableLocking
{
  public static final String TYPE = "mem"; //$NON-NLS-1$

  private long creationTime;

  private Map<String, String> properties = new HashMap<String, String>();

  private Map<Integer, BranchInfo> branchInfos = new HashMap<Integer, BranchInfo>();

  private int lastBranchID;

  private int lastLocalBranchID;

  private Map<Long, InternalCDORevision> revisions = new HashMap<Long, InternalCDORevision>();

  private List<CommitInfo> commitInfos = new ArrayList<CommitInfo>();

  private Map<Long, EClass> objectTypes = new HashMap<Long, EClass>();

  private Map<String, LockArea> lockAreas = new HashMap<String, LockArea>();

  private Map<String, Object> lobs = new HashMap<String, Object>();

  private int listLimit;

  @ExcludeFromDump
  private transient EStructuralFeature resourceNameFeature;

private InternalCDORevision currentRevision;

  /**
   * @param listLimit
   *          See {@link #setListLimit(int)}.
   * @since 2.0
   */
  public MEMStore(int listLimit)
  {
    super(TYPE, set(ChangeFormat.REVISION, ChangeFormat.DELTA), set(RevisionTemporality.NONE), set(RevisionParallelism.NONE));
    this.listLimit = listLimit;
  }

  public MEMStore()
  {
    this(UNLIMITED);
  }

  public synchronized Map<String, String> getPersistentProperties(Set<String> names)
  {
    if (names == null || names.isEmpty())
    {
      return new HashMap<String, String>(properties);
    }

    Map<String, String> result = new HashMap<String, String>();
    for (String name : names)
    {
      String value = properties.get(name);
      if (value != null)
      {
        result.put(name, value);
      }
    }

    return result;
  }

  public synchronized void setPersistentProperties(Map<String, String> properties)
  {
    this.properties.putAll(properties);
  }

  public synchronized void removePersistentProperties(Set<String> names)
  {
    for (String name : names)
    {
      properties.remove(name);
    }
  }

  public synchronized Pair<Integer, Long> createBranch(int branchID, BranchInfo branchInfo)
  {
	  return null;
  }

  public synchronized BranchInfo loadBranch(int branchID)
  {
    return branchInfos.get(branchID);
  }

  public synchronized SubBranchInfo[] loadSubBranches(int branchID)
  {
	  return null;
  }

  public synchronized int loadBranches(int startID, int endID, CDOBranchHandler handler)
  {
	  return 0;
  }

  public synchronized void loadCommitInfos(CDOCommitInfoHandler handler)
  {
    InternalCDOCommitInfoManager manager = getRepository().getCommitInfoManager();
    for (int i = 0; i < commitInfos.size(); i++)
    {
      CommitInfo info = commitInfos.get(i);
      info.handle(manager, handler);
    }
  }

  public synchronized Set<Long> readChangeSet(CDOChangeSetSegment[] segments)
  {
    Set<Long> ids = new HashSet<Long>();
    for (CDOChangeSetSegment segment : segments)
    {
        readChangeSet(segment, revisions.values(), ids);
    }

    return ids;
  }

  private void readChangeSet(CDOChangeSetSegment segment, Collection<InternalCDORevision> list, Set<Long> ids)
  {
    boolean listCheckDone = false;
    for (InternalCDORevision revision : list)
    {
      long id = revision.getID();
      if (!listCheckDone)
      {
        if (ids.contains(id))
        {
          return;
        }


        listCheckDone = true;
      }

        ids.add(id);
    }
  }

  public synchronized void handleRevisions(EClass eClass, CDORevisionHandler handler)
  {
      for (InternalCDORevision revision : revisions.values())
      {
        if (!handleRevision(revision, eClass, handler))
        {
          return;
        }
      }
  }

  private boolean handleRevision(InternalCDORevision revision, EClass eClass, CDORevisionHandler handler)
  {
    if (revision instanceof DetachedCDORevision)
    {
      return true;
    }

    if (eClass != null && revision.getEClass() != eClass)
    {
      return true;
    }


    return handler.handleRevision(revision);
  }

  /**
   * @since 2.0
   */
  public int getListLimit()
  {
    return listLimit;
  }

  /**
   * @since 2.0
   */
  public synchronized void setListLimit(int listLimit)
  {
    if (listLimit != UNLIMITED && this.listLimit != listLimit)
    {
        enforceListLimit(revisions.values());
    }

    this.listLimit = listLimit;
  }

  /**
   * @since 2.0
   */
  public synchronized InternalCDORevision getCurrentRevision()
  {
	  return currentRevision;
  }


  /**
   * @since 2.0
   */
  public synchronized InternalCDORevision getRevision(long id)
  {
	  return revisions.get(id);
  }

  public synchronized void addRevision(InternalCDORevision revision, boolean raw)
  {
	  currentRevision = revision;
	  revisions.put(revision.getID(), revision);

	    boolean resource = !(revision instanceof SyntheticCDORevision) && revision.isResource();
	    if (resource && resourceNameFeature == null)
	    {
	      resourceNameFeature = revision.getEClass().getEStructuralFeature(CDOModelConstants.RESOURCE_NODE_NAME_ATTRIBUTE);
	    }

	    if (!raw)
	    {

	      // Check duplicate resource
	      if (resource)
	      {
	        checkDuplicateResource(revision);
	      }
	    }


	    long id = revision.getID();
	    if (!objectTypes.containsKey(id))
	    {
	      objectTypes.put(id, revision.getEClass());
	    }

    if (raw)
    {
      ensureLastObjectID(revision.getID());
    }
  }

  public synchronized void addCommitInfo(String userID, String comment)
  {
    int index = commitInfos.size() - 1;
    while (index >= 0)
    {
      CommitInfo info = commitInfos.get(index);

      --index;
    }

    CommitInfo commitInfo = new CommitInfo(userID, comment);
    commitInfos.add(index + 1, commitInfo);
  }

  /**
   * @since 2.0
   */
  public synchronized boolean rollbackRevision(InternalCDORevision revision)
  {
    long id = revision.getID();

    if (!revisions.containsKey(id))
    {
      return false;
    }

    	revisions.remove(id);
        return true;
  }

  /**
   * @since 3.0
   */
  public synchronized DetachedCDORevision detachObject(long id)
  {
    EClass eClass = getObjectType(id);
    DetachedCDORevision detached = new DetachedCDORevision(eClass, id);
    addRevision(detached, false);
    return detached;
  }

  /**
   * @since 2.0
   */
  public synchronized void queryResources(IStoreAccessor.QueryResourcesContext context)
  {
    long folderID = context.getFolderID();
    String name = context.getName();
    boolean exactMatch = context.exactMatch();

    for (InternalCDORevision revision : revisions.values()) {
        String revisionName = (String)revision.data().get(resourceNameFeature, 0);
        boolean useEquals = exactMatch || revisionName == null || name == null;
        boolean match = useEquals ? ObjectUtil.equals(revisionName, name) : revisionName.startsWith(name);

        if (match)
        {
          if (!context.addResource(revision.getID()))
          {
            // No more results allowed
            break;
          }
        }
	}
  }

  public synchronized void queryXRefs(QueryXRefsContext context)
  {
    Set<Long> targetIDs = context.getTargetObjects().keySet();
    Map<EClass, List<EReference>> sourceCandidates = context.getSourceCandidates();

    for (InternalCDORevision revision : revisions.values())
    {

      EClass eClass = revision.getEClass();
      long sourceID = revision.getID();

      List<EReference> eReferences = sourceCandidates.get(eClass);
      if (eReferences != null)
      {
        for (EReference eReference : eReferences)
        {
          Object value = revision.getValue(eReference);
          if (value != null)
          {
            if (eReference.isMany())
            {
              @SuppressWarnings("unchecked")
              List<Long> ids = (List<Long>)value;
              int index = 0;
              for (Long id : ids)
              {
                if (!queryXRefs(context, targetIDs, id, sourceID, eReference, index++))
                {
                  return;
                }
              }
            }
            else
            {
              Long id = (Long)value;
              if (!queryXRefs(context, targetIDs, id, sourceID, eReference, 0))
              {
                return;
              }
            }
          }
        }
      }
    }
  }

  private boolean queryXRefs(QueryXRefsContext context, Set<Long> targetIDs, long targetID, long sourceID,
      EReference sourceReference, int index)
  {
    for (Long id : targetIDs)
    {
      if (id.equals(targetID))
      {
        if (!context.addXRef(targetID, sourceID, sourceReference, index))
        {
          // No more results allowed
          return false;
        }
      }
    }

    return true;
  }

  public synchronized void rawExport(CDODataOutput out, int fromBranchID, int toBranchID, long fromCommitTime,
      long toCommitTime)
  {
    // TODO: implement MEMStore.rawExport(out, fromBranchID, toBranchID, fromCommitTime, toCommitTime)
    throw new UnsupportedOperationException();
  }

  public synchronized void rawImport(CDODataInput in, int fromBranchID, int toBranchID, long fromCommitTime,
      long toCommitTime, OMMonitor monitor)
  {
    // TODO: implement MEMStore.rawImport(in, fromBranchID, toBranchID, fromCommitTime, toCommitTime, monitor)
    throw new UnsupportedOperationException();
  }

  public synchronized void rawDelete(long id, int version, CDOBranch branch)
  {
	  revisions.remove(id);
  }

  public synchronized LockArea createLockArea(String userID, boolean readOnly,
      Map<Long, LockGrade> locks)
  {
    String durableLockingID;

    do
    {
      durableLockingID = DurableLockArea.createDurableLockingID();
    } while (lockAreas.containsKey(durableLockingID));

    LockArea area = new DurableLockArea(durableLockingID, userID, readOnly, locks);
    lockAreas.put(durableLockingID, area);
    return area;
  }

  public synchronized LockArea getLockArea(String durableLockingID) throws LockAreaNotFoundException
  {
    LockArea area = lockAreas.get(durableLockingID);
    if (area == null)
    {
      throw new LockAreaNotFoundException(durableLockingID);
    }

    return area;
  }

  public synchronized void getLockAreas(String userIDPrefix, Handler handler)
  {
    for (LockArea area : lockAreas.values())
    {
      String userID = area.getUserID();
      if (userID != null && userID.startsWith(userIDPrefix))
      {
        if (!handler.handleLockArea(area))
        {
          return;
        }
      }
    }
  }

  public synchronized void deleteLockArea(String durableLockingID)
  {
    lockAreas.remove(durableLockingID);
  }

  public synchronized void lock(String durableLockingID, LockType type, Collection<? extends Object> objectsToLock)
  {
    LockArea area = getLockArea(durableLockingID);
    Map<Long, LockGrade> locks = area.getLocks();

    InternalLockManager lockManager = getRepository().getLockManager();
    for (Object objectToLock : objectsToLock)
    {
      long id = lockManager.getLockKeyID(objectToLock);
      LockGrade grade = locks.get(id);
      if (grade != null)
      {
        grade = grade.getUpdated(type, true);
      }
      else
      {
        grade = LockGrade.get(type);
      }

      locks.put(id, grade);
    }
  }

  public synchronized void unlock(String durableLockingID, LockType type, Collection<? extends Object> objectsToUnlock)
  {
    LockArea area = getLockArea(durableLockingID);
    Map<Long, LockGrade> locks = area.getLocks();

    InternalLockManager lockManager = getRepository().getLockManager();
    for (Object objectToUnlock : objectsToUnlock)
    {
      long id = lockManager.getLockKeyID(objectToUnlock);
      LockGrade grade = locks.get(id);
      if (grade != null)
      {
        grade = grade.getUpdated(type, false);
        if (grade == LockGrade.NONE)
        {
          locks.remove(id);
        }
      }
    }
  }

  public synchronized void unlock(String durableLockingID)
  {
    LockArea area = getLockArea(durableLockingID);
    Map<Long, LockGrade> locks = area.getLocks();
    locks.clear();
  }

  public synchronized void queryLobs(List<byte[]> ids)
  {
    for (Iterator<byte[]> it = ids.iterator(); it.hasNext();)
    {
      byte[] id = it.next();
      String key = HexUtil.bytesToHex(id);
      if (lobs.containsKey(key))
      {
        it.remove();
      }
    }
  }

  public void handleLobs(long fromTime, long toTime, CDOLobHandler handler) throws IOException
  {
    for (Entry<String, Object> entry : lobs.entrySet())
    {
      byte[] id = HexUtil.hexToBytes(entry.getKey());
      Object lob = entry.getValue();
      if (lob instanceof byte[])
      {
        byte[] blob = (byte[])lob;
        ByteArrayInputStream in = new ByteArrayInputStream(blob);
        OutputStream out = handler.handleBlob(id, blob.length);
        if (out != null)
        {
          try
          {
            IOUtil.copyBinary(in, out, blob.length);
          }
          finally
          {
            IOUtil.close(out);
          }
        }
      }
      else
      {
        char[] clob = (char[])lob;
        CharArrayReader in = new CharArrayReader(clob);
        Writer out = handler.handleClob(id, clob.length);
        if (out != null)
        {
          try
          {
            IOUtil.copyCharacter(in, out, clob.length);
          }
          finally
          {
            IOUtil.close(out);
          }
        }
      }
    }
  }

  public synchronized void loadLob(byte[] id, OutputStream out) throws IOException
  {
    String key = HexUtil.bytesToHex(id);
    Object lob = lobs.get(key);
    if (lob == null)
    {
      throw new IOException("Lob not found: " + key);
    }

    if (lob instanceof byte[])
    {
      byte[] blob = (byte[])lob;
      ByteArrayInputStream in = new ByteArrayInputStream(blob);
      IOUtil.copyBinary(in, out, blob.length);
    }
    else
    {
      char[] clob = (char[])lob;
      CharArrayReader in = new CharArrayReader(clob);
      IOUtil.copyCharacter(in, new OutputStreamWriter(out), clob.length);
    }
  }

  public synchronized void writeBlob(byte[] id, long size, InputStream inputStream) throws IOException
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOUtil.copyBinary(inputStream, out, size);
    lobs.put(HexUtil.bytesToHex(id), out.toByteArray());
  }

  public synchronized void writeClob(byte[] id, long size, Reader reader) throws IOException
  {
    CharArrayWriter out = new CharArrayWriter();
    IOUtil.copyCharacter(reader, out, size);
    lobs.put(HexUtil.bytesToHex(id), out.toCharArray());
  }

  @Override
  public MEMStoreAccessor createReader(ISession session)
  {
    return new MEMStoreAccessor(this, session);
  }

  /**
   * @since 2.0
   */
  @Override
  public MEMStoreAccessor createWriter(ITransaction transaction)
  {
    return new MEMStoreAccessor(this, transaction);
  }

  /**
   * @since 2.0
   */
  public long getCreationTime()
  {
    return creationTime;
  }

  public void setCreationTime(long creationTime)
  {
    this.creationTime = creationTime;
  }

  public boolean isFirstStart()
  {
    return true;
  }

  public synchronized List<CDORevision> getAllRevisions()
  {
    List<CDORevision> result = new ArrayList<CDORevision>(revisions.values());
    return result;
  }

  public synchronized EClass getObjectType(long id)
  {
    return objectTypes.get(id);
  }

  /**
   * @since 2.0
   */
  @Override
  protected void doActivate() throws Exception
  {
    super.doActivate();
    creationTime = System.currentTimeMillis();
  }

  @Override
  protected void doDeactivate() throws Exception
  {
    revisions.clear();
    branchInfos.clear();
    commitInfos.clear();
    objectTypes.clear();
    properties.clear();
    resourceNameFeature = null;
    lastBranchID = 0;
    lastLocalBranchID = 0;
    super.doDeactivate();
  }

  @Override
  protected StoreAccessorPool getReaderPool(ISession session, boolean forReleasing)
  {
    // Pooling of store accessors not supported
    return null;
  }

  @Override
  protected StoreAccessorPool getWriterPool(IView view, boolean forReleasing)
  {
    // Pooling of store accessors not supported
    return null;
  }





  private void checkDuplicateResource(InternalCDORevision revision)
  {
    long revisionFolder = (long)revision.data().getContainerID();
    String revisionName = (String)revision.data().get(resourceNameFeature, 0);

    IStoreAccessor accessor = StoreThreadLocal.getAccessor();

    long resourceID = accessor.readResourceID(revisionFolder, revisionName);
    if (!CDOIDUtil.isNull(resourceID))
    {
      throw new IllegalStateException("Duplicate resource: name=" + revisionName + ", folderID=" + revisionFolder); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  private void enforceListLimit(Collection<InternalCDORevision> list)
  {
    while (list.size() > listLimit)
    {
      list.remove(list.iterator().next());
    }
  }



  /**
   * @author Eike Stepper
   */
  private static final class CommitInfo
  {
    private CDOBranch branch;

    private long timeStamp;

    private long previousTimeStamp;

    private String userID;

    private String comment;

    public CommitInfo(String userID, String comment)
    {
      this.branch = branch;
      this.timeStamp = timeStamp;
      this.previousTimeStamp = previousTimeStamp;
      this.userID = userID;
      this.comment = comment;
    }

    public CDOBranch getBranch()
    {
      return branch;
    }

    public long getTimeStamp()
    {
      return timeStamp;
    }

    public void handle(InternalCDOCommitInfoManager manager, CDOCommitInfoHandler handler)
    {
      CDOCommitInfo commitInfo = manager.createCommitInfo(userID, comment, null);
      handler.handleCommitInfo(commitInfo);
    }

    @Override
    public String toString()
    {
      return MessageFormat.format("CommitInfo[{0}, {1}, {2}, {3}, {4}]", branch, timeStamp, previousTimeStamp, userID,
          comment);
    }
  }
}
