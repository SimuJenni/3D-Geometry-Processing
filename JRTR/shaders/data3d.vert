#version 150

// Uniform variables, set in main program
uniform mat4 projection; 
uniform mat4 modelview;
uniform bool displayData;

in vec4 position;
in vec4 attribute3d;

//the positions passed to the geometry shader
out vec4 position_g;

void main()
{
    // positions depending on displayData
    position_g = displayData ? modelview * attribute3d : modelview * position;
}


