package moth.trickroom.effect;

import moth.trickroom.TrickRoomMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;

public class RuinousEffect extends StatusEffect {
    public RuinousEffect() {
        super(StatusEffectCategory.HARMFUL, 0x6e0c19);
        addAttributeModifier(
                EntityAttributes.MAX_ABSORPTION,
                TrickRoomMod.id("effect.ruinous_absorption"),
                -4.0D,
                EntityAttributeModifier.Operation.ADD_VALUE
        );
        addAttributeModifier(
                EntityAttributes.MAX_HEALTH,
                TrickRoomMod.id("effect.ruinous_health"),
                -4.0D,
                EntityAttributeModifier.Operation.ADD_VALUE
        );
    }

    @Override
    public void onApplied(LivingEntity entity, int amplifier) {
        super.onApplied(entity, amplifier);
        updateHealth(entity);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        updateHealth(entity);
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    private static void updateHealth(LivingEntity entity) {
        entity.setAbsorptionAmount(0.0F);
        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }
}
