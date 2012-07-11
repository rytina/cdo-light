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

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.internal.common.branch.CDOBranchPointImpl;

import org.eclipse.net4j.util.ObjectUtil;

import java.text.MessageFormat;
import java.util.LinkedList;

/**
 * @author Eike Stepper
 * @since 3.0
 * @noextend This interface is not intended to be extended by clients.
 */
public class CDOChangeSetSegment implements CDOBranchPoint
{


  public CDOChangeSetSegment()
  {
  }


  public static CDOChangeSetSegment[] createFrom(CDOBranchPoint startPoint, CDOBranchPoint endPoint)
  {
    LinkedList<CDOChangeSetSegment> result = null;
    return result.toArray(new CDOChangeSetSegment[result.size()]);
  }
}
