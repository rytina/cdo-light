/**
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Simon McDuff - initial API and implementation
 *    Eike Stepper - maintenance
 */
package org.eclipse.emf.cdo.server.internal.net4j.protocol;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.common.protocol.CDODataInput;
import org.eclipse.emf.cdo.common.protocol.CDODataOutput;
import org.eclipse.emf.cdo.common.protocol.CDOProtocolConstants;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.CDORevisionKey;
import org.eclipse.emf.cdo.spi.common.model.InternalCDOPackageRegistry;
import org.eclipse.emf.cdo.spi.common.model.InternalCDOPackageUnit;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevision;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevisionManager;
import org.eclipse.emf.cdo.spi.common.revision.SyntheticCDORevision;
import org.eclipse.emf.cdo.spi.server.InternalSession;

import org.eclipse.net4j.util.ObjectUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Simon McDuff
 */
public class RefreshSessionIndication extends CDOServerReadIndication
{
  private List<Long> viewedRevisions = new ArrayList<Long>();

  private long lastUpdateTime;

  private int initialChunkSize;

  private boolean enablePassiveUpdates;

  public RefreshSessionIndication(CDOServerProtocol protocol)
  {
    this(protocol, CDOProtocolConstants.SIGNAL_REFRESH_SESSION);
  }

  protected RefreshSessionIndication(CDOServerProtocol protocol, short signalID)
  {
    super(protocol, signalID);
  }

  public List<Long> getViewedRevisions()
  {
    return viewedRevisions;
  }

  @Override
  protected void indicating(CDODataInput in) throws IOException
  {
    lastUpdateTime = in.readLong();
    initialChunkSize = in.readInt();
    enablePassiveUpdates = in.readBoolean();

      List<Long> revisions = new ArrayList<Long>();
      int size = in.readInt();
      for (int j = 0; j < size; j++)
      {
        long revision = in.readLong();
        revisions.add(revision);
      }
  }

  @Override
  protected void responding(CDODataOutput out) throws IOException
  {
    writePackageUnits(out);
    writeRevisions(out);

    respondingDone();
  }

  protected void respondingDone()
  {
    InternalSession session = getSession();
    session.setPassiveUpdateEnabled(enablePassiveUpdates);
  }

  protected void writPackageUnit(CDODataOutput out, InternalCDOPackageUnit packageUnit) throws IOException
  {
    out.writeByte(CDOProtocolConstants.REFRESH_PACKAGE_UNIT);
    out.writeCDOPackageUnit(packageUnit, false);
  }

  protected void writeChangedObject(CDODataOutput out, InternalCDORevision revision) throws IOException
  {
    out.writeByte(CDOProtocolConstants.REFRESH_CHANGED_OBJECT);
    out.writeCDORevision(revision, initialChunkSize);
  }

  protected void writeDetachedObject(CDODataOutput out, long key) throws IOException
  {
    out.writeByte(CDOProtocolConstants.REFRESH_DETACHED_OBJECT);
    out.writeLong(key);
  }

  private void writePackageUnits(CDODataOutput out) throws IOException
  {
    InternalCDOPackageRegistry packageRegistry = getRepository().getPackageRegistry();
    InternalCDOPackageUnit[] packageUnits = packageRegistry.getPackageUnits();
    for (InternalCDOPackageUnit packageUnit : packageUnits)
    {
      writPackageUnit(out, packageUnit);
    }
  }

  private void writeRevisions(CDODataOutput out) throws IOException
  {
    InternalCDORevisionManager revisionManager = getRepository().getRevisionManager();


      for (Long key : viewedRevisions)
      {
        InternalCDORevision revision = revisionManager.getRevision(key, CDORevision.UNCHUNKED,
            CDORevision.DEPTH_NONE, true);

        if (revision == null)
        {
          writeDetachedObject(out, key);
        }
      }

    out.writeByte(CDOProtocolConstants.REFRESH_FINISHED);
  }

}
