package cn.mcmod.pie;

public class Data {
    String name;
    String registerName;
    String englishName;
    String maxDurability;
    String maxStackSize;
    String metadata;
    String smallIcon;
    String largeIcon;

    public Data(String registerName, String name, String englishName, int maxDurability, int maxStackSize, int metadata, String smallIcon, String largeIcon) {
        this.registerName = registerName;
        this.name = name;
        this.maxDurability = Integer.toString(maxDurability);
        this.maxStackSize = Integer.toString(maxStackSize);
        this.metadata = Integer.toString(metadata);
        this.englishName = englishName;
        this.smallIcon = smallIcon;
        this.largeIcon = largeIcon;
    }
}
