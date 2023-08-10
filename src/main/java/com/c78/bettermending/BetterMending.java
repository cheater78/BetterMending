package com.c78.bettermending;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class BetterMending extends JavaPlugin implements Listener {
    class Settings{
        public double damageRepairPerXP = 1;

    }
    File m_SettingsFile;
    Settings m_Settings;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this,this);

        String path = "";
        try{
            path = BetterMending.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        path = path.replace("BetterMending.jar" , "");

        new File(path + "/BetterMending").mkdirs();

        m_SettingsFile = new File(path + "/BetterMending/settings.json");
        if(!m_SettingsFile.exists()) {
            try {
                m_SettingsFile.createNewFile();
                m_Settings = new Settings();
                storeSettings();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else {
            try {
                loadSettings();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Bukkit.getLogger().info("BetterMending started...");
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("BetterMending stopped...");
    }

    @Override
    public boolean onCommand(CommandSender s, Command c, String label, String[] args){


        return true;
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e){
        if(e.getCurrentItem() == null) return;
        if(e.isRightClick() && e.getInventory().getHolder() instanceof Player){
            Player p = (Player)e.getInventory().getHolder();
            ItemStack iStack =  e.getCurrentItem();
            if(iStack.getAmount() == 1 && iStack.getEnchantments().keySet().contains(Enchantment.MENDING)){
                ItemMeta iMeta = iStack.getItemMeta();
                if(iMeta instanceof Damageable){
                    Damageable iDmeta = (Damageable)iMeta;

                    if(iDmeta.getDamage() > 0) {
                        int damage = iDmeta.getDamage();

                        int plvl = p.getLevel();
                        float pExp = p.getExp();

                        int exp = 0;

                        if (plvl < 17) {
                            exp += (int) ((2 * ((float) plvl) + 7) * pExp);
                            exp += plvl * plvl + 6 * plvl;
                        } else if (plvl < 32) {
                            exp += (int) ((5 * ((float) plvl) - 38) * pExp);
                            exp += (int) (plvl * plvl * 2.5 - 40.5 * plvl + 360);
                        } else {
                            exp += (int) ((9 * ((float) plvl) - 158) * pExp);
                            exp += (int) (plvl * plvl * 4.5 - 162.5 * plvl + 2220);
                        }
                        if (exp > 0) {
                            int nexp = exp;

                            if (damage >= (int) (m_Settings.damageRepairPerXP * exp)) {
                                damage -= (int) (m_Settings.damageRepairPerXP * exp);
                                nexp = 0;
                            } else {
                                nexp -= (int) (damage / m_Settings.damageRepairPerXP);
                                damage = 0;
                            }

                            p.giveExp(nexp - exp);
                            iDmeta.setDamage(damage);
                            iStack.setItemMeta(iMeta);
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }




    }
    private void storeSettings () throws IOException {
        Gson gson = new Gson();
        String json = gson.toJson(m_Settings);

        if(m_SettingsFile.canWrite()){
            Files.write(m_SettingsFile.toPath(), json.getBytes());
        }

    }

    private void loadSettings() throws IOException {
        String json = Files.readString(m_SettingsFile.toPath());


        m_Settings = new Gson().fromJson(json, Settings.class);
    }
}
