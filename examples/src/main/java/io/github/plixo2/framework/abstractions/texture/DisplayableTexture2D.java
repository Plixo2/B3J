package io.github.plixo2.framework.abstractions.texture;


public sealed interface DisplayableTexture2D permits Texture2D, IOTexture2D {

    Texture2D get();

}
