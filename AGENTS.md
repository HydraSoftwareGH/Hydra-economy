# HydraEconomy - Plugin de PaperMC

## Descripción
Plugin de economía para PaperMC 1.21.5+ con moneda llamada "Hydras".

### Discord Bot (opcional)
- Slash commands: `/hydras`, `/mercado`, `/daily`, `/top`
- Requiere JDA 5.3.0+ en `libs/` del servidor y token en `config.yml`

## Características

### Sistema de Economía
- Moneda: Hydras
- Comandos: `/hydras`, `/pay <jugador> <cantidad>`
- Almacenamiento en YAML (`balances.yml`)

### Recompensa Diaria
- Comando: `/daily`
- Base: 10 Hydras
- Bonus: +5 Hydras cada 5 días consecutivos
- Almacenamiento en YAML (`dailyrewards.yml`)

### Comandos de Administrador
- Permiso: `hydraeconomy.admin`
- `/hydrasadmin give <jugador> <cantidad>` - Dar Hydras
- `/hydrasadmin remove <jugador> <cantidad>` - Quitar Hydras
- `/hydrasadmin set <jugador> <cantidad>` - Establecer saldo
- `/hydrasadmin reload` - Recargar config

### Mercado
- Comando: `/mercado`
- `/mercado sell <precio>` - Vender item en mano
- `/mercado buy <id>` - Comprar item listado
- `/mercado list` - Ver lista del mercado
- `/mercado cancel <id>` - Cancelar publicación

## Compilación
```bash
./gradlew build
```
El JAR estará en `build/libs/`.

## Compilación (GitHub Actions)
- JDK 25 vía actions/setup-java
- Bytecode Java 21 (`-source 21 -target 21`)
- paper-api 26.2.0.build.+

## Dependencias
- PaperMC 1.21.5+ (API 26.2.0)
- Java 25 (para compilar)
- Java 21 (para ejecutar en el servidor, bytecode compatible)
