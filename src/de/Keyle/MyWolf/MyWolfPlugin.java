/*
 * Copyright (C) 2011-2012 Keyle
 *
 * This file is part of MyWolf
 *
 * MyWolf is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyWolf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyWolf. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyWolf;

import de.Keyle.MyWolf.MyWolf.WolfState;
import de.Keyle.MyWolf.chatcommands.*;
import de.Keyle.MyWolf.entity.EntityMyWolf;
import de.Keyle.MyWolf.listeners.*;
import de.Keyle.MyWolf.skill.MyWolfExperience;
import de.Keyle.MyWolf.skill.MyWolfGenericSkill;
import de.Keyle.MyWolf.skill.MyWolfSkillSystem;
import de.Keyle.MyWolf.skill.skills.*;
import de.Keyle.MyWolf.util.*;
import de.Keyle.MyWolf.util.MyWolfPermissions.PermissionsType;
import de.Keyle.MyWolf.util.configuration.MyWolfNBTConfiguration;
import de.Keyle.MyWolf.util.configuration.MyWolfYamlConfiguration;
import net.minecraft.server.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class MyWolfPlugin extends JavaPlugin
{
    private static MyWolfPlugin Plugin;
    public static MyWolfLanguage MWLanguage;

    private MyWolfTimer Timer = new MyWolfTimer();

    public static final List<Player> WolfChestOpened = new ArrayList<Player>();

    public static File NBTWolvesFile;

    public static MyWolfPlugin getPlugin()
    {
        return Plugin;
    }

    public void onDisable()
    {
        saveWolves(NBTWolvesFile);
        for (MyWolf MWolf : MyWolfList.getMyWolfList())
        {
            if (MWolf.Status == WolfState.Here)
            {
                MWolf.removeWolf();
            }
        }

        MyWolfList.clearList();
        WolfChestOpened.clear();
        getPlugin().getServer().getScheduler().cancelTasks(getPlugin());

        MyWolfUtil.getLogger().info("[MyWolf] Disabled");
    }

    public void onEnable()
    {
        Plugin = this;

        MyWolfConfig.Config = this.getConfig();
        MyWolfConfig.setDefault();
        MyWolfConfig.loadConfiguration();

        MyWolfPlayerListener playerListener = new MyWolfPlayerListener();
        getServer().getPluginManager().registerEvents(playerListener, this);

        MyWolfVehicleListener vehicleListener = new MyWolfVehicleListener();
        getServer().getPluginManager().registerEvents(vehicleListener, this);

        MyWolfWorldListener worldListener = new MyWolfWorldListener();
        getServer().getPluginManager().registerEvents(worldListener, this);

        MyWolfEntityListener entityListener = new MyWolfEntityListener();
        getServer().getPluginManager().registerEvents(entityListener, this);

        MyWolfLevelUpListener levelupListener = new MyWolfLevelUpListener();
        getServer().getPluginManager().registerEvents(levelupListener, this);

        getCommand("wolfname").setExecutor(new CommandName());
        getCommand("wolfcall").setExecutor(new CommandCall());
        getCommand("wolfstop").setExecutor(new CommandStop());
        getCommand("wolfrelease").setExecutor(new CommandRelease());
        getCommand("mywolf").setExecutor(new CommandHelp());
        getCommand("wolfinventory").setExecutor(new CommandInventory());
        getCommand("wolfpickup").setExecutor(new CommandPickup());
        getCommand("wolfbehavior").setExecutor(new CommandBehavior());
        getCommand("wolfinfo").setExecutor(new CommandInfo());
        getCommand("wolfadmin").setExecutor(new CommandAdmin());
        getCommand("wolfskill").setExecutor(new CommandSkill());

        MyWolfYamlConfiguration MWSkillTreeConfig = new MyWolfYamlConfiguration(this.getDataFolder().getPath() + File.separator + "skill.yml");
        if (!MWSkillTreeConfig.ConfigFile.exists())
        {
            try
            {
                InputStream template = getPlugin().getResource("skill.yml");
                OutputStream out = new FileOutputStream(MWSkillTreeConfig.ConfigFile);

                byte[] buf = new byte[1024];
                int len;
                while ((len = template.read(buf)) > 0)
                {
                    out.write(buf, 0, len);
                }
                template.close();
                out.close();
                MyWolfUtil.getLogger().info("[MyWolf] Default skill.yml file created. Please restart the server to load the skilltrees!");
            }
            catch (IOException ex)
            {
                MyWolfUtil.getLogger().info("[MyWolf] Unable to create the default skill.yml file!");
            }
        }
        MyWolfSkillTreeConfigLoader.setConfig(MWSkillTreeConfig);
        MyWolfSkillTreeConfigLoader.loadSkillTrees();


        MyWolfSkillSystem.registerSkill(Inventory.class);
        MyWolfSkillSystem.registerSkill(HPregeneration.class);
        MyWolfSkillSystem.registerSkill(Pickup.class);
        MyWolfSkillSystem.registerSkill(Behavior.class);
        MyWolfSkillSystem.registerSkill(Damage.class);
        MyWolfSkillSystem.registerSkill(Control.class);
        MyWolfSkillSystem.registerSkill(HP.class);

        try
        {
            Class[] args = {Class.class, String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE};
            Method a = EntityTypes.class.getDeclaredMethod("a", args);
            a.setAccessible(true);
            a.invoke(a, EntityMyWolf.class, "Wolf", 95, 14144467, 13545366);
            a.invoke(a, EntityWolf.class, "Wolf", 95, 14144467, 13545366);
        }
        catch (Exception e)
        {
            MyWolfUtil.getLogger().info("[MyWolf] version " + MyWolfPlugin.Plugin.getDescription().getVersion() + " NOT ENABLED");
            e.printStackTrace();
            setEnabled(false);
            return;
        }

        // For future of the client mod
        //this.getServer().getMessenger().registerOutgoingPluginChannel(this,"MyWolfByKeyle");

        if (MyWolfConfig.PermissionsBukkit)
        {
            MyWolfPermissions.setup(PermissionsType.BukkitPermissions);
        }
        else
        {
            MyWolfPermissions.setup();
        }

        MWLanguage = new MyWolfLanguage(new MyWolfYamlConfiguration(this.getDataFolder().getPath() + File.separator + "lang.yml"));
        MWLanguage.loadVariables();


        if (MyWolfConfig.LevelSystem)
        {
            try
            {
                MyWolfExperience.JSreader = MyWolfUtil.readFileAsString(MyWolfPlugin.Plugin.getDataFolder().getPath() + File.separator + "exp.js");
            }
            catch (Exception e)
            {
                MyWolfExperience.JSreader = null;
                MyWolfUtil.getLogger().info("[MyWolf] No custom EXP-Script found (exp.js).");
            }
        }

        File MWWolvesConfigFile = new File(this.getDataFolder().getPath() + File.separator + "Wolves.yml");
        NBTWolvesFile = new File(this.getDataFolder().getPath() + File.separator + "Wolves.MyWolf");

        if (MWWolvesConfigFile.exists())
        {
            MyWolfYamlConfiguration MWWolvesConfig = new MyWolfYamlConfiguration(MWWolvesConfigFile);
            loadWolves(MWWolvesConfig);
            MWWolvesConfigFile.renameTo(new File(this.getDataFolder().getPath() + File.separator + "oldWolves.yml"));
        }
        else
        {
            loadWolves(NBTWolvesFile);
        }

        Timer.startTimer();

        if(MyWolfConfig.sendMetrics)
        {
            try
            {
                Metrics metrics = new Metrics();

                metrics.addCustomData(getPlugin(), new Metrics.Plotter("Total MyWolves")
                {
                    @Override
                    public int getValue()
                    {
                        return MyWolfList.getMyWolfCount();
                    }
                });

                metrics.beginMeasuringPlugin(getPlugin());
            }
            catch (IOException e)
            {
                MyWolfUtil.getLogger().info(e.getMessage());
            }
        }
        MyWolfUtil.getLogger().info("[MyWolf] version " + MyWolfPlugin.Plugin.getDescription().getVersion() + " ENABLED");
    }

    int loadWolves(File f)
    {
        int anzahlWolves = 0;

        MyWolfNBTConfiguration nbtConfiguration = new MyWolfNBTConfiguration(f);
        nbtConfiguration.load();
        NBTTagList Wolves = nbtConfiguration.getNBTTagCompound().getList("Wolves");

        for (int i = 0; i < Wolves.size(); i++)
        {
            NBTTagCompound MWolfNBT = (NBTTagCompound) Wolves.get(i);
            NBTTagCompound Location = MWolfNBT.getCompound("Location");

            double WolfX = Location.getDouble("X");
            double WolfY = Location.getDouble("Y");
            double WolfZ = Location.getDouble("Z");
            String WolfWorld = Location.getString("World");
            double WolfEXP = MWolfNBT.getDouble("Exp");
            int WolfHealthNow = MWolfNBT.getInt("Health");
            int WolfRespawnTime = MWolfNBT.getInt("Respawntime");
            String WolfName = MWolfNBT.getString("Name");
            String Owner = MWolfNBT.getString("Owner");
            boolean WolfSitting = MWolfNBT.getBoolean("Sitting");

            InactiveMyWolf IMWolf = new InactiveMyWolf(MyWolfUtil.getOfflinePlayer(Owner));

            IMWolf.setLocation(new Location(this.getServer().getWorld(WolfWorld) != null ? this.getServer().getWorld(WolfWorld) : this.getServer().getWorlds().get(0), WolfX, WolfY, WolfZ));
            IMWolf.setHealth(WolfHealthNow);
            IMWolf.setRespawnTime(WolfRespawnTime);
            IMWolf.setName(WolfName);
            IMWolf.setSitting(WolfSitting);
            IMWolf.setExp(WolfEXP);
            IMWolf.setSkills(MWolfNBT.getCompound("Skills"));

            MyWolfList.addInactiveMyWolf(IMWolf);

            anzahlWolves++;
        }
        MyWolfUtil.getLogger().info("[MyWolf] " + anzahlWolves + " wolf/wolves loaded");
        return anzahlWolves;
    }

    int loadWolves(MyWolfYamlConfiguration MWC)
    {
        int anzahlWolves = 0;
        if (MWC.getConfig().contains("Wolves"))
        {
            Set<String> WolfList = MWC.getConfig().getConfigurationSection("Wolves").getKeys(false);
            if (WolfList.size() != 0)
            {
                for (String ownername : WolfList)
                {
                    double WolfX = MWC.getConfig().getDouble("Wolves." + ownername + ".loc.X", 0);
                    double WolfY = MWC.getConfig().getDouble("Wolves." + ownername + ".loc.Y", 0);
                    double WolfZ = MWC.getConfig().getDouble("Wolves." + ownername + ".loc.Z", 0);
                    double WolfEXP = MWC.getConfig().getDouble("Wolves." + ownername + ".exp", 0);
                    String WolfWorld = MWC.getConfig().getString("Wolves." + ownername + ".loc.world", getServer().getWorlds().get(0).getName());
                    int WolfHealthNow = MWC.getConfig().getInt("Wolves." + ownername + ".health.now", 6);
                    int WolfRespawnTime = MWC.getConfig().getInt("Wolves." + ownername + ".health.respawntime", 0);
                    String WolfName = MWC.getConfig().getString("Wolves." + ownername + ".name", "Wolf");
                    boolean WolfSitting = MWC.getConfig().getBoolean("Wolves." + ownername + ".sitting", false);

                    NBTTagCompound Skills = new NBTTagCompound("Skills");
                    if (MWC.getConfig().contains("Wolves." + ownername + ".inventory"))
                    {
                        String Sinv = MWC.getConfig().getString("Wolves." + ownername + ".inventory", "QwE");
                        if (!Sinv.equals("QwE"))
                        {
                            String[] invSplit = Sinv.split(";");
                            MyWolfCustomInventory inv = new MyWolfCustomInventory(WolfName);
                            for (int i = 0; i < invSplit.length; i++)
                            {
                                String[] itemvalues = invSplit[i].split(",");
                                if (itemvalues.length == 3 && MyWolfUtil.isInt(itemvalues[0]) && MyWolfUtil.isInt(itemvalues[1]) && MyWolfUtil.isInt(itemvalues[2]))
                                {
                                    if (org.bukkit.Material.getMaterial(Integer.parseInt(itemvalues[0])) != null)
                                    {
                                        if (Integer.parseInt(itemvalues[1]) <= 64)
                                        {
                                            inv.setItem(i, new ItemStack(Integer.parseInt(itemvalues[0]), Integer.parseInt(itemvalues[1]), Integer.parseInt(itemvalues[2])));
                                        }
                                    }
                                }
                            }
                            Skills.set("Inventory", inv.save(new NBTTagCompound("Inventory")));
                        }
                    }

                    InactiveMyWolf IMWolf = new InactiveMyWolf(MyWolfUtil.getOfflinePlayer(ownername));

                    IMWolf.setLocation(new Location(this.getServer().getWorld(WolfWorld) != null ? this.getServer().getWorld(WolfWorld) : this.getServer().getWorlds().get(0), WolfX, WolfY, WolfZ));
                    IMWolf.setHealth(WolfHealthNow);
                    IMWolf.setRespawnTime(WolfRespawnTime);
                    IMWolf.setName(WolfName);
                    IMWolf.setSitting(WolfSitting);
                    IMWolf.setExp(WolfEXP);
                    IMWolf.setSkills(Skills);

                    MyWolfList.addInactiveMyWolf(IMWolf);

                    anzahlWolves++;
                }
            }
        }
        MyWolfUtil.getLogger().info("[MyWolf] " + anzahlWolves + " wolf/wolves loaded");
        return anzahlWolves;
    }

    public void saveWolves(File f)
    {
        MyWolfNBTConfiguration nbtConfiguration = new MyWolfNBTConfiguration(f);
        NBTTagList Wolves = new NBTTagList();
        for (MyWolf MWolf : MyWolfList.getMyWolfList())
        {

            NBTTagCompound Wolf = new NBTTagCompound();

            NBTTagCompound Location = new NBTTagCompound("Location");
            Location.setDouble("X", MWolf.getLocation().getX());
            Location.setDouble("Y", MWolf.getLocation().getY());
            Location.setDouble("Z", MWolf.getLocation().getZ());
            Location.setString("World", MWolf.getLocation().getWorld().getName());

            Wolf.setString("Owner", MWolf.getOwner().getName());
            Wolf.setCompound("Location", Location);
            Wolf.setInt("Health", MWolf.getHealth());
            Wolf.setInt("Respawntime", MWolf.RespawnTime);
            Wolf.setString("Name", MWolf.Name);
            Wolf.setBoolean("Sitting", MWolf.isSitting());
            Wolf.setDouble("Exp", MWolf.Experience.getExp());

            NBTTagCompound SkillsNBTTagCompound = new NBTTagCompound("Skills");
            Collection<MyWolfGenericSkill> Skills = MWolf.SkillSystem.getSkills();
            if (Skills.size() > 0)
            {
                for (MyWolfGenericSkill Skill : Skills)
                {
                    NBTTagCompound s = Skill.save();
                    if (s != null)
                    {
                        SkillsNBTTagCompound.set(Skill.getName(), s);
                    }
                }
            }
            Wolf.set("Skills", SkillsNBTTagCompound);
            Wolves.add(Wolf);
        }
        for (InactiveMyWolf IMWolf : MyWolfList.getInactiveMyWolfList())
        {

            NBTTagCompound Wolf = new NBTTagCompound();

            NBTTagCompound Location = new NBTTagCompound("Location");
            Location.setDouble("X", IMWolf.getLocation().getX());
            Location.setDouble("Y", IMWolf.getLocation().getY());
            Location.setDouble("Z", IMWolf.getLocation().getZ());
            Location.setString("World", IMWolf.getLocation().getWorld().getName());

            Wolf.setString("Owner", IMWolf.getOwner().getName());
            Wolf.setCompound("Location", Location);
            Wolf.setInt("Health", IMWolf.getHealth());
            Wolf.setInt("Respawntime", IMWolf.getRespawnTime());
            Wolf.setString("Name", IMWolf.getName());
            Wolf.setBoolean("Sitting", IMWolf.isSitting());
            Wolf.setDouble("Exp", IMWolf.getExp());

            Wolf.set("Skills", IMWolf.getSkills());
            Wolves.add(Wolf);
        }
        nbtConfiguration.getNBTTagCompound().set("Wolves", Wolves);
        nbtConfiguration.save();
    }
}