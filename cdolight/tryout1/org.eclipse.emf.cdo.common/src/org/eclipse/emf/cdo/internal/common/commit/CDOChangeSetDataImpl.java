/**
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.emf.cdo.internal.common.commit;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.cdo.common.commit.CDOChangeKind;
import org.eclipse.emf.cdo.common.commit.CDOChangeSetData;
import org.eclipse.emf.cdo.common.revision.CDOIDAndVersion;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.CDORevisionKey;
import org.eclipse.emf.cdo.common.revision.delta.CDOFeatureDelta;
import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
import org.eclipse.emf.cdo.spi.common.commit.CDOChangeKindCache;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevisionDelta;

/**
 * @author Eike Stepper
 */
public class CDOChangeSetDataImpl implements CDOChangeSetData
{
  private Collection<CDORevision> newObjects;

  private Collection<CDORevisionDelta> changedObjects;

  private Collection<Long> detachedObjects;

  private CDOChangeKindCache changeKindCache;

  public CDOChangeSetDataImpl(Collection<CDORevision> newObjects, Collection<CDORevisionDelta> changedObjects,
      Collection<Long> detachedObjects)
  {
    this.newObjects = newObjects;
    this.changedObjects = changedObjects;
    this.detachedObjects = detachedObjects;
  }

  public CDOChangeSetDataImpl()
  {
    this(new ArrayList<CDORevision>(), new ArrayList<CDORevisionDelta>(), new ArrayList<Long>());
  }

  public boolean isEmpty()
  {
    if (newObjects != null && !newObjects.isEmpty())
    {
      return false;
    }

    if (changedObjects != null && !changedObjects.isEmpty())
    {
      return false;
    }

    if (detachedObjects != null && !detachedObjects.isEmpty())
    {
      return false;
    }

    return true;
  }

  public CDOChangeSetData copy()
  {
    List<CDORevision> newObjectsCopy = new ArrayList<CDORevision>(newObjects);
    List<CDORevisionDelta> changedObjectsCopy = new ArrayList<CDORevisionDelta>(changedObjects);
    List<Long> detachedObjectsCopy = new ArrayList<Long>(detachedObjects);
    return new CDOChangeSetDataImpl(newObjectsCopy, changedObjectsCopy, detachedObjectsCopy);
  }

  public void merge(CDOChangeSetData changeSetData)
  {
    Map<Long, CDORevision> newMap = new HashMap<Long, CDORevision>();
    fillMap(newMap, newObjects);
    fillMap(newMap, changeSetData.getNewObjects());

    Map<Long, CDORevisionDelta> changedMap = new HashMap<Long, CDORevisionDelta>();
    fillMap(changedMap, changedObjects);
    for (CDORevisionDelta key : changeSetData.getChangedObjects())
    {
      mergeChangedObject(key, newMap, changedMap);
    }

    List<Long> detached = new ArrayList<Long>(detachedObjects);
    for (Long id : changeSetData.getDetachedObjects())
    {
      if (newMap.remove(id) == null)
      {
        detached.add(id);
      }
    }

    newObjects = new ArrayList<CDORevision>(newMap.values());
    changedObjects = new ArrayList<CDORevisionDelta>(changedMap.values());
    detachedObjects = new ArrayList<Long>(detached);
  }

  private void mergeChangedObject(CDORevisionDelta key, Map<Long, CDORevision> newMap,
      Map<Long, CDORevisionDelta> changedMap)
  {
    long id = key.getID();
    if (key instanceof CDORevisionDelta)
    {
      CDORevisionDelta delta = (CDORevisionDelta)key;

      // Try to add the delta to existing new revision
      CDOIDAndVersion oldRevision = newMap.get(id);
      if (oldRevision instanceof CDORevision)
      {
        CDORevision newRevision = (CDORevision)oldRevision;
        delta.apply(newRevision);
        return;
      }

      // Try to add the delta to existing delta
      CDORevisionKey oldDelta = changedMap.get(id);
      if (oldDelta instanceof CDORevisionDelta)
      {
        InternalCDORevisionDelta newDelta = (InternalCDORevisionDelta)oldDelta;
        for (CDOFeatureDelta featureDelta : delta.getFeatureDeltas())
        {
          newDelta.addFeatureDelta(featureDelta);
        }

        return;
      }
    }

    // Fall back
    changedMap.put(id, key);
  }

  public List<CDORevision> getNewObjects()
  {
    return (List<CDORevision>) newObjects;
  }

  public List<CDORevisionDelta> getChangedObjects()
  {
    return (List<CDORevisionDelta>) changedObjects;
  }

  public List<Long> getDetachedObjects()
  {
    return (List<Long>) detachedObjects;
  }

  public synchronized CDOChangeKind getChangeKind(long id)
  {
    if (changeKindCache == null)
    {
      changeKindCache = new CDOChangeKindCache(this);
    }

    return changeKindCache.getChangeKind(id);
  }

  @Override
  public String toString()
  {
    return MessageFormat
        .format(
            "ChangeSetData[newObjects={0}, changedObjects={1}, detachedObjects={2}]", newObjects.size(), changedObjects.size(), detachedObjects.size()); //$NON-NLS-1$
  }

  private static <T extends CDOIDAndVersion> void fillMap(Map<Long, T> map, Collection<T> c)
  {
    for (T key : c)
    {
      map.put(key.getID(), key);
    }
  }
}
