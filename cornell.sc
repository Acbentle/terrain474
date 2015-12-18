image {
  resolution 800 600
  aa 0 2
  filter gaussian
}

gi {
  type igi
  samples 64         % number of virtual photons per set
  sets 1             % number of sets (increase this to translate shadow boundaries into noise)
  b 0.00003          % bias - decrease this values until bright spots dissapear
  bias-samples 0     % set this >0 to make the algorithm unbiased
}

camera {
  type pinhole
  eye    0 -205 50
  target 0 0 50
  up     0 0 1
  fov    45
  aspect 1.333333
}

shader {
  name Mirror
  type mirror
  refl 0.7 0.7 0.7
}

shader {
  name Glass
  type glass
  eta 1.6
  color 1 1 1
}

object {
  shader none
  type cornellbox
  corner0 -60 -60 0
  corner1  60  60 100
  left    0.99 0.1 0.1
  right   0.1 0.1 0.99
  top     0.8 0.8 0.8
  bottom  0.8 0.8 0.8
  back    0.1 0.99 0.1
  emit    30 30 30
  samples 32
}

object {
  shader Mirror
  type sphere
  c -30 30 20
  r 20
}

object {
  shader Glass
  type sphere
  c 28 2 20
  r 20
}
