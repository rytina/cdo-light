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
package org.eclipse.emf.cdo.spi.common.revision;

import java.io.IOException;
import java.util.List;

import org.eclipse.emf.cdo.common.protocol.CDODataInput;
import org.eclipse.emf.cdo.common.protocol.CDODataOutput;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;

/**
 * @author Eike Stepper
 * @since 3.0
 */
public abstract class RevisionInfo
{
  private static final int NO_RESULT = 0;

  private static final int POINTER_RESULT = 1;

  private static final int DETACHED_RESULT = 2;

  private static final int NORMAL_RESULT = 3;

  private long id;


  private InternalCDORevision result;

  private SyntheticCDORevision synthetic;

  protected RevisionInfo(long id)
  {
    this.id = id;
  }

  protected RevisionInfo(CDODataInput in) throws IOException
  {
    id = in.readCDOID();
  }

  public abstract Type getType();

  public final long getID()
  {
    return id;
  }


  public InternalCDORevision getResult()
  {
    return result;
  }

  public void setResult(InternalCDORevision result)
  {
    this.result = result;
  }

  public SyntheticCDORevision getSynthetic()
  {
    return synthetic;
  }

  public void setSynthetic(SyntheticCDORevision synthetic)
  {
    this.synthetic = synthetic;
  }

  public abstract boolean isLoadNeeded();

  public void write(CDODataOutput out) throws IOException
  {
    out.writeByte(getType().ordinal());
    out.writeCDOID(getID());
  }

  public static RevisionInfo read(CDODataInput in) throws IOException
  {
    byte ordinal = in.readByte();
    Type type = Type.values()[ordinal];
    switch (type)
    {
    case AVAILABLE_NORMAL:
      return new Available.Normal(in);

    case AVAILABLE_POINTER:
      return new Available.Pointer(in);

    case AVAILABLE_DETACHED:
      return new Available.Detached(in);

    case MISSING:
      return new Missing(in);

    default:
      throw new IOException("Invalid revision info type: " + type);
    }
  }

  public void execute(InternalCDORevisionManager revisionManager, int referenceChunk)
  {
    SyntheticCDORevision[] synthetics = new SyntheticCDORevision[1];
    result = revisionManager.getRevision(getID(), referenceChunk, CDORevision.DEPTH_NONE, true,
        synthetics);
    synthetic = synthetics[0];
  }

  public void writeResult(CDODataOutput out, int referenceChunk) throws IOException
  {
    writeRevision(out, referenceChunk);
    writeResult(out, synthetic, referenceChunk);
  }

  public void readResult(CDODataInput in) throws IOException
  {
    readRevision(in);
    synthetic = (SyntheticCDORevision)readResult(in, getID());
  }

  public void processResult(InternalCDORevisionManager revisionManager, List<CDORevision> results,
      SyntheticCDORevision[] synthetics, int i)
  {
    if (result instanceof DetachedCDORevision)
    {
      results.add(null);
    }
    else
    {
      results.add(result);
    }

    if (result != null)
    {
      revisionManager.addRevision(result);
    }

    if (synthetic != null)
    {
      revisionManager.addRevision(synthetic);
      if (synthetic instanceof PointerCDORevision)
      {
        PointerCDORevision pointer = (PointerCDORevision)synthetic;
        long target = pointer.getTarget();
      }

      if (synthetics != null)
      {
        synthetics[i] = synthetic;
      }
    }
  }

  protected void writeRevision(CDODataOutput out, int referenceChunk) throws IOException
  {
    out.writeCDORevision(result, referenceChunk);
  }

  protected void readRevision(CDODataInput in) throws IOException
  {
    result = (InternalCDORevision)in.readCDORevision();
  }

  /**
   * @since 4.0
   */
  public static void writeResult(CDODataOutput out, InternalCDORevision revision, int referenceChunk)
      throws IOException
  {
    if (revision == null)
    {
      out.writeByte(NO_RESULT);
    }
    else if (revision instanceof PointerCDORevision)
    {
      PointerCDORevision pointer = (PointerCDORevision)revision;
      out.writeByte(POINTER_RESULT);
      out.writeCDOClassifierRef(pointer.getEClass());

       long target = pointer.getTarget();
        out.writeCDOID(target);
    }
    else if (revision instanceof DetachedCDORevision)
    {
      DetachedCDORevision detached = (DetachedCDORevision)revision;
      out.writeByte(DETACHED_RESULT);
      out.writeCDOClassifierRef(detached.getEClass());
    }
    else
    {
      out.writeByte(NORMAL_RESULT);
      out.writeCDORevision(revision, referenceChunk);
    }
  }

  /**
   * @since 4.0
   */
  public static InternalCDORevision readResult(CDODataInput in, long id) throws IOException
  {
    byte type = in.readByte();
    switch (type)
    {
    case NO_RESULT:
      return null;

    case POINTER_RESULT:
    {
      EClassifier classifier = in.readCDOClassifierRefAndResolve();
      long revised = in.readLong();
      InternalCDORevision target = readResult(in, id);
      return new PointerCDORevision((EClass)classifier, id, target.getID());
    }

    case DETACHED_RESULT:
    {
      EClassifier classifier = in.readCDOClassifierRefAndResolve();
      long timeStamp = in.readLong();
      long revised = in.readLong();
      int version = in.readInt();
      return new DetachedCDORevision((EClass)classifier, id);
    }

    case NORMAL_RESULT:
      return (InternalCDORevision)in.readCDORevision();

    default:
      throw new IllegalStateException("Invalid synthetic type: " + type);
    }
  }

  protected void doWriteResult(CDODataOutput out, InternalCDORevision revision, int referenceChunk) throws IOException
  {
    writeResult(out, revision, referenceChunk);
  }

  protected InternalCDORevision doReadResult(CDODataInput in) throws IOException
  {
    return readResult(in, id);
  }

  /**
   * @author Eike Stepper
   * @since 3.0
   */
  public static enum Type
  {
    AVAILABLE_NORMAL, AVAILABLE_POINTER, AVAILABLE_DETACHED, MISSING
  }

  /**
   * @author Eike Stepper
   * @since 3.0
   */
  public static abstract class Available extends RevisionInfo
  {

    protected Available(long id)
    {
      super(id);
    }

    protected Available(CDODataInput in) throws IOException
    {
      super(in);
    }


    @Override
    public boolean isLoadNeeded()
    {
      return false;
    }

    @Override
    public void write(CDODataOutput out) throws IOException
    {
      super.write(out);
    }

    @Override
    protected void writeRevision(CDODataOutput out, int referenceChunk) throws IOException
    {
      InternalCDORevision result = getResult();
      if (result != null)
      {
        // Use available
        out.writeBoolean(true);
      }
      else
      {
        out.writeBoolean(false);
        super.writeRevision(out, referenceChunk);
      }
    }

    @Override
    protected void readRevision(CDODataInput in) throws IOException
    {
      boolean useAvailable = in.readBoolean();
      if (useAvailable)
      {
      }
      else
      {
        super.readRevision(in);
      }
    }

    /**
     * @author Eike Stepper
     * @since 3.0
     */
    public static class Normal extends Available
    {
      public Normal(long id)
      {
        super(id);
      }
      
      public Normal(CDODataInput in) throws IOException
      {
        super(in);
      }
      


      @Override
      public Type getType()
      {
        return Type.AVAILABLE_NORMAL;
      }

      @Override
      public InternalCDORevision getResult()
      {

        return super.getResult();
      }

      @Override
      public void processResult(InternalCDORevisionManager revisionManager, List<CDORevision> results,
          SyntheticCDORevision[] synthetics, int i)
      {
        if (!isLoadNeeded())
        {
        }

        super.processResult(revisionManager, results, synthetics, i);
      }
    }

    /**
     * @author Eike Stepper
     * @since 3.0
     */
    public static class Pointer extends Available
    {

      private boolean hasTarget;
	private InternalCDORevision target;

      public Pointer(long id, InternalCDORevision target)
      {
        super(id);
        this.target = target;
      }

      private Pointer(CDODataInput in) throws IOException
      {
        super(in);
        if (in.readBoolean())
        {
          hasTarget = in.readBoolean();
        }
      }

      @Override
      public Type getType()
      {
        return Type.AVAILABLE_POINTER;
      }

      public boolean hasTarget()
      {
        return hasTarget;
      }

      @Override
      public boolean isLoadNeeded()
      {
    	  return false;
      }

      @Override
      public void write(CDODataOutput out) throws IOException
      {
        super.write(out);
        if (false)
        {
          out.writeBoolean(true);
          out.writeBoolean(hasTarget);
        }
        else
        {
          out.writeBoolean(false);
        }
      }

      @Override
      public void processResult(InternalCDORevisionManager revisionManager, List<CDORevision> results,
          SyntheticCDORevision[] synthetics, int i)
      {
        if (!isLoadNeeded())
        {
          if (target instanceof InternalCDORevision)
          {
            setResult((InternalCDORevision)target);
          }

          setSynthetic((PointerCDORevision)null);
        }

        super.processResult(revisionManager, results, synthetics, i);
      }
    }

    /**
     * @author Eike Stepper
     * @since 3.0
     */
    public static class Detached extends Available
    {
      public Detached(long id)
      {
        super(id);
      }

      private Detached(CDODataInput in) throws IOException
      {
        super(in);
      }

      @Override
      public Type getType()
      {
        return Type.AVAILABLE_DETACHED;
      }

      @Override
      public void processResult(InternalCDORevisionManager revisionManager, List<CDORevision> results,
          SyntheticCDORevision[] synthetics, int i)
      {
        if (!isLoadNeeded())
        {
          setSynthetic((DetachedCDORevision)null);
        }

        super.processResult(revisionManager, results, synthetics, i);
      }
    }
  }

  /**
   * @author Eike Stepper
   * @since 3.0
   */
  public static class Missing extends RevisionInfo
  {
    public Missing(long id)
    {
      super(id);
    }

    private Missing(CDODataInput in) throws IOException
    {
      super(in);
    }

    @Override
    public Type getType()
    {
      return Type.MISSING;
    }

    @Override
    public boolean isLoadNeeded()
    {
      return true;
    }
  }
}
