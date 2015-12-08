package org.sunflow.core.primitive;

import org.sunflow.SunflowAPI;
import org.sunflow.core.Instance;
import org.sunflow.core.IntersectionState;
import org.sunflow.core.ParameterList;
import org.sunflow.core.PrimitiveList;
import org.sunflow.core.Ray;
import org.sunflow.core.ShadingState;
import org.sunflow.math.BoundingBox;
import org.sunflow.math.Matrix4;
import org.sunflow.math.OrthoNormalBasis;
import org.sunflow.math.Point3;
import org.sunflow.math.Vector3;

public class Terrain implements PrimitiveList {
	private float eps = 0.01f;
	
    private Point3 center;
    private Vector3 normal;
    int k;
    private float bnu, bnv, bnd;
    private float cnu, cnv, cnd;

    public Terrain() {
        center = new Point3(0, 0, 0);
        normal = new Vector3(0, 0, 1);
        k = 3;
        bnu = bnv = bnd = 0;
        cnu = cnv = cnd = 0;
    }
	
	@Override
	public boolean update(ParameterList pl, SunflowAPI api) {
		return true;
	}

	@Override
	public BoundingBox getWorldBounds(Matrix4 o2w) {
        BoundingBox bounds = new BoundingBox(1);
        if (o2w != null)
            bounds = o2w.transform(bounds);
        return bounds;
	}

	@Override
    public float getPrimitiveBound(int primID, int i) {
        return (i & 1) == 0 ? -1 : 1;
    }

	@Override
    public int getNumPrimitives() {
        return 1;
    }
	
	@Override
	public PrimitiveList getBakingPrimitives() {
		return null;
	}

	double cx =  0.211324865405187; // (3.0-sqrt(3.0))/6.0
	double cy =  0.366025403784439; // 0.5*(sqrt(3.0)-1.0)
	double cz = -0.577350269189626; // -1.0 + 2.0 * C.x
	double cw =  0.024390243902439; // 1.0 / 41.0

	private double fract(double x) {
	    return x - Math.floor(x);
	}

	private double dot2(double x1, double y1, double x2, double y2) {
	    return x1 * x2 + y1 * y2;
	}

	private double dot3(double x1, double y1, double z1, double x2, double y2, double z2) {
	    return x1 * x2 + y1 * y2 + z1 * z2;
	}

	private double mod289(double x) {
	    return x - Math.floor(x * (1.0 / 289.0)) * 289.0;
	}

	private double permute(double x) {
	    return mod289(((x * 34.0) + 1.0) * x);
	}

	private double noise(double vx, double vy) {
	    // First corner
	    double dotVCYY = dot2(vx, vy, cy, cy);
	    double ix = Math.floor(vx + dotVCYY);
	    double iy = Math.floor(vy + dotVCYY);
	    double dotICXX = dot2(ix, iy, cx, cx);
	    double x0x = vx - ix + dotICXX;
	    double x0y = vy - iy + dotICXX;

	    // Other corners
	    double i1x = 0.0;
	    double i1y = 0.0;
	    // i1.x = step( x0.y, x0.x ) // x0.x > x0.y ? 1.0 : 0.0
	    // i1.y = 1.0 - i1.x
	    if (x0x > x0y) {
	        i1x = 1.0;
	        i1y = 0.0;
	    } else {
	        i1x = 0.0;
	        i1y = 1.0;
	    }
	    // x0 = x0 - 0.0 + 0.0 * C.xx
	    // x1 = x0 - i1 + 1.0 * C.xx
	    // x2 = x0 - 1.0 + 2.0 * C.xx
	    double x12x = x0x + cx;
	    double x12y = x0y + cx;
	    double x12z = x0x + cz;
	    double x12w = x0y + cz;
	    x12x = x12x - i1x;
	    x12y = x12y - i1y;

	    // Permutations
	    ix = mod289(ix); // Avoid truncation effects in permutation
	    iy = mod289(iy);

	    double ppx = permute(0.0 + iy);
	    double ppy = permute(i1y + iy);
	    double ppz = permute(1.0 + iy);
	    double px = permute(0.0 + ppx + ix);
	    double py = permute(i1x + ppy + ix);
	    double pz = permute(1.0 + ppz + ix);

	    double mx = Math.max(0.5 - dot2(x0x, x0y, x0x, x0y), 0.0);
	    double my = Math.max(0.5 - dot2(x12x, x12y, x12x, x12y), 0.0);
	    double mz = Math.max(0.5 - dot2(x12z, x12w, x12z, x12w), 0.0);
	    mx = mx * mx;
	    mx = mx * mx;
	    my = my * my;
	    my = my * my;
	    mz = mz * mz;
	    mz = mz * mz;

	    // Gradients: 41 points uniformly over a line, mapped onto a diamond.
	    // The ring size 17*17 = 289 is close to a multiple of 41 (41*7 = 287)
	    double xx = 2.0 * fract(px * cw) - 1.0;
	    double xy = 2.0 * fract(py * cw) - 1.0;
	    double xz =  2.0 * fract(pz * cw) - 1.0;

	    double hx = Math.abs(xx) - 0.5;
	    double hy = Math.abs(xy) - 0.5;
	    double hz = Math.abs(xz) - 0.5;

	    double a0x = xx - Math.floor(xx + 0.5);
	    double a0y = xy - Math.floor(xy + 0.5);
	    double a0z = xz - Math.floor(xz + 0.5);

	    // Normalise gradients implicitly by scaling m
	    // Approximation of: m *= inversesqrt( a0*a0 + h*h )
	    mx = mx * (1.79284291400159 - 0.85373472095314 * (a0x * a0x + hx * hx));
	    my = my * (1.79284291400159 - 0.85373472095314 * (a0y * a0y + hy * hy));
	    mz = mz * (1.79284291400159 - 0.85373472095314 * (a0z * a0z + hz * hz));

	    // Compute final noise value at P
	    double gx = a0x * x0x + hx * x0y;
	    double gy = a0y * x12x + hy * x12y;
	    double gz = a0z * x12z + hz * x12w;

	    return 130.0 * dot3(mx, my, mz, gx, gy, gz);
	}
	
	private double simplexNoise(double x, double y, int octaves, double amp, double gain, double freq, double lac) {
		double sum = 0.0;
		for (int i = 0; i < octaves; ++i) {
			sum += noise(x * freq, y * freq) * amp;
			amp *= gain;
			freq *= lac;
		}
		return sum;
	}
	
	private double roundTo(double x, double size) {
		return Math.floor(x / size) * size;
	}
	
	private double lerp(double x, double y, double t) {
		return x + (y - x) * t;
	}
	
	private double height(double x, double y) {
		double h = simplexNoise(x, y, 10, 1.0, 0.4, 0.001, 3);
		h = Math.pow(h, 2) * 20;
		
		h = lerp(h, roundTo(h, 5.0f), 0.3);
		
		return h;
	}
	
	float distPlane(float x, float y, float z) {
	    return (float) (z - height(x, y));
	}
	
	int MAX_ITERATIONS = 200;
	
	float startDelta = 0.5f;
	float stopDelta = 5000.0f;
	
	float computeDist(float x, float y, float z) {
	    float dist = distPlane(x, y, z);
	    
	    return dist;
	}
	
	float getDistToObjects( Vector3 camPos, Vector3 rayDir ) {
		float t = startDelta;
		
		for (int i = 0; i < MAX_ITERATIONS; ++i) {
			float h = computeDist(camPos.x + rayDir.x * t, camPos.y + rayDir.y * t, camPos.z + rayDir.z * t);
			if (h < (0.002 * t) || t > stopDelta)
				break;
			t += 0.5 * h;
			if (i == MAX_ITERATIONS - 1) {
				return 0.0f;
			}
		}
		
		return t;
	}
	
	Vector3 getNormalAtPoint(Vector3 pos, Ray ray, float dist, float epss) {
	    float px = ray.ox + ray.dx * dist;
	    float py = ray.oy + ray.dy * dist;
		float x = (float) (height(px - epss, py) - height(px + epss, py));
		float y = (float) (height(px, py - epss) - height(px, py + epss));
		float z = 2.0f * epss;
		float len = (float) Math.sqrt(x * x + y * y + z * z);
		return new Vector3(x / len, y / len, z / len);
	}
	
	Vector3 getSmoothedNormalAtPoint(Vector3 pos, Ray ray, float dist) {
		Vector3 fine = getNormalAtPoint(pos, ray, dist, eps);
		Vector3 smooth = getNormalAtPoint(pos, ray, dist, eps * 200);
		double t = 0.5;
		Vector3 lerped = new Vector3((float)lerp(fine.x, smooth.x, t), (float)lerp(fine.y, smooth.y, t), (float)lerp(fine.z, smooth.z, t));
		return lerped.normalize();
	}
	
    public void intersectPrimitive(Ray r, int primID, IntersectionState state) {
		float t = getDistToObjects(new Vector3(r.ox, r.oy, r.oz), new Vector3(r.dx, r.dy, r.dz));
		if (t < stopDelta * 0.5) {
			state.setIntersection(0, t, 0);
		}
    }

	@Override
	public void prepareShadingState(ShadingState state) {
        state.init();
//        state.getRay().getPoint(state.getPoint());
        Instance parent = state.getInstance();
        Vector3 pa = new Vector3(state.getPoint().x, state.getPoint().y, state.getPoint().z);
//        Vector3 worldNormal = getNormalAtPoint(pa);
//        Vector3 worldNormal = parent.transformNormalObjectToWorld(getNormalAtPoint(pa));
        Vector3 worldNormal = getSmoothedNormalAtPoint(pa, state.getRay(), state.getU());
//        Vector3 worldNormal = new Vector3(0, 0, 1);
        state.getNormal().set(worldNormal);
        state.getGeoNormal().set(worldNormal);
        state.setShader(parent.getShader(0));
        state.setModifier(parent.getModifier(0));
        
        Ray ray = state.getRay();
	    float px = ray.ox + ray.dx * state.getU();
	    float py = ray.oy + ray.dy * state.getU();
	    float pz = ray.oz + ray.dz * state.getU();
        state.getPoint().set(new Point3(px, py, pz));
        
        Point3 p = parent.transformWorldToObject(state.getPoint());
        float hu, hv;
        switch (k) {
            case 0: {
                hu = p.y;
                hv = p.z;
                break;
            }
            case 1: {
                hu = p.z;
                hv = p.x;
                break;
            }
            case 2: {
                hu = p.x;
                hv = p.y;
                break;
            }
            default:
                hu = hv = 0;
        }
        state.getUV().x = hu * bnu + hv * bnv + bnd;
        state.getUV().y = hu * cnu + hv * cnv + cnd;
        
        state.setBasis(OrthoNormalBasis.makeFromW(worldNormal));
	}
}
