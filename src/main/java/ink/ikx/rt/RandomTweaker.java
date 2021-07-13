package ink.ikx.rt;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.compiler.CompilerUtil;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import crafttweaker.CraftTweakerAPI;
import ink.ikx.rt.api.instance.file.Prop;
import ink.ikx.rt.api.instance.player.IPlayerExpansionSanity;
import ink.ikx.rt.api.mods.botania.Hydroangeas;
import ink.ikx.rt.api.mods.cote.flower.JAVATextContent;
import ink.ikx.rt.api.mods.cote.flower.generating.SubTileGeneratingRepresentation;
import ink.ikx.rt.api.mods.jei.interfaces.other.JEIPanel;
import ink.ikx.rt.api.mods.jei.interfaces.other.JEIRecipe;
import ink.ikx.rt.api.mods.player.IPlayerExpansionFTBU;
import ink.ikx.rt.impl.botania.subtitle.SubTileHydroangeasModified;
import ink.ikx.rt.impl.client.capability.PlayerSanityCapabilityHandler;
import ink.ikx.rt.impl.client.network.PlayerSanityNetWork;
import ink.ikx.rt.impl.config.RTConfig;
import ink.ikx.rt.impl.events.DreamJournal;
import ink.ikx.rt.impl.events.ManaBaubleEvent;
import ink.ikx.rt.impl.item.SanityGem;
import ink.ikx.rt.impl.jei.HydroangeasJEI;
import ink.ikx.rt.impl.proxy.IProxy;
import ink.ikx.rt.impl.utils.ItemDs;
import ink.ikx.rt.impl.utils.annotation.RTRegisterClass;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.subtile.SubTileEntity;
import vazkii.botania.common.lib.LibBlockNames;

@SuppressWarnings("unchecked")
@Mod(
    modid = RandomTweaker.MODID,
    name = RandomTweaker.NAME,
    version = RandomTweaker.VERSION,
    guiFactory = RandomTweaker.GUI_FACTORY,
    dependencies = RandomTweaker.DESPENDENCIES
)
public class RandomTweaker {

    public static final String MODID = "randomtweaker";
    public static final String NAME = "RandomTweaker";
    public static final String VERSION = "1.0.0";
    public static final String GUI_FACTORY = "ink.ikx.rt.impl.config.RTConfigGuiFactory";
    public static final String DESPENDENCIES = "required-after:crafttweaker;after:contenttweaker";

    public static final SanityGem SANITY_GEM = new SanityGem();
    public static final SoundEvent SOUND_SAN = new SoundEvent(
        new ResourceLocation(RandomTweaker.MODID, "san"))
        .setRegistryName(new ResourceLocation(RandomTweaker.MODID, "san"));

    public static Logger logger;
    public static Set<ItemDs> itemDsSet = new HashSet<>();
    public static Set<JEIPanel> JEIPanelList = new HashSet<>();
    public static List<JEIRecipe> JEIRecipeList = new ArrayList<>();
    public static Map<String, Potion> potionRegList = new HashMap<>();
    public static Map<String, PotionType> potionTypeList = new HashMap<>();
    public static BiMap<String, SubTileGeneratingRepresentation> subTileGeneratingMap = HashBiMap.create();
    @SidedProxy(clientSide = "ink.ikx.rt.impl.proxy.ClientProxy", serverSide = "ink.ikx.rt.impl.proxy.SeverProxy")
    public static IProxy proxy;

    @EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        botaniaReg();
        PlayerSanityNetWork.register();
        PlayerSanityCapabilityHandler.register();
    }

    @EventHandler
    public void onInit(FMLInitializationEvent event) {
        if (RTConfig.Botania.HydroangeasModified && Loader.isModLoaded("botania")) {
            registryHydroangeasModified();
            HydroangeasJEI.init();
        }
    }

    @EventHandler
    public void onConstruct(FMLConstructionEvent event) throws Exception {
        hashCheck();
        registerOtherClass();
    }


    private void registryHydroangeasModified() {
        final BiMap<String, Class<? extends SubTileEntity>> subTiles;
        try {
            Field field = BotaniaAPI.class.getDeclaredField("subTiles");
            field.setAccessible(true);
            subTiles = (BiMap<String, Class<? extends SubTileEntity>>) field.get(null);

            if (subTiles != null) {
                subTiles.forcePut(LibBlockNames.SUBTILE_HYDROANGEAS, SubTileHydroangeasModified.class);
            }

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void botaniaReg() {
        if (subTileGeneratingMap.isEmpty()) {
            return;
        }
        subTileGeneratingMap.forEach((k, v) -> {
            String Generating = JAVATextContent.Generating.replace("{$name}", k);
            String className = "ink.ikx.rt.api.mods.cote.flower.generating.CustomSubTileGeneratingContent_" + k;
            ClassLoader classLoader = CompilerUtil.getCompiler(null)
                .addSource(className, Generating)
                .compile();

            try {
                Class<? extends SubTileEntity> clazz = (Class<? extends SubTileEntity>) classLoader.loadClass(className);
                BotaniaAPI.registerSubTile(k, clazz);
                BotaniaAPIClient.registerSubtileModel(clazz, new ModelResourceLocation("contenttweaker:" + k));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    private void registerOtherClass() throws IOException {
        if (Loader.isModLoaded("thaumcraft")) {
            if (RTConfig.Thaumcraft.DreamJournal) {
                MinecraftForge.EVENT_BUS.register(DreamJournal.class);
            }
        }
        if (Loader.isModLoaded("botania") && Loader.isModLoaded("contenttweaker")) {
            MinecraftForge.EVENT_BUS.register(ManaBaubleEvent.class);
        }
        if (RTConfig.Botania.HydroangeasModified && Loader.isModLoaded("botania")) {
            CraftTweakerAPI.registerClass(Hydroangeas.class);
        }
        if (RTConfig.RandomTweaker.PlayerSanity) {
            CraftTweakerAPI.registerClass(IPlayerExpansionSanity.class);
        }
        if (RTConfig.FTBUltimine.AllowCrTControl) {
            CraftTweakerAPI.registerClass(IPlayerExpansionFTBU.class);
        }
        if (Prop.createOrDelete(RTConfig.RandomTweaker.Prop)) {
            CraftTweakerAPI.registerClass(Prop.class);
        }
    }

    private void hashCheck() throws Exception {
        for (ModContainer mod : Loader.instance().getActiveModList()) {
            if (mod.getModId().equals(RandomTweaker.MODID)) {
                JarFile jarFile = new JarFile(mod.getSource());
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".class")) {
                        String className = entry.getName().substring(0, entry.getName().length() - 6).replace('/', '.');
                        if (!className.contains("Mixin") && className.contains("ink.ikx.rt")) {
                            Class<?> clazz = Class.forName(className);
                            if (AnnotationUtil.hasAnnotation(clazz, RTRegisterClass.class)) {
                                boolean flag = true;
                                String[] value = AnnotationUtil.getAnnotationValue(clazz, RTRegisterClass.class);
                                for (String s : value) {
                                    if (!Loader.isModLoaded(s)) {
                                        flag = false;
                                    }
                                }
                                if (flag) {
                                    CraftTweakerAPI.registerClass(clazz);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
