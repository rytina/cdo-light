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
package org.eclipse.emf.cdo.spi.server;

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.net4j.util.ReflectUtil.ExcludeFromDump;

/**
 * @author Eike Stepper
 * @since 2.0
 */
public abstract class LongIDStore extends Store
{

  /**
   * @since 3.0
   */
  public static final long NULL = 0;

  @ExcludeFromDump
  private transient AtomicLong lastObjectID = new AtomicLong();

  @ExcludeFromDump
  private transient AtomicLong nextLocalObjectID = new AtomicLong(Long.MAX_VALUE);

  public LongIDStore(String type, Set<ChangeFormat> supportedChangeFormats,
      Set<RevisionTemporality> supportedRevisionTemporalities, Set<RevisionParallelism> supportedRevisionParallelisms)
  {
    super(type, supportedChangeFormats, supportedRevisionTemporalities, supportedRevisionParallelisms);
  }

  /**
   * @since 4.0
   */
  public long createObjectID(String val)
  {
    return Long.valueOf(val);
  }

  public long getLastObjectID()
  {
    return lastObjectID.get();
  }

  public void setLastObjectID(long lastObjectID)
  {
    this.lastObjectID.set(lastObjectID);
  }

  /**
   * @since 3.0
   */
  public long getNextLocalObjectID()
  {
    return nextLocalObjectID.get();
  }

  /**
   * @since 3.0
   */
  public void setNextLocalObjectID(long nextLocalObjectID)
  {
    this.nextLocalObjectID.set(nextLocalObjectID);
  }

  /**
   * @since 4.0
   */
  public long getNextCDOID(LongIDStoreAccessor accessor, CDORevision revision)
  {
      return nextLocalObjectID.getAndDecrement();
  }

  /**
   * @since 4.0
   */
  public boolean isLocal(long id)
  {
    return id > nextLocalObjectID.get();
  }

  /**
   * @since 4.0
   */
  public void ensureLastObjectID(long id)
  {
    if (id > getLastObjectID())
    {
      setLastObjectID(id);
    }
  }
}
