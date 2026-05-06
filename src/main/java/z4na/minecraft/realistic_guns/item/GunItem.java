package z4na.minecraft.realistic_guns.item;

import z4na.minecraft.realistic_guns.init.ModItems;
import z4na.minecraft.realistic_guns.pack.GunDefinition;
import z4na.minecraft.realistic_guns.registry.GunRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class GunItem extends Item implements GeoItem {

    public static final String NBT_GUN_ID  = "GunId";
    public static final String NBT_AMMO    = "Ammo";
    public static final String NBT_STATE   = "GunState";

    public static final String ANIM_IDLE    = "animation.gun.idle";
    public static final String ANIM_SHOOT   = "animation.gun.shoot";
    public static final String ANIM_RELOAD  = "animation.gun.reload";
    public static final String ANIM_INSPECT = "animation.gun.inspect";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GunItem(Properties props) {
        super(props);
    }

    // ============================================================
    // DataComponent ベースのNBTアクセスユーティリティ
    // 1.21系: getTag()/getOrCreateTag() は廃止
    //         → CustomData コンポーネントを使う
    // ============================================================

    /** CustomData から CompoundTag を読み取り専用で取得 */
    private static CompoundTag getTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return new CompoundTag();
        return data.copyTag();
    }

    /** CompoundTag を書き込んで CustomData にセットする */
    private static void setTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /** タグを読み出し → 編集 → 書き戻すヘルパー */
    private static void editTag(ItemStack stack, java.util.function.Consumer<CompoundTag> editor) {
        CompoundTag tag = getTag(stack);
        editor.accept(tag);
        setTag(stack, tag);
    }

    // ============================================================
    // スタック生成・NBTアクセサ
    // ============================================================

    public static ItemStack createStack(String gunId) {
        ItemStack stack = new ItemStack(ModItems.GUN.get());
        editTag(stack, tag -> {
            tag.putString(NBT_GUN_ID, gunId);
            GunRegistry.get(gunId).ifPresent(def -> tag.putInt(NBT_AMMO, def.maxAmmo));
            tag.putString(NBT_STATE, "IDLE");
        });
        return stack;
    }

    public static String getGunId(ItemStack stack) {
        CompoundTag tag = getTag(stack);
        return tag.contains(NBT_GUN_ID) ? tag.getString(NBT_GUN_ID) : "";
    }

    public static int getAmmo(ItemStack stack) {
        return getTag(stack).getInt(NBT_AMMO);
    }

    public static void setAmmo(ItemStack stack, int ammo) {
        editTag(stack, tag -> tag.putInt(NBT_AMMO, ammo));
    }

    public static String getState(ItemStack stack) {
        CompoundTag tag = getTag(stack);
        return tag.contains(NBT_STATE) ? tag.getString(NBT_STATE) : "IDLE";
    }

    public static void setState(ItemStack stack, String state) {
        editTag(stack, tag -> tag.putString(NBT_STATE, state));
    }

    public Optional<GunDefinition> getDefinition(ItemStack stack) {
        return GunRegistry.get(getGunId(stack));
    }

    // ============================================================
    // 右クリック: 射撃 / スニーク右クリック: リロード
    // ============================================================

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Optional<GunDefinition> defOpt = getDefinition(stack);
        if (defOpt.isEmpty()) return InteractionResultHolder.pass(stack);

        GunDefinition def = defOpt.get();
        String state = getState(stack);

        if (player.isShiftKeyDown()) {
            if (!"RELOADING".equals(state)) {
                setState(stack, "RELOADING");
                triggerAnim(player, GeoItem.getId(stack), "reload_controller", def.animations.reload);
                scheduleReload(level, player, stack, def);
            }
            return InteractionResultHolder.success(stack);
        }

        if ("IDLE".equals(state) && getAmmo(stack) > 0) {
            shoot(level, player, stack, def);
            return InteractionResultHolder.success(stack);
        } else if (getAmmo(stack) <= 0) {
            player.displayClientMessage(
                    Component.translatable("message.realistic_guns.empty"), true);
        }

        return InteractionResultHolder.pass(stack);
    }

    private void shoot(Level level, Player player, ItemStack stack, GunDefinition def) {
        setAmmo(stack, getAmmo(stack) - 1);
        setState(stack, "SHOOTING");
        triggerAnim(player, GeoItem.getId(stack), "shoot_controller", def.animations.shoot);

        if (!level.isClientSide()) {
            performRaycast(level, player, def);
        }

        player.getCooldowns().addCooldown(this, def.fireRate);
        setState(stack, "IDLE");
    }

    private void performRaycast(Level level, Player player, GunDefinition def) {
        var hit = player.pick(def.range, 1.0f, false);
        if (hit == null) return;

        switch (hit.getType()) {
            case ENTITY -> {
                if (hit instanceof net.minecraft.world.phys.EntityHitResult entityHit) {
                    if (entityHit.getEntity() instanceof net.minecraft.world.entity.LivingEntity target) {
                        target.hurt(
                                level.damageSources().playerAttack(player),
                                def.damage
                        );
                    }
                }
            }
            case BLOCK -> {}
            default -> {}
        }
    }

    private void scheduleReload(Level level, Player player, ItemStack stack, GunDefinition def) {
        player.getCooldowns().addCooldown(this, def.reloadTime);
        if (!level.isClientSide()) {
            setAmmo(stack, def.maxAmmo);
            setState(stack, "IDLE");
        }
    }

    // ============================================================
    // 残弾確認モーション
    // ============================================================

    public void triggerInspect(Player player, ItemStack stack) {
        getDefinition(stack).ifPresent(def -> {
            if ("IDLE".equals(getState(stack))) {
                setState(stack, "INSPECTING");
                triggerAnim(player, GeoItem.getId(stack), "inspect_controller", def.animations.inspect);
            }
        });
    }

    // ============================================================
    // ツールチップ
    // ============================================================

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        String id = getGunId(stack);
        if (!id.isEmpty()) {
            GunRegistry.get(id).ifPresent(def ->
                    tooltipComponents.add(Component.literal("§7" + def.displayName))
            );
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        String id = getGunId(stack);
        if (!id.isEmpty()) {
            Optional<GunDefinition> def = GunRegistry.get(id);
            if (def.isPresent()) {
                return Component.literal(def.get().displayName);
            }
        }
        return super.getName(stack);
    }

    // ============================================================
    // GeckoLib 実装
    // ============================================================

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "idle_controller", 5, state -> {
                    state.getController().setAnimation(
                            RawAnimation.begin().thenLoop(ANIM_IDLE)
                    );
                    return PlayState.CONTINUE;
                })
        );
        controllers.add(new AnimationController<>(this, "shoot_controller",   0, state -> PlayState.STOP));
        controllers.add(new AnimationController<>(this, "reload_controller",  0, state -> PlayState.STOP));
        controllers.add(new AnimationController<>(this, "inspect_controller", 0, state -> PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private z4na.minecraft.realistic_guns.client.renderer.GunItemRenderer renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (renderer == null) {
                    renderer = new z4na.minecraft.realistic_guns.client.renderer.GunItemRenderer();
                }
                return renderer;
            }
        });
    }
}