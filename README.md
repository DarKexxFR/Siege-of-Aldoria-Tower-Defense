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

| Touche / Action | Effet |
|---|---|
| `ESPACE` | Démarrer la vague / Pause / Reprendre |
| `1` | Sélectionner Archer Tower (60 or) |
| `2` | Sélectionner Druide Tower (90 or) |
| `3` | Sélectionner Mage Tower (100 or) |
| `4` | Sélectionner Cannon Tower (130 or) |
| `5` | Sélectionner Caserne (110 or) |
| `Clic gauche` (carte) | Poser la tour sélectionnée |
| `Clic gauche` (tour) | Sélectionner une tour posée |
| `U` | Améliorer la tour sélectionnée |
| `S` | Vendre la tour sélectionnée |
| `A` | Acheter la capacité spéciale de la tour sélectionnée |
| `X` | Changer la vitesse de jeu (x1 → x2 → x3) |
| `ESC` | Annuler le placement / Désélectionner |
| `R` | Recommencer (Game Over / Victoire) |

## 🗼 Tours

| Tour | Coût | Portée | Dégâts | Cadence | Capacité spéciale (prix) |
|---|---|---|---|---|---|
| **Archer** | 60 | 192 px | 20 | 1.5/s | **Rafale** (40 or) — 5 flèches sur 5 cibles, toutes les 12s |
| **Druide** | 90 | 150 px | 10 | 0.7/s | **Enracinement** (50 or) — immobilise tous les ennemis en portée 2.5s, toutes les 16s |
| **Mage** | 100 | 160 px | 30 | 0.9/s | **Nova** (60 or) — ralentit TOUS les ennemis à 30% pendant 3s, toutes les 18s |
| **Cannon** | 130 | 176 px | 70 | 0.5/s | **Barrage** (70 or) — 3 boulets AoE 2× dégâts, toutes les 15s |
| **Caserne** | 110 | — | — | — | **Fureur** (80 or) — soldats renforcés (2× HP, 1.5× dégâts) |

### Améliorations
Chaque tour peut être améliorée **jusqu'au niveau 3** (+40 % dégâts, +10 % portée, +15 % cadence par niveau).  
Les capacités spéciales s'achètent séparément avec `[A]` et s'activent automatiquement une fois achetées.

### Barre de recharge
Une barre colorée sous chaque tour de niveau 3 indique le temps restant avant la prochaine activation de la capacité. Un anneau d'expansion s'affiche à chaque déclenchement.

## ⚔️ Caserne

La Caserne invoque une escouade permanente de soldats mêlée autour d'elle :
- **Niveau 1** : 3 soldats | **Niveau 2** : 4 soldats | **Niveau 3** : 5 soldats
- Les soldats attaquent automatiquement les ennemis en portée (55 px)
- **Les ennemis s'arrêtent** dès qu'ils entrent au contact d'un soldat et se battent jusqu'à ce que le soldat meure
- Un soldat tué repop après **15 secondes**
- **Fureur** (80 or) : remplace immédiatement toute l'escouade par des soldats améliorés (HP orange)

## 👹 Ennemis

| Ennemi | HP | Vitesse | Récompense |
|---|---|---|---|
| **Slim** | 30 | Rapide | 4 or, 5 pts |
| **Goblin** | 60 | Rapide | 8 or, 10 pts |
| **Orc** | 180 | Moyen | 15 or, 20 pts |
| **Troll** | 450 | Lent | 30 or, 50 pts |
| **Dragon** | 900 | Moyen | 80 or, 150 pts |

Les ennemis gagnent +18 % de HP et de vitesse à chaque vague.

## 🌊 Vagues

10 vagues au total. La composition monte en puissance : Slims et Goblins en début de partie, Trolls et Dragons à partir de la vague 7.

## ⚡ Vitesse de jeu

Appuyez sur `[X]` pendant une vague pour cycler entre les vitesses :
- **x1** — vitesse normale (gris)
- **x2** — vitesse rapide (jaune)
- **x3** — vitesse très rapide (rouge)

## 🗺️ Niveaux

| Niveau | Nom | Difficulté | Chemin |
|---|---|---|---|
| 1 | Forêt d'Aldoria | ★☆☆ Facile | Chemin en S |
| 2 | Désert Brûlant | ★★☆ Moyen | Serpentin vertical |
| 3 | Forteresse | ★★★ Difficile | Zigzag complexe |

## 🔧 Prérequis

- Java 17+ (testé sur Java 26)
- Aucune dépendance externe — Java Swing pur
