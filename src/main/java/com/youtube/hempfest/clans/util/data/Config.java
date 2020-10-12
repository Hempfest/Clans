package com.youtube.hempfest.clans.util.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Config {
    private final String n;
    private final String d;
    private FileConfiguration fc;
    private File file;
    private final JavaPlugin plugin;
    private static List<Config> configs;

    static {
        Config.configs = new ArrayList<Config>();
    }

    public Config(final String n, final String d) {
        this.plugin = JavaPlugin.getProvidingPlugin(this.getClass());
        this.n = n;
        this.d = d;
        Config.configs.add(this);
    }

    public static void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        if(this.n == null) {
            try {
                throw new Exception();
            }catch(final Exception e) {
                e.printStackTrace();
            }
        }
        return this.n;
    }

    public JavaPlugin getInstance() {
        if(this.plugin == null) {
            try {
                throw new Exception();
            }catch(final Exception e) {
                e.printStackTrace();
            }
        }
        return this.plugin;
    }


    public static Config getConfig(final String n, final String d) {
        for(final Config c: Config.configs) {
            if(c.getName().equals(n)) {
                return c;
            }
        }
        if (d != null) {

            return new Config(n, d);
        } else
        return new Config(n, null);
    }

    public boolean delete() {
        return this.getFile().delete();
    }

    public boolean exists() {
        if(this.fc == null || this.file == null) {
            final File temp = new File(this.getDataFolder(), String.valueOf(this.getName()) + ".yml");
            if(!temp.exists()) {
                return false;
            }
            this.file = temp;
        }
        return true;
    }

    public File getFile() {
        if(this.file == null) {
            this.file = new File(this.getDataFolder(), String.valueOf(this.getName() + ".yml")); //create method get data folder
            if(!this.file.exists()) {
                try {
                    this.file.createNewFile();
                }catch(final IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return this.file;
    }

    public FileConfiguration getConfig() {
        if(this.fc == null) {
            this.fc = YamlConfiguration.loadConfiguration(this.getFile());
        }
        return this.fc;
    }

    public File getDataFolder() {
        final File dir = new File(Config.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll("%20", " "));
        File d = null;
        if (this.d != null) {
            d = new File(dir.getParentFile().getPath(), this.getInstance().getName() + "/" + this.d + "/");
        } else {
            d = new File(dir.getParentFile().getPath(), this.getInstance().getName());
        }
        if(!d.exists()) {
            d.mkdirs();
        }
        return d;
    }

    public void reload() {
        if(this.file == null) {
            this.file = new File(this.getDataFolder(), String.valueOf(this.getName() + ".yml"));
            if(!this.file.exists()) {
                try {
                    this.file.createNewFile();
                }catch(final IOException e) {
                    e.printStackTrace();
                }
            }

            this.fc = YamlConfiguration.loadConfiguration(this.file);
            final File defConfigStream = new File(this.plugin.getDataFolder(), String.valueOf(this.getName())+ ".yml");
            if(defConfigStream != null) {
                final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                this.fc.setDefaults(defConfig);
            }
        }
    }

    public void saveConfig() {
        try {
            this.getConfig().save(this.getFile());
        }catch(final IOException e) {
            e.printStackTrace();
        }
    }






}

