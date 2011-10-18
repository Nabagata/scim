/*
 * Copyright 2011 UnboundID Corp.
 * All Rights Reserved.
 */
package com.unboundid.scim.schema;

import com.unboundid.scim.data.AttributeValueResolver;
import com.unboundid.scim.data.Entry;
import com.unboundid.scim.sdk.SCIMAttribute;
import com.unboundid.scim.sdk.SCIMAttributeValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides methods that describe the schema for a SCIM attribute.
 * It may be used to help read and write SCIM attributes in their external XML
 * and JSON representation, and to convert SCIM attributes to and from LDAP
 * attributes.
 */
public final class AttributeDescriptor {

  /**
   * Defines the set of well known SCIM supported datatypes.
   */
  public static enum DataType {
    /**
     * String data type.
     */
    STRING,
    /**
     * Boolean data type.
     */
    BOOLEAN,
    /**
     * Date Time data type.
     */
    DATETIME,
    /**
     * Integer data type.
     */
    INTEGER,
    /**
     * Binary data type.
     */
    BINARY,
    /**
     * Complex data type.
     */
    COMPLEX;

    /**
     * Parses a supplied data type into a SCIM defined data type.
     * @param type The type to convert
     *
     * @return The DataType or null if not supported
     */
    public static DataType parse(final String type) {
      try {
        return DataType.valueOf(type.toUpperCase());
      } catch (Exception e) {
        return null;
      }
    }

  }

  static final class SubAttributeDescriptorResolver
      extends AttributeValueResolver<AttributeDescriptor>
  {
    private final String schema;

    /**
     * Construct a new sub attribute resolver.
     *
     * @param schema The schema of the parent attribute.
     */
    SubAttributeDescriptorResolver(final String schema) {
      this.schema = schema;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SCIMAttributeValue fromInstance(
        final AttributeDescriptor attributeDescriptor,
        final AttributeDescriptor value) {
      final List<SCIMAttribute> attributes =
          new ArrayList<SCIMAttribute>(6);

      attributes.add(SCIMAttribute.createSingularAttribute(
          attributeDescriptor.getSubAttribute("name"),
          SCIMAttributeValue.createStringValue(value.getName())));

      attributes.add(SCIMAttribute.createSingularAttribute(
          attributeDescriptor.getSubAttribute("type"),
          SCIMAttributeValue.createStringValue(
              value.getDataType().toString())));

      attributes.add(SCIMAttribute.createSingularAttribute(
          attributeDescriptor.getSubAttribute("description"),
          SCIMAttributeValue.createStringValue(value.getDescription())));

      attributes.add(SCIMAttribute.createSingularAttribute(
          attributeDescriptor.getSubAttribute("readOnly"),
          SCIMAttributeValue.createBooleanValue(value.isReadOnly())));

      attributes.add(SCIMAttribute.createSingularAttribute(
          attributeDescriptor.getSubAttribute("required"),
          SCIMAttributeValue.createBooleanValue(value.isRequired())));

      attributes.add(SCIMAttribute.createSingularAttribute(
          attributeDescriptor.getSubAttribute("caseExact"),
          SCIMAttributeValue.createBooleanValue(value.isCaseExact())));

      return SCIMAttributeValue.createComplexValue(attributes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeDescriptor toInstance(final SCIMAttributeValue value) {
      return new AttributeDescriptor(
          value.getSingularSubAttributeValue("name",
              AttributeValueResolver.STRING_RESOLVER),
          DataType.parse(value.getSingularSubAttributeValue("type",
              AttributeValueResolver.STRING_RESOLVER)),
          false,
          value.getSingularSubAttributeValue("description",
              AttributeValueResolver.STRING_RESOLVER),
          schema,
          value.getSingularSubAttributeValue("readOnly",
              AttributeValueResolver.BOOLEAN_RESOLVER),
          value.getSingularSubAttributeValue("required",
              AttributeValueResolver.BOOLEAN_RESOLVER),
          value.getSingularSubAttributeValue("caseExact",
              AttributeValueResolver.BOOLEAN_RESOLVER),
          null, null);
    }
  }

  /**
   * The <code>AttributeValueResolver</code> that resolves SCIM attribute values
   * to/from <code>AttributeDescriptor</code> instances.
   */
  public static final AttributeValueResolver<AttributeDescriptor>
      ATTRIBUTE_DESCRIPTOR_RESOLVER = new AttributeDescriptorResolver(false);

  static final class AttributeDescriptorResolver extends
      AttributeValueResolver<AttributeDescriptor>
  {
    private final boolean allowNesting;

    /**
     * Create a new AttributeDescriptorResolver.
     *
     * @param allowNesting <code>true</code> to allow nesting of complex
     *                     types or <code>false</code> otherwise.
     */
    AttributeDescriptorResolver(final boolean allowNesting) {
      this.allowNesting = allowNesting;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeDescriptor toInstance(final SCIMAttributeValue value) {
      String schemaValue = value.getSingularSubAttributeValue("schema",
          AttributeValueResolver.STRING_RESOLVER);
      return new AttributeDescriptor(
          value.getSingularSubAttributeValue("name",
              AttributeValueResolver.STRING_RESOLVER),
          DataType.parse(value.getSingularSubAttributeValue("type",
              AttributeValueResolver.STRING_RESOLVER)),
          value.getSingularSubAttributeValue("plural",
              AttributeValueResolver.BOOLEAN_RESOLVER),
          value.getSingularSubAttributeValue("description",
              AttributeValueResolver.STRING_RESOLVER),
          schemaValue,
          value.getSingularSubAttributeValue("readOnly",
              AttributeValueResolver.BOOLEAN_RESOLVER),
          value.getSingularSubAttributeValue("required",
              AttributeValueResolver.BOOLEAN_RESOLVER),
          value.getSingularSubAttributeValue("caseExact",
              AttributeValueResolver.BOOLEAN_RESOLVER),
          value.getPluralAttributeValue("pluralTypes",
              Entry.STRINGS_RESOLVER),
          value.getPluralAttributeValue(
              allowNesting ? "attributes" : "subAttributes",
              allowNesting ? this :
              new SubAttributeDescriptorResolver(schemaValue)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SCIMAttributeValue fromInstance(
        final AttributeDescriptor attributeDescriptor,
        final AttributeDescriptor value) {

      final List<SCIMAttribute> attributes =
          new ArrayList<SCIMAttribute>(10);

      attributes.add(SCIMAttribute.createSingularAttribute(
          attributeDescriptor.getSubAttribute("name"),
          SCIMAttributeValue.createStringValue(value.getName())));

      attributes.add(SCIMAttribute.createSingularAttribute(
          attributeDescriptor.getSubAttribute("type"),
          SCIMAttributeValue.createStringValue(
              value.getDataType().toString())));

      attributes.add(SCIMAttribute.createSingularAttribute(
          attributeDescriptor.getSubAttribute("plural"),
          SCIMAttributeValue.createBooleanValue(value.isPlural())));

      attributes.add(SCIMAttribute.createSingularAttribute(
          attributeDescriptor.getSubAttribute("description"),
          SCIMAttributeValue.createStringValue(value.getDescription())));

      attributes.add(SCIMAttribute.createSingularAttribute(
          attributeDescriptor.getSubAttribute("schema"),
          SCIMAttributeValue.createStringValue(value.getSchema())));

      attributes.add(SCIMAttribute.createSingularAttribute(
          attributeDescriptor.getSubAttribute("readOnly"),
          SCIMAttributeValue.createBooleanValue(value.isReadOnly())));

      attributes.add(SCIMAttribute.createSingularAttribute(
          attributeDescriptor.getSubAttribute("required"),
          SCIMAttributeValue.createBooleanValue(value.isRequired())));

      attributes.add(SCIMAttribute.createSingularAttribute(
          attributeDescriptor.getSubAttribute("caseExact"),
          SCIMAttributeValue.createBooleanValue(value.isCaseExact())));

      if(value.getPluralTypes() != null)
      {
        final AttributeDescriptor pluralTypesAttributeDescriptor =
            attributeDescriptor.getSubAttribute("pluralTypes");
        final SCIMAttributeValue[] pluralTypeValues =
            new SCIMAttributeValue[value.getPluralTypes().size()];
        int i = 0;
        for(Entry<String> pluralType : value.getPluralTypes())
        {
          pluralTypeValues[i++] = Entry.STRINGS_RESOLVER.fromInstance(
              pluralTypesAttributeDescriptor,pluralType);
        }
        attributes.add(SCIMAttribute.createPluralAttribute(
            pluralTypesAttributeDescriptor, pluralTypeValues));
      }

      if(value.getSubAttributes() != null)
      {
        final AttributeDescriptor subAttributesDescriptor =
            allowNesting ? attributeDescriptor :
            attributeDescriptor.getSubAttribute("subAttributes");
        final SCIMAttributeValue[] subAttributeValues =
            new SCIMAttributeValue[value.getSubAttributes().size()];
        int i = 0;
        for(AttributeDescriptor subAttribute : value.getSubAttributes())
        {
          subAttributeValues[i++] = (allowNesting ? this :
              new SubAttributeDescriptorResolver(value.getSchema())).
              fromInstance(subAttributesDescriptor, subAttribute);
        }
        attributes.add(SCIMAttribute.createPluralAttribute(
            subAttributesDescriptor, subAttributeValues));
      }

      return SCIMAttributeValue.createComplexValue(attributes);
    }
  }

  private final String schema;

  private final String name;

  private final String description;

  private final boolean readOnly;

  private final boolean required;

  private final boolean plural;

  private final boolean caseExact;

  private final DataType dataType;

  private final Map<String, AttributeDescriptor> subAttributes;

  private final Collection<Entry<String>> pluralTypes;

  /**
   * Construct a new AttributeDescriptor instance with the provided info.
   *
   * @param name         The attribute's name.
   * @param dataType     The attribute's data type.
   * @param plural       Whether the attribute is plural.
   * @param description  The attribute's human readable description.
   * @param schema       The attribute's associated schema.
   * @param readOnly     Whether the attribute is mutable.
   * @param required     Whether the attribute is required.
   * @param caseExact    Whether the string attribute is case sensitive.
   * @param pluralTypes  A list of canonical type values.
   * @param subAttributes  A list specifying the contained attributes.
   */
  private AttributeDescriptor(final String name, final DataType dataType,
                              final boolean plural, final String description,
                              final String schema, final boolean readOnly,
                              final boolean required, final boolean caseExact,
                              final Collection<Entry<String>> pluralTypes,
                            final Collection<AttributeDescriptor> subAttributes)
  {
    this.name = name;
    this.dataType = dataType;
    this.plural = plural;
    this.description = description;
    this.schema = schema;
    this.readOnly = readOnly;
    this.required = required;
    this.caseExact = caseExact;
    this.pluralTypes = pluralTypes;

    if(subAttributes != null)
    {
      this.subAttributes =
          new LinkedHashMap<String, AttributeDescriptor>(subAttributes.size());
      for(AttributeDescriptor attributeDescriptor : subAttributes)
      {
        this.subAttributes.put(attributeDescriptor.getName(),
            attributeDescriptor);
      }
    }
    else
    {
      this.subAttributes = null;
    }
  }


  /**
   * The URI for the schema that defines the SCIM attribute.
   *
   * @return The URI for the schema that defines the SCIM attribute.
   *         It is never {@code null}.
   */
  public String getSchema() {
    return schema;
  }


  /**
   * The attribute name to be used in any external representation of the SCIM
   * attribute.
   *
   * @return The attribute name to be used in any external representation of
   *         the SCIM attribute. It is never {@code null}.
   */
  public String getName() {
    return name;
  }

  /**
   * Indicates whether the attribute is a plural attribute.
   *
   * @return {@code true} if the attribute is plural.
   */
  public boolean isPlural() {
    return plural;
  }

  /**
   * Retrieves the set of descriptors for subordinate attributes of a complex
   * attribute.
   *
   * @return The set of descriptors for subordinate attributes of a complex
   *         attribute, or {@code null} if the attribute is not a complex
   *         attribute.
   */
  public Collection<AttributeDescriptor> getSubAttributes()
  {
    return subAttributes == null ? null : subAttributes.values();
  }


  /**
   * Retrieves the attribute descriptor for a specified subordinate attribute
   * of a complex attribute.
   *
   * @param externalName The external name of the subordinate attribute for
   *                     which a descriptor is required.
   * @return The attribute descriptor for the specified subordinate attribute,
   *         or {@code null} if there is no such subordinate attribute.
   */
  public AttributeDescriptor getSubAttribute(final String externalName)
  {
    return subAttributes == null ? null : subAttributes.get(externalName);
  }


  /**
   * Retrieves a list of canonical type values.
   *
   * @return A list of canonical type values or <code>null</code> if the
   *         attribute is not a plural attribute or if they are not specified.
   */
  public Collection<Entry<String>> getPluralTypes()
  {
    return pluralTypes;
  }


  /**
   * Retrieve the data type for this attribute.
   *
   * @return  The data type for this attribute, or {@code null} if the attribute
   *          is not a simple attribute.
   */
  public DataType getDataType()
  {
    return dataType;
  }



  /**
   * Retrieves this attribute's human readable description.
   *
   * @return This attribute's human redable description.
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * Specifies if this attribute is mutable.
   *
   * @return <code>false</code> if this attribute is mutable or
   *         <code>true</code> otherwise.
   */
  public boolean isReadOnly()
  {
    return readOnly;
  }

  /**
   * Specifies if this attribute is required.
   *
   * @return <code>true</code> if this attribute is required for
   *         <code>false</code> otherwise.
   */
  public boolean isRequired()
  {
    return required;
  }

  /**
   * Specifies if the string attribute is case sensitive.
   *
   * @return <code>true</code> if this attribute is case sensitive or
   *         <code>false</code> otherwise.
   */
  public boolean isCaseExact()
  {
    return caseExact;
  }

  @Override
  public String toString()
  {
    return "AttributeDescriptor{" +
        "schema='" + getSchema() + '\'' +
        ", name='" + getName() + '\'' +
        ", description='" + getDescription() + '\'' +
        ", plural=" + isPlural() +
        ", dataType=" + getDataType() +
        ", isRequired=" + isRequired() +
        ", isReadOnly=" + isReadOnly() +
        ", isCaseExact=" + isCaseExact() +
        '}';
  }

  /**
   * Returns a hash code value for the object. This method is
   * supported for the benefit of hashtables such as those provided by
   * <code>java.util.Hashtable</code>.
   * <p/>
   * The general contract of <code>hashCode</code> is:
   * <ul>
   * <li>Whenever it is invoked on the same object more than once during
   * an execution of a Java application, the <tt>hashCode</tt> method
   * must consistently return the same integer, provided no information
   * used in <tt>equals</tt> comparisons on the object is modified.
   * This integer need not remain consistent from one execution of an
   * application to another execution of the same application.
   * <li>If two objects are equal according to the <tt>equals(Object)</tt>
   * method, then calling the <code>hashCode</code> method on each of
   * the two objects must produce the same integer result.
   * <li>It is <em>not</em> required that if two objects are unequal
   * according to the {@link Object#equals(Object)}
   * method, then calling the <tt>hashCode</tt> method on each of the
   * two objects must produce distinct integer results.  However, the
   * programmer should be aware that producing distinct integer results
   * for unequal objects may improve the performance of hashtables.
   * </ul>
   * <p/>
   * As much as is reasonably practical, the hashCode method defined by
   * class <tt>Object</tt> does return distinct integers for distinct
   * objects. (This is typically implemented by converting the internal
   * address of the object into an integer, but this implementation
   * technique is not required by Java.)
   *
   * @return a hash code value for this object.
   *
   * @see Object#equals(Object)
   * @see java.util.Hashtable
   */
  @Override
  public int hashCode()
  {
    int hashCode = 0;

    hashCode += schema.hashCode();
    hashCode += name.toLowerCase().hashCode();

    return hashCode;
  }



  /**
   * Indicates whether some other object is "equal to" this one.
   * <p/>
   * The <code>equals</code> method implements an equivalence relation
   * on non-null object references:
   * <ul>
   * <li>It is <i>reflexive</i>: for any non-null reference value
   * <code>x</code>, <code>x.equals(x)</code> should return
   * <code>true</code>.
   * <li>It is <i>symmetric</i>: for any non-null reference values
   * <code>x</code> and <code>y</code>, <code>x.equals(y)</code>
   * should return <code>true</code> if and only if
   * <code>y.equals(x)</code> returns <code>true</code>.
   * <li>It is <i>transitive</i>: for any non-null reference values
   * <code>x</code>, <code>y</code>, and <code>z</code>, if
   * <code>x.equals(y)</code> returns <code>true</code> and
   * <code>y.equals(z)</code> returns <code>true</code>, then
   * <code>x.equals(z)</code> should return <code>true</code>.
   * <li>It is <i>consistent</i>: for any non-null reference values
   * <code>x</code> and <code>y</code>, multiple invocations of
   * <tt>x.equals(y)</tt> consistently return <code>true</code>
   * or consistently return <code>false</code>, provided no
   * information used in <code>equals</code> comparisons on the
   * objects is modified.
   * <li>For any non-null reference value <code>x</code>,
   * <code>x.equals(null)</code> should return <code>false</code>.
   * </ul>
   * <p/>
   * The <tt>equals</tt> method for class <code>Object</code> implements
   * the most discriminating possible equivalence relation on objects;
   * that is, for any non-null reference values <code>x</code> and
   * <code>y</code>, this method returns <code>true</code> if and only
   * if <code>x</code> and <code>y</code> refer to the same object
   * (<code>x == y</code> has the value <code>true</code>).
   * <p/>
   * Note that it is generally necessary to override the <tt>hashCode</tt>
   * method whenever this method is overridden, so as to maintain the
   * general contract for the <tt>hashCode</tt> method, which states
   * that equal objects must have equal hash codes.
   *
   * @param obj the reference object with which to compare.
   *
   * @return <code>true</code> if this object is the same as the obj
   *         argument; <code>false</code> otherwise.
   *
   * @see #hashCode()
   * @see java.util.Hashtable
   */
  @Override
  public boolean equals(final Object obj)
  {
    if (this == obj)
    {
      return true;
    }

    if (!(obj instanceof AttributeDescriptor))
    {
      return false;
    }

    final AttributeDescriptor that = (AttributeDescriptor)obj;
    if (this.schema == null && that.schema == null)
    {
      return this.name.equalsIgnoreCase(that.name);
    }
    else
    {
      return this.schema != null && that.schema != null &&
          this.schema.equals(that.schema) &&
          this.name.equalsIgnoreCase(that.name);
    }
  }

  /**
   * Create a new singular simple attribute descriptor with the provided
   * information.
   *
   * @param name         The attribute's name.
   * @param dataType     The attribute's data type.
   * @param description  The attribute's human readable description.
   * @param schema       The attribute's associated schema.
   * @param readOnly     Whether the attribute is mutable.
   * @param required     Whether the attribute is required.
   * @param caseExact    Whether the string attribute is case sensitive.
   * @return             A new singular simple attribute descriptor with the
   *                     provided information.
   */
  public static AttributeDescriptor singularSimple(
      final String name, final DataType dataType, final String description,
      final String schema, final boolean readOnly, final boolean required,
      final boolean caseExact)
  {
    return new AttributeDescriptor(name, dataType, false, description, schema,
        readOnly, required, caseExact, null, null);
  }

  /**
   * Create a new singular complex attribute descriptor with the provided
   * information.
   *
   * @param name           The attribute's name.
   * @param description    The attribute's human readable description.
   * @param schema         The attribute's associated schema.
   * @param readOnly       Whether the attribute is mutable.
   * @param required       Whether the attribute is required.
   * @param subAttributes  A list specifying the contained attributes.
   * @return               A new singular complex attribute descriptor with the
   *                       provided information.
   */
  public static AttributeDescriptor singularComplex(
      final String name, final String description, final String schema,
      final boolean readOnly, final boolean required,
      final AttributeDescriptor... subAttributes)
  {
    return new AttributeDescriptor(name, DataType.COMPLEX, false, description,
        schema, readOnly, required, false, null,Arrays.asList(subAttributes));
  }

  /**
   * Create a new plural simple attribute descriptor with the provided
   * information.
   *
   * @param name         The attribute's name.
   * @param dataType     The attribute's data type.
   * @param description  The attribute's human readable description.
   * @param schema       The attribute's associated schema.
   * @param readOnly     Whether the attribute is mutable.
   * @param required     Whether the attribute is required.
   * @param caseExact    Whether the string attribute is case sensitive.
   * @param pluralTypes  A list of canonical type values.
   * @return             A new singular simple attribute descriptor with the
   *                     provided information.
   */
  public static AttributeDescriptor pluralSimple(
      final String name, final DataType dataType, final String description,
      final String schema, final boolean readOnly, final boolean required,
      final boolean caseExact, final String... pluralTypes)
  {
    final Collection<Entry<String>> pluralTypeEntries;
    if(pluralTypes != null)
    {
      pluralTypeEntries = new ArrayList<Entry<String>>(pluralTypes.length);
      for(String pluralType : pluralTypes)
      {
        pluralTypeEntries.add(new Entry<String>(pluralType, null, false));
      }
    }
    else
    {
      pluralTypeEntries = null;
    }
    return new AttributeDescriptor(name, dataType, true, description, schema,
        readOnly, required, caseExact, pluralTypeEntries,
        CoreSchema.createPluralSimpleSubAttributeList(dataType));
  }

  /**
   * Create a new singular complex attribute descriptor with the provided
   * information.
   *
   * @param name           The attribute's name.
   * @param description    The attribute's human readable description.
   * @param schema         The attribute's associated schema.
   * @param readOnly       Whether the attribute is mutable.
   * @param required       Whether the attribute is required.
   * @param pluralTypes  A list of canonical type values.
   * @param subAttributes  A list specifying the contained attributes.
   * @return               A new singular complex attribute descriptor with the
   *                       provided information.
   */
  public static AttributeDescriptor pluralComplex(
      final String name, final String description, final String schema,
      final boolean readOnly, final boolean required,
      final String[] pluralTypes, final AttributeDescriptor... subAttributes)
  {
    final Collection<Entry<String>> pluralTypeEntries;
    if(pluralTypes != null)
    {
      pluralTypeEntries = new ArrayList<Entry<String>>(pluralTypes.length);
      for(String pluralType : pluralTypes)
      {
        pluralTypeEntries.add(new Entry<String>(pluralType, null, false));
      }
    }
    else
    {
      pluralTypeEntries = null;
    }
    return new AttributeDescriptor(name, DataType.COMPLEX, true, description,
        schema, readOnly, required, false, pluralTypeEntries,
        CoreSchema.createPluralComplexSubAttributeList(subAttributes));
  }
}