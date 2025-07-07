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
 * 兑换选择GUI - 选择要兑换的货币
 * 
 * @author CNMoney Team
 */
public class ExchangeSelectGUI extends BaseGUI {
    
    private int currentPage = 0;
    private final int itemsPerPage = 21; // 7x3的区域
    
    public ExchangeSelectGUI(CNMoney plugin, Player player) {
        super(plugin, player, plugin.getConfigManager().getMessage("gui.exchange-title"), 54);
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
            
            // 显示货币选择
            int slot = 10; // 从第二行第二列开始
            for (int i = startIndex; i < endIndex; i++) {
                Currency currency = currencies.get(i);
                ItemStack currencyItem = createExchangeSelectItem(currency);
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
                                              "&7选择要兑换的货币");
                inventory.setItem(49, pageInfo);
            }
        }
        
        // 说明信息
        ItemStack infoItem = createItem(Material.BOOK, 
                                      "&6&l兑换说明", 
                                      "&7选择你要兑换的源货币",
                                      "&7然后选择目标货币进行兑换",
                                      "",
                                      "&e手续费: &f" + (plugin.getConfigManager().getExchangeFeePercentage() * 100) + "%",
                                      "&e最小金额: &f" + plugin.getConfigManager().getExchangeMinAmount());
        inventory.setItem(4, infoItem);
        
        // 返回按钮
        addBackButton(48);
        
        // 关闭按钮
        addCloseButton(50);
    }
    
    /**
     * 创建兑换选择物品
     */
    private ItemStack createExchangeSelectItem(Currency currency) {
        BigDecimal balance = plugin.getCurrencyManager().getBalance(player.getUniqueId(), currency.getId());
        
        List<String> lore = new ArrayList<>();
        lore.add("&7货币名称: &f" + currency.getName());
        lore.add("&7货币符号: &f" + currency.getSymbol());
        lore.add("&7当前余额: " + currency.formatAmountWithColor(balance));
        lore.add("");
        
        // 检查是否有足够余额进行兑换
        double minAmount = plugin.getConfigManager().getExchangeMinAmount();
        boolean canExchange = balance.doubleValue() >= minAmount;
        
        if (canExchange) {
            lore.add("&a可以兑换");
            lore.add("&e点击选择此货币作为兑换源");
            lore.add("");
            lore.add("&7将显示可兑换的目标货币");
        } else {
            lore.add("&c余额不足");
            lore.add("&7最小兑换金额: &e" + minAmount);
            lore.add("&7需要至少: &e" + currency.formatAmount(BigDecimal.valueOf(minAmount)));
        }
        
        // 根据是否可兑换选择材料
        Material material = canExchange ? getMaterialForCurrency(currency) : Material.GRAY_DYE;
        String displayName = currency.getColor() + currency.getName();
        
        if (!canExchange) {
            displayName += " &7(余额不足)";
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
        }
        
        // 处理货币选择
        Currency selectedCurrency = getCurrencyFromSlot(slot);
        if (selectedCurrency != null) {
            handleCurrencySelect(selectedCurrency);
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
     * 处理货币选择
     */
    private void handleCurrencySelect(Currency currency) {
        BigDecimal balance = plugin.getCurrencyManager().getBalance(player.getUniqueId(), currency.getId());
        double minAmount = plugin.getConfigManager().getExchangeMinAmount();
        
        // 检查余额是否足够
        if (balance.doubleValue() < minAmount) {
            sendMessage("&c余额不足！最小兑换金额为 " + currency.formatAmount(BigDecimal.valueOf(minAmount)));
            playSound(Sound.ENTITY_VILLAGER_NO);
            return;
        }
        
        // 打开目标货币选择GUI
        openTargetCurrencyGUI(currency);
        playSound(Sound.UI_BUTTON_CLICK);
    }
    
    /**
     * 打开目标货币选择GUI
     */
    private void openTargetCurrencyGUI(Currency fromCurrency) {
        // 创建目标货币选择GUI
        ExchangeTargetGUI targetGUI = new ExchangeTargetGUI(plugin, player, fromCurrency);
        plugin.getGUIManager().openGUI(player, targetGUI);
        sendMessage("&a已选择源货币: " + fromCurrency.getName());
    }
}
