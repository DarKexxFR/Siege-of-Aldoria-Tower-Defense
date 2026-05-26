# 🏰 Siege of Aldoria

A Java Swing Tower Defense game set in the medieval fantasy world of Aldoria.

## 🎮 How to Play

### Launch
```bash
java -jar SiegeOfAldoria.jar
# or on Windows
run.bat
```

### Build from source
```bash
bash build.sh
```

### Controls

| Key / Action | Effect |
|---|---|
| `SPACE` | Start next wave / Pause / Resume |
| `1` | Select Archer Tower (60 gold) |
| `2` | Select Mage Tower (100 gold) |
| `3` | Select Cannon Tower (130 gold) |
| `Left Click` (map) | Place selected tower |
| `Left Click` (tower) | Select placed tower |
| `S` | Sell selected tower |
| `U` | Upgrade selected tower |
| `ESC` | Cancel placement / Deselect |
| `R` | Restart (Game Over / Victory) |

## 🗼 Towers

| Tower | Cost | Range | Damage | Rate | Special |
|---|---|---|---|---|---|
| **Archer** | 60 | 192px | 20 | 1.5/s | Fast, single target |
| **Mage** | 100 | 160px | 30 | 0.9/s | Slows enemies 50% for 2s |
| **Cannon** | 130 | 176px | 70 | 0.5/s | AoE splash (64px radius) |

All towers can be upgraded **up to level 3** (+40% damage, +10% range, +15% fire rate per level).

## 👹 Enemies

| Enemy | HP | Speed | Reward |
|---|---|---|---|
| **Goblin** | 60 | Fast | 8 gold, 10 pts |
| **Orc** | 180 | Medium | 15 gold, 20 pts |
| **Troll** | 450 | Slow | 30 gold, 50 pts |
| **Dragon** | 900 | Medium | 80 gold, 150 pts |

All enemies scale in HP and speed each wave (+18% per wave).

## 🌊 Waves

10 waves total. The path winds left → down → right → up → right → down → exit.  
Wave composition escalates: Goblins early, Dragons on waves 7-10.

## 🔧 Requirements

- Java 17+ (tested on Java 26)
- No external libraries — pure Java Swing
