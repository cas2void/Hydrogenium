/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.client.gui;

import java.io.InputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class Config
{
    private boolean isStandalone;
    private boolean isFullscreen;

    private int fps;

    private String host;
    private int port;
    private int id;

    // OpenGL global scene size
    private int globalWidth;
    private int globalHeight;

    // OpenGL local scene size
    private int localWidth;
    private int localHeight;

    // local scene's position in global scene
    private int localPosX;
    private int localPosY;

    // window size
    private int windowWidth;
    private int windowHeight;

    // window position in global scene
    private int windowPosX;
    private int windowPosY;


    public Config()
    {}

    public boolean load(InputStream is)
    {
        Document document = null;

        try
        {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            document = builder.parse(is);
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
        catch (SAXException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (document != null)
        {
            Element root = document.getDocumentElement();
            NodeList nodes = root.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++)
            {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE)
                {
                    if ("standalone".equals(node.getNodeName()))
                    {
                        isStandalone = Boolean.valueOf(node.getFirstChild().getNodeValue());
                    }
                    else if ("fullscreen".equals(node.getNodeName()))
                    {
                        isFullscreen = Boolean.valueOf(node.getFirstChild().getNodeValue());
                    }
                    else if ("fps".equals(node.getNodeName()))
                    {
                        fps = Integer.valueOf(node.getFirstChild().getNodeValue());
                    }
                    else if ("network".equals(node.getNodeName()))
                    {
                        Element child = (Element)node;
                        NodeList childNodes = child.getChildNodes();
                        for (int j = 0; j < childNodes.getLength(); j++)
                        {
                            Node childNode = childNodes.item(j);
                            if (childNode.getNodeType() == Node.ELEMENT_NODE)
                            {
                                if ("host".equals(childNode.getNodeName()))
                                {
                                    host = childNode.getFirstChild().getNodeValue();
                                }
                                else if ("port".equals(childNode.getNodeName()))
                                {
                                    port = Integer.valueOf(childNode.getFirstChild().getNodeValue());
                                }
                                else if ("id".equals(childNode.getNodeName()))
                                {
                                    id = Integer.valueOf(childNode.getFirstChild().getNodeValue());
                                }
                            } // end of if (childNode.getNodeType() == Node.ELEMENT_NODE)
                        } // end of for (int j = 0; j < childNodes.getLength(); j++)
                    }
                    else if ("global".equals(node.getNodeName()))
                    {
                        Element child = (Element)node;
                        NodeList childNodes = child.getChildNodes();
                        for (int j = 0; j < childNodes.getLength(); j++)
                        {
                            Node childNode = childNodes.item(j);
                            if (childNode.getNodeType() == Node.ELEMENT_NODE)
                            {
                                if ("width".equals(childNode.getNodeName()))
                                {
                                    globalWidth = Integer.valueOf(childNode.getFirstChild().getNodeValue());
                                }
                                else if ("height".equals(childNode.getNodeName()))
                                {
                                    globalHeight = Integer.valueOf(childNode.getFirstChild().getNodeValue());
                                }
                            } // end of if (childNode.getNodeType() == Node.ELEMENT_NODE)
                        } // end of for (int j = 0; j < childNodes.getLength(); j++)
                    }
                    else if ("local".equals(node.getNodeName()))
                    {
                        Element child = (Element)node;
                        NodeList childNodes = child.getChildNodes();
                        for (int j = 0; j < childNodes.getLength(); j++)
                        {
                            Node childNode = childNodes.item(j);
                            if (childNode.getNodeType() == Node.ELEMENT_NODE)
                            {
                                if ("width".equals(childNode.getNodeName()))
                                {
                                    localWidth = Integer.valueOf(childNode.getFirstChild().getNodeValue());
                                }
                                else if ("height".equals(childNode.getNodeName()))
                                {
                                    localHeight = Integer.valueOf(childNode.getFirstChild().getNodeValue());
                                }
                                else if ("x".equals(childNode.getNodeName()))
                                {
                                    localPosX = Integer.valueOf(childNode.getFirstChild().getNodeValue());
                                }
                                else if ("y".equals(childNode.getNodeName()))
                                {
                                    localPosY = Integer.valueOf(childNode.getFirstChild().getNodeValue());
                                }
                            } // end of if (childNode.getNodeType() == Node.ELEMENT_NODE)
                        } // end of for (int j = 0; j < childNodes.getLength(); j++)
                    }
                    else if ("window".equals(node.getNodeName()))
                    {
                        Element child = (Element)node;
                        NodeList childNodes = child.getChildNodes();
                        for (int j = 0; j < childNodes.getLength(); j++)
                        {
                            Node childNode = childNodes.item(j);
                            if (childNode.getNodeType() == Node.ELEMENT_NODE)
                            {
                                if ("x".equals(childNode.getNodeName()))
                                {
                                    windowPosX = Integer.valueOf(childNode.getFirstChild().getNodeValue());
                                }
                                else if ("y".equals(childNode.getNodeName()))
                                {
                                    windowPosY = Integer.valueOf(childNode.getFirstChild().getNodeValue());
                                }
                            } // end of if (childNode.getNodeType() == Node.ELEMENT_NODE)
                        } // end of for (int j = 0; j < childNodes.getLength(); j++)
                    }
                } // end of if (node.getNodeType() == Node.ELEMENT_NODE)
            } // end of for (int i = 0; i < nodes.getLength(); i++)
        } // end of if (document != null)

        windowWidth = localWidth;
        windowHeight = localHeight;

        return true;
    }

    public boolean getIsStandalone()
    {
        return isStandalone;
    }

    public boolean getIsFullscreen()
    {
        return isFullscreen;
    }

    public int getFPS()
    {
        return fps;
    }

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }

    public int getID()
    {
        return id;
    }

    public int getGlobalWidth()
    {
        return globalWidth;
    }

    public int getGlobalHeight()
    {
        return globalHeight;
    }

    public int getLocalWidth()
    {
        return localWidth;
    }

    public int getLocalHeight()
    {
        return localHeight;
    }

    public int getLocalPosX()
    {
        return localPosX;
    }

    public int getLocalPosY()
    {
        return localPosY;
    }

    public int getWindowWidth()
    {
        return windowWidth;
    }

    public int getWindowHeight()
    {
        return windowHeight;
    }

    public int getWindowPosX()
    {
        return windowPosX;
    }

    public int getWindowPosY()
    {
        return windowPosY;
    }
}
