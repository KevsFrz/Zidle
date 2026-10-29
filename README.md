# Project Z-Idle: Pandemia Global (mod Fabric 1.20.1)

Núcleo del juego incremental zombi dentro de Minecraft.

## Cómo compilarlo
1. Copia esta carpeta sobre una plantilla de Fabric 1.20.1 (para tener el `gradlew`), o ejecuta `gradle wrapper --gradle-version 8.6` aquí.
2. `./gradlew build` -> el jar queda en `build/libs/`.
3. Mételo en `mods/` junto con Fabric API 0.92.2+1.20.1.

## Cómo se juega
- Clic derecho con el **Colmillo Infectado** = infectar a mano (se entrega al entrar por primera vez).
- `/zidle` estado · `/zidle mercado` · `/zidle comprar <n> [cantidad|max]`
- `/zidle mejoras` · `/zidle mejora <id>`
- `/zidle sabotage` (solo con vacuna al 100%) · `/zidle prestigio [confirmar]` · `/zidle adn [comprar <habilidad>]`
- La barra de acción muestra virus, VPS, vacuna e infección.

## Pruebas de lógica (sin Minecraft)
```
java -m jdk.compiler/com.sun.tools.javac.Main -d out $(find src/main/java -path '*config*' -o -name PlayerState.java -o -path '*utils*' -o -path '*managers*' | grep .java) tools/*.java
java -cp out FormulaTest
java -cp out BalanceSim
```
