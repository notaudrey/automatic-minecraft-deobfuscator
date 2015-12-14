package me.curlpipesh.mcdeobf;

import lombok.Getter;
import me.curlpipesh.mcdeobf.deobf.ClassDef;
import me.curlpipesh.mcdeobf.deobf.Deobfuscator;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.block.BlockEntity;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.block.blockentity.BlockEntityChest;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.block.blockentity.BlockEntityEnderChest;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.client.GameSettings;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.client.Minecraft;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.client.gui.*;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.client.renderer.EntityRenderer;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.entity.*;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.entity.player.EntityClientPlayer;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.entity.player.EntityPlayer;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.entity.player.EntityPlayerMP;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.item.Item;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.item.ItemStack;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.item.inventory.InventoryPlayer;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.item.inventory.container.Container;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.item.nbt.*;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.network.EnumConnectionState;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.network.NetClientPlayHandler;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.network.NetworkManager;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.network.packet.Packet;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.network.packet.client.PacketClientChatMessage;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.network.packet.client.PacketClientHandshake;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.util.*;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.world.AbstractWorld;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.world.World;
import me.curlpipesh.mcdeobf.deobf.net.minecraft.world.WorldProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author c
 * @since 8/3/15
 */
@SuppressWarnings("unused")
public class Main {
    /**
     * The singleton instance of Main. Guaranteed to never change.
     */
    private static final Main instance = new Main();

    @Getter
    private final List<Deobfuscator> deobfuscators;

    @Getter
    private final Logger logger = Logger.getLogger("Deobfuscator");

    @Getter
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private Map<Deobfuscator, Byte[]> dataToMap = new ConcurrentHashMap<>();

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "FieldCanBeLocal"})
    private final List<ClassDef> classDefs;

    private Main() {
        deobfuscators = new CopyOnWriteArrayList<>();
        classDefs = new CopyOnWriteArrayList<>();
        add(
                new BlockEntityChest(),
                new BlockEntityEnderChest(),
                new BlockEntity(),
                new FontRenderer(),
                new Gui(),
                new GuiChat(),
                new GuiIngame(),
                new GuiMainMenu(),
                new GuiMultiplayer(),
                new GuiOptions(),
                new GuiScreen(),
                new GuiSingleplayer(),
                new EntityRenderer(),
                new GameSettings(),
                new Minecraft(),
                new EntityClientPlayer(),
                new EntityPlayer(),
                new EntityPlayerMP(),
                new DamageSource(),
                new Entity(),
                new EntityAgeable(),
                new EntityAnimal(),
                new EntityAttributes(),
                new EntityCreature(),
                new EntityLiving(),
                new EntityLivingBase(),
                new EntityMonster(),
                new Container(),
                new InventoryPlayer(),
                new NBTBase(),
                new NBTTagByte(),
                new NBTTagByteArray(),
                new NBTTagCompound(),
                new NBTTagDouble(),
                new NBTTagEnd(),
                new NBTTagFloat(),
                new NBTTagInt(),
                new NBTTagIntArray(),
                new NBTTagList(),
                new NBTTagLong(),
                new NBTTagShort(),
                new NBTTagString(),
                new Item(),
                new ItemStack(),
                new PacketClientChatMessage(),
                new PacketClientHandshake(),
                new Packet(),
                new EnumConnectionState(),
                new NetClientPlayHandler(),
                new NetworkManager(),
                new BlockPos(),
                new ChatComponentText(),
                new IChatComponent(),
                new ScaledResolution(),
                new Session(),
                new Vec3i(),
                new AbstractWorld(),
                new World(),
                new WorldProvider()
        );
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord logRecord) {
                System.out.println(String.format("[%s] [%s] %s", logRecord.getLoggerName(), logRecord.getLevel(),
                        logRecord.getMessage()));
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });
    }

    private void add(Deobfuscator... d) {
        Arrays.stream(d).forEach(deobfuscators::add);
    }

    /**
     * Returns the singleton instance of this class.
     *
     * @return The singleton instance of this class.
     */
    public static Main getInstance() {
        return instance;
    }

    private void run(String jar) {
        int successes = 0;
        int max = deobfuscators.size();
        try {
            JarFile jarFile = new JarFile(jar);
            Enumeration<JarEntry> entries = jarFile.entries();
            while(entries.hasMoreElements() || !deobfuscators.isEmpty()) {
                JarEntry entry = entries.nextElement();
                if(entry.getName().endsWith(".class")) {
                    try(InputStream is = jarFile.getInputStream(entry)) {
                        try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                            byte[] buffer = new byte[0xFFFF];
                            int len;
                            while((len = is.read(buffer)) != -1) {
                                os.write(buffer, 0, len);
                            }
                            Deobfuscator d = deobfuscate(os.toByteArray());
                            if(d != null) {
                                logger.info("Deobfuscated class \"" + entry.getName().replaceAll(".class", "")
                                        + "\": " + d.getDeobfuscatedName());
                                ++successes;
                                d.setObfuscatedName(entry.getName().replaceAll(".class", ""));
                                byte[] osClone = os.toByteArray();
                                Byte[] bufferClone = new Byte[osClone.length];
                                // Oh FFS, System#arraycopy
                                for(int i = 0; i < osClone.length; i++) {
                                    bufferClone[i] = osClone[i];
                                }
                                dataToMap.put(d, bufferClone);
                            }
                        }
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        logger.info("Done! (" + successes + "/" + max + ")");
        if(successes < max) {
            logger.info("Had the following deobfuscators remaining: ");
            deobfuscators.stream().forEach(d -> logger.info(d.getDeobfuscatedName()));
        } else {
            logger.info("start");
            dataToMap.entrySet().stream().filter(e -> e.getKey().getDeobfuscatedName().equals("NetClientPlayHandler"))
                    .forEach(e -> {
                        byte[] data = new byte[e.getValue().length];
                        for(int i = 0; i < data.length; i++) {
                            data[i] = e.getValue()[i];
                        }
                        //System.out.println(e.getKey().getClassDefinition(data));
                        ClassDef def = e.getKey().getClassDefinition(data);
                        logger.info(def.toString());
                        for(ClassDef.MethodDef m : def.getMethods()) {
                            logger.info(m.toString());
                        }
                    });
            logger.info("end");
        }
        // TODO: Generate actual usable mappings
    }

    private Deobfuscator deobfuscate(byte[] classBytes) {
        for(Deobfuscator d : deobfuscators) {
            if(d.deobfuscate(classBytes)) {
                // This is supposed to stay in here. Please don't remove it ;-;
                //classDefs.add(d.getClassDefinition(classBytes));
                deobfuscators.remove(d);
                return d;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        // Yes, it'd be better to use args[0] here, but I don't feel like getting
        // that working with mvn right now
        String jarPath = "/home/audrey/.minecraft/versions/1.8.8/1.8.8.jar";
        instance.run(jarPath);
    }
}
