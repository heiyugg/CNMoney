package cn.money.gui.impl;

import cn.money.CNMoney;
import cn.money.gui.BaseGUI;
import cn.money.model.Currency;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 主GUI界面
 * 
 * @author CNMoney Team
 */
public class MainGUI extends BaseGUI {
    
    public MainGUI(CNMoney plugin, Player player) {
        super(plugin, player, plugin.getConfigManager().getGUITitle(), 45);
    }
    
    @Override
    protected void initializeItems() {
        // 设置玻璃边框
        setGlassBorder();
        
        // 余额查看按钮
        ItemStack balanceItem = createBalanceItem();
        inventory.setItem(11, balanceItem);
        
        // 货币兑换按钮
        ItemStack exchangeItem = createExchangeItem();
        inventory.setItem(13, exchangeItem);
        
        // 转账按钮
        ItemStack transferItem = createTransferItem();
        inventory.setItem(15, transferItem);
        
        // 管理面板按钮（仅管理员可见）
        if (player.hasPermission("cnmoney.admin")) {
            ItemStack adminItem = createAdminItem();
            inventory.setItem(31, adminItem);
        }
        
        // 信息显示
        ItemStack infoItem = createInfoItem();
        inventory.setItem(22, infoItem);
        
        // 关闭按钮
        addCloseButton(40);
    }
    
    /**
     * 创建余额查看按钮
     */
    private ItemStack createBalanceItem() {
        List<String> lore = new ArrayList<>();
        lore.add("&7查看你的所有货币余额");
        lore.add("");
        lore.add("&e左键: 查看所有货币");
        lore.add("&e右键: 查看默认货币");
        lore.add("");
        
        // 添加当前余额预览
        Currency defaultCurrency = plugin.getCurrencyManager().getDefaultCurrency();
        BigDecimal balance = plugin.getCurrencyManager().getBalance(player.getUniqueId());
        lore.add("&7当前" + defaultCurrency.getName() + "余额:");
        lore.add("&f" + defaultCurrency.formatAmountWithColor(balance));
        
        return createItem(Material.GOLD_INGOT, 
                         getMessage("gui.balance-item"), 
                         lore);
    }
    
    /**
     * 创建兑换按钮
     */
    private ItemStack createExchangeItem() {
        List<String> lore = new ArrayList<>();
        lore.add("&7进行货币兑换操作");
        lore.add("");
        lore.add("&e左键: 打开兑换界面");
        lore.add("&e右键: 查看汇率信息");
        lore.add("");
        
        if (!plugin.getConfigManager().isExchangeEnabled()) {
            lore.add("&c兑换功能已禁用");
        } else {
            lore.add("&a兑换功能已启用");
            double feePercentage = plugin.getConfigManager().getExchangeFeePercentage() * 100;
            lore.add("&7手续费: &e" + feePercentage + "%");
        }
        
        return createItem(Material.EMERALD, 
                         getMessage("gui.exchange-item"), 
                         lore);
    }
    
    /**
     * 创建转账按钮
     */
    private ItemStack createTransferItem() {
        List<String> lore = new ArrayList<>();
        lore.add("&7转账给其他玩家");
        lore.add("");
        lore.add("&e左键: 打开转账界面");
        lore.add("");
        
        if (!plugin.getConfigManager().isTransferEnabled()) {
            lore.add("&c转账功能已禁用");
        } else {
            lore.add("&a转账功能已启用");
            double minAmount = plugin.getConfigManager().getTransferMinAmount();
            double maxAmount = plugin.getConfigManager().getTransferMaxAmount();
            lore.add("&7最小金额: &e" + minAmount);
            if (maxAmount > 0) {
                lore.add("&7最大金额: &e" + maxAmount);
            }
        }
        
        return createItem(Material.DIAMOND, 
                         getMessage("gui.transfer-item"), 
                         lore);
    }
    
    /**
     * 创建管理面板按钮
     */
    private ItemStack createAdminItem() {
        List<String> lore = new ArrayList<>();
        lore.add("&7管理员专用功能");
        lore.add("");
        lore.add("&e左键: 打开管理面板");
        lore.add("");
        lore.add("&c仅管理员可用");
        
        return createItem(Material.REDSTONE_BLOCK, 
                         getMessage("gui.admin-item"), 
                         lore);
    }
    
    /**
     * 创建信息显示
     */
    private ItemStack createInfoItem() {
        List<String> lore = new ArrayList<>();
        lore.add("&7插件信息");
        lore.add("");
        lore.add("&e版本: &f" + plugin.getDescription().getVersion());
        lore.add("&e支持货币: &f" + plugin.getCurrencyManager().getCurrencyCount() + " 种");
        lore.add("&e默认货币: &f" + plugin.getCurrencyManager().getDefaultCurrency().getName());
        lore.add("");
        lore.add("&7数据库: &f" + plugin.getConfigManager().getDatabaseType().toUpperCase());
        
        // 集成状态
        if (plugin.getConfigManager().isVaultIntegrationEnabled()) {
            lore.add("&aVault集成: 已启用");
        }
        if (plugin.getConfigManager().isPlaceholderAPIIntegrationEnabled()) {
            lore.add("&aPlaceholderAPI集成: 已启用");
        }
        
        return createItem(Material.BOOK, 
                         "&6&l插件信息", 
                         lore);
    }
    
    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = getClickedSlot(event);
        
        switch (slot) {
            case 11: // 余额查看
                handleBalanceClick(event);
                break;
                
            case 13: // 货币兑换
                handleExchangeClick(event);
                break;
                
            case 15: // 转账
                handleTransferClick(event);
                break;
                
            case 31: // 管理面板
                handleAdminClick(event);
                break;
                
            case 40: // 关闭
                player.closeInventory();
                playSound(Sound.UI_BUTTON_CLICK);
                break;
                
            default:
                // 其他位置不处理
                break;
        }
    }
    
    /**
     * 处理余额查看点击
     */
    private void handleBalanceClick(InventoryClickEvent event) {
        playSound(Sound.UI_BUTTON_CLICK);
        
        if (isLeftClick(event)) {
            // 打开余额GUI
            plugin.getGUIManager().openBalanceGUI(player);
        } else if (isRightClick(event)) {
            // 显示默认货币余额
            Currency defaultCurrency = plugin.getCurrencyManager().getDefaultCurrency();
            BigDecimal balance = plugin.getCurrencyManager().getBalance(player.getUniqueId());
            sendMessage("&a你的" + defaultCurrency.getName() + "余额: " + 
                       defaultCurrency.formatAmountWithColor(balance));
        }
    }
    
    /**
     * 处理兑换点击
     */
    private void handleExchangeClick(InventoryClickEvent event) {
        if (!plugin.getConfigManager().isExchangeEnabled()) {
            sendMessage("&c兑换功能已被禁用！");
            playSound(Sound.ENTITY_VILLAGER_NO);
            return;
        }
        
        playSound(Sound.UI_BUTTON_CLICK);
        
        if (isLeftClick(event)) {
            // 打开兑换选择GUI
            plugin.getGUIManager().openExchangeSelectGUI(player);
        } else if (isRightClick(event)) {
            // 显示汇率信息
            sendMessage("&e=== 货币兑换汇率 ===");
            for (Currency currency : plugin.getCurrencyManager().getEnabledCurrencies()) {
                sendMessage("&7" + currency.getName() + " (" + currency.getSymbol() + ")");
            }
        }
    }
    
    /**
     * 处理转账点击
     */
    private void handleTransferClick(InventoryClickEvent event) {
        if (!plugin.getConfigManager().isTransferEnabled()) {
            sendMessage("&c转账功能已被禁用！");
            playSound(Sound.ENTITY_VILLAGER_NO);
            return;
        }
        
        playSound(Sound.UI_BUTTON_CLICK);
        sendMessage("&e转账功能正在开发中...");
        // TODO: 实现转账GUI
    }
    
    /**
     * 处理管理面板点击
     */
    private void handleAdminClick(InventoryClickEvent event) {
        if (!player.hasPermission("cnmoney.admin")) {
            sendMessage("&c你没有权限使用管理功能！");
            playSound(Sound.ENTITY_VILLAGER_NO);
            return;
        }

        plugin.getGUIManager().openAdminGUI(player);
        playSound(Sound.UI_BUTTON_CLICK);
    }
}
