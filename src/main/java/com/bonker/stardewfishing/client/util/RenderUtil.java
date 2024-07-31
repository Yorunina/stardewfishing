package com.bonker.stardewfishing.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public class RenderUtil {
    public static void blitF(PoseStack poseStack, ResourceLocation texture, float x, float y, int uOffset, int vOffset, int uWidth, int vHeight) {
        float minU = uOffset / 256F;
        float minV = vOffset / 256F;
        float maxU = (uOffset + uWidth) / 256F;
        float maxV = (vOffset + vHeight) / 256F;
        
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        Matrix4f matrix4f = poseStack.last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, 0, 0, 0).uv(minU, minV).endVertex();
        bufferbuilder.vertex(matrix4f, 0, vHeight, 0).uv(minU, maxV).endVertex();
        bufferbuilder.vertex(matrix4f, uWidth, vHeight, 0).uv(maxU, maxV).endVertex();
        bufferbuilder.vertex(matrix4f, uWidth, 0, 0).uv(maxU, minV).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        poseStack.popPose();
    }

    public static void fillF(PoseStack poseStack, float minX, float minY, float maxX, float maxY, int pColor) {
        Matrix4f matrix4f = poseStack.last().pose();
        if (minX < maxX) {
            float temp = minX;
            minX = maxX;
            maxX = temp;
        }

        if (minY < maxY) {
            float temp = minY;
            minY = maxY;
            maxY = temp;
        }

        float alpha =  FastColor.ARGB32.alpha(pColor) / 255.0F;
        float red = FastColor.ARGB32.red(pColor) / 255.0F;
        float green = FastColor.ARGB32.green(pColor) / 255.0F;
        float blue = FastColor.ARGB32.blue(pColor) / 255.0F;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(matrix4f, minX, minY, 0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.vertex(matrix4f, minX, maxY, 0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.vertex(matrix4f, maxX, maxY, 0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.vertex(matrix4f, maxX, minY, 0).color(red, green, blue, alpha).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }

    public static void drawRotatedAround(PoseStack poseStack, ResourceLocation texture, float radians, float x, float y, float pivotX, float pivotY,
                                         int uOffset, int vOffset, int uWidth, int vHeight) {
        poseStack.pushPose();
        poseStack.translate(pivotX, pivotY, 0);
        poseStack.mulPose(Vector3f.ZN.rotation(radians));
        poseStack.translate(-pivotX, -pivotY, 0);
        RenderUtil.blitF(poseStack, texture, x, y, uOffset, vOffset, uWidth, vHeight);
        poseStack.popPose();
    }

    public static void drawWithAlpha(float alpha, Runnable runnable) {
        RenderSystem.setShaderColor(1, 1, 1, alpha);
        runnable.run();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    public static void drawWithBlend(Runnable runnable) {
        RenderSystem.enableBlend();
        runnable.run();
        RenderSystem.disableBlend();
    }

    public static void drawWithShake(PoseStack poseStack, Shake shake, float partialTick, boolean doShake, Runnable runnable) {
        if (doShake) {
            poseStack.pushPose();
            poseStack.translate(shake.getXOffset(partialTick), shake.getYOffset(partialTick), 0);
        }

        runnable.run();

        if (doShake) {
            poseStack.popPose();
        }
    }
}
