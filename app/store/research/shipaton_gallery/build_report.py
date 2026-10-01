"""Build the Nagly competition report (HTML -> PDF via headless Chrome) from scores_*.json."""
import json, html, os, subprocess, datetime
AW = [("onesignal", "Keep Them Coming Back (OneSignal)", "$25,000 · $15,000 · $5,000",
       ["Impl.", "Value", "Creative", "Advanced", "Evidence", "Reach"]),
      ("catvertising", "Catvertising (RevenueCat Ads)", "$20,000 · $10,000 · $5,000",
       ["Natural", "Unlock", "Fit", "Stack", "RC Ads/UX", "Reach"]),
      ("peace", "RevenueCat Peace Prize", "$20,000 · $10,000 · $5,000",
       ["Problem", "Who", "Works", "Access", "Evidence", "Reach"]),
      ("hamm", "HAMM (Help Apps Make Money)", "$20,000 · $10,000 · $5,000",
       ["Paywall", "Pricing", "Mix", "Innov.", "Evidence", "Reach"]),
      ("design", "RevenueCat Design Award", "$20,000 · $10,000 · $5,000",
       ["Idea", "Looks", "Motion", "Identity", "Proof", "Reach"])]
POOL = {"onesignal": 209, "catvertising": 323, "peace": 339, "hamm": 823, "design": 437}
e = html.escape
def sec(key, name, prize, cols):
    d = json.load(open(f"scores_{key}.json")); n = d["nagly"]
    rows = []
    for i, x in enumerate(d["entries"], 1):
        s = x["scores"]; nag = "nagly" in x["url"]
        flag = "" if "NO STORE" not in x.get("stores", "") else ' <span class="flag">NO STORE LINK</span>'
        rows.append(f'<tr class="{"nag" if nag else ""}"><td>{i}</td><td><a href="{e(x["url"])}">{e(x["title"])}</a>{flag}</td>'
                    + "".join(f"<td>{s[k]}</td>" for k in ["c1", "c2", "c3", "c4", "c5", "reach"])
                    + f'<td class="tot">{x["total"]}</td><td class="sm">{e(x.get("stores",""))}</td></tr>')
    pod = "".join(f'<div class="pod"><div class="place">{["1ST · FAVOURITE","2ND","3RD"][i]}</div><b>{e(p["title"])}</b><p>{e(p["why"])}</p></div>'
                  for i, p in enumerate(d["podium"][:3]))
    fixes = "".join(f"<li>{e(f)}</li>" for f in n["top_fixes"])
    rev = "".join(f'<div class="rev"><b>{i}. {e(x["title"])} · {x["total"]}/30</b> <span class="sm">{e(x.get("stores",""))}</span>'
                  f'<p><span class="k">STRENGTHS</span> {e(x["strengths"])}</p><p><span class="k">GAPS</span> {e(x["gaps"])}</p></div>'
                  for i, x in enumerate(d["entries"][:12], 1))
    return f'''<section><div class="eyebrow">AWARD</div><h2>{e(name)}</h2><div class="sub">{prize} · {POOL[key]} apps mention it · top {len(d["entries"])} scored in detail</div>
<div class="nagbox"><div><div class="big">{n["score_public"]}<small>/30</small></div><div class="sm">public text · rank {n["rank_public"]} of {len(d["entries"])}</div></div>
<div><div class="big">{n["score_with_judge_answers"]}<small>/30</small></div><div class="sm">with judge-only answers · rank {n["rank_with_judge_answers"]} (best case)</div></div>
<div class="nt"><p><span class="k">NAGLY STRENGTHS</span> {e(n["strengths"])}</p><p><span class="k">NAGLY GAPS</span> {e(n["gaps"])}</p></div></div>
<h3>Who wins (my call)</h3><div class="pods">{pod}</div>
<h3>What Nagly should change, most valuable first</h3><ol class="fix">{fixes}</ol>
<h3>Ranking</h3><table><tr><th>#</th><th>Entry</th>{"".join(f"<th>{c}</th>" for c in cols)}<th>/30</th><th>Stores</th></tr>{"".join(rows)}</table>
<p class="conf"><b>How confident?</b> {e(d["confidence"])}</p>
<h3>Top 12, reviewed</h3><div class="revs">{rev}</div></section>'''
summary = []
for k, name, *_ in AW:
    d = json.load(open(f"scores_{k}.json")); n = d["nagly"]
    summary.append(f'<tr><td>{e(name)}</td><td>{POOL[k]}</td><td>{n["score_public"]} · #{n["rank_public"]}</td><td>{n["score_with_judge_answers"]} · #{n["rank_with_judge_answers"]}</td><td>{e(d["podium"][0]["title"])} ({d["entries"][0]["total"]})</td></tr>')
CSS = """body{font-family:-apple-system,'Helvetica Neue',sans-serif;color:#1d1d1f;margin:0;font-size:12.5px;line-height:1.45}
.wrap{max-width:900px;margin:0 auto;padding:36px 44px}.eyebrow{font-size:10px;letter-spacing:.14em;color:#8a6d3b;font-weight:700}
h1{font-size:34px;margin:6px 0 4px}h2{font-size:24px;margin:4px 0 2px}h3{font-size:14px;margin:18px 0 6px}
.sub,.sm{color:#6b6b6b;font-size:11px}section{page-break-before:always;padding-top:4px}
table{border-collapse:collapse;width:100%;font-size:10.5px}td,th{border-bottom:1px solid #e5e5e5;padding:4px 5px;text-align:left}th{background:#f5f3ef}
td.tot{font-weight:800}tr.nag td{background:#fff1e6;font-weight:700}.flag{background:#c0392b;color:#fff;font-size:8px;padding:1px 4px;border-radius:3px;font-weight:700}
.nagbox{display:flex;gap:22px;align-items:flex-start;background:#fff7f0;border:1px solid #f0d9c5;border-radius:10px;padding:14px 16px;margin:12px 0}
.big{font-size:30px;font-weight:800;color:#12808C}.big small{font-size:13px;color:#888}.nt{flex:1;font-size:11.5px}.nt p{margin:0 0 6px}
.k{font-size:9px;font-weight:800;letter-spacing:.1em;color:#8a6d3b;margin-right:4px}.pods{display:flex;gap:10px}
.pod{flex:1;border:1px solid #e5e5e5;border-radius:8px;padding:10px}.pod p{margin:4px 0 0;font-size:11px}.place{font-size:9px;font-weight:800;letter-spacing:.1em;color:#12808C}
.fix li{margin-bottom:4px}.conf{font-size:11px;color:#444;background:#f7f7f7;padding:8px 10px;border-radius:6px}
.revs{columns:2;column-gap:20px}.rev{break-inside:avoid;margin-bottom:10px;font-size:11px}.rev p{margin:2px 0}a{color:#1d1d1f;text-decoration:none}
.stats{display:flex;gap:12px;margin:16px 0}.stat{flex:1;background:#f5f3ef;border-radius:8px;padding:10px}.stat b{font-size:24px;display:block}"""
NOTE = open("method_note.html").read() if os.path.exists("method_note.html") else ""
doc = f'''<html><head><meta charset="utf-8"><style>{CSS}</style></head><body><div class="wrap">
<div class="eyebrow">REVENUECAT SHIPATON 2026 · INDEPENDENT REVIEW · NAGLY'S AWARDS</div>
<h1>Where Nagly stands</h1><div class="sub">Gallery read {datetime.date.today():%d %b %Y} · 3,230 public submissions · 5 awards Nagly entered</div>
<div class="stats"><div class="stat"><b>3,230</b>submissions in the gallery</div><div class="stat"><b>2,159</b>with a store link</div><div class="stat"><b>5</b>open awards Nagly entered</div><div class="stat"><b>~180</b>contenders scored in detail</div></div>
<h3>Nagly at a glance</h3><table><tr><th>Award</th><th>Apps in pool</th><th>Nagly · public text</th><th>Nagly · with judge answers (best case)</th><th>Current leader</th></tr>{"".join(summary)}</table>
{NOTE}
<h3>How I judged</h3><p>Same method as the Gaming review: each award's official criteria from the Shipaton rules become six measures scored 1–5, equal weight, total /30. Reach is shared: both stores = 5, one store = 3, no store link = 1. Every app in the gallery was read; each award's pool is the apps whose own text describes that award's subject, and the strongest ~35 of each pool (by evidence, store reach, video and likes) were scored in detail, plus Nagly. Scores come only from each submission's public text, tags, links and video; apps were not installed, so an undescribed feature scores as absent. Judges also see judge-only answers: Nagly's were read, other entrants' could not be, so Nagly's "with judge answers" score is a ceiling, not a forecast.</p>
{"".join(sec(*a) for a in AW)}
</div></body></html>'''
open("Nagly_Shipaton_Review.html", "w").write(doc)
out = os.path.abspath("Nagly_Shipaton_Review.pdf")
subprocess.run(["/Applications/Google Chrome.app/Contents/MacOS/Google Chrome", "--headless=new", "--disable-gpu",
                "--no-pdf-header-footer", "--print-to-pdf=" + out, "file://" + os.path.abspath("Nagly_Shipaton_Review.html")],
               check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
print(out)
