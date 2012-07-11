/***************************************************************************
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 **************************************************************************/
package org.eclipse.emf.cdo.internal.net4j.protocol;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.eclipse.emf.cdo.common.commit.CDOCommitData;
import org.eclipse.emf.cdo.common.lob.CDOLob;
import org.eclipse.emf.cdo.common.protocol.CDODataOutput;
import org.eclipse.emf.cdo.common.protocol.CDOProtocolConstants;
import org.eclipse.emf.ecore.EClass;

/**
 * @author Eike Stepper
 */
public class CommitDelegationRequest extends CommitTransactionRequest
{
  private static final int UNKNOWN_TRANSACTION_ID = 0;

  private String userID;

  private Map<Long, EClass> detachedObjectTypes;

  public CommitDelegationRequest(CDOClientProtocol protocol, String userID, String comment,
      CDOCommitData commitData, Map<Long, EClass> detachedObjectTypes, Collection<CDOLob<?>> lobs)
  {
    super(protocol, CDOProtocolConstants.SIGNAL_COMMIT_DELEGATION, UNKNOWN_TRANSACTION_ID, comment, false, commitData, lobs);

    this.userID = userID;
    this.detachedObjectTypes = detachedObjectTypes;
  }

  @Override
  protected void requestingTransactionInfo(CDODataOutput out) throws IOException
  {
    out.writeString(userID);
  }

  @Override
  protected EClass getObjectType(long id)
  {
    return detachedObjectTypes.get(id);
  }
}
