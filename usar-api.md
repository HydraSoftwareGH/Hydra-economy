# Usar la API de HydraEconomy

## Obtener la instancia del plugin

```java
import com.hydrasoftware.hydraeconomy.HydraEconomy;
import org.bukkit.plugin.java.JavaPlugin;

public class MiPlugin extends JavaPlugin {

    private HydraEconomy getHydraEconomy() {
        return (HydraEconomy) getServer().getPluginManager().getPlugin("HydraEconomy");
    }
}
```

Asegurate de poner `HydraEconomy` como dependencia en `plugin.yml`:

```yaml
depend: [HydraEconomy]
```

---

## EconomyManager — Saldos de Hydras

```java
HydraEconomy hydra = getHydraEconomy();
EconomyManager eco = hydra.getEconomyManager();
```

| Método | Descripción |
|--------|-------------|
| `getBalance(UUID uuid)` | Devuelve el saldo de un jugador (`BigDecimal`) |
| `setBalance(UUID uuid, BigDecimal amount)` | Establece el saldo |
| `hasBalance(UUID uuid, BigDecimal amount)` | `true` si el jugador tiene >= esa cantidad |
| `withdraw(UUID uuid, BigDecimal amount)` | Descontar saldo. Devuelve `false` si no tiene suficiente |
| `deposit(UUID uuid, BigDecimal amount)` | Añadir saldo |
| `transfer(UUID from, UUID to, BigDecimal amount)` | Transferir entre jugadores. Devuelve `false` si no tiene fondos |

### Ejemplo

```java
UUID jugador = player.getUniqueId();

// Ver saldo
BigDecimal saldo = eco.getBalance(jugador);

// Dar dinero
eco.deposit(jugador, BigDecimal.valueOf(50));

// Quitar dinero (validar antes)
if (eco.hasBalance(jugador, BigDecimal.valueOf(30))) {
    eco.withdraw(jugador, BigDecimal.valueOf(30));
}

// Transferir entre jugadores
eco.transfer(origen, destino, BigDecimal.valueOf(100));
```

---

## DailyRewardManager — Recompensa diaria

```java
DailyRewardManager daily = hydra.getDailyRewardManager();
```

| Método | Descripción |
|--------|-------------|
| `claim(UUID uuid)` | Reclamar la recompensa. Devuelve `ClaimResult` |
| `getRewardData(UUID uuid)` | Obtener datos de racha (`RewardData`) |

### ClaimResult

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `success` | `boolean` | `true` si pudo reclamar |
| `amount` | `BigDecimal` | Cantidad recibida |
| `message` | `String` | Mensaje descriptivo |

### RewardData

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `lastClaim` | `String` | Fecha del último reclamo (`YYYY-MM-DD`) o `null` |
| `streak` | `int` | Días consecutivos reclamados |

### Ejemplo

```java
DailyRewardManager.ClaimResult result = daily.claim(jugador);
if (result.success) {
    // Recibio result.amount Hydras
} else {
    // Ya reclamo hoy: result.message
}
```

---

## MarketManager — Mercado

```java
MarketManager market = hydra.getMarketManager();
```

| Método | Descripción |
|--------|-------------|
| `createListing(UUID seller, BigDecimal price, ItemStack item)` | Publicar un item. Devuelve el ID |
| `buyListing(int id, UUID buyer)` | Comprar un item. Devuelve `false` si no se pudo |
| `cancelListing(int id, UUID seller)` | Cancelar publicación. Devuelve `false` si no es suya |
| `getListings()` | Lista completa de `MarketListing` ordenada por ID |
| `getListing(int id)` | Obtener una publicación específica o `null` |

### MarketListing

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | `int` | ID único de la publicación |
| `seller` | `UUID` | UUID del vendedor |
| `price` | `BigDecimal` | Precio en Hydras |
| `item` | `ItemStack` | Item a la venta |

### Ejemplo

```java
// Ver items en venta
for (MarketManager.MarketListing listing : market.getListings()) {
    player.sendMessage("ID " + listing.id + " - " + listing.item.getType() + " - " + listing.price + " Hydras");
}

// Comprar
if (market.buyListing(id, jugador)) {
    player.getInventory().addItem(listing.item);
}

// Publicar un item
ItemStack item = player.getInventory().getItemInMainHand();
int id = market.createListing(jugador, BigDecimal.valueOf(50), item);

// Cancelar una publicacion
market.cancelListing(id, jugador);
```

---

## Abrir la GUI del Mercado

```java
import com.hydrasoftware.hydraeconomy.gui.MarketGUI;

MarketGUI.open(player, hydra);
```

La GUI es un cofre virtual de 27 slots con paginacion. Cada item muestra su precio. Click para comprar.

---

## plugin.yml

```yaml
name: MiPlugin
version: 1.0
main: com.miplugin.MiPlugin
api-version: '1.21'
depend: [HydraEconomy]
```

## build.gradle.kts

```kotlin
repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.0.build.+")
    compileOnly(files("ruta/a/HydraEconomy.jar")) // o ponerlo en libs/
}
```

Si HydraEconomy esta instalado en el servidor, podes usar `depend` sin incluir el JAR. Si queres compilar contra la API sin tener el plugin en el classpath, necesitas el JAR.
