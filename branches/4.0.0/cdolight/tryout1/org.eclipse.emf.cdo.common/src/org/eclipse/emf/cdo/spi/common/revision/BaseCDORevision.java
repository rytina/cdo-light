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
import org.eclipse.emf.cdo.common.id.CDOIDUtil;
import org.eclipse.emf.cdo.common.model.CDOClassInfo;
import org.eclipse.emf.cdo.common.model.CDOClassifierRef;
import org.eclipse.emf.cdo.common.model.CDOModelUtil;
import org.eclipse.emf.cdo.common.model.CDOType;
import org.eclipse.emf.cdo.common.protocol.CDODataInput;
import org.eclipse.emf.cdo.common.protocol.CDODataOutput;
import org.eclipse.emf.cdo.common.revision.CDOList;
import org.eclipse.emf.cdo.common.revision.CDOListFactory;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.CDORevisionData;
import org.eclipse.emf.cdo.common.revision.delta.CDOContainerFeatureDelta;
import org.eclipse.emf.cdo.common.revision.delta.CDOFeatureDelta;
import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
import org.eclipse.emf.cdo.common.util.CDOCommonUtil;
import org.eclipse.emf.cdo.internal.common.bundle.OM;
import org.eclipse.emf.cdo.internal.common.messages.Messages;
import org.eclipse.emf.cdo.internal.common.revision.delta.CDORevisionDeltaImpl;
import org.eclipse.emf.cdo.spi.common.branch.CDOBranchUtil;

import org.eclipse.net4j.util.om.trace.ContextTracer;
import org.eclipse.net4j.util.om.trace.PerfTracer;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject.EStore;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.FeatureMap.Entry;
import org.eclipse.emf.ecore.util.FeatureMapUtil;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

/**
 * @author Eike Stepper
 * @since 3.0
 */
public abstract class BaseCDORevision extends AbstractCDORevision
{
  private static final ContextTracer TRACER = new ContextTracer(OM.DEBUG_REVISION, BaseCDORevision.class);

  private static final PerfTracer READING = new PerfTracer(OM.PERF_REVISION_READING, BaseCDORevision.class);

  private static final PerfTracer WRITING = new PerfTracer(OM.PERF_REVISION_WRITING, BaseCDORevision.class);

  private static final byte UNSET = 0;

  private static final byte SET_NULL = 1;

  private static final byte SET_NOT_NULL = 2;

  private long id;

  private long resourceID;

  private long containerID;

  private int containingFeatureID;

  /**
   * @since 3.0
   */
  public BaseCDORevision(EClass eClass)
  {
    super(eClass);
    if (eClass != null)
    {
      resourceID = 0;
      containerID = 0;
      containingFeatureID = 0;
      initValues(getAllPersistentFeatures());
    }
  }

  protected BaseCDORevision(BaseCDORevision source)
  {
    super(source.getEClass());
    id = source.id;
    resourceID = source.resourceID;
    containerID = source.containerID;
    containingFeatureID = source.containingFeatureID;
  }

  /**
   * @since 3.0
   */
  public void read(CDODataInput in) throws IOException
  {
    if (READING.isEnabled())
    {
      READING.start(this);
    }

    readSystemValues(in);
    readValues(in);

    if (READING.isEnabled())
    {
      READING.stop(this);
    }
  }

  /**
   * @since 4.0
   */
  protected void readSystemValues(CDODataInput in) throws IOException
  {
    EClassifier classifier = in.readCDOClassifierRefAndResolve();
    CDOClassInfo classInfo = CDOModelUtil.getClassInfo((EClass)classifier);
    setClassInfo(classInfo);

    id = in.readCDOID();
    resourceID = in.readCDOID();
    containerID = in.readCDOID();
    containingFeatureID = in.readInt();

  }

  /**
   * @since 4.0
   */
  public void write(CDODataOutput out, int referenceChunk) throws IOException
  {
    if (WRITING.isEnabled())
    {
      WRITING.start(this);
    }

    writeSystemValues(out);
    writeValues(out, referenceChunk);

    if (WRITING.isEnabled())
    {
      WRITING.stop(this);
    }
  }

  /**
   * @since 4.0
   */
  protected void writeSystemValues(CDODataOutput out) throws IOException
  {
    EClass eClass = getEClass();
    CDOClassifierRef classRef = new CDOClassifierRef(eClass);

    out.writeCDOClassifierRef(classRef);
    out.writeCDOID(id);

    out.writeCDOID(resourceID);
    out.writeCDOID(containerID);
    out.writeInt(containingFeatureID);
  }


  public long getID()
  {
    return id;
  }

  public void setID(long id)
  {
    if (CDOIDUtil.isNull(id))
    {
      throw new IllegalArgumentException(Messages.getString("AbstractCDORevision.1")); //$NON-NLS-1$
    }

    if (TRACER.isEnabled())
    {
      TRACER.format("Setting ID: {0}", id);
    }

    this.id = id;
  }



  public InternalCDORevisionDelta compare(CDORevision origin)
  {
    return new CDORevisionDeltaImpl(origin, this);
  }

  public void merge(CDORevisionDelta delta)
  {
    CDORevisionMerger applier = new CDORevisionMerger();
    applier.merge(this, delta);
  }

  public long getResourceID()
  {
    return resourceID;
  }

  public void setResourceID(long resourceID)
  {
    if (TRACER.isEnabled())
    {
      TRACER.format("Setting resourceID {0}: {1}", this, resourceID);
    }

    this.resourceID = resourceID;
  }

  public long getContainerID()
  {
    return containerID;
  }

  public void setContainerID(long containerID)
  {
    if (TRACER.isEnabled())
    {
      TRACER.format("Setting containerID {0}: {1}", this, containerID);
    }

    this.containerID = containerID;
  }

  public int getContainingFeatureID()
  {
    return containingFeatureID;
  }

  public void setContainingFeatureID(int containingFeatureID)
  {
    if (TRACER.isEnabled())
    {
      TRACER.format("Setting containingFeatureID {0}: {1}", this, containingFeatureID);
    }

    this.containingFeatureID = containingFeatureID;
  }

  public int hashCode(EStructuralFeature feature)
  {
    return getValue(feature).hashCode();
  }

  public Object get(EStructuralFeature feature, int index)
  {
    if (feature.isMany() && index != EStore.NO_INDEX)
    {
      return getList(feature).get(index);
    }

    return getValue(feature);
  }

  public boolean contains(EStructuralFeature feature, Object value)
  {
    return getList(feature).contains(value);
  }

  public int indexOf(EStructuralFeature feature, Object value)
  {
    return getList(feature).indexOf(value);
  }

  public boolean isEmpty(EStructuralFeature feature)
  {
    return getList(feature).isEmpty();
  }

  public int lastIndexOf(EStructuralFeature feature, Object value)
  {
    return getList(feature).lastIndexOf(value);
  }

  public int size(EStructuralFeature feature)
  {
    return getList(feature).size();
  }

  public Object[] toArray(EStructuralFeature feature)
  {
    if (!feature.isMany())
    {
      throw new IllegalStateException("!feature.isMany()");
    }

    return getList(feature).toArray();
  }

  public <T> T[] toArray(EStructuralFeature feature, T[] array)
  {
    if (!feature.isMany())
    {
      throw new IllegalStateException("!feature.isMany()");
    }

    return getList(feature).toArray(array);
  }

  public void add(EStructuralFeature feature, int index, Object value)
  {
    getList(feature).add(index, value);
  }

  public void clear(EStructuralFeature feature)
  {
    setValue(feature, null);
  }

  public Object move(EStructuralFeature feature, int targetIndex, int sourceIndex)
  {
    return getList(feature).move(targetIndex, sourceIndex);
  }

  public Object remove(EStructuralFeature feature, int index)
  {
    return getList(feature).remove(index);
  }

  public Object set(EStructuralFeature feature, int index, Object value)
  {
    if (feature.isMany())
    {
      return getList(feature).set(index, value);
    }

    return setValue(feature, value);
  }

  public void unset(EStructuralFeature feature)
  {
    setValue(feature, null);
  }



  public Object getValue(EStructuralFeature feature)
  {
    int featureIndex = getFeatureIndex(feature);
    return getValue(featureIndex);
  }

  public Object setValue(EStructuralFeature feature, Object value)
  {
    int featureIndex = getFeatureIndex(feature);

    try
    {
      Object old = getValue(featureIndex);
      setValue(featureIndex, value);
      return old;
    }
    catch (ArrayIndexOutOfBoundsException ex)
    {
      throw new IllegalArgumentException(MessageFormat.format(Messages.getString("AbstractCDORevision.20"), feature,
          getClassInfo()), ex);
    }
  }

  public CDOList getList(EStructuralFeature feature)
  {
    return getList(feature, 0);
  }

  public CDOList getList(EStructuralFeature feature, int size)
  {
    int featureIndex = getFeatureIndex(feature);
    CDOList list = (CDOList)getValue(featureIndex);
    if (list == null && size != -1)
    {
      list = CDOListFactory.DEFAULT.createList(size, 0, 0);
      setValue(featureIndex, list);
    }

    return list;
  }

  public void setList(EStructuralFeature feature, InternalCDOList list)
  {
    int featureIndex = getFeatureIndex(feature);
    setValue(featureIndex, list);
  }

  protected abstract void initValues(EStructuralFeature[] allPersistentFeatures);

  protected abstract Object getValue(int featureIndex);

  protected abstract void setValue(int featureIndex, Object value);

  private CDOList getValueAsList(int i)
  {
    return (CDOList)getValue(i);
  }

  private void writeValues(CDODataOutput out, int referenceChunk) throws IOException
  {
    EClass owner = getEClass();
    EStructuralFeature[] features = getAllPersistentFeatures();
    for (int i = 0; i < features.length; i++)
    {
      EStructuralFeature feature = features[i];
      Object value = getValue(i);
      if (value == null)
      {
        // Feature is NOT set
        out.writeByte(UNSET);
        continue;
      }

      // Feature IS set
      if (value == CDORevisionData.NIL)
      {
        // Feature IS null
        out.writeByte(SET_NULL);
        continue;
      }

      // Feature is NOT null
      out.writeByte(SET_NOT_NULL);
      if (feature.isMany())
      {
        CDOList list = (CDOList)value;
        out.writeCDOList(owner, feature, list, referenceChunk);
      }
      else
      {
        checkNoFeatureMap(feature);

        if (TRACER.isEnabled())
        {
          TRACER.format("Writing feature {0}: {1}", feature.getName(), value);
        }

        out.writeCDOFeatureValue(feature, value);
      }
    }
  }

  private void readValues(CDODataInput in) throws IOException
  {
    EClass owner = getEClass();
    EStructuralFeature[] features = getAllPersistentFeatures();
    initValues(features);
    for (int i = 0; i < features.length; i++)
    {
      Object value;
      EStructuralFeature feature = features[i];
      byte unsetState = in.readByte();
      switch (unsetState)
      {
      case UNSET:
        continue;

      case SET_NULL:
        setValue(i, CDORevisionData.NIL);
        continue;
      }

      if (feature.isMany())
      {
        value = in.readCDOList(owner, feature);
      }
      else
      {
        value = in.readCDOFeatureValue(feature);
        if (TRACER.isEnabled())
        {
          TRACER.format("Read feature {0}: {1}", feature.getName(), value);
        }
      }

      setValue(i, value);
    }
  }

  public static void checkNoFeatureMap(EStructuralFeature feature)
  {
    if (FeatureMapUtil.isFeatureMap(feature))
    {
      throw new UnsupportedOperationException("Single-valued feature maps not yet handled");
    }
  }

}
