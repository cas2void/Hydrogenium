/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.util;

abstract public class LoopJobFramework implements Runnable
{
    // number of frames with delay of 0 ms before the animated thread yields to others
    private static final int NO_DELAYS_PER_YIELD = 16;

    // number of frames that can be skipped in any one animation loop
    // i.e the games's state is updated but not rendered
    private static final int MAX_FRAME_SKIPS = 5;

    // number of FPS stored to get an average
    private static final int NUM_FPS = 10;

    // unit change
    private static final double S2NS_D = 1000000000.0;
    private static final long S2NS_L = 1000000000L;
    private static final double MS2NS_D = 1000000.0;
    private static final long MS2NS_L = 1000000L;

    // record stats every 1 second(roughly)
    private static final long MAX_STATS_INTERVAL = S2NS_L;

    private long period;

    // stop the thread
    protected volatile boolean running = false;
    // indicates whether resources have been released
    protected volatile boolean finished = false;

    // maintain fps
    private long beforeTime;
    // beforeTime should be restart if just resume from paused
    private volatile boolean justResume = true;

    // used for gathering statistics
    // in nanoseconds
    private long statsInterval = 0L;
    private long prevStatsTime;   
    private long totalElapsedTime = 0L;
    private long frameworkStartTime;
    // in seconds
    protected double timeSpentInGame = 0;

    private long frameCount = 0;
    private double[] fpsStore;
    private long statsCount = 0;
    protected double averageFPS = 0.0;

    private long framesSkipped = 0L;
    private long totalFramesSkipped = 0L;
    private double[] upsStore;
    protected double averageUPS = 0.0;

    // ready to store statistics
    private volatile boolean statsReady = false;

    private String jobName;

    // indicates whether this job should maintain a fixed update rate
    private boolean isFixedRate;

    // pause the loop
    private volatile boolean isPaused;

    // do the real job
    abstract protected void comply();

    public LoopJobFramework(int fps, String jobName)
    {
        initJob(fps, jobName);

        isFixedRate = false;
        isPaused = false;
    }

    public LoopJobFramework(int fps, String jobName, boolean isFixedRate)
    {
        initJob(fps, jobName);

        this.isFixedRate = isFixedRate;

        if (isFixedRate)
        {
            upsStore = new double[NUM_FPS];
            for (int i = 0; i < NUM_FPS; i++)
            {
                upsStore[i] = 0.0;
            }
        }

        isPaused = false;
    }

    public LoopJobFramework(int fps, String jobName, boolean isFixedRate, boolean isPaused)
    {
        initJob(fps, jobName);

        this.isFixedRate = isFixedRate;

        if (isFixedRate)
        {
            upsStore = new double[NUM_FPS];
            for (int i = 0; i < NUM_FPS; i++)
            {
                upsStore[i] = 0.0;
            }
        }

        this.isPaused = isPaused;
    }

    private void initJob(int fps, String jobName)
    {
        double interval = S2NS_D / (double)fps;
        Debug.format("%s - fps: %d; period: %f ms\n", jobName, fps, interval / MS2NS_D);
        period = (long)(interval + 0.5);

        // initialise timing elements
        fpsStore = new double[NUM_FPS];
        for (int i = 0; i < NUM_FPS; i++)
        {
            fpsStore[i] = 0.0;
        }

        this.jobName = jobName;
    }

    public long getPeriod()
    {
        return period;
    }

    /**
    Repeatedly update, render, sleep so loop takes close to period ns.
    Sleep inaccuracies are handled.
    Overruns in update/renders will cause extra updates to be carried out,
    so UPS ~= requested FPS.
    */
    public void run()
    {
        //long beforeTime, afterTime, timeDiff, sleepTime;
        long afterTime, timeDiff, sleepTime;
        long overSleepTime = 0L;
        int noDelays = 0;
        long excess = 0L;

        frameworkStartTime = System.nanoTime();
        //beforeTime = frameworkStartTime;

        running = true;
        finished = false;
        while (running)
        {
            if (isPaused)
            {
                continue;
            }
            else
            {
                restartClock();
            }

            // do the work
            if (isFixedRate)
            {
                update();
            }
            comply();

            // keep fps steady
            afterTime = System.nanoTime();
            timeDiff = afterTime - beforeTime;
            sleepTime = (period - timeDiff) - overSleepTime;

            if (sleepTime > 0)
            {
                try
                {
                    // sleep a bit in millisecond
                    Thread.sleep((long)((double)sleepTime / MS2NS_D + 0.5));
                }
                catch (InterruptedException ex)
                {}

                overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
            }
            else
            {
                // sleepTime <= 0; frame took longer than the period
                overSleepTime = 0L;

                if (isFixedRate)
                {
                    // store excess time value
                    excess -= sleepTime;
                }

                if (++noDelays >= NO_DELAYS_PER_YIELD)
                {
                    // give another thread a chance to run
                    Thread.yield();
                    noDelays = 0;
                }
            }

            beforeTime = System.nanoTime();

            if (isFixedRate)
            {
                // if frame animation is taking too long, update the game state without rendering it,
                // to get the updates/sec nearer to the required FPS
                int skips = 0;
                while (excess > period && skips < MAX_FRAME_SKIPS)
                {
                    excess -= period;
                    // udpate state without rendering
                    update();
                    skips++;
                }

                framesSkipped += skips;
            }

            storeStats();
        }

        printStats();

        // do the housekeeping
        cleanUp();
        // we're about to quit
        while (!isClean())
        {
            Thread.yield();
        }

        finished = true;
    }

    private void startStats()
    {
        if (!Debug.STATS)
        {
            return;
        }

        statsInterval = 0L;
        prevStatsTime = System.nanoTime();
        totalElapsedTime = 0L;
        frameCount = 0L;
    }

    private void storeStats()
    {
        if (!Debug.STATS)
        {
            return;
        }

        ++frameCount;
        statsInterval += period;

        if (statsInterval >= MAX_STATS_INTERVAL)
        {
            long timeNow = System.nanoTime();
            timeSpentInGame = (double)(timeNow - frameworkStartTime) / S2NS_D;

            // time since last stats collection
            long realElapsedTime = timeNow - prevStatsTime;
            totalElapsedTime += realElapsedTime;

            double timingErr = (double)(realElapsedTime - statsInterval) / (double)statsInterval * 100.0;

            totalFramesSkipped += framesSkipped;

            double actualFPS = 0.0;

            if (totalElapsedTime > 0)
            {
                actualFPS = (double)frameCount / totalElapsedTime * S2NS_D;
            }

            // store the latest FPS and UPS
            fpsStore[(int)statsCount % NUM_FPS] = actualFPS;

            double totalFPS = 0.0;
            for (int i = 0; i < NUM_FPS; i++)
            {
                totalFPS += fpsStore[i];
            }

            if (statsCount + 1 == 0)
            {
                averageFPS = 0;
            }
            else if (statsCount + 1 < NUM_FPS)
            {
                averageFPS = totalFPS / (statsCount + 1);
            }
            else
            {
                averageFPS = totalFPS / NUM_FPS;
            }

            if (isFixedRate)
            {
                double actualUPS = 0.0;

                if (totalElapsedTime > 0)
                {
                    actualUPS = (double)(frameCount + totalFramesSkipped) / totalElapsedTime * S2NS_D;
                }

                upsStore[(int)statsCount % NUM_FPS] = actualUPS;

                double totalUPS = 0.0;
                for (int i = 0; i < NUM_FPS; i++)
                {
                    totalUPS += upsStore[i];
                }

                if (statsCount + 1 == 0)
                {
                    averageUPS = 0;
                }
                else if (statsCount + 1 < NUM_FPS)
                {
                    averageUPS = totalUPS / (statsCount + 1);
                }
                else
                {
                    averageUPS = totalUPS / NUM_FPS;
                }

                if (Debug.FPS)
                {
                    Debug.format("%.04fs %.04fs %+.02f%% %4dc %d/%d skips %.02f %.02f afps; %.02f %.02f aups\n", 
                                 (double)statsInterval / S2NS_D, (double)realElapsedTime / S2NS_D, 
                                 timingErr, frameCount, framesSkipped, totalFramesSkipped, 
                                 actualFPS, averageFPS, actualUPS, averageUPS);
                }

                framesSkipped = 0;
            }
            else
            {
                if (Debug.FPS)
                {
                    Debug.format("%.04fs %.04fs %+.02f%% %4dc %.02f %.02f afps\n", 
                                 (double)statsInterval / S2NS_D, (double)realElapsedTime / S2NS_D, 
                                 timingErr, frameCount, actualFPS, averageFPS);
                }
            }

            ++statsCount;

            prevStatsTime = timeNow;
            statsInterval = 0L;
        }
    }

    private void printStats()
    {
        if (!Debug.STATS)
        {
            return;
        }

        System.out.format("\n%s: \n", jobName);
        if (isFixedRate)
        {
            System.out.format("\tFrame Count/Loss: %d / %d\n", frameCount, totalFramesSkipped);
        }
        else
        {
            System.out.format("\tFrame Count: %d\n", frameCount);
        }
        System.out.format("\tAverage FPS: %.02f\n", averageFPS);
        if (isFixedRate)
        {
            System.out.format("\tAverage UPS: %.02f\n", averageUPS);
        }
	System.out.format("\tTime Spent: %.02f secs\n", timeSpentInGame);
    }

    synchronized private void restartClock()
    {
        if (justResume)
        {
            startStats();
            beforeTime = System.nanoTime();
            justResume = false;
        }
    }

    public void pause()
    {
        isPaused = true;
    }

    synchronized public void resume()
    {
        isPaused = false;
        justResume = true;
    }

    public boolean isPaused()
    {
        return isPaused;
    }

    protected void update()
    {
    }

    protected void cleanUp()
    {
    }

    protected boolean isClean()
    {
        return true;
    }

    public void shutdown()
    {
        running = false;
    }

    public boolean isDown()
    {
        return finished;
    }
}
