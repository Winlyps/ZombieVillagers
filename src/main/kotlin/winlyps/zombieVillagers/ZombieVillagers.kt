package winlyps.zombieVillagers

import org.bukkit.entity.EntityType
import org.bukkit.entity.Zombie
import org.bukkit.entity.ZombieVillager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.plugin.java.JavaPlugin

class ZombieVillagers : JavaPlugin(), Listener {

    override fun onEnable() {
        // Register event listener
        server.pluginManager.registerEvents(this, this)

        // Convert all existing zombies in all loaded worlds
        convertExistingZombies()

        // Plugin startup logic
        logger.info("ZombieVillagers plugin has been enabled!")
    }

    private fun convertExistingZombies() {
        var convertedCount = 0
        
        // Iterate through all loaded worlds
        for (world in server.worlds) {
            // Get all entities in the world
            val entities = world.entities.toList() // Convert to list to avoid concurrent modification
            
            for (entity in entities) {
                // Check if the entity is a zombie (but not already a zombie villager)
                if (entity.type == EntityType.ZOMBIE && entity !is ZombieVillager) {
                    val zombie = entity as Zombie
                    
                    // Store zombie properties
                    val location = zombie.location
                    val health = zombie.health
                    val equipment = zombie.equipment
                    
                    // Remove the original zombie
                    zombie.remove()
                    
                    // Spawn a zombie villager at the same location (with null safety)
                    location.world?.let { world ->
                        val zombieVillager = world.spawnEntity(location, EntityType.ZOMBIE_VILLAGER) as ZombieVillager
                        
                        // Copy properties from the original zombie to the zombie villager
                        zombieVillager.health = health
                        zombieVillager.equipment?.setItemInMainHand(equipment?.itemInMainHand)
                        zombieVillager.equipment?.setItemInOffHand(equipment?.itemInOffHand)
                        zombieVillager.equipment?.helmet = equipment?.helmet
                        zombieVillager.equipment?.chestplate = equipment?.chestplate
                        zombieVillager.equipment?.leggings = equipment?.leggings
                        zombieVillager.equipment?.boots = equipment?.boots
                        
                        convertedCount++
                    }
                }
            }
        }
        
        if (convertedCount > 0) {
            logger.info("Converted $convertedCount existing zombies to zombie villagers")
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
        logger.info("ZombieVillagers plugin has been disabled!")
    }

    @EventHandler
    fun onEntitySpawn(event: EntitySpawnEvent) {
        val entity = event.entity

        // Check if the spawned entity is a zombie (but not already a zombie villager)
        if (entity.type == EntityType.ZOMBIE && entity !is ZombieVillager) {
            val zombie = entity as Zombie

            // Cancel the original zombie spawn
            event.isCancelled = true

            // Spawn a zombie villager at the same location (with null safety)
            val location = zombie.location
            location.world?.let { world ->
                val zombieVillager = world.spawnEntity(location, EntityType.ZOMBIE_VILLAGER) as ZombieVillager

                // Copy properties from the original zombie to the zombie villager
                zombieVillager.health = zombie.health
                zombieVillager.equipment?.setItemInMainHand(zombie.equipment?.itemInMainHand)
                zombieVillager.equipment?.setItemInOffHand(zombie.equipment?.itemInOffHand)
                zombieVillager.equipment?.helmet = zombie.equipment?.helmet
                zombieVillager.equipment?.chestplate = zombie.equipment?.chestplate
                zombieVillager.equipment?.leggings = zombie.equipment?.leggings
                zombieVillager.equipment?.boots = zombie.equipment?.boots
            }
        }
    }
}
