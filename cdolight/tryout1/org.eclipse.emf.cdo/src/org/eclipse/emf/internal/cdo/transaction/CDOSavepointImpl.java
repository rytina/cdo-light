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
 *    Simon McDuff - bug 204890
 */
package org.eclipse.emf.internal.cdo.transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.emf.cdo.CDOObject;
import org.eclipse.emf.cdo.common.commit.CDOChangeSetData;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.delta.CDOFeatureDelta;
import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
import org.eclipse.emf.cdo.internal.common.commit.CDOChangeSetDataImpl;
import org.eclipse.emf.cdo.internal.common.revision.delta.CDORevisionDeltaImpl;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDOFeatureDelta;
import org.eclipse.emf.spi.cdo.InternalCDOSavepoint;
import org.eclipse.emf.spi.cdo.InternalCDOTransaction;
import org.eclipse.net4j.util.collection.MultiMap;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;

/**
 * @author Simon McDuff
 * @since 2.0
 */
public class CDOSavepointImpl extends CDOUserSavepointImpl implements InternalCDOSavepoint
{
  private final InternalCDOTransaction transaction;

  private Map<Long, CDORevision> baseNewObjects = new HashMap<Long, CDORevision>();

  private Map<Long, CDOObject> newObjects = new HashMap<Long, CDOObject>();

  private Map<Long, CDOObject> dirtyObjects = new HashMap<Long, CDOObject>();

  private ConcurrentMap<Long, CDORevisionDelta> revisionDeltas = new ConcurrentHashMap<Long, CDORevisionDelta>();

  private Map<Long, CDOObject> detachedObjects = new HashMap<Long, CDOObject>()
  {
    private static final long serialVersionUID = 1L;

    @Override
    public CDOObject put(Long key, CDOObject object)
    {
      synchronized (transaction)
      {
        sharedDetachedObjects.add(key);
        dirtyObjects.remove(key);
        baseNewObjects.remove(key);
        newObjects.remove(key);
        revisionDeltas.remove(key);
        return super.put(key, object);
      }
    }
  };

  // Bug 283985 (Re-attachment)
  private Map<Long, CDOObject> reattachedObjects = new HashMap<Long, CDOObject>();

  /**
   * Contains all persistent CDOIDs that were removed. The same instance is shared between all save points that belong
   * to the same transaction.
   */
  private Set<Long> sharedDetachedObjects;

  private boolean wasDirty;

  public CDOSavepointImpl(InternalCDOTransaction transaction, InternalCDOSavepoint lastSavepoint)
  {
    super(transaction, lastSavepoint);
    this.transaction = transaction;

    wasDirty = transaction.isDirty();
    if (lastSavepoint == null)
    {
      sharedDetachedObjects = new HashSet<Long>();
    }
    else
    {
      sharedDetachedObjects = lastSavepoint.getSharedDetachedObjects();
    }
  }

  @Override
  public InternalCDOTransaction getTransaction()
  {
    return (InternalCDOTransaction)super.getTransaction();
  }

  @Override
  public InternalCDOSavepoint getFirstSavePoint()
  {
    synchronized (transaction)
    {
      return (InternalCDOSavepoint)super.getFirstSavePoint();
    }
  }

  @Override
  public InternalCDOSavepoint getPreviousSavepoint()
  {
    synchronized (transaction)
    {
      return (InternalCDOSavepoint)super.getPreviousSavepoint();
    }
  }

  @Override
  public InternalCDOSavepoint getNextSavepoint()
  {
    synchronized (transaction)
    {
      return (InternalCDOSavepoint)super.getNextSavepoint();
    }
  }

  public void clear()
  {
    synchronized (transaction)
    {
      newObjects.clear();
      dirtyObjects.clear();
      revisionDeltas.clear();
      baseNewObjects.clear();
      detachedObjects.clear();
      reattachedObjects.clear();
    }
  }

  public boolean wasDirty()
  {
    return wasDirty;
  }

  public Map<Long, CDOObject> getNewObjects()
  {
    return newObjects;
  }

  public Map<Long, CDOObject> getDetachedObjects()
  {
    return detachedObjects;
  }

  // Bug 283985 (Re-attachment)
  public Map<Long, CDOObject> getReattachedObjects()
  {
    return reattachedObjects;
  }

  public Map<Long, CDOObject> getDirtyObjects()
  {
    return dirtyObjects;
  }

  public Set<Long> getSharedDetachedObjects()
  {
    return sharedDetachedObjects;
  }

  public ConcurrentMap<Long, CDORevisionDelta> getRevisionDeltas()
  {
    return revisionDeltas;
  }

  public CDOChangeSetData getChangeSetData()
  {
    synchronized (transaction)
    {
      return createChangeSetData(newObjects, revisionDeltas, detachedObjects.keySet());
    }
  }

  public CDOChangeSetData getAllChangeSetData()
  {
    synchronized (transaction)
    {
      return createChangeSetData(getAllNewObjects(), getAllRevisionDeltas(), getAllDetachedObjects().keySet());
    }
  }

  private CDOChangeSetData createChangeSetData(Map<Long, CDOObject> newObjects,
      Map<Long, CDORevisionDelta> revisionDeltas, Collection<Long> detachedObjects)
  {
	    List<CDORevision> newList = new ArrayList<CDORevision>(newObjects.size());
	    for (CDOObject object : newObjects.values())
	    {
	      newList.add(object.cdoRevision());
	    }

	    List<CDORevisionDelta> changedList = new ArrayList<CDORevisionDelta>(revisionDeltas.size());
	    for (CDORevisionDelta delta : revisionDeltas.values())
	    {
	      changedList.add(delta);
	    }

	  
	  
    return new CDOChangeSetDataImpl(newList, changedList, detachedObjects);
  }

  public Map<Long, CDORevision> getBaseNewObjects()
  {
    return baseNewObjects;
  }

  /**
   * Return the list of new objects from this point.
   */
  public Map<Long, CDOObject> getAllDirtyObjects()
  {
    synchronized (transaction)
    {
      if (getPreviousSavepoint() == null)
      {
        return Collections.unmodifiableMap(getDirtyObjects());
      }

      MultiMap.ListBased<Long, CDOObject> dirtyObjects = new MultiMap.ListBased<Long, CDOObject>();
      for (InternalCDOSavepoint savepoint = this; savepoint != null; savepoint = savepoint.getPreviousSavepoint())
      {
        dirtyObjects.getDelegates().add(savepoint.getDirtyObjects());
      }

      return dirtyObjects;
    }
  }

  /**
   * Return the list of new objects from this point without objects that are removed.
   */
  public Map<Long, CDOObject> getAllNewObjects()
  {
    synchronized (transaction)
    {
      if (getPreviousSavepoint() == null)
      {
        return Collections.unmodifiableMap(getNewObjects());
      }

      if (getSharedDetachedObjects().size() == 0)
      {
        MultiMap.ListBased<Long, CDOObject> newObjects = new MultiMap.ListBased<Long, CDOObject>();
        for (InternalCDOSavepoint savepoint = this; savepoint != null; savepoint = savepoint.getPreviousSavepoint())
        {
          newObjects.getDelegates().add(savepoint.getNewObjects());
        }

        return newObjects;
      }

      Map<Long, CDOObject> newObjects = new HashMap<Long, CDOObject>();
      for (InternalCDOSavepoint savepoint = this; savepoint != null; savepoint = savepoint.getPreviousSavepoint())
      {
        for (Entry<Long, CDOObject> entry : savepoint.getNewObjects().entrySet())
        {
          if (!getSharedDetachedObjects().contains(entry.getKey()))
          {
            newObjects.put(entry.getKey(), entry.getValue());
          }
        }
      }

      return newObjects;
    }
  }

  /**
   * @since 2.0
   */
  public Map<Long, CDORevision> getAllBaseNewObjects()
  {
    synchronized (transaction)
    {
      if (getPreviousSavepoint() == null)
      {
        return Collections.unmodifiableMap(getBaseNewObjects());
      }

      MultiMap.ListBased<Long, CDORevision> newObjects = new MultiMap.ListBased<Long, CDORevision>();
      for (InternalCDOSavepoint savepoint = this; savepoint != null; savepoint = savepoint.getPreviousSavepoint())
      {
        newObjects.getDelegates().add(savepoint.getBaseNewObjects());
      }

      return newObjects;
    }
  }

  /**
   * Return the list of all deltas without objects that are removed.
   */
  public Map<Long, CDORevisionDelta> getAllRevisionDeltas()
  {
    synchronized (transaction)
    {
      if (getPreviousSavepoint() == null)
      {
        return Collections.unmodifiableMap(getRevisionDeltas());
      }

      // We need to combined the result for all delta in different Savepoint
      Map<Long, CDORevisionDelta> revisionDeltas = new HashMap<Long, CDORevisionDelta>();
      for (InternalCDOSavepoint savepoint = getFirstSavePoint(); savepoint != null; savepoint = savepoint
          .getNextSavepoint())
      {
        for (Entry<Long, CDORevisionDelta> entry : savepoint.getRevisionDeltas().entrySet())
        {
          // Skipping temporary
          if (getSharedDetachedObjects().contains(entry.getKey()))
          {
            continue;
          }

          CDORevisionDeltaImpl revisionDelta = (CDORevisionDeltaImpl)revisionDeltas.get(entry.getKey());
          if (revisionDelta == null)
          {
            revisionDeltas.put(entry.getKey(), entry.getValue().copy());
          }
          else
          {
            for (CDOFeatureDelta delta : entry.getValue().getFeatureDeltas())
            {
              revisionDelta.addFeatureDelta(((InternalCDOFeatureDelta)delta).copy());
            }
          }
        }
      }

      return Collections.unmodifiableMap(revisionDeltas);
    }
  }

  public Map<Long, CDOObject> getAllDetachedObjects()
  {
    synchronized (transaction)
    {
      if (getPreviousSavepoint() == null && reattachedObjects.isEmpty())
      {
        return Collections.unmodifiableMap(getDetachedObjects());
      }


      return detachedObjects;
    }
  }

  public void recalculateSharedDetachedObjects()
  {
    synchronized (transaction)
    {
      sharedDetachedObjects.clear();
      for (InternalCDOSavepoint savepoint = this; savepoint != null; savepoint = savepoint.getPreviousSavepoint())
      {
        for (long id : savepoint.getDetachedObjects().keySet())
        {
          sharedDetachedObjects.add(id);
        }
      }
    }
  }

  public void rollback()
  {
    synchronized (transaction)
    {
      InternalCDOTransaction transaction = getTransaction();
      LifecycleUtil.checkActive(transaction);
      transaction.getTransactionStrategy().rollback(transaction, this);
    }
  }
}
