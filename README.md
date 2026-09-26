# ☣ Infección — mod para Minecraft 1.21.1 (Fabric)

Una plaga que se extiende **sin fin** por el mundo infinito de Minecraft. Cuanto más exploras,
más mundo hay para infectar… y la plaga evoluciona según lo bien (o mal) que la estéis frenando.

## Cómo instalarlo

1. Instala [Fabric Loader](https://fabricmc.net/use/) para **Minecraft 1.21.1**.
2. Descarga [Fabric API](https://modrinth.com/mod/fabric-api) para 1.21.1.
3. Descarga `infeccion-X.jar` desde la pestaña **Releases** del repo.
4. Pon los dos `.jar` en la carpeta `.minecraft/mods` y abre el juego con el perfil de Fabric.

Cada push al repo compila el mod automáticamente (GitHub Actions) y publica una Release nueva con el `.jar`.

## ☣ Amenaza global y fases

Arriba de la pantalla hay una **barra de amenaza global** compartida por todo el servidor.

| Fase | Puntos | Qué cambia |
|---|---|---|
| 1 · **Brote** | 0 | Solo Infectados normales. Nada aparece de forma natural. |
| 2 · **Epidemia** | 1 500 | Aparecen Infectados de noche. Nacen Corredores y Escupidores. Todo se propaga más rápido. |
| 3 · **Pandemia** | 6 000 | Llegan los Hinchados y algún Bruto. Meteoritos más seguidos. |
| 4 · **Apocalipsis** | 15 000 | Brutos por la noche, niebla y cielo oscuro, meteoritos constantes. |

- Cada bloque infectado **suma 1 punto**.
- Cada bloque purificado **resta 2**.
- Cada núcleo destruido **resta 400**.

Si os organizáis, la plaga **retrocede de fase**. Si la dejáis crecer, llega el Apocalipsis.

## La plaga
- **Bloques infectados**: tierra, piedra, troncos y hojas. Se contagian a los bloques vecinos que tocan aire.
  Las flores y la hierba se marchitan, y en la tierra infectada brotan **zarcillos brillantes**.
- **Meteoritos infectados**: caen del cielo **envueltos en llamas** cerca de un jugador. Al impactar abren
  un cráter infectado con un **Núcleo infeccioso** en el centro.
- **Núcleo infeccioso**: late, absorbe esporas, se propaga con fuerza y hace nacer Infectados según la fase.
  No se puede purificar: hay que **romperlo** con un pico de hierro o mejor. Al romperlo salen sus defensores
  y suelta un **Corazón infeccioso**.
- **El fuego quema la infección**: los troncos, las hojas y los zarcillos infectados arden, y los Infectados
  reciben el doble de daño por fuego.

## El contagio
- Pisar bloques infectados, rozar zarcillos o las esporas en el aire te infectan.
- Cada mordisco o espora directa puede **empeorar** la infección (hasta el nivel IV).
- Síntomas por nivel:
  - **II**: hambre, y toses esporas a quien tengas cerca.
  - **III**: náuseas y debilidad.
  - **IV**: alucinaciones. Oyes Infectados que no existen y la oscuridad te envuelve.
- En pantalla: **viñeta morada que late** más rápido cuanto peor estás, y **latidos del corazón**.
- **Quien muere infectado se levanta como Infectado**: animales, aldeanos… y jugadores, con su nombre.
  Las criaturas grandes (vacas, gólems) vuelven como **Brutos**.

## Los Infectados (todos con ojos que brillan en la oscuridad)
| Criatura | Qué hace |
|---|---|
| **Infectado** | Zombi mutado. No se quema al sol, persigue animales y deja un rastro de infección. |
| **Corredor** | Rápido y frágil. |
| **Bruto** | Enorme, 70 de vida, te lanza por los aires. Puede soltar un Corazón infeccioso. |
| **Escupidor** | Te ataca desde lejos con bolas de esporas que infectan criaturas y bloques. |
| **Hinchado** | Se acerca, se infla parpadeando… y revienta en una nube de esporas. También al morir. |

## Equipo
| Objeto | Receta | Qué hace |
|---|---|---|
| **Espora infecciosa** | La sueltan los Infectados, los núcleos, las hojas y los zarcillos | Úsala en un bloque o criatura para infectarlo, o **lánzala** |
| **Escáner de infección** | Hierro, redstone, paneles de vidrio, espora y brújula | Muestra la fase, la contaminación de la zona y la **dirección al núcleo más cercano** |
| **Máscara de gas** | Cuero, cuerda, paneles de vidrio y carbón | Filtra las esporas del aire (suelo, toses, nubes). Se desgasta con el uso |
| **Lanzallamas** | Hierro, vara de blaze y mechero | Mantén el clic derecho: quema la infección, prende a las criaturas y hace el doble de daño a los Infectados. Se repara con polvo de blaze |
| **Vacuna** (x2) | Espora, frasco, zanahoria dorada y rodaja de sandía reluciente | Cura y da **Inmunidad** durante 5 minutos |
| **Bomba purificadora** (x2) | Polvo de piedra luminosa ×3, Vacuna y TNT | Limpia la infección en 10 bloques |
| **Purificador** | Hierro, piedra luminosa, Vacuna y **Corazón infeccioso** | Limpia sin parar la infección en 8 bloques |

## Reglas de juego (`/gamerule`)
| Regla | Por defecto | Qué hace |
|---|---|---|
| `infeccionPropagacion` | `true` | Los bloques infectados se propagan |
| `infeccionVelocidad` | `1` | Intentos de propagación por tick aleatorio (0 = casi quieto) |
| `infeccionMeteoritos` | `true` | Caen meteoritos infectados |
| `infeccionIntervaloMeteoritos` | `12000` | Ticks entre meteoritos en la fase 1 (24000 = 1 día) |
| `infeccionResurreccion` | `true` | Los muertos infectados se levantan |
| `infeccionBarra` | `true` | Muestra la barra de amenaza global |

## Comandos (op)
- `/infeccion estado`: fase, puntos y núcleos activos.
- `/infeccion puntos <n>`: fija los puntos de amenaza (para probar las fases).
- `/infeccion meteorito`: hace caer un meteorito cerca de ti.
- `/infeccion purificar <radio>`: limpia la infección a tu alrededor.

## Compilar tú mismo
```
./gradlew build
```
El `.jar` queda en `build/libs/`.
