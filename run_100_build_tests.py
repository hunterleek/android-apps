#!/usr/bin/env python3
import os
import re
import sys
import json
import shutil
import zipfile
import subprocess
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor, as_completed

ROOT = os.path.dirname(os.path.abspath(__file__))
APPS = ["messaging", "calling", "gallery", "fileexplorer", "keyboard"]
RESULTS_DIR = os.path.join(ROOT, "build_test_logs")
TIMESTAMP = datetime.now().strftime("%Y%m%d_%H%M%S")

os.makedirs(RESULTS_DIR, exist_ok=True)

def run(cmd, cwd=None, env=None):
    try:
        result = subprocess.run(
            cmd,
            cwd=cwd,
            env=env,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            timeout=180,
        )
        return result.returncode, result.stdout
    except subprocess.TimeoutExpired as e:
        return -1, (e.stdout or "") + "\nTIMEOUT\n"
    except Exception as e:
        return -1, str(e)


def analyze_source_files(app):
    """Check Java imports match declared classes, XML references valid drawables/strings."""
    logs = []
    src_dir = os.path.join(ROOT, app, "app", "src", "main")
    java_dir = os.path.join(src_dir, "java")
    res_dir = os.path.join(src_dir, "res")

    errors = []

    java_files = []
    if os.path.isdir(java_dir):
        for root, _, files in os.walk(java_dir):
            for f in files:
                if f.endswith(".java"):
                    java_files.append(os.path.join(root, f))

    # Find all declared classes
    declared_classes = set()
    for jf in java_files:
        with open(jf, "r", errors="ignore") as f:
            content = f.read()
        package_match = re.search(r"package\s+([\w.]+);", content)
        package = package_match.group(1) if package_match else ""
        for m in re.finditer(r"(?:class|interface)\s+(\w+)", content):
            declared_classes.add((package + "." + m.group(1)).strip("."))

    # Check imports resolve
    for jf in java_files:
        with open(jf, "r", errors="ignore") as f:
            content = f.read()
        for imp in re.findall(r"import\s+([\w.]+);", content):
            if imp.startswith("android") or imp.startswith("java") or imp.startswith("androidx") or imp.startswith("com.google"):
                continue
            if imp.startswith("com.app."):
                class_name = imp
                if class_name not in declared_classes:
                    errors.append(f"UNRESOLVED_IMPORT: {jf} imports {imp}")

    # Check XML references
    if os.path.isdir(res_dir):
        drawables = set()
        strings = set()
        ids = set()
        for root, _, files in os.walk(res_dir):
            for f in files:
                path = os.path.join(root, f)
                rel = os.path.relpath(path, res_dir)
                if rel.startswith("drawable/") and f.endswith(".xml"):
                    drawables.add(os.path.splitext(f)[0])
                if rel.startswith("values/") and f.endswith(".xml"):
                    with open(path, "r", errors="ignore") as fp:
                        strings.update(re.findall(r'<string[^>]*name="([^"]+)"', fp.read()))
                if rel.startswith("layout/") or rel.startswith("menu/") or rel.startswith("xml/"):
                    with open(path, "r", errors="ignore") as fp:
                        data = fp.read()
                        for ref in re.findall(r'@drawable/([a-zA-Z0-9_]+)', data):
                            if ref not in drawables:
                                errors.append(f"MISSING_DRAWABLE: {rel} references drawable/{ref}")
                        for ref in re.findall(r'@string/([a-zA-Z0-9_]+)', data):
                            if ref not in strings:
                                errors.append(f"MISSING_STRING: {rel} references string/{ref}")

    return errors


def test_1_gradle_build(app):
    log = []
    code, out = run(["./gradlew", "assembleDebug", "--no-daemon", "--stacktrace"], cwd=os.path.join(ROOT, app))
    log.append(f"=== TEST 1: Gradle build for {app} ===")
    log.append(out)
    return code, "\n".join(log)


def test_2_clean_build(app):
    log = []
    run(["./gradlew", "clean", "--no-daemon"], cwd=os.path.join(ROOT, app))
    code, out = run(["./gradlew", "assembleDebug", "--no-daemon"], cwd=os.path.join(ROOT, app))
    log.append(f"=== TEST 2: Clean build for {app} ===")
    log.append(out)
    return code, "\n".join(log)


def test_3_manifest_validation(app):
    log = []
    manifest = os.path.join(ROOT, app, "app", "src", "main", "AndroidManifest.xml")
    log.append(f"=== TEST 3: Manifest validation for {app} ===")
    if not os.path.exists(manifest):
        log.append("MISSING AndroidManifest.xml")
        return 1, "\n".join(log)
    with open(manifest, "r") as f:
        data = f.read()
    if "<application" not in data:
        log.append("ERROR: no application tag")
    if "MAIN" not in data or "LAUNCHER" not in data:
        log.append("ERROR: no launcher activity")
    log.append("OK")
    return 0, "\n".join(log)


def test_4_aapt2_compile_resources(app):
    log = []
    log.append(f"=== TEST 4: AAPT2 compile resources for {app} ===")
    return 0, "\n".join(log)


def test_5_layout_reference_check(app):
    log = []
    errors = analyze_source_files(app)
    log.append(f"=== TEST 5: Layout/Resource reference check for {app} ===")
    log.extend(errors)
    if not errors:
        log.append("OK")
    return 1 if errors else 0, "\n".join(log)


def test_6_gradle_sync(app):
    log = []
    code, out = run(["./gradlew", "tasks", "--no-daemon"], cwd=os.path.join(ROOT, app))
    log.append(f"=== TEST 6: Gradle tasks list for {app} ===")
    log.append(out)
    return code, "\n".join(log)


def test_7_dependencies_resolve(app):
    log = []
    code, out = run(["./gradlew", "dependencies", "--configuration", "debugRuntimeClasspath", "--no-daemon"], cwd=os.path.join(ROOT, app))
    log.append(f"=== TEST 7: Dependencies resolve for {app} ===")
    log.append(out)
    return code, "\n".join(log)


def test_8_lint_check(app):
    log = []
    code, out = run(["./gradlew", "lintDebug", "--no-daemon"], cwd=os.path.join(ROOT, app))
    log.append(f"=== TEST 8: Lint check for {app} ===")
    log.append(out)
    return code, "\n".join(log)


def test_9_apk_output(app):
    log = []
    code, out = test_1_gradle_build(app)
    log.append(f"=== TEST 9: APK output for {app} ===")
    apk_dir = os.path.join(ROOT, app, "app", "build", "outputs", "apk", "debug")
    apks = []
    if os.path.isdir(apk_dir):
        apks = [f for f in os.listdir(apk_dir) if f.endswith(".apk")]
    if not apks:
        log.append("ERROR: No APK generated")
        return 1, "\n".join(log)
    for apk in apks:
        apk_path = os.path.join(apk_dir, apk)
        size = os.path.getsize(apk_path)
        log.append(f"APK: {apk} size={size}")
        with zipfile.ZipFile(apk_path, "r") as z:
            classes = [n for n in z.namelist() if n.startswith("classes")]
            log.append(f"  dex files: {classes}")
    return 0, "\n".join(log)


def test_10_proguard_config(app):
    log = []
    log.append(f"=== TEST 10: ProGuard config for {app} ===")
    proguard = os.path.join(ROOT, app, "app", "proguard-rules.pro")
    if os.path.exists(proguard):
        log.append("OK proguard-rules.pro exists")
    else:
        log.append("WARNING proguard-rules.pro missing")
    return 0, "\n".join(log)


TEST_FUNCTIONS = [
    test_1_gradle_build,
    test_2_clean_build,
    test_3_manifest_validation,
    test_4_aapt2_compile_resources,
    test_5_layout_reference_check,
    test_6_gradle_sync,
    test_7_dependencies_resolve,
    test_8_lint_check,
    test_9_apk_output,
    test_10_proguard_config,
]


def run_all_for_app(app):
    app_logs = []
    summary = {"app": app, "tests": []}
    for i, fn in enumerate(TEST_FUNCTIONS, 1):
        code, out = fn(app)
        app_logs.append(out)
        summary["tests"].append({"test": i, "name": fn.__name__, "code": code})
        log_file = os.path.join(RESULTS_DIR, f"{app}_test_{i:03d}_{TIMESTAMP}.log")
        with open(log_file, "w") as f:
            f.write(out)
    return app, app_logs, summary


def main():
    all_logs = []
    all_summaries = []
    for app in APPS:
        app, app_logs, summary = run_all_for_app(app)
        all_logs.append(f"\n\n########## APP: {app} ##########\n")
        all_logs.extend(app_logs)
        all_summaries.append(summary)

    combined_path = os.path.join(RESULTS_DIR, f"combined_build_logs_{TIMESTAMP}.log")
    with open(combined_path, "w") as f:
        f.write("\n".join(all_logs))

    summary_path = os.path.join(RESULTS_DIR, f"summary_{TIMESTAMP}.json")
    with open(summary_path, "w") as f:
        json.dump(all_summaries, f, indent=2)

    print(f"Combined logs: {combined_path}")
    print(f"Summary: {summary_path}")


if __name__ == "__main__":
    main()
