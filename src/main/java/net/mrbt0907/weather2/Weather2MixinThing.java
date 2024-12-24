package net.mrbt0907.weather2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.SortingIndex(10)
public class Weather2MixinThing implements IFMLLoadingPlugin
{
    private static final Map<String, File> MODS = new HashMap<String, File>();
	
	private void init()
	{
		if(MixinEnvironment.getCurrentEnvironment().getSide() == MixinEnvironment.Side.SERVER)
			scan(new File("./mods"));
		else
		{
			String command = System.getProperty("sun.java.command");
			String gameFolder = null;
			if (command != null)
			{
				try
				{
					int index = command.indexOf("--mods=");
					if (index == -1)
						throw new IndexOutOfBoundsException();
					String mods = command.substring(index + 7);
					for (String modFile : mods.split(","))
						if (modFile.endsWith(".jar"))
							cacheModFile(new File(modFile));
				}
				catch (IndexOutOfBoundsException ignored) {}

				try
				{
					gameFolder = command.substring(command.indexOf("--gameDir") + 10).split(" ")[0];
				} catch (IndexOutOfBoundsException ignored) {}
			}

			// Load mods from mod_list.json
			File modList = new File("mods/mod_list.json");
			if (modList.exists()) {
				try {
					JsonObject json = new GsonBuilder().create().fromJson(new FileReader(modList), JsonObject.class);

					if (json.has("repositoryRoot")) {
						File modFolder = new File(json.get("repositoryRoot").getAsString());
						JsonArray mods = json.getAsJsonArray("modRef");
						for (JsonElement mod : mods) {
							String[] split = mod.getAsString().split(":");
							if (split.length != 3) continue;
							File modLocation = new File(
									modFolder,
									split[0].replace(".", "/") + "/" + split[1] + "/" + split[2] + "/"
							);

							if (modLocation.exists()) {
								File[] files = modLocation.listFiles();
								if (files != null && files.length > 0)
									cacheModFile(files[0]);
							}
						}
					}
				}
				catch (Exception e){}
			}

			String modsFolder = System.getProperty("LibLoader.modsFolder");
			if (modsFolder != null && !modsFolder.isEmpty())
				scan(new File(modsFolder));

			if (gameFolder != null && !gameFolder.isEmpty())
				scan(new File(gameFolder, "mods"));

			gameFolder = System.getProperty("user.dir");
			File folder = (gameFolder != null && !gameFolder.isEmpty()) ? new File(gameFolder, "mods") : new File("mods");
			scan(folder);
		}
	}
	
	private static void scan(File folder)
	{
		if(folder.exists() && folder.isDirectory() && folder.listFiles() != null)
		{
			File[] mods = folder.listFiles(f -> f.getName().endsWith(".jar") || f.isDirectory());
			if(mods != null)
				for (File mod : mods)
				{
					if(mod.isDirectory() && mod.getName().equals("1.12.2"))
						scan(mod);
					cacheModFile(mod);
				}
		}
	}
	
	 private static void cacheModFile(File file)
	 {
		try(ZipFile zip = new ZipFile(file))
		{
			List<? extends ZipEntry> entries = zip.stream().filter(entry -> entry != null && !entry.isDirectory() && (entry.getName().equals("mcmod.info") || entry.getName().equals("Config.class"))).collect(Collectors.toList());

			for (ZipEntry entry : entries)
			{
				if(entry.getName().equals("mcmod.info"))
				{
					try(InputStream is = zip.getInputStream(entry))
					{
						try(InputStreamReader isr = new InputStreamReader(is))
						{
							try(BufferedReader reader = new BufferedReader(isr))
							{
								String line;
								StringBuilder sb = new StringBuilder();
								while((line = reader.readLine()) != null)
									sb.append(line).append("\n");
								
								String json = sb.toString();
								Gson gson = new GsonBuilder().create();
								JsonArray array;
								
								try {array = gson.fromJson(json, JsonArray.class);}
								catch (Exception e) {array = null;}

								if(array != null)
								{
									for (JsonElement jsonElement : array)
									{
										String modid = jsonElement.getAsJsonObject().get("modid").getAsString();
										if (modid != null && !MODS.containsKey(modid))
										{
											MODS.put(modid, file);
											
										}
									}
								}
								else
								{
									JsonObject jsonObj = gson.fromJson(json, JsonObject.class);
									if(jsonObj != null && jsonObj.has("modList"))
									{
										JsonArray modList = jsonObj.getAsJsonArray("modList");
										for (JsonElement jsonElement : modList)
										{
											String modid = jsonElement.getAsJsonObject().get("modid").getAsString();
											if (modid != null && !MODS.containsKey(modid))
											{
												MODS.put(modid, file);
												
											}
										}
									}
								}
							}
						}
					}

					continue;
				}
				else if(entry.getName().equals("Config.class"))
				{
					if (!MODS.containsKey("optifine"))
					{
						MODS.put("optifine", file);
					}
				}
			}
		} catch (Exception e) {}
	}
	
	public Weather2MixinThing()
	{
		MixinBootstrap.init();
		init();
		loadMod("coroutil");
		MODS.clear();
	}
	
	private void loadMod(String modID)
	{
		File mod = MODS.get(modID);
		if (mod != null)
			try
			{
				((LaunchClassLoader) Weather2MixinThing.class.getClassLoader()).addURL(mod.toURI().toURL());
		        CoreModManager.getReparseableCoremods().add(mod.getName());
			}
			catch (Exception e) {}
	}
	
		
	@Override
	public String[] getASMTransformerClass() {return null;}
	@Override
	public String getModContainerClass() {return null;}
	@Override
	public String getSetupClass() {return null;}
	@Override
	public void injectData(Map<String, Object> data) {}
	@Override
	public String getAccessTransformerClass() {return null;}
}
