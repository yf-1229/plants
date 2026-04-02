const { spawnSync } = require('node:child_process');
const fs = require('node:fs');
const path = require('node:path');

const repoRoot = path.resolve(__dirname, '..');
const tsconfigPath = path.join(repoRoot, 'tsconfig.json');
const tscPath = path.join(repoRoot, 'node_modules', 'typescript', 'bin', 'tsc');

if (!fs.existsSync(tsconfigPath)) {
  console.error(`tsconfig not found: ${tsconfigPath}`);
  process.exit(1);
}

if (!fs.existsSync(tscPath)) {
  console.error('typescript is not installed. Run npm install first.');
  process.exit(1);
}

const args = [tscPath, '-p', tsconfigPath, ...process.argv.slice(2)];
const result = spawnSync(process.execPath, args, {
  cwd: repoRoot,
  stdio: 'inherit'
});

process.exit(result.status ?? 1);
