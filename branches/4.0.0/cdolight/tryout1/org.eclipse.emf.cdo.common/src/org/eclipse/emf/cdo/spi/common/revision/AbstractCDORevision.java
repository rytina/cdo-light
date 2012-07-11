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
 *    Simon McDuff - bug 212958
 *    Simon McDuff - bug 213402
 */
package org.eclipse.emf.cdo.spi.common.revision;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.common.model.CDOClassInfo;
import org.eclipse.emf.cdo.common.model.CDOModelUtil;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.CDORevisionData;
import org.eclipse.emf.cdo.common.util.CDOCommonUtil;
import org.eclipse.emf.cdo.internal.common.messages.Messages;

import org.eclipse.net4j.util.ObjectUtil;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.text.MessageFormat;

/**
 * @author Eike Stepper
 * @since 2.0
 */
public abstract class AbstractCDORevision implements InternalCDORevision
{
  private CDOClassInfo classInfo;

  /**
   * @since 3.0
   */
  protected AbstractCDORevision(EClass eClass)
  {
    if (eClass != null)
    {
      if (eClass.isAbstract())
      {
        throw new IllegalArgumentException(MessageFormat.format(Messages.getString("AbstractCDORevision.0"), eClass)); //$NON-NLS-1$
      }

      classInfo = CDOModelUtil.getClassInfo(eClass);
    }
  }

  /**
   * @since 3.0
   */
  public CDOClassInfo getClassInfo()
  {
    return classInfo;
  }

  public EClass getEClass()
  {
    CDOClassInfo classInfo = getClassInfo();
    if (classInfo != null)
    {
      return classInfo.getEClass();
    }

    return null;
  }

  public boolean isResourceNode()
  {
    return getClassInfo().isResourceNode();
  }

  public boolean isResourceFolder()
  {
    return getClassInfo().isResourceFolder();
  }

  public boolean isResource()
  {
    return getClassInfo().isResource();
  }

  public CDORevisionData data()
  {
    return this;
  }

  public CDORevision revision()
  {
    return this;
  }



  /**
   * @since 3.0
   */
  public void adjustForCommit()
  {
  }

  @Override
  public int hashCode()
  {
    return ((int) getID() % Integer.MAX_VALUE);
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == this)
    {
      return true;
    }

    if (obj instanceof CDORevision)
    {
      return getID() == ((CDORevision)obj).getID();
    }

    return false;
  }

  @Override
  public String toString()
  {
    EClass eClass = getEClass();
    String name = eClass == null ? "Revision" : eClass.getName();

    return name + "@" + getID();
  }

  /**
   * @since 3.0
   */
  protected void setClassInfo(CDOClassInfo classInfo)
  {
    this.classInfo = classInfo;
  }

  /**
   * @since 3.0
   */
  protected EStructuralFeature[] getAllPersistentFeatures()
  {
    return classInfo.getAllPersistentFeatures();
  }

  /**
   * @since 3.0
   */
  protected int getFeatureIndex(EStructuralFeature feature)
  {
    return classInfo.getFeatureIndex(feature);
  }
}
