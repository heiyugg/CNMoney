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
 * 汇率管理GUI
 * 
 * @author CNMoney Team
 */
public class ExchangeRateManagementGUI extends BaseGUI {
    
    private final List<Currency> currencies;
    private Currency selectedFromCurrency;
    private int currentPage = 0;
    private final int ratesPerPage = 21; // 3x7 区域
    
    public ExchangeRateManagementGUI(CNMoney plugin, Player player) {
        super(plugin, player, "汇率管理", 54);
        this.currencies = new ArrayList<>(plugin.getCurrencyManager().getEnabledCurrencies());
        this.selectedFromCurrency = currencies.isEmpty() ? null : currencies.get(0);
    }
    
    @Override
    protected void initializeItems() {
        // 清空界面
        inventory.clear();
        
        // 设置玻璃边框
        setGlassBorder();
        
        // 显示基础货币选择
        setupBaseCurrencySelection();
        
        // 显示汇率列表
        displayExchangeRates();
        
        // 设置导航按钮
        setupNavigationButtons();
        
        // 设置功能按钮
        setupFunctionButtons();
        
        // 返回和关闭按钮
        addBackButton(45);
        addCloseButton(53);
    }
    
    /**
     * 设置基础货币选择
     */
    private void setupBaseCurrencySelection() {
        if (selectedFromCurrency == null) return;
        
        List<String> lore = new ArrayList<>();
        lore.add("§7当前选择的基础货币");
        lore.add("§7所有汇率都以此货币为基准");
        lore.add("");
        lore.add("§7货币: §f" + selectedFromCurrency.getName());
        lore.add("§7符号: §f" + selectedFromCurrency.getSymbol());
        lore.add("");
        lore.add("§e左键: 选择其他基础货币");
        lore.add("§e右键: 刷新汇率数据");
        
        ItemStack baseCurrencyItem = createItem(Material.GOLD_BLOCK, 
                                              "§6基础货币: " + selectedFromCurrency.getName(), 
                                              lore);
        inventory.setItem(4, baseCurrencyItem);
    }
    
    /**
     * 显示汇率列表
     */
    private void displayExchangeRates() {
        if (selectedFromCurrency == null) return;
        
        List<Currency> targetCurrencies = new ArrayList<>();
        for (Currency currency : currencies) {
            if (!currency.getId().equals(selectedFromCurrency.getId())) {
                targetCurrencies.add(currency);
            }
        }
        
        int startIndex = currentPage * ratesPerPage;
        int endIndex = Math.min(startIndex + ratesPerPage, targetCurrencies.size());
        
        // 汇率显示区域：第2-4行，第2-8列 (slots 10-16, 19-25, 28-34)
        int[] rateSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
        };
        
        for (int i = 0; i < rateSlots.length && (startIndex + i) < endIndex; i++) {
            Currency targetCurrency = targetCurrencies.get(startIndex + i);
            ItemStack rateItem = createExchangeRateItem(targetCurrency);
            inventory.setItem(rateSlots[i], rateItem);
        }
    }
    
    /**
     * 创建汇率物品
     */
    private ItemStack createExchangeRateItem(Currency targetCurrency) {
        List<String> lore = new ArrayList<>();
        lore.add("§7兑换对: §f" + selectedFromCurrency.getName() + " → " + targetCurrency.getName());
        lore.add("");
        
        // 获取当前汇率
        BigDecimal exchangeRate = plugin.getCurrencyManager().getExchangeRate(
            selectedFromCurrency.getId(), targetCurrency.getId());
        
        lore.add("§7当前汇率: §f" + exchangeRate.toString());
        lore.add("§7即: §f1 " + selectedFromCurrency.getSymbol() + " = " + 
                exchangeRate + " " + targetCurrency.getSymbol());
        lore.add("");
        
        // 计算示例
        BigDecimal sampleAmount = new BigDecimal("100");
        BigDecimal convertedAmount = sampleAmount.multiply(exchangeRate);
        lore.add("§7兑换示例:");
        lore.add("§f" + selectedFromCurrency.formatAmount(sampleAmount) + " " + 
                selectedFromCurrency.getName() + " = ");
        lore.add("§f" + targetCurrency.formatAmount(convertedAmount) + " " + 
                targetCurrency.getName());
        lore.add("");
        
        // 反向汇率
        if (exchangeRate.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal reverseRate = BigDecimal.ONE.divide(exchangeRate, 6, BigDecimal.ROUND_HALF_UP);
            lore.add("§7反向汇率: §f" + reverseRate.toString());
            lore.add("§7即: §f1 " + targetCurrency.getSymbol() + " = " + 
                    reverseRate + " " + selectedFromCurrency.getSymbol());
        }
        lore.add("");
        
        lore.add("§e左键: 编辑汇率");
        lore.add("§e右键: 查看历史汇率");
        lore.add("§eShift+左键: 设置为1:1汇率");
        
        return createItem(Material.EMERALD, 
                         "§6" + targetCurrency.getName(), 
                         lore);
    }
    
    /**
     * 设置导航按钮
     */
    private void setupNavigationButtons() {
        if (selectedFromCurrency == null) return;
        
        List<Currency> targetCurrencies = new ArrayList<>();
        for (Currency currency : currencies) {
            if (!currency.getId().equals(selectedFromCurrency.getId())) {
                targetCurrencies.add(currency);
            }
        }
        
        // 上一页按钮
        if (currentPage > 0) {
            ItemStack prevButton = createItem(Material.ARROW, 
                                            "§6上一页", 
                                            List.of("§7点击查看上一页汇率"));
            inventory.setItem(48, prevButton);
        }
        
        // 下一页按钮
        if ((currentPage + 1) * ratesPerPage < targetCurrencies.size()) {
            ItemStack nextButton = createItem(Material.ARROW, 
                                            "§6下一页", 
                                            List.of("§7点击查看下一页汇率"));
            inventory.setItem(50, nextButton);
        }
        
        // 页面信息
        int totalPages = (int) Math.ceil((double) targetCurrencies.size() / ratesPerPage);
        ItemStack pageInfo = createItem(Material.BOOK, 
                                      "§6页面信息", 
                                      List.of("§7当前页: §f" + (currentPage + 1) + "/" + totalPages,
                                             "§7汇率对数: §f" + targetCurrencies.size()));
        inventory.setItem(49, pageInfo);
    }
    
    /**
     * 设置功能按钮
     */
    private void setupFunctionButtons() {
        // 批量设置按钮
        ItemStack batchSetButton = createItem(Material.COMMAND_BLOCK, 
                                            "§6批量设置", 
                                            List.of("§7批量设置多个汇率",
                                                   "§e功能开发中..."));
        inventory.setItem(46, batchSetButton);
        
        // 重置汇率按钮
        ItemStack resetButton = createItem(Material.BARRIER, 
                                         "§c重置汇率", 
                                         List.of("§7将所有汇率重置为默认值",
                                                "§c谨慎操作！"));
        inventory.setItem(47, resetButton);
        
        // 导入汇率按钮
        ItemStack importButton = createItem(Material.PAPER, 
                                          "§6导入汇率", 
                                          List.of("§7从文件导入汇率配置",
                                                 "§e功能开发中..."));
        inventory.setItem(51, importButton);
        
        // 导出汇率按钮
        ItemStack exportButton = createItem(Material.WRITABLE_BOOK, 
                                          "§6导出汇率", 
                                          List.of("§7导出当前汇率配置",
                                                 "§e功能开发中..."));
        inventory.setItem(52, exportButton);
    }
    
    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = getClickedSlot(event);
        
        // 处理基础货币选择
        if (slot == 4) {
            handleBaseCurrencyClick(event);
            return;
        }
        
        // 处理汇率点击
        if (isExchangeRateSlot(slot)) {
            handleExchangeRateClick(event, slot);
            return;
        }
        
        switch (slot) {
            case 45: // 返回
                plugin.getGUIManager().openAdminGUI(player);
                playSound(Sound.UI_BUTTON_CLICK);
                break;
            case 46: // 批量设置
                handleBatchSetClick(event);
                break;
            case 47: // 重置汇率
                handleResetRatesClick(event);
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
            case 51: // 导入汇率
                handleImportRatesClick(event);
                break;
            case 52: // 导出汇率
                handleExportRatesClick(event);
                break;
            case 53: // 关闭
                player.closeInventory();
                playSound(Sound.UI_BUTTON_CLICK);
                break;
        }
    }
    
    /**
     * 处理基础货币点击
     */
    private void handleBaseCurrencyClick(InventoryClickEvent event) {
        if (isLeftClick(event)) {
            // 打开基础货币选择GUI
            // TODO: 实现基础货币选择GUI
            sendMessage("§e基础货币选择功能开发中...");
            playSound(Sound.UI_BUTTON_CLICK);
        } else if (isRightClick(event)) {
            // 刷新汇率数据
            refresh();
            sendMessage("§a汇率数据已刷新！");
            playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        }
    }
    
    /**
     * 检查是否为汇率槽位
     */
    private boolean isExchangeRateSlot(int slot) {
        int[] rateSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
        };
        
        for (int rateSlot : rateSlots) {
            if (slot == rateSlot) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 处理汇率点击
     */
    private void handleExchangeRateClick(InventoryClickEvent event, int slot) {
        Currency targetCurrency = getTargetCurrencyFromSlot(slot);
        if (targetCurrency == null || selectedFromCurrency == null) {
            return;
        }
        
        if (isLeftClick(event)) {
            if (isShiftClick(event)) {
                // Shift+左键：设置为1:1汇率
                handleSetOneToOneRate(targetCurrency);
            } else {
                // 左键：编辑汇率
                // TODO: 实现汇率编辑GUI
                sendMessage("§e汇率编辑功能开发中...");
                playSound(Sound.UI_BUTTON_CLICK);
            }
        } else if (isRightClick(event)) {
            // 右键：查看历史汇率
            showExchangeRateHistory(targetCurrency);
            playSound(Sound.UI_BUTTON_CLICK);
        }
    }
    
    /**
     * 从槽位获取目标货币
     */
    private Currency getTargetCurrencyFromSlot(int slot) {
        if (selectedFromCurrency == null) return null;
        
        List<Currency> targetCurrencies = new ArrayList<>();
        for (Currency currency : currencies) {
            if (!currency.getId().equals(selectedFromCurrency.getId())) {
                targetCurrencies.add(currency);
            }
        }
        
        int[] rateSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
        };
        
        for (int i = 0; i < rateSlots.length; i++) {
            if (rateSlots[i] == slot) {
                int currencyIndex = currentPage * ratesPerPage + i;
                if (currencyIndex < targetCurrencies.size()) {
                    return targetCurrencies.get(currencyIndex);
                }
                break;
            }
        }
        return null;
    }
    
    /**
     * 设置1:1汇率
     */
    private void handleSetOneToOneRate(Currency targetCurrency) {
        // TODO: 实现设置1:1汇率的逻辑
        sendMessage("§a已将 " + selectedFromCurrency.getName() + " → " + 
                   targetCurrency.getName() + " 的汇率设置为 1:1");
        playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        refresh();
    }
    
    /**
     * 显示汇率历史
     */
    private void showExchangeRateHistory(Currency targetCurrency) {
        sendMessage("§6=== " + selectedFromCurrency.getName() + " → " + 
                   targetCurrency.getName() + " 汇率历史 ===");
        sendMessage("§e汇率历史功能开发中...");
        // TODO: 实现汇率历史查看功能
    }
    
    /**
     * 处理批量设置点击
     */
    private void handleBatchSetClick(InventoryClickEvent event) {
        playSound(Sound.UI_BUTTON_CLICK);
        sendMessage("§e批量设置功能开发中...");
        // TODO: 实现批量设置功能
    }
    
    /**
     * 处理重置汇率点击
     */
    private void handleResetRatesClick(InventoryClickEvent event) {
        sendMessage("§c确认要重置所有汇率为默认值吗？");
        sendMessage("§c请在聊天中输入 'confirm reset rates' 确认操作");
        playSound(Sound.ENTITY_VILLAGER_NO);
        // TODO: 实现确认机制和重置逻辑
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
        if (selectedFromCurrency == null) return;
        
        List<Currency> targetCurrencies = new ArrayList<>();
        for (Currency currency : currencies) {
            if (!currency.getId().equals(selectedFromCurrency.getId())) {
                targetCurrencies.add(currency);
            }
        }
        
        if ((currentPage + 1) * ratesPerPage < targetCurrencies.size()) {
            currentPage++;
            refresh();
            playSound(Sound.UI_BUTTON_CLICK);
        }
    }
    
    /**
     * 处理导入汇率点击
     */
    private void handleImportRatesClick(InventoryClickEvent event) {
        playSound(Sound.UI_BUTTON_CLICK);
        sendMessage("§e导入汇率功能开发中...");
        // TODO: 实现导入汇率功能
    }
    
    /**
     * 处理导出汇率点击
     */
    private void handleExportRatesClick(InventoryClickEvent event) {
        playSound(Sound.UI_BUTTON_CLICK);
        sendMessage("§e导出汇率功能开发中...");
        // TODO: 实现导出汇率功能
    }
    
    /**
     * 设置选择的基础货币
     */
    public void setSelectedFromCurrency(Currency currency) {
        this.selectedFromCurrency = currency;
        this.currentPage = 0; // 重置到第一页
        refresh();
    }
}
