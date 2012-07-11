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
package org.eclipse.emf.cdo.spi.server;

import java.util.List;

import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
import org.eclipse.emf.cdo.server.IView;
import org.eclipse.net4j.util.lifecycle.ILifecycle;

/**
 * @author Eike Stepper
 * @since 3.0
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface InternalView extends IView, ILifecycle
{
  public InternalSession getSession();

  public InternalRepository getRepository();

  /**
   * @since 4.0
   */
  public void setDurableLockingID(String durableLockingID);

  /**
   * @since 4.0
   */
  public void changeTarget(List<Long> invalidObjects,
      List<CDORevisionDelta> allChangedObjects, List<Long> allDetachedObjects);

  public void subscribe(long id);

  public void unsubscribe(long id);

  public boolean hasSubscription(long id);

  public void clearChangeSubscription();

  public void doClose();
}
