enablePlugins(WindowsPlugin)

name := "custom-wix"
version := "0.1.0"

// make sure we don't somehow use the generated script
wixFile in Windows := {
  sys.error("wixFile shouldn't have been called")
}

wixFiles := List(
  sourceDirectory.value / "wix" / "main.wsx",
  sourceDirectory.value / "wix" / "ui.wsx"
)
