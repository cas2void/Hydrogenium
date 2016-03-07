/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.server.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import hydrogenium.util.Debug;

public class RequestListener implements Runnable
{
    private ServerCore core;
    private int port;
    private ServerSocket serverSocket;

    public RequestListener(ServerCore core, int port)
    {
        this.core = core;
        this.port = port;

        try
        {
            serverSocket = new ServerSocket(port);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void shutdown()
    {
        if (!serverSocket.isClosed())
        {
            try
            {
                serverSocket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public boolean isDown()
    {
        return serverSocket.isClosed();
    }

    public void run()
    {
        try
        {
            Debug.format("RequestListener start on port %d\n", port);

            while(serverSocket != null && !serverSocket.isClosed())
            {
                Socket clientSocket = serverSocket.accept();
                ClientHandler connection = new ClientHandler(core, clientSocket);
                core.addConnection(connection);
                core.sendResettingMessage();
                Thread t = new Thread(connection);
                t.start();
                Thread.yield();
            }
        }
        catch (SocketException e)
        {
            Debug.println("RequestListener shutdown from outside.");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
