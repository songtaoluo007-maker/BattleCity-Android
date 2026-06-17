# Original art integration

The public build embeds compact atlas previews for tanks, campaigns, medals, and result screens. Private full builds inject the original HarmonyOS drawable files and the same UI automatically prefers those full-resolution resources by name.

This keeps the public repository buildable without publishing the private binary asset library, while the full APK uses the complete original artwork.
