#version 120

varying vec3 local_pos;
varying vec4 fragment_color;

void main()
{
    gl_FragColor = fragment_color.rgb;
}