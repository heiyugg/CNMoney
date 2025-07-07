package cn.money.gui;

import cn.money.CNMoney;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * GUI基础类
 * 
 * @author CNMoney Team
 */
public abstract class BaseGUI {
    
    protected final CNMoney plugin;
    protected final Player player;
    protected Inventory inventory;
    protected String title;
    protected int size;
    
    public BaseGUI(CNMoney plugin, Player player, String title, int size) {
        this.plugin = plugin;
        this.player = player;
        this.title = colorize(title);
        this.size = size;
        this.inventory = Bukkit.createInventory(null, size, this.title);
    }
    
    /**
     * 初始化GUI内容
     */
    protected abstract void initializeItems();
    
    /**
     * 处理点击事件
     */
    public abstract void handleClick(InventoryClickEvent event);
    
    /**
     * 打开GUI
     */
    public void open() {
        initializeItems();
        player.openInventory(inventory);
        playSound(Sound.UI_BUTTON_CLICK);
    }
    
    /**
     * 关闭GUI
     */
    public void close() {
        player.closeInventory();
    }
    
    /**
     * 刷新GUI
     */
    public void refresh() {
        inventory.clear();
        initializeItems();
    }
    
    /**
     * GUI关闭时调用
     */
    public void onClose() {
        // 子类可以重写此方法
    }
    
    /**
     * 获取库存
     */
    public Inventory getInventory() {
        return inventory;
    }
    
    /**
     * 创建物品
     */
    protected ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(colorize(name));
            if (lore.length > 0) {
                meta.setLore(Arrays.stream(lore).map(this::colorize).toList());
            }
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * 创建物品（带数量）
     */
    protected ItemStack createItem(Material material, int amount, String name, String... lore) {
        ItemStack item = createItem(material, name, lore);
        item.setAmount(Math.max(1, Math.min(64, amount)));
        return item;
    }
    
    /**
     * 创建物品（带Lore列表）
     */
    protected ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(colorize(name));
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore.stream().map(this::colorize).toList());
            }
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * 设置边框
     */
    protected void setBorder(Material material) {
        ItemStack borderItem = createItem(material, " ");
        
        // 设置顶部和底部边框
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, borderItem);
            inventory.setItem(size - 9 + i, borderItem);
        }
        
        // 设置左右边框
        for (int i = 9; i < size - 9; i += 9) {
            inventory.setItem(i, borderItem);
            inventory.setItem(i + 8, borderItem);
        }
    }
    
    /**
     * 设置玻璃边框
     */
    protected void setGlassBorder() {
        setBorder(Material.GRAY_STAINED_GLASS_PANE);
    }
    
    /**
     * 填充空槽
     */
    protected void fillEmpty(Material material) {
        ItemStack fillItem = createItem(material, " ");
        
        for (int i = 0; i < size; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, fillItem);
            }
        }
    }
    
    /**
     * 添加返回按钮
     */
    protected void addBackButton(int slot) {
        ItemStack backItem = createItem(Material.ARROW, 
                                      plugin.getConfigManager().getMessage("gui.back-item"),
                                      "&7点击返回上一页");
        inventory.setItem(slot, backItem);
    }
    
    /**
     * 添加关闭按钮
     */
    protected void addCloseButton(int slot) {
        ItemStack closeItem = createItem(Material.BARRIER, 
                                       plugin.getConfigManager().getMessage("gui.close-item"),
                                       "&7点击关闭界面");
        inventory.setItem(slot, closeItem);
    }
    
    /**
     * 添加下一页按钮
     */
    protected void addNextPageButton(int slot) {
        ItemStack nextItem = createItem(Material.SPECTRAL_ARROW, 
                                      plugin.getConfigManager().getMessage("gui.next-page"),
                                      "&7点击查看下一页");
        inventory.setItem(slot, nextItem);
    }
    
    /**
     * 添加上一页按钮
     */
    protected void addPrevPageButton(int slot) {
        ItemStack prevItem = createItem(Material.TIPPED_ARROW, 
                                      plugin.getConfigManager().getMessage("gui.prev-page"),
                                      "&7点击查看上一页");
        inventory.setItem(slot, prevItem);
    }
    
    /**
     * 播放声音
     */
    protected void playSound(Sound sound) {
        if (plugin.getConfigManager().isGUISoundEnabled()) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
    }
    
    /**
     * 发送消息
     */
    protected void sendMessage(String message) {
        player.sendMessage(plugin.getConfigManager().getMessagePrefix() + colorize(message));
    }
    
    /**
     * 颜色化文本
     */
    protected String colorize(String text) {
        if (text == null) return "";
        return text.replace('&', '§');
    }
    
    /**
     * 获取点击的槽位
     */
    protected int getClickedSlot(InventoryClickEvent event) {
        return event.getSlot();
    }
    
    /**
     * 检查是否为左键点击
     */
    protected boolean isLeftClick(InventoryClickEvent event) {
        return event.getClick().isLeftClick();
    }
    
    /**
     * 检查是否为右键点击
     */
    protected boolean isRightClick(InventoryClickEvent event) {
        return event.getClick().isRightClick();
    }
    
    /**
     * 检查是否为Shift点击
     */
    protected boolean isShiftClick(InventoryClickEvent event) {
        return event.getClick().isShiftClick();
    }
    
    /**
     * 获取配置消息
     */
    protected String getMessage(String key) {
        return plugin.getConfigManager().getMessage(key);
    }
    
    /**
     * 获取配置消息列表
     */
    protected List<String> getMessageList(String key) {
        return plugin.getConfigManager().getMessageList(key);
    }
}
