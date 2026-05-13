package kamorzy.fisheries_wbf.fishery;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public record FisheryBaitProfile(
        Item baitItem,
        Set<EntityType<?>> eligibleFishTypes,
        int requiredBites,
        int spawnCount
) {
    private static final FisheryBaitProfile ROTTEN_FLESH = new FisheryBaitProfile(
            Items.ROTTEN_FLESH,
            Set.of(EntityType.COD, EntityType.SALMON),
            8,
            4
    );

    private static final FisheryBaitProfile BEETROOT = new FisheryBaitProfile(
            Items.BEETROOT,
            Set.of(EntityType.COD, EntityType.SALMON),
            8,
            1
    );

    private static final FisheryBaitProfile SPIDER_EYE = new FisheryBaitProfile(
            Items.SPIDER_EYE,
            Set.of(EntityType.PUFFERFISH, EntityType.TROPICAL_FISH),
            4,
            1
    );

    private static final List<FisheryBaitProfile> ALL = List.of(ROTTEN_FLESH, BEETROOT, SPIDER_EYE);

    public static Optional<FisheryBaitProfile> fromItem(Item item) {
        return ALL.stream().filter(profile -> profile.baitItem() == item).findFirst();
    }

    public boolean isEligible(Mob fish) {
        return fish.isAlive() && eligibleFishTypes.contains(fish.getType());
    }
}
