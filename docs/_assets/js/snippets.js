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

  /** The module, imported at the first run, not on the way of the page: the solver as WebAssembly, or as
   *  JavaScript for the engines that do not have Wasm 3.0 yet (Safari before 26, and iOS with it).
   */
  async function load() {
    if (solver === null) {
      try {
        solver = await import(new URL("vik/main.js", base).href)
      } catch (wasm) {
        solver = await import(new URL("vik-js/main.js", base).href)
      }
    }
    return solver
  }

  /** SMT-LIB for highlight.js, which the site already loads and themes: commands and sorts as keywords, `:key`
   *  as symbols, `;` comments, numerals and bit-vector literals as numbers.
   */
  function language(hljs) {
    return {
      name: "SMT-LIB",
      keywords: {
        // Words are hyphenated here (`declare-const`), which the default pattern of highlight.js cuts.
        $pattern: /[a-zA-Z][\w-]*/,
        keyword:
          "set-logic set-info set-option get-info get-model declare-sort declare-fun declare-const define-fun " +
          "declare-datatype declare-datatypes assert check-sat check-sat-assuming push pop reset " +
          "reset-assertions exit lambda let ite forall exists",
        built_in:
          "Bool Int Real BitVec and or not xor distinct div mod abs concat extract bvadd bvsub bvneg bvnot " +
          "bvmul bvudiv bvurem bvshl bvlshr bvult bvule bvugt bvuge bvslt bvsle bvsgt bvsge zero_extend " +
          "sign_extend is",
        literal: "true false"
      },
      contains: [
        hljs.COMMENT(";", "$"),
        hljs.QUOTE_STRING_MODE,
        { className: "symbol", begin: /:[a-zA-Z][\w-]*/ },
        { className: "number", begin: /#[xb][0-9a-fA-F]+|\b\d+\b/ }
      ]
    }
  }

  /** The script, coloured; the text alone while it is edited, so the caret stays where the reader put it. */
  function highlight(code) {
    const hljs = window.hljs
    if (hljs === undefined) return
    if (hljs.getLanguage("smt2") === undefined) hljs.registerLanguage("smt2", language)
    // The site ships highlight.js 10, whose `highlight` takes the language first; `highlightBlock` reads the
    // class of the element instead, and is the one both 10 and 11 understand.
    code.removeAttribute("data-highlighted")
    hljs.highlightBlock(code)
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

    // The lines of the site's renderer are spans, which an edit would tear apart: our own colours instead.
    code.textContent = code.textContent
    highlight(code)
    code.setAttribute("contenteditable", "true")
    code.setAttribute("spellcheck", "false")
    code.addEventListener("focus", () => (code.textContent = code.textContent))
    code.addEventListener("blur", () => highlight(code))

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
