package com.numbers.projectaura.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.numbers.projectaura.ProjectAura;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.OptionalDouble;
import java.util.function.Function;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR;
import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP;

public class ProjectAuraRenderType extends RenderStateShard {

    // Code from Neat, a mod which is made by Vaskii.
    //https://github.com/UpcraftLP/Orderly/blob/master/src/main/resources/assets/orderly/textures/ui/default_health_bar.png
    public static final RenderType BAR_TEXTURE_TYPE = RenderType.create(
            "bar_texture_type",
            POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                    .setTextureState(new TextureStateShard(new ResourceLocation(ProjectAura.MOD_ID, "textures/ui/health_bar_texture.png"), false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .createCompositeState(false)
    );

    public static final RenderType OUTLINE_TYPE = RenderType.create(
            "outline_type",
            POSITION_COLOR,
            VertexFormat.Mode.DEBUG_LINES,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(POSITION_TEX_SHADER)
                    .setLineState(new LineStateShard(OptionalDouble.empty()))
                    .setLayeringState(NO_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(CULL)
                    .createCompositeState(false)
    );

    private static final Function<ResourceLocation, RenderType> COLORED_TEX_TYPE = Util.memoize((texture) -> RenderType.create(
            "colored_tex_type",
            POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                    .setTextureState(new TextureStateShard(texture, false, true))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .createCompositeState(false)
    ));

    public static RenderType coloredTexType(ResourceLocation iconTexture) {
        return COLORED_TEX_TYPE.apply(iconTexture);
    }


    private ProjectAuraRenderType(String string, Runnable r, Runnable r1) {
        super(string, r, r1);
    }

}