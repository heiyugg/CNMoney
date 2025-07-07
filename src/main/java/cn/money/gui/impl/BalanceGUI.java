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
 * 余额查看GUI
 * 
 * @author CNMoney Team
 */
public class BalanceGUI extends BaseGUI {
    
    private int currentPage = 0;
    private final int itemsPerPage = 21; // 7x3的区域
    
    public BalanceGUI(CNMoney plugin, Player player) {
        super(plugin, player, plugin.getConfigManager().getMessage("gui.balance-title"), 54);
    }
    
    @Override
    protected void initializeItems() {
        // 清空界面
        inventory.clear();
        
        // 设置玻璃边框
        setGlassBorder();
        
        // 获取所有启用的货币
        List<Currency> currencies = new ArrayList<>(plugin.getCurrencyManager().getEnabledCurrencies());
        
        if (currencies.isEmpty()) {
            // 没有货币时显示提示
            ItemStack noDataItem = createItem(Material.BARRIER, 
                                            "&c没有可用的货币", 
                                            "&7请联系管理员添加货币配置");
            inventory.setItem(22, noDataItem);
        } else {
            // 计算分页
            int totalPages = (int) Math.ceil((double) currencies.size() / itemsPerPage);
            int startIndex = currentPage * itemsPerPage;
            int endIndex = Math.min(startIndex + itemsPerPage, currencies.size());
            
            // 显示货币
            int slot = 10; // 从第二行第二列开始
            for (int i = startIndex; i < endIndex; i++) {
                Currency currency = currencies.get(i);
                ItemStack currencyItem = createCurrencyItem(currency);
                inventory.setItem(slot, currencyItem);
                
                // 计算下一个槽位
                slot++;
                if ((slot + 1) % 9 == 0) { // 跳过右边框
                    slot += 2;
                }
                if (slot >= 44) break; // 不超过底部边框
            }
            
            // 分页按钮
            if (currentPage > 0) {
                addPrevPageButton(45);
            }
            if (currentPage < totalPages - 1) {
                addNextPageButton(53);
            }
            
            // 页面信息
            if (totalPages > 1) {
                ItemStack pageInfo = createItem(Material.PAPER, 
                                              "&e第 " + (currentPage + 1) + " 页 / 共 " + totalPages + " 页",
                                              "&7显示 " + currencies.size() + " 种货币");
                inventory.setItem(49, pageInfo);
            }
        }
        
        // 返回按钮
        addBackButton(48);
        
        // 关闭按钮
        addCloseButton(50);
        
        // 刷新按钮
        ItemStack refreshItem = createItem(Material.CLOCK,
                                         "&a刷新余额",
                                         "&7点击刷新当前余额信息");
        inventory.setItem(46, refreshItem);
    }
    
    /**
     * 创建货币物品
     */
    private ItemStack createCurrencyItem(Currency currency) {
        BigDecimal balance = plugin.getCurrencyManager().getBalance(player.getUniqueId(), currency.getId());
        
        List<String> lore = new ArrayList<>();
        lore.add("&7货币名称: &f" + currency.getName());
        lore.add("&7货币符号: &f" + currency.getSymbol());
        lore.add("&7当前余额: " + currency.formatAmountWithColor(balance));
        lore.add("");
        
        // 标记主要货币
        if (currency.isVaultPrimary()) {
            lore.add("&6&l★ 主要货币");
            lore.add("");
        }
        
        // 添加操作提示
        lore.add("&e左键: 查看详细信息");
        lore.add("&e右键: 快速兑换此货币");
        lore.add("&eShift+左键: 复制余额到聊天框");
        
        // 根据余额选择材料和颜色
        Material material = getMaterialForCurrency(currency);
        String displayName = currency.getColor() + currency.getName();
        
        // 如果余额为0，添加特殊标记
        if (balance.compareTo(BigDecimal.ZERO) == 0) {
            displayName += " &7(无余额)";
        }
        
        return createItem(material, displayName, lore);
    }
    
    /**
     * 根据货币获取对应的材料
     */
    private Material getMaterialForCurrency(Currency currency) {
        String currencyId = currency.getId().toLowerCase();
        
        // 根据货币ID选择合适的材料
        if (currencyId.contains("gold")) {
            return Material.GOLD_INGOT;
        } else if (currencyId.contains("silver")) {
            return Material.IRON_INGOT;
        } else if (currencyId.contains("copper")) {
            return Material.COPPER_INGOT;
        } else if (currencyId.contains("diamond")) {
            return Material.DIAMOND;
        } else if (currencyId.contains("emerald")) {
            return Material.EMERALD;
        } else if (currencyId.contains("coin")) {
            return Material.SUNFLOWER;
        } else {
            return Material.GOLD_NUGGET; // 默认材料
        }
    }
    
    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = getClickedSlot(event);
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        
        // 处理特殊按钮
        switch (slot) {
            case 45: // 上一页
                if (currentPage > 0) {
                    currentPage--;
                    refresh();
                    playSound(Sound.UI_BUTTON_CLICK);
                }
                return;
                
            case 53: // 下一页
                List<Currency> currencies = new ArrayList<>(plugin.getCurrencyManager().getEnabledCurrencies());
                int totalPages = (int) Math.ceil((double) currencies.size() / itemsPerPage);
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    refresh();
                    playSound(Sound.UI_BUTTON_CLICK);
                }
                return;
                
            case 48: // 返回
                plugin.getGUIManager().openMainGUI(player);
                playSound(Sound.UI_BUTTON_CLICK);
                return;
                
            case 50: // 关闭
                player.closeInventory();
                playSound(Sound.UI_BUTTON_CLICK);
                return;
                
            case 46: // 刷新
                refresh();
                playSound(Sound.BLOCK_NOTE_BLOCK_PLING);
                sendMessage("&a余额信息已刷新！");
                return;
        }
        
        // 处理货币点击
        Currency clickedCurrency = getCurrencyFromSlot(slot);
        if (clickedCurrency != null) {
            handleCurrencyClick(event, clickedCurrency);
        }
    }
    
    /**
     * 从槽位获取对应的货币
     */
    private Currency getCurrencyFromSlot(int slot) {
        List<Currency> currencies = new ArrayList<>(plugin.getCurrencyManager().getEnabledCurrencies());
        
        // 计算在当前页面中的位置
        int[] validSlots = {10, 11, 12, 13, 14, 15, 16,
                           19, 20, 21, 22, 23, 24, 25,
                           28, 29, 30, 31, 32, 33, 34};
        
        int slotIndex = -1;
        for (int i = 0; i < validSlots.length; i++) {
            if (validSlots[i] == slot) {
                slotIndex = i;
                break;
            }
        }
        
        if (slotIndex == -1) return null;
        
        int currencyIndex = currentPage * itemsPerPage + slotIndex;
        if (currencyIndex >= currencies.size()) return null;
        
        return currencies.get(currencyIndex);
    }
    
    /**
     * 处理货币点击
     */
    private void handleCurrencyClick(InventoryClickEvent event, Currency currency) {
        BigDecimal balance = plugin.getCurrencyManager().getBalance(player.getUniqueId(), currency.getId());
        
        if (isLeftClick(event)) {
            if (isShiftClick(event)) {
                // Shift+左键：复制余额到聊天框
                String balanceText = currency.formatAmount(balance);
                sendMessage("&a" + currency.getName() + "余额: &f" + balanceText);
                sendMessage("&7(余额信息已发送到聊天框)");
                playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
            } else {
                // 左键：显示详细信息
                showCurrencyDetails(currency, balance);
                playSound(Sound.UI_BUTTON_CLICK);
            }
        } else if (isRightClick(event)) {
            // 右键：快速兑换
            if (plugin.getConfigManager().isExchangeEnabled()) {
                plugin.getGUIManager().openExchangeSelectGUI(player);
                playSound(Sound.UI_BUTTON_CLICK);
            } else {
                sendMessage("&c兑换功能已被禁用！");
                playSound(Sound.ENTITY_VILLAGER_NO);
            }
        }
    }
    
    /**
     * 显示货币详细信息
     */
    private void showCurrencyDetails(Currency currency, BigDecimal balance) {
        sendMessage("&6&l=== " + currency.getName() + " 详细信息 ===");
        sendMessage("&e货币ID: &f" + currency.getId());
        sendMessage("&e货币名称: &f" + currency.getName());
        sendMessage("&e货币符号: &f" + currency.getSymbol());
        sendMessage("&e当前余额: " + currency.formatAmountWithColor(balance));
        sendMessage("&e小数位数: &f" + currency.getDecimals());
        
        if (currency.isVaultPrimary()) {
            sendMessage("&a这是主要货币");
        }
        
        if (currency.isVaultPrimary()) {
            sendMessage("&aVault主货币");
        }
        
        sendMessage("&6&l========================");
    }
}
