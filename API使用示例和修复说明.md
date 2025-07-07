# CNMoney API 使用问题修复说明

## 🔍 问题分析

您在使用CNMoney作为依赖时遇到API找不到的问题，主要原因是：

1. **缺少静态API访问方法** - CNMoney类中没有静态的`getAPI()`方法
2. **API文档不完整** - 缺少详细的使用示例

## 🔧 解决方案

### 1. 修复CNMoney主类

需要在CNMoney.java中添加静态API访问方法：

```java
/**
 * 获取CNMoney API实例（静态方法）
 * 供其他插件调用
 * 
 * @return CNMoneyAPI实例，如果插件未启用则返回null
 */
public static CNMoneyAPI getAPI() {
    if (instance == null) {
        return null;
    }
    return instance.getApiInstance();
}
```

### 2. 正确的API使用方式

#### Maven依赖配置
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.heiyugg</groupId>
        <artifactId>CNMoney</artifactId>
        <version>v1.0.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

#### plugin.yml配置
```yaml
depend: [CNMoney]
# 或使用软依赖
softdepend: [CNMoney]
```

#### Java代码示例
```java
import cn.money.CNMoney;
import cn.money.api.CNMoneyAPI;
import org.bukkit.entity.Player;
import java.math.BigDecimal;

public class YourPlugin extends JavaPlugin {
    
    private CNMoneyAPI cnMoneyAPI;
    
    @Override
    public void onEnable() {
        // 检查CNMoney是否可用
        if (getServer().getPluginManager().getPlugin("CNMoney") == null) {
            getLogger().warning("CNMoney插件未找到！");
            return;
        }
        
        // 获取API实例
        cnMoneyAPI = CNMoney.getAPI();
        if (cnMoneyAPI == null) {
            getLogger().warning("无法获取CNMoney API！");
            return;
        }
        
        getLogger().info("CNMoney API已成功加载！");
    }
    
    // 使用API的示例方法
    public void exampleUsage(Player player) {
        // 检查余额
        BigDecimal balance = cnMoneyAPI.getBalance(player, "gold_coin");
        player.sendMessage("您的金币余额: " + balance);
        
        // 添加余额
        boolean success = cnMoneyAPI.addBalance(player, "gold_coin", new BigDecimal("100"));
        if (success) {
            player.sendMessage("已添加100金币！");
        }
        
        // 扣除余额
        boolean removed = cnMoneyAPI.removeBalance(player, "gold_coin", new BigDecimal("50"));
        if (removed) {
            player.sendMessage("已扣除50金币！");
        }
        
        // 货币兑换
        boolean exchanged = cnMoneyAPI.exchange(player, "gold_coin", "silver_coin", new BigDecimal("10"));
        if (exchanged) {
            player.sendMessage("成功兑换10金币为银币！");
        }
    }
}
```

## 📋 完整API方法列表

### 余额操作
- `getBalance(Player player, String currencyId)` - 获取玩家指定货币余额
- `setBalance(Player player, String currencyId, BigDecimal amount)` - 设置玩家余额
- `addBalance(Player player, String currencyId, BigDecimal amount)` - 添加余额
- `removeBalance(Player player, String currencyId, BigDecimal amount)` - 扣除余额

### 货币管理
- `getCurrency(String currencyId)` - 获取货币对象
- `getAllCurrencies()` - 获取所有货币
- `getEnabledCurrencies()` - 获取启用的货币
- `getDefaultCurrency()` - 获取默认货币

### 转账和兑换
- `transfer(Player fromPlayer, Player toPlayer, String currencyId, BigDecimal amount)` - 转账
- `exchange(Player player, String fromCurrencyId, String toCurrencyId, BigDecimal amount)` - 货币兑换

### 工具方法
- `formatAmount(String currencyId, BigDecimal amount)` - 格式化金额显示
- `formatAmountWithColor(String currencyId, BigDecimal amount)` - 格式化金额显示（带颜色）

## 🚀 下一步操作

1. **修复主类** - 添加静态API访问方法
2. **重新编译** - 生成新的JAR文件
3. **更新发布** - 推送到GitHub并触发JitPack构建
4. **测试依赖** - 在其他项目中测试API调用

## 💡 使用建议

1. **空值检查** - 始终检查API实例是否为null
2. **插件依赖** - 确保在plugin.yml中声明依赖
3. **异常处理** - 包装API调用以处理可能的异常
4. **版本兼容** - 使用特定版本标签而不是SNAPSHOT
