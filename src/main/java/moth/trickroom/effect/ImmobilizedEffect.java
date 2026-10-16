package moth.trickroom.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class ImmobilizedEffect extends StatusEffect {
    public ImmobilizedEffect() {
        super(StatusEffectCategory.HARMFUL, 0x03fcfc);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        entity.setVelocity(Vec3d.ZERO);
        entity.velocityDirty = true;
        entity.setSprinting(false);
        entity.setSneaking(false);
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }
}
