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

package de.Keyle.MyWolf.skill;

import de.Keyle.MyWolf.MyWolf;
import de.Keyle.MyWolf.MyWolfPlugin;
import de.Keyle.MyWolf.event.MyWolfExpEvent;
import de.Keyle.MyWolf.event.MyWolfLevelUpEvent;
import de.Keyle.MyWolf.util.MyWolfUtil;
import org.bukkit.entity.EntityType;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

public class MyWolfExperience
{
    private final MyWolf MWolf;

    private double Exp = 0;
    public static boolean defaultEXPvalues = true;

    public static String JSreader = null;

    public static final Map<EntityType, Double> MobEXP = new HashMap<EntityType, Double>();

    static
    {
        MobEXP.put(EntityType.SKELETON, 1.1);
        MobEXP.put(EntityType.ZOMBIE, 1.1);
        MobEXP.put(EntityType.SPIDER, 1.05);
        MobEXP.put(EntityType.WOLF, 0.5);
        MobEXP.put(EntityType.CREEPER, 1.55);
        MobEXP.put(EntityType.GHAST, 0.85);
        MobEXP.put(EntityType.PIG_ZOMBIE, 1.1);
        MobEXP.put(EntityType.GIANT, 10.75);
        MobEXP.put(EntityType.COW, 0.25);
        MobEXP.put(EntityType.PIG, 0.25);
        MobEXP.put(EntityType.CHICKEN, 0.1);
        MobEXP.put(EntityType.SQUID, 0.25);
        MobEXP.put(EntityType.SHEEP, 0.25);
    }

    public MyWolfExperience(MyWolf Wolf)
    {
        this.MWolf = Wolf;
        MyWolfPlugin.getPlugin().getServer().getPluginManager().callEvent(new MyWolfLevelUpEvent(MWolf, 1, true));
    }

    public void reset()
    {
        Exp = 0;
        MyWolfPlugin.getPlugin().getServer().getPluginManager().callEvent(new MyWolfLevelUpEvent(MWolf, 1, true));
    }

    public void setExp(double Exp)
    {
        MyWolfExpEvent event = new MyWolfExpEvent(MWolf, this.getExp(), Exp);
        MyWolfPlugin.getPlugin().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled())
        {
            return;
        }
        int tmplvl = getLevel();
        this.Exp = event.getEXP();
        for (int i = tmplvl; i < getLevel(); i++)
        {
            MyWolfPlugin.getPlugin().getServer().getPluginManager().callEvent(new MyWolfLevelUpEvent(MWolf, i + 1, true));
        }
    }

    public double getExp()
    {
        return this.Exp;
    }

    public void addExp(double Exp)
    {
        MyWolfExpEvent event = new MyWolfExpEvent(MWolf, this.Exp, this.Exp + Exp);
        MyWolfPlugin.getPlugin().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled())
        {
            return;
        }
        int tmplvl = getLevel();
        this.Exp = event.getEXP();

        for (int i = tmplvl; i < getLevel(); i++)
        {
            MyWolfPlugin.getPlugin().getServer().getPluginManager().callEvent(new MyWolfLevelUpEvent(MWolf, i + 1));
        }
    }

    public void addExp(EntityType type)
    {
        if (MobEXP.containsKey(type))
        {
            MyWolfExpEvent event = new MyWolfExpEvent(MWolf, this.Exp, MobEXP.get(type) + this.Exp);
            MyWolfPlugin.getPlugin().getServer().getPluginManager().callEvent(event);
            if (event.isCancelled())
            {
                return;
            }
            int tmplvl = getLevel();
            this.Exp = event.getEXP();
            for (int i = tmplvl; i < getLevel(); i++)
            {
                MyWolfPlugin.getPlugin().getServer().getPluginManager().callEvent(new MyWolfLevelUpEvent(MWolf, i + 1));
            }
        }
    }

    public int getLevel()
    {
        if (JSreader != null)
        {
            ScriptEngine se = parseJS();
            try
            {
                return ((Double) se.get("lvl")).intValue();
            }
            catch (Exception e)
            {
                MyWolfUtil.getLogger().info("[MyWolf] EXP-Script doesn't return valid value!");
                return 1;
            }
        }
        else
        {
            // Minecraft:   E = 7 + roundDown( n * 3.5)

            double tmpEXP = this.Exp;
            int tmplvl = 0;

            while (tmpEXP >= 7 + (int) ((tmplvl) * 3.5))
            {
                tmpEXP -= 7 + (int) ((tmplvl) * 3.5);
                tmplvl++;
            }
            //MyWolfUtil.Log.info(tmplvl+1 + " - " + tmpEXP + " - " + (7 + (int)((tmplvl) * 3.5)) + " - " + this.getExp());
            return tmplvl + 1;
        }
    }

    public double getActualEXP()
    {
        double tmpEXP = this.Exp;
        int tmplvl = 0;

        while (tmpEXP >= 7 + (int) ((tmplvl) * 3.5))
        {
            tmpEXP -= 7 + (int) ((tmplvl) * 3.5);
            tmplvl++;
        }
        return tmpEXP;
    }

    public double getrequireEXP()
    {
        if (JSreader != null)
        {
            ScriptEngine se = parseJS();
            try
            {
                return ((Double) se.get("reqEXP"));
            }
            catch (Exception e)
            {
                MyWolfUtil.getLogger().info("[MyWolf] EXP-Script doesn't return valid value!");
                return 1;
            }
        }
        else
        {
            //MyWolfUtil.Log.info(""+(7 + (int)((this.getLevel()-1) * 3.5)));
            return 7 + (int) ((this.getLevel() - 1) * 3.5);
        }
    }

    ScriptEngine parseJS()
    {
        if (JSreader != null)
        {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("js");
            engine.put("lvl", 1);
            engine.put("reqEXP", 0);

            engine.put("EXP", Exp);
            engine.put("name", MWolf.Name);
            engine.put("player", MWolf.Owner);
            try
            {
                engine.eval(JSreader);
            }
            catch (ScriptException e)
            {
                MyWolfUtil.getLogger().info("[MyWolf] Error in EXP-Script!");
                return null;
            }
            return engine;
        }
        else
        {
            return null;
        }
    }
}
