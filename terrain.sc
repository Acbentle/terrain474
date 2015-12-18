image {
  resolution 1920 1080
  aa 0 1
  filter mitchell
}

camera {
  type pinhole
  eye    -800 10000 200
  target -800 10550 30
  up     0 0 1
  fov    100
  aspect 1.7777777
}

gi {
 type path
 samples 0
}


light {
  type sunsky
  up 0 0 1
  east 0 1 0
  sundir 1.0 0.8 0.6
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

shader {
  name Water
  type glass
  eta 1.6
  color 1 1 1
}

shader {
  name Mirror
  type mirror
  refl 0.7 0.7 0.7
}

object {
  shader Water
  type plane
  p 0 0 80
  n 0 0 1
}

object {
  shader Water
  type sphere
  c -800 10100 200
  r 20
}