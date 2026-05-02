import { copyFileSync, readdirSync } from 'fs'
import { join, dirname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const iconsDir = join(__dirname, '..', 'src', 'assets', 'icons')
const tabDir = join(__dirname, '..', 'src', 'assets', 'tab')

const files = readdirSync(iconsDir).filter(f => f.endsWith('.png'))
for (const file of files) {
  const src = join(iconsDir, file)
  const name = file.replace('.png', '')
  copyFileSync(src, join(tabDir, `${name}.png`))
  copyFileSync(src, join(tabDir, `${name}-active.png`))
  console.log(`Copied ${file} -> ${name}.png & ${name}-active.png`)
}

console.log('Done!')
