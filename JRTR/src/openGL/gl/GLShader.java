package openGL.gl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL3;

import openGL.interfaces.Shader;


/**
 * Manages OpenGL shaders.
 */
public class GLShader implements Shader {

	private GL3 gl; // The OpenGL context
	private int p; // The shader identifier

	public GLShader(GL3 gl) {
		this.gl = gl;
	}

	/**
	 * Utility method. Returns the vertex/fragment shader info log as a string.
	 */
	private String getCompilerOutputShader(int shaderObject) {
		IntBuffer ib = ByteBuffer.allocateDirect(4)
				.order(ByteOrder.nativeOrder()).asIntBuffer();
		ib.rewind();

		gl.glGetShaderiv(shaderObject, GL2ES2.GL_INFO_LOG_LENGTH, ib);
		int logLenght = ib.get(0);
		if (logLenght <= 2)
			return " - No compiler messages.";

		ib.rewind();
		ByteBuffer log = ByteBuffer.allocateDirect(logLenght);
		gl.glGetShaderInfoLog(shaderObject, logLenght, ib, log);

		byte[] infoBytes = new byte[logLenght - 1]; // ignore \0-character of
													// c-string
		log.get(infoBytes);
		return new String(infoBytes);
	}

	/**
	 * Utility method. Returns the shader program compile info log as a string.
	 */
	private String getLinkerOutput(int shaderObject) {
		IntBuffer ib = ByteBuffer.allocateDirect(4)
				.order(ByteOrder.nativeOrder()).asIntBuffer();
		ib.rewind();

		gl.glGetProgramiv(shaderObject, GL2ES2.GL_INFO_LOG_LENGTH, ib);
		int logLenght = ib.get(0);
		if (logLenght <= 2)
			return " - No linker messgaes.";

		ib.rewind();
		ByteBuffer log = ByteBuffer.allocateDirect(logLenght);
		gl.glGetProgramInfoLog(shaderObject, logLenght, ib, log);

		byte[] infoBytes = new byte[logLenght - 1]; // ignore \0-character of
													// c-string
		log.get(infoBytes);
		return new String(infoBytes);
	}

	/**
	 * Load the vertex and fragment shader programs from a file.
	 */
	@Override
	public void load(String vertexFileName, String fragmentFileName)
			throws Exception {
		int v, f;

		v = gl.glCreateShader(GL2ES2.GL_VERTEX_SHADER);
		f = gl.glCreateShader(GL2ES2.GL_FRAGMENT_SHADER);

		String vsrc[] = new String[1];
		String fsrc[] = new String[1];

		BufferedReader brv = new BufferedReader(new FileReader(vertexFileName));
		vsrc[0] = "";
		String line;
		while ((line = brv.readLine()) != null) {
			vsrc[0] += line + "\n";
		}
		brv.close();

		BufferedReader brf = new BufferedReader(
				new FileReader(fragmentFileName));
		fsrc[0] = "";
		while ((line = brf.readLine()) != null) {
			fsrc[0] += line + "\n";
		}
		brf.close();

		gl.glShaderSource(v, 1, vsrc, (int[]) null, 0);
		gl.glCompileShader(v);

		System.out.println("Vertex shader output:\n"
				+ this.getCompilerOutputShader(v));

		gl.glShaderSource(f, 1, fsrc, (int[]) null, 0);
		gl.glCompileShader(f);

		System.out.println("Fragment shader output:\n"
				+ this.getCompilerOutputShader(f));

		p = gl.glCreateProgram();
		gl.glAttachShader(p, v);
		gl.glAttachShader(p, f);
		gl.glLinkProgram(p);
		// gl.glValidateProgram(p); //returns error when no texture stage bound

		System.out.println("Linker output:\n" + this.getLinkerOutput(p));

		int[] status = new int[1];
		gl.glGetShaderiv(v, GL2ES2.GL_COMPILE_STATUS, status, 0);
		if (status[0] == GL.GL_FALSE) {
			throw new Exception("Could not compile vertex shader.");
		}
		gl.glGetShaderiv(f, GL2ES2.GL_COMPILE_STATUS, status, 0);
		if (status[0] == GL.GL_FALSE) {
			throw new Exception("Could not compile fragment shader.");
		}

		IntBuffer ib = ByteBuffer.allocateDirect(4)
				.order(ByteOrder.nativeOrder()).asIntBuffer();
		ib.rewind();
		gl.glGetProgramiv(p, GL2ES2.GL_LINK_STATUS, ib);
		if (ib.get(0) == GL.GL_FALSE) {
			throw new Exception("Could not link vertex and fragment shader.");
		}
	}
	
	/**
	 * Load vertex, fragment and geometry shader from files
	 * @param vertexFileName
	 * @param fragmentFileName
	 * @param geometryFileName
	 * @throws Exception
	 */
	public void load(String vertexFileName, String fragmentFileName,
			String geometryFileName)
			throws Exception {
		int v, f, g;

		v = gl.glCreateShader(GL2ES2.GL_VERTEX_SHADER);
		f = gl.glCreateShader(GL2ES2.GL_FRAGMENT_SHADER);
		g = gl.glCreateShader(GL3.GL_GEOMETRY_SHADER);

		String vsrc[] = new String[1];
		String fsrc[] = new String[1];
		String gsrc[] = new String[1]; 

		BufferedReader brv = new BufferedReader(new FileReader(vertexFileName));
		vsrc[0] = "";
		String line;
		while ((line = brv.readLine()) != null) {
			vsrc[0] += line + "\n";
		}
		brv.close();

		BufferedReader brf = new BufferedReader(
				new FileReader(fragmentFileName));
		fsrc[0] = "";
		while ((line = brf.readLine()) != null) {
			fsrc[0] += line + "\n";
		}
		brf.close();
		
		BufferedReader brg = new BufferedReader(
				new FileReader(geometryFileName));
		gsrc[0] = "";
		while ((line = brg.readLine()) != null) {
			gsrc[0] += line + "\n";
		}
		brg.close();

		gl.glShaderSource(v, 1, vsrc, (int[]) null, 0);
		gl.glCompileShader(v);

		System.out.println("Vertex shader output:\n"
				+ this.getCompilerOutputShader(v));

		gl.glShaderSource(f, 1, fsrc, (int[]) null, 0);
		gl.glCompileShader(f);

		System.out.println("Fragment shader output:\n"
				+ this.getCompilerOutputShader(f));
		
		gl.glShaderSource(g, 1, gsrc, (int[]) null, 0);
		gl.glCompileShader(g);

		System.out.println("Geometry shader output:\n"
				+ this.getCompilerOutputShader(g));

		p = gl.glCreateProgram();
		gl.glAttachShader(p, v);
		gl.glAttachShader(p, f);
		gl.glAttachShader(p, g);
		gl.glLinkProgram(p);
		// gl.glValidateProgram(p); //returns error when no texture stage bound

		System.out.println("Linker output:\n" + this.getLinkerOutput(p));

		int[] status = new int[1];
		gl.glGetShaderiv(v, GL2ES2.GL_COMPILE_STATUS, status, 0);
		if (status[0] == GL.GL_FALSE) {
			throw new Exception("Could not compile vertex shader.");
		}
		gl.glGetShaderiv(f, GL2ES2.GL_COMPILE_STATUS, status, 0);
		if (status[0] == GL.GL_FALSE) {
			throw new Exception("Could not compile fragment shader.");
		}
		
		gl.glGetShaderiv(g, GL2ES2.GL_COMPILE_STATUS, status, 0);
		if (status[0] == GL.GL_FALSE) {
			throw new Exception("Could not compile geometry shader.");
		}

		IntBuffer ib = ByteBuffer.allocateDirect(4)
				.order(ByteOrder.nativeOrder()).asIntBuffer();
		ib.rewind();
		gl.glGetProgramiv(p, GL2ES2.GL_LINK_STATUS, ib);
		if (ib.get(0) == GL.GL_FALSE) {
			throw new Exception("Could not link vertex and fragment shader.");
		}
	}

	/**
	 * Activate the shader program. As long as the shader is active, the vertex
	 * shader is executed for each vertex, and the fragment shader for each
	 * pixel that is rendered.
	 */
	@Override
	public void use() {
		gl.glUseProgram(p);
	}

	/**
	 * Disable the shader and go back to using OpenGL standard functionality to
	 * process vertices and fragments/pixels.
	 */
	@Override
	public void disable() {
		gl.glUseProgram(0);
	}

	public int programId() {
		return p;
	}
}
