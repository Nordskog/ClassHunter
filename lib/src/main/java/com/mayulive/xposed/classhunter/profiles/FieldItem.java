package com.mayulive.xposed.classhunter.profiles;

import com.mayulive.xposed.classhunter.Modifiers;

import java.lang.reflect.Field;


public class FieldItem implements Profile<Field>
{
	//Takes a class item and a modifier
	private int mModifiers = -1;
	private ClassItem mClassItem;
	private boolean mInverted = false;

	public FieldItem(ClassItem clazz)
	{
		mClassItem = clazz;
	}

	public FieldItem(ClassItem clazz, int modifiers)
	{
		mClassItem = clazz;
		mModifiers = modifiers;
	}


	public FieldItem(int modifiers, ClassItem clazz)
	{
		mClassItem = clazz;
		mModifiers = modifiers;
	}


	public FieldItem(int modifiers)
	{
		mModifiers = modifiers;
	}

	public int getModifiers()
	{
		return mModifiers;
	}

	@Override
	public Profile<Field> setInverted(boolean inverted)
	{
		mInverted = inverted;
		return this;
	}

	public boolean _compareTo(Field right, Class rightParentClass, boolean considerModifier)
	{
		if (  considerModifier && !(mModifiers == -1 ? true : Modifiers.compare(mModifiers, right.getModifiers()) ) )
			return false;
		else
		{
			return mClassItem != null ?  mClassItem.compareTo(right.getType(), rightParentClass) : true;
		}
	}

	@Override
	public boolean compareTo(Field right, Class rightParentClass)
	{
		if (mInverted)
			return !_compareTo(right,rightParentClass, false);
		else
			return _compareTo(right,rightParentClass, false);
	}

	@Override
	public float getSimilarity(Field right, Class rightParentClass, float minSimilarity)
	{
		float classScore = mClassItem.getSimilarity( right.getType(), rightParentClass, minSimilarity );
		float modifierScore = Modifiers.getSimilarity( right.getModifiers(), mModifiers );

		if (mModifiers != -1)
			classScore = (classScore + modifierScore) / 2f;

		if (mInverted)
			return 1f - classScore;
		else
			return classScore;
	}
}