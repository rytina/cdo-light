/**
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 *    Caspar De Groot - bug 341081
 */
package org.eclipse.emf.cdo.spi.common.revision;

import java.io.IOException;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.model.CDOClassInfo;
import org.eclipse.emf.cdo.common.protocol.CDODataInput;
import org.eclipse.emf.cdo.common.protocol.CDODataOutput;
import org.eclipse.emf.cdo.common.revision.CDOList;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.CDORevisionData;
import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * @author Eike Stepper
 * @since 3.0
 */
public abstract class DelegatingCDORevision implements InternalCDORevision
{
  public DelegatingCDORevision()
  {
  }

  public abstract InternalCDORevision getDelegate();



  public long getID()
  {
    return getDelegate().getID();
  }

  public CDORevision revision()
  {
    return getDelegate().revision();
  }

  public long getResourceID()
  {
    return getDelegate().getResourceID();
  }

  public long getContainerID()
  {
    return getDelegate().getContainerID();
  }

  public int getContainingFeatureID()
  {
    return getDelegate().getContainingFeatureID();
  }

  public Object get(EStructuralFeature feature, int index)
  {
    return getDelegate().get(feature, index);
  }

  public EClass getEClass()
  {
    return getDelegate().getEClass();
  }


  public int size(EStructuralFeature feature)
  {
    return getDelegate().size(feature);
  }


  public boolean isEmpty(EStructuralFeature feature)
  {
    return getDelegate().isEmpty(feature);
  }



  /**
   * @since 4.0
   */
  public InternalCDORevision copy()
  {
    return null;
  }

  public CDOClassInfo getClassInfo()
  {
    return getDelegate().getClassInfo();
  }

  public void setID(long id)
  {
    getDelegate().setID(id);
  }

  public boolean contains(EStructuralFeature feature, Object value)
  {
    return getDelegate().contains(feature, value);
  }

  public boolean isResourceNode()
  {
    return getDelegate().isResourceNode();
  }


  public boolean isResourceFolder()
  {
    return getDelegate().isResourceFolder();
  }

  public int indexOf(EStructuralFeature feature, Object value)
  {
    return getDelegate().indexOf(feature, value);
  }

  public boolean isResource()
  {
    return getDelegate().isResource();
  }


  public void adjustForCommit()
  {
    getDelegate().adjustForCommit();
  }

  public CDORevisionData data()
  {
    return getDelegate().data();
  }

  public int lastIndexOf(EStructuralFeature feature, Object value)
  {
    return getDelegate().lastIndexOf(feature, value);
  }


  public InternalCDORevisionDelta compare(CDORevision origin)
  {
    return getDelegate().compare(origin);
  }

  public void setResourceID(long resourceID)
  {
    getDelegate().setResourceID(resourceID);
  }

  public void merge(CDORevisionDelta delta)
  {
    getDelegate().merge(delta);
  }

  public <T> T[] toArray(EStructuralFeature feature, T[] array)
  {
    return getDelegate().toArray(feature, array);
  }

  public void setContainerID(long containerID)
  {
    getDelegate().setContainerID(containerID);
  }

  public void setContainingFeatureID(int containingFeatureID)
  {
    getDelegate().setContainingFeatureID(containingFeatureID);
  }

  public Object[] toArray(EStructuralFeature feature)
  {
    return getDelegate().toArray(feature);
  }

  public void add(EStructuralFeature feature, int index, Object value)
  {
    getDelegate().add(feature, index, value);
  }

  public int hashCode(EStructuralFeature feature)
  {
    return getDelegate().hashCode(feature);
  }

  public void clear(EStructuralFeature feature)
  {
    getDelegate().clear(feature);
  }

  public Object move(EStructuralFeature feature, int targetIndex, int sourceIndex)
  {
    return getDelegate().move(feature, targetIndex, sourceIndex);
  }

  public Object remove(EStructuralFeature feature, int index)
  {
    return getDelegate().remove(feature, index);
  }

  public Object set(EStructuralFeature feature, int index, Object value)
  {
    return getDelegate().set(feature, index, value);
  }

  public void unset(EStructuralFeature feature)
  {
    getDelegate().unset(feature);
  }

  public Object getValue(EStructuralFeature feature)
  {
    return getDelegate().getValue(feature);
  }

  public Object setValue(EStructuralFeature feature, Object value)
  {
    return getDelegate().setValue(feature, value);
  }

  public void setList(EStructuralFeature feature, InternalCDOList list)
  {
    getDelegate().setList(feature, list);
  }

  public CDOList getList(EStructuralFeature feature)
  {
    return getDelegate().getList(feature);
  }

  public CDOList getList(EStructuralFeature feature, int size)
  {
    return getDelegate().getList(feature, size);
  }

  public void read(CDODataInput in) throws IOException
  {
    getDelegate().read(in);
  }

  public void write(CDODataOutput out, int referenceChunk) throws IOException
  {
    getDelegate().write(out, referenceChunk);
  }


  /**
   * @since 4.0
   */
  public void freeze()
  {
    getDelegate().freeze();
  }
}
