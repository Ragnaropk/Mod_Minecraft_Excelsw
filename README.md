# ☣ Infección — mod para Minecraft 1.21.1 (Fabric)

Una plaga que se extiende **sin fin** por el mundo infinito de Minecraft. Cuanto más exploras,
más mundo hay para infectar.

## Cómo instalarlo

1. Instala [Fabric Loader](https://fabricmc.net/use/) para **Minecraft 1.21.1**.
2. Descarga [Fabric API](https://modrinth.com/mod/fabric-api) para 1.21.1.
3. Descarga `infeccion-X.jar` desde la pestaña **Releases** del repo (el que no termina en `-sources`).
4. Pon los dos `.jar` en la carpeta `.minecraft/mods` y abre el juego con el perfil de Fabric.

Cada push al repo compila el mod automáticamente (GitHub Actions) y publica una Release nueva con el `.jar`.

## Qué trae

### La plaga
- **Bloques infectados**: tierra, piedra, troncos y hojas. Cada bloque infectado contagia a sus
  vecinos (solo los que tocan aire, para que se vea y no mate el rendimiento). Las flores y la hierba se marchitan.
- **Evoluciona**: cada 7 días de mundo la infección se propaga más rápido (hasta +4).
- **Meteoritos infectados**: cada medio día cae un meteorito cerca de un jugador al azar. Explota,
  deja un cráter infectado y un **Núcleo infeccioso** en el centro. Todo el servidor recibe el aviso.
- **Núcleo infeccioso**: se propaga con mucha fuerza y hace nacer Infectados. Destrúyelo con pico de hierro o mejor.

### El contagio
- Pisar bloques infectados o recibir un golpe de un Infectado te da el efecto **Infección**.
- Si te vuelves a contagiar, la infección **empeora** (hasta nivel IV): hambre, náuseas, debilidad…
- A partir del nivel II **toses esporas** y contagias a quien esté a tu lado.
- **Quien muere infectado se levanta como un Infectado** — animales, aldeanos… y jugadores (con tu nombre).

### El Infectado
Un zombi mutado: no se quema al sol, no se ahoga, persigue también a los animales y deja
un rastro de infección por donde camina.

### Defensa
| Objeto | Receta | Efecto |
|---|---|---|
| **Espora infecciosa** | Suelta de Infectados, núcleos y hojas | Úsala en un bloque o criatura para infectarlo |
| **Vacuna** (x2) | Espora + frasco + zanahoria dorada + rodaja de sandía reluciente (sin forma) | Cura y da **Inmunidad** 5 min |
| **Bomba purificadora** (x2) | Polvo de piedra luminosa ×3 alrededor de una Vacuna, TNT debajo | Limpia la infección en 10 bloques |
| **Purificador** | Hierro en esquinas, piedra luminosa en los lados, Vacuna al centro, diamante abajo | Limpia sin parar la infección en 8 bloques |

## Reglas de juego (`/gamerule`)
| Regla | Por defecto | Qué hace |
|---|---|---|
| `infeccionPropagacion` | `true` | Los bloques infectados se propagan |
| `infeccionVelocidad` | `1` | Intentos de propagación por tick aleatorio (0 = casi quieto) |
| `infeccionMeteoritos` | `true` | Caen meteoritos infectados |
| `infeccionIntervaloMeteoritos` | `12000` | Ticks entre meteoritos (24000 = 1 día) |
| `infeccionResurreccion` | `true` | Los muertos infectados se levantan |

## Comandos (op)
- `/infeccion meteorito` — hace caer un meteorito cerca de ti.
- `/infeccion purificar <radio>` — limpia la infección a tu alrededor.

## Compilar tú mismo
```
./gradlew build
```
El `.jar` queda en `build/libs/`.
