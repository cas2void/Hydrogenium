/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.client.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

abstract public class HFrame extends HSurface
{
    Frame frame;
    GLCanvas canvas;

    public HFrame(InputStream is)
    {
        super(is);
    }

    protected GLAutoDrawable getDrawable()
    {
        return canvas;
    }

    private void initVisualAssets()
    {
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);

        canvas = new GLCanvas(caps);
        canvas.addGLEventListener(this);
        canvas.setFocusable(true);
        canvas.requestFocus();

        frame = new Frame("HFrame");

        // by default, an AWT Frame doesn't do anything when you click
        // the close button; this bit of code will terminate the program when
        // the window is asked to close
        frame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                // Use a dedicate thread to run the stop() to ensure that the
                // animator stops before program exits.
                new Thread()
                {
                    @Override
                    public void run()
                    {
                        shutdown();
                        while (!isDown())
                        {
                            Thread.yield();
                        }
                        System.exit(0);
                    }
                }.start();
            }
        });

        canvas.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                // Use a dedicate thread to run the stop() to ensure that the
                // animator stops before program exits.
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_ESCAPE || 
                    keyCode == KeyEvent.VK_Q || 
                    keyCode == KeyEvent.VK_END || 
                    keyCode == KeyEvent.VK_C && e.isControlDown())
                {
                    new Thread()
                    {
                        @Override
                        public void run()
                        {
                            shutdown();
                            while (!isDown())
                            {
                                Thread.yield();
                            }
                            System.exit(0);
                        }
                    }.start();
                }
            }
        });

        frame.add(canvas);
    }

    protected void initFullscreen()
    {
        initVisualAssets();

        frame.setUndecorated(true);
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }

    protected void initWindow(int windowPosX, int windowPosY, int windowWidth, int windowHeight)
    {
        initVisualAssets();

        canvas.setPreferredSize(new Dimension(windowWidth, windowHeight));
        frame.pack();
        frame.setLocation(windowPosX, windowPosY);
        if (!isStandalone())
        {
            frame.setResizable(false);
        }
        frame.setVisible(true);
    }
}
