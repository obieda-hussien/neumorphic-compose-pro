# Roadmap — features after the 5.0.0 performance engine

These product features are intentionally deferred until the performance engine
changes in 5.0.0 are merged and measured. Ship performance first; then expand
the component surface.

## Status

| Item | Status |
| --- | --- |
| Performance class / thermal hysteresis / battery awareness | In 5.0.0 |
| Exact cache identity under policy changes | In 5.0.0 |
| Modifier.Node + async shadow generation | Planned |
| GPU RenderEffect backend (API 31+) with StackBlur fallback | Planned |
| ALPHA_8 geometry masks with alpha-channel blur | Planned |
| Two-level hot/protected cache or TinyLFU admission | Planned |
| Frame-overrun aware quality controller | Planned |
| `NeuTextField`, `NeuBottomSheet`, `NeuDialog`, `NeuAppBar` | Planned |
| `NeuTheme.shadowsFrom(background)` luminance helpers | Planned |
| Rich demo `LazyColumn` stress screen | Planned |
| Maven Central publishing | Planned |

## Phase A — renderer architecture (next)

1. Migrate `DrawModifier` to `Modifier.Node` / `DrawModifierNode`.
2. Generate shadows off the critical draw path with request-version checks.
3. Atomically swap completed bitmaps into the node and call `invalidateDraw()`.
4. Optional cheap placeholder shadow until the first blur completes.

## Phase B — backends and memory

1. API 31+ `RenderEffect` / `RenderNode` GPU blur backend.
2. Keep StackBlur as the portable CPU fallback.
3. ALPHA_8 masks once blur correctly softens alpha (not only RGB).
4. Hot + probation cache tiers so one-off sizes do not evict list-item shadows.

## Phase C — components and theme

1. Text field, sheets, dialogs, app bars with shared interaction semantics.
2. Automatic light/dark shadow colors derived from background luminance.
3. Public `NeuPerfStats` counters for hits/misses/blurMs/thermal tier.

## Phase D — distribution and docs

1. CHANGELOG discipline per release.
2. Maven Central in addition to JitPack.
3. Device lab notes for API 24 / 29 / 31 / 34 and 60/90/120 Hz panels.

## Rule

Do not land large feature UI on top of an unmeasured renderer change.
Performance engine → benchmarks → features.
