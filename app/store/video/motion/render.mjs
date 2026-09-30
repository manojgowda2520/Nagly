// Renders a HyperFrames-style composition (paused GSAP timeline on window.__timelines[id])
// frame by frame with system Chrome and pipes JPEG frames into ffmpeg.
// Usage: node render.mjs <project-dir> <out.mp4> [fps]
import puppeteer from 'puppeteer-core';
import { spawn } from 'node:child_process';
import path from 'node:path';

const [dir, out, fpsArg] = process.argv.slice(2);
const fps = Number(fpsArg || 24);
const file = 'file://' + path.resolve(dir, 'index.html');

const browser = await puppeteer.launch({
  executablePath: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
  headless: 'new',
  protocolTimeout: 60000,
  args: ['--hide-scrollbars', '--force-device-scale-factor=1', '--allow-file-access-from-files'],
});
const page = await browser.newPage();
await page.setViewport({ width: 1920, height: 1080, deviceScaleFactor: 1 });
await page.goto(file, { waitUntil: 'load', timeout: 30000 });
await page.evaluate(() => document.fonts.ready.then(() => true));

const { id, duration } = await page.evaluate(() => {
  const root = document.querySelector('[data-composition-id]');
  return { id: root.dataset.compositionId, duration: Number(root.dataset.duration) };
});
const frames = Math.round(duration * fps);

const ff = spawn(process.env.FFMPEG || 'ffmpeg', [
  '-loglevel', 'error', '-y', '-f', 'image2pipe', '-framerate', String(fps), '-c:v', 'mjpeg', '-i', '-',
  '-c:v', 'libx264', '-pix_fmt', 'yuv420p', '-crf', '18', '-preset', 'medium', '-r', String(fps), out,
], { stdio: ['pipe', 'inherit', 'inherit'] });

for (let i = 0; i < frames; i++) {
  await page.evaluate((cid, t) => { window.__timelines[cid].seek(t, false); return true; }, id, i / fps);
  const buf = await page.screenshot({ type: 'jpeg', quality: 92 });
  if (!ff.stdin.write(buf)) await new Promise((r) => ff.stdin.once('drain', r));
}
ff.stdin.end();
await new Promise((r) => ff.on('close', r));
await browser.close();
console.log(`rendered ${id}: ${frames} frames -> ${out}`);
