/*
 * Copyright 2011 UnboundID Corp.
 * All Rights Reserved.
 */

package com.unboundid.scim.sdk;

import static com.unboundid.scim.sdk.StaticUtils.*;



/**
 * This enumeration defines a set of debugging types that are used by the SCIM
 * SDK.
 */
public enum DebugType
{
  /**
   * The debug type that will be used for debugging information about
   * exceptions that are caught.
   */
  EXCEPTION("exception"),



  /**
   * The debug type that will be used for information about coding errors or
   * other types of incorrect uses of the SCIM SDK.
   */
  CODING_ERROR("coding-error"),



  /**
   * The debug type that will be used for debug messages not applicable to any
   * of the other categories.
   */
  OTHER("other");



  // The name for this debug type.
  private final String name;



  /**
   * Creates a new debug type with the specified name.
   *
   * @param  name  The name for this debug type.  It should be in all lowercase
   *               characters.
   */
  private DebugType(final String name)
  {
    this.name = name;
  }



  /**
   * Retrieves the name for this debug type.
   *
   * @return  The name for this debug type.
   */
  public String getName()
  {
    return name;
  }



  /**
   * Retrieves the debug type with the specified name.
   *
   * @param  name  The name of the debug type to retrieve.
   *
   * @return  The requested debug type, or {@code null} if there is no such
   *          debug type.
   */
  public static DebugType forName(final String name)
  {
    final String lowerName = toLowerCase(name);

    if (lowerName.equals("exception"))
    {
      return EXCEPTION;
    }
    else if (lowerName.equals("coding-error"))
    {
      return CODING_ERROR;
    }
    else if (lowerName.equals("other"))
    {
      return OTHER;
    }

    return null;
  }



  /**
   * Retrieves a comma-delimited list of the defined debug type names.
   *
   * @return  A comma-delimited list of the defined debug type names.
   */
  public static String getTypeNameList()
  {
    final StringBuilder buffer = new StringBuilder();

    final DebugType[] types = DebugType.values();
    for (int i=0; i < types.length; i++)
    {
      if (i > 0)
      {
        buffer.append(", ");
      }

      buffer.append(types[i].getName());
    }

    return buffer.toString();
  }



  /**
   * Retrieves a string representation of this debug type.
   *
   * @return  A string representation of this debug type.
   */
  @Override()
  public String toString()
  {
    return name;
  }
}
