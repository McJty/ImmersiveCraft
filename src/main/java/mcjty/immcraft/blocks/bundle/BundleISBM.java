package mcjty.immcraft.blocks.bundle;

import com.google.common.base.Function;
import mcjty.immcraft.ImmersiveCraft;
import mcjty.immcraft.api.util.Vector;
import mcjty.immcraft.cables.CableSectionRender;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class BundleISBM implements IBakedModel {

    public static final float THICK = 1/16.0f;

    public static final ModelResourceLocation BAKED_MODEL = new ModelResourceLocation(ImmersiveCraft.MODID + ":bundle");

    private TextureAtlasSprite bundleSprite;
    private VertexFormat format;
    private final float CT = .1f;
    private static final Map<String, TextureAtlasSprite> sprites = new HashMap<>();

    public BundleISBM(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        this.format = format;
        bundleSprite = bakedTextureGetter.apply(new ResourceLocation(ImmersiveCraft.MODID, "blocks/bundle"));
    }

    private void putVertex(UnpackedBakedQuad.Builder builder, Vec3d normal, double x, double y, double z, float u, float v, TextureAtlasSprite sprite) {
        for (int e = 0; e < format.getElementCount(); e++) {
            switch (format.getElement(e).getUsage()) {
                case POSITION:
                    builder.put(e, (float)x, (float)y, (float)z, 1.0f);
                    break;
                case COLOR:
                    builder.put(e, 1.0f, 1.0f, 1.0f, 1.0f);
                    break;
                case UV:
                    if (format.getElement(e).getIndex() == 0) {
                        u = sprite.getInterpolatedU(u);
                        v = sprite.getInterpolatedV(v);
                        builder.put(e, u, v, 0f, 1f);
                        break;
                    }
                case NORMAL:
                    builder.put(e, (float) normal.xCoord, (float) normal.yCoord, (float) normal.zCoord, 0f);
                    break;
                default:
                    builder.put(e);
                    break;
            }
        }
    }

    private BakedQuad createQuad(Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, TextureAtlasSprite sprite) {
//        Vector normal = v1.subtract(v2).cross(v3.subtract(v2));
//        Vec3d n = new Vec3d(normal.x, normal.y, normal.z);
        Vec3d n = v2.subtract(v1).crossProduct(v3.subtract(v1)).normalize();

        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setTexture(sprite);
        putVertex(builder, n, v1.xCoord, v1.yCoord, v1.zCoord, 0, 0, sprite);
        putVertex(builder, n, v2.xCoord, v2.yCoord, v2.zCoord, 0, 16, sprite);
        putVertex(builder, n, v3.xCoord, v3.yCoord, v3.zCoord, 16, 16, sprite);
        putVertex(builder, n, v4.xCoord, v4.yCoord, v4.zCoord, 16, 0, sprite);
        return builder.build();
    }

    private BakedQuad createOrientedQuad(Vec3d inside, Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, TextureAtlasSprite sprite, boolean inwards) {
        Vec3d normal = v1.subtract(v2).crossProduct(v3.subtract(v2));
        if ((inside.subtract(v2).dotProduct(normal) < 0) != inwards) {
            return createQuad(v4, v3, v2, v1, sprite);
        } else {
            return createQuad(v1, v2, v3, v4, sprite);
        }
    }


    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return bundleSprite;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (state == null || side != null) {
            return Collections.emptyList();
        }
        IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
        List<CableSectionRender> cables = extendedBlockState.getValue(BundleBlock.CABLES);
        List<BakedQuad> quads = new ArrayList<>();

        for (CableSectionRender section : cables) {
            String textureName = section.getSubType().getTextureName();
            TextureAtlasSprite sprite = sprites.get(textureName);
            if (sprite == null) {
                sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(textureName);
                sprites.put(textureName, sprite);
            }
            addCable(section, sprite, quads);
        }


        return quads;
    }

    private void addCable(CableSectionRender section, TextureAtlasSprite sprite, List<BakedQuad> quads) {
        Vec3d vector = section.getVector();
        Vec3d vector1 = section.getVector1();
        if (vector1 == null) {
            vector1 = new Vec3d(vector.xCoord + CT, vector.yCoord + CT, vector.zCoord + CT);
        }
        Vec3d vector2 = section.getVector2();

        addCablePart(quads, vector, vector1, sprite);
        if (vector2 != null) {
            addCablePart(quads, vector2, vector, sprite);
        }
    }

    private void addCablePart(List<BakedQuad> quads, Vec3d vector1, Vec3d vector2, TextureAtlasSprite sprite) {
        Pair<Vec3d, Vec3d> vectorPair = Vector.calculatePerpendicularVector(vector1, vector2);
        Vec3d v1 = vectorPair.getLeft().normalize().scale(CT);
        Vec3d v2 = vectorPair.getRight().normalize().scale(CT);
        Vec3d center = vector1.add(vector2).scale(.5f);

        Vec3d s1 = vector1.add(v1).add(v2);
        Vec3d s2 = vector2.add(v1).add(v2);
        Vec3d s3 = vector2.add(v1).subtract(v2);
        Vec3d s4 = vector1.add(v1).subtract(v2);
        Vec3d s5 = vector2.subtract(v1).subtract(v2);
        Vec3d s6 = vector1.subtract(v1).subtract(v2);
        Vec3d s7 = vector2.subtract(v1).add(v2);
        Vec3d s8 = vector1.subtract(v1).add(v2);

        quads.add(createOrientedQuad(center, s1, s2, s3, s4, sprite, false));
        quads.add(createOrientedQuad(center, s4, s3, s5, s6, sprite, false));
        quads.add(createOrientedQuad(center, s6, s5, s7, s8, sprite, false));
        quads.add(createOrientedQuad(center, s8, s7, s2, s1, sprite, false));

        // Inwards
//        quads.add(createOrientedQuad(center, s1, s2, s3, s4, sprite, true));
//        quads.add(createOrientedQuad(center, s4, s3, s5, s6, sprite, true));
//        quads.add(createOrientedQuad(center, s6, s5, s7, s8, sprite, true));
//        quads.add(createOrientedQuad(center, s8, s7, s2, s1, sprite, true));

        // Caps
        quads.add(createOrientedQuad(center, s1, s4, s6, s8, sprite, false));
        quads.add(createOrientedQuad(center, s7, s5, s3, s2, sprite, false));
    }
}
