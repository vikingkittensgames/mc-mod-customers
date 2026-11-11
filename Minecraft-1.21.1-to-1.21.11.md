# Minecraft 1.21.1 to 1.21.11

## Render Types

Minecraft 1.21.1 constructs the translucent-no-depth render type from
`RenderStateShard` state and the block vertex format. Minecraft 1.21.11 uses
the newer `RenderSetup` and render-pipeline API. `RenderCUtils` keeps this
version difference outside display-case functionality.
