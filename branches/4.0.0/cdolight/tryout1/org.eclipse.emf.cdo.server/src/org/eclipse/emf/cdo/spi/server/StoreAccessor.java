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
 *    Simon McDuff - bug 213402
 */
package org.eclipse.emf.cdo.spi.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.server.ISession;
import org.eclipse.emf.cdo.server.IStore;
import org.eclipse.emf.cdo.server.ITransaction;
import org.eclipse.emf.cdo.spi.common.model.InternalCDOPackageUnit;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevision;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevisionDelta;
import org.eclipse.net4j.util.WrappedException;
import org.eclipse.net4j.util.io.ExtendedDataInputStream;
import org.eclipse.net4j.util.om.monitor.OMMonitor;

/**
 * @author Eike Stepper
 * @since 2.0
 */
public abstract class StoreAccessor extends StoreAccessorBase
{
  protected StoreAccessor(Store store, ISession session)
  {
    super(store, session);
  }

  protected StoreAccessor(Store store, ITransaction transaction)
  {
    super(store, transaction);
  }

  /**
   * @since 4.0
   */
  @Override
  protected void doWrite(InternalCommitContext context, OMMonitor monitor)
  {
    String userID = context.getUserID();
    String commitComment = context.getCommitComment();

    boolean deltas = getStore().getSupportedChangeFormats().contains(IStore.ChangeFormat.DELTA);

    InternalCDOPackageUnit[] newPackageUnits = context.getNewPackageUnits();
    InternalCDORevision[] newObjects = context.getNewObjects();
    Long[] detachedObjects = context.getDetachedObjects();
    int dirtyCount = deltas ? context.getDirtyObjectDeltas().length : context.getDirtyObjects().length;

    try
    {
      monitor.begin(1 + newPackageUnits.length + 2 + newObjects.length + detachedObjects.length + dirtyCount);
      writeCommitInfo(userID, commitComment, monitor.fork());

      if (newPackageUnits.length != 0)
      {
        writePackageUnits(newPackageUnits, monitor.fork(newPackageUnits.length));
      }


      if (detachedObjects.length != 0)
      {
        detachObjects(detachedObjects, monitor.fork(detachedObjects.length));
      }

      if (newObjects.length != 0)
      {
        writeRevisions(newObjects, monitor.fork(newObjects.length));
      }

      if (dirtyCount != 0)
      {
        if (deltas)
        {
          writeRevisionDeltas(context.getDirtyObjectDeltas(), monitor.fork(dirtyCount));
        }
        else
        {
          writeRevisions(context.getDirtyObjects(), monitor.fork(dirtyCount));
        }
      }

      ExtendedDataInputStream in = context.getLobs();
      if (in != null)
      {
        try
        {
          int count = in.readInt();
          for (int i = 0; i < count; i++)
          {
            byte[] id = in.readByteArray();
            long size = in.readLong();
            if (size > 0)
            {
              writeBlob(id, size, in);
            }
            else
            {
              writeClob(id, -size, new InputStreamReader(in));
            }
          }
        }
        catch (IOException ex)
        {
          throw WrappedException.wrap(ex);
        }
      }
    }
    finally
    {
      monitor.done();
    }
  }


  /**
   * @since 4.0
   */
  protected abstract void writeCommitInfo(String userID, String comment, OMMonitor monitor);

  /**
   * @since 3.0
   */
  protected abstract void writeRevisions(InternalCDORevision[] revisions, OMMonitor monitor);

  /**
   * @since 3.0
   */
  protected abstract void writeRevisionDeltas(InternalCDORevisionDelta[] revisionDeltas, OMMonitor monitor);

  /**
   * @since 3.0
   */
  protected abstract void detachObjects(Long[] detachedObjects, OMMonitor monitor);

  /**
   * @since 4.0
   */
  protected abstract void writeBlob(byte[] id, long size, InputStream inputStream) throws IOException;

  /**
   * @since 4.0
   */
  protected abstract void writeClob(byte[] id, long size, Reader reader) throws IOException;
}
