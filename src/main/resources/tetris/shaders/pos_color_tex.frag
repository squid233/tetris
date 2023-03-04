#version 150 core

in vec4 vertexColor;
in vec2 texCoord;

out vec4 FragColor;

uniform sampler2D Sampler;

void main() {
    FragColor = vertexColor * texture(Sampler, texCoord);
}
