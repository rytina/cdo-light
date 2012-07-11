/***************************************************************************
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Simon McDuff - initial API and implementation
 *    Eike Stepper - maintenance
 **************************************************************************/
package org.eclipse.emf.cdo.internal.net4j.protocol;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.model.CDOPackageUnit;
import org.eclipse.emf.cdo.common.model.EMFUtil;
import org.eclipse.emf.cdo.common.protocol.CDODataInput;
import org.eclipse.emf.cdo.common.protocol.CDODataOutput;
import org.eclipse.emf.cdo.common.protocol.CDOProtocolConstants;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevision;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.spi.cdo.CDOSessionProtocol.RefreshSessionResult;

/**
 * @author Simon McDuff
 * @since 2.0
 */
public class RefreshSessionRequest extends CDOClientRequest<RefreshSessionResult>
{
  private Map<Long, InternalCDORevision> viewedRevisions;

  private int initialChunkSize;

  private boolean enablePassiveUpdates;

  public RefreshSessionRequest(CDOClientProtocol protocol, Map<Long, InternalCDORevision> viewedRevisions, int initialChunkSize,
      boolean enablePassiveUpdates)
  {
    this(protocol, CDOProtocolConstants.SIGNAL_REFRESH_SESSION, viewedRevisions, initialChunkSize,
        enablePassiveUpdates);
  }

  protected RefreshSessionRequest(CDOClientProtocol protocol, short signalID, Map<Long, InternalCDORevision> viewedRevisions, int initialChunkSize,
      boolean enablePassiveUpdates)
  {
    super(protocol, signalID);
    this.viewedRevisions = viewedRevisions;
    this.initialChunkSize = initialChunkSize;
    this.enablePassiveUpdates = enablePassiveUpdates;
  }

  @Override
  protected void requesting(CDODataOutput out) throws IOException
  {
    out.writeInt(initialChunkSize);
    out.writeBoolean(enablePassiveUpdates);

    out.writeInt(viewedRevisions.size());
      Map<Long, InternalCDORevision> revisions = viewedRevisions;

      out.writeInt(revisions.size());
      for (InternalCDORevision revision : revisions.values())
      {
        out.writeCDOID(revision.getID());
      }
  }

  @Override
  protected RefreshSessionResult confirming(CDODataInput in) throws IOException
  {
    RefreshSessionResult result = new RefreshSessionResult();

    ResourceSet resourceSet = EMFUtil.newEcoreResourceSet();
    for (;;)
    {
      byte type = in.readByte();
      switch (type)
      {
      case CDOProtocolConstants.REFRESH_PACKAGE_UNIT:
      {
        CDOPackageUnit packageUnit = in.readCDOPackageUnit(resourceSet);
        result.addPackageUnit(packageUnit);
        break;
      }

      case CDOProtocolConstants.REFRESH_CHANGED_OBJECT:
      {
        InternalCDORevision revision = (InternalCDORevision)in.readCDORevision();
        result.addChangedObject(revision);
        break;
      }

      case CDOProtocolConstants.REFRESH_DETACHED_OBJECT:
      {
        long key = in.readCDOID();
        result.addDetachedObject(key);
        break;
      }

      case CDOProtocolConstants.REFRESH_FINISHED:
        return result;

      default:
        throw new IOException("Invalid refresh type: " + type);
      }
    }
  }
}
