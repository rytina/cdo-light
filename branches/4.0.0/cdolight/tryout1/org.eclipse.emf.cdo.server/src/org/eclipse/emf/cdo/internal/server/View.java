/**
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 *    Simon McDuff - bug 233490
 */
package org.eclipse.emf.cdo.internal.server;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.CDORevisionManager;
import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevision;
import org.eclipse.emf.cdo.spi.server.InternalRepository;
import org.eclipse.emf.cdo.spi.server.InternalSession;
import org.eclipse.emf.cdo.spi.server.InternalView;
import org.eclipse.net4j.util.lifecycle.Lifecycle;

/**
 * @author Eike Stepper
 */
public class View extends Lifecycle implements InternalView
{
  private InternalSession session;

  private int viewID;

  private String durableLockingID;

  private InternalRepository repository;

  private Set<Long> changeSubscriptionIDs = new HashSet<Long>();

  /**
   * @since 2.0
   */
  public View(InternalSession session, int viewID)
  {
    this.session = session;
    this.viewID = viewID;

    repository = session.getManager().getRepository();
  }

  public InternalSession getSession()
  {
    return session;
  }

  public int getViewID()
  {
    return viewID;
  }

  public boolean isReadOnly()
  {
    return true;
  }

  public String getDurableLockingID()
  {
    return durableLockingID;
  }

  /**
   * @since 2.0
   */
  public InternalRepository getRepository()
  {
    checkOpen();
    return repository;
  }

  public InternalCDORevision getRevision(long id)
  {
    CDORevisionManager revisionManager = repository.getRevisionManager();
    return (InternalCDORevision)revisionManager.getRevision(id, CDORevision.UNCHUNKED, CDORevision.DEPTH_NONE,
        true);
  }

  public void changeTarget(List<Long> invalidObjects,
      List<CDORevisionDelta> allChangedObjects, List<Long> allDetachedObjects)
  {
    List<CDORevision> oldRevisions = getRevisions(invalidObjects);
    List<CDORevision> newRevisions = getRevisions(invalidObjects);

    Iterator<CDORevision> it = newRevisions.iterator();
    for (CDORevision oldRevision : oldRevisions)
    {
      CDORevision newRevision = it.next();
      if (newRevision == null)
      {
        allDetachedObjects.add(oldRevision.getID());
      }
      else if (newRevision != oldRevision)
      {
        CDORevisionDelta delta = newRevision.compare(oldRevision);
        allChangedObjects.add(delta);
      }
    }
  }

  private List<CDORevision> getRevisions(List<Long> ids)
  {
    return repository.getRevisionManager().getRevisions(ids, CDORevision.UNCHUNKED,
        CDORevision.DEPTH_NONE, true);
  }


  public void setDurableLockingID(String durableLockingID)
  {
    this.durableLockingID = durableLockingID;
  }

  /**
   * @since 2.0
   */
  public synchronized void subscribe(long id)
  {
    checkOpen();
    changeSubscriptionIDs.add(id);
  }

  /**
   * @since 2.0
   */
  public synchronized void unsubscribe(long id)
  {
    checkOpen();
    changeSubscriptionIDs.remove(id);
  }

  /**
   * @since 2.0
   */
  public synchronized boolean hasSubscription(long id)
  {
    checkOpen();
    return changeSubscriptionIDs.contains(id);
  }

  /**
   * @since 2.0
   */
  public synchronized void clearChangeSubscription()
  {
    checkOpen();
    changeSubscriptionIDs.clear();
  }

  @Override
  public String toString()
  {
    int sessionID = session == null ? 0 : session.getSessionID();
    return MessageFormat.format("{0}[{1}:{2}]", getClassName(), sessionID, viewID); //$NON-NLS-1$
  }

  protected String getClassName()
  {
    return "View"; //$NON-NLS-1$
  }

  /**
   * @since 2.0
   */
  public void close()
  {
    deactivate();
  }

  @Override
  protected void doDeactivate() throws Exception
  {
    if (!isClosed())
    {
      session.viewClosed(this);
    }

    super.doDeactivate();
  }

  /**
   * @since 2.0
   */
  public void doClose()
  {
    clearChangeSubscription();
    changeSubscriptionIDs = null;
    session = null;
    repository = null;
  }

  /**
   * @since 2.0
   */
  public boolean isClosed()
  {
    return repository == null;
  }

  private void checkOpen()
  {
    if (isClosed())
    {
      throw new IllegalStateException("View closed"); //$NON-NLS-1$
    }
  }
}
