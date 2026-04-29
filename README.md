# HydraEconomy

Plugin de economía para PaperMC 1.21.5+ con moneda llamada "Hydras".

## Comandos

| Comando | Descripción | Aliases |
|---------|-------------|---------|
| `/hydras [jugador]` | Ver tu saldo o el de otro jugador | `/balance`, `/money` |
| `/pay <jugador> <cantidad>` | Pagar Hydras a otro jugador | |
| `/daily` | Reclamar recompensa diaria (10 Hydras base) | |
| `/hydras help` | Mostrar ayuda completa del plugin | |

### Administrador (`hydraeconomy.admin`)

| Comando | Descripción | Aliases |
|---------|-------------|---------|
| `/hydrasadmin give <jugador> <cantidad>` | Dar Hydras a un jugador | `/hadmin`, `/ecoadmin` |
| `/hydrasadmin remove <jugador> <cantidad>` | Quitar Hydras a un jugador | |
| `/hydrasadmin set <jugador> <cantidad>` | Establecer saldo de un jugador | |
| `/hydrasadmin reload` | Recargar configuración y datos | |

### Mercado

| Comando | Descripción | Aliases |
|---------|-------------|---------|
| `/mercado sell <precio>` | Vender el item en tu mano | `/market`, `/shop` |
| `/mercado buy <id>` | Comprar un item listado por ID | |
| `/mercado list` | Ver todos los items en venta | |
| `/mercado cancel <id>` | Cancelar tu publicación y recuperar el item | |
| `/mercado help` | Mostrar ayuda del mercado | `/mercado ayuda` |

## Características

- **Economía**: Moneda Hydras, transferencias entre jugadores, consulta de saldos
- **Recompensa diaria**: Base 10 Hydras, +5 de bonus cada 5 días consecutivos
- **Mercado**: Publicar y comprar items entre jugadores usando Hydras (GUI de cofre virtual)
- **Discord Bot**: Slash commands para consultar saldos, mercado y top desde Discord
- **Permisos**: Comandos de administrador protegidos por `hydraeconomy.admin`
- **Almacenamiento**: Datos guardados en YAML (`balances.yml`, `dailyrewards.yml`, `market.yml`)

### Recompensa Diaria

- Base: **10 Hydras** por reclamo
- Bonus: **+5 Hydras** adicionales cada 5 días consecutivos
- El contador de días se reinicia si fallas un día

### Permisos

| Permiso | Descripción | Default |
|---------|-------------|---------|
| `hydraeconomy.admin` | Acceso a comandos de administrador | op |
| `hydraeconomy.use` | Acceso a comandos básicos de economía | true |

## Compilación

```bash
./gradlew build
```

El JAR se genera en `build/libs/`.

## Dependencias

- PaperMC 1.21.5+ (API 26.2.0)
- Java 25 (para compilar)
- Java 21 (para ejecutar en el servidor)
