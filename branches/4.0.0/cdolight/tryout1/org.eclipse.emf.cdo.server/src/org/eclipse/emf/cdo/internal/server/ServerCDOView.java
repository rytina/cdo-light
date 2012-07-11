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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.cdo.CDOObject;
import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.common.commit.CDOChangeSetData;
import org.eclipse.emf.cdo.common.commit.CDOCommitInfo;
import org.eclipse.emf.cdo.common.lob.CDOLobStore;
import org.eclipse.emf.cdo.common.model.CDOPackageUnit;
import org.eclipse.emf.cdo.common.protocol.CDOAuthenticator;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.CDORevisionProvider;
import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
import org.eclipse.emf.cdo.server.IStoreAccessor;
import org.eclipse.emf.cdo.session.CDORepositoryInfo;
import org.eclipse.emf.cdo.spi.common.branch.InternalCDOBranch;
import org.eclipse.emf.cdo.spi.common.branch.InternalCDOBranchManager;
import org.eclipse.emf.cdo.spi.common.commit.CDORevisionAvailabilityInfo;
import org.eclipse.emf.cdo.spi.common.commit.InternalCDOCommitInfoManager;
import org.eclipse.emf.cdo.spi.common.model.InternalCDOPackageRegistry;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevision;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevisionManager;
import org.eclipse.emf.cdo.spi.server.InternalRepository;
import org.eclipse.emf.cdo.spi.server.InternalSession;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CDOUtil;
import org.eclipse.emf.cdo.view.CDOAdapterPolicy;
import org.eclipse.emf.cdo.view.CDOFeatureAnalyzer;
import org.eclipse.emf.cdo.view.CDOFetchRuleManager;
import org.eclipse.emf.cdo.view.CDOInvalidationPolicy;
import org.eclipse.emf.cdo.view.CDORevisionPrefetchingPolicy;
import org.eclipse.emf.cdo.view.CDOStaleReferencePolicy;
import org.eclipse.emf.cdo.view.CDOView;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.internal.cdo.session.SessionUtil;
import org.eclipse.emf.internal.cdo.view.AbstractCDOView;
import org.eclipse.emf.spi.cdo.CDOSessionProtocol;
import org.eclipse.emf.spi.cdo.CDOSessionProtocol.RefreshSessionResult;
import org.eclipse.emf.spi.cdo.InternalCDOObject;
import org.eclipse.emf.spi.cdo.InternalCDORemoteSessionManager;
import org.eclipse.emf.spi.cdo.InternalCDOSession;
import org.eclipse.emf.spi.cdo.InternalCDOTransaction;
import org.eclipse.emf.spi.cdo.InternalCDOView;
import org.eclipse.net4j.util.concurrent.IRWLockManager.LockType;
import org.eclipse.net4j.util.event.IListener;
import org.eclipse.net4j.util.lifecycle.LifecycleException;
import org.eclipse.net4j.util.lifecycle.LifecycleState;
import org.eclipse.net4j.util.ref.ReferenceType;
import org.eclipse.net4j.util.ref.ReferenceValueMap;

/**
 * @author Eike Stepper
 */
public class ServerCDOView extends AbstractCDOView implements org.eclipse.emf.cdo.view.CDOView.Options
{
  private static final CDOAdapterPolicy[] ADAPTER_POLICIES = new CDOAdapterPolicy[0];

  private static final CDORevisionPrefetchingPolicy REVISION_PREFETCHING = CDOUtil
      .createRevisionPrefetchingPolicy(NO_REVISION_PREFETCHING);

  private InternalCDOSession session;

  private CDORevisionProvider revisionProvider;

  public ServerCDOView(InternalSession session, boolean legacyModeEnabled,
      CDORevisionProvider revisionProvider)
  {
    super(legacyModeEnabled);
    this.session = new ServerCDOSession(session);
    this.revisionProvider = revisionProvider;

    setViewSet(SessionUtil.prepareResourceSet(new ResourceSetImpl()));
    setObjects(new ReferenceValueMap.Weak<Long, InternalCDOObject>());
    activate();
  }

  public int getViewID()
  {
    return 1;
  }

  public InternalCDOSession getSession()
  {
    return session;
  }

  public void setLastUpdateTime(long lastUpdateTime)
  {
    throw new UnsupportedOperationException();
  }

  public Options options()
  {
    return this;
  }

  public synchronized InternalCDORevision getRevision(long id, boolean loadOnDemand)
  {
    return (InternalCDORevision)revisionProvider.getRevision(id);
  }


  public boolean isInvalidationRunnerActive()
  {
    return false;
  }

  public boolean setBranchPoint(CDOBranchPoint branchPoint)
  {
    throw new UnsupportedOperationException();
  }

  public void lockObjects(Collection<? extends CDOObject> objects, LockType lockType, long timeout)
      throws InterruptedException
  {
    throw new UnsupportedOperationException();
  }

  public void unlockObjects(Collection<? extends CDOObject> objects, LockType lockType)
  {
    throw new UnsupportedOperationException();
  }

  public void unlockObjects()
  {
    throw new UnsupportedOperationException();
  }

  public boolean waitForUpdate(long updateTime, long timeoutMillis)
  {
    throw new UnsupportedOperationException();
  }

  public void setViewID(int viewId)
  {
    throw new UnsupportedOperationException();
  }

  public void setSession(InternalCDOSession session)
  {
    throw new UnsupportedOperationException();
  }

  public String getDurableLockingID()
  {
    return null;
  }

  public String enableDurableLocking(boolean enable)
  {
    throw new UnsupportedOperationException();
  }

  public CDOFeatureAnalyzer getFeatureAnalyzer()
  {
    return CDOFeatureAnalyzer.NOOP;
  }

  public void setFeatureAnalyzer(CDOFeatureAnalyzer featureAnalyzer)
  {
    throw new UnsupportedOperationException();
  }

  public InternalCDOTransaction toTransaction()
  {
    throw new UnsupportedOperationException();
  }

  public void invalidate(List<CDORevisionDelta> allChangedObjects,
      List<Long> allDetachedObjects, Map<Long, InternalCDORevision> oldRevisions, boolean async)
  {
    throw new UnsupportedOperationException();
  }

  public void prefetchRevisions(long id, int depth)
  {
    throw new UnsupportedOperationException();
  }

  public boolean isObjectLocked(CDOObject object, LockType lockType, boolean byOthers)
  {
    return false;
  }

  public void handleAddAdapter(InternalCDOObject eObject, Adapter adapter)
  {
    // Do nothing
  }

  public void handleRemoveAdapter(InternalCDOObject eObject, Adapter adapter)
  {
    // Do nothing
  }

  public void subscribe(EObject eObject, Adapter adapter)
  {
    throw new UnsupportedOperationException();
  }

  public void unsubscribe(EObject eObject, Adapter adapter)
  {
    throw new UnsupportedOperationException();
  }

  public boolean hasSubscription(long id)
  {
    return false;
  }

  public CDOView getContainer()
  {
    return this;
  }

  public ReferenceType getCacheReferenceType()
  {
    return ReferenceType.WEAK;
  }

  public boolean setCacheReferenceType(ReferenceType referenceType)
  {
    throw new UnsupportedOperationException();
  }

  public CDOInvalidationPolicy getInvalidationPolicy()
  {
    return CDOInvalidationPolicy.DEFAULT;
  }

  public void setInvalidationPolicy(CDOInvalidationPolicy policy)
  {
    throw new UnsupportedOperationException();
  }

  public boolean isInvalidationNotificationEnabled()
  {
    return false;
  }

  public void setInvalidationNotificationEnabled(boolean enabled)
  {
    throw new UnsupportedOperationException();
  }

  public CDOAdapterPolicy[] getChangeSubscriptionPolicies()
  {
    return ADAPTER_POLICIES;
  }

  public void addChangeSubscriptionPolicy(CDOAdapterPolicy policy)
  {
    throw new UnsupportedOperationException();
  }

  public void removeChangeSubscriptionPolicy(CDOAdapterPolicy policy)
  {
    throw new UnsupportedOperationException();
  }

  public CDOAdapterPolicy getStrongReferencePolicy()
  {
    return CDOAdapterPolicy.ALL;
  }

  public void setStrongReferencePolicy(CDOAdapterPolicy policy)
  {
    throw new UnsupportedOperationException();
  }

  public CDOStaleReferencePolicy getStaleReferenceBehaviour()
  {
    return CDOStaleReferencePolicy.EXCEPTION;
  }

  public void setStaleReferenceBehaviour(CDOStaleReferencePolicy policy)
  {
    throw new UnsupportedOperationException();
  }

  public CDORevisionPrefetchingPolicy getRevisionPrefetchingPolicy()
  {
    return REVISION_PREFETCHING;
  }

  public void setRevisionPrefetchingPolicy(CDORevisionPrefetchingPolicy prefetchingPolicy)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @author Eike Stepper
   */
  private final class ServerCDOSession implements InternalCDOSession, CDORepositoryInfo
  {
    private InternalSession internalSession;

    private InternalRepository repository;

    public ServerCDOSession(InternalSession internalSession)
    {
      this.internalSession = internalSession;
      repository = internalSession.getManager().getRepository();
    }

    public CDOView[] getElements()
    {
      return new ServerCDOView[] { ServerCDOView.this };
    }

    public CDOView[] getViews()
    {
      return getElements();
    }

    public CDOView getView(int viewID)
    {
      return viewID == getViewID() ? ServerCDOView.this : null;
    }

    public CDOSessionProtocol getSessionProtocol()
    {
      throw new UnsupportedOperationException();
    }

    public CDOLobStore getLobStore()
    {
      throw new UnsupportedOperationException();
    }

    public InternalCDORevisionManager getRevisionManager()
    {
      return repository.getRevisionManager();
    }

    public InternalCDOPackageRegistry getPackageRegistry()
    {
      if (revisionProvider instanceof IStoreAccessor.CommitContext)
      {
        IStoreAccessor.CommitContext context = (IStoreAccessor.CommitContext)revisionProvider;
        return context.getPackageRegistry();
      }

      return repository.getPackageRegistry(false);
    }

    public InternalCDOCommitInfoManager getCommitInfoManager()
    {
      return repository.getCommitInfoManager();
    }

    public InternalCDOBranchManager getBranchManager()
    {
      return repository.getBranchManager();
    }

    public void setMainBranchLocal(boolean mainBranchLocal)
    {
      // Do nothing
    }

    public boolean hasListeners()
    {
      return false;
    }

    public IListener[] getListeners()
    {
      return null;
    }

    public void addListener(IListener listener)
    {
      // Do nothing
    }

    public void removeListener(IListener listener)
    {
      // Do nothing
    }

    public void activate() throws LifecycleException
    {
      throw new UnsupportedOperationException();
    }

    public Exception deactivate()
    {
      return ServerCDOView.this.deactivate();
    }

    public LifecycleState getLifecycleState()
    {
      return LifecycleState.ACTIVE;
    }

    public boolean isActive()
    {
      return ServerCDOView.this.isActive();
    }

    public boolean isClosed()
    {
      return !isActive();
    }

    public void close()
    {
      deactivate();
    }

    public CDORepositoryInfo getRepositoryInfo()
    {
      return this;
    }

    public String getName()
    {
      return repository.getName();
    }

    public String getUUID()
    {
      return repository.getUUID();
    }

    public Type getType()
    {
      return repository.getType();
    }

    public State getState()
    {
      return repository.getState();
    }

    public long getCreationTime()
    {
      return repository.getCreationTime();
    }

    public String getStoreType()
    {
      return repository.getStoreType();
    }


    public long getRootResourceID()
    {
      return repository.getRootResourceID();
    }

    public boolean isSupportingEcore()
    {
      return repository.isSupportingEcore();
    }

    public boolean isEnsuringReferentialIntegrity()
    {
      return repository.isEnsuringReferentialIntegrity();
    }

    public void handleRepositoryTypeChanged(Type oldType, Type newType)
    {
    }

    public void handleRepositoryStateChanged(State oldState, State newState)
    {
    }

    public EPackage[] loadPackages(CDOPackageUnit packageUnit)
    {
      return null;
    }

    public void releaseAtomicRequestLock(Object key)
    {
      // Do nothing
    }

    public void acquireAtomicRequestLock(Object key)
    {
      // Do nothing
    }

    public Object processPackage(Object value)
    {
      return value;
    }

    public boolean isEmpty()
    {
      return false;
    }

    public boolean waitForUpdate(long updateTime, long timeoutMillis)
    {
      throw new UnsupportedOperationException();
    }

    public void waitForUpdate(long updateTime)
    {
      throw new UnsupportedOperationException();
    }

    public String getUserID()
    {
      return null;
    }

    public int getSessionID()
    {
      return internalSession.getSessionID();
    }

    public void refresh()
    {
      throw new UnsupportedOperationException();
    }

    public Options options()
    {
      throw new UnsupportedOperationException();
    }

    public CDOView openView(String durableLockingID)
    {
      throw new UnsupportedOperationException();
    }

    public CDOView openView(String durableLockingID, ResourceSet resourceSet)
    {
      throw new UnsupportedOperationException();
    }

    public CDOView openView()
    {
      throw new UnsupportedOperationException();
    }

    public CDOView openView(ResourceSet resourceSet)
    {
      throw new UnsupportedOperationException();
    }

    public CDOView openView(long timeStamp)
    {
      throw new UnsupportedOperationException();
    }

    public CDOView openView(CDOBranch branch)
    {
      throw new UnsupportedOperationException();
    }

    public CDOView openView(CDOBranch branch, long timeStamp)
    {
      throw new UnsupportedOperationException();
    }

    public CDOView openView(CDOBranch branch, long timeStamp, ResourceSet resourceSet)
    {
      throw new UnsupportedOperationException();
    }

    public CDOTransaction openTransaction(CDOBranchPoint target, ResourceSet resourceSet)
    {
      throw new UnsupportedOperationException();
    }

    public CDOTransaction openTransaction(CDOBranchPoint target)
    {
      throw new UnsupportedOperationException();
    }

    public CDOView openView(CDOBranchPoint target, ResourceSet resourceSet)
    {
      throw new UnsupportedOperationException();
    }

    public CDOView openView(CDOBranchPoint target)
    {
      throw new UnsupportedOperationException();
    }

    public CDOTransaction openTransaction(String durableLockingID)
    {
      throw new UnsupportedOperationException();
    }

    public CDOTransaction openTransaction(String durableLockingID, ResourceSet resourceSet)
    {
      throw new UnsupportedOperationException();
    }

    public CDOTransaction openTransaction()
    {
      throw new UnsupportedOperationException();
    }

    public CDOTransaction openTransaction(CDOBranch branch)
    {
      throw new UnsupportedOperationException();
    }

    public CDOTransaction openTransaction(ResourceSet resourceSet)
    {
      throw new UnsupportedOperationException();
    }

    public CDOTransaction openTransaction(CDOBranch branch, ResourceSet resourceSet)
    {
      throw new UnsupportedOperationException();
    }

    public CDOFetchRuleManager getFetchRuleManager()
    {
      return null;
    }

    public ExceptionHandler getExceptionHandler()
    {
      return null;
    }

    public void viewDetached(InternalCDOView view)
    {
      // Do nothing
    }

    public void setUserID(String userID)
    {
      throw new UnsupportedOperationException();
    }

    public void setSessionProtocol(CDOSessionProtocol sessionProtocol)
    {
      throw new UnsupportedOperationException();
    }

    public void setSessionID(int sessionID)
    {
      throw new UnsupportedOperationException();
    }

    public void setRepositoryInfo(CDORepositoryInfo repositoryInfo)
    {
      throw new UnsupportedOperationException();
    }

    public void setRemoteSessionManager(InternalCDORemoteSessionManager remoteSessionManager)
    {
      throw new UnsupportedOperationException();
    }

    public void setLastUpdateTime(long lastUpdateTime)
    {
      throw new UnsupportedOperationException();
    }

    public void setFetchRuleManager(CDOFetchRuleManager fetchRuleManager)
    {
      throw new UnsupportedOperationException();
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler)
    {
      throw new UnsupportedOperationException();
    }

    public Object resolveElementProxy(CDORevision revision, EStructuralFeature feature, int accessIndex, int serverIndex)
    {
      throw new UnsupportedOperationException();
    }

    public void resolveAllElementProxies(CDORevision revision)
    {
      throw new UnsupportedOperationException();
    }
    

    public void processRefreshSessionResult(RefreshSessionResult result,
    		List<InternalCDOView> branchViews, Map<Long, InternalCDORevision> viewedRevisions)
    {
      throw new UnsupportedOperationException();
    }

    public void invalidate(CDOCommitInfo commitInfo, InternalCDOTransaction sender)
    {
      throw new UnsupportedOperationException();
    }

    public void handleCommitNotification(CDOCommitInfo commitInfo)
    {
      throw new UnsupportedOperationException();
    }

    public void handleBranchNotification(InternalCDOBranch branch)
    {
      throw new UnsupportedOperationException();
    }

    public InternalCDORemoteSessionManager getRemoteSessionManager()
    {
      throw new UnsupportedOperationException();
    }

    public CDOAuthenticator getAuthenticator()
    {
      throw new UnsupportedOperationException();
    }

    public void setAuthenticator(CDOAuthenticator authenticator)
    {
      throw new UnsupportedOperationException();
    }

    public void setRevisionManager(InternalCDORevisionManager revisionManager)
    {
      throw new UnsupportedOperationException();
    }

    public void setBranchManager(InternalCDOBranchManager branchManager)
    {
      throw new UnsupportedOperationException();
    }

    public void setCommitInfoManager(InternalCDOCommitInfoManager commitInfoManager)
    {
      throw new UnsupportedOperationException();
    }

    public void setPackageRegistry(InternalCDOPackageRegistry packageRegistry)
    {
      throw new UnsupportedOperationException();
    }

    public boolean isSticky()
    {
      return false;
    }

    public CDOBranchPoint getCommittedSinceLastRefresh(long id)
    {
      throw new UnsupportedOperationException();
    }

    public void setCommittedSinceLastRefresh(long id, CDOBranchPoint branchPoint)
    {
      throw new UnsupportedOperationException();
    }

    public void clearCommittedSinceLastRefresh()
    {
      throw new UnsupportedOperationException();
    }

    public CDOChangeSetData compareRevisions(CDOBranchPoint source, CDOBranchPoint target)
    {
      throw new UnsupportedOperationException();
    }

    public CDORevisionAvailabilityInfo createRevisionAvailabilityInfo(CDOBranchPoint branchPoint)
    {
      throw new UnsupportedOperationException();
    }

    public void cacheRevisions(CDORevisionAvailabilityInfo info)
    {
      throw new UnsupportedOperationException();
    }

  }

}
