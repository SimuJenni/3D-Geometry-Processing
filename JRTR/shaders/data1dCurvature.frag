#version 150

// Input variable, passed from vertex to fragment shader
// and interpolated automatically to each fragment
in vec4 frag_color;
in float curvature;

// Output variable, will be written to the display automatically
out vec4 out_color;

// helper methods for color coding
vec4 colormap(float x) {
    float color = log(1+x/5);
    return vec4(color, color-1, 1-color, 1.0);
}


void main()
{		
    out_color = colormap(curvature);
}