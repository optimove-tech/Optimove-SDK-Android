"""
Applies build fixes for Optimove SDK QA app.
Run from project root: python3 scripts/apply-build-fixes.py
"""
import os
import re

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(SCRIPT_DIR)


def apply_settings_gradle():
    path = os.path.join(ROOT, "settings.gradle")
    with open(path) as f:
        content = f.read()
    if "include ':optimove-sdk', ':app'" in content:
        return False
    if "include ':optimove-sdk'" in content:
        content = content.replace("include ':optimove-sdk'", "include ':optimove-sdk', ':app'")
        with open(path, "w") as f:
            f.write(content)
        return True
    return False


def apply_gradle_wrapper():
    path = os.path.join(ROOT, "gradle", "wrapper", "gradle-wrapper.properties")
    with open(path) as f:
        content = f.read()
    if "gradle-7.2-bin.zip" in content:
        content = content.replace("gradle-7.2-bin.zip", "gradle-8.2-bin.zip")
        with open(path, "w") as f:
            f.write(content)
        return True
    return False


def apply_root_build_gradle():
    path = os.path.join(ROOT, "build.gradle")
    with open(path) as f:
        content = f.read()
    changed = False
    if "gradle:4.2.2" in content:
        content = content.replace("com.android.tools.build:gradle:4.2.2", "com.android.tools.build:gradle:8.2.2")
        changed = True
    if "google-services:4.3.10" in content:
        content = content.replace("com.google.gms:google-services:4.3.10", "com.google.gms:google-services:4.4.2")
        changed = True
    if "kotlin-gradle-plugin" not in content:
        content = content.replace(
            "classpath 'com.android.tools.build:gradle:8.2.2'",
            "classpath 'com.android.tools.build:gradle:8.2.2'\n        classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.22'"
        )
        changed = True
    if changed:
        with open(path, "w") as f:
            f.write(content)
    return changed


def apply_optimove_sdk_build_gradle():
    path = os.path.join(ROOT, "optimove-sdk", "build.gradle")
    with open(path) as f:
        content = f.read()
    changed = False
    if "buildToolsVersion '30.0.3'" in content:
        content = re.sub(r"\s*buildToolsVersion\s+['\"]30\.0\.3['\"]\s*\n", "\n", content)
        changed = True
    if "namespace 'com.optimove.android'" not in content:
        content = content.replace(
            "android {\n",
            "android {\n    namespace 'com.optimove.android'\n\n    buildFeatures {\n        buildConfig true\n    }\n    publishing {\n        singleVariant(\"release\")\n    }\n\n"
        )
        changed = True
    if changed:
        with open(path, "w") as f:
            f.write(content)
    return changed


def apply_optimove_sdk_manifest():
    path = os.path.join(ROOT, "optimove-sdk", "src", "main", "AndroidManifest.xml")
    with open(path) as f:
        content = f.read()
    if 'package="com.optimove.android"' in content:
        content = re.sub(r'\s*package="com\.optimove\.android"\s*', ' ', content)
        with open(path, "w") as f:
            f.write(content)
        return True
    return False


def comment_out_embedded_messaging():
    path = os.path.join(ROOT, "app", "src", "main", "java", "com", "optimove", "android", "optimovemobilesdk", "MyApplication.kt")
    with open(path) as f:
        content = f.read()
    if ".enableEmbeddedMessaging(" in content and "//.enableEmbeddedMessaging(" not in content:
        content = content.replace(
            '.enableEmbeddedMessaging("embedded_config_string")',
            '//.enableEmbeddedMessaging("embedded_config_string")'
        )
        with open(path, "w") as f:
            f.write(content)
        return True
    return False


def apply_app_build_gradle():
    path = os.path.join(ROOT, "app", "build.gradle")
    with open(path) as f:
        content = f.read()
    changed = False
    if "multiDexEnabled true" not in content:
        content = content.replace(
            'applicationId "com.optimove.android.optimovemobilesdk"',
            'applicationId "com.optimove.android.optimovemobilesdk"\n        multiDexEnabled true'
        )
        changed = True
    if "androidx.multidex:multidex" not in content:
        content = content.replace(
            "dependencies {",
            "dependencies {\n    implementation 'androidx.multidex:multidex:2.0.1'"
        )
        changed = True
    if changed:
        with open(path, "w") as f:
            f.write(content)
    return changed


def main():
    print("Applying build fixes...")
    fixes = [
        ("settings.gradle", apply_settings_gradle),
        ("gradle-wrapper.properties", apply_gradle_wrapper),
        ("build.gradle", apply_root_build_gradle),
        ("optimove-sdk/build.gradle", apply_optimove_sdk_build_gradle),
        ("optimove-sdk AndroidManifest", apply_optimove_sdk_manifest),
        ("app/build.gradle", apply_app_build_gradle),
        ("MyApplication.kt - comment out enableEmbeddedMessaging", comment_out_embedded_messaging),
    ]
    for name, fn in fixes:
        try:
            if fn():
                print(f"  ✓ {name}")
            else:
                print(f"  - {name} (already applied)")
        except Exception as e:
            print(f"  ✗ {name}: {e}")
            raise


if __name__ == "__main__":
    main()
