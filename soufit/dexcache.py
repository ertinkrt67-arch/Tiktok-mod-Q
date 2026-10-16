
from __future__ import annotations

import hashlib
import json
import os
import shutil
import subprocess
import time
from concurrent.futures import ProcessPoolExecutor, as_completed
from typing import Callable, Dict, List, Optional, Tuple

from . import dexpatch
from .dexpatch import Smali

SHAPE = "3"

KEEP_DAYS = 30

HEAP = "2g"
ROOM_PER_JOB = 2.5


def folder() -> str:
    return os.environ.get("SOUFIT_CACHE") or os.path.join(
        os.path.expanduser("~"), ".cache", "soufit-dex")


def _sha(*parts: bytes) -> str:
    digest = hashlib.sha256()
    for part in parts:
        digest.update(len(part).to_bytes(8, "big"))
        digest.update(part)
    return digest.hexdigest()


def _stamp(path: str) -> bytes:
    try:
        seen = os.stat(path)
        return ("%s:%d:%d" % (os.path.basename(path), seen.st_size,
                              seen.st_mtime_ns)).encode()
    except OSError:
        return b"?"


def _touch(path: str) -> None:
    try:
        os.utime(path, None)
    except OSError:
        pass


def workers(jobs: int) -> int:
    asked = os.environ.get("SOUFIT_WORKERS")
    if asked:
        return max(1, min(int(asked), jobs or 1))
    free = 4.0
    try:
        with open("/proc/meminfo", encoding="ascii") as handle:
            for line in handle:
                if line.startswith("MemAvailable:"):
                    free = int(line.split()[1]) / (1024.0 * 1024.0)
                    break
    except (OSError, ValueError):
        pass
    by_memory = int(free // ROOM_PER_JOB)
    by_cores = (os.cpu_count() or 2) // 2
    return max(1, min(by_memory, by_cores, 6, jobs or 1))


def prune(root: Optional[str] = None) -> None:
    root = root or folder()
    limit = time.time() - KEEP_DAYS * 86400
    try:
        names = os.listdir(root)
    except OSError:
        return
    for name in names:
        path = os.path.join(root, name)
        try:
            if name == "pieces":
                for tree in os.listdir(path):
                    whole = os.path.join(path, tree)
                    mark = os.path.join(whole, "whole")
                    if not os.path.exists(mark) or os.path.getmtime(mark) < limit:
                        if os.path.getmtime(whole) < time.time() - 3600:
                            shutil.rmtree(whole, ignore_errors=True)
            elif os.path.isfile(path) and os.path.getmtime(path) < limit:
                os.remove(path)
        except OSError:
            pass


def _pieces(dex: bytes, dex_sha: str, smali: Smali, root: str, scratch: str) -> str:
    home = os.path.join(root, "pieces", "%s-a%d" % (dex_sha[:40], smali.api))
    mark = os.path.join(home, "whole")
    tree = os.path.join(home, "smali")
    if os.path.exists(mark):
        _touch(mark)
        return tree

    making = "%s.%d" % (home, os.getpid())
    shutil.rmtree(making, ignore_errors=True)
    os.makedirs(making, exist_ok=True)
    dex_in = os.path.join(scratch, "in.dex")
    with open(dex_in, "wb") as handle:
        handle.write(dex)
    smali.disassemble(dex_in, os.path.join(making, "smali"))
    with open(os.path.join(making, "whole"), "w") as handle:
        handle.write("")
    shutil.rmtree(home, ignore_errors=True)
    try:
        os.rename(making, home)
    except OSError:
        shutil.rmtree(making, ignore_errors=True)
        if not os.path.exists(mark):
            raise
    return tree


def _copy(tree: str, into: str) -> None:
    shutil.rmtree(into, ignore_errors=True)
    os.makedirs(os.path.dirname(into), exist_ok=True)
    done = subprocess.run(["cp", "-a", "--reflink=auto", tree, into],
                          capture_output=True, text=True)
    if done.returncode != 0:
        shutil.rmtree(into, ignore_errors=True)
        shutil.copytree(tree, into)


def _rewrite(root: str, literals: Dict[str, str],
             dex: Optional[bytes] = None) -> Tuple[Dict[str, int], List[str]]:
    written = set()
    real = open

    def watched(path, mode="r", *rest, **named):
        if any(letter in mode for letter in "wax+"):
            written.add(os.path.abspath(path))
        return real(path, mode, *rest, **named)

    dexpatch.open = watched
    try:
        counts = dexpatch.rewrite_everything(root, literals, dex)
    finally:
        del dexpatch.open
    return counts, sorted(written)


def _outcome(dex_sha: str, smali: Smali, tree: str, room: str,
             written: List[str]) -> str:
    parts = [SHAPE.encode(), dex_sha.encode(), str(smali.api).encode(),
             _stamp(smali.jar)]
    for path in written:
        inside = os.path.relpath(path, room)
        with open(path, "rb") as handle:
            now = handle.read()
        try:
            with open(os.path.join(tree, inside), "rb") as handle:
                if handle.read() == now:
                    continue
        except OSError:
            pass
        parts.append(inside.encode("utf-8"))
        parts.append(hashlib.sha256(now).digest())
    return _sha(*parts)


def _save(path: str, data: bytes) -> None:
    part = "%s.%d.part" % (path, os.getpid())
    with open(part, "wb") as handle:
        handle.write(data)
    os.replace(part, path)


def one(job: dict) -> Tuple[str, Optional[str], Dict[str, int], str]:
    name = job["name"]
    dex = job["dex"]
    literals = job["literals"] or {}
    root = job["root"]
    smali = Smali(job["jar"], job["api"], job["jobs"], job["heap"])
    dexpatch.FOUND = dict(job["found"])
    os.makedirs(root, exist_ok=True)

    dex_sha = hashlib.sha256(dex).hexdigest()
    with open(dexpatch.__file__, "rb") as handle:
        rules = handle.read()
    answer = _sha(SHAPE.encode(), dex_sha.encode(), rules, _stamp(smali.jar),
                  repr((smali.api, sorted(literals.items()),
                        sorted(dexpatch.FOUND.items()))).encode("utf-8"))
    answer_path = os.path.join(root, "a-%s.json" % answer)
    try:
        with open(answer_path, encoding="utf-8") as handle:
            said = json.load(handle)
        counts = said["counts"]
        if not counts:
            _touch(answer_path)
            return name, None, {}, "nothing to rewrite, as before"
        out = os.path.join(root, "o-%s.dex" % said["outcome"])
        if os.path.isfile(out):
            _touch(answer_path)
            _touch(out)
            return name, out, counts, "from the cache"
    except (OSError, ValueError, KeyError):
        pass

    scratch = os.path.join(job["workspace"], name.replace(".dex", ""))
    shutil.rmtree(scratch, ignore_errors=True)
    os.makedirs(scratch, exist_ok=True)
    try:
        tree = _pieces(dex, dex_sha, smali, root, scratch)
        room = os.path.join(scratch, "smali")
        _copy(tree, room)
        counts, written = _rewrite(room, literals, dex)
        if not counts:
            _save(answer_path, json.dumps({"counts": {}}).encode("utf-8"))
            return name, None, {}, "nothing to rewrite after all"

        outcome = _outcome(dex_sha, smali, tree, room, written)
        out = os.path.join(root, "o-%s.dex" % outcome)
        if os.path.isfile(out):
            _touch(out)
            said = "rewritten as before, not reassembled"
        else:
            dex_out = os.path.join(scratch, "out.dex")
            smali.assemble(room, dex_out)
            with open(dex_out, "rb") as handle:
                patched = handle.read()
            if dexpatch.dex_format(patched) != dexpatch.dex_format(dex):
                raise RuntimeError(
                    "%s came back as dex %s, it went in as dex %s -- an Android old "
                    "enough to be in the apk's minSdk would refuse to load it"
                    % (name, dexpatch.dex_format(patched), dexpatch.dex_format(dex)))
            _save(out, patched)
            said = "reassembled"
        _save(answer_path, json.dumps({"counts": counts, "outcome": outcome})
              .encode("utf-8"))
        return name, out, counts, said
    finally:
        shutil.rmtree(scratch, ignore_errors=True)


def patch_all(dexes: Dict[str, bytes], jar: str, api: int, workspace: str,
              literals: Optional[Dict[str, str]],
              say: Callable[[str], None]) -> Dict[str, Tuple[Optional[bytes], Dict[str, int]]]:
    names = sorted(dexes)
    if os.environ.get("SOUFIT_NO_CACHE"):
        root = os.path.join(workspace, "no-cache")
        shutil.rmtree(root, ignore_errors=True)
    else:
        root = folder()
        prune(root)
    os.makedirs(root, exist_ok=True)

    at_once = workers(len(names))
    cores = os.cpu_count() or 2
    threads = int(os.environ.get("SOUFIT_JOBS") or 0) or max(2, (2 * cores) // max(1, at_once))
    threads = min(threads, cores)
    heap = os.environ.get("SOUFIT_HEAP") or HEAP
    say("%d dex files, %d at a time" % (len(names), at_once))

    def job(name: str) -> dict:
        return {"name": name, "dex": dexes[name], "literals": literals,
                "root": root, "jar": jar, "api": api, "jobs": threads,
                "heap": heap, "found": dexpatch.FOUND, "workspace": workspace}

    found: Dict[str, Tuple[Optional[bytes], Dict[str, int]]] = {}
    started = time.time()
    order = sorted(names, key=lambda name: -len(dexes[name]))
    with ProcessPoolExecutor(max_workers=at_once) as pool:
        waiting = [pool.submit(one, job(name)) for name in order]
        try:
            for done in as_completed(waiting):
                name, path, counts, said = done.result()
                patched = None
                if path:
                    with open(path, "rb") as handle:
                        patched = handle.read()
                found[name] = (patched, counts)
                say("%s: %s (%d of %d, %ds)" % (name, said, len(found), len(names),
                                                 time.time() - started))
        except BaseException:
            for other in waiting:
                other.cancel()
            raise
    return found
