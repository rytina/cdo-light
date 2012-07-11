/***************************************************************************
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 *    Caspar De Groot - maintenance
 **************************************************************************/
package org.eclipse.emf.cdo.internal.net4j.protocol;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.common.protocol.CDODataInput;
import org.eclipse.emf.cdo.common.protocol.CDODataOutput;
import org.eclipse.emf.cdo.common.protocol.CDOProtocolConstants;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.CDORevisionKey;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevision;

import org.eclipse.net4j.util.concurrent.IRWLockManager;
import org.eclipse.net4j.util.concurrent.IRWLockManager.LockType;

import org.eclipse.emf.spi.cdo.CDOSessionProtocol.LockObjectsResult;

import java.io.IOException;
import java.util.List;

/**
 * @author Eike Stepper, Caspar De Groot
 */
public class LockObjectsRequest extends CDOClientRequest<LockObjectsResult>
{
  private int viewID;

  private IRWLockManager.LockType lockType;

  private long timeout;

  private List<InternalCDORevision> viewedRevisions;


  public LockObjectsRequest(CDOClientProtocol protocol, List<InternalCDORevision> viewedRevisions, int viewID, LockType lockType, long timeout)
  {
    super(protocol, CDOProtocolConstants.SIGNAL_LOCK_OBJECTS);

    this.viewID = viewID;
    this.lockType = lockType;
    this.timeout = timeout;
    this.viewedRevisions = viewedRevisions;
  }

  @Override
  protected void requesting(CDODataOutput out) throws IOException
  {
    out.writeInt(viewID);
    out.writeCDOLockType(lockType);
    out.writeLong(timeout);

    out.writeInt(viewedRevisions.size());
    for (CDORevision revision : viewedRevisions)
    {
      out.writeCDOID(revision.getID());
    }
  }

  @Override
  protected LockObjectsResult confirming(CDODataInput in) throws IOException
  {
    boolean succesful = in.readBoolean();
    if (succesful)
    {
      boolean clientMustWait = in.readBoolean();

      return new LockObjectsResult(true, false, clientMustWait, null);
    }

    boolean timedOut = in.readBoolean();
    if (timedOut)
    {
      return new LockObjectsResult(false, true, false, null);
    }

    int nStaleRevisions = in.readInt();
    long[] staleRevisions = new long[nStaleRevisions];
    for (int i = 0; i < nStaleRevisions; i++)
    {
      staleRevisions[i] = in.readCDOID();
    }

    return new LockObjectsResult(false, false, false, staleRevisions);
  }
}