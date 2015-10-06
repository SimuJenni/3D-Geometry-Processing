#version 150
// Simple fragment shader which does some diffuse shading.

// Input variable, passed from vertex to fragment shader
// and interpolated automatically to each fragment
in vec4 color_g;

// Output variable, will be written to the display automatically
out vec4 out_color;

void main()
{		
    out_color = color_g;
}
