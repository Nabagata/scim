/*
 * Copyright 2011 UnboundID Corp.
 * All Rights Reserved.
 */
package com.unboundid.scim.marshal.xml;

import com.unboundid.scim.config.AttributeDescriptor;
import com.unboundid.scim.config.ResourceDescriptor;
import com.unboundid.scim.config.Schema;
import com.unboundid.scim.config.SchemaManager;
import com.unboundid.scim.marshal.Unmarshaller;
import com.unboundid.scim.sdk.SCIMAttribute;
import com.unboundid.scim.sdk.SCIMAttributeValue;
import com.unboundid.scim.sdk.SCIMObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;



/**
 * This class provides a SCIM object un-marshaller implementation to read SCIM
 * objects from their XML representation.
 */
public class XmlUnmarshaller implements Unmarshaller
{

  /**
   * {@inheritDoc}
   */
  public SCIMObject unmarshal(final File file, final String resourceName)
      throws Exception
  {
    final FileInputStream fileInputStream = new FileInputStream(file);
    try
    {
      return unmarshal(fileInputStream, resourceName);
    }
    finally
    {
      fileInputStream.close();
    }
  }



  /**
   * {@inheritDoc}
   */
  public SCIMObject unmarshal(final InputStream inputStream,
                              final String resourceName)
      throws Exception
  {
    final SCIMObject scimObject = new SCIMObject();
    final DocumentBuilderFactory dbFactory =
        DocumentBuilderFactory.newInstance();
    dbFactory.setNamespaceAware(true);
    dbFactory.setIgnoringElementContentWhitespace(true);
    dbFactory.setValidating(false);
    final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    final Document doc = dBuilder.parse(inputStream);
    doc.getDocumentElement().normalize();

    final SchemaManager schemaManager = SchemaManager.instance();
    final Element documentElement = doc.getDocumentElement();
    final ResourceDescriptor resourceDescriptor =
        schemaManager.getResourceDescriptor(documentElement.getLocalName());
    if (resourceDescriptor == null)
    {
      throw new RuntimeException("No resource descriptor found for " +
                                 documentElement.getLocalName());
    }

    scimObject.setResourceName(resourceDescriptor.getName());

    final String documentNamespaceURI = documentElement.getNamespaceURI();

    final NodeList nodeList = doc.getElementsByTagName("*");
    for (int i = 0; i < nodeList.getLength(); i++)
    {
      final Node element = nodeList.item(i);

      String namespaceURI = element.getNamespaceURI();
      if (namespaceURI == null)
      {
        namespaceURI = documentNamespaceURI; // TODO: not sure about this
      }

      final Schema schema = schemaManager.getSchema(namespaceURI);

      if (schema != null)
      {
        final AttributeDescriptor attributeDescriptor =
            schema.getAttribute(element.getLocalName());

        if (attributeDescriptor != null)
        {
          final SCIMAttribute attr;
          if (attributeDescriptor.isPlural())
          {
            attr = createPluralAttribute(element, attributeDescriptor);
          }
          else if (attributeDescriptor.isComplex())
          {
            attr = createComplexAttribute(element, attributeDescriptor);
          }
          else
          {
            attr = this.createSimpleAttribute(element, attributeDescriptor);
          }

          scimObject.addAttribute(attr);
        }
      }
    }

    return scimObject;
  }



  /**
   * Parse a simple attribute from its representation as a DOM node.
   *
   * @param node                The DOM node representing the attribute.
   * @param attributeDescriptor The attribute descriptor.
   *
   * @return The parsed attribute.
   */
  private SCIMAttribute createSimpleAttribute(
      final Node node,
      final AttributeDescriptor attributeDescriptor)
  {
    return SCIMAttribute.createSimpleAttribute(attributeDescriptor,
                                               node.getTextContent());
  }



  /**
   * Parse a plural attribute from its representation as a DOM node.
   *
   * @param node                The DOM node representing the attribute.
   * @param attributeDescriptor The attribute descriptor.
   *
   * @return The parsed attribute.
   */
  private SCIMAttribute createPluralAttribute(
      final Node node,
      final AttributeDescriptor attributeDescriptor)
  {
    final NodeList pluralAttributes = node.getChildNodes();
    final List<SCIMAttributeValue> pluralScimAttributes =
        new LinkedList<SCIMAttributeValue>();
    for (int i = 0; i < pluralAttributes.getLength(); i++)
    {
      final Node pluralAttribute = pluralAttributes.item(i);
      if (pluralAttribute.getNodeType() != Node.ELEMENT_NODE)
      {
        continue;
      }
      final AttributeDescriptor pluralAttributeDescriptorInstance =
          attributeDescriptor.getAttribute(pluralAttribute.getNodeName());
      pluralScimAttributes.add(SCIMAttributeValue.createComplexValue(
          createComplexAttribute(pluralAttributes.item(i),
                                 pluralAttributeDescriptorInstance)));
    }
    SCIMAttributeValue[] vals =
        new SCIMAttributeValue[pluralScimAttributes.size()];
    vals = pluralScimAttributes.toArray(vals);
    return SCIMAttribute.createPluralAttribute(attributeDescriptor, vals);
  }



  /**
   * Parse a complex attribute from its representation as a DOM node.
   *
   * @param node                The DOM node representing the attribute.
   * @param attributeDescriptor The attribute descriptor.
   *
   * @return The parsed attribute.
   */
  private SCIMAttribute createComplexAttribute(
      final Node node,
      final AttributeDescriptor attributeDescriptor)
  {
    NodeList childNodes = node.getChildNodes();
    List<SCIMAttribute> complexAttrs = new LinkedList<SCIMAttribute>();
    for (int i = 0; i < childNodes.getLength(); i++)
    {
      Node item1 = childNodes.item(i);
      if (item1.getNodeType() == Node.ELEMENT_NODE)
      {
        AttributeDescriptor complexAttr =
            attributeDescriptor.getAttribute(item1.getNodeName());
        SCIMAttribute childAttr = createSimpleAttribute(item1, complexAttr);
        complexAttrs.add(childAttr);
      }
    }

    return SCIMAttribute.createSingularAttribute(
        attributeDescriptor,
        SCIMAttributeValue.createComplexValue(complexAttrs));
  }
}