/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.test;

import hydrogenium.client.gui.HFrame;
import java.io.InputStream;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import static javax.media.opengl.GL2.*;

public class TestHFrame extends HFrame
{
    private static final String DEFAULT_CONFIG_FILE = "client.xml";
    private double theta = 0.0;
    private double s;
    private double c;

    public TestHFrame(InputStream is)
    {
        super(is);
    }

    protected void prepare(GL2 gl)
    {}

    protected void update()
    {
        theta += 0.01;
        s = Math.sin(theta);
        c = Math.cos(theta);
    }

    protected void render(GL2 gl, GLU glu)
    {
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClear(GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        gl.glTranslatef(0.0f, 0.0f, -3.0f);
        gl.glBegin(GL_TRIANGLES);
        gl.glColor3f(1, 0, 0);
        gl.glVertex2d(-c, -c);
        gl.glColor3f(0, 1, 0);
        gl.glVertex2d(s, -s);
        gl.glColor3f(0, 0, 1);
        gl.glVertex2d(0, c);
        gl.glEnd();
    }

    protected void reset()
    {
        theta = 0.0;
    }

    protected void cleanUp()
    {
        System.out.println("HFrame.cleanUp()");
    }

    protected boolean isClean()
    {
        return true;
    }

    private static void printHelpMessage()
    {}

    public static void main(String[] args)
    {
        String configFile = DEFAULT_CONFIG_FILE;

        // Collect command line args
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].contains("-C"))
            {
                configFile = args[i].substring(2);
            } 
            else
            {
                printHelpMessage();
            }
        }

        new TestHFrame(TestHFrame.class.getResourceAsStream("config/" + configFile));
    }
}
