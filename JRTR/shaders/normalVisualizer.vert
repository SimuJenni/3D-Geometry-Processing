#version 150

// Uniform variables, set in main program
uniform mat4 projection; 
uniform mat4 modelview;

// Input vertex attributes; passed from main program to the shader 
// via glDisplayable.addElement(float[],Semantic.POSITION, 3).
in vec4 position;
in vec4 attribute3d;

//the positions and normals passed to the geometry shader
out vec4 position_g;
out vec4 normal_g;

void main()
{
	// Simple pass-through vertex-shader
	position_g = position;
    normal_g = attribute3d;
}
