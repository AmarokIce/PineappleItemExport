package cn.mcmod.pie;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

@Mod(modid = "pineapple_item_export")
public class PIE {
    public static final Logger log = LogManager.getLogger("PIE");

    public static Map<String, Map<ItemStack, String>> ModMap = null;
    public static EntityPlayer player = null;
    public static boolean canRenderer = false;

    @Mod.EventHandler
    public void init(FMLPreInitializationEvent event) {
        FMLCommonHandler.instance().bus().register(this);
    }

    @Mod.EventHandler
    public void serverInit(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandRender());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void renderTickEvent(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (PIE.player != null && PIE.ModMap != null && canRenderer) {
            for (String modid : PIE.ModMap.keySet()) {
                new PIERender(PIE.player, modid, PIE.ModMap.get(modid));
            }

            PIE.player = null;
            PIE.ModMap = null;
            PIE.canRenderer = false;
        }
    }

    static class CommandRender extends CommandBase {
        @Override
        public String getCommandName() {
            return "item_export";
        }

        @Override
        public String getCommandUsage(ICommandSender p_71518_1_) {
            return "/item_export [name | All | Hand] [int (if Hand)]";
        }

        @Override @SuppressWarnings("unchecked")
        public void processCommand(ICommandSender sender, String[] command_tree) {
            if (command_tree[0] == null) sender.addChatMessage(new ChatComponentTranslation("noModid.message"));
            sender.addChatMessage(new ChatComponentText("Now export " + command_tree[0]));

            Map<String, Map<ItemStack, String>> map = Maps.newHashMap();
            if (command_tree[0].equalsIgnoreCase("All")) {
                for (Item item : (Iterable<Item>) Item.itemRegistry) {
                    GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(item);

                    if (!map.containsKey(uid.modId)) {
                        map.put(uid.modId, Maps.newHashMap());
                    }

                    List<String> listTexture = Lists.newArrayList();
                    for (int i = 0; i <= 16; i ++) {
                        ItemStack is = new ItemStack(item, 1, i);
                        if (i == 0) {
                            listTexture.add(is.getIconIndex().getIconName());
                            map.get(uid.modId).put(is, uid.modId);
                        } else if (!listTexture.contains(is.getIconIndex().getIconName())) {
                            listTexture.add(is.getIconIndex().getIconName());
                            map.get(uid.modId).put(is, uid.modId);
                        }
                    }
                }
            } else if (command_tree[0].equalsIgnoreCase("Hand")) {
                ItemStack item = getCommandSenderAsPlayer(sender).getHeldItem();
                if (item == null) {
                    sender.addChatMessage(new ChatComponentText("If you want to export a item in your hand, hold a item first."));
                    return;
                }

                int maxNumber;
                if (command_tree[1] == null) maxNumber = 0;
                else {
                    try {
                        maxNumber = Integer.parseInt(command_tree[1]);
                    } catch (NumberFormatException e) {
                        maxNumber = 0;
                    }
                }

                List<String> listTexture = Lists.newArrayList();
                GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(item.getItem());
                for (int i = 0; i <= maxNumber + 1; i ++) {
                    ItemStack is = new ItemStack(item.getItem(), 1, i);
                    try {
                        String name = uid.toString().replace("item.", "").replace("tile.", "");
                        if (i == 0) {
                            listTexture.add(is.getIconIndex().getIconName());
                            map.get(uid.modId).put(is, name);
                        } else if (!listTexture.contains(is.getIconIndex().getIconName())) {
                            listTexture.add(is.getIconIndex().getIconName());
                            map.get(uid.modId).put(is, name);
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        log.error(e);
                        log.error(uid + " cannot get icon with meta!");
                    }
                }

            } else {
                for (Item item : (Iterable<Item>) Item.itemRegistry) {
                    GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(item);
                    if (!uid.modId.equalsIgnoreCase(command_tree[0])) continue;

                    if (!map.containsKey(uid.modId)) {
                        map.put(uid.modId, Maps.newHashMap());
                    }

                    List<String> listTexture = Lists.newArrayList();
                    int meta = item.getHasSubtypes() ? 64 : 16;
                    for (int i = 0; i <= meta; i ++) {
                        ItemStack is = new ItemStack(item, 1, i);
                        try {
                            String name = uid.toString().replace("item.", "").replace("tile.", "");
                            if (i == 0) {
                                listTexture.add(is.getIconIndex().getIconName());
                                map.get(uid.modId).put(is, name);
                            } else if (!listTexture.contains(is.getIconIndex().getIconName())) {
                                listTexture.add(is.getIconIndex().getIconName());
                                map.get(uid.modId).put(is, name);
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            log.error(e);
                            log.error(uid + " cannot get icon with meta!");
                        }
                    }
                }
            }

            PIE.player = getCommandSenderAsPlayer(sender);
            PIE.ModMap = map;

            PIE.canRenderer = true;
        }
    }
}
