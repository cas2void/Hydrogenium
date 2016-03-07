/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class MessageReceiver implements Runnable
{
    private Socket socket;
    private BufferedReader reader;

    // indicate whether this this thread quits gracefully
    private volatile boolean finished = false;

    private ArrayList<String> messageBuffer;

    public MessageReceiver(Socket socket)
    {
        try
        {
            this.socket = socket;

            InputStreamReader isReader = new InputStreamReader(socket.getInputStream());
            reader = new BufferedReader(isReader);

            messageBuffer = new ArrayList<String>();
            messageBuffer.clear();
        }
        catch (IOException e)
        {
            Debug.println("MessageReceiver constructor error.");
        }
    }

    public void shutdown()
    {
        try
        {
            if (!socket.isClosed() && !socket.isInputShutdown())
            {
                socket.shutdownInput();
            }
        }
        catch (IOException e)
        {
            Debug.println("MessageReceiver shutdown error.");
        }
    }

    public boolean isDown()
    {
        return finished;
    }

    private void cleanUp()
    {
        try
        {
            // close half of the connection
            reader.close();
            socket.close();
        }
        catch (IOException e)
        {
            Debug.println("MessageReceiver cleanup error.");
        }
    }

    private boolean isClean()
    {
        // no way to tell if a BufferedReader has been closed
        return socket.isClosed();
    }

    public void run()
    {
        String message;

        Debug.println("MessageReceiver is about to run");
        finished = false;

        try
        {
            while ((message = reader.readLine()) != null)
            {
                addMessage(message);
                Thread.yield();
            }
        }
        catch (IOException e)
        {
            Debug.println("MessageReceiver's connection is lost");
        }
        finally
        {
            cleanUp();
            while (!isClean())
            {
                Thread.yield();
            }
            finished = true;
            Debug.println("MessageReceiver is finished");
        }
    }

    synchronized private void addMessage(String message)
    {
        messageBuffer.add(message);
        //Debug.println("MessageReceiver: " + messageBuffer.toString());
    }

    /**
     * get the first message in the buffer and remove it 
     * return empty string if the buffer is empty
     */
    synchronized public String getMessage()
    {
        String message = "";

        if (messageBuffer.size() > 0)
        {
            message = messageBuffer.get(0);
            messageBuffer.remove(0);
        }

        //Debug.println("MessageReceiver: " + messageBuffer.toString());

        return message;
    }

    /**
     * get the first message which does not equal the specified message and remove it 
     * return empty string if the buffer is empty
     */
    synchronized public String excludeMessage(String excluder)
    {
        String message = "";
        boolean found = false;

        for (String item : messageBuffer)
        {
            if (!item.equals(excluder))
            {
                message = item;
                found = true;
                break;
            }
        }

        if (found)
        {
            messageBuffer.remove(message);
        }

        //Debug.println("MessageReceiver: " + messageBuffer.toString());

        return message;
    }

    /**
     * find if the buffer contains the specified message
     * remove it if it's present.
     */
    synchronized public boolean pickMessage(String message)
    {
        int index = messageBuffer.indexOf(message);

        if (index > -1)
        {
            messageBuffer.remove(index);
            //Debug.println("MessageReceiver: " + messageBuffer.toString());

            return true;
        }
        else
        {
            return false;
        }
    }
}
