package houtbecke.rs.workingon;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.util.Modules;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import roboguice.RoboGuice;

/**
 *
 * WorkingOn can be used make configuration choices that are useful during development.
 *
 * It allows you to easily override certain parts of your Guice models, based on setting a Task.
 * When you provide Guice modules they will be automatically overridden or replaced with modules
 * associated with that task.
 *
 * It also hold some static field values that by convention could be used to make your app behave
 * differently, such as the Activity or Fragment that you want to develop on.
 *
 * It provides a utility method for initializing a class (and silently failing if it doesn't exist).
 * This can be used to initialize the static fields from a class that can be outside of SCM (for
 * example by putting it in .gitignore). This way different people on the project can be working
 * with different configurations without getting in each others way.
 *
 */
public class WorkingOn {

    /**
     * The Activity you're working on in your app.
     *
     * Typically the activity opened in your manifest should check this value and forward you there if needed.
     */
    public static Activity activity;

    /**
     * The class of the Fragment you're working on in your app
     *
     * Typically this is checked when loading a fragment into your app.
     *
     */
    public static Class fragmentClass;

    /**
     * The tasks you are currently working on.
     *
     * When you add a task to this list it will be used when loadModules is invoked (but only if the
     * application is built as debuggable in case the onlyOverrideWhenInDebugMode flag is set.
     *
     * This is done by attempting to override (in case the @OverrideModule annotation is present) or
     * replacing the module with one from a subpackage of the same name as this task. Optionally you can
     * give your replacement/overriding module a suffix of the same name as the text.
     *
     * Aside from that any module in the subpackage with a name of only the task will be loaded if present.
     *
     * Since classes typically begin with a capital letter your task should too. When looking for the
     * subpackage a lowercase version of the task is always used.
     *
     * For example if the tasks "Dev" exists, and a module "my.app.module.MyModule" is passed to
     * loadModules, the following modules will be looked for:
     *
     * my.app.module.dev.Dev (if present, loaded seperatly)
     * my.app.module.dev.MyModuleDev (if present replaces or overrides my.app.module.MyModule)
     * my.app.module.dev.MyModule (only checked if previous module does exist, otherwise behaves the same)
     *
     * Module overrides based on task will happen in order as specified in this array. If a later task completely
     * replaces instead of overrides a module both the overridden and replacement module will be present. Be aware
     * this could lead to double bindings which will cause an error.
     */
    public static Set<String> tasks = new LinkedHashSet<String>(0);

    /**
     * The extra modules that should be loaded in addition to the ones passed to loadModules.
     *
     * If the onlyOverrideWhenInDebugMode flag is passed to loadModules and the application is not
     * debuggable the extra modules specified here will not be loaded.
     *
     */
    public static Set<Module> extraModules = new LinkedHashSet<Module>(0);

    /**
     *
     * Indicates if the application is under testing. If it is, the call to initConfigClass
     * will be ignored, letting the test do all configuration itself.
     *
     */
    public static boolean isTesting = false;

    private static void initConfigClass(Class c) {
        if (isTesting) return;
        try {
            c.newInstance();
        } catch (Exception ignore) {}
    }

    /**
     * Instantiates a configuration class. Will only do so when the application is not under test
     * as indicated by isTesting or the System property testClass. If such a property does exist
     * configuration is done from that class by passing it to configureTestTasks.
     *
     * @param name The name class file of the class to instantiate
     */
    public static void initConfigClass(String name) {
        String testClass = System.getProperty("testClass");
        if (testClass != null) {
            isTesting = true;
            try {
                configureTestTasks(Class.forName(testClass));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return;
        }
        if (isTesting) return;
        try {
            initConfigClass(Class.forName(name));
        } catch (ClassNotFoundException ignore) {
        }
    }

    public static boolean isInDebugMode(Context context) {
        return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    protected static Module getModule(Context context, String fullName, Module rootModuleToOverride, Class<? extends Module> rootModuleClass) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class moduleClass = Class.forName(fullName);
        return getModule(context, moduleClass, rootModuleToOverride, rootModuleClass);
    }

    protected static Module getModule(Context context, Class moduleClass, Module moduleToOverride, Class<? extends Module> rootModuleClass) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Module module = null;
        for (Constructor constructor: moduleClass.getConstructors()) {
            Class[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 1 && (parameterTypes[0].equals(boolean.class) || parameterTypes[0].equals(Boolean.class))) {
                module = (Module) constructor.newInstance(isInDebugMode(context));
                break;
            }
        }
        if (module == null)
            module = (Module) moduleClass.newInstance();

        Class<? extends Module> classToOverrideWithModuleClass = classToOverrideFor(moduleClass);
        if (moduleToOverride != null && classToOverrideWithModuleClass != null && classToOverrideWithModuleClass == rootModuleClass)
            return Modules.override(moduleToOverride).with(module);

        if (classToOverrideWithModuleClass != null)
            return Modules.override(getModule(context, classToOverrideWithModuleClass, null, null)).with(module);
        return module;
    }

    protected Module getModule(Context context, String packageName, String className, Module moduleToOverride, Class<? extends Module> rootModuleClass) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
        return getModule(context, packageName+"."+className, moduleToOverride, rootModuleClass);
    }

    /**
     * Add a Module to the supplied list.
     *
     * if moduleFullClassName points to a non-existing class this method will do nothing.
     *
     * @param context Context used for checking if the application is in debug mode.
     * @param moduleList List the Module should be added to or replace the last entry from, if a
     *                   module can be created.
     * @param moduleFullClassName The fully qualified name of the Class
     * @return true if a module was added, false if no module was added
     * @throws IllegalAccessException Thrown if a Class was found but could not be loaded.
     * @throws InstantiationException Thrown if a Class was found but could not instantiated.
     *                                Make sure you have a no-args or single boolean arg constructor.
     * @throws InvocationTargetException Thrown if a Class was found but could not instantiated.
     *                                   Make sure you have a no-args or single boolean arg constructor.
     */
    protected static Module addModule(Context context, Set<Module> moduleList, String moduleFullClassName, Module moduleToOverride, Class<? extends Module> rootModuleClass) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        try {
            Module m = getModule(context, moduleFullClassName, moduleToOverride, rootModuleClass);
            if (m != null) {
                moduleList.add(m);
                return m;
            }
        } catch (ClassNotFoundException ignore) { /* we simply do not add this class if it's not found */}
        return null;
    }


    /**
     * Loads the modules with overrides based on the task field when appropriate.
     *
     * Modules should have either a no fields constructor, or a single boolean argument constructor.
     * This boolean will be set to true in case the app is build as debuggable.
     *
     * @param application Your application. Needed by RoboGuice. If you have no application class you can try casting a Context object.
     * @param onlyOverrideWhenInDebugMode Sets whether to always make overrides, and ad extra modules,
     *                                    or only when the application is built as debuggable.
     *                                    Normally you'd only want overrides during development and this should
     *                                    be set to true.
     *
     * @param moduleClasses Classes of the modules to override and/or load.
     */
    public static void loadModules(Application application, boolean onlyOverrideWhenInDebugMode, Class<? extends Module>... moduleClasses) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        if (onlyOverrideWhenInDebugMode && !isInDebugMode(application) && !isTesting) {
            Module[] modules = new Module[moduleClasses.length + 1];
            modules[0] = RoboGuice.newDefaultRoboModule(application);
            for (int k=0; k < moduleClasses.length; k++) {
                modules[k+1]=getModule(application, moduleClasses[k], null, null);
            }
            RoboGuice.setBaseApplicationInjector(application, Stage.PRODUCTION, modules);
            return;
        }

        Set<Module> modules = new LinkedHashSet<Module>();
        Set<String> packages = new HashSet<String>();
        modules.add(RoboGuice.newDefaultRoboModule(application));
        Map<Class<? extends Module>, Module> rootModuleOverrides = new HashMap<Class<? extends Module>, Module>();

        for (Class moduleClass: moduleClasses) {
            String packageName = moduleClass.getPackage().getName();
            if (!packages.contains(packageName))
                for (String task: tasks)
                    addModule(application, modules, packageName + "." + task.toLowerCase() + "." + task, null, null);

            String className = moduleClass.getSimpleName();

            boolean addedAnyTask = false;
            for (String task: tasks) {

                /*

                - create the potential class name that could replace or override our normal module

                - see what the deepest module is that the module with this class name overrides

                - if it's been overridden before, then pass the module stored in the rootModuleOverrides
                  so that gets overridden instead of the actual root module. Else just create a new module.

                - If a new merged overrides module wa created, remove the old module from the module set.

                - store the new or merged overridden module in the rootModuleOverrides map.

                - if no module existed for the potential class name or it's alternate, just create the original module
                 */

                String potentialClassName = packageName + "." + task.toLowerCase() + "." + className+task;
                Class<? extends Module> rootClass = rootClassToOverrideFor(potentialClassName);
                Module moduleToOverride = null;
                if (rootClass != null)
                    moduleToOverride = rootModuleOverrides.get(rootClass);
                Module addedModule = addModule(application, modules, potentialClassName, moduleToOverride, rootClass);

                if (addedModule == null) {
                    // same as above but for the alternate class name case.
                    potentialClassName = packageName + "." + task.toLowerCase() + "." + className;
                    rootClass = rootClassToOverrideFor(potentialClassName);
                    if (rootClass != null)
                        moduleToOverride = rootModuleOverrides.get(rootClass);

                    addedModule = addModule(application, modules, potentialClassName, moduleToOverride, rootClass);

                }
                if (addedModule != null && rootClass != null) {
                    if (moduleToOverride != null)
                        modules.remove(moduleToOverride);
                    rootModuleOverrides.put(rootClass, addedModule);
                }
                addedAnyTask |= addedModule != null;
            }
            if (!addedAnyTask)
                addModule(application, modules, moduleClass.getName(), null, null);

        }
        modules.addAll(extraModules);
        RoboGuice.setBaseApplicationInjector(application, Stage.PRODUCTION, modules.toArray(new Module[]{}));

    }

    protected static Class<? extends Module> rootClassToOverrideFor(String fullClassName) {
        try {
            return rootClassToOverrideFor((Class<? extends Module>) Class.forName(fullClassName));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    protected static Class<? extends Module> rootClassToOverrideFor(Class<? extends Module> moduleClass) {
        Class<? extends Module> classToOverridesWithModuleClass = classToOverrideFor(moduleClass);
        if (classToOverridesWithModuleClass != null) {
            Class<? extends Module> rootClassOfClassToOverrideWithModuleClass = rootClassToOverrideFor(classToOverridesWithModuleClass);
            return  rootClassOfClassToOverrideWithModuleClass == null ? classToOverridesWithModuleClass : rootClassOfClassToOverrideWithModuleClass;
        }
        return null;
    }

    protected static Class<? extends Module> classToOverrideFor(Class<? extends Module> moduleClass) {
        if (moduleClass.isAnnotationPresent(OverridesModule.class)) {
            OverridesModule annotation = (OverridesModule) moduleClass.getAnnotation(OverridesModule.class); // android studio cast
            Class<? extends Module> classThatOverrides = annotation.value();
            if (classThatOverrides == Module.class) { // default value
                return null; // TODO implement default behaviour of looking at the parent package
            }
            return classThatOverrides;
        }
        return null;
    }


    public static Class<?> configureTestTasks(Class<?> testClass) {

        isTesting = true;
        WorkingOnTasks annotatedTasks = testClass.getAnnotation(WorkingOnTasks.class);
        if (annotatedTasks != null)
            for (String task: annotatedTasks.value())
                tasks.add(task);
        return testClass;
    }
}
