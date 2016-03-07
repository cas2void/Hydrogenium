/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.client.gui;

import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import java.io.InputStream;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;

abstract public class HWindow extends HSurface
{
    GLWindow window;

    public HWindow(InputStream is)
    {
        super(is);
    }

    protected GLAutoDrawable getDrawable()
    {
        return window;
    }

    private void initVisualAssets()
    {
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);

        window = GLWindow.create(caps);
        window.addGLEventListener(this);

        // by default, an AWT Frame doesn't do anything when you click
        // the close button; this bit of code will terminate the program when
        // the window is asked to close
        window.addWindowListener(new WindowAdapter()
        {
            public void windowDestroyNotify(WindowEvent e)
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

        window.addKeyListener(new KeyAdapter()
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

        window.setTitle("HWindow");
    }

    protected void initFullscreen()
    {
        initVisualAssets();

        window.setFullscreen(true);
        window.setVisible(true);
    }

    protected void initWindow(int windowPosX, int windowPosY, int windowWidth, int windowHeight)
    {
        initVisualAssets();

        window.setSize(windowWidth, windowHeight);
        window.setPosition(windowPosX, windowPosY);

        window.setVisible(true);
    }
}
