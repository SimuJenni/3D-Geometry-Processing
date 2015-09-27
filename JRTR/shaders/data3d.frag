#version 150

in vec4 color_g;
flat in vec3 normal_g;


// Output variable, will be written to the display automatically
out vec4 out_color;

void main()
{		
    out_color = color_g * (abs(dot(normal_g, vec3(0,0,1)))+0.1) //diffuse shading plus ambient (0.1)
    * (1.5 + sign(dot(normal_g, vec3(0,0,1)))/2.5); //scaled to a fifth when looking at the back of the triangle
}