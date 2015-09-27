#version 150

//the projection matrix set by the main program
uniform mat4 projection;
uniform mat4 modelview;

//Normal will be visualized using line-strips of the mesh and normals
layout(triangles) in;
layout(line_strip, max_vertices=6) out;

//the variable passed in from the vertex shader
//in geometry shaders the input is always organized as an array
in vec4 position_g[];
in vec4 normal_g[];

//the variables passed out to the fragment shader
out vec4 color_g;

void main()
{
	//use a constant color
	color_g = vec4(1.0f,0.5f,0.f,1.f);
    
	
	for(int i=0; i<gl_in.length(); i++)
	{
		gl_Position = projection * modelview * position_g[i];
		gl_PrimitiveID = gl_PrimitiveIDIn;
        EmitVertex();
        
        
        gl_Position = projection * modelview * vec4(position_g[i].xyz+normal_g[i].xyz*0.2, 1.0);
        gl_PrimitiveID = gl_PrimitiveIDIn;
        EmitVertex();
        EndPrimitive();

	}
	
}
