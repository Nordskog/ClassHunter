package com.mayulive.xposed.classhunter.profiles;

import android.util.Log;

import com.mayulive.xposed.classhunter.ClassHunter;
import com.mayulive.xposed.classhunter.Modifiers;


public class ClassItem implements Profile<Class>
{
	private static final String TAG = ClassHunter.getLogTag( ClassItem.class );

	private Class mClass;
	private int mModifiers = -1;
	private String mPrefix = "";
	private boolean mInvert = false;
	
	public ClassItem(Class clazz)
	{
		mClass = clazz;
	}
	
	public ClassItem(Class clazz, String prefix)
	{
		mClass = clazz;
		mPrefix = prefix;
	}

	public ClassItem(Class clazz, String prefix, int modifiers)
	{
		mClass = clazz;
		mPrefix = prefix;
		mModifiers = modifiers;
	}

	public ClassItem(String prefix, int modifiers)
	{
		mClass = null;
		mPrefix = prefix;
		mModifiers = modifiers;
	}

	public ClassItem(int modifiers)
	{
		mClass = null;
		mModifiers = modifiers;
	}

	public ClassItem(Class clazz, int modifiers)
	{
		mClass = clazz;
		mModifiers = modifiers;
	}

	public int getModifiers()
	{
		return mModifiers;
	}

	/**
	 * If enabled, compare and similarity methods will return inverted values (true to false, false to true)
	 * @param inverted Whether this item should be inverted
	 * @return
	 */
	public ClassItem setInverted(boolean inverted)
	{
		mInvert = inverted;
		return this;
	}
	
	public String getName()
	{
		if (mClass != null)
			return mClass.toString()+", "+ mPrefix;
		else
			return mPrefix;
	}

	private boolean _compareTo( Class rightClass, Class rightParentClass, boolean considerModifier)
	{
		//Comparison against null should match only if this item is completely empty.
		if (rightClass == null)
		{
			if (mClass == null && mPrefix.isEmpty() && mModifiers == -1)
				return true;
			else
				return false;
		}

		//Always use direct class comparison if available
		//Modifiers and prefix are ignored if class is available.
		if (mClass != null)
		{
			if ( mClass == rightClass )
				return true;
			else
			{
				if (ClassHunter.DEBUG_COMPARISON)
					Log.i(TAG, "Direct comparison failed. Right: "+ProfileUtil.getName(rightClass)+", profile: "+ ProfileUtil.getName(mClass));
				return false;
			}
		}

		//Both or neither must be array
		if ( (mModifiers != -1) && (Modifiers.isArray(mModifiers) != rightClass.isArray()) )
		{
			if (ClassHunter.DEBUG_COMPARISON)
				Log.i(TAG, "Array comparison failed. Right: "+ProfileUtil.getName(rightClass)+" "+rightClass.isArray()+", profile: "+ Modifiers.isArray(mModifiers));
			return false;
		}


		//Otherwise modifiers and this
		if (mModifiers != -1 && considerModifier)
		{
			//If the right class is an array, we want to compare
			//against its contained class instead
			if (rightClass.isArray())
			{
				rightClass = rightClass.getComponentType();
			}

			//If this is a match return right away
			if ( Modifiers.isThis(mModifiers))
			{
				if (rightClass == rightParentClass)
					return true;
				else
				{
					if (ClassHunter.DEBUG_COMPARISON)
						Log.i(TAG, "Thiz comparison failed. Right: "+ProfileUtil.getName(rightClass)+" "+rightClass.getModifiers()+", Parent: "+(rightParentClass == null ? "null" : ProfileUtil.getName(rightParentClass)));
					return false;
				}

			}

			//Modifiers
			if (!Modifiers.compare(mModifiers, rightClass.getModifiers()))
			{
				if (ClassHunter.DEBUG_COMPARISON)
					Log.i(TAG, "Modifiers comparison failed. Right: "+ProfileUtil.getName(rightClass)+" "+rightClass.getModifiers()+", profile: "+Modifiers.getStandard(mModifiers));
				return false;
			}
		}

		//And finally by partial path
		if ( !mPrefix.isEmpty() )
		{
			if (!ProfileUtil.getName(rightClass).startsWith(mPrefix))
			{
				if (ClassHunter.DEBUG_COMPARISON)
					Log.i(TAG, "String comparison failed. Right: "+ProfileUtil.getName(rightClass)+", Left: "+mPrefix);

				return false;
			}
		}

		return true;
	}


	
	public boolean compareTo(Class rightClass, Class rightParentClass)
	{
		if (this.mInvert)
			return !_compareTo(rightClass,rightParentClass, true);
		else
			return _compareTo(rightClass,rightParentClass, true);
	}

	@Override
	public float getSimilarity(Class right, Class rightParentClass, float minSimilarity)
	{
		if (right == null)
		{
			if (mClass == null && mPrefix.isEmpty() && mModifiers == -1)
				return 1f;
			else
				return 0f;
		}

		float classScore = this._compareTo( right, rightParentClass, false ) ? 1 : 0;

		if ( mClass == null && mModifiers != -1)
		{
			float modifierScore = 1f;
			if ( Modifiers.isThis( mModifiers ) && rightParentClass != null )
			{
				if (right.isArray())
				{
					// If ret is an array it won't match its parent
					modifierScore = right.getComponentType() == rightParentClass ? 1f : 0f;
				}
				else
				{
					// Ignore other flags if this is present
					modifierScore = right == rightParentClass ? 1f : 0f;
				}
			}
			else
			{
				if (right.isArray())
				{
					// If ret is an array it will have different modifiers than the actual class
					modifierScore = Modifiers.getSimilarity( mModifiers, right.getComponentType().getModifiers() );
				}
				else
				{
					modifierScore = Modifiers.getSimilarity( mModifiers, right.getModifiers() );
				}

			}

			// Only add to class score if modifiers set
			classScore = ( classScore * 0.7f ) + (modifierScore * 0.3f);
		}

		if (mInvert)
			return 1f - classScore;
		else
			return classScore;
	}


	@Override
    public String toString() 
	{
		String classString = "null";
		if (mClass != null)
			classString = mClass.toString();
        return ProfileUtil.getName(getClass()) + '@' +classString;
    }
	
}
