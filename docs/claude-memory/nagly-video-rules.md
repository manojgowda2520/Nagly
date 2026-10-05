---
name: nagly-video-rules
description: "User's rules for the Nagly demo video (AI clips + real app recordings), built by app/store/video/assemble.py"
metadata:
  node_type: memory
  type: feedback
  originSessionId: ed91b75c-d74d-4c55-a47c-33e78fcdfc02
  modified: 2026-09-30T13:22:04.065Z
---

Rules the user gave while reviewing the Shipaton demo video clip by clip (2026-09-30):
- Never show app features the recordings don't prove (the "tilt the phone" line was cut for this).
- Nobody drinks from a closed bottle: use an open tumbler or a clear glass; objects taken from somewhere visible.
- Notifications must visibly arrive on the phone first (Nagly banner + chime, real persona title/lines from persona_catalog.dart), then the action.
- Soft smiles, not big laughing; phone must not flip or glitch.
- App screens in the same iPhone frame (Dynamic Island) as the lock-screen scene, nothing cropped.

**Why:** judges download finalists and compare the app with the video; the user also cares about realism.
**How to apply:** review each AI clip frame by frame before showing it; fix continuity with trims/keyframes. See [[nagly-flutter-rewrite]].
