package moth.trickroom.effect;

import moth.trickroom.TrickRoomMod;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.List;

public final class ModStatusEffects {
    private static final ThreadLocal<Boolean> ROOM_REVERSAL_DISABLED = ThreadLocal.withInitial(() -> false);

    public static final RegistryEntry<StatusEffect> IMMOBILIZED = register("immobilized", new ImmobilizedEffect());
    public static final RegistryEntry<StatusEffect> RUINOUS = register("ruinous", new RuinousEffect());
    public static final RegistryEntry<StatusEffect> VULNERABILITY = register("vulnerability", new VulnerabilityEffect());

    public static final List<RegistryEntry<StatusEffect>> NEGATIVE_EFFECTS = List.of(
            StatusEffects.SLOWNESS,
            StatusEffects.MINING_FATIGUE,
            StatusEffects.WEAKNESS,
            RUINOUS,
            StatusEffects.HUNGER,
            StatusEffects.BLINDNESS,
            StatusEffects.POISON,
            StatusEffects.WITHER,
            VULNERABILITY,
            StatusEffects.GLOWING,
            StatusEffects.NAUSEA
    );

    public static final List<RegistryEntry<StatusEffect>> POSITIVE_EFFECTS = List.of(
            StatusEffects.SPEED,
            StatusEffects.HASTE,
            StatusEffects.STRENGTH,
            StatusEffects.ABSORPTION,
            StatusEffects.JUMP_BOOST,
            StatusEffects.NIGHT_VISION,
            StatusEffects.REGENERATION,
            StatusEffects.SATURATION,
            StatusEffects.RESISTANCE,
            StatusEffects.INVISIBILITY,
            StatusEffects.FIRE_RESISTANCE
    );

    private ModStatusEffects() {
    }

    public static void register() {
    }

    public static boolean isRoomReversalDisabled() {
        return ROOM_REVERSAL_DISABLED.get();
    }

    public static void runWithoutRoomReversal(Runnable action) {
        boolean wasDisabled = ROOM_REVERSAL_DISABLED.get();
        ROOM_REVERSAL_DISABLED.set(true);
        try {
            action.run();
        } finally {
            ROOM_REVERSAL_DISABLED.set(wasDisabled);
        }
    }

    public static StatusEffectInstance reverse(StatusEffectInstance effect) {
        RegistryEntry<StatusEffect> type = effect.getEffectType();
        if (type.value().getCategory() == StatusEffectCategory.NEUTRAL && !type.equals(StatusEffects.GLOWING)) {
            return effect;
        }

        for (int i = 0; i < POSITIVE_EFFECTS.size(); i++) {
            if (type.equals(POSITIVE_EFFECTS.get(i))) {
                return copyWithType(effect, NEGATIVE_EFFECTS.get(i));
            }
            if (type.equals(NEGATIVE_EFFECTS.get(i))) {
                return copyWithType(effect, POSITIVE_EFFECTS.get(i));
            }
        }

        return effect;
    }

    private static StatusEffectInstance copyWithType(StatusEffectInstance effect, RegistryEntry<StatusEffect> newType) {
        return new StatusEffectInstance(
                newType,
                effect.getDuration(),
                effect.getAmplifier(),
                effect.isAmbient(),
                effect.shouldShowParticles(),
                effect.shouldShowIcon()
        );
    }

    private static RegistryEntry<StatusEffect> register(String path, StatusEffect statusEffect) {
        StatusEffect registered = Registry.register(Registries.STATUS_EFFECT, TrickRoomMod.id(path), statusEffect);
        return Registries.STATUS_EFFECT.getEntry(registered);
    }
}
