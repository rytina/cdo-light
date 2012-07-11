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
package org.eclipse.emf.cdo.common.revision;

import java.util.List;

import org.eclipse.emf.cdo.common.CDOCommonRepository;
import org.eclipse.emf.ecore.EClass;

/**
 * Provides access to {@link CDORevision revisions} in a CDO {@link CDOCommonRepository repository} by demand loading
 * and caching them.
 * 
 * @author Eike Stepper
 * @since 3.0
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface CDORevisionManager
{
  /**
   * @return The type of an object if a revision for that object is in the revision cache, <code>null</code> otherwise.
   */
  public EClass getObjectType(long id);

  public boolean containsRevision(long id);

  public CDORevision getRevision(long id, int referenceChunk, int prefetchDepth,
      boolean loadOnDemand);

  public List<CDORevision> getRevisions(List<Long> ids, int referenceChunk,
      int prefetchDepth, boolean loadOnDemand);

}
