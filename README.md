# Copper Fast Oxidation

Right-click copper with an **Amethyst Shard** to make it weather one stage. Right-click with an **axe** to scrape one stage back (with copper-colored dust).
Doors, trapdoors, copper chests and copper golem statues need sneaking, so they still open, close or change pose normally.

- CurseForge: <https://www.curseforge.com/minecraft/mc-mods/copper-fast-oxidation>

## Versions

| Loader   | Minecraft                                             | Needs      |
|----------|-------------------------------------------------------|------------|
| Forge    | 1.20.1                                                |            |
| Fabric   | 1.20.1, 1.21.1 – 1.21.11, 26.1 – 26.3                 | Fabric API |
| NeoForge | 1.21.1 – 1.21.11, 26.1 – 26.3                         |            |

## Layout

- `forge/<mc>/`, `fabric/<mc>/`, `neoforge/<mc>/`: one small Gradle project per Minecraft version
- `fabric/shared/`, `neoforge/shared/`: the code, split by the Minecraft version range it compiles against, plus the mod metadata templates
- `VERSION`: the mod version for every build

## Build

```sh
cd fabric
./gradlew -p 1.21.11 build
```

The jar is written to `fabric/1.21.11/build/libs/`. GitHub Actions builds every version on each push and attaches them to releases.

---

# Copper Fast Oxidation（日本語）

アメジストの欠片を持って銅ブロックを右クリックすると、錆が1段階進みます。斧で右クリックすると1段階戻ります（銅色の粉が出ます）。
ドア・トラップドア・銅のチェスト・銅のゴーレムの像はスニーク中だけ反応するので、普段どおり開け閉めやポーズ変更ができます。

Fabric版には Fabric API が必要です。

## License

MIT © Mya
