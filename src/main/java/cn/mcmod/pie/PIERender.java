package cn.mcmod.pie;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.Language;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class PIERender {
    Map<ItemStack, Data> dataList = Maps.newHashMap();
    List<Data> list = Lists.newArrayList();

    public PIERender(EntityPlayer player, String modid, Map<ItemStack, String> items) {
        for (ItemStack item : items.keySet()) {
            dataList.put(item,
                    new Data(
                    items.get(item),
                    "null",
                    "null",
                    item.getMaxDamage(),
                     item.getMaxStackSize(),
                    item.getItemDamage(),
                    render(item, 32),
                    render(item, 128),
                    Block.getBlockFromItem(item.getItem()) == Blocks.air ? "Item" : "Block"
            ));
        }

        jsonInit(modid);
        player.addChatMessage(new ChatComponentText("Render Finish!"));
    }

    private void jsonInit(String modid) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            mc.getLanguageManager().setCurrentLanguage(new Language("en_US", "US", "English", false));
            mc.gameSettings.language = "en_US";
            mc.refreshResources();
            mc.fontRenderer.setUnicodeFlag(false);
            mc.gameSettings.saveOptions();
            for (ItemStack item : dataList.keySet()) {
                dataList.get(item).englishName = item.getDisplayName();
            }

            mc.getLanguageManager().setCurrentLanguage(new Language("zh_CN", "涓浗", "绠�浣撲腑鏂�", false));
            mc.gameSettings.language = "zh_CN";
            mc.refreshResources();
            mc.gameSettings.saveOptions();
            for (ItemStack item : dataList.keySet()) {
                dataList.get(item).name = item.getDisplayName();

                list.add(dataList.get(item));
            }

            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            File file = new File(System.getProperty("user.dir") + File.separator + "export", modid + "_item.json");
            if (!file.getParentFile().isDirectory()) file.getParentFile().mkdirs();
            if (!file.isFile() || !file.exists()) file.createNewFile();

            PrintWriter writer =  new PrintWriter(file, "UTF-8");
            for (Data data : list) writer.println(gson.toJson(data));
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> getMapFromLang(InputStream langFile) {
        Map<String, String> map = Maps.newHashMap();

        try {
            BufferedReader buffreader = new BufferedReader(new InputStreamReader(langFile));

            String s;
            while ((s = buffreader.readLine()) != null) {
                if (!s.contains("=")) continue;
                int count = s.indexOf("=");
                String key = new String(s.substring(0, count).getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                String value = new String(s.substring(count + 1).getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

                map.put(key, value);
            }

            buffreader.close();
            langFile.close();

            return map;
        } catch (Exception ignored) {
        }

        return map;
    }

    /**
     *  Render Item Path Start.
     *  @author Unascribed
     *  @author Someoneice
     */
    private String render(ItemStack item, int size) {

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        float scale = 8.0F * res.getScaleFactor();
        GL11.glPushMatrix();
        {
            GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            GL11.glClear(16640);
            mc.entityRenderer.setupOverlayRendering();
            RenderHelper.enableGUIStandardItemLighting();

            GL11.glTranslatef(0.0F, res.getScaledHeight() - scale * 16.0F, 0.0F);
            GL11.glScalef(scale, scale, scale);
            GL11.glTranslatef(0.0F, 0.0F, -50.0F);
            if (item.getItem().requiresMultipleRenderPasses()) {
                GL11.glDisable(2929);
            } else {
                GL11.glEnable(2929);
            }
            RenderItem.getInstance().renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, item, 0, 0);
        }
        GL11.glPopMatrix();

        int tz;
        if (scale < 4.0) tz = 32;
        else if (scale == 5.0) tz = 128;
        else tz = 512;

        try {
            BufferedImage img = resizeImage(size, tz);

            // All well done, now return the base64 item icon.
            return Base64.getEncoder().encodeToString(getByteByImg(img));
        } catch (Exception ex) {
            return "null";
        }

    }

    private BufferedImage resizeImage(int size, int target) {
        GL11.glReadBuffer(1029);
        ByteBuffer buf = BufferUtils.createByteBuffer(size * size * 4);
        GL11.glReadPixels(0, 0, size, size, 32993, 5121, buf);
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[size * size];
        buf.asIntBuffer().get(pixels);
        img.setRGB(0, 0, size, size, pixels, 0, size);

        // Create flipped because when we get the img, it's reverse ...
        AffineTransform at = new AffineTransform();
        at.concatenate(AffineTransform.getScaleInstance(1.0D, -1.0D));
        at.concatenate(AffineTransform.getTranslateInstance(0.0D, -img.getHeight()));

        BufferedImage newImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newImage.createGraphics();
        g.transform(at);
        g.drawImage(img, 0, 0, null);
        g.dispose();

        double tgs;
        if (size == 32) {
            tgs = target == 32 ? 1 : 4;
        } else if (size == 128) {
            tgs = target == 32 ? 0.25D : 1;
        } else tgs = target == 32 ? 0.0625D : 0.25D;

        AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(tgs, tgs), null);
        return op.filter(newImage, null);
    }

    // Make byte array, and make base64 ...
    private byte[] getByteByImg(BufferedImage image) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        byte[] bytes = output.toByteArray();
        output.close();
        return bytes;
    }
}
