package cn.money.gui.impl;

import cn.money.CNMoney;
import cn.money.gui.BaseGUI;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * 确认对话框GUI
 * 
 * @author CNMoney Team
 */
public class ConfirmationGUI extends BaseGUI {
    
    private final String message;
    private final Runnable confirmAction;
    private final Runnable cancelAction;
    private final Material confirmIcon;
    private final Material cancelIcon;
    
    /**
     * 构造函数
     * 
     * @param plugin 插件实例
     * @param player 玩家
     * @param title 标题
     * @param message 确认消息
     * @param confirmAction 确认操作
     * @param cancelAction 取消操作
     */
    public ConfirmationGUI(CNMoney plugin, Player player, String title, String message, 
                          Runnable confirmAction, Runnable cancelAction) {
        this(plugin, player, title, message, confirmAction, cancelAction, 
             Material.GREEN_CONCRETE, Material.RED_CONCRETE);
    }
    
    /**
     * 构造函数（自定义图标）
     * 
     * @param plugin 插件实例
     * @param player 玩家
     * @param title 标题
     * @param message 确认消息
     * @param confirmAction 确认操作
     * @param cancelAction 取消操作
     * @param confirmIcon 确认图标
     * @param cancelIcon 取消图标
     */
    public ConfirmationGUI(CNMoney plugin, Player player, String title, String message, 
                          Runnable confirmAction, Runnable cancelAction,
                          Material confirmIcon, Material cancelIcon) {
        super(plugin, player, title, 27);
        this.message = message;
        this.confirmAction = confirmAction;
        this.cancelAction = cancelAction;
        this.confirmIcon = confirmIcon;
        this.cancelIcon = cancelIcon;
        
        initializeItems();
    }
    
    @Override
    protected void initializeItems() {
        // 填充边框
        setGlassBorder();

        // 信息显示
        inventory.setItem(13, createItem(Material.PAPER, "§e确认操作",
            "§7" + message,
            "",
            "§a点击绿色方块确认",
            "§c点击红色方块取消"
        ));

        // 确认按钮
        inventory.setItem(11, createItem(confirmIcon, "§a§l确认",
            "§7点击确认执行操作",
            "",
            "§a▶ 点击确认"
        ));

        // 取消按钮
        inventory.setItem(15, createItem(cancelIcon, "§c§l取消",
            "§7点击取消返回",
            "",
            "§c▶ 点击取消"
        ));
    }
    
    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = getClickedSlot(event);
        
        switch (slot) {
            case 11: // 确认
                handleConfirm();
                break;
                
            case 15: // 取消
                handleCancel();
                break;
                
            default:
                // 其他位置不响应
                break;
        }
    }
    
    /**
     * 处理确认操作
     */
    private void handleConfirm() {
        player.closeInventory();
        playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        
        try {
            if (confirmAction != null) {
                confirmAction.run();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("执行确认操作时发生错误: " + e.getMessage());
            sendMessage("§c操作执行失败，请稍后重试。");
        }
    }
    
    /**
     * 处理取消操作
     */
    private void handleCancel() {
        player.closeInventory();
        playSound(Sound.UI_BUTTON_CLICK);
        
        try {
            if (cancelAction != null) {
                cancelAction.run();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("执行取消操作时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 创建危险操作确认对话框
     * 
     * @param plugin 插件实例
     * @param player 玩家
     * @param title 标题
     * @param message 确认消息
     * @param confirmAction 确认操作
     * @param cancelAction 取消操作
     * @return 确认对话框GUI
     */
    public static ConfirmationGUI createDangerousConfirmation(CNMoney plugin, Player player, 
                                                             String title, String message,
                                                             Runnable confirmAction, Runnable cancelAction) {
        return new ConfirmationGUI(plugin, player, title, message, confirmAction, cancelAction,
                                  Material.TNT, Material.BARRIER);
    }
    
    /**
     * 创建删除确认对话框
     * 
     * @param plugin 插件实例
     * @param player 玩家
     * @param itemName 要删除的项目名称
     * @param confirmAction 确认操作
     * @param cancelAction 取消操作
     * @return 确认对话框GUI
     */
    public static ConfirmationGUI createDeleteConfirmation(CNMoney plugin, Player player, 
                                                          String itemName,
                                                          Runnable confirmAction, Runnable cancelAction) {
        return createDangerousConfirmation(plugin, player, 
                                         "§c删除确认", 
                                         "确定要删除 " + itemName + " 吗？此操作不可撤销！",
                                         confirmAction, cancelAction);
    }
    
    /**
     * 创建重置确认对话框
     * 
     * @param plugin 插件实例
     * @param player 玩家
     * @param itemName 要重置的项目名称
     * @param confirmAction 确认操作
     * @param cancelAction 取消操作
     * @return 确认对话框GUI
     */
    public static ConfirmationGUI createResetConfirmation(CNMoney plugin, Player player, 
                                                         String itemName,
                                                         Runnable confirmAction, Runnable cancelAction) {
        return createDangerousConfirmation(plugin, player, 
                                         "§6重置确认", 
                                         "确定要重置 " + itemName + " 吗？此操作不可撤销！",
                                         confirmAction, cancelAction);
    }
}
