#version 150

// Uniform variables, set in main program
uniform mat4 projection; 
uniform mat4 modelview;

// Input vertex attributes; passed from main program to the shader 
// via glDisplayable.addElement(float[],Semantic.POSITION, 3).
in vec4 position;
in vec4 target;

out vec4 position_g;
out vec4 target_g;

void main()
{
	// Simple pass-through vertex-shader
	position_g = position;
    target_g = target;
}
