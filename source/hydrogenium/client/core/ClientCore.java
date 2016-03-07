/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.client.core;

abstract public class ClientCore
{
    private LoopDrivenJob loopDrivenJob;
    private RequestSender requestSender;

    private int fps;
    private String host;
    private int port;
    private int id;

    private int frameId;

    // stop updating and complying when it's about to shutdown
    private boolean aboutToShutdown;

    // indicate whether this core is standalone
    private boolean standalone = true;

    /**
     * method to be overridden for specific logic
     */
    abstract protected void update();
    abstract protected void comply();
    abstract protected void reset();
    abstract protected void cleanUp();
    abstract protected boolean isClean();

    public ClientCore()
    {
        init();
    }

    public ClientCore(int fps)
    {
        this.fps = fps;
        standalone = true;
        init();
    }

    public ClientCore(String host, int port, int id)
    {
        this.host = host;
        this.port = port;
        this.id = id;
        standalone = false;
        init();
    }

    private void init()
    {
        frameId = 0;
        aboutToShutdown = false;
    }

    private void setStandalone(boolean standalone)
    {
        this.standalone = standalone;
    }

    private void setFPS(int fps)
    {
        this.fps = fps;
    }

    private void setHost(String host)
    {
        this.host = host;
    }

    private void setPort(int port)
    {
        this.port = port;
    }

    private void setID(int id)
    {
        this.id = id;
    }

    synchronized public void shutdown()
    {
        aboutToShutdown = true;

        cleanUp();
        if (standalone)
        {
            loopDrivenJob.shutdown();
        }
        else
        {
            requestSender.shutdown();
        }
    }

    public boolean isDown()
    {
        return isClean() && 
            (standalone && loopDrivenJob.isDown() || !standalone && requestSender.isDown());
    }

    public void start()
    {
        if (standalone)
        {
            loopDrivenJob = new LoopDrivenJob(this, fps);
            new Thread(loopDrivenJob).start();
        }
        else
        {
            requestSender = new RequestSender(this, host, port, id);
            new Thread(requestSender).start();
        }
    }

    synchronized public boolean updateCore(int frameId)
    {
        boolean sync = false;

        if (!aboutToShutdown && this.frameId == frameId)
        {
            update();
            this.frameId++;
            sync = true;
        }

        return sync;
    }

    synchronized public void updateCore()
    {
        updateCore(frameId);
    }

    synchronized public void complyCore()
    {
        if (!aboutToShutdown)
        {
            comply();
        }
    }

    synchronized public void resetCore()
    {
        // do it for once
        if (!aboutToShutdown && frameId != 0)
        {
            frameId = 0;
            reset();
        }
    }

    public boolean isStandalone()
    {
        return standalone;
    }
}
