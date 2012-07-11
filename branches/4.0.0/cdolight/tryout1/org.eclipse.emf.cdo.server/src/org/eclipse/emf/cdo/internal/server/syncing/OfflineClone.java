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
package org.eclipse.emf.cdo.internal.server.syncing;

import java.util.List;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.commit.CDOChangeKind;
import org.eclipse.emf.cdo.common.commit.CDOChangeSetData;
import org.eclipse.emf.cdo.common.commit.CDOCommitData;
import org.eclipse.emf.cdo.common.model.CDOPackageUnit;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
import org.eclipse.emf.cdo.internal.server.TransactionCommitContext;
import org.eclipse.emf.cdo.server.StoreThreadLocal;
import org.eclipse.emf.cdo.spi.common.branch.InternalCDOBranch;
import org.eclipse.emf.cdo.spi.common.branch.InternalCDOBranchManager;
import org.eclipse.emf.cdo.spi.common.commit.CDOChangeKindCache;
import org.eclipse.emf.cdo.spi.common.model.InternalCDOPackageUnit;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevision;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevisionDelta;
import org.eclipse.emf.cdo.spi.server.InternalCommitContext;
import org.eclipse.emf.cdo.spi.server.InternalTransaction;
import org.eclipse.net4j.util.collection.IndexedList;
import org.eclipse.net4j.util.om.monitor.Monitor;
import org.eclipse.net4j.util.om.monitor.OMMonitor;

/**
 * @author Eike Stepper
 */
public class OfflineClone extends SynchronizableRepository
{
  public OfflineClone()
  {
    setState(OFFLINE);
  }

  @Override
  public final Type getType()
  {
    return CLONE;
  }

  @Override
  public final void setType(Type type)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public InternalCommitContext createCommitContext(InternalTransaction transaction)
  {
    return createWriteThroughCommitContext(transaction);
  }

  protected InternalCommitContext createBranchingCommitContext(InternalTransaction transaction, CDOBranch branch)
  {
    return new BranchingCommitContext(transaction);
  }

  /**
   * @author Eike Stepper
   */
  protected static final class CommitContextData implements CDOCommitData
  {
    private InternalCommitContext commitContext;

    private CDOChangeKindCache changeKindCache;

    public CommitContextData(InternalCommitContext commitContext)
    {
      this.commitContext = commitContext;
    }

    public boolean isEmpty()
    {
      return false;
    }

    public CDOChangeSetData copy()
    {
      throw new UnsupportedOperationException();
    }

    public void merge(CDOChangeSetData changeSetData)
    {
      throw new UnsupportedOperationException();
    }

    public List<CDOPackageUnit> getNewPackageUnits()
    {
      final InternalCDOPackageUnit[] newPackageUnits = commitContext.getNewPackageUnits();
      return new IndexedList<CDOPackageUnit>()
      {
        @Override
        public CDOPackageUnit get(int index)
        {
          return newPackageUnits[index];
        }

        @Override
        public int size()
        {
          return newPackageUnits.length;
        }
      };
    }

    public List<CDORevision> getNewObjects()
    {
      final InternalCDORevision[] newObjects = commitContext.getNewObjects();
      return new IndexedList<CDORevision>()
      {
        @Override
        public CDORevision get(int index)
        {
          return newObjects[index];
        }

        @Override
        public int size()
        {
          return newObjects.length;
        }
      };
    }

    public List<CDORevisionDelta> getChangedObjects()
    {
      final InternalCDORevisionDelta[] changedObjects = commitContext.getDirtyObjectDeltas();
      return new IndexedList<CDORevisionDelta>()
      {
        @Override
        public CDORevisionDelta get(int index)
        {
          return changedObjects[index];
        }

        @Override
        public int size()
        {
          return changedObjects.length;
        }
      };
    }

    public List<Long> getDetachedObjects()
    {
      final Long[] detachedObjects = commitContext.getDetachedObjects();
      return new IndexedList<Long>()
      {
        @Override
        public Long get(int index)
        {
          return detachedObjects[index];
        }

        @Override
        public int size()
        {
          return detachedObjects.length;
        }
      };
    }

    public synchronized CDOChangeKind getChangeKind(long id)
    {
      if (changeKindCache == null)
      {
        changeKindCache = new CDOChangeKindCache(this);
      }

      return changeKindCache.getChangeKind(id);
    }
  }

  /**
   * @author Eike Stepper
   */
  protected final class BranchingCommitContext extends TransactionCommitContext
  {

    public BranchingCommitContext(InternalTransaction transaction)
    {
      super(transaction);
    }

    @Override
    protected void lockObjects() throws InterruptedException
    {
      // Do nothing
    }

  }
}
