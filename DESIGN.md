---
name: Tide
description: A calm, warm-minimalist screen-time companion — Apple-grade native restraint with a clay-and-sea soul.
colors:
  clay: "#B8512F"
  clay-ink: "#9E4327"
  clay-dark: "#CF6B41"
  sea: "#2C6E68"
  sea-ink: "#245A54"
  sea-dark: "#5FB5A8"
  amber: "#C7803F"
  amber-ink: "#9A5A28"
  oxblood: "#9E3B26"
  oxblood-dark: "#E07A5F"
  canvas: "#FBFAF7"
  canvas-inset: "#F1EEE7"
  surface: "#FFFFFF"
  hairline: "#EAE6DD"
  ink: "#1C1A16"
  ink-muted: "#6B6457"
  ink-faint: "#9C9488"
  canvas-dark: "#15120E"
  canvas-inset-dark: "#1E1A14"
  surface-dark: "#211C16"
  hairline-dark: "#322B22"
  ink-dark: "#F0EBE0"
  ink-muted-dark: "#A79D8D"
  ink-faint-dark: "#7C7363"
typography:
  metric:
    fontFamily: "Space Grotesk"
    fontSize: "64sp"
    fontWeight: 300
    lineHeight: "1.0"
    letterSpacing: "-0.02em"
  headline:
    fontFamily: "Inter"
    fontSize: "26sp"
    fontWeight: 600
    lineHeight: "1.2"
    letterSpacing: "-0.01em"
  title:
    fontFamily: "Inter"
    fontSize: "16sp"
    fontWeight: 600
    lineHeight: "1.35"
  body:
    fontFamily: "Inter"
    fontSize: "15sp"
    fontWeight: 400
    lineHeight: "1.5"
  label:
    fontFamily: "Inter"
    fontSize: "12sp"
    fontWeight: 500
    letterSpacing: "0.02em"
rounded:
  chip: "12dp"
  button: "18dp"
  card: "24dp"
  pill: "999dp"
spacing:
  xs: "4dp"
  sm: "8dp"
  md: "16dp"
  lg: "24dp"
  xl: "40dp"
components:
  button-primary:
    backgroundColor: "{colors.clay}"
    textColor: "{colors.surface}"
    rounded: "{rounded.button}"
    padding: "18dp 24dp"
  button-ghost:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.ink}"
    rounded: "{rounded.button}"
    padding: "16dp 24dp"
  card:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.ink}"
    rounded: "{rounded.card}"
    padding: "20dp"
  chip-selected:
    backgroundColor: "{colors.clay}"
    textColor: "{colors.surface}"
    rounded: "{rounded.chip}"
    padding: "10dp 16dp"
---

# Design System: Tide

## 1. Overview

**Creative North Star: "Still Water at Dawn."**

Tide is the antidote to the apps it guards against, so its interface must never do what they do. Where they are loud, infinite, and engineered to pull, Tide is quiet, finite, and engineered to release. The whole system is built from **warm light, deep ink, generous stillness, and one grounded clay accent** — the feeling of a calm room with morning sun on a plaster wall, not a glowing screen in a dark room.

The defining move is a deliberate refusal of the category's two reflexes. The first reflex for "calm app" is lavender-and-glass; the second, once purple is rejected, is teal-water-gradient. Tide takes neither. Its soul color is **terracotta clay** — earthen, human, unhurried — with a **deep-sea teal** held in reserve as a cool counter-voice. Warmth lives in the *brand color and the type*, never in a muddy cream background; the canvas stays a near-white plaster so the clay can speak. The water in "Tide" is expressed through **form and motion** — a level that rises and ebbs, a slow breath, a meniscus on a fill — not by painting everything blue.

Density is low by mandate. One subject per screen, framed by air. Cards are quiet white planes separated by hairlines and the faintest lift, not drop-shadow theatrics. Motion is brief and physical (150–260ms springs that settle like water finding level), never choreographed or attention-seeking.

**Key Characteristics:**
- Warm near-white canvas; pure-white surfaces; deep warm-black ink.
- A single clay accent used with discipline; deep-sea teal as the cool second voice.
- Big, light Space Grotesk numerals for data; Inter for everything that is UI.
- The "tide level" gauge — a rising/ebbing water fill — as the signature metaphor, replacing the gradient ring.
- Stillness as luxury: lots of nothing, hairline structure, no glow.

## 2. Colors

A warm-neutral plaster stage carrying one earthen clay voice and a cool deep-sea counter-voice; alerts are oxblood, never fire-engine red.

### Primary
- **Clay** (#B8512F): The single brand color. Primary buttons, the active tab, selected chips, the rising-tide fill, focus/limit progress. White text sits on it. **Clay Ink** (#9E4327) is its darker cousin for clay-colored *text and values* on the light canvas (small clay text on white fails contrast otherwise). In dark mode clay brightens to **Clay Dark** (#CF6B41).

### Secondary
- **Sea** (#2C6E68): The cool counter-voice. "Under your goal" / "less than yesterday" / completed-session / positive states, and the focus breathing ring. Used as ink, stroke, and progress far more than as a fill. **Sea Ink** (#245A54) for small sea text on light; **Sea Dark** (#5FB5A8) in dark mode.

### Tertiary
- **Amber** (#C7803F): Warm caution — "more than yesterday," approaching-limit warmth, the warning chip. A softer, sunnier warm than clay so the two don't collide. **Amber Ink** (#9A5A28) for text.
- **Oxblood** (#9E3B26): Destructive and hard-limit states (delete, "always blocked," hold-to-end). Deeper and bluer than clay so it never reads as merely "warm." **Oxblood Dark** (#E07A5F) in dark mode.

### Neutral
- **Canvas** (#FBFAF7 light / #15120E dark): App background — warm plaster by day, warm espresso by night. Never cream, never blue-black.
- **Canvas Inset** (#F1EEE7 / #1E1A14): Recessed fills — input backgrounds, segmented-control troughs, gauge tracks.
- **Surface** (#FFFFFF / #211C16): Cards and sheets. Pure white by day reads as clean paper.
- **Hairline** (#EAE6DD / #322B22): Borders and dividers. 1dp, the primary way structure is drawn.
- **Ink** (#1C1A16 / #F0EBE0): Primary text and key numerals. ~14:1 on canvas.
- **Ink Muted** (#6B6457 / #A79D8D): Secondary text and labels. ≥4.5:1 — never lighter for "elegance."
- **Ink Faint** (#9C9488 / #7C7363): Decorative marks, axis ticks, disabled glyphs only — never body text.

### Named Rules
**The One Clay Rule.** Clay covers ≤10% of any screen. It marks the single most important action or the live value, and nothing else. Its rarity is what makes it read as calm rather than branded. If two things on a screen are clay, one is wrong.

**The No-Glow Rule.** Color is laid down flat and solid. No gradient fills on buttons, no neon, no glassmorphism, no glow behind cards. The only gradients permitted are the soft, near-invisible atmospheric wash on full-bleed moments (onboarding, the pause) and the meniscus on the tide fill.

## 3. Typography

**Display / Metric Font:** Space Grotesk (fallback: sans-serif)
**UI & Body Font:** Inter (fallback: system-ui, sans-serif)

**Character:** Inter carries the entire interface — headings, titles, labels, body, buttons — at a tight, confident, SF-adjacent register. Space Grotesk appears *only* as large, light numerals: the day's screen time, the focus countdown, the headline stat values. The pairing is data-vs-interface, not display-vs-body, which is why two sans-serifs can share a screen without muddiness.

### Hierarchy
- **Metric** (Space Grotesk Light, 44–72sp, lineHeight 1.0, tracking −0.02em): Hero numerals only — total screen time, countdown, big stat figures. Never used for words.
- **Headline** (Inter SemiBold, 24–28sp, −0.01em): Screen titles ("Today", "Shields"), pause headlines.
- **Title** (Inter SemiBold, 16sp): Card titles, row primary text, list items.
- **Body** (Inter Regular, 15sp, lineHeight 1.5): Descriptions and prose. Capped ~60ch.
- **Label** (Inter Medium, 11–13sp, +0.02em): Metadata, units, axis labels, button text. Sentence case.

### Named Rules
**The Numerals-Only Rule.** Space Grotesk is for figures, never for words or UI labels. The moment it spells a word, it becomes a costume; product type stays Inter.

**The Sentence-Case Rule.** Labels and buttons are sentence case ("New shield", "Start focus"), never ALL-CAPS tracked eyebrows. The calm voice doesn't shout in small text.

## 4. Elevation

Tide is flat by conviction. Depth comes from **tonal layering and hairlines**, not shadow theatrics — surface sits a hair above canvas, separated by a 1dp hairline. A single, very soft ambient shadow is permitted on true floating objects (the bottom nav, modal sheets, the primary CTA at rest) to lift them off the page; it is diffuse and shadow-only, never a dark drop. Pressed states *reduce* elevation and scale slightly, like setting something down.

### Shadow Vocabulary
- **Float** (`0 8dp 24dp rgba(28,26,22,0.08)` light / `0 8dp 24dp rgba(0,0,0,0.40)` dark): The bottom nav, modal sheets, the running-focus CTA. Diffuse, soft, barely there.

### Named Rules
**The Flat-By-Default Rule.** Cards do not cast shadows at rest. If a 2014-app drop shadow appears under a content card, it is wrong; redraw the separation with a hairline and tonal step.

## 5. Components

### Buttons
- **Shape:** Gently rounded (18dp), consistent across the app.
- **Primary:** Solid **Clay** fill, white label, 18dp vertical padding. One per screen. No gradient, ever.
- **Ghost / Secondary:** **Surface** fill with a 1dp **Hairline** border, ink label. The default for non-primary actions.
- **Press / Focus:** Scale to ~0.97 and lower elevation on press (a calm "settle"), 160ms spring. Disabled drops to 38% opacity.

### Chips (presets, day-dots, labels)
- **Style:** Unselected = **Canvas Inset** fill, muted ink, no border. Selected = **Clay** fill, white text (or **Sea** tint for the focus "intention" chips). 12dp radius.
- **State:** Selection animates fill + text color over a 200ms spring; no bounce.

### Cards / Containers
- **Corner Style:** 24dp.
- **Background:** **Surface** (#FFFFFF / #211C16).
- **Shadow Strategy:** None at rest (see Elevation). Hairline border + tonal step only.
- **Border:** 1dp **Hairline**.
- **Internal Padding:** 20dp (lg-ish). Rows inside use 16dp.

### Inputs / Fields
- **Style:** **Canvas Inset** fill, no visible border at rest, 16dp radius — a quiet recessed trough.
- **Focus:** A 1.5dp **Clay** border fades in; no glow.
- **Placeholder:** **Ink Muted** (full contrast, not a faint gray).

### Navigation
- **Style:** A low, floating bottom bar — **Surface** fill, hairline border, the soft **Float** shadow, pill radius. Four destinations (Today · Shields · Focus · Insights).
- **States:** Active destination shows a clay-tinted pill behind the icon and reveals its label; inactive are muted-ink icons only. The active indicator slides between destinations on a spring. No labels shout; the active one whispers.

### Signature: The Tide Gauge
The hero metric is not a gradient ring. It is a **vessel that fills with a rising tide**: a thin circular outline whose interior fills bottom-up to the usage fraction, with a gentle animated wave on the water's surface (a meniscus). Under goal, the water is **sea/ink-calm**; as it nears and passes the goal it warms to **clay**. The day's figure sits over it in light Space Grotesk. The same metaphor recurs as the horizontal **tide bar** in usage rows (a fill with a soft leading meniscus) and as the breathing level in the focus and pause screens. This is Tide's one ownable form; it earns the name.

## 6. Do's and Don'ts

### Do:
- **Do** keep the canvas a near-white warm plaster (#FBFAF7) and let **Clay** (#B8512F) carry the warmth. Warm primary on a near-white stage; never both warm.
- **Do** hold clay to ≤10% of a screen (The One Clay Rule) and reach for **Sea** for positive/calm states.
- **Do** draw structure with 1dp hairlines and tonal steps; keep surfaces flat (The Flat-By-Default Rule).
- **Do** reserve Space Grotesk for numerals and use Inter for every word and label.
- **Do** animate state with brief, settling springs (150–260ms). Motion conveys state, not decoration.
- **Do** use the Tide Gauge / tide-bar / breathing level as the through-line metaphor for any progress.

### Don't:
- **Don't** use **neon glassmorphism** — no frosted-glass cards, no glowing violet/mint gradients, no dark-mode neon. (This was Tide's old skin; it is the anti-reference.)
- **Don't** drift toward **meditation purple** — no purple-to-blue gradients, soft blobs, or mascots.
- **Don't** build a **busy quantified-self dashboard** — no walls of chips, charts, and numbers competing at once. One subject per screen.
- **Don't** fall back to **corporate/sterile flat-gray Material** — keep the warm, human palette and the clay voice.
- **Don't** gradient-fill buttons or text, ever (The No-Glow Rule). Solid clay only.
- **Don't** put a cream/sand body background under the warm clay (the "claude-beige" trap); the canvas stays near-white.
- **Don't** set words in Space Grotesk or print ALL-CAPS tracked eyebrows above sections.
- **Don't** gamify with streak-anxiety or urgency; data stays honest and encouraging.
