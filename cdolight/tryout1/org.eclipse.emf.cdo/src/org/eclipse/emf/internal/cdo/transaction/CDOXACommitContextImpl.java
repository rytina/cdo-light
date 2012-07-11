/***************************************************************************
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Simon McDuff - initial API and implementation
 **************************************************************************/
package org.eclipse.emf.internal.cdo.transaction;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.cdo.CDOObject;
import org.eclipse.emf.cdo.common.commit.CDOCommitData;
import org.eclipse.emf.cdo.common.lob.CDOLob;
import org.eclipse.emf.cdo.common.model.CDOPackageUnit;
import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
import org.eclipse.emf.cdo.util.CDOUtil;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.spi.cdo.CDOSessionProtocol.CommitTransactionResult;
import org.eclipse.emf.spi.cdo.InternalCDOObject;
import org.eclipse.emf.spi.cdo.InternalCDOTransaction;
import org.eclipse.emf.spi.cdo.InternalCDOTransaction.InternalCDOCommitContext;
import org.eclipse.emf.spi.cdo.InternalCDOXATransaction;
import org.eclipse.emf.spi.cdo.InternalCDOXATransaction.InternalCDOXACommitContext;

/**
 * @author Simon McDuff
 * @since 2.0
 */
public class CDOXACommitContextImpl implements InternalCDOXACommitContext
{
  private InternalCDOXATransaction transactionManager;

  private IProgressMonitor progressMonitor;

  private CDOXAState state;

  private CommitTransactionResult result;

  private InternalCDOCommitContext delegateCommitContext;

  private Map<Long, InternalCDOTransaction> requestedIDs = new HashMap<Long, InternalCDOTransaction>();

  public CDOXACommitContextImpl(InternalCDOXATransaction manager, InternalCDOCommitContext commitContext)
  {
    transactionManager = manager;
    delegateCommitContext = commitContext;
  }

  public InternalCDOXATransaction getTransactionManager()
  {
    return transactionManager;
  }

  public void setProgressMonitor(IProgressMonitor progressMonitor)
  {
    this.progressMonitor = progressMonitor;
  }

  public CDOXAState getState()
  {
    return state;
  }

  public void setState(CDOXAState state)
  {
    this.state = state;
  }

  public CommitTransactionResult getResult()
  {
    return result;
  }

  public void setResult(CommitTransactionResult result)
  {
    this.result = result;
  }

  public InternalCDOTransaction getTransaction()
  {
    return delegateCommitContext.getTransaction();
  }

  public Map<Long, InternalCDOTransaction> getRequestedIDs()
  {
    return requestedIDs;
  }

  public Map<Long, CDOObject> getDirtyObjects()
  {
    return delegateCommitContext.getDirtyObjects();
  }

  public Map<Long, CDOObject> getNewObjects()
  {
    return delegateCommitContext.getNewObjects();
  }

  public List<CDOPackageUnit> getNewPackageUnits()
  {
    return delegateCommitContext.getNewPackageUnits();
  }

  public Map<Long, CDOObject> getDetachedObjects()
  {
    return delegateCommitContext.getDetachedObjects();
  }

  public Map<Long, CDORevisionDelta> getRevisionDeltas()
  {
    return delegateCommitContext.getRevisionDeltas();
  }

  public CDOCommitData getCommitData()
  {
    return delegateCommitContext.getCommitData();
  }

  public Collection<CDOLob<?>> getLobs()
  {
    return delegateCommitContext.getLobs();
  }

  public boolean isPartialCommit()
  {
    return delegateCommitContext.isPartialCommit();
  }

  public Object call() throws Exception
  {
    state.handle(this, progressMonitor);
    return true;
  }

  public long provideCDOID(Object idOrObject)
  {
      if (idOrObject instanceof CDOObject)
      {
        if (!requestedIDs.containsKey(((CDOObject) idOrObject).cdoID()))
        {
          InternalCDOObject cdoObject = (InternalCDOObject)CDOUtil.getCDOObject((InternalEObject)idOrObject);
          InternalCDOTransaction cdoTransaction = (InternalCDOTransaction)cdoObject.cdoView();
          getTransactionManager().add(cdoTransaction, ((CDOObject) idOrObject).cdoID());
          requestedIDs.put(((CDOObject) idOrObject).cdoID(), cdoTransaction);
        }
        return ((CDOObject) idOrObject).cdoID();
      }else{
    	  return (Long) idOrObject;
      }
  }

  public void preCommit()
  {
    delegateCommitContext.preCommit();
  }

  public void postCommit(CommitTransactionResult result)
  {
    if (result != null)
    {

      delegateCommitContext.postCommit(result);
    }
  }

  @Override
  public String toString()
  {
    return MessageFormat.format("CDOXACommitContext[{0}, {1}]", transactionManager, state);
  }
}
