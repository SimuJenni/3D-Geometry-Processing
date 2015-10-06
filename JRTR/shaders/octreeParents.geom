#version 150

//the projection matrix set by the main program
uniform mat4 projection;
uniform mat4 modelview;

layout(points) in;
layout(line_strip, max_vertices=2) out;

//the variable passed in from the vertex shader
in vec4 position_g[];
in vec4 parent_g[];

//the variables passed out to the fragment shader
out vec4 color_g;

void main()
{
	//use a constant color
	color_g = vec4(0.0f,0.0f,0.f,0.9);
    
    gl_Position = projection * modelview * position_g[0];
    EmitVertex();
    
    gl_Position = projection * modelview * parent_g[0];
    EmitVertex();
}
