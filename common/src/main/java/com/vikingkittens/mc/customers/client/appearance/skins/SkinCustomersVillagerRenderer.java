package com.vikingkittens.mc.customers.client.appearance.skins;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;
import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerDefinition;

final class SkinCustomersVillagerRenderer<T extends Mob & CustomersVillager>
        extends HumanoidMobRenderer<T, AvatarRenderState, PlayerModel> {
    private static final String TEXTURE_PREFIX = "textures/";
    private static final String TEXTURE_SUFFIX = ".png";
    private final boolean slim;

    SkinCustomersVillagerRenderer(EntityRendererProvider.Context context, boolean slim) {
        super(
                context,
                new PlayerModel(context.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slim),
                SkinCustomersVillagerDefinition.DEFAULT_SHADOW_RADIUS
        );
        this.slim = slim;
        ArmorModelSet<HumanoidModel<AvatarRenderState>> armorModels = ArmorModelSet.bake(
                slim ? ModelLayers.PLAYER_SLIM_ARMOR : ModelLayers.PLAYER_ARMOR,
                context.getModelSet(),
                HumanoidModel::new
        );
        addLayer(new HumanoidArmorLayer<>(this, armorModels, context.getEquipmentRenderer()));
    }

    @Override
    public AvatarRenderState createRenderState() {
        return new AvatarRenderState();
    }

    @Override
    public void extractRenderState(
            T entity,
            AvatarRenderState renderState,
            float partialTick
    ) {
        super.extractRenderState(entity, renderState, partialTick);
        SkinCustomersVillagerDefinition skin = SkinCustomersVillagerClientAppearance.getSkin(entity);
        renderState.skin = createPlayerSkin(
                SkinCustomersVillagerTextureManager.getTexture(skin),
                slim
        );
        renderState.scale *= skin.scale();
        renderState.shadowRadius = skin.shadowRadius();
        renderState.showHat = true;
        renderState.showJacket = true;
        renderState.showLeftPants = true;
        renderState.showRightPants = true;
        renderState.showLeftSleeve = true;
        renderState.showRightSleeve = true;
    }

    @Override
    public Identifier getTextureLocation(AvatarRenderState renderState) {
        return renderState.skin.body().texturePath();
    }

    @Override
    protected float getShadowRadius(AvatarRenderState renderState) {
        return renderState.shadowRadius;
    }

    static PlayerSkin createPlayerSkin(Identifier texture, boolean slim) {
        Identifier assetId = toAssetId(texture);
        ClientAsset.Texture asset =
                new ClientAsset.ResourceTexture(assetId, texture);
        return PlayerSkin.insecure(
                asset,
                null,
                null,
                slim ? PlayerModelType.SLIM : PlayerModelType.WIDE
        );
    }

    static Identifier toAssetId(Identifier texture) {
        String path = texture.getPath();
        if (path.startsWith(TEXTURE_PREFIX)
                && path.endsWith(TEXTURE_SUFFIX)) {
            path = path.substring(
                    TEXTURE_PREFIX.length(),
                    path.length() - TEXTURE_SUFFIX.length()
            );
        }
        return Identifier.fromNamespaceAndPath(
                texture.getNamespace(),
                path
        );
    }
}
