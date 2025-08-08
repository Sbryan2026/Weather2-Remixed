package net.mrbt0907.weather2remastered.util.coro;

import java.lang.reflect.Field;

import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class CoroEntParticle {
	
	public static double getPosX(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getX();
		} else {
			return getPosXParticle(obj);
		}
	}

	private static double getPosXParticle(Object obj) {
	    try {
	        Field xField = Particle.class.getDeclaredField("x");
	        xField.setAccessible(true);
	        return xField.getDouble(obj);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return 0;
	    }
	}
	
	public static double getPosY(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getY();
		} else {
			return getPosYParticle(obj);
		}
	}

	private static double getPosYParticle(Object obj) {
	    try {
	        Field yField = Particle.class.getDeclaredField("y");
	        yField.setAccessible(true);
	        return yField.getDouble(obj);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return 0;
	    }
	}
	
	public static double getPosZ(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getZ();
		} else {
			return getPosZParticle(obj);
		}
	}

	private static double getPosZParticle(Object obj) {
	    try {
	        Field zField = Particle.class.getDeclaredField("z");
	        zField.setAccessible(true);
	        return zField.getDouble(obj);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return 0;
	    }
	}
	
	public static double getMotionX(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getDeltaMovement().x;
		} else {
			return getMotionXParticle(obj);
		}
	}

	private static double getMotionXParticle(Object obj) {
	    try {
	        // 'zd' is the field name for motionZ in 1.16.5 Particle class
	        Field xdField = obj.getClass().getSuperclass().getDeclaredField("xd");
	        xdField.setAccessible(true);
	        return xdField.getDouble(obj);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return 0.0;
	    }
	}
	
	public static double getMotionY(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getDeltaMovement().y;
		} else {
			return getMotionYParticle(obj);
		}
	}
	
	private static double getMotionYParticle(Object obj) {
	    try {
	        // 'zd' is the field name for motionZ in 1.16.5 Particle class
	        Field ydField = obj.getClass().getSuperclass().getDeclaredField("yd");
	        ydField.setAccessible(true);
	        return ydField.getDouble(obj);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return 0.0;
	    }
	}
	
	public static double getMotionZ(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getDeltaMovement().z;
		} else {
			return getMotionZParticle(obj);
		}
	}

	private static double getMotionZParticle(Object obj) {
	    try {
	        // 'zd' is the field name for motionZ in 1.16.5 Particle class
	        Field zdField = obj.getClass().getSuperclass().getDeclaredField("zd");
	        zdField.setAccessible(true);
	        return zdField.getDouble(obj);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return 0.0;
	    }
	}
	
	public static void setMotionX(Object obj, double val) {
		if (obj instanceof Entity) {
			Entity entity = (Entity) obj;
		    Vector3d velocity = entity.getDeltaMovement();
		    entity.setDeltaMovement(val, velocity.y, velocity.z);
		} else {
			setMotionXParticle(obj, val);
		}
	}

	private static void setMotionXParticle(Object obj, double val) {
		try {
	        // Get the 'xd' field from the Particle class (or its superclass)
	        Field xdField = obj.getClass().getSuperclass().getDeclaredField("xd");
	        xdField.setAccessible(true);
	        xdField.setDouble(obj, val);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public static void setMotionY(Object obj, double val) {
		if (obj instanceof Entity) {
			Entity entity = (Entity) obj;
		    Vector3d velocity = entity.getDeltaMovement();
		    entity.setDeltaMovement(velocity.x, val, velocity.z);
		} else {
			setMotionYParticle(obj, val);
		}
	}

	private static void setMotionYParticle(Object obj, double val) {
		try {
	        // Get the 'xd' field from the Particle class (or its superclass)
	        Field ydField = obj.getClass().getSuperclass().getDeclaredField("yd");
	        ydField.setAccessible(true);
	        ydField.setDouble(obj, val);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public static void setMotionZ(Object obj, double val) {
		if (obj instanceof Entity) {
			Entity entity = (Entity) obj;
		    Vector3d velocity = entity.getDeltaMovement();
		    entity.setDeltaMovement(velocity.x, velocity.y, val);
		} else {
			setMotionZParticle(obj, val);
		}
	}

	private static void setMotionZParticle(Object obj, double val) {
		try {
	        // Get the 'xd' field from the Particle class (or its superclass)
	        Field zdField = obj.getClass().getSuperclass().getDeclaredField("zd");
	        zdField.setAccessible(true);
	        zdField.setDouble(obj, val);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public static World getWorld(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).level;
		} else {
			return getWorldParticle(obj);
		}
	}

	private static World getWorldParticle(Object obj) {
	    try {
	        Field levelField = Particle.class.getDeclaredField("level");
	        levelField.setAccessible(true);
	        return (World) levelField.get(obj);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}

	public static double getDistance(Object obj, double x, double y, double z)
	{
		double d0 = getPosX(obj) - x;
		double d1 = getPosY(obj) - y;
		double d2 = getPosZ(obj) - z;
		return (double) MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
	}

	public static void setPosX(Object obj, double val) {
		if (obj instanceof Entity) {
			Entity entity = (Entity) obj;
		    entity.setPos(val, entity.getY(), entity.getZ());
		} else {
			setPosXParticle(obj, val);
		}
	}

	private static void setPosXParticle(Object obj, double val) {
		 try {
		        Field xField = obj.getClass().getSuperclass().getDeclaredField("x");
		        xField.setAccessible(true);
		        xField.setDouble(obj, val);
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
	}

	public static void setPosY(Object obj, double val) {
		if (obj instanceof Entity) {
			Entity entity = (Entity) obj;
		    entity.setPos(entity.getX(), val, entity.getZ());
		} else {
			setPosYParticle(obj, val);
		}
	}

	private static void setPosYParticle(Object obj, double val) {
		try {
	        Field yField = obj.getClass().getSuperclass().getDeclaredField("y");
	        yField.setAccessible(true);
	        yField.setDouble(obj, val);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	public static void setPosZ(Object obj, double val) {
		if (obj instanceof Entity) {
			Entity entity = (Entity) obj;
		    entity.setPos(entity.getX(), entity.getY(), val);
		} else {
			setPosZParticle(obj, val);
		}
	}

	private static void setPosZParticle(Object obj, double val) {
		try {
	        Field zField = obj.getClass().getSuperclass().getDeclaredField("z");
	        zField.setAccessible(true);
	        zField.setDouble(obj, val);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
}
