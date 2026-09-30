---
name: Gallius Dynamic Media Engine
colors:
  surface: '#111317'
  surface-dim: '#111317'
  surface-bright: '#37393e'
  surface-container-lowest: '#0c0e12'
  surface-container-low: '#1a1c20'
  surface-container: '#1e2024'
  surface-container-high: '#282a2e'
  surface-container-highest: '#333539'
  on-surface: '#e2e2e8'
  on-surface-variant: '#c7c4d6'
  inverse-surface: '#e2e2e8'
  inverse-on-surface: '#2f3035'
  outline: '#918f9f'
  outline-variant: '#464554'
  surface-tint: '#c2c1ff'
  primary: '#c2c1ff'
  on-primary: '#1c0b9f'
  primary-container: '#5856d6'
  on-primary-container: '#e7e4ff'
  inverse-primary: '#4f4ccd'
  secondary: '#42e2d8'
  on-secondary: '#003734'
  secondary-container: '#00c5bc'
  on-secondary-container: '#004c48'
  tertiary: '#68d3ff'
  on-tertiary: '#003546'
  tertiary-container: '#007090'
  on-tertiary-container: '#c8ecff'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  background: '#111317'
  on-background: '#e2e2e8'
  canvas: '#0F1115'
  card: '#181B20'
  elevated: '#22262E'
  accent-indigo: '#5856D6'
  accent-cyan: '#00C7BE'
---

# Gallius design system (source of truth in code)

Implement tokens in:

- `app/src/main/java/com/balius/galius/ui/theme/Color.kt`
- `Type.kt` / `Shape.kt` / `Spacing.kt` / `Theme.kt`

Use `GaliusTheme`, `MaterialTheme`, and `GaliusThemeTokens` for colors/typography/spacing.
Do not invent ad-hoc hex values in feature UI.

Bottom navigation tabs: **Home · Search · More**.

All user-visible copy lives in `res/values/strings.xml`.
