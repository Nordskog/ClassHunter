# Class Hunter

Class Hunter is an Android library for recognizing classes, methods, and constructors based on their signatures.
It is primarily intended for use in Xposed modules, to deal with proguarded classes and methods changing names and moving around.

A dex or apk file may be parsed and separated into nested packages and classes, which are then compared against an input profile.
Each class is awarded a score based on how closely it resembles the profile, and the top result may be loaded.

You may also use profiles to find individual methods, fields, or constructors within a given class. 

A simple web-interface is provided to automatically generate profiles for classes, including selection of partially obfuscated paths that are expected to change.

Search results can be stored to speed up the process on subsequent launches.

## Using Class Hunter in your application

Add this to your app's build.gradle

```java
dependencies
{            
          compile 'com.mayulive.xposed.classhunter:classhunter:1.0.0'      
}
```

Available in the `jcenter` repository

## Basic usage

A `Profile` is at the lowest level comprised of `ClassItem` objects. These are a simplified representation of a class.
The `Profile` interface to allows for comparing and similarity checking.

A `ClassItem` may be defined with a Class object, a partial canonical class name, and/or with modifiers.

Matches int.class
```java
new ClassItem( int.class )
```

Matches any enum
```java
new ClassItem( ENUM )
```

Matches any class whose name begins with `"partial.class.path"`, and is also an enum.
```java
new ClassItem("partial.class.path" , ENUM  )
```

This library also defines a few additional modifiers that can be used alongside the standard java ones

```java
THIS      // Class must match the parent class in methods, constructors, and fields
ARRAY     // Must be an array
EXACT     // All modifiers, apart from these non-standard ones, must match exactly. 
          // Without this modifier ENUM will match ENUM | PUBLIC | STATIC,
          // but without it will not.
REQUIRED  // When lists of profiles are compared (e.g. when comparing method parameters)
          // the other list will only be considered a match if it contains this item.
NOT_FOUND // Special marker for classes that trigger a NoClassDefFoundError.
          // This is primarily (and rarely) used when auto-generating class profiles.
```
Method, constructor, and field profiles following the same pattern:
      
```java
new MethodProfile
(           
  //Modifiers
  PUBLIC | STATIC | EXACT ,            
  //Return type
  new ClassItem(float.class),  
  
  //Parameter types
  new ClassItem(android.content.res.TypedArray.class),            
  new ClassItem(int.class),           
  new ClassItem(float.class),            
  new ClassItem(float.class)      
)
```

## ClassProfile

A `ClassProfile` is comprised of multiple arrays of these profiles, along with a information about super classes, parameters, and implemented interfaces.

Unlike the other profiles, a `ClassProfile` also includes information used to search the dex file for the class, namely:

```
Full Path   // The full path of the class, even if obfuscated. E.g. com.mayulive.a.b.c
Known Path  // The portion of the path expected to remain constant, e.g. com.mayulive
Min Depth   // The depth from knownpath to begin seaching for the class. 0 means within package com.mayulive.
Max Depth   // The absolute search depth. Must be the same or higher than min depth.
```

It is not recommended to manually define a `ClassProfile`. The`ProfileServer` web interface is covered below.

## Loading Classes

With a `ClassProfile` created, you may search a dex file for matching classes. These are available in `ProfileHelpers`.
Searching for a class requires that the dex file is loaded into a `PackageTree`, which separates the individual classes into nested packages and classes.

In android, the dex file is the apk. The below example uses the path provided by the Xposed framework.

```java
//Construct a PackageTree using the apk and the classloader, here provided by Xposed
PackageTree classTree = new PackageTree(lpparam.appInfo.sourceDir, lpparam.classLoader);
```

The heavy lifting is performed by methods in `ProfileSearch`, but `ProfileHelpers` provides static methods for loading profiled classes directly:

```java
ClassProfile exampleProfile = new ClassProfile(); //Blank profile for this example. 
                                                  //Use web interface to generate profiles.
Class classLoadedFromProfile = ProfileHelpers.loadProfiledClass(  exampleProfile, param);
```

## Profile Web Interface

A  `ClassProfile` can get very complex. A simple web-interface is provided to automatically generate these profiles.

The server must be running in a context where the target class is loadable.

It may be run with or without a PackageTree instance. This is generally not necessary unless the target dex file uses unusual characters in class and package names. 
`$` , the character normally used to denote inner and anonymous classes, is in fact a valid character for package and class name, and the server will require a PackageTree instance if they are present elsewhere.
The context must also have the necessary permissions (Internet Access permission) for the web interface to be accessible.

```java
ProfileServer server = new ProfileServer(8282, classLoader, packageTree); //packageTree is nullable
server.start();
```

Once started the web-interface will be accessible at phoneip:8282, and looks like this:
![Web Interface](https://raw.github.com/nordskog/ClassHunter/master/classhunter_webinterface.png)

From here you may select the obfuscated portions of paths that may change, and move resolvable classes to the left to make the generated code a bit prettier.
Once finished you can resubmit the request and you will provided with a copy-pastable profile, in the form of a wall-of-text

## Caching

Comparing a profiles against classes isn't super expensive, but it does slow things down. A simple caching interface is provided that stores the previous results to a file.
When the cache is loaded we compare the similarity score, and if they still match we use the cached resut.

```java
//Set the save/load location of the cache and load it if it exists. 
ProfileCache.setSaveLocation(lpparam.appInfo.dataDir+"/files/CLASSHUNTER_CACHE");
ProfileCache.loadCache();

//Load clasess from profiles
// ...

//Store the results.
ProfileCache.saveCache();
```

## License

Licensed under the MIT licensed. See License.txt for details.

