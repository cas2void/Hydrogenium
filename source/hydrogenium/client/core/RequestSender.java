/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.client.core;

import java.io.IOException;
import java.net.Socket;
import hydrogenium.util.Debug;
import hydrogenium.util.LoopJobFramework;

public class RequestSender extends LoopJobFramework
{
    private static final int DEFAULT_FPS = 60;

    private ClientCore core;
    private ServerHandler server;

    private String host;
    private int port;
    private int clientId;

    private boolean connectionEstablished;

    public RequestSender(ClientCore core, String host, int port, int clientId)
    {
        super(DEFAULT_FPS, "RequestSender");
        this.core = core;
        this.host = host;
        this.port = port;
        this.clientId = clientId;

        connectionEstablished = false;
    }

    protected void comply()
    {
        if (!connectionEstablished)
        {
            try
            {
                Socket socket = new Socket(host, port);
                connectionEstablished = true;
                Debug.println(socket.toString());

                server = new ServerHandler(core, socket, clientId);
                new Thread(server).start();
            }
            catch (IOException e)
            {
                Debug.println("Can't connect to server, about to reconnect..");
            }
        }
        else
        {
            if (server.isDown())
            {
                connectionEstablished = false;
            }
        }
    }

    protected void cleanUp()
    {
        if (connectionEstablished)
        {
            server.shutdown();
            while (!server.isDown())
            {
                Thread.yield();
            }
        }

        Debug.println("RequestSender cleaned up");
    }
}
