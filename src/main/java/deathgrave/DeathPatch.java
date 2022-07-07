package deathgrave;

import deathgrave.inventory.DeathGrave;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.server.ServerClient;
import necesse.engine.registries.ObjectRegistry;
import necesse.entity.mobs.Attacker;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.pickup.ItemPickupEntity;
import necesse.entity.pickup.PickupEntity;
import net.bytebuddy.asm.Advice;

import java.util.HashSet;

@ModMethodPatch(
        target = PlayerMob.class,
        name = "onDeath",
        arguments = {Attacker.class, HashSet.class}
)
public class DeathPatch {
    @Advice.OnMethodExit
    static void onExit(@Advice.This Mob mob, @Advice.Argument(0) Attacker attacker, @Advice.Argument(1) HashSet<Attacker> attackers) {
        DeathGrave deathGrave = (DeathGrave) ObjectRegistry.getObject("kew_grave");
        int tileX = mob.getTileX();
        int tileY = mob.getTileY();
        ServerClient serverClient = ((PlayerMob) mob).getServerClient();
        boolean changeCoord = true;
        if (tileX == serverClient.spawnTile.x && tileY == serverClient.spawnTile.y) {
            tileY++;
        }
        while (deathGrave.canPlace(mob.getLevel(), tileX, tileY, 0) != null) {
            if (changeCoord) tileX++;
            else tileY++;
            changeCoord = !changeCoord;
        }
        deathGrave.placeObject(mob.getLevel(), tileX, tileY, 0);
        DeathGrave.DeathGraveInventoryObjectEntity objectEntity = (DeathGrave.DeathGraveInventoryObjectEntity) mob.getLevel().entityManager.getObjectEntity(tileX, tileY);

        for (PickupEntity pickupEntity : mob.getLevel().entityManager.pickups) {
            ItemPickupEntity next = (ItemPickupEntity) pickupEntity;
            objectEntity.getInventory().addItem(mob.getLevel(), null, next.item, "death");
        }

        deathGrave.setServerClient(serverClient);
        mob.getLevel().sendObjectUpdatePacket(tileX, tileY);
    }
}
