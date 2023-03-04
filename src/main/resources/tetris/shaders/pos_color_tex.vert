#version 150 core

in vec2 Position;
in vec3 Color;
in vec2 UV;

out vec4 vertexColor;
out vec2 texCoord;

uniform mat4 Projection, ModelView;

void main() {
    gl_Position = Projection * ModelView * vec4(Position, 0.0, 1.0);
    vertexColor = vec4(Color, 1.0);
    texCoord = UV;
}
