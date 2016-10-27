/*
 * Copyright 2009-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.core.groovy.tests.model;

import java.util.Arrays;
import java.util.HashMap;

import junit.framework.Test;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.ModuleNodeMapper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.tests.util.Util;

/**
 * @author Andrew Eisenberg
 * @created Jun 4, 2009
 */
public class GroovyCompilationUnitTests extends AbstractGroovyTypeRootTests {

    public static Test suite() {
        return buildTestSuite(GroovyCompilationUnitTests.class);
    }

    public GroovyCompilationUnitTests(String name) {
        super(name);
    }

    public void testCreateJavaCompilationUnit() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addClass(root, "p1", "Hello",
            "package p1;\n"+
            "public class Hello {\n"+
            "   public static void main(String[] args) {\n"+
            "      System.out.println(\"Hello world\");\n"+
            "   }\n"+
            "}\n"
            );

        IFile javaFile = getFile("Project/src/p1/Hello.java");
        ICompilationUnit unit = JavaCore.createCompilationUnitFrom(javaFile);
        assertTrue("CompilationUnit " + javaFile + " should exist.", unit.exists());
        assertFalse("CompilationUnit " + javaFile + " should not be a Groovy compilation unit.", unit instanceof GroovyCompilationUnit);
    }

    public void testCreateGroovyCompilationUnit() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        ICompilationUnit unit = JavaCore.createCompilationUnitFrom(groovyFile);
        assertTrue("CompilationUnit " + groovyFile + " should exist.", unit.exists());
        assertTrue("CompilationUnit " + groovyFile + " should be a Groovy compilation unit.", unit instanceof GroovyCompilationUnit);
    }

    public void testGetModuleNode_1() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        GroovyCompilationUnit unit1 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit1.becomeWorkingCopy(null);
        ModuleNode node1 = unit1.getModuleNode();
        ModuleNode node2 = unit1.getModuleNode();
        assertTrue("Multiple calls to getModuleNode should return the same object if nothing has changed underneath", node1 == node2);
        unit1.discardWorkingCopy();
    }

    public void testGetModuleNode_2() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        GroovyCompilationUnit unit1 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit1.becomeWorkingCopy(null);
        ModuleNode node1 = unit1.getModuleNode();
        GroovyCompilationUnit unit2 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit2.becomeWorkingCopy(null);
        ModuleNode node2 = unit2.getModuleNode();
        assertTrue("Multiple calls to getModuleNode should return the same object if nothing has changed underneath", node1 == node2);
        unit1.discardWorkingCopy();
        unit2.discardWorkingCopy();
    }

    public void testGetModuleNode_3() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        GroovyCompilationUnit unit1 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit1.becomeWorkingCopy(null);
        ModuleNode node1 = unit1.getModuleNode();
        unit1.reconcile(AST.JLS3, true, unit1.owner, null);
        ModuleNode node2 = unit1.getModuleNode();
        assertTrue("Multiple calls to getModuleNode should not return the same object after a call to reconcile with problem detection enabled", node1 != node2);
        unit1.discardWorkingCopy();
    }

    public void testGetModuleNode_4() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        GroovyCompilationUnit unit1 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit1.becomeWorkingCopy(null);
        ModuleNode node1 = unit1.getModuleNode();
        unit1.makeConsistent(AST.JLS3, true, ICompilationUnit.FORCE_PROBLEM_DETECTION, new HashMap(), null);
        ModuleNode node2 = unit1.getModuleNode();
        assertTrue("Multiple calls to getModuleNode should return the same object if nothing has changed underneath", node1 == node2);
        unit1.discardWorkingCopy();
    }

    public void testGetModuleNode_5() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        GroovyCompilationUnit unit1 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit1.becomeWorkingCopy(null);
        ModuleNode node1 = unit1.getModuleNode();
        unit1.getBuffer().append(" ");
        ModuleNode node2 = unit1.getModuleNode();
        assertTrue("Multiple calls to getModuleNode should return different objects if something has changed underneath", node1 != node2);
        unit1.discardWorkingCopy();
    }

    public void testGetModuleNode_6() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        GroovyCompilationUnit unit1 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit1.becomeWorkingCopy(null);
        ModuleNode node1 = unit1.getModuleNode();
        GroovyCompilationUnit unit2 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit2.becomeWorkingCopy(null);
        ModuleNode node2 = unit2.getModuleNode();
        unit1.getBuffer().append(" ");
        ModuleNode node3 = unit1.getModuleNode();
        ModuleNode node4 = unit2.getModuleNode();
        unit1.discardWorkingCopy();
        unit2.discardWorkingCopy();

        assertTrue("Multiple calls to getModuleNode should return the same object if nothing has changed underneath", node1 == node2);
        assertTrue("Multiple calls to getModuleNode should return different objects if something has changed underneath", node1 != node3);
        assertTrue("Multiple calls to getModuleNode should return the same object if nothing has changed underneath", node3 == node4);
    }

    public void testGetModuleNode_7() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        GroovyCompilationUnit unit1 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit1.becomeWorkingCopy(null);
        unit1.getModuleNode();
        GroovyCompilationUnit unit2 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit2.becomeWorkingCopy(null);
        unit2.getModuleNode();
        unit1.getBuffer().append(" ");
        unit1.getModuleNode();
        unit2.getModuleNode();

        unit1.discardWorkingCopy();
        unit2.discardWorkingCopy();

        assertTrue("ModuleNodeMapper should be empty when there are no working copies", getInitialModuleNodeMapperSize() >= ModuleNodeMapper.size());
    }

    public void testGetModuleNode_8() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        GroovyCompilationUnit unit1 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        ModuleNode node1 = unit1.getModuleNode();
        ModuleNode node2 = unit1.getModuleNode();
        assertFalse("Multiple calls to getModuleNode should return the different objects if unit is not a working copy", node1 == node2);
    }

    public void testGetModuleNode_9() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        GroovyCompilationUnit unit1 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit1.becomeWorkingCopy(null);
        ModuleNode node1 = unit1.getModuleNode();
        unit1.reconcile(true, null);
        ModuleNode node2 = unit1.getModuleNode();
        unit1.discardWorkingCopy();
        assertFalse("Multiple calls to getModuleNode should return the different objects after a call to reconcile with force problem detection", node1 == node2);
    }

    public void testGetModuleNode_10() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        GroovyCompilationUnit unit1 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit1.becomeWorkingCopy(null);
        ModuleNode node1 = unit1.getModuleNode();
        unit1.reconcile(false, null);
        ModuleNode node2 = unit1.getModuleNode();
        unit1.discardWorkingCopy();
        assertTrue("Multiple calls to getModuleNode should return the same object after a call to reconcile with no force problem detection", node1 == node2);
    }

    public void testGetNewModuleNode() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        GroovyCompilationUnit unit1 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        ModuleNode module1 = unit1.getModuleNode();
        ModuleNode module2 = unit1.getNewModuleInfo().module;
        assertTrue("getNewModuleNode() should have forced creation of a new module node", module1 != module2);
    }

    public void testMarkerAnnotation_1() throws Exception {
        IPath root = createAnnotationGroovyProject();
        env.addGroovyClass(root, "p", "X",
                "package p;\n" +
                "@Anno2\n"+
                "public class X {\n" +
                "  public int foo = 5\n"+
                "  public static void main(String[]argv) {\n"+
                "    print \"success\"\n"+
                "  }\n"+
                "}\n"
            );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        IFile file = getFile("Project/src/p/X.groovy");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
        IType type = unit.getType("X");
        assertEquals("Should have one annotation on type X", 1, type.getAnnotations().length);
        IAnnotation annotation = type.getAnnotations()[0];
        assertMarkerAnnotation(annotation, "Anno2");
    }

    public void testMarkerAnnotation_2() throws Exception {
        IPath root = createAnnotationGroovyProject();
        env.addGroovyClass(root, "p", "X",
                "package p;\n" +
                "public class X {\n" +
                "  @Anno2\n"+
                "  public int foo = 5\n"+
                "  public static void m() {\n"+
                "    print \"success\"\n"+
                "  }\n"+
                "}\n"
            );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        IFile file = getFile("Project/src/p/X.groovy");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
        IType type = unit.getType("X");
        IField field = type.getField("foo");
        assertEquals("Should have one annotation on field foo", 1, field.getAnnotations().length);
        IAnnotation annotation = field.getAnnotations()[0];
        assertMarkerAnnotation(annotation, "Anno2");
    }

    public void testMarkerAnnotation_3() throws Exception {
        IPath root = createAnnotationGroovyProject();
        env.addGroovyClass(root, "p", "X",
                "package p;\n" +
                "public class X {\n" +
                "  public int foo = 5\n"+
                "  @Anno2\n"+
                "  public static void m() {\n"+
                "    print \"success\"\n"+
                "  }\n"+
                "}\n"
            );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        IFile file = getFile("Project/src/p/X.groovy");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
        IType type = unit.getType("X");
        IMethod method = type.getMethod("m", new String[0]);
        assertEquals("Should have one annotation on method main()", 1, method.getAnnotations().length);
        IAnnotation annotation = method.getAnnotations()[0];
        assertMarkerAnnotation(annotation, "Anno2");
    }

    public void testMarkerAnnotation_4() throws Exception {
        IPath root = createAnnotationGroovyProject();
        env.addGroovyClass(root, "p", "X",
                "package p;\n" +
                "@p.Anno2\n"+
                "public class X {\n" +
                "  public int foo = 5\n"+
                "  public static void main(String[]argv) {\n"+
                "    print \"success\"\n"+
                "  }\n"+
                "}\n"
            );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        IFile file = getFile("Project/src/p/X.groovy");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
        IType type = unit.getType("X");
        assertEquals("Should have one annotation on type X", 1, type.getAnnotations().length);
        IAnnotation annotation = type.getAnnotations()[0];
        assertMarkerAnnotation(annotation, "p.Anno2");
    }

    public void testMarkerAnnotation_5() throws Exception {
        IPath root = createAnnotationGroovyProject();
        env.addGroovyClass(root, "p", "X",
                "package p;\n" +
                "public class X {\n" +
                "  @p.Anno2\n"+
                "  public int foo = 5\n"+
                "  public static void m() {\n"+
                "    print \"success\"\n"+
                "  }\n"+
                "}\n"
            );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        IFile file = getFile("Project/src/p/X.groovy");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
        IType type = unit.getType("X");
        IField field = type.getField("foo");
        assertEquals("Should have one annotation on field foo", 1, field.getAnnotations().length);
        IAnnotation annotation = field.getAnnotations()[0];
        assertMarkerAnnotation(annotation, "p.Anno2");
    }

    public void testMarkerAnnotation_6() throws Exception {
        IPath root = createAnnotationGroovyProject();
        env.addGroovyClass(root, "p", "X",
                "package p;\n" +
                "public class X {\n" +
                "  public int foo = 5\n"+
                "  @p.Anno2\n"+
                "  public static void m() {\n"+
                "    print \"success\"\n"+
                "  }\n"+
                "}\n"
            );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        IFile file = getFile("Project/src/p/X.groovy");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
        IType type = unit.getType("X");
        IMethod method = type.getMethod("m", new String[0]);
        assertEquals("Should have one annotation on method main()", 1, method.getAnnotations().length);
        IAnnotation annotation = method.getAnnotations()[0];
        assertMarkerAnnotation(annotation, "p.Anno2");
    }

    private void assertMarkerAnnotation(IAnnotation annotation, String expectedName)
            throws JavaModelException {
        assertEquals("Wrong name for annotation", expectedName, annotation.getElementName());
        assertEquals("Should be a marker annotation, but had member-value pairs",
                0, annotation.getMemberValuePairs().length);
    }

    public void testSingleMemberAnnotation_1() throws Exception {
        IPath root = createAnnotationGroovyProject();
        env.addGroovyClass(root, "p", "X",
                "package p;\n" +
                "@Anno1(Target.class)\n"+
                "public class X {\n" +
                "  public int foo = 5\n"+
                "  public static void main(String[]argv) {\n"+
                "    print \"success\"\n"+
                "  }\n"+
                "}\n"
            );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        IFile file = getFile("Project/src/p/X.groovy");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
        IType type = unit.getType("X");
        assertEquals("Should have one annotation on type X", 1, type.getAnnotations().length);
        assertSingleMemberAnnotation(type, "Target");
    }

    public void testSingleMemberAnnotation_2() throws Exception {
        IPath root = createAnnotationGroovyProject();
        env.addGroovyClass(root, "p", "X",
                "package p;\n" +
                "public class X {\n" +
                "  @Anno1(Target.class)\n"+
                "  public int foo = 5\n"+
                "  public static void main(String[]argv) {\n"+
                "    print \"success\"\n"+
                "  }\n"+
                "}\n"
            );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        IFile file = getFile("Project/src/p/X.groovy");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
        IType type = unit.getType("X");
        IField field = type.getField("foo");
        assertEquals("Should have one annotation on field foo", 1, field.getAnnotations().length);
        assertSingleMemberAnnotation(field, "Target");
    }

    public void testSingleMemberAnnotation_3() throws Exception {
        IPath root = createAnnotationGroovyProject();
        env.addGroovyClass(root, "p", "X",
                "package p;\n" +
                "public class X {\n" +
                "  public int foo = 5\n"+
                "  @Anno1(Target.class)\n"+
                "  public static void m() {\n"+
                "    print \"success\"\n"+
                "  }\n"+
                "}\n"
            );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        IFile file = getFile("Project/src/p/X.groovy");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
        IType type = unit.getType("X");
        IMethod method = type.getMethod("m", new String[0]);
        assertEquals("Should have one annotation on field m", 1, method.getAnnotations().length);
        assertSingleMemberAnnotation(method, "Target");
    }

    public void testSingleMemberAnnotation_4() throws Exception {
        IPath root = createAnnotationGroovyProject();
        env.addGroovyClass(root, "p", "X",
                "package p;\n" +
                "@Anno1(p.Target.class)\n"+
                "public class X {\n" +
                "  public int foo = 5\n"+
                "  public static void main(String[]argv) {\n"+
                "    print \"success\"\n"+
                "  }\n"+
                "}\n"
            );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        IFile file = getFile("Project/src/p/X.groovy");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
        IType type = unit.getType("X");
        assertEquals("Should have one annotation on type X", 1, type.getAnnotations().length);
        assertSingleMemberAnnotation(type, "p.Target");
    }

    public void testSingleMemberAnnotation_5() throws Exception {
        IPath root = createAnnotationGroovyProject();
        env.addGroovyClass(root, "p", "X",
                "package p;\n" +
                "public class X {\n" +
                "  @Anno1(p.Target.class)\n"+
                "  public int foo = 5\n"+
                "  public static void main(String[]argv) {\n"+
                "    print \"success\"\n"+
                "  }\n"+
                "}\n"
            );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        IFile file = getFile("Project/src/p/X.groovy");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
        IType type = unit.getType("X");
        IField field = type.getField("foo");
        assertEquals("Should have one annotation on field foo", 1, field.getAnnotations().length);
        assertSingleMemberAnnotation(field, "p.Target");
    }

    public void testSingleMemberAnnotation_6() throws Exception {
        IPath root = createAnnotationGroovyProject();
        env.addGroovyClass(root, "p", "X",
                "package p;\n" +
                "public class X {\n" +
                "  public int foo = 5\n"+
                "  @Anno1(p.Target.class)\n"+
                "  public static void m() {\n"+
                "    print \"success\"\n"+
                "  }\n"+
                "}\n"
            );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        IFile file = getFile("Project/src/p/X.groovy");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
        IType type = unit.getType("X");
        IMethod method = type.getMethod("m", new String[0]);
        assertEquals("Should have one annotation on field m", 1, method.getAnnotations().length);
        assertSingleMemberAnnotation(method, "p.Target");
    }

    /**
     * Only marker annotations or SingleMember annotations whose
     * member is a class literal are copied over.
     * This class tests that these other kinds of annotaitons are not copied over
     */
    public void testSingleMemberAnnotation_7() throws Exception {
        IPath root = createAnnotationGroovyProject();
        env.addGroovyClass(root, "p", "X",
                "package p;\n" +
                "@Anno3(\"Hello\")\n"+
                "@Anno4(value1 = Target.class)\n"+
                "public class X {\n" +
                "  public int foo = 5\n"+
                "  public static void m() {\n"+
                "    print \"success\"\n"+
                "  }\n"+
                "}\n"
            );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        IFile file = getFile("Project/src/p/X.groovy");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
        IType type = unit.getType("X");
        assertEquals("These annotations should not be included in the model", 0, type.getAnnotations().length);
    }

    public void testAnonymousInner1() throws Exception {
        IPath root = createAnnotationGroovyProject();
        env.addGroovyClass(root, "p", "X",
                "package p;\n" +
                "def foo = new Runnable() { void run() { } }"
            );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        IFile file = getFile("Project/src/p/X.groovy");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
        unit.becomeWorkingCopy(new NullProgressMonitor());
        try {
            IType type = unit.getType("X");
            IMethod method = type.getMethod("run", new String[0]);
            IJavaElement[] children = method.getChildren();
            assertEquals("Expecting exactly one child, but found: " + Arrays.toString(children), 1, children.length);
            IType anonType = (IType) children[0];
            assertEquals("Anon type should have empty name", "", anonType.getElementName());
            children = anonType.getChildren();
            assertEquals("Expecting exactly one child, but found: " + Arrays.toString(children), 1, children.length);
            assertEquals("run", children[0].getElementName());
        } finally {
            unit.discardWorkingCopy();
        }
    }

    public void testAnonymousInner2() throws Exception {
        IPath root = createAnnotationGroovyProject();
        env.addGroovyClass(root, "p", "X",
                "package p;\n" +
                "def foo = new Runnable() { void run() { } }\n" +
                "foo = new Runnable() { void run() { }\n  void other() { } }"
            );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        IFile file = getFile("Project/src/p/X.groovy");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
        unit.becomeWorkingCopy(new NullProgressMonitor());
        try {
            IType type = unit.getType("X");
            IMethod method = type.getMethod("run", new String[0]);
            IJavaElement[] children = method.getChildren();
            assertEquals("Expecting exactly one child, but found: " + Arrays.toString(children), 2, children.length);

            for (int i = 0; i < 2; i++) {
                IType anonType = (IType) children[i];
                assertEquals("Anon type should have empty name", "", anonType.getElementName());
                 IJavaElement[] anonChildren = anonType.getChildren();
                assertEquals("Expecting exactly one child, but found: " + Arrays.toString(anonChildren), i+1, anonChildren.length);
                assertEquals("run", anonChildren[0].getElementName());
                if (i == 1) {
                    assertEquals("other", anonChildren[1].getElementName());
                }
            }
        } finally {
            unit.discardWorkingCopy();
        }
    }

    public void testAnonymousInner3() throws Exception {
        IPath root = createAnnotationGroovyProject();
        env.addGroovyClass(root, "p", "X",
                "package p;\n" +
                "class Foo {\n" +
                "  def run() {\n" +
                "    def foo = new Runnable() { void run() { } }\n" +
                "  }\n" +
                "}"
            );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        IFile file = getFile("Project/src/p/X.groovy");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
        unit.becomeWorkingCopy(new NullProgressMonitor());
        try {
            IType type = unit.getType("Foo");
            IMethod method = type.getMethod("run", new String[0]);
            IJavaElement[] children = method.getChildren();
            assertEquals("Expecting exactly one child, but found: " + Arrays.toString(children), 1, children.length);
            IType anonType = (IType) children[0];
            assertEquals("Anon type should have empty name", "", anonType.getElementName());
            children = anonType.getChildren();
            assertEquals("Expecting exactly one child, but found: " + Arrays.toString(children), 1, children.length);
            assertEquals("run", children[0].getElementName());
        } finally {
            unit.discardWorkingCopy();
        }
    }

    public void testAnonymousInner4() throws Exception {
        IPath root = createAnnotationGroovyProject();
        env.addGroovyClass(root, "p", "X",
                "package p;\n" +
                "class Foo {\n" +
                "  def run() {\n" +
                "    def foo = new Runnable() { void run() { } }\n" +
                "    foo = new Runnable() { void run() { }\n  void other() { } }" +
                "  }\n" +
                "}"
            );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        IFile file = getFile("Project/src/p/X.groovy");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
        unit.becomeWorkingCopy(new NullProgressMonitor());
        try {
            IType type = unit.getType("Foo");
            IMethod method = type.getMethod("run", new String[0]);
            IJavaElement[] children = method.getChildren();
            assertEquals("Expecting exactly one child, but found: " + Arrays.toString(children), 2, children.length);

            for (int i = 0; i < 2; i++) {
                IType anonType = (IType) children[i];
                assertEquals("Anon type should have empty name", "", anonType.getElementName());
                IJavaElement[] innerChildren = anonType.getChildren();
                assertEquals("Expecting exactly one child, but found: " + Arrays.toString(children), i+1, innerChildren.length);
                assertEquals("run", innerChildren[0].getElementName());
                if (i == 1) {
                    assertEquals("other", innerChildren[1].getElementName());
                }
            }
        } finally {
            unit.discardWorkingCopy();
        }
    }

    // FIXADE TODO: anon inner classes not properly structured on field initializers
    public void testAnonymousInner5() throws Exception {
        IPath root = createAnnotationGroovyProject();
        env.addGroovyClass(root, "p", "X",
                "package p;\n" +
                "class Foo {\n" +
                "  def foo = new Runnable() { void run() { } }\n" +
                "}"
            );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        IFile file = getFile("Project/src/p/X.groovy");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
        unit.becomeWorkingCopy(new NullProgressMonitor());
        try {
            IType type = unit.getType("Foo");
            IField field = type.getField("foo");
            assertEquals(0, field.getChildren().length);
            IType anon = type.getType("1");
            assertTrue("Anon inner type should exist as a member type", anon.exists());
        } finally {
            unit.discardWorkingCopy();
        }
    }

    // Test that classes in a script are not treated as anon inners
    public void testAnonInner6() throws Exception {
        IPath root = createAnnotationGroovyProject();
        env.addGroovyClass(root, "p", "X",
                "package p;\n" +
                "class Other{ }\n" +
                "def foo = new Runnable() { void run() { } }\n" +
                "class Other2 { }"
            );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        IFile file = getFile("Project/src/p/X.groovy");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
        unit.becomeWorkingCopy(new NullProgressMonitor());
        try {
            unit.getType("Other").exists();
            unit.getType("Other2").exists();
            IType type = unit.getType("X");
            IMethod method = type.getMethod("run", new String[0]);
            IJavaElement[] children = method.getChildren();
            assertEquals("Expecting exactly one child, but found: " + Arrays.toString(children), 1, children.length);
            IType anonType = (IType) children[0];
            assertEquals("Anon type should have empty name", "", anonType.getElementName());
            children = anonType.getChildren();
            assertEquals("Expecting exactly one child, but found: " + Arrays.toString(children), 1, children.length);
            assertEquals("run", children[0].getElementName());
        } finally {
            unit.discardWorkingCopy();
        }
    }

    private void assertSingleMemberAnnotation(IAnnotatable type, String expectedName) throws JavaModelException {
        IAnnotation annotation = type.getAnnotations()[0];
        assertEquals("Wrong name for annotation", "Anno1", annotation.getElementName());
        assertEquals("Should be a single member annotation, but did not have exactly one member-value pair", 1, annotation.getMemberValuePairs().length);
        IMemberValuePair mvp = annotation.getMemberValuePairs()[0];
        assertEquals("value", mvp.getMemberName());
        assertEquals(IMemberValuePair.K_CLASS, mvp.getValueKind());
        assertEquals(expectedName, mvp.getValue());
    }
}
