package net.mrbt0907.weather2.client.rendering.shaders.mesh;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

public class SimpleVolumetricMesh
{
    public final boolean vbo_enabled;
    public final int vbo_id;
    /**How many vertices this mesh contains in the buffer*/
    public final int length;

    public SimpleVolumetricMesh(int quality, int particle_count)
    {
        FloatBuffer buffer = SimpleVolumetricMesh.createVertices(quality, particle_count);
        length = buffer.limit() / 4;

        // ----- VBO On ----- \\
        vbo_id = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo_id);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        vbo_enabled = true;
    }

    public void bindVBO()
    {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo_id);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 16 ,0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 1, GL11.GL_FLOAT, false, 16 ,12);
    }

    public void unbindVBO()
    {
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void delete()
    {
        if (vbo_enabled)
            GL15.glDeleteBuffers(vbo_id);
    }

    protected static FloatBuffer createVertices(int quality, int particle_count)
    {
        int lat_bands = quality, long_bands = quality, verteces = 0; 
        FloatBuffer buffer = BufferUtils.createFloatBuffer(lat_bands * long_bands * 6 * 4);
        double theta1, theta2, phi1, phi2, lat_pi = Math.PI / lat_bands, long_pi = (Math.PI / long_bands) * 2;

        for (int p = 0; p < particle_count; p++)
            for (int lat = 0; lat < lat_bands; lat++)
            {
                theta1 = lat * lat_pi;
                theta2 = (lat + 1) * lat_pi;

                for (int lon = 0; lon < long_bands; lon++)
                {
                    phi1 = lon * long_pi;
                    phi2 = (lon + 1) * long_pi;

                    float[] vertex_1 = SimpleVolumetricMesh.sphericalToCartesian(theta1, phi1);
                    float[] vertex_2 = SimpleVolumetricMesh.sphericalToCartesian(theta2, phi1);
                    float[] vertex_3 = SimpleVolumetricMesh.sphericalToCartesian(theta2, phi2);
                    float[] vertex_4 = SimpleVolumetricMesh.sphericalToCartesian(theta1, phi2);

                    // Triangle 1
                    buffer.put(vertex_1).put(verteces++);
                    buffer.put(vertex_2).put(verteces++);
                    buffer.put(vertex_3).put(verteces++);
                    // Triangle 2
                    buffer.put(vertex_3).put(verteces++);
                    buffer.put(vertex_4).put(verteces++);
                    buffer.put(vertex_1).put(verteces++);
                }
            }

        buffer.flip();
        return buffer;
    }

    public static float[] sphericalToCartesian(double theta, double phi)
    {
        return new float[] {
            (float) (Math.sin(theta) * Math.cos(phi)),
            (float) Math.cos(theta),
            (float) (Math.sin(theta) * Math.sin(phi))
        };
    }
}