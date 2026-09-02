"""Splices freshly generated tables into docs/_docs/evaluation.md.

Usage: python3 bench/splice.py warm.md cold.md
Either argument may be `-` to leave that table as it is. The warm chunk carries
its confidence-interval note; the page's existing note is replaced with it.
"""
import pathlib
import sys

OPEN, CLOSE = '<div class="table-scroll">', "</div>"
NOTE = "The 95% confidence intervals"


def region(text, index):
    """The bounds of the index-th generated table."""
    start = -1
    for _ in range(index + 1):
        start = text.index(OPEN, start + 1)
    return start, text.index(CLOSE, start) + len(CLOSE)


def main(warm_path, cold_path):
    page = pathlib.Path("docs/_docs/evaluation.md")
    text = page.read_text()
    if cold_path != "-":
        s, e = region(text, 1)
        text = text[:s] + pathlib.Path(cold_path).read_text().strip() + text[e:]
    if warm_path != "-":
        note_start = text.index(NOTE)
        note_end = text.index("\n\n", note_start)
        text = text[:note_start] + text[note_end:].lstrip("\n")  # the note and the blank lines after it
        s, e = region(text, 0)
        text = text[:s] + pathlib.Path(warm_path).read_text().strip() + "\n\n" + text[e:].lstrip("\n")
    assert text.count(OPEN) == 2, "two generated tables expected"
    assert text.count(NOTE) == 1, "one confidence note expected"
    page.write_text(text)
    print("spliced")


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
