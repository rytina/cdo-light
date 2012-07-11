/**
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 *    Simon McDuff - bug 226778
 *    Simon McDuff - bug 213402
 *    Martin Taal - Added subtype handling and EClass conversion, bug 283106
 */
package org.eclipse.emf.cdo.common.id;


/**
 * Various static methods that may help with CDO {@link CDOID IDs}.
 * 
 * @author Eike Stepper
 * @since 2.0
 */
public final class CDOIDUtil
{
  private CDOIDUtil()
  {
  }

  /**
   * @since 2.0
   */
  public static boolean isNull(long id)
  {
    return id == 0;
  }


  public static void write(StringBuilder builder, long id)
  {

    builder.append(CDOIDUtil.toURIFragment(id));
  }

  private static Object toURIFragment(long id) {
	return String.valueOf(id);
 }


  public static long read(String uriFragment)
  {
    char typeID = uriFragment.charAt(0);
    String fragment = uriFragment.substring(1);
    return Long.parseLong(fragment);
  }


}
