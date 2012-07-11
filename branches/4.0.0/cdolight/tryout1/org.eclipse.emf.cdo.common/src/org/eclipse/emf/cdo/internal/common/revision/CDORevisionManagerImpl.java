/**
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 *    Simon McDuff - bug 201266
 *    Simon McDuff - bug 230832
 */
package org.eclipse.emf.cdo.internal.common.revision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.CDORevisionCache;
import org.eclipse.emf.cdo.common.revision.CDORevisionFactory;
import org.eclipse.emf.cdo.internal.common.bundle.OM;
import org.eclipse.emf.cdo.spi.common.revision.DetachedCDORevision;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevision;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevisionCache;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevisionManager;
import org.eclipse.emf.cdo.spi.common.revision.PointerCDORevision;
import org.eclipse.emf.cdo.spi.common.revision.RevisionInfo;
import org.eclipse.emf.cdo.spi.common.revision.SyntheticCDORevision;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.net4j.util.ReflectUtil.ExcludeFromDump;
import org.eclipse.net4j.util.lifecycle.Lifecycle;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;
import org.eclipse.net4j.util.om.trace.ContextTracer;

/**
 * @author Eike Stepper
 */
public class CDORevisionManagerImpl extends Lifecycle implements InternalCDORevisionManager
{
  private static final ContextTracer TRACER = new ContextTracer(OM.DEBUG_REVISION, CDORevisionManagerImpl.class);

  private boolean supportingAudits;

  private boolean supportingBranches;

  private RevisionLoader revisionLoader;

  private RevisionLocker revisionLocker;

  private CDORevisionFactory factory;

  private InternalCDORevisionCache cache;

  @ExcludeFromDump
  private transient Object loadAndAddLock = new Object()
  {
    @Override
    public String toString()
    {
      return "LoadAndAddLock"; //$NON-NLS-1$
    }
  };

  @ExcludeFromDump
  private transient Object reviseLock = new Object()
  {
    @Override
    public String toString()
    {
      return "ReviseLock"; //$NON-NLS-1$
    }
  };

  public CDORevisionManagerImpl()
  {
  }

  public boolean isSupportingAudits()
  {
    return supportingAudits;
  }

  public void setSupportingAudits(boolean on)
  {
    checkInactive();
    supportingAudits = on;
  }

  public boolean isSupportingBranches()
  {
    return supportingBranches;
  }

  public void setSupportingBranches(boolean on)
  {
    checkInactive();
    supportingBranches = on;
  }

  public RevisionLoader getRevisionLoader()
  {
    return revisionLoader;
  }

  public void setRevisionLoader(RevisionLoader revisionLoader)
  {
    checkInactive();
    this.revisionLoader = revisionLoader;
  }

  public RevisionLocker getRevisionLocker()
  {
    return revisionLocker;
  }

  public void setRevisionLocker(RevisionLocker revisionLocker)
  {
    checkInactive();
    this.revisionLocker = revisionLocker;
  }

  public CDORevisionFactory getFactory()
  {
    return factory;
  }

  public void setFactory(CDORevisionFactory factory)
  {
    checkInactive();
    this.factory = factory;
  }

  public InternalCDORevisionCache getCache()
  {
    return cache;
  }

  public void setCache(CDORevisionCache cache)
  {
    checkInactive();
    this.cache = (InternalCDORevisionCache)cache;
  }

  public EClass getObjectType(long id)
  {
    return cache.getObjectType(id);
  }

  public boolean containsRevision(long id)
  {
    if (supportingBranches)
    {
      return getRevision(id,  CDORevision.UNCHUNKED, CDORevision.DEPTH_NONE, false, null) != null;
    }

    return getCachedRevision(id) != null;
  }


  public void reviseLatest(long id)
  {
    acquireAtomicRequestLock(reviseLock);

    try
    {
      InternalCDORevision revision = (InternalCDORevision)cache.getRevision(id);
      if (revision != null)
      {
        cache.removeRevision(id);
      }
    }
    finally
    {
      releaseAtomicRequestLock(reviseLock);
    }
  }

  public void reviseVersion(long id, long timeStamp)
  {
    acquireAtomicRequestLock(reviseLock);

    try
    {
      InternalCDORevision revision = getCachedRevision(id);
      if (revision != null)
      {
          cache.removeRevision(id);
      }
    }
    finally
    {
      releaseAtomicRequestLock(reviseLock);
    }
  }


  public InternalCDORevision getRevision(long id, int referenceChunk, int prefetchDepth,
      boolean loadOnDemand)
  {
    return getRevision(id, referenceChunk, prefetchDepth, loadOnDemand, null);
  }

  public InternalCDORevision getRevision(long id, int referenceChunk, int prefetchDepth,
      boolean loadOnDemand, SyntheticCDORevision[] synthetics)
  {
    List<Long> ids = Collections.singletonList(id);
    CDORevision result = getRevisions(ids, referenceChunk, prefetchDepth, loadOnDemand, synthetics).get(0);
    return (InternalCDORevision)result;
  }

  public List<CDORevision> getRevisions(List<Long> ids, int referenceChunk,
      int prefetchDepth, boolean loadOnDemand)
  {
    return getRevisions(ids, referenceChunk, prefetchDepth, loadOnDemand, null);
  }

  public List<CDORevision> getRevisions(List<Long> ids, int referenceChunk,
      int prefetchDepth, boolean loadOnDemand, SyntheticCDORevision[] synthetics)
  {
    RevisionInfo[] infos = new RevisionInfo[ids.size()];
    List<RevisionInfo> infosToLoad = createRevisionInfos(ids, prefetchDepth, loadOnDemand, infos);
    if (infosToLoad != null)
    {
      loadRevisions(infosToLoad,  referenceChunk, prefetchDepth);
    }

    return getResultsAndSynthetics(infos, synthetics);
  }

  private List<RevisionInfo> createRevisionInfos(List<Long> ids, int prefetchDepth,
      boolean loadOnDemand, RevisionInfo[] infos)
  {
    List<RevisionInfo> infosToLoad = null;
    Iterator<Long> idIterator = ids.iterator();
    for (int i = 0; i < infos.length; i++)
    {
      long id = idIterator.next();
      RevisionInfo info = createRevisionInfo(id);
      infos[i] = info;

      if (loadOnDemand && (prefetchDepth != CDORevision.DEPTH_NONE || info.isLoadNeeded()))
      {
        if (infosToLoad == null)
        {
          infosToLoad = new ArrayList<RevisionInfo>(1);
        }

        infosToLoad.add(info);
      }
    }

    return infosToLoad;
  }

  private RevisionInfo createRevisionInfo(long id)
  {
    InternalCDORevision revision = getCachedRevision(id);
    if (revision != null)
    {
      return createRevisionInfoAvailable(revision);
    }

    if (supportingBranches)
    {
      revision = getCachedRevisionRecursively(id);
      if (revision != null)
      {
        return createRevisionInfoAvailable(revision);
      }
    }

    return createRevisionInfoMissing(id);
  }

  private RevisionInfo.Available createRevisionInfoAvailable(InternalCDORevision revision)
  {
    if (revision instanceof PointerCDORevision)
    {
      PointerCDORevision pointer = (PointerCDORevision)revision;
      InternalCDORevision targetRevision = getCachedRevision(pointer.getID());
      InternalCDORevision target = null;
	if (targetRevision != null)
      {
        target = targetRevision;
      }

      return new RevisionInfo.Available.Pointer(pointer.getID(), target);
    }

    if (revision instanceof DetachedCDORevision)
    {
      DetachedCDORevision detached = (DetachedCDORevision)revision;
      return new RevisionInfo.Available.Detached(detached.getID());
    }

    return new RevisionInfo.Available.Normal(revision.getID());
  }

  private RevisionInfo.Missing createRevisionInfoMissing(long id)
  {
    return new RevisionInfo.Missing(id);
  }

  protected List<InternalCDORevision> loadRevisions(List<RevisionInfo> infosToLoad, int referenceChunk, int prefetchDepth)
  {
    acquireAtomicRequestLock(loadAndAddLock);

    try
    {
      List<InternalCDORevision> additionalRevisions = //
      revisionLoader.loadRevisions(infosToLoad, referenceChunk, prefetchDepth);

      if (additionalRevisions != null)
      {
        for (InternalCDORevision revision : additionalRevisions)
        {
          addRevision(revision);
        }
      }

      return additionalRevisions;
    }
    finally
    {
      releaseAtomicRequestLock(loadAndAddLock);
    }
  }

  private List<CDORevision> getResultsAndSynthetics(RevisionInfo[] infos, SyntheticCDORevision[] synthetics)
  {
    List<CDORevision> results = new ArrayList<CDORevision>(infos.length);
    for (int i = 0; i < infos.length; i++)
    {
      RevisionInfo info = infos[i];
      info.processResult(this, results, synthetics, i);
    }

    return results;
  }

  public void addRevision(CDORevision revision)
  {
    if (revision != null)
    {
      cache.addRevision(revision);
    }
  }

  @Override
  protected void doBeforeActivate() throws Exception
  {
    super.doBeforeActivate();
    if (factory == null)
    {
      factory = CDORevisionFactory.DEFAULT;
    }

    if (cache == null)
    {
      cache = CDORevisionCacheNonAuditing.INSTANCE;
    }
  }

  @Override
  protected void doActivate() throws Exception
  {
    super.doActivate();
    LifecycleUtil.activate(cache);
  }

  @Override
  protected void doDeactivate() throws Exception
  {
    LifecycleUtil.deactivate(cache);
    super.doDeactivate();
  }

  private void acquireAtomicRequestLock(Object key)
  {
    if (revisionLocker != null)
    {
      revisionLocker.acquireAtomicRequestLock(key);
    }
  }

  private void releaseAtomicRequestLock(Object key)
  {
    if (revisionLocker != null)
    {
      revisionLocker.releaseAtomicRequestLock(key);
    }
  }


  private InternalCDORevision getCachedRevision(long id)
  {
    return (InternalCDORevision)cache.getRevision(id);
  }

  private InternalCDORevision getCachedRevisionRecursively(long id)
  {

    // Reached main branch
    return null;
  }

}
