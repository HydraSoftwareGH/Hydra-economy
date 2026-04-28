# HydraEconomy

Plugin de economía para PaperMC 1.21.11 con moneda llamada "Hydras".

## Comandos

| Comando | Descripción |
|---------|-------------|
| `/hydras [jugador]` | Ver saldo de Hydras |
| `/pay <jugador> <cantidad>` | Pagar Hydras a otro jugador |
| `/daily` | Reclamar recompensa diaria |
| `/hydras help` | Mostrar ayuda |

### Administrador (`hydraeconomy.admin`)
| Comando | Descripción |
|---------|-------------|
| `/hydrasadmin give <jugador> <cantidad>` | Dar Hydras |
| `/hydrasadmin remove <jugador> <cantidad>` | Quitar Hydras |
| `/hydrasadmin set <jugador> <cantidad>` | Establecer saldo |
| `/hydrasadmin reload` | Recargar configuración |

### Mercado
| Comando | Descripción |
|---------|-------------|
| `/mercado sell <precio>` | Vender el item en tu mano |
| `/mercado buy <id>` | Comprar un item listado |
| `/mercado list` | Ver items en venta |
| `/mercado cancel <id>` | Cancelar tu publicación |

## Características

- **Economía**: Moneda Hydras, transferencias entre jugadores
- **Recompensa diaria**: Base 10 Hydras, +5 bonus cada 5 días consecutivos
- **Mercado**: Publicar y comprar items entre jugadores
- **Almacenamiento**: Datos guardados en YAML (`balances.yml`, `dailyrewards.yml`, `market.yml`)

## Compilación

```bash
./gradlew build
```

El JAR se genera en `build/libs/`.

## Dependencias

- PaperMC 1.21.11 (API 26.1.2)
- Java 25 (toolchain de compilación)
- Java 21 (ejecución en servidor)
