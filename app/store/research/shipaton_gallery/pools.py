"""Split scraped projects into per-award competitor pools (keyword evidence from each submission's own text)."""
import json, re, collections
P = [json.loads(l) for l in open("projects.jsonl")]
def txt(p): return " ".join([p["title"], p["tagline"], p.get("story", ""), " ".join(p.get("built_with", []))]).lower()
def stores(p):
    L = " ".join(p.get("links", [])).lower()
    return ("apps.apple.com" in L or "testflight" in L, "play.google.com" in L, "galaxystore" in L or "galaxy.store" in L)
RULES = {
  "onesignal":    lambda t: "onesignal" in t,
  "catvertising": lambda t: bool(re.search(r"revenuecat ads|catvertising|rewarded (video )?ads?|ad[- ]supported|interstitial|admob", t)),
  "hamm":         lambda t: bool(re.search(r"\bhamm\b|help apps make money", t)) or len(re.findall(r"paywall|subscription|lifetime|annual|monthly|trial|in-app purchase|offering", t)) >= 6,
  "design":       lambda t: bool(re.search(r"design award", t)) or len(re.findall(r"animation|animated|haptic|gesture|illustrat|delight|micro-interaction|shader|3d|particle|confetti", t)) >= 4,
  "peace":        lambda t: bool(re.search(r"peace prize|social good", t)) or len(re.findall(r"accessib|mental health|medication|elderly|older adults|disabilit|caregiver|community|nonprofit|climate|refugee|low-income|literacy|blind|deaf|wellbeing|loneliness|addiction|health", t)) >= 5,
}
pools = collections.defaultdict(list)
for p in P:
    t = txt(p); ios, android, galaxy = stores(p)
    p["ios"], p["android"], p["galaxy"] = ios, android, galaxy
    for k, f in RULES.items():
        if f(t): pools[k].append(p)
for k, v in pools.items():
    json.dump(v, open(f"pool_{k}.json", "w"))
    print(k, len(v))
nag = [p for p in P if "nagly" in p["url"]]
print("nagly found:", [p["url"] for p in nag])
print("total", len(P), "with store link", sum(1 for p in P if p["ios"] or p["android"]))
