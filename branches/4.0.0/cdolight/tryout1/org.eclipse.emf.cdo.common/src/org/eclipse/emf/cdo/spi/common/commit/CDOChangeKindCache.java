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
package org.eclipse.emf.cdo.spi.common.commit;

import java.util.HashMap;
import java.util.List;

import org.eclipse.emf.cdo.common.commit.CDOChangeKind;
import org.eclipse.emf.cdo.common.commit.CDOChangeKindProvider;
import org.eclipse.emf.cdo.common.commit.CDOChangeSetData;
import org.eclipse.emf.cdo.common.revision.CDOIDAndVersion;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;

/**
 * @author Eike Stepper
 * @since 4.0
 */
public class CDOChangeKindCache extends HashMap<Long, CDOChangeKind> implements CDOChangeKindProvider
{
  private static final long serialVersionUID = 1L;

  public CDOChangeKindCache(CDOChangeSetData changeSetData)
  {
    List<CDORevision> newObjects = changeSetData.getNewObjects();
    if (newObjects != null)
    {
      for (CDOIDAndVersion key : newObjects)
      {
        put(key.getID(), CDOChangeKind.NEW);
      }
    }

    List<CDORevisionDelta> changedObjects = changeSetData.getChangedObjects();
    if (changedObjects != null)
    {
      for (CDOIDAndVersion key : changedObjects)
      {
        put(key.getID(), CDOChangeKind.CHANGED);
      }
    }

    List<Long> detachedObjects = changeSetData.getDetachedObjects();
    if (detachedObjects != null)
    {
      for (Long key : detachedObjects)
      {
        put(key, CDOChangeKind.DETACHED);
      }
    }
  }

  public CDOChangeKind getChangeKind(long id)
  {
    return get(id);
  }
}
