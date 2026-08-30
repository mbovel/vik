// Runnable SMT-LIB snippets: every `smt2` code block of the site gets a Run button, and VIK — linked to
// WebAssembly into `js/vik/` by the `docsAssets` task of the build — answers it in the reader's browser.
// Each `check-sat` gets a fuel (a number of solver steps), so a snippet cannot hang the page.
"use strict"
;(() => {
  const fuel = 20000000
  const script = document.currentScript || document.querySelector('script[src$="js/snippets.js"]')
  const base = new URL(script.getAttribute("src"), document.baseURI)
  let solver = null

  /** The module, imported at the first run: 800 kB of WebAssembly, not on the way of the page. */
  async function load() {
    if (solver === null) solver = await import(new URL("vik/main.js", base).href)
    return solver
  }

  for (const code of document.querySelectorAll("code.language-smt2")) {
    const snippet = code.closest(".snippet") || code.parentElement
    const buttons = snippet.querySelector(".buttons") || snippet.appendChild(document.createElement("div"))
    const output = document.createElement("pre")
    output.className = "snippet-output"
    output.hidden = true
    snippet.after(output)

    code.setAttribute("contenteditable", "true") // the reader can change the script before running it
    code.setAttribute("spellcheck", "false")

    const button = document.createElement("button")
    button.className = "snippet-run"
    button.textContent = "Run"
    button.title = "Solve this script here, in your browser"
    buttons.append(button)

    button.addEventListener("click", async () => {
      button.disabled = true
      output.hidden = false
      output.textContent = "solving..."
      try {
        const vik = await load()
        const start = performance.now()
        const answers = vik.solve(code.innerText, fuel)
        output.textContent = `${answers}\n(${Math.round(performance.now() - start)} ms)`
      } catch (e) {
        output.textContent = `the solver failed: ${e}`
      } finally {
        button.disabled = false
      }
    })
  }
})()
