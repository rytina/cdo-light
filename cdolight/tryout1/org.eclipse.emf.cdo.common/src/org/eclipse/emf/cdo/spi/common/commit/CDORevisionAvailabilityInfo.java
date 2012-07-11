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
package org.eclipse.emf.cdo.spi.common.commit;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.CDORevisionKey;
import org.eclipse.emf.cdo.common.revision.CDORevisionProvider;

/**
 * @author Eike Stepper
 * @since 3.0
 */
public final class CDORevisionAvailabilityInfo implements CDORevisionProvider
{

  private Map<Long, CDORevisionKey> availableRevisions = new HashMap<Long, CDORevisionKey>();

  public CDORevisionAvailabilityInfo()
  {
  }

  public Map<Long, CDORevisionKey> getAvailableRevisions()
  {
    return availableRevisions;
  }

  public void addRevision(CDORevisionKey key)
  {
    availableRevisions.put(key.getID(), key);
  }

  public void removeRevision(long id)
  {
    availableRevisions.remove(id);
  }

  public boolean containsRevision(long id)
  {
    return availableRevisions.containsKey(id);
  }

  public CDORevision getRevision(long id)
  {
    return (CDORevision)availableRevisions.get(id);
  }

}
