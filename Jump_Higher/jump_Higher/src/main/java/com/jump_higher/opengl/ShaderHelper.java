package com.jump_higher.opengl;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

/**
 * @author Stav Bodik
 * This class used to load/read shader files for OpenGL program.
 */
public class ShaderHelper
{
	private static final String TAG = "ShaderHelper";
	
	/** 
	 * Helper function to compile a shader.
	 * 
	 * @param shaderType The shader type.
	 * @param shaderSource The shader source code.
	 * @return An OpenGL handle to the shader.
	 */
	public static int compileShader(final int shaderType, final String shaderSource) 
	{
		int shaderHandle = GLES20.glCreateShader(shaderType);

		if (shaderHandle != 0) 
		{
			// Pass in the shader source.
			GLES20.glShaderSource(shaderHandle, shaderSource);

			// Compile the shader.
			GLES20.glCompileShader(shaderHandle);

			// Get the compilation status.
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

			// If the compilation failed, delete the shader.
			if (compileStatus[0] == 0) 
			{
				Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderHandle));
				GLES20.glDeleteShader(shaderHandle);
				shaderHandle = 0;
			}
		}

		if (shaderHandle == 0)
		{			
			throw new RuntimeException("Error creating shader.");
		}
		
		return shaderHandle;
	}
	
	/**
	 * Helper function to compile and link a program.
	 * 
	 * @param vertexShaderHandle An OpenGL handle to an already-compiled vertex shader.
	 * @param fragmentShaderHandle An OpenGL handle to an already-compiled fragment shader.
	 * @param attributes Attributes that need to be bound to the program.
	 * @return An OpenGL handle to the program.
	 */
	public static int createAndLinkProgram(int vertexShaderHandle,int fragmentShaderHandle, String[] attributes,String from) 
	{
		int programHandle = GLES20.glCreateProgram();
		Log.e(TAG, "______________________"+from+"___________________________");

		if (programHandle != 0) 
		{
			Log.e(TAG, "glAttachShader vertex Before");
			// Bind the vertex shader to the program.
			GLES20.glAttachShader(programHandle, vertexShaderHandle);	
			Log.e(TAG, "glAttachShader vertex after");

			Log.e(TAG, "glAttachShader fragment Before");
			// Bind the fragment shader to the program.
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);
			Log.e(TAG, "glAttachShader fragment after");

			// Bind attributes
			if (attributes != null)
			{
				final int size = attributes.length;
				for (int i = 0; i < size; i++)
				{
					GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
				}						
			}
			
			
			// Link the two shaders together into a program.
			GLES20.glLinkProgram(programHandle);

			Log.e(TAG, "glLinkProgram " + programHandle);

			// Get the link status.
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

			Log.e(TAG, "linkStatus " + linkStatus[0]);

			
			// If the link failed, delete the program.
			if (linkStatus[0] == 0) 
			{			
				Log.e(TAG, "Error createAndLinkProgram compiling program: " + GLES20.glGetProgramInfoLog(programHandle));
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}
		}
		
		if (programHandle == 0)
		{
			throw new RuntimeException("Error createAndLinkProgram creating program.");
		}
		
		return programHandle;
	}
	
	public static int buildProgram(String vertexShaderSource, String fragmentShaderSource, String attributes[]){
		int program;
		int vertexShader = compileShader(GLES20.GL_VERTEX_SHADER,vertexShaderSource);
		int fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER,fragmentShaderSource);
		program = createAndLinkProgram(vertexShader, fragmentShader, attributes,"buildProgram");
		
		return program;
	}
	
	public static String readTextFileFromRawResource(final Context context,final int resourceId)
	{
		final InputStream inputStream = context.getResources().openRawResource(
				resourceId);
		final InputStreamReader inputStreamReader = new InputStreamReader(
				inputStream);
		final BufferedReader bufferedReader = new BufferedReader(
				inputStreamReader);

		String nextLine;
		final StringBuilder body = new StringBuilder();

		try
		{
			while ((nextLine = bufferedReader.readLine()) != null)
			{
				body.append(nextLine);
				body.append('\n');
			}
		}
		catch (IOException e){
			e.printStackTrace();
			return null;
		}

		return body.toString();
	}

}
