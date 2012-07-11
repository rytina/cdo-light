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
package org.eclipse.emf.cdo.internal.server;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.cdo.common.model.CDOPackageUnit;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.util.CDOQueryInfo;
import org.eclipse.emf.cdo.server.IQueryHandler;
import org.eclipse.emf.cdo.server.IQueryHandlerProvider;
import org.eclipse.emf.cdo.server.IStoreAccessor;
import org.eclipse.emf.cdo.server.IStoreAccessor.CommitContext;
import org.eclipse.emf.cdo.server.ITransaction;
import org.eclipse.emf.cdo.spi.common.branch.InternalCDOBranchManager;
import org.eclipse.emf.cdo.spi.common.model.InternalCDOPackageRegistry;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevision;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevisionManager;
import org.eclipse.emf.cdo.spi.common.revision.RevisionInfo;
import org.eclipse.emf.cdo.spi.server.InternalCommitManager;
import org.eclipse.emf.cdo.spi.server.InternalLockManager;
import org.eclipse.emf.cdo.spi.server.InternalQueryManager;
import org.eclipse.emf.cdo.spi.server.InternalRepository;
import org.eclipse.emf.cdo.spi.server.InternalSession;
import org.eclipse.emf.cdo.spi.server.InternalSessionManager;
import org.eclipse.emf.cdo.spi.server.InternalStore;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.net4j.util.collection.Pair;
import org.eclipse.net4j.util.event.IListener;
import org.eclipse.net4j.util.om.monitor.OMMonitor;

/**
 * @author Eike Stepper
 */
public abstract class DelegatingRepository implements InternalRepository
{
  public DelegatingRepository()
  {
  }

  protected abstract InternalRepository getDelegate();

  public void addHandler(Handler handler)
  {
    getDelegate().addHandler(handler);
  }

  public void addListener(IListener listener)
  {
    getDelegate().addListener(listener);
  }


  public IStoreAccessor ensureChunk(InternalCDORevision revision, EStructuralFeature feature, int chunkStart,
      int chunkEnd)
  {
    return getDelegate().ensureChunk(revision, feature, chunkStart, chunkEnd);
  }

  public InternalCommitManager getCommitManager()
  {
    return getDelegate().getCommitManager();
  }

  public long getCreationTime()
  {
    return getDelegate().getCreationTime();
  }

  public Object[] getElements()
  {
    return getDelegate().getElements();
  }

  public IListener[] getListeners()
  {
    return getDelegate().getListeners();
  }

  public InternalLockManager getLockManager()
  {
    return getDelegate().getLockManager();
  }

  public String getName()
  {
    return getDelegate().getName();
  }

  public InternalCDOPackageRegistry getPackageRegistry()
  {
    return getDelegate().getPackageRegistry();
  }

  public InternalCDOPackageRegistry getPackageRegistry(boolean considerCommitContext)
  {
    return getDelegate().getPackageRegistry(considerCommitContext);
  }

  public Map<String, String> getProperties()
  {
    return getDelegate().getProperties();
  }

  public IQueryHandler getQueryHandler(CDOQueryInfo info)
  {
    return getDelegate().getQueryHandler(info);
  }

  public IQueryHandlerProvider getQueryHandlerProvider()
  {
    return getDelegate().getQueryHandlerProvider();
  }

  public InternalQueryManager getQueryManager()
  {
    return getDelegate().getQueryManager();
  }

  public InternalCDORevisionManager getRevisionManager()
  {
    return getDelegate().getRevisionManager();
  }

  public InternalSessionManager getSessionManager()
  {
    return getDelegate().getSessionManager();
  }

  public InternalStore getStore()
  {
    return getDelegate().getStore();
  }

  public String getUUID()
  {
    return getDelegate().getUUID();
  }

  public boolean hasListeners()
  {
    return getDelegate().hasListeners();
  }

  public boolean isEmpty()
  {
    return getDelegate().isEmpty();
  }

  public EPackage[] loadPackages(CDOPackageUnit packageUnit)
  {
    return getDelegate().loadPackages(packageUnit);
  }

  public InternalCDOBranchManager getBranchManager()
  {
    return getDelegate().getBranchManager();
  }

  public void setBranchManager(InternalCDOBranchManager branchManager)
  {
    getDelegate().setBranchManager(branchManager);
  }

  public BranchInfo loadBranch(int branchID)
  {
    return getDelegate().loadBranch(branchID);
  }

  public SubBranchInfo[] loadSubBranches(int branchID)
  {
    return getDelegate().loadSubBranches(branchID);
  }

  public List<InternalCDORevision> loadRevisions(List<RevisionInfo> infos,
      int referenceChunk, int prefetchDepth)
  {
    return getDelegate().loadRevisions(infos, referenceChunk, prefetchDepth);
  }

  public InternalCDORevision loadRevision(long id, int referenceChunk)
  {
    return getDelegate().loadRevision(id, referenceChunk);
  }

  public void notifyReadAccessHandlers(InternalSession session, CDORevision[] revisions,
      List<CDORevision> additionalRevisions)
  {
    getDelegate().notifyReadAccessHandlers(session, revisions, additionalRevisions);
  }

  public void notifyWriteAccessHandlers(ITransaction transaction, CommitContext commitContext, boolean beforeCommit,
      OMMonitor monitor)
  {
    getDelegate().notifyWriteAccessHandlers(transaction, commitContext, beforeCommit, monitor);
  }

  public void removeHandler(Handler handler)
  {
    getDelegate().removeHandler(handler);
  }

  public void removeListener(IListener listener)
  {
    getDelegate().removeListener(listener);
  }

  public void setName(String name)
  {
    getDelegate().setName(name);
  }

  public void setProperties(Map<String, String> properties)
  {
    getDelegate().setProperties(properties);
  }

  public void setQueryHandlerProvider(IQueryHandlerProvider queryHandlerProvider)
  {
    getDelegate().setQueryHandlerProvider(queryHandlerProvider);
  }

  public void setRevisionManager(InternalCDORevisionManager revisionManager)
  {
    getDelegate().setRevisionManager(revisionManager);
  }

  public void setSessionManager(InternalSessionManager sessionManager)
  {
    getDelegate().setSessionManager(sessionManager);
  }

  public void setStore(InternalStore store)
  {
    getDelegate().setStore(store);
  }

  public void validateTimeStamp(long timeStamp) throws IllegalArgumentException
  {
    getDelegate().validateTimeStamp(timeStamp);
  }
}
