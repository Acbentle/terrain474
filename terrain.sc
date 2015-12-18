image {
  resolution 640 480
  aa 0 1
  filter mitchell
}

camera {
  type pinhole
  eye    0 7000 20
  target 0 7050 15
  up     0 0 1
  fov    90
  aspect 1.333333
}

gi {
 type path
 samples 32
}

light {
  type sunsky
  up 0 0 1
  east 0 1 0
  sundir 1.0 0.8 0.5
  turbidity 1.0
  samples 1
}

shader {
  name TerrainShader
  type terrain
}

shader {
  name Diffuse
  type diffuse
  diff 1.0 1.0 1.0
}

object {
  shader TerrainShader
  type terrain
}