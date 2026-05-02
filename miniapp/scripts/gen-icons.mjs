import { writeFileSync } from 'fs'
import { deflateSync } from 'zlib'
import { join, dirname } from 'path'
import { fileURLToPath } from 'url'
import { createHash } from 'crypto'

const __dirname = dirname(fileURLToPath(import.meta.url))
const outDir = join(__dirname, '..', 'src', 'assets', 'tab')
const SIZE = 81
const CENTER = 40
const RADIUS = 30

function crc32(buf) {
  let crc = 0xffffffff
  for (let i = 0; i < buf.length; i++) {
    crc ^= buf[i]
    for (let j = 0; j < 8; j++) {
      crc = (crc >>> 1) ^ (crc & 1 ? 0xedb88320 : 0)
    }
  }
  return (crc ^ 0xffffffff) >>> 0
}

function chunk(type, data) {
  const len = Buffer.alloc(4)
  len.writeUInt32BE(data.length)
  const typeData = Buffer.concat([Buffer.from(type), data])
  const crcVal = Buffer.alloc(4)
  crcVal.writeUInt32BE(crc32(typeData))
  return Buffer.concat([len, typeData, crcVal])
}

function createPNG(pixels, width, height) {
  const sig = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10])

  const ihdr = Buffer.alloc(13)
  ihdr.writeUInt32BE(width, 0)
  ihdr.writeUInt32BE(height, 4)
  ihdr[8] = 8
  ihdr[9] = 6
  ihdr[10] = 0
  ihdr[11] = 0
  ihdr[12] = 0

  const raw = Buffer.alloc(height * (1 + width * 4))
  for (let y = 0; y < height; y++) {
    raw[y * (1 + width * 4)] = 0
    for (let x = 0; x < width; x++) {
      const si = (y * width + x) * 4
      const di = y * (1 + width * 4) + 1 + x * 4
      raw[di] = pixels[si]
      raw[di + 1] = pixels[si + 1]
      raw[di + 2] = pixels[si + 2]
      raw[di + 3] = pixels[si + 3]
    }
  }

  const compressed = deflateSync(raw)

  return Buffer.concat([
    sig,
    chunk('IHDR', ihdr),
    chunk('IDAT', compressed),
    chunk('IEND', Buffer.alloc(0)),
  ])
}

function dist(x1, y1, x2, y2) {
  return Math.sqrt((x1 - x2) ** 2 + (y1 - y2) ** 2)
}

function drawHome(color) {
  const pixels = Buffer.alloc(SIZE * SIZE * 4)
  for (let y = 0; y < SIZE; y++) {
    for (let x = 0; x < SIZE; x++) {
      const i = (y * SIZE + x) * 4
      let alpha = 0
      const cx = x - CENTER
      const cy = y - CENTER
      const inRoof = cy >= -28 && cy <= -5 && Math.abs(cx) <= (28 + cy) * 0.85
      const inBody = cy >= -5 && cy <= 25 && Math.abs(cx) <= 18
      const inDoor = cy >= 8 && cy <= 25 && Math.abs(cx) <= 8
      if (inRoof || (inBody && !inDoor)) alpha = 255
      pixels[i] = color[0]; pixels[i + 1] = color[1]; pixels[i + 2] = color[2]; pixels[i + 3] = alpha
    }
  }
  return pixels
}

function drawFood(color) {
  const pixels = Buffer.alloc(SIZE * SIZE * 4)
  for (let y = 0; y < SIZE; y++) {
    for (let x = 0; x < SIZE; x++) {
      const i = (y * SIZE + x) * 4
      let alpha = 0
      const cx = x - CENTER
      const cy = y - CENTER
      const inPlate = dist(cx, cy, 0, 5) <= 26
      const inInner = dist(cx, cy, 0, 5) <= 20
      const inChopstick1 = cx >= -12 && cx <= -9 && cy >= -30 && cy <= -5
      const inChopstick2 = cx >= 9 && cx <= 12 && cy >= -30 && cy <= -5
      if ((inPlate && !inInner) || inChopstick1 || inChopstick2) alpha = 255
      pixels[i] = color[0]; pixels[i + 1] = color[1]; pixels[i + 2] = color[2]; pixels[i + 3] = alpha
    }
  }
  return pixels
}

function drawCamera(color) {
  const pixels = Buffer.alloc(SIZE * SIZE * 4)
  for (let y = 0; y < SIZE; y++) {
    for (let x = 0; x < SIZE; x++) {
      const i = (y * SIZE + x) * 4
      let alpha = 0
      const cx = x - CENTER
      const cy = y - CENTER
      const inBody = cy >= -8 && cy <= 22 && Math.abs(cx) <= 28
      const inTop = cy >= -18 && cy <= -8 && cx >= -12 && cx <= 12
      const inLens = dist(cx, cy, 0, 7) <= 14
      const inLensInner = dist(cx, cy, 0, 7) <= 9
      if ((inBody || inTop) && !(inLens && !inLensInner)) alpha = 255
      if (inLensInner) alpha = 0
      pixels[i] = color[0]; pixels[i + 1] = color[1]; pixels[i + 2] = color[2]; pixels[i + 3] = alpha
    }
  }
  return pixels
}

function drawProfile(color) {
  const pixels = Buffer.alloc(SIZE * SIZE * 4)
  for (let y = 0; y < SIZE; y++) {
    for (let x = 0; x < SIZE; x++) {
      const i = (y * SIZE + x) * 4
      let alpha = 0
      const cx = x - CENTER
      const cy = y - CENTER
      const inHead = dist(cx, cy, 0, -12) <= 14
      const inBody = cy >= 6 && cy <= 30 && dist(cx, cy, 0, 6) <= 24
      if (inHead || inBody) alpha = 255
      pixels[i] = color[0]; pixels[i + 1] = color[1]; pixels[i + 2] = color[2]; pixels[i + 3] = alpha
    }
  }
  return pixels
}

const gray = [142, 142, 147]
const green = [67, 233, 123]

const icons = [
  { name: 'home', draw: drawHome },
  { name: 'food', draw: drawFood },
  { name: 'camera', draw: drawCamera },
  { name: 'profile', draw: drawProfile },
]

for (const icon of icons) {
  const normalPath = join(outDir, `${icon.name}.png`)
  const activePath = join(outDir, `${icon.name}-active.png`)
  const normalPixels = icon.draw(gray)
  const activePixels = icon.draw(green)
  writeFileSync(normalPath, createPNG(normalPixels, SIZE, SIZE))
  writeFileSync(activePath, createPNG(activePixels, SIZE, SIZE))
  console.log(`Generated ${icon.name}.png and ${icon.name}-active.png`)
}

console.log('All icons generated successfully!')
