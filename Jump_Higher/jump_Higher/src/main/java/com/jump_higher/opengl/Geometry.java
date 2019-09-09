package com.jump_higher.opengl;

/**
 * @author Stav Bodik
 * This class used to make mathematics geometry calculations . 
 */
public class Geometry {
	
	public static float[] findSurfaceEquastion(float x1,float y1,float z1,float x2,float y2,float z2,float x3,float y3,float z3){
		   // given 3 points return equation  ax+by+cz=d   of triangle surface (part of the mesh)
		   
		   //1. converting the 3 points to 2 vectors (subtract v3-v1,v2-v1)
		   float v1[] = {x3-x1,y3-y1,z3-z1};
		   float v2[] = {x2-x1,y2-y1,z2-z1};

		   //2. find cross product of the vectors which is the normal/perpendicular vector to v1,v2 to the place .
		   float normal[] = {v1[1]*v2[2]-v1[2]*v2[1],v1[2]*v2[0]-v1[0]*v2[2],v1[0]*v2[1]-v1[1]*v2[0]};
		   
		   float d=normal[0]*x1+normal[1]*y1+normal[2]*z1;
		   
		   return new float[]{normal[0],normal[1],normal[2],d};
	}
	
	public static float[] normlizeVector(float vectorStart[],float vectorEnd[]){
		
		float size=(float) Math.sqrt(Math.pow(vectorEnd[0]-vectorStart[0], 2)+Math.pow(vectorEnd[1]-vectorStart[1], 2)+Math.pow(vectorEnd[2]-vectorStart[2], 2));
		
		return new float[]{(vectorEnd[0]-vectorStart[0])/size,(vectorEnd[1]-vectorStart[1])/size,(vectorEnd[2]-vectorStart[2])/size};
	}
	
	public static float[] crossProduct(float A[],float B[]){
		  
		   return new float[]{A[1]*B[2]-A[2]*B[1],A[2]*B[0]-A[0]*B[2],A[0]*B[1]-A[1]*B[0]};
	   }
	
	public static float dotProduct(float v1[],float v2[]){			    
		return Vector.dot(v1, v2);
	}
	
	public static class Point{
		public final float x, y, z;
		
		public Point(float x, float y, float z){
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public Point translateY(float distance){
			return new Point(x, y + distance, z);
		}
		
		public Point translate(Vector vector){
			return new Point(x + vector.x,
							y + vector.y,
							z + vector.z);
		}
	}
	
	public static class Circle{
		public final Point center;
		public final float radius;
		
		public Circle(Point center, float radius){
			this.center = center;
			this.radius = radius;
		}
		
		public Circle scale(float scale){
			return new Circle(center, radius * scale);
		}
	}
	
	public static class Cylinder{
		public final Point center;
		public final float radius;
		public final float height;
		
		public Cylinder(Point center, float radius, float height){
			this.center = center;
			this.radius = radius;
			this.height = height;
		}
	}
	
	public static class SemiSphere{
		public final Point center;
		public final float radius;
		
		public SemiSphere(Point center, float radius){
			this.center = center;
			this.radius = radius;
		}
	}
	
	public static class Ring{
		public final Point center;
		public final float innerRadius;
		public final float width;
		
		public Ring(Point center, float radius, float width){
			this.center = center;
			this.innerRadius = radius;
			this.width = width;
		}
	}
	
	public static class Ray{
		public final Point point;
		public final Vector vector;
		
		public Ray(Point point, Vector vector){
			this.point = point;
			this.vector = vector;
		}
	}
	
	public static class Vector {
		public float x, y, z;
		
		public Vector(float x, float y, float z){
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public float length(){
			return (float) Math.sqrt(x*x + y*y + z*z);		
		}
		
		public Vector crossProduct(Vector other){
			return new Vector((y * other.z) - (z * other.y),
							(z * other.x) - (x * other.z),
							(x * other.y) - (y * other.x));
		}
		
		public float dotProduct(Vector other){
			return x * other.x + 
					y * other.y +
					z * other.z;
		}
		
		public Vector scale(float f){
			return new Vector(x * f,
							y * f,
							z * f);
		}
		
		
		// dot product (3D) which allows vector operations in arguments
	    public static float dot(float[] u,float[] v) {
	        return ((u[X] * v[X]) + (u[Y] * v[Y]) + (u[Z] * v[Z]));
	    }
	    public static float[] minus(float[] u, float[] v){
	        return new float[]{u[X]-v[X],u[Y]-v[Y],u[Z]-v[Z]};
	    }
	    public static float[] addition(float[] u, float[] v){
	        return new float[]{u[X]+v[X],u[Y]+v[Y],u[Z]+v[Z]};
	    }
	    //scalar product
	    public static float[] scalarProduct(float r, float[] u){
	        return new float[]{u[X]*r,u[Y]*r,u[Z]*r};
	    }
	    
	    
	    
	    // (cross product)
	    public static float[] crossProduct(float[] u, float[] v){
	        return new float[]{(u[Y]*v[Z]) - (u[Z]*v[Y]),(u[Z]*v[X]) - (u[X]*v[Z]),(u[X]*v[Y]) - (u[Y]*v[X])};
	    }
	    //mangnatude or length
	    public static float length(float[] u){
	        return (float) Math.abs(Math.sqrt((u[X] *u[X]) + (u[Y] *u[Y]) + (u[Z] *u[Z])));
	    }
	 
	    public static final int X = 0;
	    public static final int Y = 1;
	    public static final int Z = 2;
		public static float[] normalize(float[] minus) {

		    double d =  Math.sqrt(minus[0]*minus[0]+minus[1]*minus[1]+minus[2]*minus[2]);

			return new float[]{(float) (minus[0]/d),(float) (minus[1]/d),(float) (minus[2]/d)};
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	}
	
	public static Vector vectorBetween(Point from, Point to){
		return new Vector(to.x - from.x,
						to.y - from.y,
						to.z - from.z);
	}
	
	public static class Sphere{
		public final Point center;
		public final float radius;
		
		public Sphere(Point center, float radius){
			this.center = center;
			this.radius = radius;
		}
	}
	
	public static boolean intersects(Sphere sphere, Ray ray){
		return distanceBetween(sphere.center, ray) < sphere.radius;
	}
	
	public static float distanceBetween(Point point, Ray ray){
		Vector p1ToPoint = vectorBetween(ray.point, point);
		Vector p2ToPoint = vectorBetween(ray.point.translate(ray.vector), point);
		
		float areaOfTriangleTimeTwo = p1ToPoint.crossProduct(p2ToPoint).length();
		float lenghtOfBase = ray.vector.length();
	
		float distanceFromPointToRay = areaOfTriangleTimeTwo / lenghtOfBase;
		return distanceFromPointToRay;
	}
	
	public static class Plane{
		public final Point point;
		public final Vector normal;
		
		public Plane(Point point, Vector normal){
			this.point = point;
			this.normal = normal;
		}
	}
	
	public static Point intersectionPoint(Ray ray, Plane plane){
		Vector rayToPlaneVector = vectorBetween(ray.point, plane.point);
		
		float scaleFactor = rayToPlaneVector.dotProduct(plane.normal) / ray.vector.dotProduct(plane.normal);
		
		Point intersectionPoint = ray.point.translate(ray.vector.scale(scaleFactor));
		
		return intersectionPoint;
	}
	
}
