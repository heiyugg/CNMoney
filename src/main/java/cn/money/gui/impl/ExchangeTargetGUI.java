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
 * 兑换目标选择GUI - 选择兑换的目标货币
 * 
 * @author CNMoney Team
 */
public class ExchangeTargetGUI extends BaseGUI {
    
    private final Currency fromCurrency;
    private int currentPage = 0;
    private final int itemsPerPage = 21; // 7x3的区域
    
    public ExchangeTargetGUI(CNMoney plugin, Player player, Currency fromCurrency) {
        super(plugin, player, "&6&l选择目标货币", 54);
        this.fromCurrency = fromCurrency;
    }
    
    @Override
    protected void initializeItems() {
        // 清空界面
        inventory.clear();
        
        // 设置玻璃边框
        setGlassBorder();
        
        // 显示源货币信息
        ItemStack fromCurrencyItem = createFromCurrencyInfo();
        inventory.setItem(4, fromCurrencyItem);
        
        // 获取所有可兑换的目标货币（排除源货币）
        List<Currency> targetCurrencies = new ArrayList<>();
        for (Currency currency : plugin.getCurrencyManager().getEnabledCurrencies()) {
            if (!currency.getId().equals(fromCurrency.getId())) {
                targetCurrencies.add(currency);
            }
        }
        
        if (targetCurrencies.isEmpty()) {
            // 没有可兑换的目标货币
            ItemStack noDataItem = createItem(Material.BARRIER, 
                                            "&c没有可兑换的目标货币", 
                                            "&7请联系管理员添加更多货币配置");
            inventory.setItem(22, noDataItem);
        } else {
            // 计算分页
            int totalPages = (int) Math.ceil((double) targetCurrencies.size() / itemsPerPage);
            int startIndex = currentPage * itemsPerPage;
            int endIndex = Math.min(startIndex + itemsPerPage, targetCurrencies.size());
            
            // 显示目标货币选择
            int slot = 10; // 从第二行第二列开始
            for (int i = startIndex; i < endIndex; i++) {
                Currency currency = targetCurrencies.get(i);
                ItemStack currencyItem = createTargetCurrencyItem(currency);
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
                                              "&7选择兑换目标货币");
                inventory.setItem(49, pageInfo);
            }
        }
        
        // 返回按钮
        addBackButton(48);
        
        // 关闭按钮
        addCloseButton(50);
    }
    
    /**
     * 创建源货币信息显示
     */
    private ItemStack createFromCurrencyInfo() {
        BigDecimal balance = plugin.getCurrencyManager().getBalance(player.getUniqueId(), fromCurrency.getId());
        
        List<String> lore = new ArrayList<>();
        lore.add("&7要兑换的货币");
        lore.add("");
        lore.add("&7货币名称: &f" + fromCurrency.getName());
        lore.add("&7货币符号: &f" + fromCurrency.getSymbol());
        lore.add("&7当前余额: " + fromCurrency.formatAmountWithColor(balance));
        lore.add("");
        lore.add("&e请选择要兑换成的目标货币");
        
        Material material = getMaterialForCurrency(fromCurrency);
        String displayName = "&a&l源货币: " + fromCurrency.getColor() + fromCurrency.getName();
        
        return createItem(material, displayName, lore);
    }
    
    /**
     * 创建目标货币选择物品
     */
    private ItemStack createTargetCurrencyItem(Currency currency) {
        List<String> lore = new ArrayList<>();
        lore.add("&7目标货币信息");
        lore.add("");
        lore.add("&7货币名称: &f" + currency.getName());
        lore.add("&7货币符号: &f" + currency.getSymbol());
        
        // 计算兑换率
        BigDecimal exchangeRate = calculateExchangeRate(fromCurrency, currency);
        lore.add("&7兑换汇率: &e1 " + fromCurrency.getSymbol() + " = " + 
                currency.formatAmount(exchangeRate) + " " + currency.getSymbol());
        
        // 计算手续费
        double feePercentage = plugin.getConfigManager().getExchangeFeePercentage() * 100;
        lore.add("&7手续费: &e" + feePercentage + "%");
        
        lore.add("");
        lore.add("&e点击选择此货币作为兑换目标");
        lore.add("&7将打开兑换金额输入界面");
        
        Material material = getMaterialForCurrency(currency);
        String displayName = currency.getColor() + currency.getName();
        
        return createItem(material, displayName, lore);
    }
    
    /**
     * 计算兑换汇率
     */
    private BigDecimal calculateExchangeRate(Currency from, Currency to) {
        // 直接从源货币获取到目标货币的汇率
        BigDecimal rate = from.getExchangeRate(to.getId());
        if (rate != null) {
            return rate;
        }

        // 如果没有直接汇率，尝试反向计算
        BigDecimal reverseRate = to.getExchangeRate(from.getId());
        if (reverseRate != null && reverseRate.compareTo(BigDecimal.ZERO) > 0) {
            return BigDecimal.ONE.divide(reverseRate, to.getDecimals(), BigDecimal.ROUND_HALF_UP);
        }

        // 默认返回1:1汇率
        return BigDecimal.ONE;
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
                List<Currency> targetCurrencies = new ArrayList<>();
                for (Currency currency : plugin.getCurrencyManager().getEnabledCurrencies()) {
                    if (!currency.getId().equals(fromCurrency.getId())) {
                        targetCurrencies.add(currency);
                    }
                }
                int totalPages = (int) Math.ceil((double) targetCurrencies.size() / itemsPerPage);
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    refresh();
                    playSound(Sound.UI_BUTTON_CLICK);
                }
                return;
                
            case 48: // 返回
                plugin.getGUIManager().openExchangeSelectGUI(player);
                playSound(Sound.UI_BUTTON_CLICK);
                return;
                
            case 50: // 关闭
                player.closeInventory();
                playSound(Sound.UI_BUTTON_CLICK);
                return;
        }
        
        // 处理目标货币选择
        Currency targetCurrency = getTargetCurrencyFromSlot(slot);
        if (targetCurrency != null) {
            handleTargetCurrencySelect(targetCurrency);
        }
    }
    
    /**
     * 从槽位获取对应的目标货币
     */
    private Currency getTargetCurrencyFromSlot(int slot) {
        List<Currency> targetCurrencies = new ArrayList<>();
        for (Currency currency : plugin.getCurrencyManager().getEnabledCurrencies()) {
            if (!currency.getId().equals(fromCurrency.getId())) {
                targetCurrencies.add(currency);
            }
        }
        
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
        if (currencyIndex >= targetCurrencies.size()) return null;
        
        return targetCurrencies.get(currencyIndex);
    }
    
    /**
     * 处理目标货币选择
     */
    private void handleTargetCurrencySelect(Currency targetCurrency) {
        // 打开兑换金额输入GUI
        plugin.getGUIManager().openExchangeGUI(player, fromCurrency.getId(), targetCurrency.getId());
        playSound(Sound.UI_BUTTON_CLICK);
        
        sendMessage("&a已选择兑换: " + fromCurrency.getName() + " → " + targetCurrency.getName());
    }
}
