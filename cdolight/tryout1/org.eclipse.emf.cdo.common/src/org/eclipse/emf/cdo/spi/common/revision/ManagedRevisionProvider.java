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

import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.CDORevisionManager;
import org.eclipse.emf.cdo.common.revision.CDORevisionProvider;

/**
 * @author Eike Stepper
 * @since 4.0
 */
public class ManagedRevisionProvider implements CDORevisionProvider
{
  private CDORevisionManager revisionManager;

  public ManagedRevisionProvider(CDORevisionManager revisionManager, CDOBranchPoint branchPoint)
  {
    this.revisionManager = revisionManager;
  }

  public CDORevisionManager getRevisionManager()
  {
    return revisionManager;
  }


  public CDORevision getRevision(long id)
  {
    return revisionManager.getRevision(id, CDORevision.UNCHUNKED, CDORevision.DEPTH_NONE, true);
  }
}
