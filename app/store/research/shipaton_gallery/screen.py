"""Stage 1: cheap evidence score per award -> top N contenders (+ Nagly) written as compact files for detailed scoring."""
import json, re, sys
N = int(sys.argv[1]) if len(sys.argv) > 1 else 35
KW = {
 "onesignal": r"onesignal|journey|in-app message|segment|tag|liquid|deep link|action button|outcome|click rate|ctr|open rate|win-back|re-engag|streak|push",
 "catvertising": r"revenuecat ads|rewarded|opt-in|ad-free|frequency cap|unlock|watch an ad|native ad|interstitial|banner|no ads for|ads? only|24 hours|catvertising",
 "hamm": r"paywall|placement|targeting|offering|experiment|a/b|trial|lifetime|annual|monthly|weekly|consumable|web purchase|stripe|funnel|revenue|\$\d|mrr|conversion|pricing|tier|ads",
 "design": r"animation|animated|haptic|gesture|illustrat|delight|shader|3d|particle|confetti|metal|lottie|rive|spring|physics|accelerometer|parallax|custom font|hand-drawn|pixel|motion",
 "peace": r"peace prize|social good|accessib|mental health|medication|elderly|older|disabilit|caregiver|community|nonprofit|climate|low-income|literacy|blind|deaf|loneliness|addiction|free forever|offline|privacy|million|patients|students|children",
}
def reach(p):
    return 5 if p["ios"] and p["android"] else 3 if (p["ios"] or p["android"]) else 1
for award, kw in KW.items():
    pool = json.load(open(f"pool_{award}.json"))
    for p in pool:
        t = (p["title"] + " " + p["tagline"] + " " + p["story"]).lower()
        hits = len(re.findall(kw, t))
        p["_screen"] = min(hits, 40) + 3 * reach(p) + min(p["likes"], 30) * 0.3 + (8 if p["video"] else 0)
    pool.sort(key=lambda p: -p["_screen"])
    top = pool[:N]
    if not any("nagly" in p["url"] for p in top):
        nag = [p for p in pool if "nagly" in p["url"]] or [dict(p, ios=True, android=False) for p in map(json.loads, open("projects.jsonl")) if "nagly" in p["url"]]
        top += nag
    slim = [{"id": i + 1, "title": p["title"], "url": p["url"], "tagline": p["tagline"],
             "stores": ("iOS " if p["ios"] else "") + ("Android" if p["android"] else "") or "NO STORE LINK",
             "likes": p["likes"], "video": bool(p["video"]), "built_with": p["built_with"][:25],
             "story": p["story"][:5500]} for i, p in enumerate(top)]
    json.dump(slim, open(f"top_{award}.json", "w"), indent=1)
    print(award, "pool", len(pool), "-> scored", len(slim), "| nagly screen rank:",
          next((i + 1 for i, p in enumerate(pool) if "nagly" in p["url"]), "not in pool"))
