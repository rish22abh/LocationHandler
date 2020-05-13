# LocationHandler

Steps to use-<br/>
1.To use Location library you have to place locationLib.aar file in libs folder of your project.<br/>
2.Place following code in project level build file.<br />
      repositories {<br />
        google()<br />
        jcenter()<br />
        flatDir{<br />
            dirs 'libs'<br />
        }<br />
    }<br />
 3. Add implementation(name:"locationLib" ,ext : 'aar') in dependency.<br />
