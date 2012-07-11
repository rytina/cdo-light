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
package org.eclipse.emf.cdo.internal.common.protocol;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.common.commit.CDOChangeSetData;
import org.eclipse.emf.cdo.common.commit.CDOCommitData;
import org.eclipse.emf.cdo.common.commit.CDOCommitInfo;
import org.eclipse.emf.cdo.common.id.CDOIDReference;
import org.eclipse.emf.cdo.common.model.CDOClassifierRef;
import org.eclipse.emf.cdo.common.model.CDOModelUtil;
import org.eclipse.emf.cdo.common.model.CDOPackageInfo;
import org.eclipse.emf.cdo.common.model.CDOPackageRegistry;
import org.eclipse.emf.cdo.common.model.CDOPackageUnit;
import org.eclipse.emf.cdo.common.model.CDOType;
import org.eclipse.emf.cdo.common.protocol.CDODataOutput;
import org.eclipse.emf.cdo.common.revision.CDOIDAndBranch;
import org.eclipse.emf.cdo.common.revision.CDOIDAndVersion;
import org.eclipse.emf.cdo.common.revision.CDOList;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.CDORevisionKey;
import org.eclipse.emf.cdo.common.revision.CDORevisionUtil;
import org.eclipse.emf.cdo.common.revision.delta.CDOFeatureDelta;
import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
import org.eclipse.emf.cdo.internal.common.bundle.OM;
import org.eclipse.emf.cdo.internal.common.messages.Messages;
import org.eclipse.emf.cdo.internal.common.model.CDOTypeImpl;
import org.eclipse.emf.cdo.internal.common.revision.delta.CDOFeatureDeltaImpl;
import org.eclipse.emf.cdo.internal.common.revision.delta.CDORevisionDeltaImpl;
import org.eclipse.emf.cdo.spi.common.model.InternalCDOPackageInfo;
import org.eclipse.emf.cdo.spi.common.model.InternalCDOPackageUnit;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevision;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.FeatureMap.Entry;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
import org.eclipse.net4j.util.concurrent.IRWLockManager.LockType;
import org.eclipse.net4j.util.io.ExtendedDataOutput;
import org.eclipse.net4j.util.io.StringIO;
import org.eclipse.net4j.util.om.trace.ContextTracer;

/**
 * @author Eike Stepper
 */
public abstract class CDODataOutputImpl extends ExtendedDataOutput.Delegating implements CDODataOutput
{
  private static final ContextTracer TRACER = new ContextTracer(OM.DEBUG_PROTOCOL, CDODataOutputImpl.class);

  public CDODataOutputImpl(ExtendedDataOutput delegate)
  {
    super(delegate);
  }

  public void writeCDOPackageUnit(CDOPackageUnit packageUnit, boolean withPackages) throws IOException
  {
    ((InternalCDOPackageUnit)packageUnit).write(this, withPackages);
  }

  public void writeCDOPackageUnits(CDOPackageUnit... packageUnits) throws IOException
  {
    int size = packageUnits.length;
    writeInt(size);
    if (TRACER.isEnabled())
    {
      TRACER.format("Writing {0} package units", size); //$NON-NLS-1$
    }

    for (CDOPackageUnit packageUnit : packageUnits)
    {
      writeCDOPackageUnit(packageUnit, false);
    }
  }

  public void writeCDOPackageUnitType(CDOPackageUnit.Type type) throws IOException
  {
    writeByte(type.ordinal());
  }

  public void writeCDOPackageInfo(CDOPackageInfo packageInfo) throws IOException
  {
    ((InternalCDOPackageInfo)packageInfo).write(this);
  }

  public void writeCDOClassifierRef(CDOClassifierRef eClassifierRef) throws IOException
  {
    eClassifierRef.write(this);
  }

  public void writeCDOClassifierRef(EClassifier eClassifier) throws IOException
  {
    writeCDOClassifierRef(new CDOClassifierRef(eClassifier));
  }

  public void writeCDOPackageURI(String uri) throws IOException
  {
    getPackageURICompressor().write(this, uri);
  }

  public void writeCDOType(CDOType cdoType) throws IOException
  {
    ((CDOTypeImpl)cdoType).write(this);
  }


  public void writeCDOChangeSetData(CDOChangeSetData changeSetData) throws IOException
  {
    Collection<CDORevision> newObjects = changeSetData.getNewObjects();
    writeInt(newObjects.size());
    for (CDORevision data : newObjects)
    {
        writeCDORevision((CDORevision)data, CDORevision.UNCHUNKED);
    }

    Collection<CDORevisionDelta> changedObjects = changeSetData.getChangedObjects();
    writeInt(changedObjects.size());
    for (CDORevisionKey data : changedObjects)
    {
        writeCDORevisionDelta((CDORevisionDelta)data);
    }

    Collection<Long> detachedObjects = changeSetData.getDetachedObjects();
    writeInt(detachedObjects.size());
    for (Long id : detachedObjects)
    {
      writeCDOID(id);
    }
  }

  public void writeCDOCommitData(CDOCommitData commitData) throws IOException
  {
    Collection<CDOPackageUnit> newPackageUnits = commitData.getNewPackageUnits();
    writeInt(newPackageUnits.size());
    for (CDOPackageUnit data : newPackageUnits)
    {
      writeCDOPackageUnit(data, false);
    }

    writeCDOChangeSetData(commitData);
  }

  public void writeCDOCommitInfo(CDOCommitInfo commitInfo) throws IOException
  {
      writeBoolean(true);
      writeString(commitInfo.getUserID());
      writeString(commitInfo.getComment());
      writeCDOCommitData(commitInfo);
  }

  public void writeCDOID(long id) throws IOException
  {
    writeLong(id);
  }

  public void writeCDOIDReference(CDOIDReference idReference) throws IOException
  {
    idReference.write(this);
  }




  public void writeCDORevision(CDORevision revision, int referenceChunk) throws IOException
  {
    if (revision != null)
    {
      writeBoolean(true);
      ((InternalCDORevision)revision).write(this, referenceChunk);
    }
    else
    {
      writeBoolean(false);
    }
  }


  public void writeCDOList(EClass owner, EStructuralFeature feature, CDOList list, int referenceChunk)
      throws IOException
  {
    // TODO Simon: Could most of this stuff be moved into the list?
    // (only if protected methods of this class don't need to become public)
    int size = list == null ? 0 : list.size();
    if (size > 0)
    {
      // Need to adjust the referenceChunk in case where we do not have enough value in the list.
      // Even if the referenceChunk is specified, a provider of data could have override that value.
      int sizeToLook = referenceChunk == CDORevision.UNCHUNKED ? size : Math.min(referenceChunk, size);
      for (int i = 0; i < sizeToLook; i++)
      {
        Object element = list.get(i, false);
        if (element == CDORevisionUtil.UNINITIALIZED)
        {
          referenceChunk = i;
          break;
        }
      }
    }

    if (referenceChunk != CDORevision.UNCHUNKED && referenceChunk < size)
    {
      // This happens only on server-side
      if (TRACER.isEnabled())
      {
        TRACER.format("Writing feature {0}: size={1}, referenceChunk={2}", feature.getName(), size, referenceChunk); //$NON-NLS-1$
      }

      writeInt(-size);
      writeInt(referenceChunk);
      size = referenceChunk;
    }
    else
    {
      if (TRACER.isEnabled())
      {
        TRACER.format("Writing feature {0}: size={1}", feature.getName(), size); //$NON-NLS-1$
      }

      writeInt(size);
    }

    boolean isFeatureMap = FeatureMapUtil.isFeatureMap(feature);
    for (int j = 0; j < size; j++)
    {
      Object value = list.get(j, false);
      EStructuralFeature innerFeature = feature; // Prepare for possible feature map
      if (isFeatureMap)
      {
        Entry entry = (FeatureMap.Entry)value;
        innerFeature = entry.getEStructuralFeature();
        value = entry.getValue();

        int featureID = owner.getFeatureID(innerFeature);
        writeInt(featureID);
      }


      if (TRACER.isEnabled())
      {
        TRACER.trace("    " + value); //$NON-NLS-1$
      }

      writeCDOFeatureValue(innerFeature, value);
    }
  }

  public void writeCDOFeatureValue(EStructuralFeature feature, Object value) throws IOException
  {
    CDOType type = CDOModelUtil.getType(feature);
    type.writeValue(this, value);
  }

  public void writeCDORevisionDelta(CDORevisionDelta revisionDelta) throws IOException
  {
    ((CDORevisionDeltaImpl)revisionDelta).write(this);
  }

  public void writeCDOFeatureDelta(EClass owner, CDOFeatureDelta featureDelta) throws IOException
  {
    ((CDOFeatureDeltaImpl)featureDelta).write(this, owner);
  }

  public void writeCDORevisionOrPrimitive(Object value) throws IOException
  {
    if (value == null)
    {
      value = 0;
    }
    else if (value instanceof EObject)
    {
    	//rytina todo
    	throw new RuntimeException("implement it!");
//      value = getIDProvider().provideCDOID(value);
    }
    else if (value instanceof CDORevision)
    {
      value = ((CDORevision)value).getID();
    }

    CDOType type = null;
    if (value instanceof Long) //rytina: what is when value is not a reference?
    {
      type = CDOType.OBJECT;
    }
    else
    {
      type = CDOModelUtil.getPrimitiveType(value.getClass());
      if (type == null)
      {
        throw new IllegalArgumentException(MessageFormat.format(
            Messages.getString("CDODataOutputImpl.6"), value.getClass())); //$NON-NLS-1$
      }
    }

    type.writeValue(this, value);
  }

  public void writeCDORevisionOrPrimitiveOrClassifier(Object value) throws IOException
  {
    if (value instanceof EClassifier)
    {
      writeBoolean(true);
      writeCDOClassifierRef((EClass)value);
    }
    else
    {
      writeBoolean(false);
      writeCDORevisionOrPrimitive(value);
    }
  }

  public void writeCDOLockType(LockType lockType) throws IOException
  {
    writeBoolean(lockType == LockType.WRITE ? true : false);
  }

  public CDOPackageRegistry getPackageRegistry()
  {
    return null;
  }

  protected StringIO getPackageURICompressor()
  {
    return StringIO.DIRECT;
  }
}
