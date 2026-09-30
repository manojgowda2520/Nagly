"""Builds the Nagly demo video from Flow clips + real app screenshots.

Run: <venv>/bin/python build_video.py   (needs imageio-ffmpeg and Google Chrome)
Optional: put voiceover MP3s in vo/<segment-id>.mp3 and they are mixed in.
"""
import html
import os
import subprocess
import sys

import imageio_ffmpeg

FF = imageio_ffmpeg.get_ffmpeg_exe()
CHROME = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
HERE = os.path.dirname(os.path.abspath(__file__))
STORE = os.path.dirname(HERE)
SHOTS = os.path.join(STORE, "ios_screenshots")
OUT = os.path.join(HERE, "build")
W, H, FPS = 1920, 1080, 24

# (id, kind, source, seconds, chapter label, headline, subtitle)
SEGMENTS = [
    ("01_a", "clip", "A", 7, "A NORMAL DAY", "",
     "Every day, I swipe away a hundred reminders."),
    ("02_b", "clip", "B", 8, "BACK HOME", "",
     "Back home, there was one voice I never ignored. Now I live alone, and nobody nags me."),
    ("03_title", "title", None, 4, "", "", ""),
    ("04_home", "app", "raw_101/1_home.png", 8, "01 · NAGS YOU CAN'T IGNORE",
     "Reminders in the voice of someone who loves you.",
     "I picked Mom and named her Subbu Amma. Log a glass in one tap."),
    ("05_c", "clip", "C", 8, "", "",
     "“Beta, sip some water. I'm not nagging, I'm caring.”"),
    ("06_personas", "app", "raw_101/b_personas_mom.png", 8, "02 · 15 VOICES",
     "Mom, Dad, Grandma, Bestie, Spouse.",
     "Fifteen voices in five families. Mom is free, forever."),
    ("07_custom", "app", "raw_101/5_make_it_yours.png", 7, "03 · MAKE IT YOURS",
     "Give her a real name.",
     "Rename any persona to the real person, with their emoji."),
    ("08_d", "clip", "D", 7, "NOT JUST WATER", "",
     "Creatine, protein, vitamins, pills."),
    ("09_supps", "app", "raw_101/4_supplements.png", 8, "04 · MEDS & SUPPLEMENTS",
     "Pills, vitamins, creatine, protein.",
     "Quick picks, and \"Took it\" right from the notification. One reminder is free forever."),
    ("10_e", "clip", "E", 8, "AMMA USES IT TOO", "",
     "I set it up for Amma too. Appa's voice reminds her about her BP tablet."),
    ("11_history", "app", "raw_101/6_history.png", 7, "05 · IT REMEMBERS",
     "History is a chat with your nagger.",
     "Every nudge and every sip, with the mood it was sent in."),
    ("12_paywall", "app", "raw/4_paywall.png", 7, "06 · FREE TO START",
     "Keep Dad around?",
     "The persona you want pleads for itself. Payments by RevenueCat, nudges by OneSignal."),
    ("13_f", "clip", "F", 8, "", "",
     "People ignore alarms. They don't ignore their mom."),
    ("14_end", "end", None, 6, "", "", ""),
]

CSS = """
:root{--ink:#2B1A12;--soft:#6B4A3A;--teal:#12808C;--cream:#FFF3E6;--peach:#FFD9C2}
*{margin:0;box-sizing:border-box}
body{width:%dpx;height:%dpx;overflow:hidden;font-family:-apple-system,'Helvetica Neue',sans-serif}
.label{font-size:26px;font-weight:800;letter-spacing:.18em;color:var(--teal)}
.serif{font-family:'New York','Georgia',serif}
.sub{position:absolute;left:0;right:0;bottom:56px;text-align:center}
.sub span{display:inline-block;max-width:1500px;padding:14px 28px;border-radius:14px;
  background:rgba(20,12,8,.62);color:#fff;font-size:40px;font-weight:600;line-height:1.3}
""" % (W, H)


def render(name, body, transparent=False):
    path_html = os.path.join(OUT, name + ".html")
    path_png = os.path.join(OUT, name + ".png")
    with open(path_html, "w") as f:
        f.write("<html><head><meta charset='utf-8'><style>%s</style></head><body%s>%s</body></html>"
                % (CSS, " style='background:transparent'" if transparent else "", body))
    args = [CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars",
            "--force-device-scale-factor=1", "--window-size=%d,%d" % (W, H),
            "--screenshot=" + path_png, "file://" + path_html]
    if transparent:
        args.insert(2, "--default-background-color=00000000")
    subprocess.run(args, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    return path_png


def sub_html(text):
    return "<div class='sub'><span>%s</span></div>" % html.escape(text) if text else ""


def overlay_png(seg_id, label, subtitle):
    label_html = ("<div style='position:absolute;left:64px;top:52px'><span class='label' "
                  "style='color:#fff;background:rgba(18,128,140,.85);padding:10px 18px;"
                  "border-radius:10px'>%s</span></div>" % html.escape(label)) if label else ""
    return render(seg_id + "_ov", label_html + sub_html(subtitle), transparent=True)


def app_png(seg_id, shot, label, headline, subtitle):
    shot_path = os.path.join(SHOTS, shot)
    body = f"""
<div style="position:absolute;inset:0;background:linear-gradient(135deg,var(--cream),var(--peach))"></div>
<div style="position:absolute;left:150px;top:0;bottom:0;width:980px;display:flex;flex-direction:column;justify-content:center">
  <div class="label">{html.escape(label)}</div>
  <div class="serif" style="margin-top:26px;font-size:80px;font-weight:700;line-height:1.1;color:var(--ink)">{html.escape(headline)}</div>
  <div style="margin-top:34px;font-size:40px;font-weight:600;line-height:1.35;color:var(--soft)">{html.escape(subtitle)}</div>
</div>
<img src="file://{shot_path}" style="position:absolute;right:260px;top:70px;height:940px;
  border-radius:56px;box-shadow:0 30px 60px rgba(60,30,10,.35);border:10px solid #1b1b1b">"""
    return render(seg_id, body)


def title_png(seg_id, end=False):
    icon = os.path.join(STORE, "icon-1024.png")
    line = ("Live on the App Store" if end else "Reminders that sound like Mom")
    extra = ("<div style='margin-top:36px;font-size:40px;color:var(--soft);font-weight:600'>"
             "Water · Meds · Supplements &nbsp;·&nbsp; Search “Nagly”</div>") if end else ""
    body = f"""
<div style="position:absolute;inset:0;background:linear-gradient(135deg,var(--cream),var(--peach));
  display:flex;flex-direction:column;align-items:center;justify-content:center">
  <img src="file://{icon}" style="width:260px;border-radius:58px;box-shadow:0 20px 50px rgba(60,30,10,.3)">
  <div class="serif" style="margin-top:40px;font-size:150px;font-weight:800;color:var(--ink)">Nagly</div>
  <div style="font-size:56px;font-weight:700;color:var(--teal)">{line}</div>{extra}
</div>"""
    return render(seg_id, body)


def run(args):
    subprocess.run([FF, "-hide_banner", "-loglevel", "error", "-y"] + args, check=True)


def vo_input(seg_id):
    p = os.path.join(HERE, "vo", seg_id + ".mp3")
    return p if os.path.exists(p) else None


def build_segment(seg):
    seg_id, kind, src, secs, label, headline, subtitle = seg
    out = os.path.join(OUT, seg_id + ".mp4")
    fade = "fade=t=in:st=0:d=0.35,fade=t=out:st=%.2f:d=0.35" % (secs - 0.35)
    vo = vo_input(seg_id)
    if kind == "clip":
        clip = os.path.join(HERE, "clips", src + ".mp4")
        ov = overlay_png(seg_id, label, subtitle)
        has_audio = src != "D"
        inputs = ["-i", clip, "-i", ov]
        if not has_audio:
            inputs += ["-f", "lavfi", "-t", str(secs), "-i", "anullsrc=r=48000:cl=stereo"]
        amb = "[0:a]" if has_audio else "[2:a]"
        vf = ("[0:v]scale=%d:%d,setsar=1,fps=%d[b];[b][1:v]overlay=0:0,%s[v]" % (W, H, FPS, fade))
        af = "%svolume=0.45,afade=t=in:d=0.35,afade=t=out:st=%.2f:d=0.35[a0]" % (amb, secs - 0.35)
    else:
        png = app_png(seg_id, src, label, headline, subtitle) if kind == "app" else title_png(seg_id, kind == "end")
        inputs = ["-i", png,
                  "-f", "lavfi", "-t", str(secs), "-i", "anullsrc=r=48000:cl=stereo"]
        frames = secs * FPS
        vf = ("[0:v]scale=%d:%d,zoompan=z='1+0.035*on/%d':x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':"
              "d=%d:s=%dx%d:fps=%d,setsar=1,%s[v]" % (W * 2, H * 2, frames, frames, W, H, FPS, fade))
        af = "[1:a]anull[a0]"
    if vo:
        inputs += ["-i", vo]
        vo_idx = inputs.count("-i") - 1
        af += ";[%d:a]adelay=250|250[vo];[a0][vo]amix=inputs=2:duration=first:normalize=0[a]" % vo_idx
    else:
        af += ";[a0]anull[a]"
    run(inputs + ["-filter_complex", vf + ";" + af, "-map", "[v]", "-map", "[a]",
                  "-t", str(secs), "-c:v", "libx264", "-pix_fmt", "yuv420p", "-preset", "medium",
                  "-crf", "19", "-r", str(FPS), "-c:a", "aac", "-ar", "48000", "-ac", "2", "-b:a", "160k", out])
    return out


def main():
    os.makedirs(OUT, exist_ok=True)
    only = sys.argv[1:]
    parts = []
    for seg in SEGMENTS:
        if only and seg[0] not in only:
            parts.append(os.path.join(OUT, seg[0] + ".mp4"))
            continue
        print("building", seg[0], flush=True)
        parts.append(build_segment(seg))
    listfile = os.path.join(OUT, "list.txt")
    with open(listfile, "w") as f:
        f.writelines("file '%s'\n" % p for p in parts)
    final = os.path.join(HERE, "Nagly_demo.mp4")
    run(["-f", "concat", "-safe", "0", "-i", listfile, "-c", "copy", "-movflags", "+faststart", final])
    print("done:", final, sum(s[3] for s in SEGMENTS), "seconds")


if __name__ == "__main__":
    main()
