package com.youtube.hempfest.clans.metadata;

import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import com.youtube.hempfest.hempcore.library.HFEncoded;
import com.youtube.hempfest.hempcore.library.HUID;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;

public class PersistentClan extends ClanMeta implements Serializable {

	private static final Map<HUID, ClanMeta> metaDataContainer = new HashMap<>();

	private final String clanID;

	private final HUID huid;

	private String value;

	private final PersistentClan instance;

	private static boolean debugging;

	private final List<String> values = new ArrayList<>();

	public PersistentClan(String clanID) {
		this.clanID = clanID;
		instance = this;
		huid = HUID.randomID();
	}

	@Override
	public HUID getId() {
		return huid;
	}

	@Override
	public String value() {
		return value;
	}

	@Override
	public String value(int index) {
		return values.get(index);
	}

	@Override
	public Clan getClan() {
		return Clan.clanUtil.getClan(clanID);
	}

	/**
	 * Save any specified object to the meta data.
	 * @param o The object data to be stored within the container.
	 */
	public void setValue(Object o) {
		try {
			this.value = new HFEncoded(o).serialize();
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe("Unable to parse object.");
			e.printStackTrace();
		}
	}

	/**
	 * Save any specified object to the meta data large container.
	 * @param o The object to insert
	 * @param index Insert an object into the value array at the given position.
	 */
	public void setValue(Object o, int index) {
		try {
			this.values.add(index, new HFEncoded(o).serialize());
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe("Unable to parse object.");
			e.printStackTrace();
		}
	}

	/**
	 * Get all currently loaded meta id's
	 * @return Gets an array of all loaded data id's
	 */
	public static HUID[] getMetaDataContainer() {
		List<HUID> array = new ArrayList<>();
		for (Map.Entry<HUID, ClanMeta> entry : metaDataContainer.entrySet()) {
			array.add(entry.getKey());
		}
		return array.toArray(new HUID[0]);
	}

	/**
	 * Get all currently loaded meta id's by clan delimiter
	 * @param clanID The clan to query
	 * @return Gets an array of all loaded data id's by clan delimiter
	 */
	public static HUID[] getClanContainer(String clanID) {
		List<HUID> array = new ArrayList<>();
		for (HUID id : getMetaDataContainer()) {
			ClanMeta meta = loadTempInstance(id);
			if (meta.getClan().getClanID().equals(clanID)) {
				array.add(id);
			}
		}
		return array.toArray(new HUID[0]);
	}

	/**
	 * @param b true = console information displays every action used.
	 */
	public void setDebugging(boolean b) {
		debugging = b;
	}

	/**
	 * Load an instance of meta data from hard storage.
	 * @param huid The id to load from storage.
	 * @return Gets a saved data instance
	 */
	public static PersistentClan loadSavedInstance(HUID huid) {
		DataManager dm = new DataManager(huid.toString(), "Meta");
		Config meta = dm.getFile(ConfigType.MISC_FILE);
		PersistentClan persistentClan = null;
		if (meta.exists()) {
			try {
				PersistentClan instance = (PersistentClan) new HFEncoded(meta.getConfig().getString("Data")).deserialized();
				metaDataContainer.put(huid, instance);
				persistentClan = instance;
			} catch (IOException | ClassNotFoundException e) {
				Bukkit.getServer().getLogger().severe("[Clans] - Instance not loadable. One or more values changed or object location changed.");
				e.printStackTrace();
			}
		} else {
			Bukkit.getServer().getLogger().severe("[Clans] - No saved meta data can be found. Are you sure you saved it?");
		}
		if (persistentClan == null) {
			Bukkit.getServer().getLogger().severe("[Clans] - Failed attempt at loading non existent instance of HUID link");
		}
		return persistentClan;
	}

	/**
	 * Load an instance of meta data from cache
	 * @param huid The id to load from cache
	 * @return Gets a cached data instance.
	 */
	public static ClanMeta loadTempInstance(HUID huid) {
		ClanMeta meta = null;
		for (HUID entry : getMetaDataContainer()) {
			if (entry.toString().equals(huid.toString())) {
				meta = metaDataContainer.get(entry);
			}
		}
		return meta;
	}

	/**
	 * Delete an instance of meta data from both cache and hard storage.
	 * @param huid The id to delete from cache/storage
	 */
	public static void deleteInstance(HUID huid) {
		Arrays.stream(getMetaDataContainer()).forEach(I -> {
			if (I.toString().equals(huid.toString())) {
				DataManager cm = new DataManager(metaDataContainer.get(I).getClan().getClanID());
				Config clan = cm.getFile(ConfigType.CLAN_FILE);
				if (huid.toString().equals(clan.getConfig().getString("NO-ID"))) {
					clan.getConfig().set("NO-ID", null);
					clan.saveConfig();
				}
				if (!clan.getConfig().isConfigurationSection("Data")) {
					throw new NullPointerException("[Clans] - No data is currently saved.");
				}
				for (String d : clan.getConfig().getConfigurationSection("Data").getKeys(false)) {
					if (d.equals(huid.toString())) {
						clan.getConfig().set("Data." + d, null);
						clan.saveConfig();
						break;
					}
				}
				DataManager dm = new DataManager(I.toString(), "Meta");
				Config meta = dm.getFile(ConfigType.MISC_FILE);
				meta.delete();
				if (debugging) {
					Bukkit.getServer().getLogger().info("[Clans] - Instance for ID #" + I.toString() + " deleted.");
				}
				metaDataContainer.remove(I);
			}
		});
	}

	/**
	 * Load all storage saved clan meta into cache,
	 * this should not be used as it is already logged on server enable.
	 */
	public static void querySaved() {
		final File dir = new File(Config.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll("%20", " "));
		File file = new File(dir.getParentFile().getPath(), HempfestClans.getInstance().getName() + "/Meta/");
		Arrays.stream(file.listFiles()).forEach(f -> {
			HUID id = HUID.fromString(f.getName().replace(".yml", ""));
			PersistentClan m = loadSavedInstance(id);
			metaDataContainer.put(id, m);
		});
	}

	/**
	 * Store the clan meta into temp storage.
	 */
	public void storeTemp() {
		metaDataContainer.put(huid, instance);
		if (debugging) {
			Bukkit.getServer().getLogger().info("[Clans] - Instance for ID #" + instance.huid.toString() + " cached.");
		}
	}

	/**
	 * Store the clan meta into hard storage.
	 */
	public void saveMeta() {
		DataManager cm = new DataManager(clanID);
		Config clan = cm.getFile(ConfigType.CLAN_FILE);
		clan.getConfig().set("NO-ID", instance.huid.toString());
		clan.saveConfig();
		DataManager dm = new DataManager(instance.huid.toString(), "Meta");
		Config meta = dm.getFile(ConfigType.MISC_FILE);
		try {
			meta.getConfig().set("Data", new HFEncoded(instance).serialize());
			meta.saveConfig();
			if (debugging) {
				Bukkit.getServer().getLogger().info("[Clans] - Instance for ID #" + instance.huid.toString() + " saved.");
				if (value != null) {
					Bukkit.getServer().getLogger().info("[Clans] - Object value for ID #" + instance.huid.toString() + " saved.");
				}
			}
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe("[Clans] - Unable to parse object.");
			e.printStackTrace();
		}
	}

	/**
	 * Store the clan meta into hard storage under a specified delimiter.
	 * @param id The delimiter to save the meta under.
	 */
	public void saveMeta(int id) {
		DataManager cm = new DataManager(clanID);
		Config clan = cm.getFile(ConfigType.CLAN_FILE);
		clan.getConfig().set("Data." + instance.huid.toString(), id);
		clan.saveConfig();
		DataManager dm = new DataManager(huid.toString(), "Meta");
		Config meta = dm.getFile(ConfigType.MISC_FILE);
		try {
			meta.getConfig().set("Data", new HFEncoded(instance).serialize());
			meta.saveConfig();
			if (debugging) {
				Bukkit.getServer().getLogger().info("[Clans] - Instance for ID #" + instance.huid.toString() + " saved.");
				if (value != null) {
					Bukkit.getServer().getLogger().info("[Clans] - Object value for ID #" + instance.huid.toString() + " saved.");
				}
			}
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe("[Clans] - Unable to parse object.");
			e.printStackTrace();
		}
	}



}
