package br.com.mz;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ActionBar {

    private static String nmsVersion;
    private static boolean setup = false;
    private static Constructor<?> packetPlayOutChatConstructor;
    private static Method chatSerializerAMethod;

    static {
        try {
            nmsVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + nmsVersion + ".IChatBaseComponent");
            Class<?> packetPlayOutChatClass = Class.forName("net.minecraft.server." + nmsVersion + ".PacketPlayOutChat");
            Class<?> chatSerializerClass = Class.forName("net.minecraft.server." + nmsVersion + ".IChatBaseComponent$ChatSerializer");

            packetPlayOutChatConstructor = packetPlayOutChatClass.getConstructor(iChatBaseComponentClass, byte.class);
            chatSerializerAMethod = chatSerializerClass.getMethod("a", String.class);

            setup = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void send(Player player, String message) {
        if (!setup) return;
        try {
            Object iChatBaseComponent = chatSerializerAMethod.invoke(null, "{\"text\":\"" + ChatColor.translateAlternateColorCodes('&', message) + "\"}");
            Object packet = packetPlayOutChatConstructor.newInstance(iChatBaseComponent, (byte) 2);

            Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
            playerConnection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + nmsVersion + ".Packet")).invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}