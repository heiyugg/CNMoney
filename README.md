# CNMoney - 多货币管理插件

[![JitPack](https://jitpack.io/v/heiyugg/cnmoney.svg)](https://jitpack.io/#heiyugg/cnmoney)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

CNMoney 是一个功能强大的 Minecraft Paper 1.21.3 多货币管理插件，支持自定义货币、汇率管理、GUI界面操作等功能。

## 🌟 主要功能

- 🪙 **多货币支持** - 支持无限种自定义货币
- 💱 **汇率管理** - 灵活的货币兑换系统
- 🖥️ **GUI界面** - 直观的图形化管理界面
- 🔧 **管理面板** - 完整的管理员操作界面
- 💾 **数据库支持** - SQLite/MySQL 双数据库支持
- 🔌 **API支持** - 完整的开发者API
- 🏷️ **占位符支持** - PlaceholderAPI 集成
- 💰 **Vault集成** - 兼容 Vault 经济系统

## 📦 作为依赖使用

### Maven 依赖

在你的 `pom.xml` 中添加 JitPack 仓库：

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

添加 CNMoney 依赖：

```xml
<dependencies>
    <dependency>
        <groupId>com.github.heiyugg</groupId>
        <artifactId>cnmoney</artifactId>
        <version>1.0.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### Gradle 依赖

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.heiyugg:cnmoney:1.0.0'
}
```

### plugin.yml 配置

```yaml
depend: [CNMoney]
# 或使用软依赖
softdepend: [CNMoney]
```

## 🔧 API 使用示例

### 基本用法

```java
// 获取 CNMoney API
CNMoneyAPI api = CNMoney.getAPI();

// 检查玩家余额
BigDecimal balance = api.getBalance(player, "gold_coin");

// 添加余额
api.addBalance(player, "gold_coin", new BigDecimal("100"));

// 扣除余额
boolean success = api.removeBalance(player, "gold_coin", new BigDecimal("50"));

// 设置余额
api.setBalance(player, "gold_coin", new BigDecimal("1000"));
```

### 货币管理

```java
// 获取所有货币
Set<String> currencies = api.getCurrencies();

// 检查货币是否存在
boolean exists = api.hasCurrency("diamond_coin");

// 获取货币显示名
String displayName = api.getCurrencyDisplayName("gold_coin");
```

### 汇率操作

```java
// 获取汇率
BigDecimal rate = api.getExchangeRate("gold_coin", "silver_coin");

// 货币兑换
boolean success = api.exchangeCurrency(player, "gold_coin", "silver_coin", new BigDecimal("10"));
```

## 📋 系统要求

- **服务器**: Paper 1.21.3 或更高版本
- **Java**: JDK 21 或更高版本
- **依赖插件**: 
  - Vault (可选)
  - PlaceholderAPI (可选)

## 🛠️ 编译

```bash
# 克隆项目
git clone https://github.com/heiyugg/cnmoney.git
cd cnmoney

# 编译插件
mvn clean package

# 或使用提供的脚本
编译.bat
```

## 📖 配置文件

### config.yml
```yaml
# 数据库配置
database:
  type: "sqlite"  # sqlite 或 mysql
  
# 日志配置
logging:
  gui-operations: false
  database-operations: false
  debug-info: false
```

### currencies.yml
```yaml
# 货币配置
currencies:
  gold_coin:
    display-name: "金币"
    enabled: true
  silver_coin:
    display-name: "银币"
    enabled: true
```

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

本项目采用 [MIT 许可证](LICENSE)。

## 📞 支持

如有问题，请在 [GitHub Issues](https://github.com/heiyugg/cnmoney/issues) 中提交。
