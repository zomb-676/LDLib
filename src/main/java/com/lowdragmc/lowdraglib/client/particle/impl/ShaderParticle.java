package com.lowdragmc.lowdraglib.client.particle.impl;

import com.lowdragmc.lowdraglib.client.particle.LParticle;
import com.lowdragmc.lowdraglib.client.shader.Shaders;
import com.lowdragmc.lowdraglib.client.shader.management.Shader;
import com.lowdragmc.lowdraglib.client.shader.management.ShaderManager;
import com.lowdragmc.lowdraglib.client.shader.management.ShaderProgram;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author KilaBash
 * @date 2022/06/15
 * @implNote ShaderParticle
 */
@OnlyIn(Dist.CLIENT)
public class ShaderParticle extends LParticle {
    public final ShaderTrailRenderType renderType;

    public ShaderParticle(ClientLevel level, double x, double y, double z, ShaderTrailRenderType renderType) {
        super(level, x, y, z);
        this.renderType = renderType;
    }

    public ShaderParticle(ClientLevel level, double x, double y, double z, ResourceLocation texture) {
        super(level, x, y, z);
        this.renderType = TYPE.apply(texture);
    }

    public ShaderParticle(ClientLevel level, double x, double y, double z, double sX, double sY, double sZ, ShaderTrailRenderType renderType) {
        super(level, x, y, z, sX, sY, sZ);
        this.renderType = renderType;
    }

    public ShaderParticle(ClientLevel level, double x, double y, double z, double sX, double sY, double sZ, ResourceLocation texture) {
        super(level, x, y, z, sX, sY, sZ);
        this.renderType = TYPE.apply(texture);
    }

    @Override
    @Nonnull
    public ParticleRenderType getRenderType() {
        return renderType;
    }

    @Override
    protected float getU0(float pPartialTicks) {
        return 0;
    }

    @Override
    protected float getU1(float pPartialTicks) {
        return 1;
    }

    @Override
    protected float getV0(float pPartialTicks) {
        return 0;
    }

    @Override
    protected float getV1(float pPartialTicks) {
        return 1;
    }

    public static class ShaderTrailRenderType implements ParticleRenderType {
        ResourceLocation shader;
        Consumer<ShaderProgram> shaderProgramConsumer;

        public ShaderTrailRenderType(ResourceLocation shader) {
            this.shader = shader;
        }

        public ShaderTrailRenderType(ResourceLocation shader, Consumer<ShaderProgram> shaderProgramConsumer) {
            this(shader);
            this.shaderProgramConsumer = shaderProgramConsumer;
        }

        @Override
        public void begin(@Nonnull BufferBuilder bufferBuilder, @Nonnull TextureManager textureManager) {
            RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
            RenderTarget target = ShaderManager.getInstance().renderFullImageInFramebuffer(ShaderManager.getTempTarget(), Shaders.load(Shader.ShaderType.FRAGMENT, shader), null, shaderProgramConsumer);

            mainTarget.bindWrite(!ShaderManager.getInstance().hasViewPort());
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
            RenderSystem.setShader(Shaders::getParticleShader);
            RenderSystem.setShaderTexture(0, target.getColorTextureId());
            RenderSystem.enableCull();
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(@Nonnull Tesselator tesselator) {
            tesselator.end();
            RenderSystem.depthMask(true);
        }
    }

    protected static final Function<ResourceLocation, ShaderTrailRenderType> TYPE = Util.memoize((texture) -> new ShaderTrailRenderType(texture));

}
