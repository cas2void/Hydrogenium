/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.server.core;

import java.util.ArrayList;
import java.util.Iterator;
import hydrogenium.util.Debug;
import hydrogenium.util.LoopJobFramework;

public class ServerCore extends LoopJobFramework 
{
    private RequestListener requestListener;

    // list of connection
    private ArrayList<ClientHandler> connectionList;
    // list of valid client
    private ArrayList<ClientHandler> clientList;
    // shadow copy of connectionList used for checking whether the ClientHandler is down
    private ArrayList<ClientHandler> dumpList;

    // send to clients to sync their frame
    private int currFrameId;

    // whether resetting current frame when new client connected
    private boolean reset;

    // server should send first rendering message 
    // if all "ok" messages has been received
    private volatile boolean okResponsed;

    public ServerCore(int port, int fps, boolean reset)
    {
        // non fixed rate, paused
        super(fps, "ServerCore", false, true);

        requestListener = new RequestListener(this, port);
        connectionList = new ArrayList<ClientHandler>();
        clientList = new ArrayList<ClientHandler>();

        currFrameId = 0;
        this.reset = reset;

        new Thread(requestListener).start();
    }

    synchronized protected void cleanUp()
    {
        dumpList = new ArrayList<ClientHandler>(connectionList);
        Iterator<ClientHandler> iter = connectionList.iterator();
        while (iter.hasNext())
        {
            System.out.println("about to shutdown client in ServerCore");
            iter.next().shutdown();
        }

        requestListener.shutdown();
    }

    synchronized protected boolean isClean()
    {
        if (!requestListener.isDown())
        {
            return false;
        }
        else
        {
            for (ClientHandler client : dumpList)
            {
                if (!client.isDown())
                {
                    return false;
                }
            }
        }

        // now we can do the housekeeping
        dumpList.clear();

        // return true only if everyone is down
        return true;
    }

    synchronized public void addConnection(ClientHandler client)
    {
        connectionList.add(client);
        Debug.println("Add Connection");
        Debug.println("connectionList: " + connectionList);
    }

    synchronized public void delConnection(ClientHandler client)
    {
        connectionList.remove(client);
        client.shutdown();
        Debug.println("Del Connection");
        Debug.println("connectionList: " + connectionList);
    }

    synchronized public boolean hasClient(ClientHandler client)
    {
        boolean found = false;
        for (ClientHandler c : clientList)
        {
            // delete testing of client's id, as we may use backup client machines, 
            // whose ids are the same
            //if (c.getId() == client.getId() || c.getName() == client.getName())
            if (c.getName() == client.getName())
            {
                // found one
                Debug.println("already have it");
                found = true;
                break;
            }
        }

        return found;
    }

    synchronized public boolean addClient(ClientHandler client)
    {
        boolean found = hasClient(client);

        // add this client only when neither its id nor name matches 
        if (!found)
        {
            clientList.add(client);

            Debug.format("Add Client, id: %d, name: %s\n", client.getId(), client.getName());
            printClientList();

            return true;
        }
        else
        {
            return false;
        }
    }

    synchronized public void delClient(ClientHandler client)
    {
        clientList.remove(client);

        // client's id should be non-negative, get rid of the debug info when there is
        // such client in the list.
        if (client.getId() > 0)
        {
            Debug.format("Del Client, id: %d, name: %s\n", client.getId(), client.getName());
            printClientList();
        }
    }

    synchronized public void printClientList()
    {
        if (Debug.ENABLED)
        {
            Debug.println("clientList size: " + clientList.size());

            for (ClientHandler client : clientList)
            {
                client.printClient();
            }
        }
    }

    synchronized public int getCurrFrameId()
    {
        return currFrameId;
    }

    synchronized private boolean isAllFinished()
    {
        if (clientList.size() == 0)
        {
            return false;
        }
        else
        {
            for (ClientHandler client : clientList)
            {
                if (!client.isRenderingFinished())
                {
                    return false;
                }
            }

            return true;
        }
    }

    synchronized private boolean isAllReady()
    {
        if (connectionList.size() == 0)
        {
            return false;
        }
        else
        {
            for (ClientHandler client : connectionList)
            {
                if (!client.isReady())
                {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * broadcast "reset" message
     */
    synchronized public void sendResettingMessage()
    {
        // reset server's current frame
        currFrameId = 0;

        // server should send first rendering message 
        // if all "ok" messages has been received
        okResponsed = false;

        // add client only when server receive "ok" message
        //for (ClientHandler client : clientList)
        for (ClientHandler client : connectionList)
        {
            client.startResetting();
        }
    }

    /**
     * broadcast "render:?" message
     * this methods is only called from other synchronized methods,
     * so it doesn't need to be synchronized, but marking it as such will do no harm
     */
    synchronized private void sendRenderingMessage()
    {
        for (ClientHandler client : clientList)
        {
            client.startRendering(currFrameId);
        }
    }

    protected void comply()
    {
        if (isAllReady())
        {
            if (!okResponsed)
            {
                System.out.println("ServerCore.comply() - All ready, send first rendering");
                sendRenderingMessage();
                okResponsed = true;
            }
            else if (isAllFinished())
            {
                currFrameId++;
                sendRenderingMessage();
            }
        }
    }
}
