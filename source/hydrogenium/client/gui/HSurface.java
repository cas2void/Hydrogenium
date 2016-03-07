/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.client.gui;

import hydrogenium.client.core.ClientCore;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.nio.IntBuffer;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import static javax.media.opengl.GL2.*;

abstract public class HSurface extends ClientCore implements GLEventListener
{
    private static final boolean DEFAULT_STANDALONE = true;
    private static final boolean DEFAULT_FULLSCREEN = false;
    private static final int DEFAULT_FPS = 30;
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5000;
    private static final int DEFAULT_ID = 1;
    private static final int DEFAULT_GLOBAL_WIDTH = 800;
    private static final int DEFAULT_GLOBAL_HEIGHT = 300;
    private static final int DEFAULT_LOCAL_WIDTH = 400;
    private static final int DEFAULT_LOCAL_HEIGHT = 300;
    private static final int DEFAULT_LOCAL_POS_X = 0;
    private static final int DEFAULT_LOCAL_POS_Y = 0;
    private static final int DEFAULT_WINDOW_POS_X = 50;
    private static final int DEFAULT_WINDOW_POS_Y = 50;

    // OpenGL global scene size
    private int globalWidth;
    private int globalHeight;

    // OpenGL local scene size
    private int localWidth;
    private int localHeight;

    // local scene's position in global scene
    private int localPosX;
    private int localPosY;

    // window size
    private int windowWidth;
    private int windowHeight;

    // window position in global scene
    private int windowPosX;
    private int windowPosY;

    private int fbo;
    private int rbo;

    private IntBuffer frameBuf;
    private IntBuffer renderBuf;

    private GLU glu;

    abstract protected GLAutoDrawable getDrawable();
    abstract protected void initFullscreen();
    abstract protected void initWindow(int windowPosX, int windowPosY, int windowWidth, int windowHeight);
    abstract protected void prepare(GL2 gl);
    abstract protected void render(GL2 gl, GLU glu);

    public HSurface(InputStream is)
    {
        super();

        // set default value
        boolean isStandalone = DEFAULT_STANDALONE;
        boolean isFullscreen = DEFAULT_FULLSCREEN;
        int fps = DEFAULT_FPS;
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        int id = DEFAULT_ID;

        globalWidth = DEFAULT_GLOBAL_WIDTH;
        globalHeight = DEFAULT_GLOBAL_HEIGHT;

        localWidth = DEFAULT_LOCAL_WIDTH;
        localHeight = DEFAULT_LOCAL_HEIGHT;

        localPosX = DEFAULT_LOCAL_POS_X;
        localPosY = DEFAULT_LOCAL_POS_Y;

        windowWidth = DEFAULT_LOCAL_WIDTH;
        windowHeight = DEFAULT_LOCAL_HEIGHT;

        windowPosX = DEFAULT_WINDOW_POS_X;
        windowPosY = DEFAULT_WINDOW_POS_Y;

        // get from config file
        Config config = new Config();
        if (!config.load(is))
        {
            System.out.println("Invalid config file. Use default value.");
        }
        else
        {
            isStandalone = config.getIsStandalone();
            isFullscreen = config.getIsFullscreen();
            fps = config.getFPS();
            host = config.getHost();
            port = config.getPort();
            id = config.getID();

            globalWidth = config.getGlobalWidth();
            globalHeight = config.getGlobalHeight();

            localWidth = config.getLocalWidth();
            localHeight = config.getLocalHeight();

            localPosX = config.getLocalPosX();
            localPosY = config.getLocalPosY();

            windowWidth = config.getWindowWidth();
            windowHeight = config.getWindowHeight();

            windowPosX = config.getWindowPosX();
            windowPosY = config.getWindowPosY();
        }

        setStandalone(isStandalone);
        if (isStandalone)
        {
            setFPS(fps);
        }
        else
        {
            setHost(host);
            setPort(port);
            setID(id);
        }

        if (isFullscreen)
        {
            initFullscreen();
        }
        else
        {
            initWindow(windowPosX, windowPosY, windowWidth, windowHeight);
        }

        startCore();
    }

    private void startCore()
    {
        // Start ClientCore after GL initialization is finished
        while (!getDrawable().getGLEventListenerInitState(this))
        {
            Thread.yield();
        }

        start();
    }

    protected void comply()
    {
        getDrawable().display();
    }

    public void init(GLAutoDrawable drawable)
    {
        GL2 gl = drawable.getGL().getGL2();      // get the OpenGL graphics context

        glu = new GLU();

        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // set background (clear) color

        if (!isStandalone())
        {
            fbo = genFBO(gl);
            gl.glBindFramebuffer(GL_FRAMEBUFFER, fbo);

            rbo = genRBO(gl);
            gl.glBindRenderbuffer(GL_RENDERBUFFER, rbo);
            gl.glRenderbufferStorage(GL_RENDERBUFFER, GL_RGBA, globalWidth, globalHeight);
            gl.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, rbo);
        }

        prepare(gl);
    }

    public void dispose(GLAutoDrawable drawable)
    {
        GL2 gl = drawable.getGL().getGL2();      // get the OpenGL graphics context

        if (!isStandalone())
        {
            gl.glDeleteRenderbuffers(1, renderBuf);
            gl.glDeleteFramebuffers(1, frameBuf);
        }
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
    {
        GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context
 
        this.windowWidth = width;
        this.windowHeight = height;

        // render buffer will keep its size
        //gl.glRenderbufferStorage(GL_RENDERBUFFER, GL_RGBA, width, height);
 
        // Set the view port (display area) to cover the entire window
        float aspect;
        if (!isStandalone())
        {
            gl.glViewport(0, 0, globalWidth, globalHeight);

            if (globalHeight == 0)
            {
                globalHeight = 1;   // prevent divide by zero
            }
            aspect = (float)globalWidth / (float)globalHeight;
        }
        else
        {
            gl.glViewport(0, 0, windowWidth, windowHeight);

            if (windowHeight == 0)
            {
                windowHeight = 1;   // prevent divide by zero
            }
            aspect = (float)windowWidth / (float)windowHeight;
        }
 
        // Setup perspective projection, with aspect ratio matches viewport
        gl.glMatrixMode(GL_PROJECTION);  // choose projection matrix
        gl.glLoadIdentity();             // reset projection matrix
        glu.gluPerspective(45.0, aspect, 0.1, 100.0); // fovy, aspect, zNear, zFar
 
        // Enable the model-view transform
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity(); // reset
    }

    public void display(GLAutoDrawable drawable)
    {
        GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context

        if (!isStandalone())
        {
            // bind read and write framebuffer to fbo
            gl.glBindFramebuffer(GL_FRAMEBUFFER, fbo);
            int status = gl.glCheckFramebufferStatus(GL_FRAMEBUFFER);
            if (status != GL_FRAMEBUFFER_COMPLETE)
            {
                System.out.println("frame buffer error");
                return;
            }
        }
        // actual rendering
        render(gl, glu);

        if (!isStandalone())
        {
            // bind read framebuffer to fbo
            gl.glBindFramebuffer(GL_READ_FRAMEBUFFER, fbo);
            // bind write framebuffer to screen
            gl.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            gl.glClear(GL_COLOR_BUFFER_BIT);
            gl.glBlitFramebuffer(localPosX, localPosY, localPosX + localWidth, localPosY + localHeight, 
                                 0, 0, windowWidth, windowHeight, GL_COLOR_BUFFER_BIT, GL_NEAREST);
        }
    }

    private int genFBO(GL2 gl)
    {
        int[] array = new int[1];
        frameBuf = IntBuffer.wrap(array);
        gl.glGenFramebuffers(1, frameBuf);
        return frameBuf.get(0);
    }

    private int genRBO(GL2 gl)
    {
        int[] array = new int[1];
        renderBuf = IntBuffer.wrap(array);
        gl.glGenRenderbuffers(1, renderBuf);
        return renderBuf.get(0);
    }
}
