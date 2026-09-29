package net.countercraft.movecraft.mapUpdater.update;

import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.events.ExplosionEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.Waterlogged;

import java.util.Objects;

public class ExplosionUpdateCommand extends UpdateCommand {
    private final Location explosionLocation;
    private final float explosionStrength;
    private final boolean incendiary;

    public ExplosionUpdateCommand(Location explosionLocation, float explosionStrength, boolean incendiary) throws IllegalArgumentException {
        if(explosionStrength < 0){
            throw new IllegalArgumentException("Explosion strength cannot be negative");
        }
        this.explosionLocation = explosionLocation;
        this.explosionStrength = explosionStrength;
        this.incendiary = incendiary;
    }

    public Location getLocation() {
        return explosionLocation;
    }

    public float getStrength() {
        return explosionStrength;
    }

    public boolean isIncendiary() {
        return incendiary;
    }

    @Override
    public void doUpdate() {
        ExplosionEvent e = new ExplosionEvent(explosionLocation, explosionStrength, incendiary);
        Bukkit.getServer().getPluginManager().callEvent(e);
        if(e.isCancelled())
            return;

        if (Settings.Debug) {
            Bukkit.broadcastMessage("Explosion strength: " + explosionStrength + " at " + explosionLocation.toVector().toString());
        }

        if (Settings.WarheadsDrainWaterloggedBlocks){
            this.dewaterlog(explosionLocation.add(.5,.5,.5), explosionStrength);
        }
        this.createExplosion(explosionLocation.add(.5,.5,.5), explosionStrength, incendiary);
    }

    private void dewaterlog(Location loc, float explosionPower){
        // Get a hitbox (try for sphere, but settle for cubiod if that's too hard)
        int radius = (int) Math.ceil(explosionPower);
        for (int i = -1*radius; i <= radius; i++ ){
            for (int j = -1*radius; j <= radius; j++){
                for (int k = -1*radius; k <= radius; k++)
                {
                    if (i*i + j*j + k*k <= explosionPower * explosionPower){
                        // get location, then get block, then
                        Location testLoc = loc.clone().add(i,j,k);
                        var block = testLoc.getBlock();
                        var data = block.getBlockData();
                        if(!(data instanceof Waterlogged)){
                            continue;
                        }
                        ((Waterlogged) data).setWaterlogged(false);
                        block.setBlockData(data, true);
                    }
                }
            }
        }

        //
    }

    private void createExplosion(Location loc, float explosionPower, boolean incendiary) {
        loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), explosionPower, incendiary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(explosionLocation, explosionStrength);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ExplosionUpdateCommand)){
            return false;
        }
        ExplosionUpdateCommand other = (ExplosionUpdateCommand) obj;
        return this.explosionLocation.equals(other.explosionLocation) &&
                this.explosionStrength == other.explosionStrength &&
                this.incendiary == other.incendiary;
    }
}
