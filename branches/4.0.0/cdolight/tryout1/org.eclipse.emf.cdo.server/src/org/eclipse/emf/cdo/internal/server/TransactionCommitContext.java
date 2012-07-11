/**
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Simon McDuff - initial API and implementation
 *    Eike Stepper - maintenance
 *    Martin Fluegge - maintenance, bug 318518
 */
package org.eclipse.emf.cdo.internal.server;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.common.commit.CDOCommitData;
import org.eclipse.emf.cdo.common.commit.CDOCommitInfo;
import org.eclipse.emf.cdo.common.id.CDOIDReference;
import org.eclipse.emf.cdo.common.id.CDOIDUtil;
import org.eclipse.emf.cdo.common.model.CDOModelUtil;
import org.eclipse.emf.cdo.common.model.CDOPackageUnit;
import org.eclipse.emf.cdo.common.revision.CDOIDAndBranch;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.delta.CDOAddFeatureDelta;
import org.eclipse.emf.cdo.common.revision.delta.CDOFeatureDelta;
import org.eclipse.emf.cdo.common.revision.delta.CDOFeatureDeltaVisitor;
import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
import org.eclipse.emf.cdo.common.revision.delta.CDOSetFeatureDelta;
import org.eclipse.emf.cdo.common.util.CDOQueryInfo;
import org.eclipse.emf.cdo.internal.common.commit.CDOCommitDataImpl;
import org.eclipse.emf.cdo.internal.common.commit.FailureCommitInfo;
import org.eclipse.emf.cdo.internal.common.model.CDOPackageRegistryImpl;
import org.eclipse.emf.cdo.internal.server.bundle.OM;
import org.eclipse.emf.cdo.server.ContainmentCycleDetectedException;
import org.eclipse.emf.cdo.server.IStoreAccessor;
import org.eclipse.emf.cdo.server.IStoreAccessor.QueryXRefsContext;
import org.eclipse.emf.cdo.server.StoreThreadLocal;
import org.eclipse.emf.cdo.spi.common.commit.InternalCDOCommitInfoManager;
import org.eclipse.emf.cdo.spi.common.model.InternalCDOPackageInfo;
import org.eclipse.emf.cdo.spi.common.model.InternalCDOPackageRegistry;
import org.eclipse.emf.cdo.spi.common.model.InternalCDOPackageUnit;
import org.eclipse.emf.cdo.spi.common.revision.CDOFeatureDeltaVisitorImpl;
import org.eclipse.emf.cdo.spi.common.revision.DetachedCDORevision;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevision;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevisionDelta;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevisionManager;
import org.eclipse.emf.cdo.spi.common.revision.StubCDORevision;
import org.eclipse.emf.cdo.spi.server.InternalCommitContext;
import org.eclipse.emf.cdo.spi.server.InternalLockManager;
import org.eclipse.emf.cdo.spi.server.InternalRepository;
import org.eclipse.emf.cdo.spi.server.InternalSession;
import org.eclipse.emf.cdo.spi.server.InternalTransaction;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.net4j.util.CheckUtil;
import org.eclipse.net4j.util.StringUtil;
import org.eclipse.net4j.util.collection.IndexedList;
import org.eclipse.net4j.util.concurrent.IRWLockManager.LockType;
import org.eclipse.net4j.util.io.ExtendedDataInputStream;
import org.eclipse.net4j.util.om.monitor.Monitor;
import org.eclipse.net4j.util.om.monitor.OMMonitor;

/**
 * @author Simon McDuff
 * @since 2.0
 */
public class TransactionCommitContext implements InternalCommitContext
{
  private static final InternalCDORevision DETACHED = new StubCDORevision(null);

  private InternalTransaction transaction;

  private InternalRepository repository;

  private InternalCDORevisionManager revisionManager;

  private InternalLockManager lockManager;

  private TransactionPackageRegistry packageRegistry;

  private IStoreAccessor accessor;

  private String commitComment;

  private InternalCDOPackageUnit[] newPackageUnits = new InternalCDOPackageUnit[0];

  private InternalCDORevision[] newObjects = new InternalCDORevision[0];

  private InternalCDORevisionDelta[] dirtyObjectDeltas = new InternalCDORevisionDelta[0];

  private Long[] detachedObjects = new Long[0];

  private Map<Long, EClass> detachedObjectTypes;

  private InternalCDORevision[] dirtyObjects = new InternalCDORevision[0];

  private InternalCDORevision[] cachedDetachedRevisions = new InternalCDORevision[0];

  private Map<Long, InternalCDORevision> cachedRevisions;

  private Set<Object> lockedObjects = new HashSet<Object>();

  private List<Long> lockedTargets;

  private String rollbackMessage;

  private List<CDOIDReference> xRefs;

  private boolean ensuringReferentialIntegrity;

  private boolean autoReleaseLocksEnabled;

  private ExtendedDataInputStream lobs;

  public TransactionCommitContext(InternalTransaction transaction)
  {
    this.transaction = transaction;

    repository = transaction.getRepository();
    revisionManager = repository.getRevisionManager();
    lockManager = repository.getLockManager();
    ensuringReferentialIntegrity = repository.isEnsuringReferentialIntegrity();

    packageRegistry = new TransactionPackageRegistry(repository.getPackageRegistry(false));
    packageRegistry.activate();
  }

  public InternalTransaction getTransaction()
  {
    return transaction;
  }


  public String getUserID()
  {
    return transaction.getSession().getUserID();
  }

  public String getCommitComment()
  {
    return commitComment;
  }

  public boolean isAutoReleaseLocksEnabled()
  {
    return autoReleaseLocksEnabled;
  }

  public String getRollbackMessage()
  {
    return rollbackMessage;
  }

  public List<CDOIDReference> getXRefs()
  {
    return xRefs;
  }

  public InternalCDOPackageRegistry getPackageRegistry()
  {
    return packageRegistry;
  }

  public InternalCDOPackageUnit[] getNewPackageUnits()
  {
    return newPackageUnits;
  }

  public InternalCDORevision[] getNewObjects()
  {
    return newObjects;
  }

  public InternalCDORevision[] getDirtyObjects()
  {
    return dirtyObjects;
  }

  public Long[] getDetachedObjects()
  {
    return detachedObjects;
  }

  public Map<Long, EClass> getDetachedObjectTypes()
  {
    return detachedObjectTypes;
  }

  public InternalCDORevision[] getDetachedRevisions()
  {
    // TODO This array can contain null values as they only come from the cache
    for (InternalCDORevision cachedDetachedRevision : cachedDetachedRevisions)
    {
      if (cachedDetachedRevision == null)
      {
        throw new AssertionError("Detached revisions are incomplete");
      }
    }

    return cachedDetachedRevisions;
  }

  public InternalCDORevisionDelta[] getDirtyObjectDeltas()
  {
    return dirtyObjectDeltas;
  }

  public CDORevision getRevision(long id)
  {
    if (cachedRevisions == null)
    {
      cachedRevisions = cacheRevisions();
    }

    // Try "after state"
    InternalCDORevision revision = cachedRevisions.get(id);
    if (revision == DETACHED)
    {
      return null;
    }

    if (revision != null)
    {
      return revision;
    }

    // Fall back to "before state"
    return transaction.getRevision(id);
  }

  private Map<Long, InternalCDORevision> cacheRevisions()
  {
    Map<Long, InternalCDORevision> cache = new HashMap<Long, InternalCDORevision>();
    if (newObjects != null)
    {
      for (int i = 0; i < newObjects.length; i++)
      {
        InternalCDORevision revision = newObjects[i];
        cache.put(revision.getID(), revision);
      }
    }

    if (dirtyObjects != null)
    {
      for (int i = 0; i < dirtyObjects.length; i++)
      {
        InternalCDORevision revision = dirtyObjects[i];
        cache.put(revision.getID(), revision);
      }
    }

    if (detachedObjects != null)
    {
      for (int i = 0; i < detachedObjects.length; i++)
      {
        cache.put(detachedObjects[i], DETACHED);
      }
    }

    return cache;
  }



  public void preWrite()
  {
    // Allocate a store writer
    accessor = repository.getStore().getWriter(transaction);

    // Make the store writer available in a ThreadLocal variable
    StoreThreadLocal.setAccessor(accessor);
    StoreThreadLocal.setCommitContext(this);
  }

  public void setNewPackageUnits(InternalCDOPackageUnit[] newPackageUnits)
  {
    this.newPackageUnits = newPackageUnits;
  }

  public void setNewObjects(InternalCDORevision[] newObjects)
  {
    this.newObjects = newObjects;
  }

  public void setDirtyObjectDeltas(InternalCDORevisionDelta[] dirtyObjectDeltas)
  {
    this.dirtyObjectDeltas = dirtyObjectDeltas;
  }

  public void setDetachedObjects(Long[] detachedObjects)
  {
    this.detachedObjects = detachedObjects;
  }

  public void setDetachedObjectTypes(Map<Long, EClass> detachedObjectTypes)
  {
    this.detachedObjectTypes = detachedObjectTypes;
  }

  public void setAutoReleaseLocksEnabled(boolean on)
  {
    autoReleaseLocksEnabled = on;
  }

  public void setCommitComment(String commitComment)
  {
    this.commitComment = commitComment;
  }

  public ExtendedDataInputStream getLobs()
  {
    return lobs;
  }

  public void setLobs(ExtendedDataInputStream in)
  {
    lobs = in;
  }

  /**
   * @since 2.0
   */
  public void write(OMMonitor monitor)
  {
    try
    {
      monitor.begin(107);
      dirtyObjects = new InternalCDORevision[dirtyObjectDeltas.length];

      lockObjects(); // Can take long and must come before setTimeStamp()
      monitor.worked();

      adjustForCommit();
      monitor.worked();

      computeDirtyObjects(monitor.fork());

      checkXRefs();
      monitor.worked();
      if (rollbackMessage != null)
      {
        return;
      }

      detachObjects(monitor.fork());
      repository.notifyWriteAccessHandlers(transaction, this, true, monitor.fork());
      accessor.write(this, monitor.fork(100));
    }
    catch (Throwable t)
    {
      handleException(t);
    }
    finally
    {
      finishMonitor(monitor);
    }
  }

  public void commit(OMMonitor monitor)
  {
    try
    {
      monitor.begin(101);
      accessor.commit(monitor.fork(100));
      updateInfraStructure(monitor.fork());

      // Bugzilla 297940
      repository.endCommit();
    }
    catch (Throwable ex)
    {
      handleException(ex);
    }
    finally
    {
      finishMonitor(monitor);
    }
  }

  private void handleException(Throwable ex)
  {
    try
    {
      OM.LOG.error(ex);
      String storeClass = repository.getStore().getClass().getSimpleName();
      rollback("Rollback in " + storeClass + ": " + StringUtil.formatException(ex)); //$NON-NLS-1$ //$NON-NLS-2$
    }
    catch (Exception ex1)
    {
      if (rollbackMessage == null)
      {
        rollbackMessage = ex1.getMessage();
      }

      try
      {
        OM.LOG.error(ex1);
      }
      catch (Exception ignore)
      {
      }
    }
  }

  private void finishMonitor(OMMonitor monitor)
  {
    try
    {
      monitor.done();
    }
    catch (Exception ex)
    {
      try
      {
        OM.LOG.warn(ex);
      }
      catch (Exception ignore)
      {
      }
    }
  }


  public void postCommit(boolean success)
  {
    try
    {
      InternalSession sender = transaction.getSession();
      CDOCommitInfo commitInfo = success ? createCommitInfo() : createFailureCommitInfo();

      repository.sendCommitNotification(sender, commitInfo);
    }
    catch (Exception ex)
    {
      OM.LOG.warn("A problem occured while notifying other sessions", ex);
    }
    finally
    {
      StoreThreadLocal.release();
      accessor = null;
      lockedTargets = null;

      if (packageRegistry != null)
      {
        packageRegistry.deactivate();
        packageRegistry = null;
      }
    }
  }

  public CDOCommitInfo createCommitInfo()
  {
    String userID = transaction.getSession().getUserID();
    CDOCommitData commitData = createCommitData();

    InternalCDOCommitInfoManager commitInfoManager = repository.getCommitInfoManager();
    return commitInfoManager.createCommitInfo( userID, commitComment, commitData);
  }

  public CDOCommitInfo createFailureCommitInfo()
  {
    return new FailureCommitInfo();
  }

  private CDOCommitData createCommitData()
  {
    List<CDOPackageUnit> newPackageUnitsCollection = new IndexedList.ArrayBacked<CDOPackageUnit>()
    {
      @Override
      protected CDOPackageUnit[] getArray()
      {
        return newPackageUnits;
      }
    };

    List<CDORevision> newObjectsCollection = new IndexedList.ArrayBacked<CDORevision>()
    {
      @Override
      protected CDORevision[] getArray()
      {
        return newObjects;
      }
    };

    List<CDORevisionDelta> changedObjectsCollection = new IndexedList.ArrayBacked<CDORevisionDelta>()
    {
      @Override
      protected CDORevisionDelta[] getArray()
      {
        return dirtyObjectDeltas;
      }
    };

    List<Long> detachedObjectsCollection = new IndexedList<Long>()
    {
      @Override
      public Long get(int i)
      {
        if (cachedDetachedRevisions[i] != null)
        {
          return cachedDetachedRevisions[i].getID();
        }

        return detachedObjects[i];
      }

      @Override
      public int size()
      {
        return detachedObjects.length;
      }
    };

    return new CDOCommitDataImpl(newPackageUnitsCollection, newObjectsCollection, changedObjectsCollection,
        detachedObjectsCollection);
  }

  protected void adjustForCommit()
  {
    for (InternalCDORevision newObject : newObjects)
    {
      newObject.adjustForCommit();
    }
  }

  protected void lockObjects() throws InterruptedException
  {
    lockedObjects.clear();
    lockedTargets = null;

    try
    {

      CDOFeatureDeltaVisitor deltaTargetLocker = null;
      if (ensuringReferentialIntegrity)
      {
        final Set<Long> newIDs = new HashSet<Long>();
        for (int i = 0; i < newObjects.length; i++)
        {
          InternalCDORevision newRevision = newObjects[i];
          long newID = newRevision.getID();
            newIDs.add(newID);
        }

        deltaTargetLocker = new CDOFeatureDeltaVisitorImpl()
        {
          @Override
          public void visit(CDOAddFeatureDelta delta)
          {
            lockTarget(delta.getValue(), newIDs, false);
          }

          @Override
          public void visit(CDOSetFeatureDelta delta)
          {
            lockTarget(delta.getValue(), newIDs, false);
          }
        };

      }

      for (int i = 0; i < dirtyObjectDeltas.length; i++)
      {
        InternalCDORevisionDelta delta = dirtyObjectDeltas[i];
        long id = delta.getID();
        Object key = lockManager.getLockKey(id);
        lockedObjects.add(new DeltaLockWrapper(key, delta));

        if (hasContainmentChanges(delta))
        {
          if (isContainerLocked(delta))
          {
            throw new ContainmentCycleDetectedException("Parent (" + key
                + ") is already locked for containment changes");
          }
        }
      }

      for (int i = 0; i < dirtyObjectDeltas.length; i++)
      {
        InternalCDORevisionDelta delta = dirtyObjectDeltas[i];
        if (deltaTargetLocker != null)
        {
          delta.accept(deltaTargetLocker);
        }
      }

      for (int i = 0; i < detachedObjects.length; i++)
      {
        long id = detachedObjects[i];
        Object key = lockManager.getLockKey(id);
        lockedObjects.add(key);
      }

      if (!lockedObjects.isEmpty())
      {
        // First lock all objects (incl. possible ref targets).
        // This is a transient operation, it does not check for existance!
        lockManager.lock(LockType.WRITE, transaction, lockedObjects, 1000);

        // If all locks could be acquired, check if locked targets do still exist
        if (lockedTargets != null)
        {
          for (long id : lockedTargets)
          {
            InternalCDORevision revision = //
            revisionManager.getRevision(id, CDORevision.UNCHUNKED, CDORevision.DEPTH_NONE, true);

            if (revision == null || revision instanceof DetachedCDORevision)
            {
              throw new IllegalStateException("Object " + id
                  + " can not be referenced anymore because it has been detached");
            }
          }
        }
      }
    }
    catch (RuntimeException ex)
    {
      lockedObjects.clear();
      lockedTargets = null;
      throw ex;
    }
  }

  /**
   * Iterates up the eContainers of an object and returns <code>true</code> on the first parent locked by another view.
   * 
   * @return <code>true</code> if any parent is locked, <code>false</code> otherwise.
   */
  private boolean isContainerLocked(InternalCDORevisionDelta delta)
  {
    long id = delta.getID();
    InternalCDORevision revision = revisionManager.getRevision(id, CDORevision.UNCHUNKED, CDORevision.DEPTH_INFINITE, true);
    if (revision == null)
    {
      // Can happen with non-auditing cache
      throw new ConcurrentModificationException("Attempt by " + transaction + " to modify historical revision: "
          + delta);
    }

    return isContainerLocked(revision);
  }

  private boolean isContainerLocked(InternalCDORevision revision)
  {
    long id = revision.getContainerID();
    if (CDOIDUtil.isNull(id))
    {
      return false;
    }

    Object key = lockManager.getLockKey(id);
    DeltaLockWrapper lockWrapper = new DeltaLockWrapper(key, null);

    if (lockManager.hasLockByOthers(LockType.WRITE, transaction, lockWrapper))
    {
      Object object = lockManager.getLockEntryObject(lockWrapper);
      if (object instanceof DeltaLockWrapper)
      {
        InternalCDORevisionDelta delta = ((DeltaLockWrapper)object).getDelta();
        if (delta != null && hasContainmentChanges(delta))
        {
          return true;
        }
      }
    }

    InternalCDORevision parent = revisionManager.getRevision(id, CDORevision.UNCHUNKED,
        CDORevision.DEPTH_NONE, true);

    if (parent != null)
    {
      return isContainerLocked(parent);
    }

    return false;
  }

  private boolean hasContainmentChanges(InternalCDORevisionDelta delta)
  {
    for (CDOFeatureDelta featureDelta : delta.getFeatureDeltas())
    {
      EStructuralFeature feature = featureDelta.getFeature();
      if (feature instanceof EReference)
      {
        if (((EReference)feature).isContainment())
        {
          return true;
        }
      }
    }

    return false;
  }

  private void lockTarget(Object value, Set<Long> newIDs, boolean supportingBranches)
  {
    if (value instanceof Long)
    {
      long id = (Long)value;
      if (id == 0)
      {
        return;
      }

      if (newIDs.contains(id))
      {
        // After merges newObjects may contain non-TEMP ids
        return;
      }

      if (detachedObjectTypes != null && detachedObjectTypes.containsKey(id))
      {
        throw new IllegalStateException("This commit deletes object " + id + " and adds a reference at the same time");
      }

      // Let this object be locked
      Object key = lockManager.getLockKey(id);
      lockedObjects.add(key);

      // Let this object be checked for existance after it has been locked
      if (lockedTargets == null)
      {
        lockedTargets = new ArrayList<Long>();
      }

      lockedTargets.add(id);
    }
  }

  protected void checkXRefs()
  {
    if (ensuringReferentialIntegrity && detachedObjectTypes != null)
    {
      XRefContext context = new XRefContext();
      xRefs = context.getXRefs(accessor);
      if (!xRefs.isEmpty())
      {
        rollbackMessage = "Referential integrity violated";
      }
    }
  }

  private synchronized void unlockObjects()
  {
    if (!lockedObjects.isEmpty())
    {
      lockManager.unlock(LockType.WRITE, transaction, lockedObjects);
      lockedObjects.clear();
    }
  }

  private void computeDirtyObjects(OMMonitor monitor)
  {
    try
    {
      monitor.begin(dirtyObjectDeltas.length);
      for (int i = 0; i < dirtyObjectDeltas.length; i++)
      {
        dirtyObjects[i] = computeDirtyObject(dirtyObjectDeltas[i]);
        if (dirtyObjects[i] == null)
        {
          throw new IllegalStateException("Can not retrieve origin revision for " + dirtyObjectDeltas[i]); //$NON-NLS-1$
        }

        monitor.worked();
      }
    }
    finally
    {
      monitor.done();
    }
  }

  private InternalCDORevision computeDirtyObject(InternalCDORevisionDelta delta)
  {
    long id = delta.getID();

    InternalCDORevision oldRevision = revisionManager.getRevision(id, CDORevision.UNCHUNKED, CDORevision.DEPTH_INFINITE, true);
    if (oldRevision == null)
    {
      throw new IllegalStateException("Origin revision not found for " + delta);
    }

    // Make sure all chunks are loaded
    for (EStructuralFeature feature : CDOModelUtil.getAllPersistentFeatures(oldRevision.getEClass()))
    {
      if (feature.isMany())
      {
        repository.ensureChunk(oldRevision, feature, 0, oldRevision.getList(feature).size());
      }
    }

    InternalCDORevision newRevision = oldRevision.copy();
    newRevision.adjustForCommit();

    delta.apply(newRevision);
    return newRevision;
  }


  public synchronized void rollback(String message)
  {
    // Check if we already rolled back
    if (rollbackMessage == null)
    {
      rollbackMessage = message;
      unlockObjects();
      if (accessor != null)
      {
        try
        {
          accessor.rollback();
        }
        catch (RuntimeException ex)
        {
          OM.LOG.warn("Problem while rolling back  the transaction", ex); //$NON-NLS-1$
        }
        finally
        {
          repository.failCommit();
        }
      }
    }
  }

  protected IStoreAccessor getAccessor()
  {
    return accessor;
  }

  private void updateInfraStructure(OMMonitor monitor)
  {
    try
    {
      monitor.begin(7);
      addNewPackageUnits(monitor.fork());
      addRevisions(newObjects, monitor.fork());
      addRevisions(dirtyObjects, monitor.fork());

      unlockObjects();
      monitor.worked();

      if (isAutoReleaseLocksEnabled())
      {
        repository.getLockManager().unlock(transaction);
      }

      monitor.worked();
      repository.notifyWriteAccessHandlers(transaction, this, false, monitor.fork());
    }
    finally
    {
      monitor.done();
    }
  }

  private void addNewPackageUnits(OMMonitor monitor)
  {
    InternalCDOPackageRegistry repositoryPackageRegistry = repository.getPackageRegistry(false);
    synchronized (repositoryPackageRegistry)
    {
      try
      {
        monitor.begin(newPackageUnits.length);
        for (int i = 0; i < newPackageUnits.length; i++)
        {
          newPackageUnits[i].setState(CDOPackageUnit.State.LOADED);
          repositoryPackageRegistry.putPackageUnit(newPackageUnits[i]);
          monitor.worked();
        }
      }
      finally
      {
        monitor.done();
      }
    }
  }

  private void addRevisions(CDORevision[] revisions, OMMonitor monitor)
  {
    try
    {
      monitor.begin(revisions.length);
      for (CDORevision revision : revisions)
      {
        if (revision != null)
        {
          revisionManager.addRevision(revision);
        }

        monitor.worked();
      }
    }
    finally
    {
      monitor.done();
    }
  }


  private void detachObjects(OMMonitor monitor)
  {
    int size = detachedObjects.length;
    cachedDetachedRevisions = new InternalCDORevision[size];

    Long[] detachedObjects = getDetachedObjects();

    try
    {
      monitor.begin(size);
      for (int i = 0; i < size; i++)
      {
        Long id = detachedObjects[i];

        // Remember the cached revision that must be revised after successful commit through updateInfraStructure
        cachedDetachedRevisions[i] = (InternalCDORevision)revisionManager.getCache().getRevision(id);
        monitor.worked();
      }
    }
    finally
    {
      monitor.done();
    }
  }


  /**
   * @author Eike Stepper
   */
  public static final class TransactionPackageRegistry extends CDOPackageRegistryImpl
  {
    private static final long serialVersionUID = 1L;

    public TransactionPackageRegistry(InternalCDOPackageRegistry repositoryPackageRegistry)
    {
      delegateRegistry = repositoryPackageRegistry;
      setPackageLoader(repositoryPackageRegistry.getPackageLoader());
    }

    @Override
    public synchronized void putPackageUnit(InternalCDOPackageUnit packageUnit)
    {
      packageUnit.setPackageRegistry(this);
      for (InternalCDOPackageInfo packageInfo : packageUnit.getPackageInfos())
      {
        EPackage ePackage = packageInfo.getEPackage();
        basicPut(ePackage.getNsURI(), ePackage);
      }

      resetInternalCaches();
    }

    @Override
    protected void disposePackageUnits()
    {
      // Do nothing
    }

    @Override
    public Collection<Object> values()
    {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * @author Martin Fluegge
   */
  private static final class DeltaLockWrapper implements CDOIDAndBranch
  {
    private Object key;

    private InternalCDORevisionDelta delta;

    public DeltaLockWrapper(Object key, InternalCDORevisionDelta delta)
    {
      this.key = key;
      this.delta = delta;
    }

    public Object getKey()
    {
      return key;
    }

    public InternalCDORevisionDelta getDelta()
    {
      return delta;
    }

    public long getID()
    {
      return key instanceof CDOIDAndBranch ? ((CDOIDAndBranch)key).getID() : (Long)key;
    }

    public CDOBranch getBranch()
    {
      return key instanceof CDOIDAndBranch ? ((CDOIDAndBranch)key).getBranch() : null;
    }

    @Override
    public boolean equals(Object obj)
    {
      if (obj instanceof DeltaLockWrapper)
      {
        DeltaLockWrapper wrapper = (DeltaLockWrapper)obj;
        return key.equals(wrapper.getKey());
      }

      return key.equals(obj);
    }

    @Override
    public int hashCode()
    {
      return key.hashCode();
    }

    @Override
    public String toString()
    {
      return key.toString();
    }
  }

  /**
   * @author Eike Stepper
   */
  private final class XRefContext implements QueryXRefsContext
  {
    private Map<EClass, List<EReference>> sourceCandidates = new HashMap<EClass, List<EReference>>();

    private Set<Long> detachedIDs = new HashSet<Long>();

    private Set<Long> dirtyIDs = new HashSet<Long>();

    private List<CDOIDReference> result = new ArrayList<CDOIDReference>();

    public XRefContext()
    {
      XRefsQueryHandler.collectSourceCandidates(transaction, detachedObjectTypes.values(), sourceCandidates);

      for (Long id : detachedObjects)
      {
        detachedIDs.add(id);
      }

      for (InternalCDORevision revision : dirtyObjects)
      {
        dirtyIDs.add(revision.getID());
      }
    }

    public List<CDOIDReference> getXRefs(IStoreAccessor accessor)
    {
      accessor.queryXRefs(this);
      checkDirtyObjects();
      return result;
    }

    private void checkDirtyObjects()
    {
    }

    public Map<Long, EClass> getTargetObjects()
    {
      return detachedObjectTypes;
    }

    public EReference[] getSourceReferences()
    {
      return new EReference[0];
    }

    public Map<EClass, List<EReference>> getSourceCandidates()
    {
      return sourceCandidates;
    }

    public int getMaxResults()
    {
      return CDOQueryInfo.UNLIMITED_RESULTS;
    }

    public boolean addXRef(long targetID, long sourceID, EReference sourceReference, int sourceIndex)
    {
      if (CDOIDUtil.isNull(targetID))
      {
        // Compensate potential issues with the XRef implementation in the store accessor.
        return true;
      }

      if (detachedIDs.contains(sourceID))
      {
        // Ignore XRefs from objects that are about to be detached themselves by this commit.
        return true;
      }

      if (dirtyIDs.contains(sourceID))
      {
        // Ignore XRefs from objects that are about to be modified by this commit. They're handled later in getXRefs().
        return true;
      }

      result.add(new CDOIDReference(targetID, sourceID, sourceReference, sourceIndex));
      return true;
    }
  }
}
