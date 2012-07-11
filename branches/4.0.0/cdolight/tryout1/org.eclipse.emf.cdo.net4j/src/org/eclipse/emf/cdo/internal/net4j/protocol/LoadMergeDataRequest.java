/***************************************************************************
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 **************************************************************************/
package org.eclipse.emf.cdo.internal.net4j.protocol;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.cdo.common.protocol.CDODataInput;
import org.eclipse.emf.cdo.common.protocol.CDODataOutput;
import org.eclipse.emf.cdo.common.protocol.CDOProtocolConstants;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.CDORevisionKey;
import org.eclipse.emf.cdo.spi.common.commit.CDORevisionAvailabilityInfo;
import org.eclipse.net4j.util.om.monitor.OMMonitor;

/**
 * @author Eike Stepper
 */
public class LoadMergeDataRequest extends CDOClientRequestWithMonitoring<Set<Long>>
{
  private CDORevisionAvailabilityInfo targetInfo;

  private CDORevisionAvailabilityInfo sourceInfo;

  private CDORevisionAvailabilityInfo targetBaseInfo;

  private CDORevisionAvailabilityInfo sourceBaseInfo;

  private int infos;

  public LoadMergeDataRequest(CDOClientProtocol protocol, CDORevisionAvailabilityInfo targetInfo,
      CDORevisionAvailabilityInfo sourceInfo, CDORevisionAvailabilityInfo targetBaseInfo,
      CDORevisionAvailabilityInfo sourceBaseInfo)
  {
    super(protocol, CDOProtocolConstants.SIGNAL_LOAD_MERGE_DATA);
    this.targetInfo = targetInfo;
    this.sourceInfo = sourceInfo;
    this.targetBaseInfo = targetBaseInfo;
    this.sourceBaseInfo = sourceBaseInfo;
    infos = 2 + (targetBaseInfo != null ? 1 : 0) + (sourceBaseInfo != null ? 1 : 0);
  }

  @Override
  protected void requesting(CDODataOutput out, OMMonitor monitor) throws IOException
  {
    out.writeInt(infos);
    monitor.begin(infos);

    try
    {
      writeRevisionAvailabilityInfo(out, targetInfo, monitor.fork());
      writeRevisionAvailabilityInfo(out, sourceInfo, monitor.fork());

      if (infos > 2)
      {
        writeRevisionAvailabilityInfo(out, targetBaseInfo, monitor.fork());
      }

      if (infos > 3)
      {
        writeRevisionAvailabilityInfo(out, sourceBaseInfo, monitor.fork());
      }
    }
    finally
    {
      monitor.done();
    }
  }

  private void writeRevisionAvailabilityInfo(CDODataOutput out, CDORevisionAvailabilityInfo info, OMMonitor monitor)
      throws IOException
  {
    Set<Long> availableRevisions = info.getAvailableRevisions().keySet();
    int size = availableRevisions.size();

    out.writeInt(size);

    monitor.begin(size);

    try
    {
      for (Long id : availableRevisions)
      {
        out.writeCDOID(id);
        monitor.worked();
      }
    }
    finally
    {
      monitor.done();
    }
  }

  @Override
  protected Set<Long> confirming(CDODataInput in, OMMonitor monitor) throws IOException
  {
    Set<Long> result = new HashSet<Long>();

    int size = in.readInt();
    monitor.begin(size + infos);

    try
    {
      for (int i = 0; i < size; i++)
      {
        long id = in.readCDOID();
        result.add(id);
        monitor.worked();
      }

      readRevisionAvailabilityInfo(in, targetInfo, result, monitor.fork());
      readRevisionAvailabilityInfo(in, sourceInfo, result, monitor.fork());

      if (infos > 2)
      {
        readRevisionAvailabilityInfo(in, targetBaseInfo, result, monitor.fork());
      }

      if (infos > 3)
      {
        readRevisionAvailabilityInfo(in, sourceBaseInfo, result, monitor.fork());
      }

      return result;
    }
    finally
    {
      monitor.done();
    }
  }

  private void readRevisionAvailabilityInfo(CDODataInput in, CDORevisionAvailabilityInfo info, Set<Long> result,
      OMMonitor monitor) throws IOException
  {
    int size = in.readInt();
    monitor.begin(size + 1);

    try
    {
      for (int i = 0; i < size; i++)
      {
        CDORevision revision;
        if (in.readBoolean())
        {
          revision = in.readCDORevision();
        }
        else
        {
          long key = in.readCDOID();
          revision = getRevision(key, targetInfo);

          if (revision == null && sourceInfo != null)
          {
            revision = getRevision(key, sourceInfo);
          }

          if (revision == null && targetBaseInfo != null)
          {
            revision = getRevision(key, targetBaseInfo);
          }

          if (revision == null)
          {
            throw new IllegalStateException("Missing revision: " + key);
          }
        }

        info.addRevision(revision);
        monitor.worked();
      }

      Set<Map.Entry<Long, CDORevisionKey>> entrySet = info.getAvailableRevisions().entrySet();
      for (Iterator<Map.Entry<Long, CDORevisionKey>> it = entrySet.iterator(); it.hasNext();)
      {
        Map.Entry<Long, CDORevisionKey> entry = it.next();
        if (!result.contains(entry.getKey()))
        {
          it.remove();
        }
      }

      monitor.worked();
    }
    finally
    {
      monitor.done();
    }
  }

  private CDORevision getRevision(long key, CDORevisionAvailabilityInfo info)
  {
    CDORevisionKey revision = info.getRevision(key);
    if (revision instanceof CDORevision)
    {
        return (CDORevision)revision;
    }

    return null;
  }
}
