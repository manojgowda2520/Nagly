"""Scrape the public Shipaton 2026 Devpost gallery + each project page (polite: 4 workers)."""
import json, re, os, sys, time, html, concurrent.futures as cf, urllib.request
BASE = "https://revenuecat-shipaton-2026.devpost.com/project-gallery?page=%d"
UA = {"User-Agent": "Mozilla/5.0 (Nagly team research; contact via devpost)"}
def get(url, tries=4):
    for i in range(tries):
        try:
            with urllib.request.urlopen(urllib.request.Request(url, headers=UA), timeout=30) as r:
                return r.read().decode("utf-8", "replace")
        except Exception as e:
            time.sleep(2 + 3 * i)
    return ""
def strip(s): return html.unescape(re.sub(r"\s+", " ", re.sub(r"<[^>]+>", " ", s))).strip()

def gallery():
    out, page = [], 1
    while True:
        h = get(BASE % page)
        items = re.findall(r'link-to-software" href="([^"]+)">(.*?)</div></a>', h, re.S)
        if not items: break
        for href, body in items:
            title = strip((re.search(r"<h5>(.*?)</h5>", body, re.S) or [None, ""])[1])
            tag = strip((re.search(r'<p class="small tagline">(.*?)</p>', body, re.S) or [None, ""])[1])
            likes = re.search(r'like-count.*?</i>\s*(\d+)', body, re.S); com = re.search(r'comment-count.*?</i>\s*(\d+)', body, re.S)
            out.append({"url": href, "title": title, "tagline": tag,
                        "likes": int(likes.group(1)) if likes else 0, "comments": int(com.group(1)) if com else 0})
        print("page", page, len(out), flush=True); page += 1; time.sleep(0.5)
    return out

def project(p):
    h = get(p["url"])
    story = re.search(r'id="app-details-left">(.*?)<div id="built-with"', h, re.S)
    p["story"] = strip(story.group(1)) if story else ""
    p["built_with"] = [strip(x) for x in re.findall(r'<span class="cp-tag[^"]*">(.*?)</span>', h, re.S)]
    links = re.search(r'<nav class="app-links[^"]*">(.*?)</nav>', h, re.S)
    p["links"] = re.findall(r'href="([^"]+)"', links.group(1)) if links else []
    p["video"] = re.findall(r'<iframe[^>]+src="([^"]*(?:youtube|vimeo|youtu)[^"]*)"', h)
    sub = re.search(r'id="submissions"(.*?)</div>\s*</div>', h, re.S)
    p["winner"] = "winner" in (sub.group(1).lower() if sub else "")
    return p

if __name__ == "__main__":
    if not os.path.exists("gallery.json"):
        json.dump(gallery(), open("gallery.json", "w"))
    g = json.load(open("gallery.json")); print("total", len(g), flush=True)
    done = {}
    if os.path.exists("projects.jsonl"):
        for l in open("projects.jsonl"): d = json.loads(l); done[d["url"]] = d
    todo = [p for p in g if p["url"] not in done]
    with open("projects.jsonl", "a") as f, cf.ThreadPoolExecutor(4) as ex:
        for i, p in enumerate(ex.map(project, todo)):
            f.write(json.dumps(p) + "\n"); f.flush()
            if i % 100 == 0: print("projects", i, "/", len(todo), flush=True)
    print("DONE", flush=True)
