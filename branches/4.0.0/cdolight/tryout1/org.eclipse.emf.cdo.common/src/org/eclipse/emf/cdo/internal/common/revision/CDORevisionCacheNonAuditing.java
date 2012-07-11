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
 */
package org.eclipse.emf.cdo.internal.common.revision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevision;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevisionCache;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.net4j.util.CheckUtil;

/**
 * @author Eike Stepper
 */
public class CDORevisionCacheNonAuditing extends AbstractCDORevisionCache
{
  private Map<Long, InternalCDORevision> revisions = new HashMap<Long, InternalCDORevision>();
  
  public static CDORevisionCacheNonAuditing INSTANCE = new CDORevisionCacheNonAuditing(); 

  private CDORevisionCacheNonAuditing()
  {
  }

  public InternalCDORevisionCache instantiate(CDORevision revision)
  {
    return INSTANCE;
  }

  public EClass getObjectType(long id)
  {
    synchronized (revisions)
    {
      InternalCDORevision ref = revisions.get(id);
      if (ref != null)
      {
        InternalCDORevision revision = ref;
        if (revision != null)
        {
          return revision.getEClass();
        }
      }

      return null;
    }
  }

  public InternalCDORevision getRevision(long id)
  {
    synchronized (revisions)
    {
      InternalCDORevision ref = revisions.get(id);
      if (ref != null)
      {
        InternalCDORevision revision = ref;
        if (revision != null )
        {
          return revision;
        }
      }

      return null;
    }
  }


  public List<CDORevision> getCurrentRevisions()
  {
    List<CDORevision> currentRevisions = new ArrayList<CDORevision>();
    synchronized (revisions)
    {
      for (InternalCDORevision ref : revisions.values())
      {
        InternalCDORevision revision = ref;
        if (revision != null )
        {
          currentRevisions.add(revision);
        }
      }
    }

    return currentRevisions;
  }

  public List<CDORevision> getAllRevisions()
  {
    List<CDORevision> result = new ArrayList<CDORevision>();
    synchronized (revisions)
    {
      List<CDORevision> list = new ArrayList<CDORevision>();
      for (InternalCDORevision ref : revisions.values())
      {
        InternalCDORevision revision = ref;
        if (revision != null)
        {
          list.add(revision);
        }
      }
    }

    return result;
  }

  public List<CDORevision> getRevisions(CDOBranchPoint branchPoint)
  {
    List<CDORevision> result = new ArrayList<CDORevision>();
    synchronized (revisions)
    {
      for (InternalCDORevision ref : revisions.values())
      {
        InternalCDORevision revision = ref;
        if (revision != null )
        {
          result.add(revision);
        }
      }
    }

    return result;
  }

  public void addRevision(CDORevision revision)
  {
    CheckUtil.checkArg(revision, "revision");
      synchronized (revisions)
      {
        revisions.put(revision.getID(), (InternalCDORevision)revision);
      }
  }

  public InternalCDORevision removeRevision(long id)
  {
    synchronized (revisions)
    {
      InternalCDORevision ref = revisions.get(id);
      if (ref != null)
      {
        InternalCDORevision revision = ref;
        if (revision != null)
        {
            revisions.remove(id);
            return revision;
        }
        else
        {
          revisions.remove(id);
        }
      }
    }

    return null;
  }

  public void clear()
  {
    synchronized (revisions)
    {
      revisions.clear();
    }
  }

  @Override
  public String toString()
  {
    synchronized (revisions)
    {
      return revisions.toString();
    }
  }
}
