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
package org.eclipse.emf.cdo.internal.common.revision;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevision;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevisionCache;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.net4j.util.lifecycle.Lifecycle;

/**
 * @author Eike Stepper
 */
public class NOOPRevisionCache extends Lifecycle implements InternalCDORevisionCache
{
  public static final NOOPRevisionCache INSTANCE = new NOOPRevisionCache();

  private static final List<CDORevision> EMPTY = Collections.emptyList();

  public NOOPRevisionCache()
  {
  }

  public InternalCDORevisionCache instantiate(CDORevision revision)
  {
    return this;
  }

  public boolean isSupportingBranches()
  {
    return true;
  }

  public EClass getObjectType(long id)
  {
    return null;
  }

  public List<CDORevision> getCurrentRevisions()
  {
    return EMPTY;
  }

  public InternalCDORevision getRevision(long id)
  {
    return null;
  }

  public InternalCDORevision getRevision(long id, CDOBranchPoint branchPoint)
  {
    return null;
  }



  public void addRevision(CDORevision revision)
  {
    // Do nothing
  }

  public InternalCDORevision removeRevision(long id)
  {
    return null;
  }

  public void clear()
  {
    // Do nothing
  }

  public List<CDORevision> getAllRevisions()
  {
    return Collections.emptyList();
  }

  public List<CDORevision> getRevisions(CDOBranchPoint branchPoint)
  {
    return Collections.emptyList();
  }
}
