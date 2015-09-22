#version 150

// Input variable, passed from vertex to fragment shader
// and interpolated automatically to each fragment
in vec4 frag_color;

// Output variable, will be written to the display automatically
out vec4 out_color;

void main()
{		
    out_color = frag_color;
}