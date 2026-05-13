package kamorzy.fisheries_wbf.fishery;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.TagValueOutput;
import java.util.UUID;

public record FisheryBiteRecord(
        UUID parentUuid,
        EntityType<?> fishType,
        boolean persistent,
        CompoundTag copiedFishData
) {
    public static FisheryBiteRecord from(Mob fish) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, fish.registryAccess());
        fish.saveWithoutId(output);
        CompoundTag tag = output.buildResult();

        // Identity, position, movement, and passenger data belong to the offspring, not the parent.
        tag.remove("UUID");
        tag.remove("Pos");
        tag.remove("Motion");
        tag.remove("Rotation");
        tag.remove("Leash");
        tag.remove("Passengers");
        tag.remove("FallDistance");
        tag.remove("PortalCooldown");

        return new FisheryBiteRecord(
                fish.getUUID(),
                fish.getType(),
                fish.requiresCustomPersistence(),
                tag
        );
    }
}
