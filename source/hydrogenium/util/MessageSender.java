/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class MessageSender implements Runnable
{
    private Socket socket;
    private PrintWriter writer;

    // stop the thread
    private volatile boolean running = false;

    // indicate whether this this thread quits gracefully
    private volatile boolean finished = false;

    private ArrayList<String> messageBuffer;

    public MessageSender(Socket socket)
    {
        try
        {
            this.socket = socket;

            writer = new PrintWriter(socket.getOutputStream(), true);

            messageBuffer = new ArrayList<String>();
        }
        catch (IOException e)
        {
            Debug.println("MessageSender constructor error.");
        }
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
        writer.close();
    }

    private boolean isClean()
    {
        // no way to tell if a PrintWriter has been closed
        return true;
    }

    public void run()
    {
        String message;

        Debug.println("MessageSender is about to run");
        running = true;
        finished = false;

        while (running)
        {
            transferMessage();
            Thread.yield();
        }

        cleanUp();
        while (!isClean())
        {
            Thread.yield();
        }
        finished = true;
        Debug.println("MessageSender is finished");
    }

    /** 
     * get the first message in the buffer and send it
     * then remove it from the buffer
     */
    synchronized private void transferMessage()
    {
        if (messageBuffer.size() > 0)
        {
            writer.println(messageBuffer.get(0));
            messageBuffer.remove(0);
        }
    }

    synchronized public void sendMessage(String message)
    {
        messageBuffer.add(message);
        //Debug.println("MessageSender: " + messageBuffer.toString());
    }
}
