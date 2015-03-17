package edu.ucsb.mobemb.mars;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.Vuforia;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.ucsb.mobemb.mars.SampleApplication.SampleApplicationSession;
import edu.ucsb.mobemb.mars.SampleApplication.utils.CubeShaders;
import edu.ucsb.mobemb.mars.SampleApplication.utils.MeshObject;
import edu.ucsb.mobemb.mars.SampleApplication.utils.SampleUtils;
import edu.ucsb.mobemb.mars.SampleApplication.utils.Teapot;
import edu.ucsb.mobemb.mars.SampleApplication.utils.Texture;

/**
 * Created by Pradeep on 2/19/2015.
 */



public class CloudARRenderer  implements GLSurfaceView.Renderer
{
    SampleApplicationSession vuforiaAppSession;

    private static final float OBJECT_SCALE_FLOAT = 300.0f;

    private int shaderProgramID;
    private int vertexHandle;
    private int normalHandle;
    private int textureCoordHandle;
    private int mvpMatrixHandle;
    private int texSampler2DHandle;

    private Vector<Texture> mTextures;

    private Teapot mTeapot;
    private GLText glText;                             // A GLText Instance

    private CloudAR mActivity;

    Texture myImgTexture;


    static final float planeVertices[] =
        {
                -0.5f, -0.5f, 0.0f, 0.5f, -0.5f, 0.0f, 0.5f, 0.5f, 0.0f, -0.5f, 0.5f, 0.0f,
        };
    static final float planeTexcoords[] =
        {
                0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f
        };
    static final float planeNormals[] =
        {
                0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f
        };
    static final short planeIndices[] =
        {
                0, 1, 2, 0, 2, 3
        };



    public CloudARRenderer(SampleApplicationSession session, CloudAR activity)
    {
        vuforiaAppSession = session;
        mActivity = activity;
    }


    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        // Call function to initialize rendering:
        Log.d("GP","Renderer SurfaceCreated");
        initRendering();

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();
    }


    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
    }


    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl)
    {
        // Call our function to render content
        renderFrame();
    }


    // Function for initializing the renderer.
    private void initRendering()
    {
        // Define clear color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);

//        for (Texture t : mTextures)
//        {
//            Log.d("GP", "Checking Texture t=" + t.mTextureID + " string = "+t.toString());
//            GLES20.glGenTextures(1, t.mTextureID, 0);
//            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
//            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
//            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
//                    t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
//                    GLES20.GL_UNSIGNED_BYTE, t.mData);
//        }

        //GP - rendering text


       myImgTexture = Texture.loadTextureFromText(mActivity, "Testing MARS :) ");
//        myImgTexture = Texture.loadTextureFromApk("TextureTeapotBlue.png",
//                mActivity.getAssets());

        Log.d("GP", "Checking string Texture t=" + myImgTexture.mTextureID + " string = "+myImgTexture.toString());
        GLES20.glGenTextures(1, myImgTexture.mTextureID, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, myImgTexture.mTextureID[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                myImgTexture.mWidth, myImgTexture.mHeight, 0, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, myImgTexture.mData);

//

        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
                CubeShaders.CUBE_MESH_VERTEX_SHADER,
                CubeShaders.CUBE_MESH_FRAGMENT_SHADER);

        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexPosition");
        normalHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexNormal");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "texSampler2D");

//        glText = new GLText(mActivity.getAssets());
//        // Load the font from file (set size + padding), creates the texture
//        // NOTE: after a successful call to this the font is ready for rendering!
//        glText.load("DroidSans.ttf", 14, 2, 2 );  // Create Font (Height: 14 Pixels / X+Y Padding 2 Pixels)
//
//        mTeapot = new Teapot();

        // enable texture + alpha blending
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }


    // The render function.
    private void renderFrame()
    {
        // Clear color and depth buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        // Get the state from Vuforia and mark the beginning of a rendering
        // section
        State state = Renderer.getInstance().begin();



        // Explicitly render the Video Background
        Renderer.getInstance().drawVideoBackground();

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW);  // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW);   // Back camera

        // Did we find any trackables this frame?
        if (state.getNumTrackableResults() > 0)
        {
            // Gets current trackable result
            TrackableResult trackableResult = state.getTrackableResult(0);

            if (trackableResult == null)
            {
                return;
            }

            mActivity.stopFinderIfStarted();

            // Renders the Augmentation View with the 3D Book data Panel
            renderAugmentation(trackableResult);

        }
        else
        {
            mActivity.startFinderIfStopped();
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        Renderer.getInstance().end();
    }


    private void renderAugmentation(TrackableResult trackableResult)
    {
        Matrix44F modelViewMatrix_Vuforia = Tool
                .convertPose2GLMatrix(trackableResult.getPose());
        float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();

        int textureIndex = 0;

        // deal with the modelview and projection matrices
        float[] modelViewProjection = new float[16];
        Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f, OBJECT_SCALE_FLOAT);
        Matrix.scaleM(modelViewMatrix, 0, OBJECT_SCALE_FLOAT,
                OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT);
       // Log.e("GP", "Scaling OBJECT_SCALE_FLOAT = " +OBJECT_SCALE_FLOAT);
        Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
                .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);

 //       glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, modelViewProjection );         // Begin Text Rendering (Set Color WHITE)
//      glText.drawC("Test 3D!", 0f, 0f, 0f, 0, 0, 0);
//        glText.drawC("M", 0f, 40f, 40f, 0, 0, 0);
//        glText.drawC("A", 20f, 40f, 40f, 0, 0, 0);
//        glText.drawC("R", 40f, 40f, 40f, 0, 0, 0);
//        glText.drawC("S", 60f, 40f, 40f, 0, 0, 0);




        //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
//
//        // Set filtering
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE );  // Set U Wrapping
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE );  // Set V Wrapping
//
//        // Load the bitmap into the bound texture.
//        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
//
//        GLES20.glUniform4fv(mColorHandle, 1, color , 0);
//        GLES20.glEnableVertexAttribArray(mColorHandle);
//
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);  // Set the active texture unit to texture unit 0
//
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId); // Bind the texture to this unit
//
//        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0
//        GLES20.glUniform1i(mTextureUniformHandle, 0);
//
//
//

        //GP - end




//        // activate the shader program and bind the vertex/normal/tex coords
//        GLES20.glUseProgram(shaderProgramID);
//        GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false,
//                0, mTeapot.getVertices());
//        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false,
//                0, mTeapot.getNormals());
//        GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT,
//                false, 0, mTeapot.getTexCoords());
//
//        GLES20.glEnableVertexAttribArray(vertexHandle);
//        GLES20.glEnableVertexAttribArray(normalHandle);
//        GLES20.glEnableVertexAttribArray(textureCoordHandle);
//
//        // activate texture 0, bind it, and pass to shader
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
//                mTextures.get(textureIndex).mTextureID[0]);
//        GLES20.glUniform1i(texSampler2DHandle, 0);
//
//        // pass the model view matrix to the shader
//        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
//                modelViewProjection, 0);

        // finally draw the teapot
     //   GLES20.glDrawElements(GLES20.GL_TRIANGLES, mTeapot.getNumObjectIndex(),
        //        GLES20.GL_UNSIGNED_SHORT, mTeapot.getIndices());

        //GP -



        GLES20.glUseProgram(shaderProgramID);

        GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                false, 0, fillBuffer(planeVertices));
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                false, 0, fillBuffer(planeNormals));
        GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                GLES20.GL_FLOAT, false, 0, fillBuffer(planeTexcoords));

        GLES20.glEnableVertexAttribArray(vertexHandle);
        GLES20.glEnableVertexAttribArray(normalHandle);
        GLES20.glEnableVertexAttribArray(textureCoordHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                myImgTexture.mTextureID[0]);
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                modelViewProjection, 0);
        GLES20.glUniform1i(texSampler2DHandle, 0);


       // GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        // Draw the square
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6,
                GLES20.GL_UNSIGNED_SHORT, fillBuffer(planeIndices));
        //GP


        // disable the enabled arrays
        GLES20.glDisableVertexAttribArray(vertexHandle);
        GLES20.glDisableVertexAttribArray(normalHandle);
        GLES20.glDisableVertexAttribArray(textureCoordHandle);

        SampleUtils.checkGLError("CloudReco renderFrame");
    }


    public void setTextures(Vector<Texture> textures)
    {
        mTextures = textures;
    }

    protected Buffer fillBuffer(short[] array)
    {
        // Each short takes 2 bytes
        ByteBuffer bb = ByteBuffer.allocateDirect(2 * array.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (short s : array)
            bb.putShort(s);
        bb.rewind();

        return bb;

    }

    protected Buffer fillBuffer(float[] array)
    {
        // Each float takes 4 bytes
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * array.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (float d : array)
            bb.putFloat(d);
        bb.rewind();

        return bb;

    }

}
