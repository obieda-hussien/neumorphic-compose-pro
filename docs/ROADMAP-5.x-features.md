# 5.x feature status

## Implemented on the 5.0.0 branch

- Performance class, thermal hysteresis and battery-aware work settings.
- Cache-only Compose drawing, shared bounded workers, immutable settings snapshots.
- Actual shape geometry in worker requests; custom Compose Shape adapter.
- One byte budget for protected/probation cache entries; exact GPU dimensions.
- NeuDesignTheme, NeuDesignTokens and NeuSurface; scoped colors, depth and light.
- Contrast foreground selection and optional shape-aware borders, snap transitions and static indeterminate indicators in reduced-motion mode.
- NeuSegmentedButton, NeuTabs, NeuNavigationBar, NeuDialog, NeuBottomSheet,
  NeuRangeSlider and accessible NeuRotaryKnob.
- Existing NeuTextField extended with error/supporting text/label/length limit.
- Gallery/Playground/Stress demo; purple preset; copy complete Kotlin snippets.
- Runtime cache/worker telemetry and demo frame-duration percentiles.
- Multi-API tests, explicit stress benchmark, benchmark comparison and JVM API tools.
- Signed local Central bundle workflow, generated API docs and current Portal instructions.

## Requires release validation or credentials

- Physical low/mid/high-end comparisons, thermal soak, battery measurements and screenshot review.
- Central namespace verification and signing credentials; no claim of publication before Portal confirmation.
- JVM API comparison against the consumer's baseline AAR. The 5.0 engine extends internal-facing
  ShapeConfig/NeuShape contracts; rebuild consumers and test custom shapes when migrating.

## Further research

- Direct GPU compositing without CPU readback.
- ALPHA_8 storage with backend-consistent alpha convolution.
- Measured frame-overrun feedback controller; explicit prefetch/priority scheduling.
- Pinned last-frame masks with accounting across nodes and cache.
- Additional accessibility audits on physical devices and contrast testing for user-provided colors.
