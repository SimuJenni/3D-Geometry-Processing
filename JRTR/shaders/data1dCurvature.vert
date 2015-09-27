#version 150
// This shader expects position input laid out as triangles
// and diffusely shades the surface.
// 
// No further vertex attributes besides the position are needed,
// the per face normals needed for the shading are computed on the fly in the
// geometry shader.

// Uniform variables, set in main program
uniform mat4 projection; 
uniform mat4 modelview;

// Input vertex attributes; passed from main program to the shader 
// via glDisplayable.addElement(float[],Semantic.POSITION, 3).
in vec4 position;
in float attribute1d;

out float curvature;

// helper methods for color coding
vec4 colormap(float x) {
    float color = log(1+x/5);
    return vec4(color, color-1, 1-color, 1.0);
}

void main()
{
	//the position coordinates are transformed into view coordinates but are
	//not yet projected
    gl_Position = projection * modelview * position;
    
    curvature = attribute1d;
}


