package z4na.minecraft.realistic_guns.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import z4na.minecraft.realistic_guns.item.GunItem;
import z4na.minecraft.realistic_guns.pack.GunDefinition;
import z4na.minecraft.realistic_guns.registry.GunRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.Optional;

public class GunItemRenderer extends GeoItemRenderer<GunItem> {

    public GunItemRenderer() {
        super(new GunGeoModel());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
                             PoseStack poseStack, MultiBufferSource bufferSource,
                             int packedLight, int packedOverlay) {
        ((GunGeoModel) getGeoModel()).setCurrentStack(stack);
        super.renderByItem(stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
    }

    // ============================================================
    // GeoModel — 4.7系の新API使用
    // getModelResource(T) / getTextureResource(T) は非推奨
    // → 引数なしオーバーロードを使う
    // ============================================================

    public static class GunGeoModel extends GeoModel<GunItem> {

        private ItemStack currentStack = ItemStack.EMPTY;

        public void setCurrentStack(ItemStack stack) {
            this.currentStack = stack;
        }

        private Optional<GunDefinition> getDef() {
            return GunRegistry.get(GunItem.getGunId(currentStack));
        }

        // 非推奨の引数付きオーバーロードではなく、
        // animatable を使わない形で override
        @Override
        public ResourceLocation getModelResource(GunItem animatable) {
            return getDef().map(def ->
                    ResourceLocation.fromNamespaceAndPath(
                            "realistic_guns",
                            "geo/" + def.geoModel.replace(":", "/") + ".geo.json"
                    )
            ).orElse(ResourceLocation.fromNamespaceAndPath(
                    "realistic_guns", "geo/default.geo.json"
            ));
        }

        @Override
        public ResourceLocation getTextureResource(GunItem animatable) {
            return getDef().map(def ->
                    ResourceLocation.fromNamespaceAndPath(
                            "realistic_guns",
                            "textures/" + def.texture.replace(":", "/") + ".png"
                    )
            ).orElse(ResourceLocation.fromNamespaceAndPath(
                    "realistic_guns", "textures/item/default_gun.png"
            ));
        }

        @Override
        public ResourceLocation getAnimationResource(GunItem animatable) {
            return getDef().map(def ->
                    ResourceLocation.fromNamespaceAndPath(
                            "realistic_guns",
                            "animations/" + def.animationFile.replace(":", "/") + ".animation.json"
                    )
            ).orElse(ResourceLocation.fromNamespaceAndPath(
                    "realistic_guns", "animations/default.animation.json"
            ));
        }

        @Override
        public void setCustomAnimations(GunItem animatable, long instanceId,
                                        AnimationState<GunItem> animationState) {}
    }
}