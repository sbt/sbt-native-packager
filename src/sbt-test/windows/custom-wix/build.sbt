enablePlugins(WindowsPlugin)

name := "custom-wix"
version := "0.1.0"

wixFiles := List(sourceDirectory.value / "wix" / "main.wsx", sourceDirectory.value / "wix" / "ui.wsx")
