/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.util;

public class ConnectionHandler extends LoopJobFramework
{
    public static final String DEFAULT_HEARTBEAT_MESSAGE = "heartbeat";

    private static final int DEFAULT_FPS = 30;
    private static final int MAX_LOST_HEARTBEAT = 5;

    // counter indicates how many loops without hearing any heartbeat
    private int lostHeartbeatCount;
    // last time in nano seconds got heart beat
    // don't tick the counter if the interval is less than period
    private long lastTimeHeartbeatReceived;

    private ConnectionWatcher watcher;
    private MessageReceiver receiver;
    private MessageSender sender;

    public ConnectionHandler(ConnectionWatcher watcher, 
                             MessageReceiver receiver, MessageSender sender)
    {
        super(DEFAULT_FPS, "ConnectionHandler");

        init(watcher, receiver, sender);
    }

    public ConnectionHandler(ConnectionWatcher watcher, int fps, 
                             MessageReceiver receiver, MessageSender sender)
    {
        super(fps, "ConnectionHandler");

        init(watcher, receiver, sender);
    }

    private void init(ConnectionWatcher watcher, MessageReceiver receiver, MessageSender sender)
    {
        lostHeartbeatCount = 0;
        lastTimeHeartbeatReceived = System.nanoTime();

        this.watcher = watcher;
        this.receiver = receiver;
        this.sender = sender;
    }

    protected void comply()
    {
        if (lostHeartbeatCount > MAX_LOST_HEARTBEAT)
        {
            watcher.connectionLost();
        }
        else
        {
            if (receiver.pickMessage(DEFAULT_HEARTBEAT_MESSAGE))
            {
                lostHeartbeatCount = 0;
                lastTimeHeartbeatReceived = System.nanoTime();
                Debug.println("got heartbeat");
            }
            else
            {
                // increase heartbeat counter if the interval is longer than period
                if (System.nanoTime() - lastTimeHeartbeatReceived > getPeriod())
                {
                    lostHeartbeatCount++;
                }
            }

            Debug.println("ConnectionHandler.comply() - lostHeartbeatCount: " + lostHeartbeatCount);

            sender.sendMessage(DEFAULT_HEARTBEAT_MESSAGE);
        }
    }
}
