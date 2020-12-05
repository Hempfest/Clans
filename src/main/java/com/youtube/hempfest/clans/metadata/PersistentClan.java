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

	private static final Map<HUID, ClanMeta> metaData = new HashMap<>();

	private String clanID;

	private final HUID huid;

	private String value;

	private final PersistentClan instance;

	private boolean debugging;

	public PersistentClan(String clanID) {
		this.clanID = clanID;
		instance = this;
		huid = HUID.randomID();
	}

	public PersistentClan(HUID huid) {
		this.huid = huid;
		instance = this;
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
	public Clan getClan() {
		return Clan.clanUtil.getClan(clanID);
	}

	public void setValue(Object o) {
		try {
			this.value = new HFEncoded(o).serialize();
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe("Unable to parse object.");
			e.printStackTrace();
		}
	}

	public static HUID[] getMetaDataContainer() {
		List<HUID> array = new ArrayList<>();
		for (Map.Entry<HUID, ClanMeta> entry : metaData.entrySet()) {
			array.add(entry.getKey());
		}
		return array.toArray(new HUID[0]);
	}

	public void setDebugging(boolean b) {
		this.debugging = b;
	}

	public static PersistentClan loadSavedInstance(HUID huid) {
		DataManager dm = new DataManager(huid.toString(), "Meta");
		Config meta = dm.getFile(ConfigType.MISC_FILE);
		PersistentClan persistentClan = null;
		if (meta.exists()) {
			try {
				PersistentClan instance = (PersistentClan) new HFEncoded(meta.getConfig().getString("Data")).deserialized();
				metaData.put(huid, instance);
				persistentClan = instance;
			} catch (IOException | ClassNotFoundException e) {
				Bukkit.getServer().getLogger().severe("[Clans] - Instance not loadable. One or more values changed or object location changed.");
				e.printStackTrace();
			}
		} else {
			Bukkit.getServer().getLogger().severe("[Clans] - No saved meta data can be found. Are you sure you saved it?");
		}
		if (persistentClan == null) {
			Bukkit.getServer().getLogger().severe("[Clans] - No instance to load was found under this HUID.");
		}
		return persistentClan;
	}

	public static ClanMeta loadTempInstance(HUID huid) {
		ClanMeta meta = null;
		for (HUID entry : getMetaDataContainer()) {
			if (entry.toString().equals(huid.toString())) {
				meta = metaData.get(entry);
			}
		}
		return meta;
	}

	public static void deleteInstance(HUID huid) {
		Arrays.stream(getMetaDataContainer()).forEach(I -> {
			if (I.toString().equals(huid.toString())) {
				Bukkit.getServer().getLogger().info("[Clans] - Instance for ID #" + I.toString() + " deleted.");
				DataManager cm = new DataManager(metaData.get(I).getClan().getClanID());
				Config clan = cm.getFile(ConfigType.CLAN_FILE);
				clan.getConfig().set("HUID", null);
				clan.saveConfig();
				DataManager dm = new DataManager(I.toString(), "Meta");
				Config meta = dm.getFile(ConfigType.MISC_FILE);
				meta.delete();
				metaData.remove(I);
			}
		});
	}

	public static void querySaved() {
		final File dir = new File(Config.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll("%20", " "));
		File file = new File(dir.getParentFile().getPath(), HempfestClans.getInstance().getName() + "/Meta/");
		Arrays.stream(file.listFiles()).forEach(f -> {
			HUID id = HUID.fromString(f.getName().replace(".yml", ""));
			PersistentClan m = loadSavedInstance(id);
			metaData.put(id, m);
		});
	}

	public void storeTemp() {
		metaData.put(huid, instance);
		Bukkit.getServer().getLogger().info("[Clans] - Instance for ID #" + huid.toString() + " cached.");
	}

	public void saveMeta() {
		DataManager cm = new DataManager(clanID);
		Config clan = cm.getFile(ConfigType.CLAN_FILE);
		clan.getConfig().set("HUID", huid.toString());
		clan.saveConfig();
		DataManager dm = new DataManager(huid.toString(), "Meta");
		Config meta = dm.getFile(ConfigType.MISC_FILE);
		try {
			meta.getConfig().set("Data", new HFEncoded(instance).serialize());
			meta.saveConfig();
			if (debugging) {
				Bukkit.getServer().getLogger().info("[Clans] - Instance for ID #" + instance.huid.toString() + " saved.");
				if (value != null) {
					Bukkit.getServer().getLogger().info("[Clans] - Object value for ID #" + huid.toString() + " saved.");
				}
			}
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe("[Clans] - Unable to parse object.");
			e.printStackTrace();
		}
	}



}
