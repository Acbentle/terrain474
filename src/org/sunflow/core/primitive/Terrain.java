package org.sunflow.core.primitive;

import org.sunflow.SunflowAPI;
import org.sunflow.core.Instance;
import org.sunflow.core.IntersectionState;
import org.sunflow.core.LightSample;
import org.sunflow.core.LightSource;
import org.sunflow.core.ParameterList;
import org.sunflow.core.PrimitiveList;
import org.sunflow.core.Ray;
import org.sunflow.core.Shader;
import org.sunflow.core.ShadingState;
import org.sunflow.core.shader.TerrainShader;
import org.sunflow.image.Color;
import org.sunflow.math.BoundingBox;
import org.sunflow.math.Matrix4;
import org.sunflow.math.Noise;
import org.sunflow.math.OrthoNormalBasis;
import org.sunflow.math.Point3;
import org.sunflow.math.Vector3;

public class Terrain implements PrimitiveList {
	private float eps = 0.01f;

    public Terrain() {
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
	
	private double roundTo(double x, double size) {
		return Math.floor(x / size) * size;
	}
	
	private double lerp(double x, double y, double t) {
		return x + (y - x) * t;
	}
	
	double smoothstep(double edge0, double edge1, double x)
	{
	    // Scale, bias and saturate x to 0..1 range
		
	    x = Math.min(Math.max((x - edge0)/(edge1 - edge0), 0.0), 1.0); 
	    // Evaluate polynomial
	    return x*x*(3 - 2*x);
	}
	
	private double roughMountain(double x, double y) {
//		double mountain = Noise.simplexNoise(x, y, 2, 1.0, 0.25, 0.0004, 2);
//		mountain = Math.pow(mountain, 4) * 300;
		
//		mountain += Noise.simplexNoise(x, y, 8, 10.0, 0.5, 0.004, 2);
		
//		return mountain;
		return 0.0;
	}
	
	private double rockHeight(double x, double y) {
		double height = 0.0;

		height = roughMountain(x, y);
		
		return height;
	}
	
	private double sandHeight(double x, double y) {
		double height = 0.0;
		
		height = Noise.sharpNoise(x + 1000, y, 3, 1.0, 0.5, 0.0001, 2);
		height = Math.pow(height, 2) * 200;
		height += Noise.sharpNoise(x + 1000, y, 8, 10.0, 0.5, 0.001, 2);
		height += Noise.simplexNoise(x, y, 2, 0.025, 0.4, 1, 3);
		
		return height + 50.0;
	}
	
	private double height(double x, double y) {		
//		return rockHeight(x, y);
		return Math.max(rockHeight(x, y), sandHeight(x, y));
	}
	
	double distPlane(double x, double y, double z) {
	    return z - height(x, y);
	}
	
	int MAX_ITERATIONS = 500;
	
	double startDelta = 0.1;
	double stopDelta = 1000000.0;
	
	double computeDist(double x, double y, double z) {
		double dist = distPlane(x, y, z);
	    
	    return dist;
	}
	
	double getDistToObjects(Vector3 camPos, Vector3 rayDir ) {
		double t = startDelta;
		
		for (int i = 0; i < MAX_ITERATIONS; ++i) {
			double h = computeDist(camPos.x + rayDir.x * t, camPos.y + rayDir.y * t, camPos.z + rayDir.z * t);
			if (h < (0.002 * t) || t > stopDelta) break;
			t += 0.5 * h;
			if (i == MAX_ITERATIONS - 1) {
				return stopDelta;
			}
		}
		
		return t;
	}
	
	Vector3 getNormalAtPoint(float px, float py, float epss) {
		float x = (float) (height(px - epss, py) - height(px + epss, py));
		float y = (float) (height(px, py - epss) - height(px, py + epss));
		float z = 2.0f * epss;
		float len = (float) Math.sqrt(x * x + y * y + z * z);
		return new Vector3(x / len, y / len, z / len);
	}
	
	Vector3 getSmoothedNormalAtPoint(float px, float py) {
		Vector3 fine = getNormalAtPoint(px, py, eps);
		Vector3 smooth = getNormalAtPoint(px, py, eps * 200);
		double t = 0.5;
		Vector3 lerped = new Vector3((float)lerp(fine.x, smooth.x, t), (float)lerp(fine.y, smooth.y, t), (float)lerp(fine.z, smooth.z, t));
		return lerped.normalize();
	}
	
    public void intersectPrimitive(Ray r, int primID, IntersectionState state) {
    	double t = getDistToObjects(new Vector3(r.ox, r.oy, r.oz), new Vector3(r.dx, r.dy, r.dz));
		if (t < stopDelta && (r.getMax() > t || r.getMax() == Float.POSITIVE_INFINITY))
			state.setIntersection(0, (float)t, 0);
    }
    
	@Override
	public void prepareShadingState(ShadingState state) {
        state.init();
        
        Ray ray = state.getRay();
	    float px = ray.ox + ray.dx * state.getU();
	    float py = ray.oy + ray.dy * state.getU();
	    float pz = ray.oz + ray.dz * state.getU();
        state.getPoint().set(new Point3(px, py, pz));
        
        Vector3 worldNormal = getSmoothedNormalAtPoint(px, py);
        state.getNormal().set(worldNormal);
        state.getGeoNormal().set(worldNormal);
        state.setBasis(OrthoNormalBasis.makeFromW(worldNormal));
        
        Instance parent = state.getInstance();
        state.setShader(parent.getShader(0));
        state.setModifier(parent.getModifier(0));
	}
}
