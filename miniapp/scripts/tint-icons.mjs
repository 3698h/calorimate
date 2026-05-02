import sharp from 'sharp'
import { join, dirname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const tabDir = join(__dirname, '..', 'src', 'assets', 'tab')

async function tintIcon(name) {
  const input = join(tabDir, `${name}.png`)
  const output = join(tabDir, `${name}-active.png`)

  const { data, info } = await sharp(input)
    .ensureAlpha()
    .raw()
    .toBuffer({ resolveWithObject: true })

  const pixels = Buffer.from(data)
  for (let i = 0; i < pixels.length; i += 4) {
    const r = pixels[i], g = pixels[i + 1], b = pixels[i + 2], a = pixels[i + 3]
    if (a === 0) continue
    const gray = Math.round(r * 0.299 + g * 0.587 + b * 0.114)
    pixels[i] = Math.round(67 * gray / 255)
    pixels[i + 1] = Math.round(233 * gray / 255)
    pixels[i + 2] = Math.round(123 * gray / 255)
  }

  await sharp(pixels, { raw: { width: info.width, height: info.height, channels: 4 } })
    .png()
    .toFile(output)

  console.log(`${name}.png -> ${name}-active.png`)
}

const icons = ['home', 'food', 'camera', 'profile']
for (const name of icons) {
  await tintIcon(name)
}
console.log('Done!')
