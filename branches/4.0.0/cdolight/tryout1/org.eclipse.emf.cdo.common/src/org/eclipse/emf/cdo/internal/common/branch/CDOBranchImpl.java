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
package org.eclipse.emf.cdo.internal.common.branch;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.spi.common.branch.InternalCDOBranch;
import org.eclipse.emf.cdo.spi.common.branch.InternalCDOBranchManager;
import org.eclipse.emf.cdo.spi.common.branch.InternalCDOBranchManager.BranchLoader;
import org.eclipse.emf.cdo.spi.common.branch.InternalCDOBranchManager.BranchLoader.BranchInfo;
import org.eclipse.emf.cdo.spi.common.branch.InternalCDOBranchManager.BranchLoader.SubBranchInfo;
import org.eclipse.net4j.util.container.Container;

/**
 * @author Eike Stepper
 */
public class CDOBranchImpl extends Container<CDOBranch> implements InternalCDOBranch
{
  public static final int ILLEGAL_BRANCH_ID = Integer.MIN_VALUE;

  private InternalCDOBranchManager branchManager;

  private int id;

  private String name;


  private InternalCDOBranch[] branches;

  public CDOBranchImpl(InternalCDOBranchManager branchManager, int id, String name)
  {
    this.branchManager = branchManager;
    this.id = id;
    this.name = name;
    activate();
  }

  public boolean isMainBranch()
  {
    return false;
  }

  public boolean isLocal()
  {
    return id < 0;
  }

  public InternalCDOBranchManager getBranchManager()
  {
    return branchManager;
  }

  public int getID()
  {
    return id;
  }

  public synchronized String getName()
  {
    if (name == null)
    {
      load();
    }

    return name;
  }

  public boolean isProxy()
  {
    return name == null ;
  }

  public String getPathName()
  {
    StringBuilder builder = new StringBuilder();
    computePathName(this, builder);
    return builder.toString();
  }

  private void computePathName(CDOBranch branch, StringBuilder builder)
  {
    builder.append(branch.getName());
  }

  public CDOBranchPoint[] getBasePath()
  {
    List<CDOBranchPoint> path = new ArrayList<CDOBranchPoint>();
    return path.toArray(new CDOBranchPoint[path.size()]);
  }


  public InternalCDOBranch[] getElements()
  {
    return getBranches();
  }

  public InternalCDOBranch[] getBranches(boolean loadOnDemand)
  {
    if (branches == null && loadOnDemand)
    {
      InternalCDOBranchManager branchManager = getBranchManager();
      SubBranchInfo[] infos = branchManager.getBranchLoader().loadSubBranches(id);
      branches = new InternalCDOBranch[infos.length];
      for (int i = 0; i < infos.length; i++)
      {
        SubBranchInfo info = infos[i];
        branches[i] = branchManager.getBranch(info.getID(), info.getName());
      }
    }

    return branches;
  }

  public synchronized InternalCDOBranch[] getBranches()
  {
    return getBranches(true);
  }

  public InternalCDOBranch getBranch(String path)
  {
    while (path.startsWith(PATH_SEPARATOR))
    {
      path = path.substring(1);
    }

    while (path.endsWith(PATH_SEPARATOR))
    {
      path = path.substring(0, path.length() - PATH_SEPARATOR.length());
    }

    int sep = path.indexOf(PATH_SEPARATOR);
    if (sep == -1)
    {
      return getChild(path);
    }

    String name = path.substring(0, sep);
    InternalCDOBranch child = getChild(name);
    if (child == null)
    {
      return null;
    }

    // Recurse
    String rest = path.substring(sep + 1);
    return child.getBranch(rest);
  }

  private InternalCDOBranch getChild(String name)
  {
    InternalCDOBranch[] branches = getBranches();
    for (InternalCDOBranch branch : branches)
    {
      if (name.equals(branch.getName()))
      {
        return branch;
      }
    }

    return null;
  }

  public BranchInfo getBranchInfo()
  {
    return new BranchInfo(getName());
  }

  public void setBranchInfo(String name)
  {
    this.name = name;
  }

  public void addChild(InternalCDOBranch branch)
  {
    synchronized (this)
    {
      if (branches == null)
      {
        branches = new InternalCDOBranch[] { branch };
      }
      else
      {
        InternalCDOBranch[] newBranches = new InternalCDOBranch[branches.length + 1];
        System.arraycopy(branches, 0, newBranches, 0, branches.length);
        newBranches[branches.length] = branch;
        branches = newBranches;
      }
    }

    fireElementAddedEvent(branch);
  }

  public int compareTo(CDOBranch o)
  {
    int otherID = o.getID();
    return id < otherID ? -1 : id == otherID ? 0 : 1;
  }

  @Override
  public int hashCode()
  {
    return id;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == this)
    {
      return true;
    }

    if (obj instanceof CDOBranch)
    {
      CDOBranch that = (CDOBranch)obj;
      return id == that.getID();
    }

    return false;
  }

  @Override
  public String toString()
  {
    if (isProxy())
    {
      return MessageFormat.format("Branch[id={0}, PROXY]", id); //$NON-NLS-1$
    }

    return MessageFormat.format("Branch[id={0}, name={1}]", id, name); //$NON-NLS-1$
  }

  private synchronized void load()
  {
    BranchInfo branchInfo = branchManager.getBranchLoader().loadBranch(id);
    name = branchInfo.getName();
  }

  /**
   * @author Eike Stepper
   */
  public static class Main extends CDOBranchImpl
  {
    private boolean local;

    public Main(InternalCDOBranchManager branchManager, boolean local, long timeStamp)
    {
      super(branchManager, MAIN_BRANCH_ID, MAIN_BRANCH_NAME);
      this.local = local;
    }

    @Override
    public boolean isMainBranch()
    {
      return true;
    }

    @Override
    public boolean isLocal()
    {
      return local;
    }
  }

}
