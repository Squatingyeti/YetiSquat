package net.yeticraft.squatingyeti.YetiSquat;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.ChatColor;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.entity.ExperienceOrb;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;

public class YetiSquat extends JavaPlugin implements Listener {
	Logger log = Logger.getLogger("Minecraft");
	
	boolean hasPermission(Player player, String perm) {
        return player.isOp() || player.hasPermission(perm);
}
	
	//completely stole this part because I needed it
    Set<String> squatting = new HashSet<String>();


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
    	// make sure we're dealing with a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("This Command only works for players");
            return true;
        }
        
        // Commence toilet punish
        Player player = (Player) sender;
        if (label.equalsIgnoreCase("squat")) {
        	if(!hasPermission(player, "yetisquat.squat")) {
                player.sendMessage(ChatColor.BLUE + "[YetiSquat] " + ChatColor.YELLOW + "You lack the proper form for correct toilet punishment.");
                return true;
        } 
            squat(player);
            return true;
        }
        // Finished reading Atlas Shrugged...wipe and get back to normal activities    
		else if(label.equalsIgnoreCase("stand")) { 
		  if (!hasPermission(player, "yetisquat.squat")) {
    			player.sendMessage(ChatColor.BLUE + "[YetiSquat] " + ChatColor.YELLOW + "You lack the proper form for correct toilet punishment");
    			return true;
    		}
            stand(player);
            return true;
        }
			return false;
    }
	
    public void onDisable() {
        log.info("YetiSquat Disabled.");
    }

    
    public void onEnable() {
        for (Player player : getServer().getOnlinePlayers()) {
            updateSquatState(player);
        }
        
        // Get the pluginmanager to register our events
        getServer().getPluginManager().registerEvents(this, this);
        log.info("YetiSquat Enabled");
    }
    
    // update squatting state when you join the game
    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerJoin(PlayerJoinEvent event) {
    	//Player pl = event.getPlayer();
        updateSquatState(event.getPlayer());
        //stand(pl);
        
    }
    
    // don't let squatting players make things look like they appear from nowhere...deny drops
    @EventHandler(priority = EventPriority.LOW)
    void onItemDrop(PlayerDropItemEvent event) {
    if (event.isCancelled() ) return;
    Player player = event.getPlayer();
    if (!squatting.contains(player.getName().toLowerCase())) return;
    event.setCancelled(true);
    }
    
    // don't allow squatting players to make things seemingly disappear...deny item pickup
    @EventHandler(priority = EventPriority.LOW)
    void onItemPickUp(PlayerPickupItemEvent event) {
    if (event.isCancelled() ) return;
    Player player = event.getPlayer();
    if (!squatting.contains(player.getName().toLowerCase())) return;
    event.setCancelled(true);
    }
    
    /* stop creepers from getting their creep on while we're getting OUR creep on.
     set the priorities on both eventhandlers to low. Don't know if that's right
     Attempting to stop experience orbs from assaulting you while squatting
     */
    @EventHandler(priority = EventPriority.LOW)
    void onEntityTarget(EntityTargetEvent event) {
        if (event.isCancelled() ) 
        	return;
        Entity target = event.getTarget();
        Entity entity = event.getEntity();
        if (!(target instanceof Player)) 
        	return;
        String playerName = ((Player) target).getName();
        if (squatting.contains(playerName.toLowerCase())) 
        	event.setTarget(null);
        	if (entity instanceof ExperienceOrb) {
        		entity.setVelocity(null);
        	}
    }
    
    // stops entity damage, including fire
    @EventHandler(priority = EventPriority.LOW)
    void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled() ) 
        	return;
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) 
        	return;
        String playerName = ((Player) entity).getName();
        if (!squatting.contains(playerName.toLowerCase())) 
        	return;
        event.setCancelled(true);
        if (entity.getFireTicks()>0) entity.setFireTicks(0);
    }
    
    // our squat method...make sure other players cannot see us
    public void squat(Player player) {
        squatting.add(player.getName().toLowerCase());
        for (Player other : getServer().getOnlinePlayers()) {
            if (!other.equals(player) && other.canSee(player) && !hasPermission(other, "yetisquat.see")) {
            	other.hidePlayer(player);
            }
        }
        player.sendMessage(ChatColor.BLUE +"[YetiSquat] "+ ChatColor.WHITE + "You are now "+ ChatColor.GREEN + "punishing toilets.");

    }
    
    // our stand method...make sure other players can see us again
    public void stand(Player player) {
        squatting.remove(player.getName().toLowerCase());
        for (Player other : getServer().getOnlinePlayers()) {
            if (!other.equals(player) && !other.canSee(player)) {
            	other.showPlayer(player);
            }
        }
        player.sendMessage(ChatColor.BLUE + "[YetiSquat] "+ ChatColor.WHITE + "You are now " + ChatColor.GREEN + "back to kicking ass and taking names.");
    }
    
    // the updateSquatState method...make sure other players can see you if you're not squatting
    public void updateSquatState(Player player){
        String playerName = player.getName();
        Server server = getServer();
        if (squatting.contains(playerName.toLowerCase())) squat(player);
        else{
            for (Player looking : server.getOnlinePlayers()){
                if (!looking.canSee(player)) looking.showPlayer(player);
            }
        }
        
        if (!hasPermission(player, "yetisquat.see")) {
        	for (String name : squatting) {
        		if(name.equals(playerName)) continue;
        		Player looking = server.getPlayerExact(name);
        		if (looking != null && player.canSee(looking)) {
        			player.hidePlayer(looking);
        		}
        	}
        } 
    	else {
    	  for (String name : squatting) {
    		  if (name.equals(playerName)) continue;
    		  Player looking = server.getPlayerExact(name);
    		  if (looking != null && !player.canSee(looking)) {
    			  player.showPlayer(looking);
    	     }
          }
       }
    }
}
        