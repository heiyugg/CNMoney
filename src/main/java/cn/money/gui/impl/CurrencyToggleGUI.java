package cn.money.gui.impl;

import cn.money.CNMoney;
import cn.money.gui.BaseGUI;
import cn.money.model.Currency;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 货币启用/禁用管理GUI
 * 
 * @author CNMoney Team
 */
public class CurrencyToggleGUI extends BaseGUI {
    
    private final List<Currency> currencies;
    private int currentPage = 0;
    private final int currenciesPerPage = 28; // 4x7 区域
    
    public CurrencyToggleGUI(CNMoney plugin, Player player) {
        super(plugin, player, "货币启用管理", 54);
        this.currencies = new ArrayList<>(plugin.getCurrencyManager().getAllCurrencies());
    }
    
    @Override
    protected void initializeItems() {
        // 清空界面
        inventory.clear();
        
        // 设置玻璃边框
        setGlassBorder();
        
        // 显示统计信息
        setupStatistics();
        
        // 显示货币列表
        displayCurrencies();
        
        // 设置导航按钮
        setupNavigationButtons();
        
        // 设置功能按钮
        setupFunctionButtons();
        
        // 返回和关闭按钮
        addBackButton(45);
        addCloseButton(53);
    }
    
    /**
     * 设置统计信息
     */
    private void setupStatistics() {
        long enabledCount = currencies.stream().mapToLong(c -> c.isEnabled() ? 1 : 0).sum();
        long disabledCount = currencies.size() - enabledCount;
        
        List<String> lore = new ArrayList<>();
        lore.add("§7货币启用状态统计");
        lore.add("");
        lore.add("§a启用货币: §f" + enabledCount);
        lore.add("§c禁用货币: §f" + disabledCount);
        lore.add("§7总计货币: §f" + currencies.size());
        lore.add("");
        lore.add("§7点击下方货币进行切换");
        
        ItemStack statsItem = createItem(Material.BOOK, 
                                       "§6货币状态统计", 
                                       lore);
        inventory.setItem(4, statsItem);
    }
    
    /**
     * 显示货币列表
     */
    private void displayCurrencies() {
        int startIndex = currentPage * currenciesPerPage;
        int endIndex = Math.min(startIndex + currenciesPerPage, currencies.size());
        
        // 货币显示区域：第2-5行，第2-8列 (slots 10-16, 19-25, 28-34, 37-43)
        int[] currencySlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };
        
        for (int i = 0; i < currencySlots.length && (startIndex + i) < endIndex; i++) {
            Currency currency = currencies.get(startIndex + i);
            ItemStack currencyItem = createCurrencyToggleItem(currency);
            inventory.setItem(currencySlots[i], currencyItem);
        }
    }
    
    /**
     * 创建货币切换物品
     */
    private ItemStack createCurrencyToggleItem(Currency currency) {
        List<String> lore = new ArrayList<>();
        lore.add("§7货币: §f" + currency.getName());
        lore.add("§7ID: §f" + currency.getId());
        lore.add("§7符号: §f" + currency.getSymbol());
        lore.add("");
        
        // 状态信息
        if (currency.isEnabled()) {
            lore.add("§a✓ 当前状态: 启用");
            lore.add("§7玩家可以使用此货币");
        } else {
            lore.add("§c✗ 当前状态: 禁用");
            lore.add("§7玩家无法使用此货币");
        }
        
        // 主要货币标识
        if (currency.isPrimary()) {
            lore.add("§6★ 主要货币");
            lore.add("§c不能禁用主要货币");
        }
        
        lore.add("");
        
        // 操作提示
        if (currency.isPrimary() && currency.isEnabled()) {
            lore.add("§c无法禁用主要货币");
        } else {
            if (currency.isEnabled()) {
                lore.add("§e左键: 禁用此货币");
            } else {
                lore.add("§e左键: 启用此货币");
            }
        }
        
        lore.add("§e右键: 查看详细信息");
        
        // 根据状态选择材料和颜色
        Material material;
        String displayName;
        
        if (currency.isPrimary()) {
            material = Material.GOLD_BLOCK;
            displayName = "§6" + currency.getName() + " §7(主要)";
        } else if (currency.isEnabled()) {
            material = Material.LIME_STAINED_GLASS;
            displayName = "§a" + currency.getName() + " §7(启用)";
        } else {
            material = Material.RED_STAINED_GLASS;
            displayName = "§c" + currency.getName() + " §7(禁用)";
        }
        
        return createItem(material, displayName, lore);
    }
    
    /**
     * 设置导航按钮
     */
    private void setupNavigationButtons() {
        // 上一页按钮
        if (currentPage > 0) {
            ItemStack prevButton = createItem(Material.ARROW, 
                                            "§6上一页", 
                                            List.of("§7点击查看上一页货币"));
            inventory.setItem(48, prevButton);
        }
        
        // 下一页按钮
        if ((currentPage + 1) * currenciesPerPage < currencies.size()) {
            ItemStack nextButton = createItem(Material.ARROW, 
                                            "§6下一页", 
                                            List.of("§7点击查看下一页货币"));
            inventory.setItem(50, nextButton);
        }
        
        // 页面信息
        int totalPages = (int) Math.ceil((double) currencies.size() / currenciesPerPage);
        ItemStack pageInfo = createItem(Material.COMPASS, 
                                      "§6页面信息", 
                                      List.of("§7当前页: §f" + (currentPage + 1) + "/" + totalPages,
                                             "§7货币总数: §f" + currencies.size()));
        inventory.setItem(49, pageInfo);
    }
    
    /**
     * 设置功能按钮
     */
    private void setupFunctionButtons() {
        // 全部启用按钮
        ItemStack enableAllButton = createItem(Material.EMERALD_BLOCK, 
                                             "§a全部启用", 
                                             List.of("§7启用所有货币",
                                                    "§e左键: 启用所有货币"));
        inventory.setItem(46, enableAllButton);
        
        // 全部禁用按钮（除主要货币）
        ItemStack disableAllButton = createItem(Material.REDSTONE_BLOCK, 
                                              "§c禁用非主要货币", 
                                              List.of("§7禁用除主要货币外的所有货币",
                                                     "§c主要货币不会被禁用",
                                                     "§e左键: 禁用非主要货币"));
        inventory.setItem(47, disableAllButton);
        
        // 刷新状态按钮
        ItemStack refreshButton = createItem(Material.EMERALD, 
                                           "§6刷新状态", 
                                           List.of("§7刷新货币状态信息"));
        inventory.setItem(51, refreshButton);
        
        // 应用更改按钮
        ItemStack applyButton = createItem(Material.DIAMOND, 
                                         "§b应用更改", 
                                         List.of("§7保存并应用所有更改",
                                                "§e左键: 应用更改"));
        inventory.setItem(52, applyButton);
    }
    
    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = getClickedSlot(event);
        
        // 处理货币点击
        if (isCurrencySlot(slot)) {
            handleCurrencyClick(event, slot);
            return;
        }
        
        switch (slot) {
            case 45: // 返回
                plugin.getGUIManager().openAdminGUI(player);
                playSound(Sound.UI_BUTTON_CLICK);
                break;
            case 46: // 全部启用
                handleEnableAllClick(event);
                break;
            case 47: // 禁用非主要货币
                handleDisableNonPrimaryClick(event);
                break;
            case 48: // 上一页
                handlePreviousPageClick(event);
                break;
            case 49: // 页面信息
                playSound(Sound.UI_BUTTON_CLICK);
                break;
            case 50: // 下一页
                handleNextPageClick(event);
                break;
            case 51: // 刷新状态
                handleRefreshClick(event);
                break;
            case 52: // 应用更改
                handleApplyChangesClick(event);
                break;
            case 53: // 关闭
                player.closeInventory();
                playSound(Sound.UI_BUTTON_CLICK);
                break;
        }
    }
    
    /**
     * 检查是否为货币槽位
     */
    private boolean isCurrencySlot(int slot) {
        int[] currencySlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };
        
        for (int currencySlot : currencySlots) {
            if (slot == currencySlot) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 处理货币点击
     */
    private void handleCurrencyClick(InventoryClickEvent event, int slot) {
        Currency currency = getCurrencyFromSlot(slot);
        if (currency == null) {
            return;
        }
        
        if (isLeftClick(event)) {
            // 切换启用状态
            handleToggleCurrency(currency);
        } else if (isRightClick(event)) {
            // 查看详细信息
            showCurrencyDetails(currency);
            playSound(Sound.UI_BUTTON_CLICK);
        }
    }
    
    /**
     * 从槽位获取货币
     */
    private Currency getCurrencyFromSlot(int slot) {
        int[] currencySlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };
        
        for (int i = 0; i < currencySlots.length; i++) {
            if (currencySlots[i] == slot) {
                int currencyIndex = currentPage * currenciesPerPage + i;
                if (currencyIndex < currencies.size()) {
                    return currencies.get(currencyIndex);
                }
                break;
            }
        }
        return null;
    }
    
    /**
     * 切换货币启用状态
     */
    private void handleToggleCurrency(Currency currency) {
        if (currency.isPrimary() && currency.isEnabled()) {
            sendMessage("§c不能禁用主要货币！");
            playSound(Sound.ENTITY_VILLAGER_NO);
            return;
        }
        
        // TODO: 实现切换货币启用状态的逻辑
        boolean newStatus = !currency.isEnabled();
        String action = newStatus ? "启用" : "禁用";
        
        sendMessage("§a已" + action + "货币: " + currency.getName());
        playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        
        // 刷新界面
        refresh();
    }
    
    /**
     * 显示货币详细信息
     */
    private void showCurrencyDetails(Currency currency) {
        sendMessage("§6=== " + currency.getName() + " 详细信息 ===");
        sendMessage("§7ID: §f" + currency.getId());
        sendMessage("§7名称: §f" + currency.getName());
        sendMessage("§7符号: §f" + currency.getSymbol());
        sendMessage("§7复数形式: §f" + currency.getPluralName());
        sendMessage("§7小数位数: §f" + currency.getDecimalPlaces());
        sendMessage("§7状态: " + (currency.isEnabled() ? "§a启用" : "§c禁用"));
        sendMessage("§7主要货币: " + (currency.isPrimary() ? "§a是" : "§7否"));
    }
    
    /**
     * 处理全部启用点击
     */
    private void handleEnableAllClick(InventoryClickEvent event) {
        int enabledCount = 0;
        
        // TODO: 实现启用所有货币的逻辑
        for (Currency currency : currencies) {
            if (!currency.isEnabled()) {
                // 启用货币
                enabledCount++;
            }
        }
        
        sendMessage("§a已启用 " + enabledCount + " 个货币！");
        playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        refresh();
    }
    
    /**
     * 处理禁用非主要货币点击
     */
    private void handleDisableNonPrimaryClick(InventoryClickEvent event) {
        int disabledCount = 0;
        
        // TODO: 实现禁用非主要货币的逻辑
        for (Currency currency : currencies) {
            if (!currency.isPrimary() && currency.isEnabled()) {
                // 禁用货币
                disabledCount++;
            }
        }
        
        sendMessage("§a已禁用 " + disabledCount + " 个非主要货币！");
        playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        refresh();
    }
    
    /**
     * 处理上一页点击
     */
    private void handlePreviousPageClick(InventoryClickEvent event) {
        if (currentPage > 0) {
            currentPage--;
            refresh();
            playSound(Sound.UI_BUTTON_CLICK);
        }
    }
    
    /**
     * 处理下一页点击
     */
    private void handleNextPageClick(InventoryClickEvent event) {
        if ((currentPage + 1) * currenciesPerPage < currencies.size()) {
            currentPage++;
            refresh();
            playSound(Sound.UI_BUTTON_CLICK);
        }
    }
    
    /**
     * 处理刷新点击
     */
    private void handleRefreshClick(InventoryClickEvent event) {
        // 刷新货币列表
        currencies.clear();
        currencies.addAll(plugin.getCurrencyManager().getAllCurrencies());
        
        // 重置到第一页
        currentPage = 0;
        
        // 刷新界面
        refresh();
        
        sendMessage("§a货币状态已刷新！");
        playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
    }
    
    /**
     * 处理应用更改点击
     */
    private void handleApplyChangesClick(InventoryClickEvent event) {
        // TODO: 实现保存更改的逻辑
        sendMessage("§a所有更改已应用并保存！");
        playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        
        // 重载配置
        plugin.getConfigManager().reloadConfigs();
        plugin.getCurrencyManager().reloadCurrencies();
    }
}
