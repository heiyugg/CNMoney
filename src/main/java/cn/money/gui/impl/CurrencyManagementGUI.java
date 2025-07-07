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
 * 货币管理GUI
 * 
 * @author CNMoney Team
 */
public class CurrencyManagementGUI extends BaseGUI {
    
    private final List<Currency> currencies;
    private int currentPage = 0;
    private final int currenciesPerPage = 21; // 3x7 区域
    
    public CurrencyManagementGUI(CNMoney plugin, Player player) {
        super(plugin, player, "货币管理", 54);
        this.currencies = new ArrayList<>(plugin.getCurrencyManager().getAllCurrencies());
    }
    
    @Override
    protected void initializeItems() {
        // 清空界面
        inventory.clear();
        
        // 设置玻璃边框
        setGlassBorder();
        
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
     * 显示货币列表
     */
    private void displayCurrencies() {
        int startIndex = currentPage * currenciesPerPage;
        int endIndex = Math.min(startIndex + currenciesPerPage, currencies.size());
        
        // 货币显示区域：第2-4行，第2-8列 (slots 10-16, 19-25, 28-34)
        int[] currencySlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
        };
        
        for (int i = 0; i < currencySlots.length && (startIndex + i) < endIndex; i++) {
            Currency currency = currencies.get(startIndex + i);
            ItemStack currencyItem = createCurrencyItem(currency);
            inventory.setItem(currencySlots[i], currencyItem);
        }
    }
    
    /**
     * 创建货币物品
     */
    private ItemStack createCurrencyItem(Currency currency) {
        List<String> lore = new ArrayList<>();
        lore.add("§7货币ID: §f" + currency.getId());
        lore.add("§7名称: §f" + currency.getName());
        lore.add("§7符号: §f" + currency.getSymbol());
        lore.add("§7复数形式: §f" + currency.getPluralName());
        lore.add("§7小数位数: §f" + currency.getDecimalPlaces());
        lore.add("§7状态: " + (currency.isEnabled() ? "§a启用" : "§c禁用"));
        lore.add("§7主要货币: " + (currency.isPrimary() ? "§a是" : "§7否"));
        lore.add("");
        
        // 显示格式化示例
        BigDecimal sampleAmount = new BigDecimal("1234.56");
        lore.add("§7格式化示例:");
        lore.add("§f" + currency.formatAmountWithColor(sampleAmount));
        lore.add("");
        
        lore.add("§e左键: 编辑货币");
        lore.add("§e右键: 切换启用状态");
        lore.add("§eShift+左键: 设为主要货币");
        lore.add("§eShift+右键: 删除货币");
        
        // 根据货币状态选择材料
        Material material;
        if (!currency.isEnabled()) {
            material = Material.GRAY_STAINED_GLASS;
        } else if (currency.isPrimary()) {
            material = Material.GOLD_BLOCK;
        } else {
            material = Material.EMERALD_BLOCK;
        }
        
        return createItem(material, 
                         "§6" + currency.getName(), 
                         lore);
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
        ItemStack pageInfo = createItem(Material.BOOK, 
                                      "§6页面信息", 
                                      List.of("§7当前页: §f" + (currentPage + 1) + "/" + totalPages,
                                             "§7货币总数: §f" + currencies.size()));
        inventory.setItem(49, pageInfo);
    }
    
    /**
     * 设置功能按钮
     */
    private void setupFunctionButtons() {
        // 添加新货币按钮
        ItemStack addButton = createItem(Material.LIME_STAINED_GLASS, 
                                       "§a添加新货币", 
                                       List.of("§7创建一个新的货币类型",
                                              "§e点击开始创建"));
        inventory.setItem(46, addButton);
        
        // 批量操作按钮
        ItemStack batchButton = createItem(Material.COMMAND_BLOCK, 
                                         "§6批量操作", 
                                         List.of("§7批量管理货币设置",
                                                "§e功能开发中..."));
        inventory.setItem(47, batchButton);
        
        // 刷新列表按钮
        ItemStack refreshButton = createItem(Material.EMERALD, 
                                           "§6刷新列表", 
                                           List.of("§7刷新货币列表"));
        inventory.setItem(51, refreshButton);
        
        // 导入/导出按钮
        ItemStack importExportButton = createItem(Material.PAPER, 
                                                "§6导入/导出", 
                                                List.of("§7导入或导出货币配置",
                                                       "§e功能开发中..."));
        inventory.setItem(52, importExportButton);
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
            case 46: // 添加新货币
                handleAddCurrencyClick(event);
                break;
            case 47: // 批量操作
                handleBatchClick(event);
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
            case 51: // 刷新列表
                handleRefreshClick(event);
                break;
            case 52: // 导入/导出
                handleImportExportClick(event);
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
            28, 29, 30, 31, 32, 33, 34
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
            if (isShiftClick(event)) {
                // Shift+左键：设为主要货币
                handleSetPrimaryCurrency(currency);
            } else {
                // 左键：编辑货币
                // TODO: 实现货币编辑GUI
                sendMessage("§e货币编辑功能开发中...");
                playSound(Sound.UI_BUTTON_CLICK);
            }
        } else if (isRightClick(event)) {
            if (isShiftClick(event)) {
                // Shift+右键：删除货币
                handleDeleteCurrency(currency);
            } else {
                // 右键：切换启用状态
                handleToggleCurrency(currency);
            }
        }
    }
    
    /**
     * 从槽位获取货币
     */
    private Currency getCurrencyFromSlot(int slot) {
        int[] currencySlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
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
     * 设置主要货币
     */
    private void handleSetPrimaryCurrency(Currency currency) {
        if (currency.isPrimary()) {
            sendMessage("§c" + currency.getName() + " 已经是主要货币了！");
            playSound(Sound.ENTITY_VILLAGER_NO);
            return;
        }
        
        // TODO: 实现设置主要货币的逻辑
        sendMessage("§a已将 " + currency.getName() + " 设置为主要货币！");
        playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        refresh();
    }
    
    /**
     * 删除货币
     */
    private void handleDeleteCurrency(Currency currency) {
        if (currency.isPrimary()) {
            sendMessage("§c不能删除主要货币！");
            playSound(Sound.ENTITY_VILLAGER_NO);
            return;
        }
        
        sendMessage("§c确认要删除货币 " + currency.getName() + " 吗？");
        sendMessage("§c这将删除所有相关的余额数据！");
        sendMessage("§c请在聊天中输入 'confirm delete " + currency.getId() + "' 确认操作");
        playSound(Sound.ENTITY_VILLAGER_NO);
        
        // TODO: 实现确认机制和删除逻辑
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
        
        // TODO: 实现切换启用状态的逻辑
        boolean newStatus = !currency.isEnabled();
        sendMessage("§a已" + (newStatus ? "启用" : "禁用") + "货币 " + currency.getName());
        playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        refresh();
    }
    
    /**
     * 处理添加货币点击
     */
    private void handleAddCurrencyClick(InventoryClickEvent event) {
        playSound(Sound.UI_BUTTON_CLICK);
        sendMessage("§e新货币创建功能开发中...");
        // TODO: 实现新货币创建GUI
    }
    
    /**
     * 处理批量操作点击
     */
    private void handleBatchClick(InventoryClickEvent event) {
        playSound(Sound.UI_BUTTON_CLICK);
        sendMessage("§e批量操作功能开发中...");
        // TODO: 实现批量操作功能
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
        
        sendMessage("§a货币列表已刷新！当前货币数量: " + currencies.size());
        playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
    }
    
    /**
     * 处理导入/导出点击
     */
    private void handleImportExportClick(InventoryClickEvent event) {
        playSound(Sound.UI_BUTTON_CLICK);
        sendMessage("§e导入/导出功能开发中...");
        // TODO: 实现导入/导出功能
    }
}
