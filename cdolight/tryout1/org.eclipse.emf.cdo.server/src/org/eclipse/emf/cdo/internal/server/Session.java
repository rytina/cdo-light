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
 *    Simon McDuff - bug 233490
 *    Simon McDuff - bug 213402
 */
package org.eclipse.emf.cdo.internal.server;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.cdo.common.CDOCommonRepository;
import org.eclipse.emf.cdo.common.CDOCommonSession;
import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.common.commit.CDOCommitInfo;
import org.eclipse.emf.cdo.common.id.CDOIDUtil;
import org.eclipse.emf.cdo.common.model.CDOModelUtil;
import org.eclipse.emf.cdo.common.protocol.CDOProtocolConstants;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
import org.eclipse.emf.cdo.internal.common.commit.DelegatingCommitInfo;
import org.eclipse.emf.cdo.server.IView;
import org.eclipse.emf.cdo.session.remote.CDORemoteSessionMessage;
import org.eclipse.emf.cdo.spi.common.branch.InternalCDOBranch;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevision;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevisionManager;
import org.eclipse.emf.cdo.spi.server.ISessionProtocol;
import org.eclipse.emf.cdo.spi.server.InternalSession;
import org.eclipse.emf.cdo.spi.server.InternalSessionManager;
import org.eclipse.emf.cdo.spi.server.InternalTransaction;
import org.eclipse.emf.cdo.spi.server.InternalView;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.net4j.util.ReflectUtil.ExcludeFromDump;
import org.eclipse.net4j.util.collection.IndexedList;
import org.eclipse.net4j.util.container.Container;
import org.eclipse.net4j.util.event.EventUtil;
import org.eclipse.net4j.util.event.IListener;
import org.eclipse.net4j.util.lifecycle.ILifecycle;
import org.eclipse.net4j.util.lifecycle.LifecycleEventAdapter;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;
import org.eclipse.net4j.util.om.log.OMLogger;

/**
 * @author Eike Stepper
 */
public class Session extends Container<IView> implements InternalSession
{
  private InternalSessionManager manager;

  private ISessionProtocol protocol;

  private int sessionID;

  private String userID;

  private boolean passiveUpdateEnabled = true;

  private PassiveUpdateMode passiveUpdateMode = PassiveUpdateMode.INVALIDATIONS;


  @ExcludeFromDump
  private Object lastUpdateTimeLock = new Object();

  private ConcurrentMap<Integer, InternalView> views = new ConcurrentHashMap<Integer, InternalView>();

  private AtomicInteger lastTempViewID = new AtomicInteger();

  @ExcludeFromDump
  private IListener protocolListener = new LifecycleEventAdapter()
  {
    @Override
    protected void onDeactivated(ILifecycle lifecycle)
    {
      deactivate();
    }
  };

  private boolean subscribed;

  /**
   * @since 2.0
   */
  public Session(InternalSessionManager manager, ISessionProtocol protocol, int sessionID, String userID)
  {
    this.manager = manager;
    this.protocol = protocol;
    this.sessionID = sessionID;
    this.userID = userID;

    EventUtil.addListener(protocol, protocolListener);
    activate();
  }

  /**
   * @since 2.0
   */
  public Options options()
  {
    return this;
  }

  /**
   * @since 2.0
   */
  public CDOCommonSession getContainer()
  {
    return this;
  }

  public InternalSessionManager getManager()
  {
    return manager;
  }

  public ISessionProtocol getProtocol()
  {
    return protocol;
  }

  public int getSessionID()
  {
    return sessionID;
  }

  /**
   * @since 2.0
   */
  public String getUserID()
  {
    return userID;
  }

  /**
   * @since 2.0
   */
  public boolean isSubscribed()
  {
    return subscribed;
  }

  /**
   * @since 2.0
   */
  public void setSubscribed(boolean subscribed)
  {
    checkActive();
    if (this.subscribed != subscribed)
    {
      this.subscribed = subscribed;
      byte opcode = subscribed ? CDOProtocolConstants.REMOTE_SESSION_SUBSCRIBED
          : CDOProtocolConstants.REMOTE_SESSION_UNSUBSCRIBED;
      manager.sendRemoteSessionNotification(this, opcode);
    }
  }

  /**
   * @since 2.0
   */
  public boolean isPassiveUpdateEnabled()
  {
    return passiveUpdateEnabled;
  }

  /**
   * @since 2.0
   */
  public void setPassiveUpdateEnabled(boolean passiveUpdateEnabled)
  {
    checkActive();
    this.passiveUpdateEnabled = passiveUpdateEnabled;
  }

  public PassiveUpdateMode getPassiveUpdateMode()
  {
    return passiveUpdateMode;
  }

  public void setPassiveUpdateMode(PassiveUpdateMode passiveUpdateMode)
  {
    checkActive();
    checkArg(passiveUpdateMode, "passiveUpdateMode");
    this.passiveUpdateMode = passiveUpdateMode;
  }


  public InternalView[] getElements()
  {
    checkActive();
    return getViews();
  }

  @Override
  public boolean isEmpty()
  {
    checkActive();
    return views.isEmpty();
  }

  public InternalView[] getViews()
  {
    checkActive();
    return getViewsArray();
  }

  private InternalView[] getViewsArray()
  {
    return views.values().toArray(new InternalView[views.size()]);
  }

  public InternalView getView(int viewID)
  {
    checkActive();
    return views.get(viewID);
  }

  /**
   * @since 2.0
   */
  public InternalView openView(int viewID)
  {
    checkActive();
    if (viewID == TEMP_VIEW_ID)
    {
      viewID = -lastTempViewID.incrementAndGet();
    }

    InternalView view = new View(this, viewID);
    view.activate();
    addView(view);
    return view;
  }

  /**
   * @since 2.0
   */
  public InternalTransaction openTransaction(int viewID)
  {
    checkActive();
    if (viewID == TEMP_VIEW_ID)
    {
      viewID = -lastTempViewID.incrementAndGet();
    }

    InternalTransaction transaction = new Transaction(this, viewID);
    transaction.activate();
    addView(transaction);
    return transaction;
  }

  private void addView(InternalView view)
  {
    checkActive();
    int viewID = view.getViewID();
    views.put(viewID, view);
    fireElementAddedEvent(view);
  }

  /**
   * @since 2.0
   */
  public void viewClosed(InternalView view)
  {
    int viewID = view.getViewID();
    if (views.remove(viewID) == view)
    {
      view.doClose();
      fireElementRemovedEvent(view);
    }
  }

  /**
   * TODO I can't see how recursion is controlled/limited
   * 
   * @since 2.0
   */
  public void collectContainedRevisions(InternalCDORevision revision, int referenceChunk,
      Set<Long> revisions, List<CDORevision> additionalRevisions)
  {
    InternalCDORevisionManager revisionManager = getManager().getRepository().getRevisionManager();
    EClass eClass = revision.getEClass();
    EStructuralFeature[] features = CDOModelUtil.getAllPersistentFeatures(eClass);
    for (int i = 0; i < features.length; i++)
    {
      EStructuralFeature feature = features[i];
      // TODO Clarify feature maps
      if (feature instanceof EReference && !feature.isMany() && ((EReference)feature).isContainment())
      {
        Object value = revision.getValue(feature);
          long id = (Long)value;
          if (!CDOIDUtil.isNull(id) && !revisions.contains(id))
          {
            InternalCDORevision containedRevision = revisionManager.getRevision(id, referenceChunk,
                CDORevision.DEPTH_NONE, true);
            revisions.add(id);
            additionalRevisions.add(containedRevision);

            // Recurse
            collectContainedRevisions(containedRevision, referenceChunk, revisions, additionalRevisions);
          }
      }
    }
  }


  public void sendRepositoryTypeNotification(CDOCommonRepository.Type oldType, CDOCommonRepository.Type newType)
      throws Exception
  {
    if (protocol != null)
    {
      protocol.sendRepositoryTypeNotification(oldType, newType);
    }
  }

  public void sendRepositoryStateNotification(CDOCommonRepository.State oldState, CDOCommonRepository.State newState)
      throws Exception
  {
    if (protocol != null)
    {
      protocol.sendRepositoryStateNotification(oldState, newState);
    }
  }

  public void sendCommitNotification(final CDOCommitInfo commitInfo) throws Exception
  {
    if (protocol == null)
    {
      return;
    }

    if (!isPassiveUpdateEnabled())
    {
      return;
    }

    final InternalView[] views = getViews();
    protocol.sendCommitNotification(new DelegatingCommitInfo()
    {
      private final PassiveUpdateMode passiveUpdateMode = getPassiveUpdateMode();

      private final boolean additions = passiveUpdateMode == PassiveUpdateMode.ADDITIONS;

      private final boolean changes = passiveUpdateMode == PassiveUpdateMode.CHANGES;

      @Override
      protected CDOCommitInfo getDelegate()
      {
        return commitInfo;
      }

      @Override
      public List<CDORevision> getNewObjects()
      {
        final List<CDORevision> newObjects = super.getNewObjects();
        return new IndexedList<CDORevision>()
        {
          @Override
          public CDORevision get(int index)
          {
            // The following will always be a CDORevision!
            CDORevision newObject = newObjects.get(index);
            if (additions)
            {
              // Return full revisions if not in INVALIDATION mode
              return newObject;
            }

            return newObject;
          }

          @Override
          public int size()
          {
            return newObjects.size();
          }
        };
      }

      @Override
      public List<CDORevisionDelta> getChangedObjects()
      {
        final List<CDORevisionDelta> changedObjects = super.getChangedObjects();
        return new IndexedList<CDORevisionDelta>()
        {
          @Override
          public CDORevisionDelta get(int index)
          {
            // The following will always be a CDORevisionDelta!
            CDORevisionDelta changedObject = changedObjects.get(index);
              return changedObject;
          }

          @Override
          public int size()
          {
            return changedObjects.size();
          }
        };
      }
    });

  }

  private boolean hasSubscription(long id, InternalView[] views)
  {
    for (InternalView view : views)
    {
      if (view.hasSubscription(id))
      {
        return true;
      }
    }

    return false;
  }

  public void sendRemoteSessionNotification(InternalSession sender, byte opcode) throws Exception
  {
    if (protocol != null)
    {
      protocol.sendRemoteSessionNotification(sender, opcode);
    }
  }

  public void sendRemoteMessageNotification(InternalSession sender, CDORemoteSessionMessage message) throws Exception
  {
    if (protocol != null)
    {
      protocol.sendRemoteMessageNotification(sender, message);
    }
  }

  @Override
  public String toString()
  {
    return MessageFormat.format("Session[{0}]", sessionID); //$NON-NLS-1$
  }

  /**
   * @since 2.0
   */
  public void close()
  {
    LifecycleUtil.deactivate(this, OMLogger.Level.DEBUG);
  }

  /**
   * @since 2.0
   */
  public boolean isClosed()
  {
    return !isActive();
  }

  @Override
  protected void doDeactivate() throws Exception
  {
    EventUtil.removeListener(protocol, protocolListener);
    protocolListener = null;

    LifecycleUtil.deactivate(protocol, OMLogger.Level.DEBUG);
    protocol = null;

    for (IView view : getViewsArray())
    {
      view.close();
    }

    views = null;
    manager.sessionClosed(this);
    manager = null;
    super.doDeactivate();
  }

}
