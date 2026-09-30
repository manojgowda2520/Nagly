"""Nagly demo v2 — assembles AI clips, motion graphics, app screens, voice-over, music and SFX.

Run: ~/development/videnv/bin/python assemble.py
Inputs (relative to this folder):
  clips_q/K1..K5.mp4      Veo 3.1 Quality story shots (K3 falls back to clips/C.mp4)
  build/m_*.mp4           motion graphics rendered from motion/*/index.html
  rec/*.mov|mp4           your iPhone screen recordings (optional; screenshots are used until they exist)
  vo_lines/*.wav          ElevenLabs lines (n01..n15 Arjun, a01..a03 Amma)
  music/heartfelt_app_score.mp3, sfx/chime.wav
"""
import html
import os
import subprocess

FF = os.path.expanduser("~/development/bin/ffmpeg")
CHROME = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
HERE = os.path.dirname(os.path.abspath(__file__))
SHOTS = os.path.join(os.path.dirname(HERE), "ios_screenshots", "raw_101")
B = os.path.join(HERE, "build")
W, H, FPS, XF = 1920, 1080, 24, 0.4


def p(*a):
    return os.path.join(HERE, *a)


def run(args):
    subprocess.run([FF, "-hide_banner", "-loglevel", "error", "-y"] + args, check=True)


def clip(name, fallback=None):
    path = p("clips_q", name + ".mp4")
    return path if os.path.exists(path) else fallback


def rec(name):
    for ext in (".mov", ".mp4", ".MOV", ".MP4"):
        path = p("rec", name + ext)
        if os.path.exists(path):
            return path
    return None


# ---- app screen cards (phone on the left headline, real screen on the right) ----
CARD_CSS = """
@font-face { font-family:'New York'; src: local('New York'), local('Georgia'); }
*{margin:0;box-sizing:border-box} body{width:1920px;height:1080px;overflow:hidden;
font-family:-apple-system,'Helvetica Neue',sans-serif;background:linear-gradient(135deg,#FFF3E6,#FFD9C2)}
.l{position:absolute;left:150px;top:0;bottom:0;width:900px;display:flex;flex-direction:column;justify-content:center}
.k{font-size:26px;font-weight:800;letter-spacing:.18em;color:#12808C}
.h{margin-top:24px;font-family:'New York',Georgia,serif;font-size:80px;font-weight:700;line-height:1.08;color:#2B1A12}
.s{margin-top:30px;font-size:38px;font-weight:600;line-height:1.35;color:#6B4A3A}
.ph{position:absolute;right:230px;top:40px;width:574px;height:1000px;border-radius:72px;background:#111;padding:14px;
box-shadow:0 40px 80px rgba(60,30,10,.35)} .ph div{width:100%;height:100%;border-radius:60px;overflow:hidden;background:#fff}
.ph img{width:100%;height:100%;object-fit:cover}"""


def card_png(seg_id, label, headline, sub, shot=None):
    screen = f"<img src='file://{shot}'>" if shot else ""
    body = (f"<div class='l'><div class='k'>{html.escape(label)}</div><div class='h'>{html.escape(headline)}</div>"
            f"<div class='s'>{html.escape(sub)}</div></div><div class='ph'><div>{screen}</div></div>")
    hp, png = os.path.join(B, seg_id + ".html"), os.path.join(B, seg_id + ".png")
    open(hp, "w").write(f"<html><head><meta charset='utf-8'><style>{CARD_CSS}</style></head><body>{body}</body></html>")
    subprocess.run([CHROME, "--headless=new", "--hide-scrollbars", "--force-device-scale-factor=1",
                    f"--window-size={W},{H}", "--screenshot=" + png, "file://" + hp],
                   check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    return png


def island_png():
    png = os.path.join(B, "island.png")
    if not os.path.exists(png):
        hp = os.path.join(B, "island.html")
        open(hp, "w").write("<html><body style='margin:0;background:transparent'>"
                            "<div style='width:130px;height:38px;border-radius:20px;background:#000'></div></body></html>")
        subprocess.run([CHROME, "--headless=new", "--hide-scrollbars", "--force-device-scale-factor=1",
                        "--default-background-color=00000000", "--window-size=130,38", "--screenshot=" + png, "file://" + hp],
                       check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    return png


def app_segment(seg_id, secs, label, headline, sub, shot, recording):
    """Card with the phone screen: a real recording if present, else a slow zoom on the screenshot."""
    out = os.path.join(B, seg_id + ".mp4")
    if recording:
        bg = card_png(seg_id, label, headline, sub, None)
        # screen area inside the phone frame: 546x972 at (1130,54), the same 750x1334 shape the iPhone records
        # (nothing is cropped), 60 px corners and a Dynamic Island, matching the lock-screen phone in motion/notify
        run(["-loop", "1", "-t", str(secs), "-i", bg, "-i", recording, "-loop", "1", "-i", island_png(), "-filter_complex",
             # the recorded status bar (09:41, icons clipped by the corners) is covered by the app's own background row
             f"[1:v]scale=546:972,split[r0][r1];[r1]crop=546:2:0:34,scale=546:34[bar];[r0][bar]overlay=0:0,format=rgba,"
             f"geq=r='r(X,Y)':g='g(X,Y)':b='b(X,Y)':a='if(gt(pow(max(0,abs(X-273)-213),2)+pow(max(0,abs(Y-486)-426),2),60*60),0,255)'[s];"
             f"[0:v][s]overlay=1130:54[p];[p][2:v]overlay=1338:64:shortest=1,fps={FPS},format=yuv420p[v]", "-map", "[v]", "-t", str(secs),
             "-c:v", "libx264", "-crf", "18", "-an", out])
    else:
        png = card_png(seg_id, label, headline, sub, shot)
        frames = int(secs * FPS)
        run(["-i", png, "-vf", f"scale={W*2}:{H*2},zoompan=z='1+0.03*on/{frames}':x='iw/2-(iw/zoom/2)':"
             f"y='ih/2-(ih/zoom/2)':d={frames}:s={W}x{H}:fps={FPS},format=yuv420p",
             "-frames:v", str(frames), "-c:v", "libx264", "-crf", "18", out])
    return out


def ai_segment(seg_id, src, secs, start=0.0, memory=False, delogo=None, speed=1.0, crop="iw*0.94:ih*0.94", hold=0.0, push=0.0):
    out = os.path.join(B, seg_id + ".mp4")
    grade = (",eq=saturation=0.78:gamma=1.05,colorbalance=rs=.08:gs=.03:bs=-.08,noise=alls=10:allf=t,vignette=PI/4.5"
             if memory else ",eq=saturation=1.04:contrast=1.03")
    # delogo hides third-party logos (e.g. a laptop brand); the 6% zoom crops Veo's corner watermark
    dl = f"delogo=x={delogo[0]}:y={delogo[1]}:w={delogo[2]}:h={delogo[3]}," if delogo else ""
    slow = f"setpts=PTS/{speed}," if speed != 1.0 else ""
    # hold: open on a still of the first frame; push: slow zoom-in across the whole shot
    slow += f"tpad=start_duration={hold}:start_mode=clone," if hold else ""
    zoom = (f",zoompan=z='1+{push}*on/{int(secs * FPS)}':d=1:x='iw/2-iw/zoom/2':y='ih/2-ih/zoom/2':s={W}x{H}:fps={FPS}"
            if push else "")
    run(["-ss", str(start), "-i", src, "-vf",
         f"{slow}{dl}crop={crop},scale={W}:{H}:flags=lanczos,fps={FPS}{zoom}{grade},format=yuv420p", "-t", str(secs), "-an", "-c:v", "libx264", "-crf", "18", out])
    return out


# ---- iOS-style notification banners laid over a story shot ----
BANNER_CSS = """
*{margin:0;box-sizing:border-box} html,body{background:transparent}
body{width:700px;height:200px;padding:24px 30px;font-family:-apple-system,'Helvetica Neue',sans-serif}
.b{display:flex;gap:22px;align-items:center;padding:22px 26px;border-radius:34px;background:rgba(246,244,242,.94);
box-shadow:0 14px 34px rgba(0,0,0,.28)}
.i{flex:none;width:78px;height:78px;border-radius:18px;display:flex;align-items:center;justify-content:center;font-size:46px}
.t{flex:1;min-width:0} .r{display:flex;justify-content:space-between;align-items:baseline}
.a{font-size:30px;font-weight:700;color:#111} .n{font-size:24px;color:#8a8580} .m{margin-top:4px;font-size:29px;color:#333}"""


def banner_png(name, icon, bg, title, msg, when="now"):
    """icon: an emoji, or a path to an image (e.g. the real Nagly app icon)."""
    if os.path.exists(str(icon)):
        icon = f"<img src='file://{os.path.abspath(icon)}' style='width:100%;height:100%;border-radius:18px'>"
    body = (f"<div class='b'><div class='i' style='background:{bg}'>{icon}</div><div class='t'><div class='r'>"
            f"<span class='a'>{html.escape(title)}</span><span class='n'>{when}</span></div>"
            f"<div class='m'>{html.escape(msg)}</div></div></div>")
    hp, png = os.path.join(B, name + ".html"), os.path.join(B, name + ".png")
    open(hp, "w").write(f"<html><head><meta charset='utf-8'><style>{BANNER_CSS}</style></head><body>{body}</body></html>")
    subprocess.run([CHROME, "--headless=new", "--hide-scrollbars", "--force-device-scale-factor=1",
                    "--default-background-color=00000000", "--window-size=700,200", "--screenshot=" + png, "file://" + hp],
                   check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    return png


def with_banners(seg_id, video, secs, banners):
    """Slide banners in from the right, stacking top-right: [(png, at_seconds[, swiped_at]), ...]."""
    out = os.path.join(B, seg_id + "_b.mp4")
    ins, fc, prev = ["-i", video], [], "[0:v]"
    for k, (png, at, *gone) in enumerate(banners, 1):
        ins += ["-loop", "1", "-t", str(secs), "-i", png]
        y = 40 + (k - 1) * 170
        fc.append(f"[{k}:v]format=rgba,fade=in:st={at}:d=0.2:alpha=1[p{k}]")
        x = f"W-720+760*pow(max(0,1-(t-{at})/0.35),3)"
        on = f"gte(t,{at})"
        if gone:  # flicked off to the right
            x += f"+1400*pow(max(0,t-{gone[0]})/0.3,2)"
            on = f"between(t,{at},{gone[0] + 0.3})"
        fc.append(f"{prev}[p{k}]overlay=x='{x}':y={y}:enable='{on}'[o{k}]")
        prev = f"[o{k}]"
    fc[-1] = fc[-1].rsplit("[", 1)[0] + "[v]"
    run(ins + ["-filter_complex", ";".join(fc), "-map", "[v]", "-t", str(secs), "-c:v", "libx264", "-crf", "18",
               "-pix_fmt", "yuv420p", out])
    return out


def concat(seg_id, parts):
    out = os.path.join(B, seg_id + ".mp4")
    ins = sum((["-i", x] for x in parts), [])
    run(ins + ["-filter_complex", "".join(f"[{i}:v]" for i in range(len(parts))) + f"concat=n={len(parts)}:v=1:a=0[v]",
               "-map", "[v]", "-c:v", "libx264", "-crf", "18", "-pix_fmt", "yuv420p", out])
    return out


def motion(name, secs):
    src = os.path.join(B, f"m_{name}.mp4")
    out = os.path.join(B, f"seg_{name}.mp4")
    run(["-i", src, "-t", str(secs), "-vf", f"fps={FPS},format=yuv420p", "-an", "-c:v", "libx264", "-crf", "18", out])
    return out


def main():
    os.makedirs(B, exist_ok=True)
    k3 = clip("K3", p("clips", "C.mp4"))
    # (video file, seconds, [(audio file, offset, gain)], subtitle)
    S = []
    # K1: only the first 3.5 s (after that he picks the phone up and it flips). Wide shot, then a closer cut
    # of the same moment; alarms pop up as he lists them and get swiped away on "swipe, swipe, swipe"
    wide = ai_segment("s01a", clip("K1"), 5.83, delogo=(333, 466, 66, 64), speed=0.6)
    close = ai_segment("s01b", clip("K1"), 4.0, start=0.3, delogo=(333, 466, 66, 64), speed=0.8, crop="1040:585:150:110")
    s01 = with_banners("s01", concat("s01", [wide, close]), 9.8, [
        (banner_png("bn_water", "⏰", "#FF9F0A", "Alarm", "Drink water"), 1.0, 8.5),
        (banner_png("bn_vit", "💊", "#FFFFFF", "Reminders", "Take Vitamin D"), 4.3, 7.5),
        (banner_png("bn_bp", "💊", "#FFFFFF", "Reminders", "BP tablet · 9:00 PM"), 5.3, 6.5)])
    S.append((s01, 9.8, [("n01", .3), ("n02", 2.8)],
              [(.3, 2.5, "I ignore every reminder on my phone."), (3.4, 6.2, "Water. Vitamins. My tablets."),
               (6.5, 9.4, "Swipe… swipe… swipe.")]))
    # K2: a still "photo" of home while he remembers, then it comes alive as she raises her finger;
    # cut at 5.5 s of the clip, before the glass goes back and forth
    S.append((ai_segment("s02", clip("K2"), 8.8, memory=True, hold=3.3, push=0.07), 8.8, [("n03", .1), ("a01", 3.4)],
              [(.1, 3.3, "But back home, there was one voice I never ignored."), (3.4, 6.2, "“Kanna, drink water first, then go.”")]))
    S.append((motion("title", 4.0), 4.0, [("chime", .15), ("n04", 1.1)], [(1.1, 3.8, "So now… my phone sounds like her.")]))
    S.append((app_segment("s04", 5.0, "01 · MAKE IT YOURS", "Pick who nags you.", "Mom, renamed to the real person: Subbu Amma.",
                          os.path.join(SHOTS, "5_make_it_yours.png"), rec("make_it_yours")), 5.0,
              [("n05", .4)], [(.4, 3.0, "Pick who nags you, and give them a real name.")]))
    S.append((motion("notify", 13.5), 13.5,
              [("n06", .2), ("chime", .8), ("chime", 3.2), ("chime", 5.6), ("n07", 7.3), ("chime", 11.4), ("a03", 11.6)],
              [(.2, 5.4, "Nagly plans nudges across my day, and every one is written in her voice."),
               (7.3, 9.4, "And I can answer right from the lock screen."), (11.6, 13.3, "“That's my child!”")]))
    # K3: the real Nagly nudge lands (same title format and Mom line the app sends), then Amma's voice;
    # he smiles, puts the phone down and sips from an open steel tumbler, like the one she handed him at home
    s06 = with_banners("s06", ai_segment("s06", k3, 7.8, delogo=(1163, 598, 56, 60)), 7.8, [
        (banner_png("bn_nagly", p("motion", "_shared", "icon.png"), "#fff", "👩 Subbu Amma", "Beta, sip some water."), 0.3, 3.0)])
    S.append((s06, 7.8, [("chime", .3), ("a02", .9)],
              [(.9, 5.8, "“Beta, sip some water. I'm not nagging… I'm caring.”")]))
    # tilt line dropped: the recording doesn't show the phone being tilted
    S.append((app_segment("s07", 4.4, "02 · SHE HAS MOODS", "Proud when you're on track.", "Worried when you're not.",
                          os.path.join(SHOTS, "1_home.png"), rec("home")), 4.4,
              [("n08_short", .2)], [(.2, 3.2, "She's proud when I'm on track. Worried when I'm not.")]))
    S.append((motion("voices", 8.0), 8.0, [("n09", 1.2)], [(1.2, 6.3, "Fifteen voices. Dad's puns, Nonna's guilt, your bestie's side-eye.")]))
    S.append((app_segment("s09", 6.5, "03 · NOT JUST WATER", "Pills, vitamins, creatine, protein.", "One reminder is free, forever.",
                          os.path.join(SHOTS, "4_supplements.png"), rec("supplements")), 6.5,
              [("n10", .4)], [(.4, 6.0, "Pills, vitamins, creatine, protein. One reminder is free, forever.")]))
    # K4: Amma's phone gets the real Dad-persona med nudge (renamed "Appa"), then she presses a tablet out of the strip and drinks from the glass beside her
    s10 = with_banners("s10", ai_segment("s10", clip("K4"), 7.8, delogo=(1163, 598, 56, 60)), 7.8, [
        (banner_png("bn_appa", p("motion", "_shared", "icon.png"), "#fff", "🧔 Appa", "Take your BP tablet, then we talk."), 0.3, 2.8)])
    S.append((s10, 7.8, [("chime", .3), ("n11", 1.2)], [(1.2, 4.4, "I set it up for Amma too. Appa's voice reminds her now.")]))
    S.append((app_segment("s11", 3.8, "04 · IT REMEMBERS", "History is a chat with your nagger.", "Every nudge, every sip, and the mood she was in.",
                          os.path.join(SHOTS, "6_history.png"), rec("history")), 3.8, [], []))
    S.append((motion("rc", 8.0), 8.0, [("n12", .5)], [(.5, 5.7, "Free to start. The paywall is personal, and RevenueCat picks the right plan for the moment.")]))
    S.append((motion("os", 8.5), 8.5, [("n13", .5), ("chime", 3.8)], [(.5, 5.4, "When I go quiet, OneSignal brings me back. Same voice. Her face on it.")]))
    S.append((motion("proof", 6.5), 6.5, [("n14", .6)], [(.6, 5.2, "Nagly is live on the App Store, and real people are already drinking more water.")]))
    # K5: video call with Amma; he toasts her with a clear glass of water and sips; her real app line plays from the call
    S.append((ai_segment("s15", clip("K5"), 7.8, delogo=(1163, 598, 56, 60)), 7.8, [("a03", 1.0), ("n15", 4.0)],
              [(1.0, 2.8, "“That's my child!”"), (4.0, 7.5, "People ignore alarms. They don't ignore their mom.")]))
    S.append((motion("end", 5.5), 5.5, [("chime", .2)], []))

    # absolute start times with crossfades
    starts, t = [], 0.0
    for i, s in enumerate(S):
        starts.append(t)
        t += s[1] - (XF if i < len(S) - 1 else 0)
    total = t
    print(f"total {total:.1f}s")

    # ---- video: xfade chain ----
    vin, fc, prev = [], [], "[0:v]"
    for s in S:
        vin += ["-i", s[0]]
    for i in range(1, len(S)):
        off = starts[i]
        fc.append(f"{prev}[{i}:v]xfade=transition=fade:duration={XF}:offset={off:.3f}[x{i}]")
        prev = f"[x{i}]"
    # subtitles (.ass)
    ass = os.path.join(B, "subs.ass")
    def ts(x):
        return f"{int(x//3600)}:{int(x%3600//60):02d}:{x%60:05.2f}"
    lines = ["[Script Info]", "ScriptType: v4.00+", f"PlayResX: {W}", f"PlayResY: {H}", "",
             "[V4+ Styles]", "Format: Name, Fontname, Fontsize, PrimaryColour, OutlineColour, BackColour, Bold, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV",
             "Style: S,Helvetica Neue,40,&H00FFFFFF,&H00000000,&H9E140C08,1,3,14,0,2,200,200,52", "",
             "[Events]", "Format: Layer, Start, End, Style, Text"]
    for st, s in zip(starts, S):
        for a, b, txt in s[3]:
            lines.append(f"Dialogue: 0,{ts(st+a)},{ts(st+b)},S,{txt}")
    open(ass, "w").write("\n".join(lines))
    fc.append(f"{prev}subtitles={ass}[v]")
    silent = os.path.join(B, "v2_video.mp4")
    if not (os.environ.get("AUDIO_ONLY") and os.path.exists(silent)):  # AUDIO_ONLY=1 reuses the rendered picture
        run(vin + ["-filter_complex", ";".join(fc), "-map", "[v]", "-c:v", "libx264", "-crf", "18", "-preset", "medium",
                   "-pix_fmt", "yuv420p", "-r", str(FPS), silent])

    # ---- audio: VO + SFX placed on the timeline, music ducked under voice ----
    MUSIC_VOL = float(os.environ.get("MUSIC_VOL", "0.14"))  # was 0.42; user asked for quieter music
    MASTER_GAIN = float(os.environ.get("MASTER_GAIN", "6"))  # fixed gain (loudnorm used to undo the music cut)
    ain, parts, idx = [], [], 0
    for st, s in zip(starts, S):
        for name, off in s[2]:
            f = p("sfx", "chime.wav") if name == "chime" else p("vo_lines", name + ".wav")
            gain = 0.55 if name == "chime" else (1.0 if name.startswith("n") else 1.08)
            ain += ["-i", f]
            ms = int((st + off) * 1000)
            parts.append(f"[{idx}:a]aformat=sample_rates=48000:channel_layouts=stereo,volume={gain},adelay={ms}|{ms}[v{idx}]")
            idx += 1
    voice_mix = "".join(f"[v{i}]" for i in range(idx)) + f"amix=inputs={idx}:normalize=0:duration=longest,apad=whole_dur={total:.2f}[voice]"
    music_i = idx
    ain += ["-i", p("music", "heartfelt_app_score.mp3")]
    fc = parts + [voice_mix, "[voice]asplit=2[vo1][vo2]",
                  f"[{music_i}:a]aformat=sample_rates=48000:channel_layouts=stereo,atrim=0:{total:.2f},volume={MUSIC_VOL},"
                  f"afade=t=in:d=1.5,afade=t=out:st={total-3:.2f}:d=3[mus]",
                  "[mus][vo2]sidechaincompress=threshold=0.03:ratio=8:attack=20:release=400[duck]",
                  f"[vo1][duck]amix=inputs=2:normalize=0,volume={MASTER_GAIN}dB,alimiter=limit=0.95[a]"]
    audio = os.path.join(B, "v2_audio.m4a")
    run(ain + ["-filter_complex", ";".join(fc), "-map", "[a]", "-t", f"{total:.2f}", "-c:a", "aac", "-b:a", "192k", audio])

    final = p("Nagly_demo_v2.mp4")
    run(["-i", silent, "-i", audio, "-map", "0:v", "-map", "1:a", "-c:v", "copy", "-c:a", "copy",
         "-shortest", "-movflags", "+faststart", final])
    print("done:", final)


if __name__ == "__main__":
    main()
