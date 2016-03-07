/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.client.core;

import java.net.Socket;
import hydrogenium.util.ConnectionHandler;
import hydrogenium.util.ConnectionWatcher;
import hydrogenium.util.Debug;
import hydrogenium.util.MessageReceiver;
import hydrogenium.util.MessageSender;

public class ServerHandler implements Runnable, ConnectionWatcher
{
    private ClientCore core;
    private int clientId;

    private MessageReceiver receiver;
    private MessageSender sender;
    private ConnectionHandler connectionHandler;

    // stop the thread
    private volatile boolean running = false;

    // indicate whether this ServerHandler quits gracefully
    private volatile boolean finished = false;

    public ServerHandler(ClientCore core, Socket socket, int clientId)
    {
        this.core = core;
        this.clientId = clientId;

        receiver = new MessageReceiver(socket);
        new Thread(receiver).start();

        sender = new MessageSender(socket);
        new Thread(sender).start();

        connectionHandler = new ConnectionHandler(this, receiver, sender);
        new Thread(connectionHandler).start();
    }

    public void run()
    {
        String message;

        Debug.println("ServerHandler is about to run");

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
        Debug.println("ServerHandler is finished");
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
    }

    private boolean isClean()
    {
        return connectionHandler.isDown() && sender.isDown() && receiver.isDown();
    }

    private void sendOkMessage()
    {
        sender.sendMessage("ok:" + clientId);
    }

    private void sendDoneMessage(int frameId)
    {
        sender.sendMessage("done:" + frameId);
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
            if (tokens[0].equalsIgnoreCase("reset"))
            {
                core.resetCore();
                sendOkMessage();
            }
            else if (tokens[0].equalsIgnoreCase("render"))
            {
                if (tokens.length > 1)
                {
                    int frameId = Integer.parseInt(tokens[1]);
                    if (core.updateCore(frameId))
                    {
                        core.complyCore();
                        sendDoneMessage(frameId);
                    }
                }
            }
            else
            {
                Debug.println("Unrecognized message from server");
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
