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

//the positions passed to the geometry shader
out vec4 position_g;
out vec4 frag_color;


// helper methods for color coding
vec4 colormap(float x) {
    float r = 0.0, g = 0.0, b = 0.0;
    
    if (x < 0.0) {
        r = 127.0 / 255.0;
    } else if (x <= 1.0 / 9.0) {
        r = 1147.5 * (1.0 / 9.0 - x) / 255.0;
    } else if (x <= 5.0 / 9.0) {
        r = 0.0;
    } else if (x <= 7.0 / 9.0) {
        r = 1147.5 * (x - 5.0 / 9.0) / 255.0;
    } else {
        r = 1.0;
    }
    
    if (x <= 1.0 / 9.0) {
        g = 0.0;
    } else if (x <= 3.0 / 9.0) {
        g = 1147.5 * (x - 1.0 / 9.0) / 255.0;
    } else if (x <= 7.0 / 9.0) {
        g = 1.0;
    } else if (x <= 1.0) {
        g = 1.0 - 1147.5 * (x - 7.0 / 9.0) / 255.0;
    } else {
        g = 0.0;
    }
    
    if (x <= 3.0 / 9.0) {
        b = 1.0;
    } else if (x <= 5.0 / 9.0) {
        b = 1.0 - 1147.5 * (x - 3.0 / 9.0) / 255.0;
    } else {
        b = 0.0;
    }
    
    return vec4(r, g, b, 1.0);
}

void main()
{
	//the position coordinates are transformed into view coordinates but are
	//not yet projected
    gl_Position = projection * modelview * position;
    
    frag_color = colormap(attribute1d);
}


