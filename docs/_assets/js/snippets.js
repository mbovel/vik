// Runnable SMT-LIB snippets: every `smt2` code block of the site gets a Run button, and VIK — linked to
// WebAssembly into `js/vik/` by the `docsAssets` task of the build — answers it in the reader's browser.
// The blocks are editable, and each `check-sat` gets a fuel (a number of solver steps), so a snippet cannot
// hang the page.
"use strict"
;(() => {
  const fuel = 20000000
  const script = document.currentScript || document.querySelector('script[src$="js/snippets.js"]')
  const base = new URL(script.getAttribute("src"), document.baseURI)
  let solver = null

  /** The module, imported at the first run: 780 kB of WebAssembly, not on the way of the page. */
  async function load() {
    if (solver === null) solver = await import(new URL("vik/main.js", base).href)
    return solver
  }

  /** A code block of the site's own shape, for the answers of a run. */
  function answers() {
    const block = document.createElement("div")
    block.className = "snippet mono-small-block snippet-answers"
    block.hidden = true
    const code = document.createElement("code") // no `language-smt2`: the answers are not a script to run
    const pre = document.createElement("pre")
    pre.append(code)
    block.append(pre)
    return [block, code]
  }

  function decorate(code) {
    const snippet = code.closest(".snippet") || code.parentElement
    const [block, output] = answers()
    snippet.after(block)

    // The lines of the site's renderer are spans, which an edit would tear apart: plain text instead.
    code.textContent = code.textContent
    code.setAttribute("contenteditable", "true")
    code.setAttribute("spellcheck", "false")

    const button = document.createElement("button")
    button.className = "snippet-run"
    button.title = "Run this script in your browser"
    button.setAttribute("aria-label", "Run")
    snippet.append(button)

    button.addEventListener("click", async () => {
      button.disabled = true
      block.hidden = false
      output.textContent = "solving..."
      try {
        const vik = await load()
        const start = performance.now()
        output.textContent = `${vik.solve(code.textContent, fuel)}\n(${Math.round(performance.now() - start)} ms)`
      } catch (e) {
        output.textContent = `the solver failed: ${e}`
      } finally {
        button.disabled = false
      }
    })
  }

  // After the page's own scripts, which highlight the blocks and add their buttons.
  window.addEventListener("load", () => document.querySelectorAll("code.language-smt2").forEach(decorate))
})()
