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
package org.eclipse.emf.cdo.internal.common.commit;

import java.util.List;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.commit.CDOChangeKind;
import org.eclipse.emf.cdo.common.commit.CDOChangeSetData;
import org.eclipse.emf.cdo.common.commit.CDOCommitInfo;
import org.eclipse.emf.cdo.common.commit.CDOCommitInfoManager;
import org.eclipse.emf.cdo.common.model.CDOPackageUnit;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;

/**
 * @author Eike Stepper
 */
public abstract class DelegatingCommitInfo implements CDOCommitInfo
{
  public DelegatingCommitInfo()
  {
  }

  protected abstract CDOCommitInfo getDelegate();


  public CDOCommitInfoManager getCommitInfoManager()
  {
    return getDelegate().getCommitInfoManager();
  }


  public String getUserID()
  {
    return getDelegate().getUserID();
  }

  public String getComment()
  {
    return getDelegate().getComment();
  }

  public boolean isEmpty()
  {
    return getDelegate().isEmpty();
  }

  public List<CDOPackageUnit> getNewPackageUnits()
  {
    return getDelegate().getNewPackageUnits();
  }

  public List<CDORevision> getNewObjects()
  {
    return getDelegate().getNewObjects();
  }

  public List<CDORevisionDelta> getChangedObjects()
  {
    return getDelegate().getChangedObjects();
  }

  public List<Long> getDetachedObjects()
  {
    return getDelegate().getDetachedObjects();
  }

  public CDOChangeKind getChangeKind(long id)
  {
    return getDelegate().getChangeKind(id);
  }

  public CDOChangeSetData copy()
  {
    return getDelegate().copy();
  }

  public void merge(CDOChangeSetData changeSetData)
  {
    getDelegate().merge(changeSetData);
  }
}
