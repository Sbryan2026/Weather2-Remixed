package net.mrbt0907.weather2remastered.api.weather;

import net.mrbt0907.weather2remastered.util.StringUtils;

public class WeatherEnum
{
	public static enum Type
	{
		CLOUD(), RAIN(), THUNDER(), SUPERCELL(), TORNADO(true),
		TROPICAL_DISTURBANCE(), TROPICAL_DEPRESSION(), TROPICAL_STORM(true), HURRICANE(true),
		SANDSTORM(true),
		BLIZZARD(true);
		
		private boolean isDangerous;
		
		Type()
		{
			this(false);
		}
		
		Type(boolean isDangerous)
		{
			this.isDangerous = isDangerous;
		}
		//SEE MRBT FOR HELP MAKING THIS AN ARRAYLIST, ENUMHELPER IS IMPOSSIBLE IN 1.16.5 DUE TO MAJOR SECURITY ISSUES / CODE INJECTION ATTACKS
		public static Type add(Type typeEnum, boolean isDangerous)
		{
			//EnumHelper.addEnum(Type.class, typeEnum.name(), new Class[] {Boolean.class}, isDangerous);
			return typeEnum;
		}
		
		public static Type get(int id)
		{
			return values()[id];
		}
		
		public static int size()
		{
			return values().length;
		}
		
		public boolean isDangerous()
		{
			return isDangerous;
		}
		
		@Override
		public String toString()
		{
			return StringUtils.toUpperCaseAlt(super.toString().replaceAll("\\_", " ").toLowerCase());
		}
	}
	
	public static enum Stage
	{
		NORMAL(0), RAIN(1), THUNDER(2), SEVERE(3), TROPICAL_DISTURBANCE(2), TROPICAL_DEPRESSION(3), TROPICAL_STORM(4), TORNADO(4), HURRICANE(5);
		
		private int stage;
		
		Stage(int stage)
		{
			this.stage = stage;
		}
		//SEE MRBT FOR HELP MAKING THIS AN ARRAYLIST, ENUMHELPER IS IMPOSSIBLE IN 1.16.5 DUE TO MAJOR SECURITY ISSUES / CODE INJECTION ATTACKS
		public static Stage add(Stage stageEnum, int stage)
		{
			//EnumHelper.addEnum(Stage.class, stageEnum.name(), new Class[] {Integer.class}, stage);
			return stageEnum;
		}
		
		public static Stage get(int id)
		{
			return values()[id];
		}
		
		public static int size()
		{
			return values().length;
		}
		
		public int getStage()
		{
			return stage;
		}
		
		@Override
		public String toString()
		{
			return StringUtils.toUpperCaseAlt(super.toString().replaceAll("\\_", " ").toLowerCase());
		}
	}
}