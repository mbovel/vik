// VIK on Node.js for the cold-start comparison: the WebAssembly module of the documentation site
// (`docs/_assets/js/vik/main.js`, built by the `docsAssets` task), run on one SMT-LIB file.
import { readFileSync } from "node:fs"
import { pathToFileURL } from "node:url"

const module = pathToFileURL(process.argv[3] ?? "docs/_assets/js/vik/main.js").href
const vik = await import(module)
console.log(vik.solve(readFileSync(process.argv[2], "utf8"), Number.MAX_SAFE_INTEGER))
