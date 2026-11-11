// The chart of the evaluation page: its two tables, drawn. A group of bars per instance, a bar per solver, on
// a logarithmic axis; selectors keep the warm or the cold table, the instances whose logic names a theory
// and the instances of one origin, and say whether a bar is a time or the ratio of a time to VIK's. The
// times are read off the tables themselves — bench/warm2md.py and bench/cold2md.py write each one into its
// cell, unrounded — so the chart cannot say anything else than the tables do. No library: a bar is a box
// placed in percent, and the colours are the stylesheet's (css/site.css), so the chart follows the theme of
// the site.
"use strict"
;(() => {
  const root = document.getElementById("evaluation-chart")
  if (root === null || document.getElementById("warm-times") === null) return

  const budget = 10 // seconds: no measurement runs past it, and a solver stopped there has a bar to the edge
  const origins = { dotty: "Dotty", stainless: "Stainless", others: "Others" } // `corpus/<logic>/<origin>/`
  const named = {
    A: "arrays",
    UF: "uninterpreted functions",
    BV: "bit-vectors",
    DT: "datatypes",
    LIA: "linear integer arithmetic",
    LRA: "linear real arithmetic"
  }

  function element(tag, className = "", text = "") {
    const e = document.createElement(tag)
    e.className = className
    e.textContent = text
    return e
  }

  /** The theories a logic names: `QF_AUFLIA` is arrays, functions and integers, and `QF_AX` is arrays too. */
  function theories(logic) {
    const atoms = logic.replace(/^QF_/, "").match(/AX?|UF|BV|FP|DT|S|LIRA|LIA|LRA|NIRA|NIA|NRA|IDL|RDL|.+/g) || []
    return atoms.map((atom) => (atom === "AX" ? "A" : atom))
  }

  /** A table of the page as data: the names of its solver columns and, for each instance, its name, its
   *  corpus file, its logic, its origin and its cells — seconds, or the text of a cell that has no time.
   */
  function read(id) {
    const table = document.getElementById(id)
    const solvers = (row) => [...row.cells].slice(4) // after the instance, its theory, incremental and status
    const instances = [...table.tBodies[0].rows].map((row) => {
      const link = row.cells[0].querySelector("a")
      const origin = link === null ? null : link.getAttribute("href").match(/\/corpus\/[^/]+\/([^/]+)\//)
      const logic = row.cells[1].textContent
      return {
        name: link === null ? row.cells[0].textContent : link.title,
        href: link === null ? null : link.href,
        logic,
        theories: theories(logic),
        origin: origin !== null && origin[1] in origins ? origin[1] : "others",
        status: row.cells[3].textContent,
        times: solvers(row).map((td) => ("seconds" in td.dataset ? Number(td.dataset.seconds) : td.textContent))
      }
    })
    return { solvers: solvers(table.tHead.rows[0]).map((th) => th.textContent), instances }
  }

  const digits = (x) => Number(x.toPrecision(3))
  const timed = (t) => typeof t === "number"

  /** A time in the unit that suits it, to three digits: `6.12 µs`, `675 µs`, `21.6 ms`, `1.24 s`. */
  function time(seconds) {
    const [per, unit] = seconds < 0.9995e-3 ? [1e6, "µs"] : seconds < 0.9995 ? [1e3, "ms"] : [1, "s"]
    return `${digits(seconds * per)} ${unit}`
  }

  /** A time against the reference's, in words: `12.3× slower`, `2630× faster`. Where one of the two is
   *  past the budget, the budget stands for it and the ratio is a bound: `over 40× faster`.
   */
  function against(t, reference) {
    if (timed(t) && timed(reference))
      return t >= reference ? `${digits(t / reference)}× slower` : `${digits(reference / t)}× faster`
    if (timed(t) && reference === "🐢") return `over ${digits(budget / t)}× faster`
    if (t === "🐢" && timed(reference)) return `over ${digits(budget / reference)}× slower`
    return ""
  }

  /** What counts as one on an instance: a second or, given the column of a reference, the reference's time
   *  there. The budget stands for a reference past it: the ratios of the others are then bounds, each solver
   *  at least that much faster.
   */
  function unit(instance, reference) {
    const t = reference === null ? 1 : instance.times[reference]
    return t === "🐢" ? budget : t
  }

  /** What the axis measures, in whole decades, and where the bars start on it. Seconds: from the decade of
   *  the table's fastest time up to the budget, the bars starting at the left edge. Or, given the column of
   *  a reference, the ratio of each time to the reference's on the same instance: from the smallest of the
   *  table to the largest, the bars starting at 1× and running left where a solver is faster. Either is the
   *  table's, not the selection's: the bars of an instance stay where they are from a choice to the next.
   */
  function scale({ instances }, reference) {
    const relative = reference !== null
    const values = instances.flatMap((i) => {
      const one = unit(i, reference)
      return timed(one) ? i.times.filter(timed).map((t) => t / one) : []
    })
    const from = Math.floor(Math.log10(Math.min(1, ...values)))
    const to = relative ? Math.max(from + 1, Math.ceil(Math.log10(Math.max(1, ...values)))) : Math.log10(budget)
    const place = (value) => (100 * (Math.min(to, Math.max(from, Math.log10(value))) - from)) / (to - from) // percent
    // A ratio is written out up to a thousand either way (`0.001×`, `1000×`), and as a power of ten past it.
    const power = (decade) => `10${String(decade).replace(/./g, (c) => "⁻⁰¹²³⁴⁵⁶⁷⁸⁹"["-0123456789".indexOf(c)])}×`
    const ratio = (decade) => (Math.abs(decade) > 3 ? power(decade) : `${Number((10 ** decade).toPrecision(1))}×`)
    const label = (decade) => (relative ? ratio(decade) : time(10 ** decade))
    return { from, to, place, label, base: relative ? place(1) : 0 }
  }

  /** The colour of each column, by the solver its name starts with: a hue per solver, the same in both
   *  tables, and the platforms of VIK, where a table has several, as shades of VIK's (css/site.css).
   */
  function colours(solvers) {
    const viks = solvers.filter((s) => s.startsWith("VIK"))
    return solvers.map((s) => {
      const solver = s.split(" ")[0].toLowerCase().replace(/[^a-z0-9]/g, "")
      return `var(--${viks.length > 1 && viks.includes(s) ? `vik-${viks.indexOf(s) + 1}` : solver}, var(--other))`
    })
  }

  const tables = { warm: read("warm-times"), cold: read("cold-times") }
  const state = { table: "warm", times: "absolute", theory: "all", origin: "all", hidden: new Set() }

  /** The column the relative times are relative to: of VIK's, the last whose bars are shown. The cold table
   *  lists VIK's platforms from the slowest to start to the fastest — the JVM, its AOT cache, Node, the
   *  Scala Native binary, the native image (bench/cold2md.py) — so that is the fastest VIK in sight.
   */
  function reference() {
    const { solvers } = tables[state.table]
    const viks = solvers.map((s, k) => k).filter((k) => solvers[k].startsWith("VIK"))
    if (state.times === "absolute" || viks.length === 0) return null
    return viks.findLast((k) => !state.hidden.has(solvers[k])) ?? viks.at(-1)
  }

  const controls = element("div", "chart-controls")
  const legend = element("div", "chart-legend")
  const ends = element("div", "chart-ends") // which way is better, over the two ends of the axis
  const axis = element("div", "chart-axis")
  const head = element("div", "chart-head")
  const plot = element("div", "chart-plot")
  const count = element("p", "chart-count")
  const tip = element("div", "chart-tip")
  tip.hidden = true
  axis.setAttribute("aria-hidden", "true")
  head.append(legend, ends, axis)

  /** A row of exclusive choices for one key of the state: radio buttons, which the keyboard knows. */
  function choice(key, title, options) {
    const group = element("div", "chart-choice")
    group.setAttribute("role", "radiogroup")
    group.setAttribute("aria-label", title)
    group.append(element("span", "chart-choice-title", title))
    for (const [value, text, hint = ""] of options) {
      const label = element("label", "", text)
      const input = element("input")
      input.type = "radio"
      input.name = `evaluation-${key}`
      input.checked = state[key] === value
      input.addEventListener("change", () => {
        state[key] = value
        draw()
      })
      label.title = hint
      label.prepend(input)
      group.append(label)
    }
    return group
  }

  /** The times of an instance, every solver shown, in a box of its own: at a place given in the window. */
  function tell(instance, x, y) {
    const { solvers } = tables[state.table]
    const colour = colours(solvers)
    const one = reference()
    const times = element("div", "chart-tip-times")
    instance.times.forEach((t, k) => {
      if (state.hidden.has(solvers[k])) return
      const key = element("i")
      key.style.setProperty("--colour", colour[k])
      // The value the bars show is the one in bold: the time, or its ratio with the time beside it.
      const value = timed(t) ? time(t) : t === "🐢" ? `past ${budget} s` : t
      times.append(key, element("span", "", solvers[k]), element(one === null ? "b" : "span", "chart-tip-time", value))
      if (one !== null) times.append(element("b", "", k === one ? "1×" : against(t, instance.times[one])))
    })
    times.classList.toggle("chart-tip-relative", one !== null)
    tip.replaceChildren(
      element("strong", "", instance.name),
      element("span", "chart-tip-about", `${instance.logic} · ${instance.status}`),
      times
    )
    tip.hidden = false
    // Right of the place, or left of it where the window ends; below it in the upper half of the window
    // and above it in the lower half, clear of the bars the site keeps at the top and at the bottom. The
    // box is positioned in the chart, whose corner is the origin of its coordinates.
    const here = root.getBoundingClientRect()
    const { width, height } = tip.getBoundingClientRect()
    const left = x + 16 + width > window.innerWidth ? Math.max(0, x - 16 - width) : x + 16
    const top = y > window.innerHeight / 2 ? Math.max(0, y - 16 - height) : y + 16
    tip.style.left = `${left - here.left}px`
    tip.style.top = `${top - here.top}px`
  }

  function draw() {
    const { solvers, instances } = tables[state.table]
    const colour = colours(solvers)
    const shown = instances.filter(
      (i) =>
        (state.theory === "all" || i.theories.includes(state.theory)) &&
        (state.origin === "all" || i.origin === state.origin)
    )

    // The legend: a solver's name hides and shows its bars. Then what the two marks without a time mean.
    const keys = solvers.map((s, k) => {
      const key = element("button", "chart-key")
      key.type = "button"
      key.title = "Hide or show these bars"
      key.style.setProperty("--colour", colour[k])
      key.addEventListener("click", () => {
        if (!state.hidden.delete(s)) state.hidden.add(s)
        rows()
      })
      return key
    })
    const marks = element("span", "chart-marks")
    marks.append(
      element("span", "chart-key chart-key-timeout", `past the ${budget} s budget`),
      element("span", "chart-key chart-key-none", "no time")
    )
    legend.replaceChildren(...keys, marks)

    /** The axis and the bars. The legend is not made again here: a key keeps the focus that pressed it. */
    function rows() {
      const one = reference()
      const { from, to, place, label, base } = scale(tables[state.table], one)
      keys.forEach((key, k) => {
        key.textContent = k === one ? `${solvers[k]} = 1×` : solvers[k]
        key.setAttribute("aria-pressed", String(!state.hidden.has(solvers[k])))
      })
      const ticks = Array.from({ length: to - from + 1 }, (_, k) => {
        // Every other label goes on a narrow page (css/site.css): the odd decades, which keeps 1×.
        const tick = element("span", `chart-tick${(from + k) % 2 === 0 ? "" : " chart-tick-odd"}`, label(from + k))
        tick.style.left = `${(100 * k) / (to - from)}%`
        return tick
      })
      axis.replaceChildren(...ticks)
      const than = one === null ? "" : " than VIK"
      ends.replaceChildren(element("span", "", `← faster${than}`), element("span", "", `slower${than} →`))
      plot.style.setProperty("--decades", to - from)
      plot.style.setProperty("--base", base / 100)

      const groups = shown.map((instance) => {
        const row = element("div", "chart-row")
        const name = element("div", "chart-name")
        // The name in two parts, for the stylesheet to cut the first where the column ends and keep the
        // second whole: the family is at the start of a name, and the file at its end.
        const link = element(instance.href === null ? "span" : "a", "chart-instance")
        if (instance.href !== null) link.href = instance.href
        link.append(element("span", "", instance.name.slice(0, -12)), element("span", "", instance.name.slice(-12)))
        name.append(link, element("small", "", instance.logic))
        const bars = element("div", "chart-bars")
        const per = unit(instance, one)
        instance.times.forEach((t, k) => {
          if (state.hidden.has(solvers[k])) return
          // Where the bar ends, in percent of the axis: at its time, or at its ratio to the reference's. A
          // solver past the budget has a hatched bar to the right edge, the reference too; and where there
          // is no time, the solver's or the reference's, there is nothing to draw but a dash.
          const end = t === "🐢" ? 100 : timed(t) && timed(per) ? place(t / per) : null
          const bar = element("div", end === null ? "chart-none" : timed(t) ? "chart-bar" : "chart-bar chart-timeout")
          bar.style.setProperty("--colour", colour[k])
          bar.style.marginLeft = `${Math.min(base, end ?? base)}%`
          if (end !== null) bar.style.width = `${Math.abs(end - base)}%`
          if (end !== null && end < base) bar.classList.add("chart-faster")
          bars.append(bar)
        })
        row.append(name, bars)
        // The mouse events, which a touch screen also sends on a tap, and the focus of the keyboard.
        // Entering counts as moving: a page that scrolls under a still pointer brings it a row without a move.
        for (const type of ["mouseenter", "mousemove"])
          row.addEventListener(type, (e) => tell(instance, e.clientX, e.clientY))
        row.addEventListener("mouseleave", () => (tip.hidden = true))
        row.addEventListener("focusin", () => {
          const box = bars.getBoundingClientRect()
          tell(instance, box.left, box.bottom - 12)
        })
        row.addEventListener("focusout", () => (tip.hidden = true))
        return row
      })
      tip.hidden = true
      const none = element("p", "chart-empty", "No instance of the evaluation is in this selection.")
      plot.replaceChildren(...(groups.length === 0 ? [none] : groups))
      count.textContent =
        `${shown.length} of the ${instances.length} instances.` +
        (one === null
          ? ""
          : ` Each time is divided by the time of ${solvers[one]} on the same instance: a bar to the left of` +
            " 1× is a faster solver, a bar to the right a slower one.")
    }
    rows()
  }

  const everyTheory = [...new Set(tables.warm.instances.flatMap((i) => i.theories))]
  controls.append(
    choice("table", "Measurement", [
      ["warm", "Warm", "the solver as a library, in a running JVM"],
      ["cold", "Cold", "a process per question"]
    ]),
    choice("times", "Times", [
      ["absolute", "Absolute"],
      ["relative", "Relative to VIK", "each time divided by VIK's on the same instance"]
    ]),
    choice("theory", "Theory", [["all", "All"], ...everyTheory.map((t) => [t, t, named[t]])]),
    choice("origin", "Instances", [["all", "All"], ...Object.entries(origins)])
  )
  root.replaceChildren(controls, head, plot, count, tip)
  draw()
})()
