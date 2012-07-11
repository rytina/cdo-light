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
package org.eclipse.emf.cdo.common.util;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.eclipse.emf.cdo.common.branch.CDOBranchManager;
import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.common.commit.CDOCommitInfoManager;
import org.eclipse.emf.cdo.common.lob.CDOLobStore;
import org.eclipse.emf.cdo.common.model.CDOPackageRegistry;
import org.eclipse.emf.cdo.common.protocol.CDODataInput;
import org.eclipse.emf.cdo.common.protocol.CDODataOutput;
import org.eclipse.emf.cdo.common.revision.CDOListFactory;
import org.eclipse.emf.cdo.common.revision.CDORevisionFactory;
import org.eclipse.emf.cdo.internal.common.protocol.CDODataInputImpl;
import org.eclipse.emf.cdo.internal.common.protocol.CDODataOutputImpl;
import org.eclipse.net4j.util.io.ExtendedDataInputStream;
import org.eclipse.net4j.util.io.ExtendedDataOutput;

/**
 * @author Eike Stepper
 * @since 3.0
 */
public final class CDOCommonUtil
{
  /**
   * @since 4.0
   */
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss'.'SSS");

  private CDOCommonUtil()
  {
  }

  /**
   * @since 4.0
   */
  public static CDODataInput createCDODataInput(ExtendedDataInputStream inputStream,
      final CDOPackageRegistry packageRegistry, final CDOBranchManager branchManager,
      final CDOCommitInfoManager commitManager, final CDORevisionFactory revisionFactory,
      final CDOListFactory listFactory, final CDOLobStore lobStore) throws IOException
  {
    return new CDODataInputImpl(inputStream)
    {
      @Override
      protected CDOPackageRegistry getPackageRegistry()
      {
        return packageRegistry;
      }

      @Override
      protected CDOBranchManager getBranchManager()
      {
        return branchManager;
      }

      @Override
      protected CDOCommitInfoManager getCommitInfoManager()
      {
        return commitManager;
      }

      @Override
      protected CDORevisionFactory getRevisionFactory()
      {
        return revisionFactory;
      }

      @Override
      protected CDOListFactory getListFactory()
      {
        return listFactory;
      }

      @Override
      protected CDOLobStore getLobStore()
      {
        return lobStore;
      }
    };
  }

  /**
   * @since 4.0
   */
  public static CDODataOutput createCDODataOutput(ExtendedDataOutput extendedDataOutputStream,
      final CDOPackageRegistry packageRegistry)
  {
    return new CDODataOutputImpl(extendedDataOutputStream)
    {
      @Override
      public CDOPackageRegistry getPackageRegistry()
      {
        return packageRegistry;
      }

    };
  }


}
