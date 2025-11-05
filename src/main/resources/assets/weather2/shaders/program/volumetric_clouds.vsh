#version 120

// The data that was passed to this shader via a texture
uniform sampler2D particle_data;
// The pov of the screen
uniform vec3 camera;
// Used to determine how many vertices to render
uniform int quality;
// Width and Height of particle_data
uniform int width;
uniform int height;

attribute vec3 position;
attribute float vertex_id;
varying vec4 fragment_color;

void main()
{
    int index = int(vertex_id / quality);

    // Gather UVs for sampling
    float u1 = 0.5F / width; // 1st Texel
    float u2 = 1.5F / width; // 2nd Texel
    float u3 = 2.5F / width; // 3rd Texel
    float v = (float(index) + 0.5F) / float(height);

    // Sample the data for the specified particle
    vec4 tex1 = texture2D(particle_data, vec2(u1, v));
    vec4 tex2 = texture2D(particle_data, vec2(u2, v));
    vec4 tex3 = texture2D(particle_data, vec2(u3, v));

    // Extract the data from the texels
    vec3 particle_pos = tex1.xyz;
    float particle_height = tex1.w;
    float particle_width = tex2.x;
    vec4 color = vec4(tex2.y, tex2.z, tex2.w, tex3.x);
    float brightness = tex3.y;

    // Use the data and pass color to the fragment shader
    vec3 world_pos = particle_pos + position * vec3(particle_width, particle_width, particle_width);
    gl_Position = gl_ModelViewProjectionMatrix * vec4(world_pos, 1.0F);
    fragment_color = color;
}