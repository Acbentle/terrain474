image {
  resolution 640 480
  aa 0 1
  filter mitchell
}

camera {
  type pinhole
  eye    -10.5945 -30.0581 18
  target 0.0554193 0.00521195 5.38209
  up     0 0 1
  fov    90
  aspect 1.333333
}

light {
  type sunsky
  up 0 0 1
  east 1 0 0
  sundir 0.8 0.5 0.3
  turbidity 0.999
  samples 2
}

shader {
  name TerrainShader
  type terrain
}

shader {
  name Diffuse
  type diffuse
  diff 1.0 0.5 0.5
}

object {
  shader TerrainShader
  type terrain
}