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
package org.eclipse.emf.cdo.spi.common.revision;

import java.util.List;

import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.CDORevisionCache;
import org.eclipse.emf.cdo.common.revision.CDORevisionFactory;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.net4j.util.lifecycle.Lifecycle;

/**
 * @author Eike Stepper
 * @since 3.0
 */
public abstract class DelegatingCDORevisionManager extends Lifecycle implements InternalCDORevisionManager
{
  public DelegatingCDORevisionManager()
  {
  }

  public InternalCDORevisionCache getCache()
  {
    return getDelegate().getCache();
  }

  /**
   * @since 4.0
   */
  public void setCache(CDORevisionCache cache)
  {
    getDelegate().setCache(cache);
  }

  public void setFactory(CDORevisionFactory factory)
  {
    getDelegate().setFactory(factory);
  }

  public CDORevisionFactory getFactory()
  {
    return getDelegate().getFactory();
  }

  public RevisionLoader getRevisionLoader()
  {
    return getDelegate().getRevisionLoader();
  }

  public void setRevisionLoader(RevisionLoader revisionLoader)
  {
    getDelegate().setRevisionLoader(revisionLoader);
  }

  public RevisionLocker getRevisionLocker()
  {
    return getDelegate().getRevisionLocker();
  }

  public void setRevisionLocker(RevisionLocker revisionLocker)
  {
    getDelegate().setRevisionLocker(revisionLocker);
  }

  /**
   * @since 4.0
   */
  public void addRevision(CDORevision revision)
  {
    getDelegate().addRevision(revision);
  }

  public boolean containsRevision(long id)
  {
    return getDelegate().containsRevision(id);
  }


  public EClass getObjectType(long id)
  {
    return getDelegate().getObjectType(id);
  }


  public InternalCDORevision getRevision(long id,  int referenceChunk, int prefetchDepth,
      boolean loadOnDemand)
  {
    return getDelegate().getRevision(id, referenceChunk, prefetchDepth, loadOnDemand);
  }

  public InternalCDORevision getRevision(long id, int referenceChunk, int prefetchDepth,
      boolean loadOnDemand, SyntheticCDORevision[] synthetics)
  {
    return getDelegate().getRevision(id, referenceChunk, prefetchDepth, loadOnDemand, synthetics);
  }

  public List<CDORevision> getRevisions(List<Long> ids, int referenceChunk,
      int prefetchDepth, boolean loadOnDemand)
  {
    return getDelegate().getRevisions(ids, referenceChunk, prefetchDepth, loadOnDemand);
  }

  public List<CDORevision> getRevisions(List<Long> ids, int referenceChunk,
      int prefetchDepth, boolean loadOnDemand, SyntheticCDORevision[] synthetics)
  {
    return getDelegate().getRevisions(ids, referenceChunk, prefetchDepth, loadOnDemand, synthetics);
  }



  @Override
  protected void doActivate() throws Exception
  {
    if (isDelegatingLifecycle())
    {
      getDelegate().activate();
    }
  }

  @Override
  protected void doDeactivate() throws Exception
  {
    if (isDelegatingLifecycle())
    {
      getDelegate().deactivate();
    }
  }

  protected boolean isDelegatingLifecycle()
  {
    return true;
  }

  protected abstract InternalCDORevisionManager getDelegate();
}
