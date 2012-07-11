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

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.commit.CDOChangeKind;
import org.eclipse.emf.cdo.common.commit.CDOChangeSetData;
import org.eclipse.emf.cdo.common.commit.CDOCommitData;
import org.eclipse.emf.cdo.common.commit.CDOCommitInfo;
import org.eclipse.emf.cdo.common.model.CDOPackageUnit;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
import org.eclipse.emf.cdo.internal.common.branch.CDOBranchPointImpl;
import org.eclipse.emf.cdo.spi.common.commit.InternalCDOCommitInfoManager;
import org.eclipse.net4j.util.CheckUtil;

/**
 * @author Eike Stepper
 */
public class CDOCommitInfoImpl extends CDOBranchPointImpl implements CDOCommitInfo
{
  private InternalCDOCommitInfoManager commitInfoManager;

  private String userID;

  private String comment;

  private CDOCommitData commitData;

  public CDOCommitInfoImpl(InternalCDOCommitInfoManager commitInfoManager, String userID, String comment, CDOCommitData commitData)
  {
    CheckUtil.checkArg(commitInfoManager, "commitInfoManager"); //$NON-NLS-1$
    this.commitInfoManager = commitInfoManager;
    this.userID = userID;
    this.comment = comment;
    this.commitData = commitData;
  }

  public InternalCDOCommitInfoManager getCommitInfoManager()
  {
    return commitInfoManager;
  }


  public String getUserID()
  {
    return userID;
  }

  public String getComment()
  {
    return comment;
  }

  public boolean isEmpty()
  {
    return false;
  }

  public CDOChangeSetData copy()
  {
    return commitData == null ? null : commitData.copy();
  }

  public void merge(CDOChangeSetData changeSetData)
  {
    loadCommitDataIfNeeded();
    commitData.merge(changeSetData);
  }

  public synchronized List<CDOPackageUnit> getNewPackageUnits()
  {
    loadCommitDataIfNeeded();
    return commitData.getNewPackageUnits();
  }

  public synchronized List<CDORevision> getNewObjects()
  {
    loadCommitDataIfNeeded();
    return commitData.getNewObjects();
  }

  public synchronized List<CDORevisionDelta> getChangedObjects()
  {
    loadCommitDataIfNeeded();
    return commitData.getChangedObjects();
  }

  public synchronized List<Long> getDetachedObjects()
  {
    loadCommitDataIfNeeded();
    return commitData.getDetachedObjects();
  }

  public CDOChangeKind getChangeKind(long id)
  {
    loadCommitDataIfNeeded();
    return commitData.getChangeKind(id);
  }

  @Override
  public String toString()
  {
    String data = null;
    if (commitData != null)
    {
      data = commitData.toString();
    }

    return MessageFormat
        .format(
            "CommitInfo[{0}, {1}, {2}, {3}, {4}, {5}]", getUserID(), getComment(), data); //$NON-NLS-1$
  }

  private void loadCommitDataIfNeeded()
  {
    if (commitData == null)
    {
      commitData = commitInfoManager.getCommitInfoLoader().loadCommitData();
    }
  }
}
