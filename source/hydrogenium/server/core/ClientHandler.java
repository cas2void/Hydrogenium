/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.server.core;

import java.net.Socket;
import hydrogenium.util.ConnectionHandler;
import hydrogenium.util.ConnectionWatcher;
import hydrogenium.util.Debug;
import hydrogenium.util.MessageReceiver;
import hydrogenium.util.MessageSender;

public class ClientHandler implements Runnable, ConnectionWatcher
{
    private ServerCore core;

    private int clientId;
    private String clientName;

    // flag indicates whether the client's rendering job has been finished
    private volatile boolean renderingFinished = false;
    // flag indicates whether the client's state has been reset or synced
    // init with false, the client should be ready after "ok" message received
    private volatile boolean ready = false;

    private MessageReceiver receiver;
    private MessageSender sender;
    private ConnectionHandler connectionHandler;

    // stop the thread
    private volatile boolean running = false;

    // indicate whether this this thread quits gracefully
    private volatile boolean finished = false;

    public ClientHandler(ServerCore core, Socket clientSocket)
    {
        this.core = core;

        clientId = -1;
        clientName = clientSocket.getRemoteSocketAddress().toString();

        receiver = new MessageReceiver(clientSocket);
        new Thread(receiver).start();

        sender = new MessageSender(clientSocket);
        new Thread(sender).start();

        connectionHandler = new ConnectionHandler(this, receiver, sender);
        new Thread(connectionHandler).start();
    }

    public int getId()
    {
        return clientId;
    }

    public String getName()
    {
        return clientName;
    }

    public void printClient()
    {
        System.out.format("clientId: %d, clientName: %s\n", clientId, clientName);
    }

    public void startRendering(int frameId)
    {
        renderingFinished = false;
        sender.sendMessage("render:" + frameId);
    }

    private void finishRendering(int frameId)
    {
        if (core.getCurrFrameId() == frameId)
        {
            renderingFinished = true;
            Debug.println(clientId + " finished: " + frameId + " - " + renderingFinished);
        }
        else
        {
            renderingFinished = false;
            Debug.println(clientId + " finished: " + frameId + " - " + renderingFinished);
            //Debug.println(clientId + " finished untouched: " + renderingFinished);
        }
    }

    public boolean isRenderingFinished()
    {
        return renderingFinished;
    }

    public void startResetting()
    {
        ready = false;
        sender.sendMessage("reset");
    }

    public void finishResetting()
    {
        ready = true;
    }

    public boolean isReady()
    {
        return ready;
    }

    public void shutdown()
    {
        running = false;
    }

    public boolean isDown()
    {
        return finished;
    }

    private void cleanUp()
    {
        connectionHandler.shutdown();
        sender.shutdown();
        receiver.shutdown();

        core.delClient(this);
        core.delConnection(this);
    }

    private boolean isClean()
    {
        return connectionHandler.isDown() && sender.isDown() && receiver.isDown();
    }

    public void run()
    {
        String message;

        Debug.println(clientName + " is about to run");

        running = true;
        finished = false;

        while (running && !receiver.isDown())
        {
            // only parse message not heartbeat
            message = receiver.excludeMessage(ConnectionHandler.DEFAULT_HEARTBEAT_MESSAGE);
            parseMessage(message);
            Thread.yield();
        }

        cleanUp();
        while (!isClean())
        {
            Thread.yield();
        }
        finished = true;
        Debug.println(clientName + " is finished");
    }

    private void parseMessage(String message)
    {
        if (message.length() <= 0)
        {
            return;
        }

        Debug.println("read " + message);

        String[] tokens = message.split(":");
        if (tokens.length > 0)
        {
            if (tokens[0].equalsIgnoreCase("ok"))
            {
                if (tokens.length > 1)
                {
                    clientId = Integer.parseInt(tokens[1]);

                    // clients send "ok" after they receive "reset"
                    core.addClient(this);
                    finishResetting();
                }
            }
            else if (tokens[0].equalsIgnoreCase("disconnect"))
            {
                core.delClient(this);
            }
            else if (tokens[0].equalsIgnoreCase("done"))
            {
                if (tokens.length > 1)
                {
                    int frameId = Integer.parseInt(tokens[1]);
                    finishRendering(frameId);
                }
            }
            else
            {
                Debug.format("Unrecognized message [%s] from client: %s\n", message, clientName);
            }
        }
    }

    /**
     * implements interface ConnectionWatcher
     */
    public void connectionLost()
    {
        shutdown();
    }
}
