# Changelog

## 5.0.0 — unreleased

- Move built-in Compose shadow generation off the drawing path with bounded, shared requests.
- Correct worker corner geometry, byte-budget protected caching and GPU target dimensions.
- Capture rendering policy once per request and reuse it for cache keys and actual sampling.
- Add scoped design tokens, arbitrary Compose shapes and surface-based components.
- Add accessible rotary control, range selection, sheets/dialogs and navigation controls.
- Extend text input with label, errors, support text and maximum length.
- Add interactive Kotlin-export Playground, purple preset, stress screen and diagnostics.
- Expand device CI and provide benchmark/API comparison tools and Central bundle staging.

Migration: existing basic modifier call sites remain source-compatible. Recompile consumers
and custom NeuShape implementations; 5.0 does not promise binary compatibility with 4.0.1.
Custom shapes should report geometry through resolveCorners and respect cache-only drawing.

## 4.0.1

Shared renderer, memory-pressure handling, interaction semantics and regression coverage.
