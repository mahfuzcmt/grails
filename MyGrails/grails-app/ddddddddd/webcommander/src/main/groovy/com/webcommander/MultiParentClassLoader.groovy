package com.webcommander

/**
 * Created by zobair on 17/07/2014.
 */
class MultiParentClassLoader extends GroovyClassLoader {
    List<ClassLoader> parents = []

    public MultiParentClassLoader() {}

    public MultiParentClassLoader(ClassLoader... parents) {
        super(parents[0])
        this.parents = parents as List
        this.parents.remove(0)
        optimizeParents()
    }

    private boolean optimizeParents() {
        List<ClassLoader> optimized = [parent]
        for(ClassLoader loader : parents) {
            boolean found = false
            for(ClassLoader added : optimized) {
                if(added instanceof MultiParentClassLoader ? added.isParent(loader) : classLoadersParentCheck(added, loader)) {
                    found = true;
                    break
                }
            }
            if(!found) {
                optimized.add(loader)
            }
        }
        parents = optimized
        parents.remove(0)
    }

    private boolean classLoadersParentCheck(a, b) {
        ClassLoader cp = a.parent
        while(cp) {
            if(cp == b) {
                return true
            }
            cp = cp.parent
        }
        return false
    }

    private boolean isParent(ClassLoader loader) {
        Closure eq_check = { _loader ->
            if(_loader == loader) {
                return true
            }
            if(_loader instanceof MultiParentClassLoader) {
                return _loader.isParent(loader)
            }
            return classLoadersParentCheck(_loader, loader)
        }
        return eq_check(parent) || parents.find(eq_check) != null
    }

    @Override
    protected synchronized Class<?> findClass(String s) throws ClassNotFoundException {
        try {
            return super.findClass(s)
        } catch (ClassNotFoundException n) {
            for(ClassLoader loader : parents) {
                try {
                    return loader.loadClass(s)
                } catch (ClassNotFoundException n2) {}
            }
            throw n
        }
    }
}
