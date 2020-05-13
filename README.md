# LocationHandler

Steps to use-
1.To use Location library you have to place locationLib.aar file in libs folder of your project.
2.Place following code in project level build file.
      repositories {
        google()
        jcenter()
        flatDir{
            dirs 'libs'
        }
    }
 3. Add implementation(name:"locationLib" ,ext : 'aar') in dependency.
