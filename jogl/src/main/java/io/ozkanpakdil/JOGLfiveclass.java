package io.ozkanpakdil;


import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;

import javax.swing.*;

import java.io.IOException;

import static com.jogamp.opengl.GL4.GL_POINTS;

public class JOGLfiveclass extends JFrame implements GLEventListener {

    private GLCanvas myCanvas;
    private int renderingProgram;
    private int vao[] = new int[1];

    public JOGLfiveclass() {
        setTitle("Test");
        setSize(600, 600);
        myCanvas = new GLCanvas();
        myCanvas.addGLEventListener(this);
        this.add(myCanvas);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        System.setProperty("jogl.disable.openglcore", "false");
        JOGLfiveclass current_instance = new JOGLfiveclass();
    }

    @Override
    public void display(GLAutoDrawable arg0) {
        // TODO Auto-generated method stub
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glUseProgram(renderingProgram);
        gl.glPointSize(50f);
        gl.glDrawArrays(GL_POINTS, 0, 1);
    }

    @Override
    public void dispose(GLAutoDrawable arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void init(GLAutoDrawable arg0) {
        // TODO Auto-generated method stub
        GL4 gl = (GL4) GLContext.getCurrentGL();
        try {
            this.renderingProgram = Utils.createShaderProgram("vShaderSourceFive.glsl", "fShaderSourceFive.glsl");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        gl.glGenVertexArrays(vao.length, vao, 0);
        gl.glBindVertexArray(vao[0]);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        // TODO Auto-generated method stub
    }
}